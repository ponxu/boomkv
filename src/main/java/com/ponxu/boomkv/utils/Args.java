package com.ponxu.boomkv.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ponxu
 * @date 2016-12-18
 */
public class Args {
    private String[] inputs;
    private List<Arg> infos;

    public Args(String[] inputs) {
        this.inputs = inputs;
        this.infos = new ArrayList<>();
    }

    public String getString(String tag, String def, String desc) {
        infos.add(new Arg<String>(tag, def, desc));
        String v = get(tag);
        return v != null ? v : def;
    }

    public int getInt(String tag, int def, String desc) {
        infos.add(new Arg<Integer>(tag, def, desc));
        String v = get(tag);
        return v != null ? Integer.parseInt(v) : def;
    }

    public String get(String tag) {
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i].equals(tag)) {
                if (i < inputs.length - 1) {
                    return inputs[i + 1];
                }
            }
        }
        return null;
    }

    public void outputUsage(OutputStream out) {
        StringBuilder help = new StringBuilder("Usage:");
        for (Arg a : infos) {
            help.append("\n\t").append(a.tag).append(" ").append(a.description);
            if (a.def != null) {
                help.append(" (").append(a.def).append(")");
            }
        }

        try {
            out.write(help.toString().getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Arg<T> {
        String tag;
        T def;
        String description;

        public Arg(String tag, T def, String description) {
            this.tag = tag;
            this.def = def;
            this.description = description;
        }
    }
}
