package exception;

import java.nio.file.FileAlreadyExistsException;

/**
 * @author xinhaojie
 * @create 2021-03-18-16:01
 */
public class DatabaseFileExistedException extends FileAlreadyExistsException {

    public DatabaseFileExistedException(String file) {
        super(file);
    }
}
