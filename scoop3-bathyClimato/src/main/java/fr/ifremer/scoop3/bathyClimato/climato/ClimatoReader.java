package fr.ifremer.scoop3.bathyClimato.climato;

import java.util.ArrayList;
import java.util.Arrays;

import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ifremer.scoop3.infra.logger.SC3Logger;

public class ClimatoReader {

    /**
     * LOGGER used by the whole SCOOP application
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ClimatoReader.class);

    /**
     * VARIABLES
     */
    private Float[] profondeur = new Float[Constantes.NB_IMMERSIONS_MAX_CLIM_HYDRO];

    private Integer[] nbMesures = new Integer[Constantes.NB_IMMERSIONS_MAX_CLIM_HYDRO];

    private Float[] moyennes = new Float[Constantes.NB_IMMERSIONS_MAX_CLIM_HYDRO];

    private Float[] valeurs = new Float[Constantes.NB_IMMERSIONS_MAX_CLIM_HYDRO];

    private Float[] ecarts = new Float[Constantes.NB_IMMERSIONS_MAX_CLIM_HYDRO];

    private Float[] ecartType = new Float[Constantes.NB_IMMERSIONS_MAX_CLIM_HYDRO];

    private Integer[] nbValeurs = new Integer[Constantes.NB_IMMERSIONS_MAX_CLIM_HYDRO];

    private final Float[] valeurInit = new Float[Constantes.NB_IMMERSIONS_MAX_CLIM_HYDRO];

    private Integer nbMaxImmersion = -1;

    private static ClimatoDirectories climDir;

    private ArrayList<Float> climatoValeurIndefinie;

    /**
     * Lit la valeur, le nombre de valeur ainsi que l'ecart type d'un parametre pour une climatologie donn�e. Cette
     * fonction r�alise un "best of", c'est � dire, qu'elle essaye d'avoir le moins de valeur ind�finie possible. Elle
     * initialise la m�thode de recherche dans les r�pertoires
     *
     * @param (String)
     *            type de climato
     * @param longitude
     * @param latitude
     * @param (int)
     *            month
     * @param (String)
     *            parameter parma�tre physique
     * @return (int) status
     */
    public int readClimatoHydro(final String weatherType, final Float latitude, final Float longitude, final int month,
	    final String parameter) {
	int status = Constantes.CLIM_OK;
	climDir = new ClimatoDirectories();
	String path = null;

	path = Constantes.getValue(weatherType);

	if (weatherType.compareTo(Constantes.CLIM_BOBY_CLIM) == 0) {
	    // ordre de recherche pour la valeur
	    climDir.addDirectoryValue(path + "/mois_1x1_month/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(month - 1);
	    climDir.addDirectoryValue(path + "/annee_1x1_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(0);
	    climDir.setNbDirectoryValues(2);
	    // ordre de recherche ecart-type et nombre
	    climDir.addDirectoryVariation(path + "/saison_5x5_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(calcSeasonLev(month));
	    if (climDir.getDateVariation(0) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.setNbDirectoryVariations(1);
	} else if (weatherType.compareTo(Constantes.CLIM_LEVITUS_83) == 0) {
	    // ordre de recherche pour la valeur
	    climDir.addDirectoryValue(path + "/mois_1x1_month/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(month - 1);
	    climDir.addDirectoryValue(path + "/saison_1x1_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(calcSeasonLev98(month));
	    if (climDir.getDateValue(1) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryValue(path + "/annee_1x1_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(0);
	    climDir.setNbDirectoryValues(3);
	    // ordre de recherche ecart-type et nombre
	    climDir.addDirectoryVariation(path + "/mois_1x1_month/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(month - 1);
	    climDir.addDirectoryVariation(path + "/saison_1x1_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(calcSeasonLev98(month));
	    if (climDir.getDateVariation(1) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryVariation(path + "/annee_1x1_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(0);
	    climDir.setNbDirectoryVariations(3);
	} else if (weatherType.compareTo(Constantes.CLIM_LEVITUS_94) == 0) {
	    // ordre de recherche pour la valeur
	    climDir.addDirectoryValue(path + "/mois_1x1_month/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(month - 1);
	    climDir.addDirectoryValue(path + "/saison_1x1_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(calcSeasonLev(month));
	    if (climDir.getDateValue(1) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryValue(path + "/annee_1x1_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(0);
	    climDir.setNbDirectoryValues(3);
	    // ordre de recherche ecart-type et nombre
	    climDir.addDirectoryVariation(path + "/mois_5x5_month/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(month - 1);
	    climDir.addDirectoryVariation(path + "/saison_5x5_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(calcSeasonLev(month));
	    if (climDir.getDateVariation(1) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryVariation(path + "/annee_5x5_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(0);
	    climDir.setNbDirectoryVariations(3);
	} else if (weatherType.compareTo(Constantes.CLIM_LEVITUS_98_1X1) == 0) {
	    // ordre de recherche pour la valeur
	    climDir.addDirectoryValue(path + "/mois_1x1_month/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(month - 1);
	    climDir.addDirectoryValue(path + "/saison_1x1_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(calcSeasonLev98(month));
	    if (climDir.getDateValue(1) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryValue(path + "/annee_1x1_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(0);
	    climDir.setNbDirectoryValues(3);
	    // ordre de recherche ecart-type et nombre
	    climDir.addDirectoryVariation(path + "/mois_1x1_month/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(month - 1);
	    climDir.addDirectoryVariation(path + "/saison_1x1_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(calcSeasonLev98(month));
	    if (climDir.getDateVariation(1) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryVariation(path + "/annee_1x1_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(0);
	    climDir.setNbDirectoryVariations(3);
	} else if (weatherType.compareTo(Constantes.CLIM_LEVITUS_98_5X5) == 0) {
	    // ordre de recherche pour la valeur
	    climDir.addDirectoryValue(path + "/mois_1x1_month/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(month - 1);
	    climDir.addDirectoryValue(path + "/saison_1x1_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(calcSeasonLev98(month));
	    if (climDir.getDateValue(1) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryValue(path + "/annee_1x1_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(0);
	    climDir.setNbDirectoryValues(3);
	    // ordre de recherche ecart-type et nombre
	    climDir.addDirectoryVariation(path + "/mois_5x5_month/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(month - 1);
	    climDir.addDirectoryVariation(path + "/saison_5x5_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(calcSeasonLev98(month));
	    if (climDir.getDateVariation(1) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryVariation(path + "/annee_5x5_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(0);
	    climDir.setNbDirectoryVariations(3);
	} else if (weatherType.compareTo(Constantes.CLIM_LIEGE_MODB2) == 0) {
	    // ordre de recherche pour la valeur
	    climDir.addDirectoryValue(path + "/saison_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(calcSeasonLevModb(month));
	    if (climDir.getDateValue(0) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.setNbDirectoryValues(1);
	    // ordre de recherche ecart-type et nombre
	    // pas d'ecart type
	    climDir.setNbDirectoryVariations(0);
	} else if (weatherType.compareTo(Constantes.CLIM_LIEGE_MODB5) == 0) {
	    // ordre de recherche pour la valeur
	    climDir.addDirectoryValue(path + "/saison_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(calcSeasonLevModb(month));
	    if (climDir.getDateValue(0) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.setNbDirectoryValues(1);
	    // ordre de recherche ecart-type et nombre
	    // pas d'ecart type
	    climDir.setNbDirectoryVariations(0);
	} else if (weatherType.compareTo(Constantes.CLIM_LEVITUS_2001) == 0) {
	    // ordre de recherche pour la valeur
	    climDir.addDirectoryValue(path + "/mois_1x1_month/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(month - 1);
	    climDir.addDirectoryValue(path + "/saison_1x1_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(calcSeasonLev98(month));
	    if (climDir.getDateValue(1) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryValue(path + "/annee_1x1_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(0);
	    climDir.setNbDirectoryValues(3);
	    // ordre de recherche ecart-type et nombre
	    climDir.addDirectoryVariation(path + "/mois_1x1_month/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(month - 1);
	    climDir.addDirectoryVariation(path + "/saison_1x1_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(calcSeasonLev98(month));
	    if (climDir.getDateVariation(1) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryVariation(path + "/annee_1x1_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(0);
	    climDir.setNbDirectoryVariations(3);
	} else if (weatherType.compareTo(Constantes.CLIM_LEVITUS_2005) == 0) {
	    // ordre de recherche pour la valeur
	    climDir.addDirectoryValue(path + "/mois_1x1_month/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(month - 1);
	    climDir.addDirectoryValue(path + "/saison_1x1_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(calcSeasonLev98(month));
	    if (climDir.getDateValue(1) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryValue(path + "/annee_1x1_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(0);
	    climDir.setNbDirectoryValues(3);
	    // ordre de recherche ecart-type et nombre
	    climDir.addDirectoryVariation(path + "/mois_1x1_month/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(month - 1);
	    climDir.addDirectoryVariation(path + "/saison_1x1_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(calcSeasonLev98(month));
	    if (climDir.getDateVariation(1) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryVariation(path + "/annee_1x1_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(0);
	    climDir.setNbDirectoryVariations(3);
	} else if (weatherType.compareTo(Constantes.CLIM_MEDATLAS_MED) == 0) {
	    // ordre de recherche pour la valeur
	    climDir.addDirectoryValue(path + "/saison_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(calcSeasonLev98(month));
	    if (climDir.getDateValue(0) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryValue(path + "/annee_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(0);
	    climDir.setNbDirectoryValues(2);
	    // ordre de recherche ecart-type et nombre
	    climDir.addDirectoryVariation(path + "/saison_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(calcSeasonLev98(month));
	    if (climDir.getDateVariation(0) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryVariation(path + "/annee_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(0);
	    climDir.setNbDirectoryVariations(2);
	} else if (weatherType.compareTo(Constantes.CLIM_MEDATLAS_BLACK) == 0) {
	    // ordre de recherche pour la valeur
	    climDir.addDirectoryValue(path + "/saison_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(calcSeasonLev98(month));
	    if (climDir.getDateValue(0) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryValue(path + "/annee_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(0);
	    climDir.setNbDirectoryValues(2);
	    // ordre de recherche ecart-type et nombre
	    climDir.addDirectoryVariation(path + "/saison_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(calcSeasonLev98(month));
	    if (climDir.getDateVariation(0) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryVariation(path + "/annee_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(0);
	    climDir.setNbDirectoryVariations(2);
	} else if (weatherType.compareTo(Constantes.CLIM_REYNAUD) == 0) {
	    // ordre de recherche pour la valeur
	    climDir.addDirectoryValue(path + "/saison_1x1_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(calcSeasonLev98(month));
	    if (climDir.getDateValue(1) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryValue(path + "/annee_1x1_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateValue(0);
	    climDir.setNbDirectoryValues(2);
	    // ordre de recherche ecart-type et nombre
	    climDir.addDirectoryVariation(path + "/saison_1x1_season/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(calcSeasonLev98(month));
	    if (climDir.getDateVariation(0) == -1) {
		status = Constantes.CLIM_ERR_ARG;
	    }
	    climDir.addDirectoryVariation(path + "/annee_1x1_year/" + parameter.toLowerCase() + ".cli");
	    climDir.addDateVariation(0);
	    climDir.setNbDirectoryVariations(2);
	}

	// lire_climato
	final Slf4JStopWatch swReadClimato = new Slf4JStopWatch("readClimato", SC3Logger.LOGGER,
		Slf4JStopWatch.DEBUG_LEVEL);
	readClimatology(latitude, longitude);
	swReadClimato.stop();

	return status;
    }

    /**
     * Calcul l'indice de la saison en fonction du numero de mois pour les climatologies levitus 83 et 94
     *
     * @param month
     * @return indice de saison
     */
    protected int calcSeasonLev(final int month) {
	int value;
	switch (month) {
	case 2:
	case 3:
	case 4:
	    value = 0;
	    break;
	case 5:
	case 6:
	case 7:
	    value = 1;
	    break;
	case 8:
	case 9:
	case 10:
	    value = 2;
	    break;
	case 11:
	case 12:
	case 1:
	    value = 3;
	    break;
	default:
	    value = -1;
	    break;
	}
	return value;
    }

    /**
     * Calcul l'indice de la saison en fonction du numero de mois pour les climatologies levitus 98
     *
     * @param month
     * @return indice de saison
     */
    protected int calcSeasonLev98(final int month) {
	int value;
	switch (month) {
	case 1:
	case 2:
	case 3:
	    value = 0;
	    break;
	case 4:
	case 5:
	case 6:
	    value = 1;
	    break;
	case 7:
	case 8:
	case 9:
	    value = 2;
	    break;
	case 10:
	case 11:
	case 12:
	    value = 3;
	    break;
	default:
	    value = -1;
	    break;
	}
	return value;
    }

    /**
     * Calcul l'indice de la saison en fonction du numero de mois pour les climatologies MODB La gestion des indices est
     * identique a celle du Levitus 98
     *
     * @param month
     * @return indice de saison
     */
    protected int calcSeasonLevModb(final int month) {
	return calcSeasonLev98(month);
    }

    /**
     * Lit les valeurs dans les diff�rents fichiers initialis�s pour r�aliser le "best of"
     *
     * @param latitude
     * @param longitude
     * @param indexPeriod
     * @return status
     */
    protected int readClimatology(final Float latitude, final Float longitude) {
	GeneriClimatoFile climFile;
	int compteur = 0;
	int compteurImmersion = 0;
	int erreurFichier = 0;
	try {
	    climFile = new GeneriClimatoFile();

	    for (compteur = 0; compteur < climDir.getNbDirectoryValues(); compteur++) {
		// chargement de l'entete
		if (climFile.loadHeaderFile(climDir.getDirectoryValue(compteur)) == Constantes.CLIM_ERR_SYS) {
		    erreurFichier++;
		    continue;
		}
		// test de validit� des coordonn�es saisies
		if (!climFile.isLatitudeValid(latitude)) {
		    return Constantes.CLIM_TERRE;
		}
		if (!climFile.isLongitudeValid(longitude)) {
		    return Constantes.CLIM_TERRE;
		}

		init(climFile, true);

		try {
		    if (compteur == 0) {
			climFile.readDataValue(latitude, longitude, climDir.getDateValue(compteur), this.moyennes);

		    } else {
			climFile.readDataValue(latitude, longitude, climDir.getDateValue(compteur), valeurs);
			for (compteurImmersion = 0; compteurImmersion < this.nbMaxImmersion; compteurImmersion++) {
			    if (climatoValeurIndefinie.contains(this.moyennes[compteurImmersion])) {
				moyennes[compteurImmersion] = valeurs[compteurImmersion];
			    }
			}
		    }
		} catch (final Exception e) {
		    LOGGER.error(e.getMessage());
		    return Constantes.CLIM_ERR_SYS;
		}
		climFile.close();
	    }
	    if (erreurFichier == climDir.getNbDirectoryValues()) {
		return Constantes.CLIM_UNDEF;
	    }
	    for (compteur = 0; compteur < climDir.getNbDirectoryVariations(); compteur++) {

		if (climFile.loadHeaderFile(climDir.getDirectoryVariation(compteur)) == Constantes.CLIM_ERR_SYS) {
		    continue;
		}
		init(climFile, false);

		climFile.readDataVariation(latitude, longitude, climDir.getDateVariation(compteur), nbValeurs,
			ecartType);

		for (compteurImmersion = 0; compteurImmersion < this.nbMaxImmersion; compteurImmersion++) {
		    if (climatoValeurIndefinie.contains(ecarts[compteurImmersion])
			    && !climatoValeurIndefinie.contains(this.moyennes[compteurImmersion])) {
			ecarts[compteurImmersion] = ecartType[compteurImmersion];
			nbMesures[compteurImmersion] = nbValeurs[compteurImmersion];
		    }
		}
		climFile.close();
		if (Constantes.SCOOP) {
		    for (compteur = 0; compteur < climDir.getNbDirectoryVariations(); compteur++) {
			if (climatoValeurIndefinie.contains(this.moyennes[compteur])) {
			    moyennes[compteur] = Constantes.DEFAULT_PAR;
			}
			if (climatoValeurIndefinie.contains(ecarts[compteur])) {
			    ecarts[compteur] = Constantes.DEFAULT_PAR;
			    nbMesures[compteur] = Constantes.DEFAULT_NBV;
			}
		    }
		}
	    }
	} catch (final Exception e) {
	    LOGGER.error(e.getMessage());
	}
	return Constantes.CLIM_OK;
    }

    /**
     * Initialise les differents tableaux
     *
     * @param climFile
     * @param initMoyennesAndValeur
     *            un boul�en indiquant si on initialise moyennes et valeurs
     *
     */
    protected void init(final GeneriClimatoFile climFile, final boolean initMoyennesAndValeur) {

	if (this.nbMaxImmersion < climFile.getNbImmersion()) {
	    // Taille initiale du tableau
	    if (this.nbMaxImmersion == -1) {
		this.nbMaxImmersion = climFile.getNbImmersion();

		initTableau(this.nbMaxImmersion, climFile);

	    } else {

		if (initMoyennesAndValeur) {
		    final Float[] localMoyennes = new Float[this.nbMaxImmersion];
		    final Float[] localValeurs = new Float[this.nbMaxImmersion];

		    // Sauvegarde des �l�ments lus pr�cedement
		    System.arraycopy(this.moyennes, 0, localMoyennes, 0, this.moyennes.length);
		    System.arraycopy(this.valeurs, 0, localValeurs, 0, this.valeurs.length);

		    // initialisation du tableau avec la nouvelle taille
		    this.nbMaxImmersion = climFile.getNbImmersion();
		    initTableau(this.nbMaxImmersion, climFile);
		    // ajout des �lements initiales dans le nouveau tableau
		    System.arraycopy(localMoyennes, 0, this.moyennes, 0, localMoyennes.length);
		    System.arraycopy(localValeurs, 0, this.valeurs, 0, localValeurs.length);

		} else {
		    final Integer[] localNbMesures = new Integer[this.nbMaxImmersion];
		    final Integer[] localNbValeurs = new Integer[this.nbMaxImmersion];
		    final Float[] localEcarts = new Float[this.nbMaxImmersion];
		    final Float[] localEcartType = new Float[this.nbMaxImmersion];
		    final Float[] localProfondeur = new Float[this.nbMaxImmersion];

		    // Sauvegarde des �l�ments lus pr�cedement
		    System.arraycopy(this.nbMesures, 0, localNbMesures, 0, this.nbMesures.length);
		    System.arraycopy(this.nbValeurs, 0, localNbValeurs, 0, this.nbValeurs.length);
		    System.arraycopy(this.ecarts, 0, localEcarts, 0, this.ecarts.length);
		    System.arraycopy(this.ecartType, 0, localEcartType, 0, this.ecartType.length);
		    System.arraycopy(this.profondeur, 0, localProfondeur, 0, this.profondeur.length);

		    // initialisation du tableau avec la nouvelle taille
		    this.nbMaxImmersion = climFile.getNbImmersion();
		    initTableau(this.nbMaxImmersion, climFile);

		    // ajout des �lements initiales dans le nouveau tableau
		    System.arraycopy(localNbMesures, 0, this.nbMesures, 0, localNbMesures.length);
		    System.arraycopy(localNbValeurs, 0, this.nbValeurs, 0, localNbValeurs.length);
		    System.arraycopy(localEcarts, 0, this.ecarts, 0, localEcarts.length);
		    System.arraycopy(localEcartType, 0, this.ecartType, 0, localEcartType.length);
		    System.arraycopy(localProfondeur, 0, this.profondeur, 0, localProfondeur.length);
		}
	    }
	}
    }

    /**
     * Initialisation de la taille et de la valeur du tableau et remplissage par des valeurs par d�faut
     *
     * @param size
     * @param climFile
     */
    private void initTableau(final int size, final GeneriClimatoFile climFile) {

	final float localValeurInit = climFile.getValeurIndefinie();

	// add the valeurInit in the arrayList if it doesn't contain it yet
	if (climatoValeurIndefinie == null) {
	    climatoValeurIndefinie = new ArrayList<Float>();
	}
	if (!climatoValeurIndefinie.contains(localValeurInit)) {
	    climatoValeurIndefinie.add(localValeurInit);
	}

	final Integer[] libelleImmersions = climFile.getLibelleImmersion();
	this.profondeur = new Float[size];
	this.moyennes = new Float[size];
	this.ecarts = new Float[size];
	this.valeurs = new Float[size];
	this.ecartType = new Float[size];
	this.nbMesures = new Integer[size];
	this.nbValeurs = new Integer[size];

	Arrays.fill(this.nbMesures, climFile.getNbImmersion());
	Arrays.fill(this.nbValeurs, climFile.getNbImmersion());
	Arrays.fill(this.moyennes, localValeurInit);
	Arrays.fill(this.ecarts, localValeurInit);
	Arrays.fill(this.valeurs, localValeurInit);
	Arrays.fill(this.ecartType, localValeurInit);

	for (int compteur = 0; compteur < climFile.getNbImmersion(); compteur++) {
	    this.profondeur[compteur] = libelleImmersions[compteur].floatValue();
	}
    }

    /**
     * Initialise le tableau des profondeurs
     *
     * @param nbImmersion
     * @param libelle
     */
    /*
     * protected void initProfondeurs(int nbImmersion,Integer[] libelleImmersions) { int compteur = 0;
     *
     * if (this.nbMaxImmersion < nbImmersion) { this.nbMaxImmersion = nbImmersion; this.profondeur = new
     * Float[this.nbMaxImmersion]; for (compteur = 0; compteur < this.nbMaxImmersion; compteur++) {
     * this.profondeur[compteur] = libelleImmersions[compteur].floatValue(); } } }
     */

    // GETTERS

    public Float[] getEcarts() {
	return ecarts;
    }

    public Float[] getMoyennes() {
	return moyennes;
    }

    public Integer getNbMaxImmersion() {
	return nbMaxImmersion;
    }

    public Integer[] getNbMesures() {
	return nbMesures;
    }

    public Float[] getProfondeur() {
	return profondeur;
    }

    public Float[] getValeurInit() {
	return valeurInit;
    }

    public ArrayList<Float> getClimatoValeurIndefinie() {
	return climatoValeurIndefinie;
    }

}
