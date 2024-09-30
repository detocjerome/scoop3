package fr.ifremer.scoop3.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.ifremer.scoop3.model.valueAndQc.ValueAndQC;

public class Platform implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6702537545444854344L;

    /**
     * Platform code
     */
    private String code;

    /**
     * Platform name
     */
    private String label;

    /**
     * Misc metadata
     */
    private final HashMap<String, ValueAndQC> metadata;

    /**
     * Platform's sensors
     */
    private final ArrayList<Sensor> sensors;

    /**
     * List of all observations of sensors
     */
    private final List<Observation> allObservations;

    /**
     * Default constructor
     *
     * @param code
     */
    public Platform(final String code) {
	this(code, null);
    }

    /**
     * Default constructor
     *
     * @param code
     * @param label
     */
    public Platform(final String code, final String label) {
	this.code = code;
	this.label = label;
	metadata = new HashMap<>();
	sensors = new ArrayList<>();
	allObservations = new ArrayList<>();
    }

    /**
     * add a new metadata - use fr.ifremer.scoop3.bpc.io.medatlas.reader.Metadata for medatlas keys
     *
     * @param key
     * @param value
     */
    public void addMetadata(final String key, final ValueAndQC value) {
	metadata.put(key, value);
    }

    /**
     * @return the code
     */
    public String getCode() {
	return code;
    }

    /**
     * @return the label
     */
    public String getLabel() {
	return label;
    }

    /**
     * @return the metadata
     */
    public HashMap<String, ValueAndQC> metadata() {
	return metadata;
    }

    /**
     *
     * @param key
     * @return metadata value for given key - medatlas keys are defined in
     *         fr.ifremer.scoop3.bpc.io.medatlas.reader.Metadata
     */
    public ValueAndQC getMetadata(final String key) {
	return metadata.get(key);
    }

    /**
     * @return a new Platform (without the sensors)
     */
    public Platform getNewEmptyPlatform() {

	final Platform newPlatform = new Platform(getCode(), getLabel());

	/*
	 * Copy Metadata
	 */
	for (final String metedataKey : metadata.keySet()) {
	    newPlatform.addMetadata(metedataKey, getMetadata(metedataKey));
	}

	return newPlatform;
    }

    /**
     * Get all observations
     *
     * @return
     */
    public List<Observation> getAllObservations() {
	return allObservations;
    }

    /**
     * Check if the Platform has already this Sensor. If exists, return the Sensor already referenced. If not, return
     * the "sensor".
     *
     * @param sensor
     * @return
     */
    public Sensor getSensor(final Sensor sensor) {
	for (final Sensor ptfSensor : sensors) {
	    if (ptfSensor.equals(sensor)) {
		return ptfSensor;
	    }
	}
	return sensor;
    }

    /**
     * @return the sensors
     */
    public ArrayList<Sensor> getSensors() {
	return sensors;
    }

    /**
     * @return true if there is no other observation for this platform
     */
    public boolean isFirstObservation() {
	return getSensors().isEmpty() || getAllObservations().isEmpty();
    }

    /**
     * @param code
     *            the code to set
     */
    public void setCode(final String code) {
	this.code = code;
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel(final String label) {
	this.label = label;
    }

    /**
     * Add a new Sensor to the Platform
     *
     * @param sensor
     */
    void addSensor(final Sensor sensor) {
	if (!sensors.contains(sensor)) {
	    sensors.add(sensor);
	    sensor.setPlatform(this);
	    allObservations.addAll(sensor.getObservations());
	}
    }
}
