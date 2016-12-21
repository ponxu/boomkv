package com.ponxu.boomkv.core;

import com.ponxu.boomkv.utils.Args;

import static com.ponxu.boomkv.core.Constants.*;

/**
 * the booter of boomkv
 *
 * @author ponxu
 * @date 2016-12-18
 */
public class Bootstrap {
    public static void main(String[] args) {
        Args arg = new Args(args);
        String bind = arg.getString("-b", DEFAULT_BIND, "bind");
        int port = arg.getInt("-p", DEFAULT_PORT, "port");
        String nspath = arg.getString("-n", null, "namespace");
        int sc = arg.getInt("-s", DEFAULT_SC, "sloat count");
        String ibclass = arg.getString("-i", DEFAULT_IB, "index builder class");
        if (nspath == null) {
            arg.outputUsage(System.out);
            System.exit(1);
        }

        Server server = new Server(bind, port, nspath, sc, ibclass);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }
}
