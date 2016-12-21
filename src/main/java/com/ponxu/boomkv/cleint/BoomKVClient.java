package com.ponxu.boomkv.cleint;

import com.ponxu.boomkv.utils.ByteUtils;
import com.ponxu.boomkv.utils.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author ponxu
 * @date 2016-12-18
 */
public class BoomKVClient implements Closeable {
    private String host;
    private int port;
    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public BoomKVClient(String host, int port) {
        this.host = host;
        this.port = port;
        connect();
    }

    private void connect() {
        try {
            socket = new Socket(host, port);
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            socket.setReuseAddress(true);

            out = socket.getOutputStream();
            in = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    public byte[] exec(byte cmd, byte[] rq) {
        byte[] tmp = null;
        try {
            if (socket == null) {
                connect();
            }
            out.write(cmd);
            out.write(ByteUtils.int2Bytes(rq.length));
            out.write(rq);

            // read length of response
            tmp = new byte[4];
            in.read(tmp);
            int len = ByteUtils.bytes2Int(tmp);

            // TODO read response
            if (len > 0) {
                tmp = new byte[len];
                in.read(tmp);
            } else {
                tmp = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }

        return tmp;
    }

    @Override
    public void close() {
        IOUtils.closeQuiet(socket);
        socket = null;
        out = null;
        in = null;
    }
}
