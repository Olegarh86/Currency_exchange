package dao.util;

import exception.LoadPropertiesException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesUtil {
    private static final Properties PROPERTIES = new Properties();
    static {
        loadProperties();
    }

    private PropertiesUtil() {
    }

    public static String getProperties(String key) {
        return PROPERTIES.getProperty(key);
    }

    private static void loadProperties() {
        try (InputStream resourceAsStream = PropertiesUtil.class.getClassLoader().getResourceAsStream(
                "application.properties")) {
            PROPERTIES.load(resourceAsStream);
        } catch (IOException e) {
            throw new LoadPropertiesException(e);
        }
    }
}
