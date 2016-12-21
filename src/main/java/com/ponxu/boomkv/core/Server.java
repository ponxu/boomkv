package com.ponxu.boomkv.core;

import com.ponxu.boomkv.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.ponxu.boomkv.core.Constants.WORKER_NAME;

/**
 * 1. load namespce
 * 2. handle network
 *
 * @author ponxu
 * @date 2016-12-18
 */
public class Server implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    // network
    private String bind;
    private int port;
    private boolean isRunning;
    private ServerSocketChannel ssc;
    private Selector sel;
    private Thread worker;

    // namespace
    private String nspath;
    private String ibclass;
    private int sc;
    private NameSpace ns;
    private ReentrantReadWriteLock lock;

    public Server(String bind, int port, String nspath, int sc, String ibclass) {
        this.bind = bind;
        this.port = port;

        this.nspath = nspath;
        this.ibclass = ibclass;
        this.sc = sc;
        this.lock = new ReentrantReadWriteLock();
    }

    public void start() {
        try {
            loadNameSpace(nspath, sc, ibclass);
            startNetwork();
            LOG.info("server is started");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private synchronized void startNetwork() throws IOException {
        if (!isRunning) {
            ssc = ServerSocketChannel.open();
            ssc.bind(new InetSocketAddress(bind, port));
            ssc.configureBlocking(false);

            sel = Selector.open();
            ssc.register(sel, SelectionKey.OP_ACCEPT);

            isRunning = true;
            worker = new Thread(this, WORKER_NAME);
            worker.start();

            LOG.info("network is listen on {}:{}", bind, port);
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                int n = sel.select();
                if (n <= 0) continue;
            } catch (IOException e) {
                continue;
            }

            Iterator<SelectionKey> itr = sel.selectedKeys().iterator();
            while (itr.hasNext()) {
                SelectionKey key = itr.next();
                itr.remove();

                if (!key.isValid()) continue;
                try {
                    if (key.isAcceptable()) doAccept(key);
                    else if (key.isReadable()) doRead(key);
                    else if (key.isWritable()) doWrite(key);
                } catch (Throwable e) {
                    LOG.error("occr error, while handle network-io:" + e.getMessage(), e);
                }
            }
        }
    }

    private void doAccept(SelectionKey key) throws IOException {
        SocketChannel sc = ssc.accept();
        sc.configureBlocking(false);
        sc.socket().setTcpNoDelay(true);
        sc.socket().setKeepAlive(true);
        sc.socket().setReuseAddress(true);

        sc.register(sel, SelectionKey.OP_READ, new Session(sel, sc, ns, this));
        LOG.info("{} is connected", sc.socket());
    }

    private void doRead(SelectionKey key) throws IOException {
        Session session = (Session) key.attachment();
        session.doRead();
    }

    private void doWrite(SelectionKey key) throws IOException {
        Session session = (Session) key.attachment();
        session.doWrite();
    }

    private synchronized void stopNetwork() {
        if (isRunning) {
            isRunning = false;
            sel.wakeup();
            IOUtils.closeQuiet(ssc);
            IOUtils.closeQuiet(sel);
            LOG.info("network is stopped");

            // TODO close clients
        }
    }

    public void loadNameSpace(String nspath, int sc, String ibclass) {
        NameSpace _ns;
        try {
            _ns = new NameSpace(nspath, ibclass, sc);
            _ns.load();
        } catch (Exception e) {
            LOG.error("fail to load namespace " + nspath + ": " + e.getMessage(), e);
            return;
        }

        // update TODO single-thread lock is unnecessary
        try {
            lock.writeLock().lock();
            closeNameSpace();
            this.ns = _ns;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void closeNameSpace() {
        if (ns != null) {
            ns.close();
            ns = null;
            LOG.info("namespace is closed");
        }
    }

    public void stop() {
        stopNetwork();

        try {
            lock.writeLock().lock();
            closeNameSpace();
        } finally {
            lock.writeLock().unlock();
        }
        LOG.info("server is stopped");
    }
}
