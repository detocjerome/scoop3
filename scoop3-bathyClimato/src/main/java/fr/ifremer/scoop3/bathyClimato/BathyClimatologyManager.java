package fr.ifremer.scoop3.bathyClimato;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Marker;

import climato.Climatology;
import climato.Climatology.CLIMATOLOGY_ID;
import climato.Climatology.CLIMATOLOGY_PERIOD;
import climato.Climatology.CLIMATOLOGY_RESOLUTION;
import climato.Climatology.GF3_PARAMETER;
import climato.ClimatologyFactory;
import climato.result.ClimatologyRequest;
import climato.result.ClimatologyResults;
import climato.result.ClimatologyValues;
import fr.ifremer.scoop3.bathyClimato.climato.ClimatoResult;
import fr.ifremer.scoop3.bathyClimato.etopo1.Etopo1ReaderException;
import fr.ifremer.scoop3.bathyClimato.etopo5.Etopo5ReaderException;
import fr.ifremer.scoop3.bathyClimato.gebco.GebcoReaderException;
import fr.ifremer.scoop3.bathyClimato.services.BathyService;
import fr.ifremer.scoop3.chart.model.ChartDataset;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.infra.tools.Conversions;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.parameter.SpatioTemporalParameter;
import fr.ifremer.scoop3.model.parameter.TimeParameter;
import javafx.geometry.Point2D;

/**
 * Gestionnaire des climatologies : Il permet de conserver les informations sur les paramètres disponibles pour les
 * climatologiers déjà visualisées
 */

public class BathyClimatologyManager {

    private boolean isBathyNull = false;
    private static String bathyClimatoPropertiesName = "scoop3-bathyClimato.local.properties";
    private static final String ETOPO1_VARNAME = "bathymetry.etopo1.datafile";
    private static final String GEBCO_VARNAME = "bathymetry.gebco.datafile";
    private static final String ETOPO5_VARNAME = "bathymetry.etopo5.datafile";
    private static final String ETOPO_PREF_VARNAME = "bathymetry.userPref.datafile";
    private String bathymetryName;

    /*
     * Cache of ClimatoResult key : (boundingBox,month,parameter,climatoCode)
     */
    private class ClimatoResultCacheKey {

	Double latitude;
	Double longitude;
	int month;
	String gf3;
	String climatologyCode;

	public ClimatoResultCacheKey(final Double latitude, final Double longitude, final int month, final String GF3,
		final String climatologyCode) {

	    this.latitude = latitude;
	    this.longitude = longitude;
	    this.month = month;
	    this.gf3 = GF3;
	    this.climatologyCode = climatologyCode;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (obj == null) {
		return false;
	    }
	    if (getClass() != obj.getClass()) {
		return false;
	    }
	    final ClimatoResultCacheKey other = (ClimatoResultCacheKey) obj;
	    if (!getOuterType().equals(other.getOuterType())) {
		return false;
	    }
	    if (gf3 == null) {
		if (other.gf3 != null) {
		    return false;
		}
	    } else if (!gf3.equals(other.gf3)) {
		return false;
	    }
	    if (climatologyCode == null) {
		if (other.climatologyCode != null) {
		    return false;
		}
	    } else if (!climatologyCode.equals(other.climatologyCode)) {
		return false;
	    }
	    if (latitude == null) {
		if (other.latitude != null) {
		    return false;
		}
	    } else if (!latitude.equals(other.latitude)) {
		return false;
	    }
	    if (longitude == null) {
		if (other.longitude != null) {
		    return false;
		}
	    } else if (!longitude.equals(other.longitude)) {
		return false;
	    }
	    if (month != other.month) {
		return false;
	    }
	    return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = (prime * result) + getOuterType().hashCode();
	    result = (prime * result) + ((gf3 == null) ? 0 : gf3.hashCode());
	    result = (prime * result) + ((climatologyCode == null) ? 0 : climatologyCode.hashCode());
	    result = (prime * result) + ((latitude == null) ? 0 : latitude.hashCode());
	    result = (prime * result) + ((longitude == null) ? 0 : longitude.hashCode());
	    result = (prime * result) + month;
	    return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    return "ClimatoResultCacheKey [latitude=" + latitude + ", longitude=" + longitude + ", month=" + month
		    + ", GF3=" + gf3 + ", climatologyCode=" + climatologyCode + "]";
	}

	private BathyClimatologyManager getOuterType() {
	    return BathyClimatologyManager.this;
	}

    }

    /**
     * Constantes de classe
     *
     */

    /* TODO replace by enum */
    public static final String ETOPO1_FILE = "Etopo1";

    public static final String ETOPO5_FILE = "Etopo5";

    public static final String GEBCO_FILE = "Gebco";
    /*
     * TODO remove from configuration file, remlace by a static final Example # The default values returned by the
     * climato (separated by space character) core.climato-default-values=-99.9999 -99.999 99.999 99.99
     */
    public static final Set<Float> CLIMATO_DEFAULT_VALUES = new HashSet<>();

    static {
	final String defaultValuesStr = FileConfig.getScoop3FileConfig().getString("core.climato-default-values");
	if (defaultValuesStr != null) {
	    for (final String defaultValueStr : defaultValuesStr.split(" ")) {
		try {
		    CLIMATO_DEFAULT_VALUES.add(Float.parseFloat(defaultValueStr));
		} catch (final NumberFormatException e) {
		    SC3Logger.LOGGER.error(e.getMessage(), e);
		}
	    }
	}
    }

    private static final String DOX2 = "DOX2";
    private static final Float DOX2_MULTIPLIER = 0.022951f;
    private static final String DOXY = "DOXY";
    private static final Float DOXY_MULTIPLIER = 0.022391f;
    private static final String SLCW = "SLCW";
    private static final Float SLCW_MULTIPLIER = 1.025f;
    private static final String NTZW = "NTZW";
    private static final Float NTZW_MULTIPLIER = 1.025f;
    private static final String SSAL = "SSAL";
    private static final Float SSAL_MULTIPLIER = 0.004f;
    private static final String PHOW = "PHOW";
    private static final Float PHOW_MULTIPLIER = 1.025f;
    private static final String ALKW = "ALKW";
    private static final Float ALKW_MULTIPLIER = 1.025f;
    private static final String NTAW = "NTAW";
    private static final Float NTAW_MULTIPLIER = 1.025f;
    private static final String NTIW = "NTIW";
    private static final Float NTIW_MULTIPLIER = 1.025f;
    private static final String AMOW = "AMOW";
    private static final Float AMOW_MULTIPLIER = 1.025f;

    private static HashMap<String, Float> converterOceanicParameters = new HashMap<String, Float>();

    // fill the converterOceanicParameters
    static {
	converterOceanicParameters.put(DOX2, DOX2_MULTIPLIER);
	converterOceanicParameters.put(DOXY, DOXY_MULTIPLIER);
	converterOceanicParameters.put(SLCW, SLCW_MULTIPLIER);
	converterOceanicParameters.put(NTZW, NTZW_MULTIPLIER);
	converterOceanicParameters.put(SSAL, SSAL_MULTIPLIER);
	converterOceanicParameters.put(PHOW, PHOW_MULTIPLIER);
	converterOceanicParameters.put(ALKW, ALKW_MULTIPLIER);
	converterOceanicParameters.put(NTAW, NTAW_MULTIPLIER);
	converterOceanicParameters.put(NTIW, NTIW_MULTIPLIER);
	converterOceanicParameters.put(AMOW, AMOW_MULTIPLIER);
    }

    /**
     * Singleton
     */
    private static BathyClimatologyManager singleton = null;

    /**
     * Attributs
     */

    /*
     *
     */
    private final BathyService bathyService;

    /* Map contenant les climatologies et les paramètres associés */
    private final Map<String, List<String>> climatologiesAndParameters;

    private final HashMap<ClimatoResultCacheKey, ClimatoResult> climatoResultCache;

    public static BathyClimatologyManager getSingleton() {
	if (singleton == null) {
	    singleton = new BathyClimatologyManager();
	}
	return singleton;
    }

    /*
     * @param etopoFileName
     *
     * @return true if the etopoFileName is a valid file name
     */
    public static boolean isValidEtopoFile(final String etopoFileName) {
	return (etopoFileName != null) && (etopoFileName.equals(ETOPO1_FILE) || etopoFileName.equals(ETOPO5_FILE));
    }

    private BathyClimatologyManager() {
	bathyService = new BathyService();
	climatologiesAndParameters = new HashMap<String, List<String>>();
	climatoResultCache = new HashMap<ClimatoResultCacheKey, ClimatoResult>();
	initializeClimatologyFactory();
    }

    /**
     * Retourne la valeur de bathy selon le fichier etopo utilisé
     *
     * @param etopoFileName
     *            Le nom du fichier etopo
     * @param latitude
     *            La valeur de la latitude
     * @param longitude
     *            La valeur de la longitude
     * @return la valeur de la bathy
     * @throws BathyException
     */
    public short getBathymetry(final String etopoFileName, final Double latitude, final Double longitude)
	    throws BathyException {

	short bathy = 0;

	if ((etopoFileName == null) || "".equals(etopoFileName)) {
	    // On prend etopo5 par défaut
	    try {
		bathy = bathyService.getBathyEtopo5(latitude, longitude);
		if (isBathyNull) {
		    isBathyNull = false;
		}
	    } catch (final Etopo5ReaderException e) {
		isBathyNull = true;
		SC3Logger.LOGGER.debug(Etopo5ReaderException.CREATE_NEW_ETOPO5READER + " : ", e);
	    }
	} else if (etopoFileName.equals(BathyClimatologyManager.ETOPO1_FILE)) {
	    // Choix du fichier etopo1
	    Short value = 0;
	    try {
		value = bathyService.getBathyEtopo1(latitude, longitude);
		if (isBathyNull) {
		    isBathyNull = false;
		}
	    } catch (final Etopo1ReaderException e) {
		SC3Logger.LOGGER.debug(Etopo1ReaderException.CREATE_NEW_ETOPO1READER + " : ", e);
		isBathyNull = true;
	    }
	    if (value != Short.MAX_VALUE) {
		bathy = value;
	    }
	} else if (etopoFileName.equals(BathyClimatologyManager.GEBCO_FILE)) {
	    // Choix du fichier gebco
	    Short value = 0;
	    try {
		value = bathyService.getBathyGebco(latitude, longitude);
		if (isBathyNull) {
		    isBathyNull = false;
		}
	    } catch (final GebcoReaderException e) {
		SC3Logger.LOGGER.debug(GebcoReaderException.CREATE_NEW_GEBCOREADER + " : ", e);
		isBathyNull = true;
	    }
	    if (value != Short.MAX_VALUE) {
		bathy = value;
	    }
	} else {
	    // Choix etopo5
	    try {
		bathy = bathyService.getBathyEtopo5(latitude, longitude);
		if (isBathyNull) {
		    isBathyNull = false;
		}
	    } catch (final Etopo5ReaderException e) {
		isBathyNull = true;
		SC3Logger.LOGGER.debug(Etopo5ReaderException.CREATE_NEW_ETOPO5READER + " : ", e);
	    }
	}

	return bathy;
    }

    /**
     * Retourne la valeur de bathy sur ETOPO1 puis ETOPO5 ou NULL.
     *
     * @param latitude
     *            La valeur de la latitude
     * @param longitude
     *            La valeur de la longitude
     * @return la valeur de la bathy /!\ peut renvoyer NULL !!!
     * @throws BathyException
     */
    public Short getBestBathymetryWithoutException(final Double latitude, final Double longitude) {
	Short bathy = null;

	// if there is no bathymetry selected in properties by the user
	if ((FileConfig.getScoop3FileConfig().getString(ETOPO_PREF_VARNAME) == null)
		|| FileConfig.getScoop3FileConfig().getString(ETOPO_PREF_VARNAME).trim().equals("")) {
	    try {
		if (!FileConfig.getScoop3FileConfig().getString(ETOPO1_VARNAME).trim().equals("")) {
		    bathy = BathyClimatologyManager.getSingleton().getBathymetry(BathyClimatologyManager.ETOPO1_FILE,
			    latitude, longitude);
		    bathymetryName = BathyClimatologyManager.ETOPO1_FILE;
		    if (BathyClimatologyManager.getSingleton().isBathyNull) {
			bathy = null;
			bathymetryName = "null";
		    }
		} else if (!FileConfig.getScoop3FileConfig().getString(GEBCO_VARNAME).trim().equals("")) {
		    bathy = BathyClimatologyManager.getSingleton().getBathymetry(BathyClimatologyManager.GEBCO_FILE,
			    latitude, longitude);
		    bathymetryName = BathyClimatologyManager.GEBCO_FILE;
		    if (BathyClimatologyManager.getSingleton().isBathyNull) {
			bathy = null;
			bathymetryName = "null";
		    }
		} else {
		    try {
			if (!FileConfig.getScoop3FileConfig().getString(ETOPO5_VARNAME).trim().equals("")) {
			    bathy = BathyClimatologyManager.getSingleton()
				    .getBathymetry(BathyClimatologyManager.ETOPO5_FILE, latitude, longitude);
			    bathymetryName = BathyClimatologyManager.ETOPO5_FILE;
			    if (BathyClimatologyManager.getSingleton().isBathyNull) {
				bathy = null;
				bathymetryName = "null";
			    }
			} else {
			    bathy = null;
			    bathymetryName = "null";
			}
		    } catch (final BathyException e1) {
			bathy = null;
			bathymetryName = "null";
		    }
		}
	    } catch (final BathyException e) {
		try {
		    if (!FileConfig.getScoop3FileConfig().getString(ETOPO5_VARNAME).trim().equals("")) {
			bathy = BathyClimatologyManager.getSingleton()
				.getBathymetry(BathyClimatologyManager.ETOPO5_FILE, latitude, longitude);
			bathymetryName = BathyClimatologyManager.ETOPO5_FILE;
			if (BathyClimatologyManager.getSingleton().isBathyNull) {
			    bathy = null;
			    bathymetryName = "null";
			}
		    } else {
			bathy = null;
			bathymetryName = "null";
		    }
		} catch (final BathyException e1) {
		    bathy = null;
		    bathymetryName = "null";
		}
	    }
	} else {
	    String userPref = FileConfig.getScoop3FileConfig().getString(ETOPO_PREF_VARNAME).trim();
	    userPref = userPref.substring(11, userPref.length() - 9);
	    String bathyFile = "";
	    // Add new condition for a new ETOPO_FILE if needed
	    if (BathyClimatologyManager.ETOPO1_FILE.equalsIgnoreCase(userPref)) {
		bathyFile = BathyClimatologyManager.ETOPO1_FILE;
	    } else if (BathyClimatologyManager.GEBCO_FILE.equalsIgnoreCase(userPref)) {
		bathyFile = BathyClimatologyManager.GEBCO_FILE;
	    } else if (BathyClimatologyManager.ETOPO5_FILE.equalsIgnoreCase(userPref)) {
		bathyFile = BathyClimatologyManager.ETOPO5_FILE;
	    } else {
		SC3Logger.LOGGER.error("Le fichier de bathymétrie " + userPref
			+ " n'est pas intégré à Scoop. Contactez un administrateur pour l'ajouter à la configuration.");
	    }
	    bathymetryName = bathyFile;

	    if (!bathyFile.equals("")) {
		try {
		    if (!FileConfig.getScoop3FileConfig()
			    .getString("bathymetry." + bathyFile.toLowerCase() + ".datafile").equals("")) {
			bathy = BathyClimatologyManager.getSingleton().getBathymetry(bathyFile, latitude, longitude);
			if (BathyClimatologyManager.getSingleton().isBathyNull) {
			    bathy = null;
			    bathymetryName = "null";
			}
		    } else {
			bathy = null;
			bathymetryName = "null";
		    }
		} catch (final BathyException e) {
		    try {
			if (!FileConfig.getScoop3FileConfig().getString(ETOPO1_VARNAME).trim().equals("")) {
			    bathy = BathyClimatologyManager.getSingleton()
				    .getBathymetry(BathyClimatologyManager.ETOPO1_FILE, latitude, longitude);
			    bathymetryName = BathyClimatologyManager.ETOPO1_FILE;
			    if (BathyClimatologyManager.getSingleton().isBathyNull) {
				bathy = null;
				bathymetryName = "null";
			    }
			} else {
			    bathy = null;
			    bathymetryName = "null";
			}
			if (!FileConfig.getScoop3FileConfig().getString(GEBCO_VARNAME).trim().equals("")) {
			    bathy = BathyClimatologyManager.getSingleton()
				    .getBathymetry(BathyClimatologyManager.GEBCO_FILE, latitude, longitude);
			    bathymetryName = BathyClimatologyManager.GEBCO_FILE;
			    if (BathyClimatologyManager.getSingleton().isBathyNull) {
				bathy = null;
				bathymetryName = "null";
			    }
			} else {
			    bathy = null;
			    bathymetryName = "null";
			}
		    } catch (final BathyException e0) {
			try {
			    if (!FileConfig.getScoop3FileConfig().getString(ETOPO5_VARNAME).trim().equals("")) {
				bathy = BathyClimatologyManager.getSingleton()
					.getBathymetry(BathyClimatologyManager.ETOPO5_FILE, latitude, longitude);
				bathymetryName = BathyClimatologyManager.ETOPO5_FILE;
				if (BathyClimatologyManager.getSingleton().isBathyNull) {
				    bathy = null;
				    bathymetryName = "null";
				}
			    } else {
				bathy = null;
				bathymetryName = "null";
			    }
			} catch (final BathyException e1) {
			    bathy = null;
			    bathymetryName = "null";
			}
		    }
		}
	    }
	}

	return bathy;
    }

    /**
     *
     * @param latitude
     * @param longitude
     * @param month
     * @param GF3
     * @param climatoEnum
     * @return ClimatoResult or ClimatologyResults
     * @throws Exception
     */
    public Object getClimatologie(final Double latitude, final Double longitude, final int month, final String GF3,
	    final String climatologyCode) throws Exception {
	Object toReturn = null;

	final CLIMATOLOGY_ID climatologyId = getClimatoId(climatologyCode);
	final CLIMATOLOGY_RESOLUTION climatologyResolution = getClimatoResolution(climatologyCode);

	final String requestID = "requestID";

	if (!GF3.equals("DENS") && !GF3.equals("DENS_ADJUSTED")) {
	    if (climatologyId == null) {
		toReturn = getClimatologieSC2(latitude, longitude, month, GF3, climatologyCode);
	    } else {
		toReturn = getClimatologyResults(requestID, latitude, longitude, month, GF3, climatologyId,
			climatologyResolution);
	    }

	    if (toReturn == null) {
		SC3Logger.LOGGER
			.debug("Il n'existe pas de climatologie " + climatologyCode + " pour le paramètre " + GF3);
	    }
	}

	return toReturn;
    }

    /**
     *
     * @param latitude
     * @param longitude
     * @param month
     * @param GF3
     * @param climatoEnum
     * @return ClimatoResult or ClimatologyResults
     */
    public ArrayList<ClimatologyResults> getClimatologieForTS(final CLIMATOLOGY_ID climatologyId,
	    final CLIMATOLOGY_RESOLUTION climatologyResolution, final String GF3, final String climatologyCode,
	    final Observation observation, final boolean timeserieDivided, final ChartDataset chartDataset,
	    final int currentObsNumber) {
	ArrayList<ClimatologyResults> toReturn = null;

	final String requestID = "requestID";

	toReturn = getClimatologyResultsForTS(requestID, GF3, climatologyId, climatologyResolution, observation,
		timeserieDivided, chartDataset, currentObsNumber);

	if (toReturn == null) {
	    SC3Logger.LOGGER.debug("Il n'existe pas de climatologie " + climatologyCode + " pour le paramètre " + GF3);
	}

	return toReturn;
    }

    /**
     * Renvois la climatologie
     *
     * @param latitude
     * @param longitude
     * @param month
     * @param GF3
     * @param climatologyCode
     * @return CustomClimatoResult
     */
    private ClimatoResult getClimatologieSC2(final Double latitude, final Double longitude, final int month,
	    final String GF3, final String climatologyCode) {

	// if the asked GF3 code doesn't exist in climatology, use another code which exists and convert it with a
	// multiplier
	String requestGF3 = "";
	if (GF3.equals("NTRZ") || GF3.equals("NTAW") || GF3.equals("NTZW")) {
	    requestGF3 = "NTRA";
	} else if (GF3.equals("DOX2")) {
	    requestGF3 = "DOX1";
	} else if (GF3.equals("DOXY")) {
	    requestGF3 = "DOX1";
	} else if (GF3.equals("SLCW")) {
	    requestGF3 = "SLCA";
	} else if (GF3.equals("SSAL")) {
	    requestGF3 = "PSAL";
	} else if (GF3.equals("PHOW")) {
	    requestGF3 = "PHOS";
	} else if (GF3.equals("ALKW")) {
	    requestGF3 = "ALKY";
	} else if (GF3.equals("NTIW")) {
	    requestGF3 = "NTRI";
	} else if (GF3.equals("AMOW")) {
	    requestGF3 = "AMON";
	} else {
	    requestGF3 = GF3;
	}

	/* Calcul de la cle a chercher/ajouter en cache */
	// Precision a 0.5 degre
	final double lat = (Math.round((latitude / 0.5))) * 0.5;
	final double lon = (Math.round((longitude / 0.5))) * 0.5;
	final ClimatoResultCacheKey keyToGet = new ClimatoResultCacheKey(lat, lon, month, GF3, climatologyCode);

	// Recherche/ajout prealable en cache
	ClimatoResult cliRes = null;
	if (climatoResultCache.containsKey(keyToGet)) {
	    cliRes = climatoResultCache.get(keyToGet);
	    SC3Logger.LOGGER.trace("climatoresultCache find: " + keyToGet);
	} else {
	    cliRes = bathyService.getClimato(latitude, longitude, month, requestGF3, climatologyCode);
	    SC3Logger.LOGGER.trace("climatoresultCache put: " + keyToGet);

	    // Convert parameters if needed
	    convertOceanicParameters(cliRes, GF3, requestGF3);

	    climatoResultCache.put(keyToGet, cliRes);
	}

	return cliRes;
    }

    private ClimatologyResults getClimatologyResults(final String requestID, final Double latitude,
	    final Double longitude, int month, final String GF3, final CLIMATOLOGY_ID climatologyId,
	    final CLIMATOLOGY_RESOLUTION climatologyResolution) {
	ClimatologyResults toReturn = null;
	try {

	    // apply the NTRA climatology (nitrates) to NTAW (nitrates too), NTZW and NTRZ data (nitrates + nitrites)
	    // because nitrites are negligible
	    String requestGF3 = "";
	    if (GF3.equals("NTRZ") || GF3.equals("NTRA") || GF3.equals("NTAW")) {
		requestGF3 = "NTZW";
	    } else {
		requestGF3 = GF3;
	    }

	    // define the period
	    CLIMATOLOGY_PERIOD period;
	    if (climatologyId == CLIMATOLOGY_ID.MIN_MAX) { // special case for MIN_MAX
		period = CLIMATOLOGY_PERIOD.ANNUAL;
		month = 1;
	    } else {
		period = CLIMATOLOGY_PERIOD.MONTHLY;
	    }
	    // define the gf3 parameter
	    final GF3_PARAMETER gf3Parameter = getGF3ClimatologyParameter(requestGF3);

	    // test if climato exist
	    if (ClimatologyFactory.exists(climatologyId, period, climatologyResolution, gf3Parameter)) {
		final Climatology climatology = ClimatologyFactory.get(climatologyId, period, climatologyResolution,
			gf3Parameter);

		toReturn = climatology.getResults(
			new ClimatologyRequest(requestID, latitude.floatValue(), longitude.floatValue(), month));

		// give the min_max climatology a scale form
		if (climatologyId == CLIMATOLOGY_ID.MIN_MAX) {
		    final int depthsCount = 2 * toReturn.getClimatoValues().get(0).getDepthsCount();
		    final boolean isMinMax = toReturn.getClimatoValues().get(0).isMinMax();
		    final ClimatologyRequest request = toReturn.getClimatoValues().get(0).getRequest();

		    // create empty arrays
		    final int[] returnNumberOfObservations = new int[2
			    * toReturn.getClimatoValues().get(0).getNumberOfObservations().length];
		    final float[] returnDepth = new float[2 * toReturn.getClimatoValues().get(0).getDepths().length];
		    final float[] returnMinValues = new float[2
			    * toReturn.getClimatoValues().get(0).getMinValues().length];
		    final float[] returnMaxValues = new float[2
			    * toReturn.getClimatoValues().get(0).getMaxValues().length];

		    // fill those arrays with old duplicate values
		    for (int index = 0; index < depthsCount; index++) {
			if ((index % 2) == 0) {
			    returnDepth[index] = toReturn.getClimatoValues().get(0).getDepths()[index / 2];
			    returnMinValues[index] = toReturn.getClimatoValues().get(0).getMinValues()[index / 2];
			    returnMaxValues[index] = toReturn.getClimatoValues().get(0).getMaxValues()[index / 2];
			} else {
			    if (index != (depthsCount - 1)) {
				returnDepth[index] = toReturn.getClimatoValues().get(0).getDepths()[(index + 1) / 2];
			    } else {
				returnDepth[index] = returnDepth[index - 1]
					+ (returnDepth[index - 1] - returnDepth[index - 3]);
			    }
			    returnMinValues[index] = toReturn.getClimatoValues().get(0).getMinValues()[(index - 1) / 2];
			    returnMaxValues[index] = toReturn.getClimatoValues().get(0).getMaxValues()[(index - 1) / 2];
			}
			returnNumberOfObservations[index] = 1;
		    }

		    // create the climatologyValues
		    final ArrayList<ClimatologyValues> climatoValuesList = new ArrayList<ClimatologyValues>();
		    final ClimatologyValues climatologyValues = new ClimatologyValues(isMinMax, request, depthsCount,
			    new Point2D(longitude, latitude));
		    for (int index = 0; index < depthsCount; index++) {
			climatologyValues.add(index, returnDepth[index], returnMinValues[index], returnMaxValues[index],
				returnNumberOfObservations[index]);
		    }
		    // attribute the new climato
		    climatoValuesList.add(climatologyValues);
		    toReturn.setClimatoValues(climatoValuesList);
		}
	    }
	} catch (final NullPointerException e1) {
	    SC3Logger.LOGGER.warn("Climatology request returned null");
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	}

	return toReturn;
    }

    private ArrayList<ClimatologyResults> getClimatologyResultsForTS(final String requestID, final String GF3,
	    final CLIMATOLOGY_ID climatologyId, final CLIMATOLOGY_RESOLUTION climatologyResolution,
	    final Observation observation, final boolean timeserieDivided, final ChartDataset chartDataset,
	    final int currentObsNumber) {

	ClimatologyResults climatoResults;
	final ArrayList<ClimatologyResults> toReturn = new ArrayList<ClimatologyResults>();
	try {
	    // define the period
	    CLIMATOLOGY_PERIOD period;
	    int periodValue;
	    if (climatologyId == CLIMATOLOGY_ID.MIN_MAX) { // special case for MIN_MAX
		period = CLIMATOLOGY_PERIOD.ANNUAL;
	    } else {
		period = CLIMATOLOGY_PERIOD.MONTHLY;
	    }
	    // define the gf3 parameter
	    final GF3_PARAMETER gf3Parameter = getGF3ClimatologyParameter(GF3);

	    // reset climatoResults
	    climatoResults = null;

	    // test if climato exist
	    if (ClimatologyFactory.exists(climatologyId, period, climatologyResolution, gf3Parameter)) {

		final Climatology climatology = ClimatologyFactory.get(climatologyId, period, climatologyResolution,
			gf3Parameter);

		// ArrayList<Double[]> timeParameterValues = null;
		// if (!timeserieDivided) {
		// timeParameterValues = (ArrayList<Double[]>) observation.getReferenceParameter().getValues();
		// } else {
		// if (chartDataset != null) {
		// timeParameterValues = (ArrayList<Double[]>) chartDataset
		// .getPhysicalVariable(observation.getReferenceParameter().getCode())
		// .getPhysicalValuesByStation();
		// }
		// }

		for (int indexTS = 0; indexTS < ((SpatioTemporalParameter<Number>) observation.getReferenceParameter())
			.getValues().size(); indexTS++) {

		    // date
		    long date = -1;
		    Double dateDouble = null;
		    if (!timeserieDivided) {
			date = ((TimeParameter) observation.getReferenceParameter()).getValues().get(indexTS);
		    } else {
			dateDouble = chartDataset.getPhysicalVariable(observation.getReferenceParameter().getCode())
				.getPhysicalValuesByStation().get(currentObsNumber)[indexTS];
			if (dateDouble != null) {
			    date = dateDouble.longValue();
			}
		    }

		    final Calendar calendar = Conversions.getUTCCalendar();
		    if (!timeserieDivided || (timeserieDivided && (dateDouble != null))) {
			calendar.setTime(new Date(date));
			if (climatologyId == CLIMATOLOGY_ID.MIN_MAX) { // special case for MIN_MAX
			    periodValue = 1;
			} else {
			    periodValue = calendar.get(Calendar.MONTH) + 1;
			}

			// lat & lon
			Double latitude;
			Double longitude;
			if (observation.getLatitude().getValues().size() == 1) {
			    latitude = observation.getLatitude().getValues().get(0);
			    longitude = observation.getLongitude().getValues().get(0);
			} else {
			    latitude = observation.getLatitude().getValues().get(indexTS);
			    longitude = observation.getLongitude().getValues().get(indexTS);
			}

			if ((latitude != null) && (longitude != null)) {
			    final ClimatologyRequest climatologyRequest = new ClimatologyRequest(requestID,
				    latitude.floatValue(), longitude.floatValue(), periodValue);

			    if ((climatoResults != null) && climatology
				    .inSameAreaAndPeriod(climatoResults.getClimatoValues().get(0), climatologyRequest)
				    && !timeserieDivided) {
				toReturn.add(climatoResults);
			    } else {
				climatoResults = climatology.getResults(climatologyRequest);

				// give the min_max climatology a scale form
				if (climatologyId == CLIMATOLOGY_ID.MIN_MAX) {
				    final int depthsCount = 2
					    * climatoResults.getClimatoValues().get(0).getDepthsCount();
				    final boolean isMinMax = climatoResults.getClimatoValues().get(0).isMinMax();
				    final ClimatologyRequest request = climatoResults.getClimatoValues().get(0)
					    .getRequest();

				    // create empty arrays
				    final int[] returnNumberOfObservations = new int[2 * climatoResults
					    .getClimatoValues().get(0).getNumberOfObservations().length];
				    final float[] returnDepth = new float[2
					    * climatoResults.getClimatoValues().get(0).getDepths().length];
				    final float[] returnMinValues = new float[2
					    * climatoResults.getClimatoValues().get(0).getMinValues().length];
				    final float[] returnMaxValues = new float[2
					    * climatoResults.getClimatoValues().get(0).getMaxValues().length];

				    // fill those arrays with old duplicate values
				    for (int index = 0; index < depthsCount; index++) {
					if ((index % 2) == 0) {
					    returnDepth[index] = climatoResults.getClimatoValues().get(0)
						    .getDepths()[index / 2];
					    returnMinValues[index] = climatoResults.getClimatoValues().get(0)
						    .getMinValues()[index / 2];
					    returnMaxValues[index] = climatoResults.getClimatoValues().get(0)
						    .getMaxValues()[index / 2];
					} else {
					    if (index != (depthsCount - 1)) {
						returnDepth[index] = climatoResults.getClimatoValues().get(0)
							.getDepths()[(index + 1) / 2];
					    } else {
						returnDepth[index] = returnDepth[index - 1]
							+ (returnDepth[index - 1] - returnDepth[index - 3]);
					    }
					    returnMinValues[index] = climatoResults.getClimatoValues().get(0)
						    .getMinValues()[(index - 1) / 2];
					    returnMaxValues[index] = climatoResults.getClimatoValues().get(0)
						    .getMaxValues()[(index - 1) / 2];
					}
					returnNumberOfObservations[index] = 1;
				    }

				    // create the climatologyValues
				    final ArrayList<ClimatologyValues> climatoValuesList = new ArrayList<ClimatologyValues>();
				    final ClimatologyValues climatologyValues = new ClimatologyValues(isMinMax, request,
					    depthsCount, new Point2D(longitude, latitude));
				    for (int index = 0; index < depthsCount; index++) {
					climatologyValues.add(index, returnDepth[index], returnMinValues[index],
						returnMaxValues[index], returnNumberOfObservations[index]);
				    }
				    // attribute the new climato
				    climatoValuesList.add(climatologyValues);
				    climatoResults.setClimatoValues(climatoValuesList);
				}

				toReturn.add(climatoResults);
			    }
			}
		    } else {
			toReturn.add(null);
		    }
		}
	    }
	} catch (final Exception e) {
	    if (toReturn != null) {
		SC3Logger.LOGGER.error(e.getMessage(), e);
	    }
	}

	return toReturn;
    }

    private GF3_PARAMETER getGF3ClimatologyParameter(final String gf3Code) {
	/*
	 * Modifie le param�tre GF3 si celui-ci est suffix� de ADJUSTED Dans le cas ou celui-ci est suffix� de
	 * ADJUSTED_ERROR aucune modification n'est r�alis�e ex : <param>_ADJUSTED -> <param>
	 */
	String modifiedParameter = gf3Code.toUpperCase();
	if (modifiedParameter.matches("(.*)_ADJUSTED")) {
	    modifiedParameter = modifiedParameter.replace("_ADJUSTED", "");
	}

	try {
	    return GF3_PARAMETER.valueOf(modifiedParameter);
	} catch (final Exception e) {
	}

	return null;
    }

    // /**
    // * Retourne le code du paramètre physique parent s'il existe et n'est pas de type erreur autrement retourne le
    // code
    // * du paramètre physique de l'élément
    // */
    // public int getParentPhysicalParamCodeAdjusted(final PhysicalParameterBO physicalParameter) {
    // // Bouml preserved body begin 0001F9FE
    // if ((physicalParameter.getPhysicalParamParent() != null)
    // && physicalParameter.getLabel().toUpperCase().contains("ADJUSTED")
    // && !(physicalParameter.getLabel().toUpperCase().contains("ERROR"))) {
    // return physicalParameter.getPhysicalParamParent().getId().getPhysicalParamCode();
    // } else {
    // return physicalParameter.getId().getPhysicalParamCode();
    // }
    // // Bouml preserved body end 0001F9FE
    // }

    /**
     * Renvois les valeurs de la batyhymétrie de la position et des 8 points autour pour un fichier etopo1
     *
     * @param latitude
     * @param longitude
     * @return (short) tableau de la valeur de bathymétrie à la position et des 8 points autour Les carreaux voisins
     *         sont numerotes dans le sens horaire :
     * @throws BathyException
     */

    public short[] getSurroundBathy(final String etopoFileName, final Double latitude, final Double longitude)
	    throws BathyException {

	short[] profondeurs = null;

	try {
	    if ((etopoFileName == null) || "".equals(etopoFileName)) {
		// On prend etopo5 par défaut
		profondeurs = bathyService.getSurroundBathyEtopo5(latitude, longitude);
	    } else if (etopoFileName.equals(BathyClimatologyManager.ETOPO1_FILE)) {
		// Choix du fichier etopo1
		profondeurs = bathyService.getSurroundBathyEtopo1(latitude, longitude);
	    } else if (etopoFileName.equals(BathyClimatologyManager.GEBCO_FILE)) {
		// Choix du fichier etopo1
		profondeurs = bathyService.getSurroundBathyGebco(latitude, longitude);
	    } else {
		// Choix etopo5
		profondeurs = bathyService.getSurroundBathyEtopo5(latitude, longitude);
	    }
	} catch (final Exception exception) {
	    SC3Logger.LOGGER.error((Marker) null, null, exception);
	    throw new BathyException(exception);
	}

	return profondeurs;
    }

    /**
     * Enabled the call to the web service
     */
    public void initCallBathyWebService() {
	// empty method
    }

    /**
     * Retourne true si le paramètre est disponible pour cette climatologie
     */
    public boolean isAvailableParameter(final String parameterGf3Code, final String climatologyCode) {
	// Bouml preserved body begin 0001F9FE
	try {
	    if (!hasClimatology(climatologyCode)) {
		final List<String> params = new ArrayList<String>();
		final String parameters = bathyService.getParametersAvailable(climatologyCode);
		final String[] tabParams = parameters.split("\\|");

		for (int i = 0; i < tabParams.length; i++) {

		    params.add(tabParams[i]);
		    climatologiesAndParameters.put(climatologyCode, params);
		}

	    }
	    return climatologiesAndParameters.get(climatologyCode).contains(parameterGf3Code);
	} catch (final Exception e) {
	    return false;
	}
	// Bouml preserved body end 0001F9FE
    }

    /**
     * Retourne true si la climatologie est déjà connue du manager
     */
    private boolean hasClimatology(final String climatologyCode) {
	// Bouml preserved body begin 0001F97E
	return climatologiesAndParameters.containsKey(climatologyCode);
	// Bouml preserved body end 0001F97E
    }

    /**
     *
     */
    private void initializeClimatologyFactory() {
	// !! may change for using a Thredds server as netcdf file provider set climatologies directories

	final String climatologiesPaths = FileConfig.getScoop3FileConfig().getString("climatologies.paths");
	if (climatologiesPaths != null) {
	    for (final String climatologyIdPath : climatologiesPaths.split(";")) {
		if ((climatologyIdPath != null) && !climatologyIdPath.isEmpty()) {
		    final String[] climatologyIdPathSplitted = climatologyIdPath.split(" : ");
		    if (climatologyIdPathSplitted.length >= 2) {
			final String climatologyIdStr = climatologyIdPathSplitted[0].trim();
			final String path = climatologyIdPathSplitted[1].trim();
			final Path filePath = Paths.get(path);
			if (Files.exists(filePath)) {
			    try {
				final CLIMATOLOGY_ID climatologyId = CLIMATOLOGY_ID.valueOf(climatologyIdStr);
				SC3Logger.LOGGER.info("path for climato [" + climatologyId + "] : " + path);
				ClimatologyFactory.setDirectory(climatologyId, path);
			    } catch (final Exception e) {
				e.printStackTrace();
			    }
			} else {
			    SC3Logger.LOGGER.info("This path doesn't exist : " + filePath);
			}
		    }
		}
	    }
	}
    }

    /**
     * Get the climatology ID given the climatologyCode
     *
     * @param climatologyCode
     * @return
     */
    public CLIMATOLOGY_ID getClimatoId(final String climatologyCode) {
	CLIMATOLOGY_ID climatologyId = null;
	switch (climatologyCode) {
	case "ARIVO_05":
	    climatologyId = CLIMATOLOGY_ID.ARIVO;
	    break;
	case "ISAS13_05":
	    climatologyId = CLIMATOLOGY_ID.ISAS13;
	    break;
	case "WOA09_1":
	    climatologyId = CLIMATOLOGY_ID.WOA09;
	    break;
	case "WOA09_5":
	    climatologyId = CLIMATOLOGY_ID.WOA09;
	    break;
	case "WOA13_1":
	    climatologyId = CLIMATOLOGY_ID.WOA13V2;
	    break;
	case "WOA13_5":
	    climatologyId = CLIMATOLOGY_ID.WOA13V2;
	    break;
	case "WOA18_1":
	    climatologyId = CLIMATOLOGY_ID.WOA18;
	    break;
	case "WOA18_5":
	    climatologyId = CLIMATOLOGY_ID.WOA18;
	    break;
	case "MIN_MAX":
	    climatologyId = CLIMATOLOGY_ID.MIN_MAX;
	    break;
	default:
	    // Nothing to do here ...
	    break;
	}
	return climatologyId;
    }

    /**
     * Get the climatology Resolution given the climatologyCode
     *
     * @param climatologyCode
     * @return
     */
    public CLIMATOLOGY_RESOLUTION getClimatoResolution(final String climatologyCode) {
	CLIMATOLOGY_RESOLUTION climatologyResolution = null;
	switch (climatologyCode) {
	case "ARIVO_05":
	    climatologyResolution = CLIMATOLOGY_RESOLUTION.DEGREE_05;
	    break;
	case "ISAS13_05":
	    climatologyResolution = CLIMATOLOGY_RESOLUTION.DEGREE_05;
	    break;
	case "WOA09_1":
	    climatologyResolution = CLIMATOLOGY_RESOLUTION.DEGREE_1;
	    break;
	case "WOA09_5":
	    climatologyResolution = CLIMATOLOGY_RESOLUTION.DEGREE_5;
	    break;
	case "WOA13_1":
	    climatologyResolution = CLIMATOLOGY_RESOLUTION.DEGREE_1;
	    break;
	case "WOA13_5":
	    climatologyResolution = CLIMATOLOGY_RESOLUTION.DEGREE_5;
	    break;
	case "WOA18_1":
	    climatologyResolution = CLIMATOLOGY_RESOLUTION.DEGREE_1;
	    break;
	case "WOA18_5":
	    climatologyResolution = CLIMATOLOGY_RESOLUTION.DEGREE_5;
	    break;
	case "MIN_MAX":
	    climatologyResolution = CLIMATOLOGY_RESOLUTION.ISEA_40962;
	    break;
	default:
	    // Nothing to do here ...
	    break;
	}
	return climatologyResolution;
    }

    public boolean isBathyNull() {
	return this.isBathyNull;
    }

    public static void setBathyClimatoPropertiesName(final String s) {
	bathyClimatoPropertiesName = s;
    }

    public String getBathymetryName() {
	return bathymetryName;
    }

    private void convertOceanicParameters(final ClimatoResult cliRes, final String GF3, final String requestGF3) {
	if (!requestGF3.equals(GF3) && (converterOceanicParameters.get(GF3) != null) && !GF3.equals(SSAL)) {
	    final Float diviser = converterOceanicParameters.get(GF3);

	    if (diviser != 0) {
		// multiply ecarts
		final Float[] ecartsTab = new Float[cliRes.getEcarts().length];
		for (int i = 0; i < cliRes.getEcarts().length; i++) {
		    if ((bathyService.getClimatoReader().getClimatoValeurIndefinie() != null) && !bathyService
			    .getClimatoReader().getClimatoValeurIndefinie().contains(cliRes.getEcarts()[i])
			    && (cliRes.getNbMesures()[i] != -1)) {
			ecartsTab[i] = cliRes.getEcarts()[i] / diviser;
		    } else {
			ecartsTab[i] = cliRes.getEcarts()[i];
		    }
		}
		cliRes.setEcarts(ecartsTab);

		// multiply moyennes
		final Float[] moyennesTab = new Float[cliRes.getMoyennes().length];
		for (int i = 0; i < cliRes.getMoyennes().length; i++) {
		    if ((bathyService.getClimatoReader().getClimatoValeurIndefinie() != null) && !bathyService
			    .getClimatoReader().getClimatoValeurIndefinie().contains(cliRes.getMoyennes()[i])
			    && (cliRes.getNbMesures()[i] != -1)) {
			moyennesTab[i] = cliRes.getMoyennes()[i] / diviser;
		    } else {
			moyennesTab[i] = cliRes.getMoyennes()[i];
		    }
		}
		cliRes.setMoyennes(moyennesTab);
	    }
	} else if (GF3.equals(SSAL)) {
	    final Float diff = converterOceanicParameters.get(GF3);

	    // substract moyennes
	    final Float[] moyennesTab = new Float[cliRes.getMoyennes().length];
	    for (int i = 0; i < cliRes.getMoyennes().length; i++) {
		if ((bathyService.getClimatoReader().getClimatoValeurIndefinie() != null) && !bathyService
			.getClimatoReader().getClimatoValeurIndefinie().contains(cliRes.getMoyennes()[i])
			&& (cliRes.getNbMesures()[i] != -1)) {
		    moyennesTab[i] = cliRes.getMoyennes()[i] + diff;
		} else {
		    moyennesTab[i] = cliRes.getMoyennes()[i];
		}
	    }
	    cliRes.setMoyennes(moyennesTab);
	}
    }
}
