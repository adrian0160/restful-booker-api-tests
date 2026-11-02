package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads settings from config.properties on the test classpath.
 * Any key can be overridden from the command line, e.g. mvn test -Dbase.url=http://localhost:3001
 */
public final class Config {

    private static final String FILE = "config.properties";
    private static final Properties PROPS = load();

    private Config() {
    }

    public static String baseUrl() {
        return get("base.url");
    }

    public static String username() {
        return get("auth.username");
    }

    public static String password() {
        return get("auth.password");
    }

    public static String get(String key) {
        String value = System.getProperty(key, PROPS.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing config key '" + key + "' in " + FILE);
        }
        return value.trim();
    }

    private static Properties load() {
        Properties props = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(FILE)) {
            if (in == null) {
                throw new IllegalStateException(FILE + " not found on the test classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + FILE, e);
        }
        return props;
    }
}
