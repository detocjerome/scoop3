package fr.ifremer.scoop3.bathyClimato.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BathyFileConfig implements IBathyConfig {

    private final Properties defaultProps;
    private final Properties localProps;

    public BathyFileConfig() throws Exception {
	defaultProps = new Properties();
	localProps = new Properties(defaultProps);

	loadProperties(defaultProps, "default.properties");
	loadProperties(localProps, "custom.properties");
    }

    private void loadProperties(final Properties props, final String resourceName) throws Exception {
	final InputStream inStream = getClass().getClassLoader().getResourceAsStream(resourceName);
	if (inStream != null) {
	    try {
		props.load(inStream);
	    } catch (final IOException ioe) {
		throw new Exception("Unable to load resource \"" + resourceName + "\" (resource located OK)", ioe);
	    }
	} else {
	    throw new Exception("Unable to locate resource \"" + resourceName + "\"");
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getEtopo1FilePath()
     */
    @Override
    public String getEtopo1FilePath() {
	return localProps.getProperty("scoop2.etopo1FilePath");
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getEtopo5FilePath()
     */
    @Override
    public String getEtopo5FilePath() {
	return localProps.getProperty("scoop2.etopo5FilePath");
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLevitus83Path()
     */
    @Override
    public String getBobyClimPath() {
	return localProps.getProperty("scoop2.bobyClimPath");
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLevitus94Path()
     */
    @Override
    public String getLevitus94Path() {
	return localProps.getProperty("scoop2.levitus94Path");
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLevitus98Path()
     */
    @Override
    public String getLevitus98Path() {
	return localProps.getProperty("scoop2.levitus98Path");
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLiegeModb5Path()
     */
    @Override
    public String getLiegeModb2Path() {
	return localProps.getProperty("scoop2.liegeModb2Path");
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLiegeModb5Path()
     */
    @Override
    public String getLiegeModb5Path() {
	return localProps.getProperty("scoop2.liegeModb5Path");
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getMedatlas2002MedPath()
     */
    @Override
    public String getMedatlas2002MedPath() {
	return localProps.getProperty("scoop2.medatlas2002MedPath");
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getMedatlas2002BlackPath()
     */
    @Override
    public String getMedatlas2002BlackPath() {
	return localProps.getProperty("scoop2.medatlas2002BlackPath");
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getReynaud1997Path()
     */
    @Override
    public String getReynaud1997Path() {
	return localProps.getProperty("scoop2.reynaud1997Path");
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLevitus2001Path()
     */
    @Override
    public String getLevitus2001Path() {
	return localProps.getProperty("scoop2.levitus2001Path");
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLevitus2005Path()
     */
    @Override
    public String getLevitus2005Path() {
	return localProps.getProperty("scoop2.levitus2005Path");
    }

}
