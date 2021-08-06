package exception;

import java.io.FileNotFoundException;

/**
 * @author xinhaojie
 * @create 2021-03-18-15:58
 */
public class DataBaseFileNotFoundException extends FileNotFoundException {
    public DataBaseFileNotFoundException() {
    }

    public DataBaseFileNotFoundException(String s) {
        super(s);
    }
}
