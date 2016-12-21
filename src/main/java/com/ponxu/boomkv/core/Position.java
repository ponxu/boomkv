package com.ponxu.boomkv.core;

/**
 * position for v
 *
 * @author ponxu
 * @date 2016-12-18
 */
public class Position {
    /**
     * index in filemanager
     */
    private int datafile;
    /**
     * the begin byte of v
     */
    private long offset;
    /**
     * the length of v
     */
    private int length;

    public Position() {
    }

    public Position(int datafile, long offset, int length) {
        this.datafile = datafile;
        this.offset = offset;
        this.length = length;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getDatafile() {
        return datafile;
    }

    public void setDatafile(int datafile) {
        this.datafile = datafile;
    }

    @Override
    public String toString() {
        return "Position{" +
                "datafile=" + datafile +
                ", offset=" + offset +
                ", length=" + length +
                '}';
    }
}
