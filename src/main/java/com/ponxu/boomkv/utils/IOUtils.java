package com.ponxu.boomkv.utils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author ponxu
 * @date 2016-12-18
 */
public class IOUtils {
    public static void listFiles(File src, List<File> files) {
        if (src == null || !src.exists()) {
            return;
        }

        if (src.isFile()) {
            files.add(src);
        } else if (src.isDirectory()) {
            File[] subFiles = src.listFiles();
            if (subFiles != null) {
                for (File f : subFiles) {
                    listFiles(f, files);
                }
            }
        }
    }

    public static void flushQuiet(OutputStream out) {
        if (out != null) {
            try {
                out.flush();
            } catch (IOException e) {
                // quiet
            }
        }
    }

    public static void closeQuiet(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                // quiet
            }
        }
    }
}
