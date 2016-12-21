package com.ponxu.boomkv.tools;

import com.ponxu.boomkv.utils.Args;
import com.ponxu.boomkv.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.ponxu.boomkv.core.Constants.SEPARATOR;
import static com.ponxu.boomkv.core.ib.SimpleIndexBuilder.getDatPath;

/**
 * @author ponxu
 * @date 2016-12-18
 */
public class SimpleDataGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleDataGenerator.class);
    private String idxPath;
    private String datPath;
    private int size;

    public SimpleDataGenerator(String idxPath, int size) {
        this.idxPath = idxPath;
        this.datPath = getDatPath(idxPath);
        this.size = size;
    }

    public void run() {
        FileOutputStream idx = null, dat = null;
        try {
            idx = new FileOutputStream(idxPath);
            dat = new FileOutputStream(datPath);

            long offset = 0;
            for (int i = 0; i < size; i++) {
                byte[] k = ("testkey" + i).getBytes();
                byte[] v = ("testvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvalue" + i).getBytes();

                // write idx
                if (i > 0) {
                    idx.write(System.lineSeparator().getBytes());
                }
                idx.write(k);
                idx.write(SEPARATOR.getBytes());
                idx.write(String.valueOf(offset).getBytes());
                idx.write(SEPARATOR.getBytes());
                idx.write(String.valueOf(v.length).getBytes());

                // write dat
                dat.write(v);

                offset += v.length;

                if (size % 50000 == 0) {
                    IOUtils.flushQuiet(idx);
                    IOUtils.flushQuiet(dat);
                    LOG.info("processing: {}/{}", i, size);
                }
            }
            LOG.info("OK");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            IOUtils.flushQuiet(idx);
            IOUtils.flushQuiet(dat);
            IOUtils.closeQuiet(idx);
            IOUtils.closeQuiet(dat);
        }
    }

    public static void main(String[] args) {
        Args arg = new Args(args);
        String idx = arg.getString("-i", "/tmp/test.idx", "idx file");
        int size = arg.getInt("-s", 100000, "data size");

        new SimpleDataGenerator(idx, size).run();
    }
}
