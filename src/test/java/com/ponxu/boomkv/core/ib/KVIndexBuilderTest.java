package com.ponxu.boomkv.core.ib;

import com.ponxu.boomkv.core.FileManager;
import com.ponxu.boomkv.core.NameSpace;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.ponxu.boomkv.core.Constants.DEFAULT_SC;

/**
 * @author ponxu
 * @date 2016-12-18
 */
public class KVIndexBuilderTest {
    String nspath = "/home/xwz/apus/codes/falcon/falcon-service/data/up/up_simple/dyna";
    String file = "/home/xwz/apus/codes/falcon/falcon-service/data/up/up_simple/dyna/000000_0.dyna";
    NameSpace ns;

    @Before
    public void setUp() throws Exception {
        ns = new NameSpace(nspath, KVIndexBuilder.class.getName(), DEFAULT_SC);
        ns.setFileManager(FileManager.fromDirectory(nspath));
    }

    @Test
    public void build() throws Exception {
        KVIndexBuilder kib = new KVIndexBuilder();
        kib.build(new File(file), ns);
    }
}