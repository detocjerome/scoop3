package fr.ifremer.scoop3.io.driver;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.ImageIcon;

import fr.ifremer.scoop3.core.report.validation.Report;

public abstract class AbstractDriver {

    /*
     * Retourne le chemin d'une Resource pointant sur un fichier properties Null si non redefinit
     */
    protected String getPropsPath() {
	return null;
    }

    /*
     * Retourne une image représentant le driver l'emplacement de l'image est trouvée par la property path Null si non
     * trouvée
     */
    public Image getBackgroundImage() throws IOException {
	Image background = null;
	if (getPropsPath() != null) {
	    final InputStream input = getClass().getClassLoader().getResourceAsStream(getPropsPath());
	    final Properties props = new Properties();
	    props.load(input);
	    if (props.getProperty("path") != null) {
		background = new ImageIcon(getClass().getClassLoader().getResource(props.getProperty("path")))
			.getImage();
	    }
	}
	return background;
    }

    /*
     * TODO A completer
     */
    public Report validate() {
	return null;
    }
}
