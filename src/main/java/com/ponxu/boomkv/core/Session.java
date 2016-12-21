package com.ponxu.boomkv.core;

import com.ponxu.boomkv.utils.ByteUtils;
import com.ponxu.boomkv.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Map;

import static com.ponxu.boomkv.core.Constants.*;

/**
 * cmd(1) + len(4) + rq(len)
 *
 * @author ponxu
 * @date 2016-12-18
 */
public class Session {
    private static final Logger LOG = LoggerFactory.getLogger(Session.class);

    private static final int STATUS_READ_CMD = 0;
    private static final int STATUS_READ_LEN = 1;
    private static final int STATUS_READ_RQ = 2;
    private static final int STATUS_WRITE_RS_BUFFER = 3;
    private static final int STATUS_WRITE_RS_FILE = 4;

    // for network
    private ByteBuffer buffer;
    private Selector sel;
    private SocketChannel sc;
    private NameSpace ns;
    private Server server;
    private boolean isClosed;
    private boolean isReading;

    // for process
    private int state = STATUS_READ_CMD;
    private byte cmd = -1;
    private int len = -1;
    private Map<Byte, Cmd> cmds;
    private String password = "xxx";
    private boolean isAuthed;

    // for output file
    private FileChannel sendfile;
    private long totalLength = -1;
    private long fileOffset = -1;
    private long sendedLength = -1;

    public Session(Selector sel, SocketChannel sc, NameSpace ns, Server server) {
        this.buffer = ByteBuffer.allocate(BUFF_SIZE);
        this.sel = sel;
        this.sc = sc;
        this.ns = ns;
        this.server = server;
        this.isClosed = false;
        this.isReading = true;
        this.isAuthed = false;
        initCmds();
    }

    private void initCmds() {
        this.cmds = new HashMap<>();
        // add cmd here
        cmds.put(CMD_GET, new GetCmd());
        cmds.put(CMD_AUTH, new AuthCmd());
        cmds.put(CMD_INFO, new InfoCmd());
        cmds.put(CMD_RELOAD, new ReloadCmd());
        cmds.put(CMD_SHUTDOWN, new ShutdownCmd());
    }

    public void doRead() throws IOException {
        if (state == STATUS_WRITE_RS_BUFFER || state == STATUS_WRITE_RS_FILE) {
            LOG.warn("pre-cmd is processing");
            closeSocket();
            return;
        }

        if (state == STATUS_READ_CMD && !isClosed) readCmd();
        if (state == STATUS_READ_LEN && !isClosed) readLen();
        if (state == STATUS_READ_RQ && !isClosed) readRQ();
    }

    private void readCmd() throws IOException {
        byte[] temp = readLimit(1);
        if (temp != null) {
            cmd = temp[0];
            state = STATUS_READ_LEN;
            LOG.debug("cmd: {}", cmd);

            if (!cmds.keySet().contains(cmd)) {
                closeSocket();
                LOG.warn("unsupport cmd {}", cmd);
            }
        }
    }

    private void readLen() throws IOException {
        byte[] temp = readLimit(4);
        if (temp != null) {
            len = ByteUtils.bytes2Int(temp);
            state = STATUS_READ_RQ;
            LOG.debug("rq len: {}", len);
        }
    }

    private void readRQ() throws IOException {
        byte[] temp = readLimit(len);
        if (temp != null) {
            state = STATUS_WRITE_RS_BUFFER;

            Cmd c = cmds.get(cmd);
            if (c != null) {
                LOG.debug("exec:{}", c.getClass().getSimpleName());
                c.run(temp);
            } else {
                closeSocket();
                LOG.warn("unsupport cmd {}", cmd);
            }
        }
    }


    private byte[] readLimit(int limit) throws IOException {
        byte[] rs = null;
        if (buffer.limit() != limit) {
            buffer.limit(limit);
        }

        int l = sc.read(buffer);
        if (l == -1) {
            closeSocket();
            return rs;
        }

        if (!buffer.hasRemaining()) {
            rs = new byte[limit];
            buffer.flip();
            buffer.get(rs);
            buffer.clear();
        }

        return rs;
    }

    private void closeSocket() {
        IOUtils.closeQuiet(sc);
        isClosed = true;
        LOG.info("{} is disconnected", sc.socket());
    }

    public void doWrite() throws IOException {
        switch (state) {
            case STATUS_WRITE_RS_BUFFER:
                outputBuffer();
                break;
            case STATUS_WRITE_RS_FILE:
                outputFile();
                break;
        }
    }

    private void outputBuffer() throws IOException {
        sc.write(buffer);
        if (!buffer.hasRemaining()) {
            if (sendfile != null) {
                state = STATUS_WRITE_RS_FILE;
                outputFile();
            } else {
                state = STATUS_READ_CMD;
                waitRead();
            }
            buffer.clear();
        } else {
            waitWrite();
        }
    }

    private void outputFile() throws IOException {
        long l = sendfile.transferTo(fileOffset, totalLength - sendedLength, sc);
        sendedLength += l;
        fileOffset += l;

        if (sendedLength == totalLength) {
            state = STATUS_READ_CMD;
            waitRead();

            sendfile = null;
            sendedLength = totalLength = fileOffset = -1;
        } else {
            waitWrite();
        }
    }

    private void waitRead() throws ClosedChannelException {
        if (!isReading) {
            sc.register(sel, SelectionKey.OP_READ, this);
            isReading = true;
        }
    }

    private void waitWrite() throws ClosedChannelException {
        if (isReading) {
            sc.register(sel, SelectionKey.OP_WRITE, this);
            isReading = false;
        }
    }

    private void prepareBuffer(byte[] data) {
        if (data != null && data.length > 0) {
            buffer.put(ByteUtils.int2Bytes(data.length));
            buffer.put(data);
        } else {
            buffer.put(ByteUtils.int2Bytes(-1));
        }
        buffer.flip();
    }

    private boolean checkCmdAuth() throws IOException {
        if (!isAuthed) {
            prepareBuffer(RS_AUTH);
            outputBuffer();
            return false;
        }
        return true;
    }

    private interface Cmd {
        void run(byte[] rq) throws IOException;
    }

    private class GetCmd implements Cmd {
        @Override
        public void run(byte[] rq) throws IOException {
            String k = new String(rq);
            LOG.debug("get key: {}", k);

            Position pos = ns.get(k);
            if (pos != null) {
                sendfile = ns.getFile(pos.getDatafile());
                fileOffset = pos.getOffset();
                totalLength = pos.getLength();
                sendedLength = 0;
            }

            byte[] temp = ByteUtils.int2Bytes((int) totalLength);
            LOG.debug("totalLength: {}", totalLength);
            buffer.put(temp);
            buffer.flip();

            outputBuffer();
        }
    }

    private class AuthCmd implements Cmd {
        @Override
        public void run(byte[] rq) throws IOException {
            String inputPassword = new String(rq);
            if (password.equals(inputPassword)) {
                isAuthed = true;
                prepareBuffer(RS_OK);
            } else {
                prepareBuffer(RS_FAIL);
            }
            outputBuffer();
        }
    }

    private class InfoCmd implements Cmd {
        @Override
        public void run(byte[] rq) throws IOException {
            if (checkCmdAuth()) {
                String info = String.format("keys:%d slots:%d", ns.getKeyCount(), ns.getSlotCount());
                prepareBuffer(info.getBytes());
                outputBuffer();
            }
        }
    }

    private class ReloadCmd implements Cmd {
        @Override
        public void run(byte[] rq) throws IOException {
            if (checkCmdAuth()) {
                // <nspath> [sc [ib]]
                String[] parts = new String(rq).split(" ");
                String nspath = parts[0];
                int sc = parts.length >= 2 ? Integer.parseInt(parts[1]) : DEFAULT_SC;
                String ib = parts.length >= 3 ? parts[2] : DEFAULT_IB;

                // TODO async
                server.loadNameSpace(nspath, sc, ib);
                prepareBuffer(RS_OK);
                outputBuffer();
            }
        }
    }

    private class ShutdownCmd implements Cmd {
        @Override
        public void run(byte[] rq) throws IOException {
            if (checkCmdAuth()) {
                server.stop();
                prepareBuffer(RS_OK);
                outputBuffer();
            }
        }
    }
}
