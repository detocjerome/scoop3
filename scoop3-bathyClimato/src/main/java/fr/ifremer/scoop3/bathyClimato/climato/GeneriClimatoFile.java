package fr.ifremer.scoop3.bathyClimato.climato;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashSet;

import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ifremer.scoop3.infra.logger.SC3Logger;

public class GeneriClimatoFile {

    /**
     * LOGGER used by the whole SCOOP application
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(GeneriClimatoFile.class);

    /**
     * VARIABLES
     */

    protected Integer offset;

    protected RandomAccessFile fichierData;

    protected Integer valid;

    protected String libelleClimatologie;

    protected String labelClimatologie;

    protected String libelleParametre;

    protected String labelParametre;

    protected Integer nbLatitude;

    protected Float pasLatitude;

    protected Float latSud;

    protected Integer nbLongitude;

    protected Float pasLongitude;

    protected Float lonOuest;

    protected Boolean temoin180;

    protected Integer nbDate;

    protected HashSet<String> libelleDate = new HashSet<String>();

    protected HashSet<String> labelDate = new HashSet<String>();

    protected Integer nbImmersion;

    protected Integer[] libelleImmersion;

    protected String libelleUnite;

    protected String labelUnite;

    protected Integer nbCaracteres;

    protected Integer precision;

    protected Float valeurIndefinie;

    protected Boolean temoinValeurInSitu;

    protected Boolean temoinEcartType;

    private float[] longitudeIndexArray;

    private float[] latitudeIndexArray;

    /**
     * Charge l'entete a l'aide d'un fichier header
     *
     * @param (String)
     *            chemin du fichier binaire
     * @return (int) status
     */
    public int loadHeaderFile(final String filePath) {
	final String headerFilePath = filePath.replace(".cli", ".hdr");
	String indexFilePath;
	DataInputStream headReader = null;
	final byte[] stringBuffer = new byte[132];
	int compteur = 0;
	int status = 0;
	int statusIndex = 0;

	try {
	    headReader = new DataInputStream(new BufferedInputStream(new FileInputStream(headerFilePath)));
	    headReader.read(stringBuffer);
	    this.libelleClimatologie = (new String(stringBuffer)).trim();
	    headReader.read(stringBuffer);
	    this.labelClimatologie = (new String(stringBuffer)).trim();
	    headReader.read(stringBuffer);
	    this.libelleParametre = (new String(stringBuffer)).trim();
	    headReader.read(stringBuffer);
	    this.labelParametre = (new String(stringBuffer)).trim();
	    this.nbLatitude = headReader.readInt();
	    this.pasLatitude = headReader.readFloat();
	    this.latSud = headReader.readFloat();
	    this.nbLongitude = headReader.readInt();
	    this.pasLongitude = headReader.readFloat();
	    this.lonOuest = headReader.readFloat();
	    // this.temoin180 = headReader.readBoolean();
	    if (headReader.readInt() == 0) {
		this.temoin180 = false;
	    } else {
		this.temoin180 = true;
	    }
	    this.nbDate = headReader.readInt();
	    for (compteur = 0; compteur < this.nbDate; compteur++) {
		headReader.read(stringBuffer);
		this.libelleDate.add(new String(stringBuffer));
	    }
	    for (compteur = 0; compteur < this.nbDate; compteur++) {
		headReader.read(stringBuffer);
		this.labelDate.add(new String(stringBuffer));
	    }
	    this.nbImmersion = headReader.readInt();
	    this.libelleImmersion = new Integer[this.nbImmersion];
	    for (compteur = 0; compteur < this.nbImmersion; compteur++) {
		this.libelleImmersion[compteur] = headReader.readInt();

	    }
	    headReader.read(stringBuffer);
	    this.libelleUnite = (new String(stringBuffer)).trim();
	    headReader.read(stringBuffer);
	    this.labelUnite = (new String(stringBuffer)).trim();
	    this.nbCaracteres = headReader.readInt();
	    this.precision = headReader.readInt();
	    this.valeurIndefinie = headReader.readFloat();
	    // this.temoinValeurInSitu = headReader.readBoolean(); //.readInt();
	    if (headReader.readInt() == 0) {
		this.temoinValeurInSitu = false;
	    } else {
		this.temoinValeurInSitu = true;
	    }
	    // this.temoinEcartType = headReader.readBoolean();
	    if (headReader.readInt() == 0) {
		this.temoinEcartType = false;
	    } else {
		this.temoinEcartType = true;
	    }

	    try {
		this.fichierData = new RandomAccessFile(filePath, "r");
	    } catch (final FileNotFoundException e) {
		this.valid = Constantes.ERROR_DATA;
		status = Constantes.CLIM_ERR_SYS;
	    }
	    this.valid = Constantes.NO_ERROR;

	    indexFilePath = filePath.substring(0, filePath.length() - 7);
	    if (this.pasLatitude <= 0) {
		statusIndex = this.loadIndexLatitudeArray(indexFilePath + "grid.lat");
		if (statusIndex != 0) {
		    status = statusIndex;
		}
	    }
	    if (this.pasLongitude <= 0) {
		statusIndex = this.loadIndexLongitudeArray(indexFilePath + "grid.lon");
		if (statusIndex != 0) {
		    status = statusIndex;
		}
	    }
	} catch (final IOException e) {
	    this.valid = Constantes.ERROR_DATA;
	    status = Constantes.CLIM_ERR_SYS;
	} finally {
	    if (headReader != null) {
		try {
		    headReader.close();
		} catch (final IOException e) {
		    e.printStackTrace();
		}
	    }
	}

	return status;
    }

    /**
     * lit la valeur dans le fichier binaire contenant les donn�es
     *
     * @param latitude
     * @param longitude
     * @param indexPeriod
     * @param moyenne
     * @throws IOException
     */
    public void readDataValue(final Float latitude, final Float longitude, final Integer indexPeriod,
	    final Float[] moyenne) throws IOException {

	final Slf4JStopWatch swreadDataValue = new Slf4JStopWatch("readDataValue", SC3Logger.PERF4JLOGGER,
		Slf4JStopWatch.DEBUG_LEVEL);
	int compteur = 0;
	int localOffset;

	for (compteur = 0; compteur < this.nbImmersion; compteur++) {
	    localOffset = calculOffset(latitude, longitude, indexPeriod, compteur);
	    if (this.temoinValeurInSitu.booleanValue()) {
		localOffset += (Integer.SIZE / Byte.SIZE);
	    }
	    this.fichierData.seek(localOffset);
	    moyenne[compteur] = this.fichierData.readFloat();
	}
	swreadDataValue.stop();
    }

    /**
     * lit le nombre de valeur in-situ ainsi que l'ecart type dans le fichier binaire
     *
     * @param latitude
     * @param longitude
     * @param indexPeriod
     * @param nbMesures
     * @param variations
     * @throws IOException
     */
    public void readDataVariation(final Float latitude, final Float longitude, final int indexPeriod,
	    final Integer[] nbMesures, final Float[] variations) throws IOException {
	int compteur = 0;
	int localOffset;

	for (compteur = 0; compteur < this.nbImmersion; compteur++) {
	    localOffset = calculOffset(latitude, longitude, indexPeriod, compteur);
	    if (this.temoinValeurInSitu.booleanValue()) {
		this.fichierData.seek(localOffset);
		nbMesures[compteur] = this.fichierData.readInt();
		localOffset += Integer.SIZE / Byte.SIZE;
	    }
	    localOffset += Float.SIZE / Byte.SIZE;
	    if (this.temoinEcartType.booleanValue()) {
		this.fichierData.seek(localOffset);
		variations[compteur] = this.fichierData.readFloat();
	    }
	}
    }

    /**
     * Calcul de l'offset
     *
     * @param latitude
     * @param longitude
     * @param indexPeriod
     * @param indexImmersion
     * @return
     */
    public int calculOffset(final Float latitude, final Float longitude, final int indexPeriod,
	    final int indexImmersion) {
	int deltaLon;
	int deltaLat;
	final int index = 0;
	int status;
	int elementSize;

	if (this.pasLatitude <= 0) {
	    status = rechIndexLat(latitude, index);
	    if (status == Constantes.CLIM_OK) {
		deltaLat = index;
	    } else {
		return status;
	    }
	} else {
	    deltaLat = nint((latitude - this.latSud) / this.pasLatitude);
	}
	if (this.pasLongitude <= 0) {
	    status = rechIndexLon(longitude, index);
	    if (status == Constantes.CLIM_OK) {
		deltaLon = index;
	    } else {
		return status;
	    }
	} else {
	    if (!this.temoin180.booleanValue()) {
		deltaLon = nint((longitude - this.lonOuest) / this.pasLongitude);
	    } else {
		if (longitude >= 0) {
		    deltaLon = nint((longitude - this.lonOuest) / this.pasLongitude);
		} else {
		    deltaLon = nint((180 - longitude - this.lonOuest) / this.pasLongitude);
		}
	    }
	}

	// calcul de la taille d'un ou plusieurs elements
	elementSize = (Float.SIZE / Byte.SIZE) + (this.temoinEcartType.booleanValue() ? (Integer.SIZE / Byte.SIZE) : 0)
		+ (this.temoinValeurInSitu.booleanValue() ? (Float.SIZE / Byte.SIZE) : 0);

	this.offset = (indexPeriod * this.nbLongitude * this.nbLatitude * this.nbImmersion * elementSize)
		+ (deltaLon * this.nbLatitude * this.nbImmersion * elementSize)
		+ (deltaLat * this.nbImmersion * elementSize) + (indexImmersion * elementSize);

	return this.offset;
    }

    /**
     * charge le tableau d'index des latitudes
     *
     * @param fileName
     */
    public int loadIndexLatitudeArray(final String fileName) {
	return loadIndexArray(fileName, this.nbLatitude, this.latitudeIndexArray);
    }

    /**
     * charge le tableau d'index des longitudes
     *
     * @param fileName
     */
    public int loadIndexLongitudeArray(final String fileName) {
	return loadIndexArray(fileName, this.nbLongitude, this.longitudeIndexArray);
    }

    /**
     * charge le tableau d'index d'un type de coordonn�es (longitude ou latitude)
     *
     * @param fileName
     * @param nb
     * @param indexArray
     * @return statut
     */
    private int loadIndexArray(final String fileName, int nb, float[] indexArray) {
	BufferedInputStream reader = null;
	final byte[] buffer = new byte[256];
	int compteur = 0;
	int status = Constantes.CLIM_OK;
	// ouverture du fichier
	try {
	    reader = new BufferedInputStream(new FileInputStream(fileName));
	} catch (final FileNotFoundException e) {
	    status = Constantes.CLIM_ERR_SYS;
	    LOGGER.error(e.getMessage());
	}
	// lecture du nombre de longitude dans l'index
	try {
	    if (reader != null) {
		reader.read(buffer);
		nb = Integer.valueOf(Arrays.toString(buffer));
		// creation du tableau
		indexArray = new float[nb];
		for (compteur = 0; compteur < this.nbLongitude; compteur++) {
		    reader.read(buffer);
		    indexArray[compteur] = Float.valueOf(Arrays.toString(buffer));
		}
	    }
	} catch (final IOException e) {
	    status = Constantes.CLIM_ERR_SYS;
	} finally {
	    try {
		if (reader != null) {
		    reader.close();
		}
	    } catch (final IOException e1) {
		status = Constantes.CLIM_ERR_SYS;
		LOGGER.debug(e1.getMessage());
	    }
	}
	return status;
    }

    /**
     * recherche index de longitude
     *
     * @param longitude
     * @param index
     * @return status
     */
    protected int rechIndexLon(final Float longitude, int index) {
	int compteur;
	float minValue;
	float maxValue;
	float diff;
	int status = Constantes.CLIM_OK;

	if (this.temoin180.booleanValue()) {
	    status = rechIndex(longitude, this.longitudeIndexArray, this.nbLongitude, index);
	} else {
	    // difference entre les 2 premiers points du tableau
	    if (this.longitudeIndexArray[1] < 0) {
		diff = 180 + longitudeIndexArray[1];
		diff += (180 - longitudeIndexArray[0]);
		diff /= 2;
	    } else {
		diff = (longitudeIndexArray[1] - longitudeIndexArray[0]) / 2;
	    }
	    minValue = longitudeIndexArray[0] - diff;
	    // difference entre les 2 derniers points du tableau
	    if (this.longitudeIndexArray[this.nbLongitude - 2] > 0) {
		diff = 180 + this.longitudeIndexArray[this.nbLongitude - 1];
		diff += (180 - this.longitudeIndexArray[this.nbLongitude - 2]);
		diff /= 2;
	    } else {
		diff = (Math.abs(this.longitudeIndexArray[this.nbLongitude - 2])
			- Math.abs(this.longitudeIndexArray[this.nbLongitude - 1])) / 2;
	    }
	    maxValue = this.longitudeIndexArray[this.nbLongitude - 1] + diff;
	    if (longitude >= 0) {
		if (longitude < minValue) {
		    status = Constantes.CLIM_TERRE;
		}
		if (longitude < this.longitudeIndexArray[0]) {
		    index = 0;
		}
		for (compteur = 0; compteur < this.nbLongitude; compteur++) {
		    if (this.longitudeIndexArray[compteur] < 0) {
			diff = 180 + longitudeIndexArray[compteur];
			diff += (180 - longitudeIndexArray[compteur - 1]);
			diff /= 2;
			if (this.longitudeIndexArray[compteur - 1] > diff) {
			    index = compteur;
			} else {
			    index = compteur - 1;
			}
		    }
		    if (longitude > this.longitudeIndexArray[compteur]) {
			continue;
		    }
		    if (longitude == this.longitudeIndexArray[compteur]) {
			index = compteur;
		    }
		    diff = (this.longitudeIndexArray[compteur] - this.longitudeIndexArray[compteur - 1]) / 2;
		    if ((longitude - longitudeIndexArray[compteur - 1]) > diff) {
			index = compteur;
		    } else {
			index = compteur - 1;
		    }
		}
	    } else {
		if (longitude > maxValue) {
		    status = Constantes.CLIM_TERRE;
		}
		if (longitude > this.longitudeIndexArray[this.nbLongitude - 1]) {
		    index = 0;
		}
		for (compteur = 0; compteur < this.nbLongitude; compteur++) {
		    if (this.longitudeIndexArray[compteur] > 0) {
			continue;
		    }
		    if (longitude > this.longitudeIndexArray[compteur]) {
			continue;
		    } else {
			if (this.longitudeIndexArray[compteur - 1] > 0) {
			    diff = 180 + longitudeIndexArray[compteur];
			    diff += (180 - longitudeIndexArray[compteur - 1]);
			    diff /= 2;
			} else {
			    diff = (this.longitudeIndexArray[compteur] - this.longitudeIndexArray[compteur - 1]) / 2;
			}
		    }
		    if ((this.longitudeIndexArray[compteur] - longitude) > diff) {
			index = compteur - 1;
		    } else {
			index = compteur;
		    }
		}
	    }
	}
	return status;
    }

    /**
     * recherche index de latitude
     *
     * @param latitude
     * @param index
     * @return
     */
    protected int rechIndexLat(final Float latitude, final int index) {
	return rechIndex(latitude, this.latitudeIndexArray, this.nbLatitude, index);
    }

    /**
     *
     * @param coord
     * @param indexArray
     * @param nb
     * @param index
     * @return
     */
    protected int rechIndex(final Float coord, final float[] indexArray, final int nb, int index) {
	int compteur;
	float minValue;
	float maxValue;
	float diff;
	int status = Constantes.CLIM_OK;

	// difference entre les 2 premiers points du tableau
	diff = Math.abs(indexArray[1] - indexArray[0]) / 2;
	minValue = indexArray[0] - diff;
	// difference entre les 2 derniers points du tableau
	diff = Math.abs(indexArray[nb - 1] - indexArray[nb - 2]) / 2;
	maxValue = indexArray[nb - 1] + diff;

	if ((coord < minValue) || (coord > maxValue)) {
	    status = Constantes.CLIM_TERRE;
	}
	if (coord < indexArray[0]) {
	    index = 0;
	}
	if (coord > indexArray[nb - 1]) {
	    index = this.nbLatitude - 1;
	}
	for (compteur = 0; compteur < nb; compteur++) {
	    if (coord > indexArray[compteur]) {
		continue;
	    }
	    if (coord == indexArray[compteur]) {
		index = compteur;
	    }
	    diff = (indexArray[compteur] - indexArray[compteur - 1]) / 2;
	    if ((coord - indexArray[compteur - 1]) > diff) {
		index = compteur;
	    } else {
		index = compteur - 1;
	    }
	}
	return status;
    }

    /**
     * Teste la latitude en fonction de la zone de couverture du fichier de climatologie courant
     *
     * @param latitude
     * @return
     */
    public boolean isLatitudeValid(final Float latitude) {
	int deltaLat;
	final int index = 0;
	boolean status = true;

	if (this.pasLatitude <= 0.0) {
	    if (rechIndexLat(latitude, index) != Constantes.CLIM_OK) {
		status = false;
	    }
	} else {
	    deltaLat = nint((latitude - this.latSud) / this.pasLatitude);
	    if (deltaLat <= this.nbLatitude) {
		if ((this.latSud + (deltaLat * this.pasLatitude)) > 90.0) {
		    status = true;
		}
	    } else {
		status = false;
	    }
	}
	return status;
    }

    /**
     * Teste la longitude en fonction de la zone de couverture du fichier de climatologie courant
     *
     * @param longitude
     * @return (boolean)
     */
    public boolean isLongitudeValid(final Float longitude) {
	int deltaLon;
	final int index = 0;
	boolean status = true;

	if (this.pasLongitude <= 0.0) {
	    if (rechIndexLon(longitude, index) != Constantes.CLIM_OK) {
		status = false;
	    }
	} else {
	    if (!this.temoin180.booleanValue()) {
		deltaLon = nint((longitude - this.lonOuest) / this.pasLongitude);
	    } else {
		if (longitude >= 0) {
		    deltaLon = nint((longitude - this.lonOuest) / this.pasLongitude);
		} else {
		    deltaLon = nint((180 - longitude - this.lonOuest) / this.pasLongitude);
		}
	    }
	    if (deltaLon > this.nbLongitude) {
		status = false;
	    }
	}
	return status;
    }

    /**
     * arrondi un r�el en entier si la partie entiere est �gale � 0.5, le nombre est arrondi � la magnitude sup�rieure
     * ex : 4.5 -> 5 et -4.5 -> -5
     *
     * @param (double)
     * @return (int) valeur arrondie
     */
    protected int nint(final double x) {
	int intVal = 0;
	int signe = 1;

	intVal = (int) x;

	if (x < 0) {
	    signe = -1;
	} else {
	    signe = 1;
	}

	if (Math.abs(x) < (Math.abs(intVal) + 0.5)) {
	    return intVal;
	} else {
	    return (intVal + (signe * 1));
	}
    }

    /**
     * Ferme et nettoie tous les attributs de l'objet
     */
    public void close() {
	if (this.valid != Constantes.ERROR_HEADER) {
	    this.clear();
	}
	if (this.valid == Constantes.NO_ERROR) {
	    try {
		this.fichierData.close();
	    } catch (final IOException e) {
		LOGGER.error("Erreur lors de la fermeture du fichier");
		LOGGER.error(e.getMessage());
	    }
	}
    }

    /**
     * Vide toutes les variables de l'objet
     *
     */
    public void clear() {
	this.labelClimatologie = null;
	this.labelDate.clear();
	this.labelParametre = null;
	this.labelUnite = null;
	this.latSud = null;
	this.libelleClimatologie = null;
	this.libelleDate.clear();
	this.libelleParametre = null;
	this.libelleUnite = null;
	this.lonOuest = null;
	this.nbCaracteres = null;
	this.nbDate = null;
	this.nbImmersion = null;
	this.nbLatitude = null;
	this.nbLongitude = null;
	this.offset = null;
	this.pasLatitude = null;
	this.pasLongitude = null;
	this.precision = null;
	this.temoin180 = null;
	this.temoinEcartType = null;
	this.temoinValeurInSitu = null;
    }

    public Integer[] getLibelleImmersion() {
	return libelleImmersion;
    }

    public Float getValeurIndefinie() {
	return valeurIndefinie;
    }

    public Integer getNbImmersion() {
	return nbImmersion;
    }

}
