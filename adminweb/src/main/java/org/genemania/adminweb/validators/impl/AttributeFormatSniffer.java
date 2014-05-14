package org.genemania.adminweb.validators.impl;

import java.io.File;
import java.io.IOException;

public class AttributeFormatSniffer {
    public static final int FORMAT_UNKNOWN = 0;
    public static final int FORMAT_GMT = 1;
    public static final int FORMAT_ATTRIBUTE_LIST = 2;

    private static final char SEP = '\t';
    File file;

    public AttributeFormatSniffer(File file) {
        this.file = file;
    }

    public int sniff() throws IOException {

        int format;
        format = sniffName();
        if (format == FORMAT_UNKNOWN) {
            format = sniffContent();
        }

        // until sniffing properly implemented, just fall back to the old format
        if (format == FORMAT_UNKNOWN) {
            return FORMAT_ATTRIBUTE_LIST;
        }
        else {
            return format;
        }
    }

    // the name is a hint
    private int sniffName() {
        if (file.toString().toLowerCase().endsWith(".gmt")) {
            return FORMAT_GMT;
        }
        else {
            return FORMAT_UNKNOWN;
        }
    }

    private int sniffContent() {
        return FORMAT_UNKNOWN;
    }
}
