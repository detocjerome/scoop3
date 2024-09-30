package fr.ifremer.scoop3.bathyClimato.etopo1;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class Etopo1Reader {

    /**
     * LOGGER used by the whole SCOOP application
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Etopo1Reader.class);

    private static NetcdfFile dataFile = null;
    private static String depthMissingValue;

    /* File to parse for retrieving bathy */
    private static final String DATAFILE_STRING = FileConfig.getScoop3FileConfig().getString("bathymetry.etopo1.datafile")
	    .trim();

    private static Etopo1Reader instance;

    /**
     * La variable DEPTH
     */
    private static Variable variableDepth;

    public Etopo1Reader() throws Etopo1ReaderException {

	// NetcdfFile dataFile = null;
	if (dataFile == null) {
	    try {
		// Lecture du fichier netcdf
		LOGGER.info("Etopo1Reader : Ouverture Fichier");
		dataFile = NetcdfFile.open(DATAFILE_STRING, null);
		variableDepth = dataFile.findVariable("DEPTH");
		depthMissingValue = variableDepth.findAttribute("missing_value").getValues().toString();
	    } catch (final IOException e) {
		SC3Logger.LOGGER.error("Etopo 1 doesn't exist at this adress : " + DATAFILE_STRING);
		// throw new Etopo1ReaderException(Etopo1ReaderException.CREATE_NEW_ETOPO1READER + " : ", e);
	    }
	}
    }

    /**
     * @return an instance of Etopo1Reader
     * @throws Etopo1ReaderException
     */
    public static synchronized Etopo1Reader getInstance() throws Etopo1ReaderException {
	if (instance == null) {
	    instance = new Etopo1Reader();
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

		    if (!atributeValues.equals(depthMissingValue)) {
			data = variableDepth.read(atributeValues);
		    } else {
			return Short.MAX_VALUE;
		    }
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
	    // indexno existante
	    indexlatitude = null;

	} else {
	    // caclul de l'index de la latitude variant de -90 � 90�
	    if (latitude >= 0.0) {
		indexlatitude = (90 * 60) + ((Double) (latitude * 60)).intValue();
	    } else {
		indexlatitude = (90 * 60) - ((-1) * ((Double) (latitude * 60)).intValue());
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
	// caclul de l'index de la longitude varaiant de -180 � 180�
	if (longitude >= 0.0) {
	    indexLongitude = (180 * 60) + ((Double) (longitude * 60)).intValue();
	} else {
	    indexLongitude = (180 * 60) - ((-1) * ((Double) (longitude * 60)).intValue());
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
    public short[] getSurroundBathyEtopo1(final Double latitude, final Double longitude) {
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
			    "getSurroundBathyEtopo1 : erreur de lecture valeur de la bathy" + e.getMessage() + data);
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
