package fr.ifremer.scoop3.bathyClimato.etopo5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;

public class Etopo5Reader {

    /**
     * LOGGER used by the whole SCOOP application
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Etopo5Reader.class);

    /**
     * CONSTANTES
     */
    public static final boolean STATUS_OK = true;

    public static final boolean STATUS_NOK = false;

    /* File to parse for retrieving bathy */
    private static final String DATAFILE = FileConfig.getScoop3FileConfig().getString("bathymetry.etopo5.datafile")
	    .trim();

    private static Etopo5Reader instance;

    /**
     * @return an instance of Etopo1Reader
     */
    public static synchronized Etopo5Reader getInstance() {
	if (instance == null) {
	    instance = new Etopo5Reader();
	}

	return instance;
    }

    /**
     * Permet la lecture du fichier Etopo5, renvoi la profondeur/hauteur du point et des 8 positions autour de celle-ci
     * Numerotation : ------------- | 1 | 2 | 3 | ------------- | 8 | 0 | 4 | ------------- | 7 | 6 | 5 | -------------
     * Le fichier ouvert est celui dont le chemin est mis en constante
     *
     * @param Latitude
     * @param Longitude
     * @param (short
     *            array)profondeurs tableaux contenant la bathymetrie des 9 points
     * @return
     * @throws Etopo5ReaderException
     */
    protected boolean lireEtopo5(final Double Latitude, final Double Longitude, final short[] profondeurs,
	    final String dataFile) throws Etopo5ReaderException {

	RandomAccessFile lecteur = null;
	int indiceLongitude;
	int indiceLatitude;
	int compteurValeur = 0;
	int compteurLatitude;
	int compteurLongitude;
	double latitudeVoisine;
	double longitudeVoisine;
	int position;
	final short[] valeurs = new short[9];
	boolean status = STATUS_OK;
	// double longitudeCarre;
	// double latitudeCarre;

	LOGGER.info("lireEtopo5 : debut");
	try {
	    LOGGER.info("lireEtopo5 : Ouverture Fichier");
	    final File etopoFile = new File(dataFile);
	    lecteur = new RandomAccessFile(etopoFile, "r");
	    // controle de la longitude
	    if ((Longitude > 180.0) || (Longitude <= -180.0)) {
		status = STATUS_NOK;
		LOGGER.info("lireEtopo5 : Latitude hors domaine");
	    }
	    // controle de la latitude
	    if ((Latitude > 90.0) || (Latitude < -90.0)) {
		status = STATUS_NOK;
		LOGGER.info("lireEtopo5 : longitude hors domaine");
	    }
	    LOGGER.info("lireEtopo5 : Coordonnées correctes");
	    // calcul de l'indice de la longitude variant de 0
	    // � 360*12-1 (359 55' de longitude pour le point demand�)
	    // (il y a douze points par degr�s car la maille est de 5'
	    if (Longitude >= 0.0) {
		indiceLongitude = ((Double) (Longitude * 12.0)).intValue();
	    } else {
		indiceLongitude = ((360 * 12) + ((Double) (Longitude * 12.0)).intValue()) - 1;
	    }
	    // calcul de l'indice de la latitude varaiant de 0
	    // � 180*12 (90 de latiitude pour le point demand�)
	    if (Latitude >= 0.0) {
		indiceLatitude = (90 * 12) - ((Double) (Latitude * 12)).intValue();
	    } else {
		indiceLatitude = ((90 * 12) - ((Double) (Latitude * 12.0)).intValue()) + 1;
	    }
	    // calcul de la position ducentre du carr�
	    // Recherche encore l'utilit� du code ci-dessous
	    // if(Longitude >= 0.0) {
	    // longitudeCarre = ((Integer) ((Double) (Longitude *
	    // 12.0)).intValue()).floatValue() / 12.0 + 2.5 / 60;
	    // } else {
	    // longitudeCarre = ((Integer) ((Double) (Longitude *
	    // 12.0)).intValue()).floatValue() / 12.0 - 2.5 / 60;
	    // }
	    // if(Latitude >= 0.0) {
	    // latitudeCarre = ((Integer) ((Double) (Latitude *
	    // 12.0)).intValue()).floatValue() / 12.0 + 2.5 / 60;
	    // } else {
	    // latitudeCarre = ((Integer) ((Double) (Latitude *
	    // 12.0)).intValue()).floatValue() / 12.0 - 2.5 / 60;
	    // }
	    // if( latitudeCarre > 90.0) {
	    // latitudeCarre = 90.0;
	    // }
	    // if( latitudeCarre < -90.0) {
	    // latitudeCarre = -90.0;
	    // }

	    // pour les 9 positions � consid�rer
	    for (compteurLatitude = indiceLatitude - 1; compteurLatitude <= (indiceLatitude + 1); compteurLatitude++) {
		for (compteurLongitude = indiceLongitude
			- 1; compteurLongitude <= (indiceLongitude + 1); compteurLongitude++) {
		    // latitude au pole Nord
		    if (compteurLatitude <= 0) {
			valeurs[compteurValeur++] = -4290;
			continue;
		    }
		    // Latitude au pole Sud
		    if (compteurLatitude >= 2160) {
			valeurs[compteurValeur++] = 2810;
			continue;
		    }
		    latitudeVoisine = compteurLatitude;
		    // Longitude depassant le 0 en Est/Ouest
		    if (compteurLongitude < 0) {
			longitudeVoisine = 4320 + (double) compteurLongitude;
		    } else if (compteurLongitude > 4319) {
			// Longitude depassant le 0 en Ouest/Est
			longitudeVoisine = (double) compteurLongitude - 4320;
		    } else {
			// Cas Normal
			longitudeVoisine = compteurLongitude;
		    }
		    // positionnement de la bonne valeur dans le fichier
		    position = ((Double) ((latitudeVoisine * 360 * 12 * ((double) Short.SIZE / Byte.SIZE))
			    + (longitudeVoisine * ((double) Short.SIZE / Byte.SIZE)))).intValue();
		    lecteur.seek(position);
		    valeurs[compteurValeur++] = lecteur.readShort();
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

	} catch (final FileNotFoundException e) {
	    status = STATUS_NOK;
	    SC3Logger.LOGGER.error("Etopo 5 doesn't exist at this adress : " + dataFile);
	    // LOGGER.error("Etopo5Reader :" + e.getMessage());
	    // throw new Etopo5ReaderException(Etopo5ReaderException.CREATE_NEW_ETOPO5READER + " : ", e);
	} catch (final Exception e) {
	    status = STATUS_NOK;
	    LOGGER.error("Etopo5Reader :" + e.getMessage());
	} finally {
	    if (lecteur != null) {
		try {
		    lecteur.close();
		    LOGGER.info("getBathyPosition : Fermeture fichier");
		} catch (final IOException e) {
		    e.printStackTrace();
		}
	    }
	}
	return status;
    }

    /**
     * Permet la lecture du fichier Etopo5, renvoi la profondeur/hauteur du point et des 8 positions autour de celle-ci
     *
     * @param (Double)
     *            latitude
     * @param (Double)
     *            longitude
     * @param (short
     *            array) profondeurs tableaux contenant la bathymetrie des 9 points
     *
     * @param (String)
     *            Le chemin du fichier de bathymetrie
     *
     * @return boolean
     * @throws Etopo5ReaderException
     *
     */
    public boolean lireEtopo5FromFile(final Double Latitude, final Double Longitude, final short[] profondeurs,
	    String dataFilePath) throws Etopo5ReaderException {

	if (dataFilePath == null) {
	    dataFilePath = DATAFILE;
	}

	return lireEtopo5(Latitude, Longitude, profondeurs, dataFilePath);
    }

    /**
     * Renvois la bathym�trie de la position
     *
     * @param (Double)
     *            latitude
     * @param (Double)
     *            longitude
     * @return (short) la valeur de la bathym�trie � cette position
     * @throws Etopo5ReaderException
     */
    public short getBathyPosition(final Double latitude, final Double longitude) throws Etopo5ReaderException {
	final short[] profondeurs = new short[9];
	LOGGER.info("getBathyPosition : " + latitude + "," + longitude);
	if (lireEtopo5FromFile(latitude, longitude, profondeurs, null)) {
	    return profondeurs[0];
	} else {
	    return 0;
	}
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
     * @throws Etopo5ReaderException
     */
    public short[] getSurroundBathy(final Double latitude, final Double longitude) throws Etopo5ReaderException {
	final short[] profondeurs = new short[9];
	LOGGER.info("getSurroundBathy : " + latitude + "," + longitude);
	if (lireEtopo5FromFile(latitude, longitude, profondeurs, null)) {
	    // la profondeur � l'index de valeur 0 est la
	    // profondeur du centre du carr�
	    return profondeurs;
	} else {
	    return new short[9];
	}
    }

}
