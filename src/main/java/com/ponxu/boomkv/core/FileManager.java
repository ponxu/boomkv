package com.ponxu.boomkv.core;

import com.ponxu.boomkv.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * manage file
 *
 * @author ponxu
 * @date 2016-12-18
 */
public class FileManager {
    private static final Logger LOG = LoggerFactory.getLogger(FileManager.class);
    public static final int NOT_FOUND = -1;
    private FileInfo[] infos;

    private FileManager() {
    }

    public static FileManager fromDirectory(String dir) throws FileNotFoundException {
        File f = new File(dir);
        if (!f.exists()) {
            throw new FileNotFoundException(dir);
        }

        // list file only
        List<File> files = new ArrayList<>();
        IOUtils.listFiles(f, files);
        // order by filename asc
        Collections.sort(files, (f1, f2) -> f1.getAbsolutePath().compareTo(f2.getAbsolutePath()));

        // create filemanager with info
        FileManager fm = new FileManager();
        fm.infos = new FileInfo[files.size()];
        for (int i = 0; i < files.size(); i++) {
            fm.infos[i] = new FileInfo(files.get(i).getAbsolutePath());
            LOG.info("add file: {}", fm.infos[i].path);
        }

        LOG.info("{} with file count: {}", dir, files.size());

        return fm;
    }

    public File[] getFiles() {
        File[] files = new File[infos.length];
        for (int i = 0; i < infos.length; i++) {
            files[i] = new File(infos[i].path);
        }
        return files;
    }


    public FileChannel get(int i) {
        return i < infos.length ? infos[i].fc : null;
    }

    public int lookup(String path) {
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].path.equals(path)) return i;
        }
        return NOT_FOUND;
    }

    public void close() {
        for (FileInfo fi : infos) {
            IOUtils.closeQuiet(fi.fc);
            IOUtils.closeQuiet(fi.rfile);
            LOG.info("close file: {}", fi.path);
        }
    }

    private static class FileInfo {
        String path;
        RandomAccessFile rfile;
        FileChannel fc;

        public FileInfo(String path) throws FileNotFoundException {
            this.path = path;
            this.rfile = new RandomAccessFile(path, "r");
            this.fc = rfile.getChannel();
        }
    }
}
