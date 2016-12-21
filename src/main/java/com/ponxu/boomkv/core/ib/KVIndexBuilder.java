package com.ponxu.boomkv.core.ib;

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
 * base on kv file
 * dat file: k \001 v
 *
 * @author ponxu
 * @date 2016-12-18
 */
public class KVIndexBuilder implements IndexBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(KVIndexBuilder.class);

    @Override
    public void build(File file, NameSpace ns) {
        int df = ns.getFileIndex(file.getAbsolutePath());

        long offset = 0;
        long total = 0, succ = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                total++;
                String[] parts = line.split(SEPARATOR, 2);
                if (parts.length == 2) {
                    int kl = parts[0].getBytes().length;
                    int vl = parts[1].getBytes().length;
                    ns.add(parts[0], new Position(df, offset + kl + 1, vl));
                    succ++;
                }
                int ll = line.getBytes().length + 1; // TODO windows:\r\n  linux:\n
                offset += ll;
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuiet(br);
        }
        LOG.info("success to load kv:{} total:{} succ:{}", file.getAbsolutePath(), total, succ);
    }
}
