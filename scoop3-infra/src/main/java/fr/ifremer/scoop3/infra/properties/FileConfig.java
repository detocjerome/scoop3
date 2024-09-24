package fr.ifremer.scoop3.infra.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import fr.ifremer.scoop3.infra.logger.SC3Logger;

/**
 * Access to configuration file custom.properties erase default.properties
 *
 */
public class FileConfig {

    public static final String CONTROLS_PROPERTIES = "controls.properties";
    public static final String CUSTOM_CONTROLS_PROPERTIES_DIRECTORY = System.getProperty("user.home") + "/scoop3/";
    public static final String USER_PROPERTIES_FILE = CUSTOM_CONTROLS_PROPERTIES_DIRECTORY + "user.properties";

    private static Properties defaultProps;
    private static FileConfig fileConfig = null;
    private static Properties localProps;
    private static Properties userProps;

    public static FileConfig getScoop3FileConfig() {
	if (fileConfig == null) {
	    fileConfig = new FileConfig();
	}
	return fileConfig;
    }

    /**
     * Constructor.
     *
     * @throws Exception
     */
    private FileConfig() {
	defaultProps = new Properties();
	localProps = new Properties(defaultProps);
	userProps = new Properties(localProps);

	SC3Logger.LOGGER.info("Loading properties file {}", "default.properties");
	loadProperties(defaultProps, "default.properties");
	SC3Logger.LOGGER.info("Loading properties file {}", "custom.properties");
	loadProperties(localProps, "custom.properties");
	loadUserProperties();
    }

    /**
     * Add a properties file
     */
    public void addPropertiesFile(final String resourceName) {
	loadProperties(localProps, resourceName);
    }

    /**
     *
     * @return
     */
    public String getApplicationVersion() {
	return localProps.getProperty("application.version");
    }

    /**
     * @param key
     * @return the value defined only in the file "default.properties" or "custom.properties"
     */
    public String getDefaultString(final String key) {
	return localProps.getProperty(key);
    }

    /**
     *
     * @param key
     * @return the string
     */
    public String getString(final String key) {
	return userProps.getProperty(key);
    }

    /**
     *
     * @param key
     * @return the string
     */
    public int getInt(final String key) {
	return (Integer.parseInt(userProps.getProperty(key)));
    }

    public void loadUserProperties() {
	SC3Logger.LOGGER.info("Loading properties file {}", "user.properties");
	loadProperties(userProps, USER_PROPERTIES_FILE);
    }

    /**
     * Load a properties file
     *
     * @param properties
     * @param resourceName
     * @throws Exception
     */
    private void loadProperties(final Properties properties, final String resourceName) {
	InputStream inStream = getClass().getClassLoader().getResourceAsStream(resourceName);
	if (inStream == null) {
	    try {
		inStream = new FileInputStream(new File(resourceName));
	    } catch (final FileNotFoundException e) {
	    }
	}
	if (inStream != null) {
	    try {
		properties.load(inStream);
	    } catch (final IOException e) {
		SC3Logger.LOGGER.error(e.getMessage(), e);
	    }
	} else {
	    SC3Logger.LOGGER.error("Unable to locate resource : {} ", resourceName);
	}
    }
}
