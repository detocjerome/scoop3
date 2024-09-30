package fr.ifremer.scoop3.chart.model;

import java.util.ArrayList;
import java.util.List;

import fr.ifremer.scoop3.infra.mail.UnhandledException;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Observation;

public class ChartPhysicalVariable implements ChartDatasetVariable {

    private boolean isADate;

    /** measure increment */
    private boolean isLevelParameter;

    /** pressure or depth */
    private boolean isReferenceParameter;

    private int maxLevel = -1;
    private int maxLevelDepth = -1;

    private String physicalParameter = null;
    private List<Double[]> physicalValuesByStation = null;
    private List<int[]> qcValuesByStation = null;
    private String unit;
    private List<String> platformsCodes = null;
    private List<String> obsList = null;

    /**
     * Constructor
     *
     * @param physicalParameter
     */
    public ChartPhysicalVariable(final String physicalParameter, final String unit) {
	this.physicalParameter = physicalParameter;
	physicalValuesByStation = new ArrayList<>();
	qcValuesByStation = new ArrayList<>();
	platformsCodes = new ArrayList<>();
	obsList = new ArrayList<>();
	isReferenceParameter = false;
	isLevelParameter = false;
	isADate = false;
	this.unit = unit;
    }

    public void addValuesAndQCs(final String platformCode, final List<Observation> observations, final Double[] values,
	    final char[] qc) {

	if (!Dataset.isInstanceReseted()) {
	    if (platformCode != null) {
		platformsCodes.add(platformCode);
	    }

	    if (observations != null) {
		for (int i = 0; i < observations.size(); i++) {
		    if (!obsList.isEmpty()) {
			boolean add = true;
			for (int j = 0; j < obsList.size(); j++) {
			    if (obsList.get(j).equals(observations.get(i).getId())) {
				add = false;
			    }
			}
			if (add) {
			    obsList.add(observations.get(i).getId());
			}
		    } else {
			obsList.add(observations.get(i).getId());
		    }
		}
	    }

	    addValues(values);
	    addQC(qc);
	}
    }

    @Override
    public String getLabel() {
	return physicalParameter;
    }

    /**
     * @return the maxLevel
     */
    public int getMaxLevel() {
	return maxLevel;
    }

    public void setMaxLevel(final int maxLevel) {
	this.maxLevel = maxLevel;
    }

    /**
     * @return the physicalValuesByStation
     */
    public List<Double[]> getPhysicalValuesByStation() {
	return physicalValuesByStation;
    }

    public void setPhysicalValuesByStation(final List<Double[]> physicalValuesByStation) {
	this.physicalValuesByStation = physicalValuesByStation;
    }

    /**
     * @return the platformsCodes
     */
    public List<String> getPlatformsCodes() {
	return platformsCodes;
    }

    public void setPlatformsCodes(final List<String> platformsCodes) {
	this.platformsCodes = platformsCodes;
    }

    /**
     * @return the list of observations
     */
    public List<String> getObervationsList() {
	return obsList;
    }

    public void setObservationsList(final List<String> obsList) {
	this.obsList = obsList;
    }

    /**
     * @return the qcValuesByStation
     */
    public List<int[]> getQcValuesByStation() {
	return qcValuesByStation;
    }

    public void setQcValuesByStation(final List<int[]> qcValuesByStation) {
	this.qcValuesByStation = qcValuesByStation;
    }

    /*
     * Retourne La liste des liste de QC correspondant aux observations la platforme courante La plateforme courante est
     * récupéré a partir de l'index de l'observation courante platformsCode d'index i correspond à qcValuesByStation
     * d'index i
     */
    public List<int[]> getQcValuesAPlatform(final int observationIndex) {

	final List<int[]> result = new ArrayList<>();
	final String currentPlatform = platformsCodes.get(observationIndex);

	for (int i = 0; i < qcValuesByStation.size(); i++) {
	    if (currentPlatform.equals(platformsCodes.get(i))) {
		result.add(qcValuesByStation.get(i));
	    }
	}
	return result;

    }

    /*
     * Retourne La liste des liste de QC correspondant aux observations du dataset
     */
    public List<int[]> getQcValuesADataset() {
	final List<int[]> result = new ArrayList<>();
	for (int i = 0; i < qcValuesByStation.size(); i++) {
	    result.add(qcValuesByStation.get(i));
	}
	return result;
    }

    /*
     * Retourne La liste des liste de valeurs correspondant aux observations la platforme courante La plateforme
     * courante est récupéré a partir de l'index de l'observation courante platformsCode d'index i correspond à
     * physicalValuesByStation d'index i
     */
    public List<Double[]> getPhysicalValuesAPlatform(final int observationIndex) {
	final List<Double[]> result = new ArrayList<>();
	final String currentPlatform = platformsCodes.get(observationIndex);

	for (int i = 0; i < physicalValuesByStation.size(); i++) {
	    if (currentPlatform.equals(platformsCodes.get(i))) {
		result.add(physicalValuesByStation.get(i));
	    }
	}
	return result;
    }

    /*
     * Retourne La liste des liste de valeurs correspondant aux observations du dataset
     */
    public List<Double[]> getPhysicalValuesADataset() {
	final List<Double[]> result = new ArrayList<>();
	for (int i = 0; i < physicalValuesByStation.size(); i++) {
	    result.add(physicalValuesByStation.get(i));
	}
	return result;
    }

    // public void addValues(Object values){
    // physicalValuesByStation.add(Conversions.convertDoubleListToFloatArray((LinkedList<Double>)values));
    // }

    /**
     * @return the unit
     */
    public String getUnit() {
	return unit;
    }

    public void setUnit(final String unit) {
	this.unit = unit;
    }

    // public void addValues(LinkedList<Double> values){
    // float[] floats = Conversions.convertDoubleListToFloatArray(values);
    // physicalValuesByStation.add(floats);
    // }

    // public void addValues(List<Double> values){
    // float[] floats = Conversions.convertDoubleListToFloatArray(values);
    // physicalValuesByStation.add(floats);
    // }

    // public void addValues(List<Number> values){
    // float[] floats = Conversions.convertNumberListToFloatArray(values);
    // physicalValuesByStation.add(floats);
    // }

    /**
     * @return the isADate
     */
    public boolean isADate() {
	return isADate;
    }

    public boolean isLevelParameter() {
	return isLevelParameter;
    }

    /**
     * @return the isReferenceParameter
     */
    public boolean isReferenceParameter() {
	return isReferenceParameter;
    }

    /**
     * Unload data to save memory
     */
    public void prepareForDispose() {
	if (physicalValuesByStation != null) {
	    physicalValuesByStation.clear();
	    physicalValuesByStation = null;
	}
	if (qcValuesByStation != null) {
	    qcValuesByStation.clear();
	    qcValuesByStation = null;
	}
    }

    /**
     * @param isADate
     *            the isADate to set
     */
    public void setIsADate(final boolean isADate) {
	this.isADate = isADate;
    }

    public void setLevelParameter(final boolean isLevelParameter) {
	this.isLevelParameter = isLevelParameter;
    }

    public void setReferenceParameter(final boolean isReferenceParameter) {
	this.isReferenceParameter = isReferenceParameter;
    }

    private void addQC(final char[] qc) {
	final int[] temp = convertCharArrayToIntArray(qc);
	qcValuesByStation.add(temp);
    }

    /**
     * @param qc
     * @return
     */
    private int[] convertCharArrayToIntArray(final char[] qc) {
	final int[] temp = new int[qc.length];
	for (int i = 0; i < qc.length; i++) {
	    switch (qc[i]) {
	    case '0':
		temp[i] = 0;
		break;
	    case '1':
		temp[i] = 1;
		break;
	    case '2':
		temp[i] = 2;
		break;
	    case '3':
		temp[i] = 3;
		break;
	    case '4':
		temp[i] = 4;
		break;
	    case '5':
		temp[i] = 5;
		break;
	    case '6':
		temp[i] = 6;
		break;
	    case '7':
		temp[i] = 7;
		break;
	    case '8':
		temp[i] = 8;
		break;
	    case '9':
		temp[i] = 9;
		break;
	    case 'A':
		temp[i] = -126;
		break;
	    case 'B':
		temp[i] = -125;
		break;
	    case 'Q':
		temp[i] = -127;
		break;
	    case '?':
		temp[i] = -1;
		break;
	    default:
		temp[i] = 0;
	    }
	}
	return temp;
    }

    private void addValues(final Double[] values) {
	if (values.length > maxLevelDepth) {
	    maxLevelDepth = values.length;
	    // Get Size as the "values" is not inserted yet
	    maxLevel = physicalValuesByStation.size();
	}
	try {
	    if (physicalValuesByStation == null) {
		physicalValuesByStation = new ArrayList<>();
	    }
	    physicalValuesByStation.add(values);
	} catch (final NullPointerException e) {
	    final UnhandledException exception = new UnhandledException(
		    "La variable physicalValuesByStation a pour valeur : " + physicalValuesByStation + " / parametre : "
			    + this.physicalParameter,
		    e);
	}
    }

    public void updateQCs(final int observationNumber, final char[] newQCs) {
	while (qcValuesByStation.size() <= observationNumber) {
	    try {
		Thread.sleep(1);
	    } catch (final InterruptedException e) {
		e.printStackTrace();
	    }
	}
	qcValuesByStation.set(observationNumber, convertCharArrayToIntArray(newQCs));
    }

    public void setMaxLevelDepth(final int maxLevelDepth) {
	this.maxLevelDepth = maxLevelDepth;
    }

    public int getMaxLevelDepth() {
	return this.maxLevelDepth;
    }

    public String getPhysicalParameter() {
	return physicalParameter;
    }

    public void setPhysicalParameter(final String physicalParameter) {
	this.physicalParameter = physicalParameter;
    }

}
