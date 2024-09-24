package fr.ifremer.scoop3.infra.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import fr.ifremer.scoop3.infra.properties.FileConfig;

/**
 * Access to messages_fr.properties.
 *
 */
public class Messages {

    private static FileConfig fileConfig = FileConfig.getScoop3FileConfig();
    private static String language = fileConfig.getString("language");
    private static String country = fileConfig.getString("country");
    private static Locale locale = new Locale(language, country);
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", locale);

    /**
     *
     * @param key
     *            the key
     * @return the message
     */
    public static String getMessage(final String key) {
	String message = "";
	if (resourceBundle.containsKey(key)) {
	    message = resourceBundle.getString(key);
	} else {
	    message = key;
	}
	return message;
    }

    /**
     * @return the locale
     */
    public static Locale getLocale() {
	return locale;
    }

    public static ResourceBundle getResourceBundle() {
	return resourceBundle;
    }

    public static void setResourceBundle(final ResourceBundle resourceBundle) {
	Messages.resourceBundle = resourceBundle;
    }
}
