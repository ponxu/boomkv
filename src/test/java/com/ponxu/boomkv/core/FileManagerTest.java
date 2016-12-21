package com.ponxu.boomkv.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author ponxu
 * @date 2016-12-18
 */
public class FileManagerTest {
    FileManager fm;

    @Before
    public void setUp() throws Exception {
        fm = FileManager.fromDirectory("/tmp");
    }

    @After
    public void tearDown() throws Exception {
        fm.close();
    }

    @Test
    public void get() throws Exception {
        System.out.println(fm.get(0));
    }

    @Test
    public void lookup() throws Exception {
        System.out.println(fm.lookup("/tmp/ct.shutdown"));
        System.out.println(fm.lookup("/tmp/ct.shutdown2"));
    }
}