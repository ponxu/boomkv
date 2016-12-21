package com.ponxu.boomkv.core;

import org.junit.Before;
import org.junit.Test;

import static com.ponxu.boomkv.core.Constants.DEFAULT_IB;
import static com.ponxu.boomkv.core.Constants.DEFAULT_SC;
import static com.ponxu.boomkv.core.TestConstants.NS_PATH;

/**
 * @author ponxu
 * @date 2016-12-18
 */
public class NameSpaceTest {
    NameSpace ns;

    @Before
    public void setUp() throws Exception {
        ns = new NameSpace(NS_PATH, DEFAULT_IB, DEFAULT_SC);
        ns.load();
    }

    @Test
    public void get() throws Exception {
        Position pos = ns.get("testkey9981"); // testkey9981^A128643^A13
        System.out.println(pos);

//        RandomAccessFile rfile = ns.getFile(pos.getDatafile());
//        rfile.seek(pos.getOffset());
//        byte[] data = new byte[pos.getLength()];
//        int reads = rfile.read(data);
//        System.out.println(reads + " " + new String(data));
    }

    @Test
    public void getKeyCount() throws Exception {
        System.out.println(ns.getKeyCount());
    }
}