/**
 *
 */
package fr.ifremer.scoop3.bathyClimato.util;

/**
 * <p>
 * Cette interface d�finit les accesseurs associ�s aux attributs de configuration Coriolis.
 * <p>
 *
 * @author vfachero
 */
public interface IBathyConfig {

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getEtopo1FilePath()
     */
    public String getEtopo1FilePath();

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getEtopo5FilePath()
     */
    public String getEtopo5FilePath();

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLevitus83Path()
     */
    public String getBobyClimPath();

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLevitus94Path()
     */
    public String getLevitus94Path();

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLevitus98Path()
     */
    public String getLevitus98Path();

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLiegeModb5Path()
     */
    public String getLiegeModb2Path();

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLiegeModb5Path()
     */
    public String getLiegeModb5Path();

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getMedatlas2002MedPath()
     */
    public String getMedatlas2002MedPath();

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getMedatlas2002BlackPath()
     */
    public String getMedatlas2002BlackPath();

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getReynaud1997Path()
     */
    public String getReynaud1997Path();

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLevitus2001Path()
     */
    public String getLevitus2001Path();

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop2.server.config.IScoop2Config#getLevitus2005Path()
     */
    public String getLevitus2005Path();

}