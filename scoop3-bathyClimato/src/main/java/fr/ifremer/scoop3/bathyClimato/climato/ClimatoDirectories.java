package fr.ifremer.scoop3.bathyClimato.climato;

import java.util.ArrayList;

public class ClimatoDirectories {

    // tableaux des index pour les grilles irreguliere
    private final ArrayList<String> filePathValue = new ArrayList<String>();

    private final ArrayList<String> filePathVariation = new ArrayList<String>();

    private final ArrayList<Integer> dateValue = new ArrayList<Integer>();

    private final ArrayList<Integer> dateVariation = new ArrayList<Integer>();

    private int nbDirectoryValues = 0;

    private int nbDirectoryVariations = 0;

    public ClimatoDirectories() {
	// empty method
    }

    /**
     * Retourne le chemin de la climatologie associ� (voir constantes)
     * 
     * @param weatherType
     *            String
     * @return path String
     */
    public String getClimatoPath(final String weatherType) {
	return Constantes.getValue(weatherType);
    }

    /**
     * 
     * @param directory
     */
    public void addDirectoryValue(final String directory) {
	this.filePathValue.add(directory);
    }

    /**
     * 
     * @param dateValue
     */
    public void addDateValue(final int dateValue) {
	this.dateValue.add(dateValue);
    }

    /**
     * 
     * @param directory
     */
    public void addDirectoryVariation(final String directory) {
	this.filePathVariation.add(directory);
    }

    /**
     * 
     * @param dateVariation
     */
    public void addDateVariation(final int dateVariation) {
	this.dateVariation.add(dateVariation);
    }

    /**
     * 
     * @param index
     * @return
     */
    public String getDirectoryValue(final int index) {
	if (index >= this.dateValue.size()) {
	    return "";
	} else {
	    return this.filePathValue.get(index);
	}
    }

    /**
     * 
     * @param index
     * @return
     */
    public String getDirectoryVariation(final int index) {
	if (index >= this.dateValue.size()) {
	    return "";
	} else {
	    return this.filePathVariation.get(index);
	}
    }

    /**
     * 
     * @param index
     * @return
     */
    public Integer getDateValue(final int index) {
	if (index >= this.dateValue.size()) {
	    return -1;
	} else {
	    return this.dateValue.get(index);
	}
    }

    /**
     * 
     * @param index
     * @return
     */
    public Integer getDateVariation(final int index) {
	if (index >= this.dateValue.size()) {
	    return -1;
	} else {
	    return this.dateVariation.get(index);
	}
    }

    public void setNbDirectoryValues(final int nbDirectoryValues) {
	this.nbDirectoryValues = nbDirectoryValues;
    }

    public void setNbDirectoryVariations(final int nbDirectoryVariations) {
	this.nbDirectoryVariations = nbDirectoryVariations;
    }

    public int getNbDirectoryValues() {
	return nbDirectoryValues;
    }

    public int getNbDirectoryVariations() {
	return nbDirectoryVariations;
    }

}
