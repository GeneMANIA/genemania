package org.genemania.adminweb.exception;

public class DatamartException extends Exception {
    public DatamartException(String string) {
        super(string);
    }

    public DatamartException(String string, Exception e) {
        super(string, e);
    }

    private static final long serialVersionUID = -5324711690464589921L;
}
