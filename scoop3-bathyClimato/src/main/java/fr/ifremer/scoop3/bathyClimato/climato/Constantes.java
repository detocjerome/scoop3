package fr.ifremer.scoop3.bathyClimato.climato;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ifremer.scoop3.infra.properties.FileConfig;

public class Constantes {

    /**
     * LOGGER used by the whole SCOOP application
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Constantes.class);

    /**
     * CONSTANTES
     */
    // Path des climatologies

    // public final static String DIRECTORY = "E:/scoop2";
    public static final boolean SCOOP = true;
    // Status

    public static final int CLIM_OK = 0;

    public static final int CLIM_ERR_SYS = -1;

    public static final int CLIM_ERR_ARG = 1;

    public static final int CLIM_TERRE = 2;

    public static final int CLIM_UNDEF = 3;

    public static final int NO_ERROR = 0;

    public static final int ERROR_HEADER = 1;

    public static final int ERROR_DATA = 2;

    // Nombre d'immersions maximum dans la climatologie par d�faut
    public static final int NB_IMMERSIONS_MAX_CLIM_HYDRO = 40;

    // Valeurs par defaut, necessaire pour SCOOP
    public static final float DEFAULT_PAR = (float) -99.999;

    public static final int DEFAULT_NBV = -1;

    public static final float DEFAULT_DST = (float) -99.999;

    public static final float DEFAULT_VSP = (float) -99.999;

    // Types de de climatologies

    public static final String CLIM_BOBY_CLIM = "BOBY";

    public static final String CLIM_LEVITUS_83 = "L83";

    public static final String CLIM_LEVITUS_94 = "L94";

    public static final String CLIM_LEVITUS_98_1X1 = "L98_1";

    public static final String CLIM_LEVITUS_98_5X5 = "L98_5";

    public static final String CLIM_LIEGE_MODB2 = "MO2";

    public static final String CLIM_LIEGE_MODB5 = "MO5";

    public static final String CLIM_LEVITUS_2001 = "L01";

    public static final String CLIM_LEVITUS_2005 = "L05";

    public static final String CLIM_REYNAUD = "REYN";

    public static final String CLIM_MEDATLAS_MED = "M02M";

    public static final String CLIM_MEDATLAS_BLACK = "M02B";

    public static final String EXTENSION_FILTRE = ".cli";

    public static final String MONTH = "MONTH";

    public static final String YEAR = "YEAR";

    public static final String SEASON = "SEASON";

    public static String getValue(final String climatologie) {
	if (FileConfig.getScoop3FileConfig().getString("climatology." + climatologie + ".path") != null) {
	    return FileConfig.getScoop3FileConfig().getString("climatology." + climatologie + ".path").trim();
	} else {
	    return null;
	}
    }
}
