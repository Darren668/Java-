package exception;

import java.io.FileNotFoundException;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:58
 */
public class TableFileNotFoundException extends FileNotFoundException {
    public TableFileNotFoundException() {
    }

    public TableFileNotFoundException(String s) {
        super(s);
    }
}
