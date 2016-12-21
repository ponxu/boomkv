package com.ponxu.boomkv.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author ponxu
 * @date 2016-12-18
 */
public class ArgsTest {
    String[] inputs = {"-h", "127.0.0.1", "-p", "9763"};
    Args a = new Args(inputs);

    @Test
    public void getString() throws Exception {
        Assert.assertEquals("127.0.0.1", a.getString("-h", null, "host"));
        a.outputUsage(System.out);
    }

    @Test
    public void getInt() throws Exception {
        Assert.assertEquals(9763, a.getInt("-p", -1, "port"));
        a.outputUsage(System.out);
    }

}