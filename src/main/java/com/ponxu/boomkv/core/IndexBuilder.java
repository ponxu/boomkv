package com.ponxu.boomkv.core;

import java.io.File;

/**
 * build memery index for namespace
 *
 * @author ponxu
 * @date 2016-12-18
 */
public interface IndexBuilder {
    /**
     * add k and position to namespace
     *
     * @param file someone file which contians k info
     * @param ns   the namespace handle k
     * @return data file
     */
    public void build(File file, NameSpace ns);
}
