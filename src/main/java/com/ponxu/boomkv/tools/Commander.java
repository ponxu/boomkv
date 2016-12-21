package com.ponxu.boomkv.tools;

import com.ponxu.boomkv.cleint.BoomKVClient;
import com.ponxu.boomkv.utils.Args;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.ponxu.boomkv.core.Constants.*;

/**
 * @author ponxu
 * @date 2016-12-18
 */
public class Commander {
    public static void main(String[] args) throws IOException {
        Args arg = new Args(args);
        String host = arg.getString("-h", null, "host");
        int port = arg.getInt("-p", 0, "port");
        if (host == null || port == 0) {
            arg.outputUsage(System.out);
            System.exit(1);
        }
        System.out.println(String.format("server %s:%d", host, port));

        // String password = "xxx";
        BoomKVClient client = new BoomKVClient(host, port);
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String input = stdin.readLine();
            if (input.equals("")) {
                continue;
            }

            String parts[] = input.split(" ", 2);
            String cmd = parts[0];

            try {
                byte[] rs = null;
                if (cmd.equalsIgnoreCase("GET")) {
                    rs = client.exec(CMD_GET, parts[1].getBytes());
                } else if (cmd.equalsIgnoreCase("AUTH")) {
                    rs = client.exec(CMD_AUTH, parts[1].getBytes());
                } else if (cmd.equalsIgnoreCase("INFO")) {
                    rs = client.exec(CMD_INFO, RQ_EMPTY);
                } else if (cmd.equalsIgnoreCase("RELOAD")) {
                    rs = client.exec(CMD_RELOAD, parts[1].getBytes());
                } else if (cmd.equalsIgnoreCase("SHUTDOWN")) {
                    rs = client.exec(CMD_SHUTDOWN, RQ_EMPTY);
                } else {
                    System.out.println("bye");
                    break;
                }
                System.out.println(rs == null ? "null" : new String(rs));
            } catch (Exception e) {
                System.err.println("Wrong");
            }
        }
        client.close();
    }
}
