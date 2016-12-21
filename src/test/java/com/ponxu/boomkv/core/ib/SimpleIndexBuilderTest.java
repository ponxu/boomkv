package com.ponxu.boomkv.core.ib;

import com.ponxu.boomkv.core.FileManager;
import com.ponxu.boomkv.core.NameSpace;
import com.ponxu.boomkv.tools.SimpleDataGenerator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.ponxu.boomkv.core.Constants.DEFAULT_IB;
import static com.ponxu.boomkv.core.Constants.DEFAULT_SC;
import static com.ponxu.boomkv.core.TestConstants.*;

/**
 * @author ponxu
 * @date 2016-12-18
 */
public class SimpleIndexBuilderTest {
    NameSpace ns;

    @Before
    public void setUp() throws Exception {
        if (!new File(IDX).exists()) {
            new SimpleDataGenerator(IDX, SIZE).run();
        }

        ns = new NameSpace(NS_PATH, DEFAULT_IB, DEFAULT_SC);
        ns.setFileManager(FileManager.fromDirectory(NS_PATH));
    }

    @Test
    public void build() throws Exception {
        SimpleIndexBuilder sib = new SimpleIndexBuilder();
        sib.build(new File(IDX), ns);
    }
}