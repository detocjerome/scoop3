package fr.ifremer.scoop3.bathyClimato.gebco;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ifremer.scoop3.bathyClimato.etopo1.Etopo1ReaderException;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class GebcoReader {

    /**
     * LOGGER used by the whole SCOOP application
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(GebcoReader.class);

    private static NetcdfFile dataFile = null;
    // private static String depth_missing_value;

    /* File to parse for retrieving bathy */
    private static final String DATAFILE_STRING = FileConfig.getScoop3FileConfig()
	    .getString("bathymetry.gebco.datafile").trim();

    private static GebcoReader instance;

    /**
     * La variable DEPTH
     */
    private static Variable variableDepth;
    private static String latName = "lat";
    private static String lonName = "lon";
    private static String elevationName = "elevation";

    public GebcoReader() throws GebcoReaderException {

	// NetcdfFile dataFile = null;
	if (dataFile == null) {
	    try {
		// Lecture du fichier netcdf
		LOGGER.info("GebcoReader : Ouverture Fichier");
		dataFile = NetcdfFile.open(DATAFILE_STRING, null);
		variableDepth = dataFile.findVariable(elevationName);
	    } catch (final IOException e) {
		SC3Logger.LOGGER.error("GEBCO doesn't exist at this adress : " + DATAFILE_STRING);
		// throw new GebcoReaderException(GebcoReaderException.CREATE_NEW_GEBCOREADER + " : ", e);
	    }
	}
    }

    /**
     * @return an instance of Etopo1Reader
     * @throws Etopo1ReaderException
     */
    public static synchronized GebcoReader getInstance() throws GebcoReaderException {
	if (instance == null) {
	    instance = new GebcoReader();
	}

	return instance;
    }

    /**
     * Retourne la valeur de la profondeur selon la latitude et longitude renseign�es
     *
     * @param latitude
     * @param longitude
     */
    public short findDepthValue(final Double latitude, final Double longitude) {
	int indexLatitude = 0;
	int indexLongitude = 0;
	Array data = null;
	try {

	    if ((getIndexLatitude(latitude) != null) && (getIndexLongitude(longitude) != null)) {
		indexLatitude = (Integer) getIndexLatitude(latitude);
		indexLongitude = (Integer) getIndexLongitude(longitude);

		try {
		    // Lecture de la valeur du Depth
		    LOGGER.info("findDepthValue : lecture valeur du Depth");
		    final String atributeValues = indexLatitude + "," + indexLongitude;

		    data = variableDepth.read(atributeValues);
		} catch (final IOException | InvalidRangeException e) {
		    LOGGER.error("findDepthValue : erreur de lecture valeur du Depth" + e.getMessage() + data);
		}
	    } else {
		LOGGER.error("findDepthValue : index hors domaine");
	    }

	} catch (final Exception e) {
	    LOGGER.error("findDepthValue : erreur de lecture de l' index de latitude ou longitude " + e.getMessage());
	}

	if (data == null) {
	    return Short.MAX_VALUE;
	}
	return data.getShort(0);
    }

    /**
     * Retourne la valeur de la profondeur selon la latitude et longitude renseign�es en se basant sur les 4 coins de la
     * cellule et en les moyennant
     *
     * @param latitude
     * @param longitude
     */
    public short findDepthValueWithCellAverage(final Double latitude, final Double longitude) {
	int indexLatitude = 0;
	int indexLongitude = 0;
	Array dataUpperLeft = null;
	Array dataUpperRight = null;
	Array dataLowerLeft = null;
	Array dataLowerRight = null;
	try {

	    if ((getIndexLatitude(latitude) != null) && (getIndexLongitude(longitude) != null)) {
		final long numberOfLat = dataFile.findVariable(latName).getSize();
		final long numberOfLon = dataFile.findVariable(lonName).getSize();

		final Array latValues = dataFile.findVariable(latName).read();
		while ((indexLatitude < (numberOfLat - 1)) && (latitude > latValues.getDouble(indexLatitude + 1))) {
		    indexLatitude++;
		}

		final Array lonValues = dataFile.findVariable(lonName).read();
		while ((indexLongitude < (numberOfLon - 1)) && (longitude > lonValues.getDouble(indexLongitude + 1))) {
		    indexLongitude++;
		}

		try {
		    // Lecture des 4 valeurs de Depth aux coins de la cellule
		    LOGGER.info("findDepthValue : lecture valeur du Depth");
		    final String atributeValuesUpperLeft = indexLatitude + "," + indexLongitude;
		    final String atributeValuesUpperRight = indexLatitude + "," + (indexLongitude + 1);
		    final String atributeValuesLowerLeft = (indexLatitude + 1) + "," + indexLongitude;
		    final String atributeValuesLowerRight = (indexLatitude + 1) + "," + (indexLongitude + 1);

		    dataUpperLeft = variableDepth.read(atributeValuesUpperLeft);
		    dataUpperRight = variableDepth.read(atributeValuesUpperRight);
		    dataLowerLeft = variableDepth.read(atributeValuesLowerLeft);
		    dataLowerRight = variableDepth.read(atributeValuesLowerRight);
		} catch (final IOException | InvalidRangeException e) {
		    LOGGER.error("findDepthValue : erreur de lecture valeur du Depth" + e.getMessage() + " "
			    + dataUpperLeft + " | " + dataUpperRight + " | " + dataLowerLeft + " | " + dataLowerRight);
		}
	    } else {
		LOGGER.error("findDepthValue : index hors domaine");
	    }

	} catch (final Exception e) {
	    LOGGER.error("findDepthValue : erreur de lecture de l' index de latitude ou longitude " + e.getMessage());
	}

	if ((dataUpperLeft == null) || (dataUpperRight == null) || (dataLowerLeft == null)
		|| (dataLowerRight == null)) {
	    return Short.MAX_VALUE;
	}
	return (short) ((dataUpperLeft.getShort(0) + dataUpperRight.getShort(0) + dataLowerLeft.getShort(0)
		+ dataLowerRight.getShort(0)) / 4);
    }

    /**
     * Retourne la valeur de l'index de la latitude donn�e
     *
     * @param latitude
     *            La valeur de la latitude) dont on veut conna�tre l'index
     * @return l'indexe de la valeur renseign�e
     */
    private Object getIndexLatitude(final Double latitude) {

	Object indexlatitude = 0;

	if ((latitude > 90.0) || (latitude <= -90.0)) {
	    LOGGER.error("getIndexLatitude : latitude hors domaine" + latitude);
	    // index non existant
	    indexlatitude = null;

	} else {
	    // calcul de l'index de la latitude variant de -90 � 90�
	    if (latitude >= 0.0) {
		indexlatitude = (90 * 240) + ((Double) (latitude * 240)).intValue();
	    } else {
		indexlatitude = (90 * 240) - ((-1) * ((Double) (latitude * 240)).intValue());
	    }
	}

	return indexlatitude;
    }

    /**
     * Retourne la valeur de l'index de la longitude s�pcifi�e
     *
     * @param longitude
     *            La valeur de la longitud) dont on veut conna�tre l'index
     * @return l'indexe de la valeur renseign�e
     */
    private Object getIndexLongitude(final double longitude) {
	Object indexLongitude = 0;

	if ((longitude > 180.0) || (longitude <= -180.0)) {
	    LOGGER.error("getIndexLongitude : longitude hors domaine" + longitude);
	    // index non existant
	    indexLongitude = null;
	}
	// calcul de l'index de la longitude variant de -180 � 180�
	if (longitude >= 0.0) {
	    indexLongitude = (180 * 240) + ((Double) (longitude * 240)).intValue();
	} else {
	    indexLongitude = (180 * 240) - ((-1) * ((Double) (longitude * 240)).intValue());
	}
	return indexLongitude;
    }

    /**
     * Retourne les valeurs de la variable sp�cifi�e
     *
     * @param ncFile
     *            Le fichier netCDF
     * @param variableName
     *            Le nom de la variable dont on veut r�cup�rer la valeur
     * @return La liste des valeurs de la variable
     */

    public Array getVariableValue(final NetcdfFile ncFile, final String variableName) {
	final Variable variable = ncFile.findVariable(variableName);
	Array dataArray = null;

	try {
	    dataArray = variable.read();
	} catch (final IOException e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	}
	return dataArray == null ? null : dataArray;
    }

    /**
     * Renvois les valeurs de la batyhym�trie de la position et des 8 points autour
     *
     * @param (Double)
     *            latitude
     * @param (Double)
     *            longitude
     * @return (short) tableau de la valeur de bathym�trie � la position et des 8 points autour Les carreaux voisins
     *         sont numerotes dans le sens horaire : ------------- | 1 | 2 | 3 | ------------- | 8 | 0 | 4 |
     *         ------------- | 7 | 6 | 5 | ------------- L'index des tableaux correspond a cette numerotation
     * @throws Exception
     */
    public short[] getSurroundBathyGebco(final Double latitude, final Double longitude) {
	final short[] profondeurs = new short[9];
	int compteurLatitude;
	int compteurLongitude;
	int latitudeVoisine;
	int compteurValeur = 0;
	int longitudeVoisine;
	final short[] valeurs = new short[9];
	int indiceLatitude = 0;
	int indiceLongitude = 0;
	try {
	    indiceLatitude = (Integer) getIndexLatitude(latitude);
	    indiceLongitude = (Integer) getIndexLongitude(longitude);
	} catch (final Exception e) {
	    LOGGER.error("getIndexLongitude  getIndexLatitude" + indiceLatitude + indiceLongitude);
	}

	LOGGER.info("getSurroundBathyEtopo1 : " + latitude + "," + longitude);
	Array data = null;

	for (compteurLatitude = indiceLatitude - 1; compteurLatitude <= (indiceLatitude + 1); compteurLatitude++) {
	    for (compteurLongitude = indiceLongitude
		    - 1; compteurLongitude <= (indiceLongitude + 1); compteurLongitude++) {

		latitudeVoisine = compteurLatitude;
		longitudeVoisine = compteurLongitude;

		try {
		    data = variableDepth.read(latitudeVoisine + "," + longitudeVoisine);
		} catch (final IOException | InvalidRangeException e) {
		    LOGGER.error(
			    "getSurroundBathyGebco : erreur de lecture valeur de la bathy" + e.getMessage() + data);
		}

		if (data != null) {
		    valeurs[compteurValeur] = data.getShort(0);
		}

		if (compteurValeur < (profondeurs.length - 1)) {
		    compteurValeur++;
		}
	    }
	}
	// Rangement du tableau dans l'ordre specifie
	profondeurs[0] = valeurs[4];
	profondeurs[1] = valeurs[0];
	profondeurs[2] = valeurs[1];
	profondeurs[3] = valeurs[2];
	profondeurs[4] = valeurs[5];
	profondeurs[5] = valeurs[8];
	profondeurs[6] = valeurs[7];
	profondeurs[7] = valeurs[6];
	profondeurs[8] = valeurs[3];

	return profondeurs;
    }

}
