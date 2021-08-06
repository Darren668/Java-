package exception;

import java.nio.file.FileAlreadyExistsException;

/**
 * @author xinhaojie
 * @create 2021-03-18-16:00
 */
public class TableFileExistedException extends FileAlreadyExistsException {

    public TableFileExistedException(String file) {
        super(file);
    }
}
