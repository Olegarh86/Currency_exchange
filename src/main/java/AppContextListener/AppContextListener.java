package AppContextListener;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dao.CurrencyDao;
import dao.ExchangeRateDao;
import exception.ConnectionException;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
@WebListener
public class AppContextListener implements ServletContextListener {
    private static final String APPLICATION_PROPERTIES = "application.properties";
    private static final String PROPERTIES_NOT_FOUND = "application.properties not found";
    private static final String DRIVER_KEY = "db.driver";
    private static final String URL_KEY = "db.path";
    private static final String USER = "db.user";
    private static final String PASSWORD = "db.password";
    private static final String TIMEOUT = "timeout";
    private static final String POOL_SIZE_KEY = "db.pool.size";
    private static final String DEFAULT_POOL_SIZE_KEY = "db.default.pool.size";
    private static final String ATTRIBUTE_DATA_SOURCE = "dataSource";
    private static final String INSTANCE_CURRENCY = "instanceCurrency";
    private static final String FAILED_TO_PROCESS_INIT_CONTEXT = "Failed to process init context ";
    private static final String FAILED_TO_PROCESS_LOAD_DRIVER = "Failed to process load driver {}";
    private static final String INSTANCE_EXCHANGE_RATE = "instanceExchangeRate";
    private HikariDataSource hikariDataSource;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            Properties properties = loadProperties();
            loadDriver(properties);
            createConnectionPool(properties);
            event.getServletContext().setAttribute(ATTRIBUTE_DATA_SOURCE, hikariDataSource);
        } catch (Exception e) {
            throw new ConnectionException(FAILED_TO_PROCESS_INIT_CONTEXT + e.getMessage());
        }

        CurrencyDao instanceCurrency = new CurrencyDao(hikariDataSource);
        ExchangeRateDao instanceExchangeRate = new ExchangeRateDao(hikariDataSource);

        event.getServletContext().setAttribute(INSTANCE_CURRENCY, instanceCurrency);
        event.getServletContext().setAttribute(INSTANCE_EXCHANGE_RATE, instanceExchangeRate);
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream(APPLICATION_PROPERTIES)) {
            if (is == null) {
                throw new RuntimeException(PROPERTIES_NOT_FOUND);
            }
            props.load(is);
        }
        return props;
    }

    private static void loadDriver(Properties properties) {
        try {
            Class.forName(properties.getProperty(DRIVER_KEY));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(FAILED_TO_PROCESS_LOAD_DRIVER + e.getMessage());
        }
    }

    private void createConnectionPool(Properties properties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty(URL_KEY));
        config.setUsername(properties.getProperty(USER));
        config.setPassword(properties.getProperty(PASSWORD));
        config.setConnectionTimeout(Long.parseLong(properties.getProperty(TIMEOUT)));
        int poolSize = Integer.parseInt(properties.getProperty(POOL_SIZE_KEY));
        if (poolSize <= 0) {
            poolSize = Integer.parseInt(properties.getProperty(DEFAULT_POOL_SIZE_KEY));
        }
        config.setMaximumPoolSize(poolSize);

        hikariDataSource = new HikariDataSource(config);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (hikariDataSource != null) {
            hikariDataSource.close();
        }
    }
}
