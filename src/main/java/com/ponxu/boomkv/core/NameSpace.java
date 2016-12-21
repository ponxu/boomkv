package com.ponxu.boomkv.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * handle key namespace
 *
 * @author ponxu
 * @date 2016-12-18
 */
public class NameSpace {
    private static final Logger LOG = LoggerFactory.getLogger(NameSpace.class);
    private String nspath;
    private String ibclass;
    private FileManager fm;
    private int sc;
    private Map<String, Position>[] slots;

    public NameSpace(String nspath, String ibclass, int sc) {
        this.nspath = nspath;
        this.ibclass = ibclass;
        this.sc = sc;
        this.slots = new Map[sc];
        for (int i = 0; i < sc; i++) {
            slots[i] = new HashMap<>();
        }
    }

    public void load() throws Exception {
        fm = FileManager.fromDirectory(nspath);
        IndexBuilder ib = (IndexBuilder) Class.forName(ibclass).newInstance();
        for (File file : fm.getFiles()) {
            ib.build(file, this);
        }
        LOG.info("namespace {} is loaded by {}", nspath, ibclass);
    }

    public void add(String k, Position pos) {
        getSlot(k).put(k, pos);
    }

    public Position get(String k) {
        return getSlot(k).get(k);
    }

    public FileChannel getFile(int i) {
        return fm.get(i);
    }

    public int getFileIndex(String path) {
        return fm.lookup(path);
    }

    public void setFileManager(FileManager fm) {
        this.fm = fm;
    }

    private Map<String, Position> getSlot(String k) {
        int i = Math.abs(k.hashCode()) % sc;
        return slots[i];
    }

    public int getKeyCount() {
        int total = 0;
        for (int i = 0; i < sc; i++) {
            Map slot = slots[i];
            total += slot.size();
            LOG.debug("{}:{}", i, slot.size());
        }
        return total;
    }

    public int getSlotCount() {
        return sc;
    }

    public void close() {
        for (Map slot : slots) {
            slot.clear();
        }
    }
}
