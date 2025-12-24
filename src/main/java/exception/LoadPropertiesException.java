package exception;

import java.io.IOException;

public class LoadPropertiesException extends RuntimeException {
    public LoadPropertiesException(IOException e) {
        super("Please check availability application.properties file in 'resources' directory " + e);
    }
}
