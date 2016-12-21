package com.ponxu.boomkv.core.ib;

import com.ponxu.boomkv.core.FileManager;
import com.ponxu.boomkv.core.IndexBuilder;
import com.ponxu.boomkv.core.NameSpace;
import com.ponxu.boomkv.core.Position;
import com.ponxu.boomkv.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static com.ponxu.boomkv.core.Constants.SEPARATOR;

/**
 * base on idx file:
 * idx file: k \001 offset-in-datafile \001 length-of-data
 * dat file: data....
 *
 * @author ponxu
 * @date 2016-12-18
 */
public class SimpleIndexBuilder implements IndexBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleIndexBuilder.class);
    public static final String IDX_EXT = ".idx";
    public static final String DAT_EXT = ".dat";

    @Override
    public void build(File file, NameSpace ns) {
        if (file == null || ns == null) {
            throw new IllegalStateException("parameter should not be null");
        }

        String idxPath = file.getAbsolutePath();
        if (!idxPath.endsWith(IDX_EXT)) {
            return;
        }

        // find out the datafile in filemanager
        String datPath = getDatPath(idxPath);
        int df = ns.getFileIndex(datPath);
        if (FileManager.NOT_FOUND == df) {
            LOG.warn("there is not found data file for: {} {}", idxPath, datPath);
            return;
        }

        long total = 0, succ = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                total++;
                String[] parts = line.split(SEPARATOR);
                if (parts.length == 3) {
                    try {
                        long offset = Long.parseLong(parts[1]);
                        int length = Integer.parseInt(parts[2]);
                        ns.add(parts[0], new Position(df, offset, length));
                        succ++;
                    } catch (Exception e) {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuiet(br);
        }
        LOG.info("success to load idx:{} total:{} succ:{}", idxPath, total, succ);
    }

    public static String getDatPath(String idxPath) {
        if (!idxPath.endsWith(IDX_EXT)) {
            throw new IllegalStateException(String.format("%s should be %s", idxPath, IDX_EXT));
        }
        return idxPath.substring(0, idxPath.length() - IDX_EXT.length()) + DAT_EXT;
    }
}
