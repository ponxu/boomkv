package com.ponxu.boomkv.tools;

import com.ponxu.boomkv.cleint.BoomKVClient;
import com.ponxu.boomkv.utils.Args;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.ponxu.boomkv.core.Constants.CMD_GET;

/**
 * @author ponxu
 * @date 2016-12-18
 */
public class Benchmark {
    public static void main(String[] args) {
        Args arg = new Args(args);
        String host = arg.getString("-h", null, "host");
        int port = arg.getInt("-p", 0, "port");
        int c = arg.getInt("-c", 100, "concurrent");
        int n = arg.getInt("-n", 10000, "number of request per concurrent");

        if (host == null || port == 0) {
            arg.outputUsage(System.out);
            System.exit(1);
        }
        System.out.println(String.format("server %s:%d  concurrent:%d request:%d", host, port, c, n));

        long s = System.currentTimeMillis();

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < c; i++) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    BoomKVClient client = new BoomKVClient(host, port);
                    Random r = new Random();
                    for (int j = 0; j < n; j++) {
                        byte[] k = ("testkey" + r.nextInt(10_000)).getBytes();
                        byte[] v = client.exec(CMD_GET, k);
                        //System.out.println(new String(k) + " " + new String(v));
                    }
                    client.close();
                }
            };
            t.start();
            threads.add(t);
        }

        // wait finish
        threads.forEach((thread) -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        long used = System.currentTimeMillis() - s;
        System.out.println(String.format("Time:%d QPS:%f", used, (n * c) / (used / 1000.0)));
    }
}
