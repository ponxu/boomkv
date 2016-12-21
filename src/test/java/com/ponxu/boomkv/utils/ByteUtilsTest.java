package com.ponxu.boomkv.utils;

import org.junit.Test;

import java.util.Arrays;

/**
 * @author ponxu
 * @date 2016-12-18
 */
public class ByteUtilsTest {
    @Test
    public void int2Bytes() throws Exception {
        System.out.println(Arrays.toString(ByteUtils.int2Bytes(13)));
    }

    @Test
    public void bytes2Int() throws Exception {
        byte[] bytes = ByteUtils.int2Bytes(12);
        System.out.println(ByteUtils.bytes2Int(bytes));
    }

}