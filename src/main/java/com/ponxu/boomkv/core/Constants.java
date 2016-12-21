package com.ponxu.boomkv.core;

import com.ponxu.boomkv.core.ib.SimpleIndexBuilder;

/**
 * constants
 *
 * @author ponxu
 * @date 2016-12-18
 */
public interface Constants {
    // for namespace
    String SEPARATOR = "\001";
    int DEFAULT_SC = 8;
    String DEFAULT_IB = SimpleIndexBuilder.class.getName();

    // for network
    String WORKER_NAME = "BoomKV-NetWorker";
    String DEFAULT_BIND = "0.0.0.0";
    int DEFAULT_PORT = 9736;
    int BUFF_SIZE = 1024;

    // for session
    byte CMD_GET = 1;
    byte CMD_AUTH = 100;
    byte CMD_INFO = 101;
    byte CMD_RELOAD = 102;
    byte CMD_SHUTDOWN = 103;
    byte[] RQ_EMPTY = "NONE".getBytes();
    byte[] RS_OK = "OK".getBytes();
    byte[] RS_FAIL = "FAIL".getBytes();
    byte[] RS_AUTH = "NOT AUTH".getBytes();
}
