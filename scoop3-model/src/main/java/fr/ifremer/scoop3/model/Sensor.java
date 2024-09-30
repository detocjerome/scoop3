package fr.ifremer.scoop3.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import fr.ifremer.scoop3.model.valueAndQc.DoubleValueAndQC;
import fr.ifremer.scoop3.model.valueAndQc.ValueAndQC;

/**
 *
 * @author Altran
 *
 */
public class Sensor implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1205575263866371781L;
    private DoubleValueAndQC distanceFromBottom;
    private double magneticDeclination;
    private DoubleValueAndQC nominalDepth;

    /**
     * Misc metadata
     */
    private final HashMap<String, ValueAndQC> metadataMisc = new HashMap<String, ValueAndQC>();

    /**
     *
     * @param key
     * @return Misc metadata value for given key - keys are defined in specific IO driver
     */
    public ValueAndQC getMetadata(final String key) {
	return metadataMisc.get(key);
    }

    /**
     * Sensor's observations
     */
    private final ArrayList<Observation> observations;
    private Platform platform;
    private double samplingRateInSeconds;

    /**
     * Default constructor
     */
    public Sensor() {
	distanceFromBottom = null;
	magneticDeclination = DoubleValueAndQC.DEFAULT_VALUE;
	nominalDepth = null;
	samplingRateInSeconds = DoubleValueAndQC.DEFAULT_VALUE;
	observations = new ArrayList<>();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
	if (obj instanceof Sensor) {
	    final Sensor otherSensor = (Sensor) obj;
	    boolean equals = true;
	    equals &= ((distanceFromBottom == otherSensor.getDistanceFromBottom()) //
		    || (distanceFromBottom.getValueAsDouble() == otherSensor.getDistanceFromBottom()
			    .getValueAsDouble()));
	    equals &= (magneticDeclination == otherSensor.getMagneticDeclination());
	    equals &= ((nominalDepth == otherSensor.getNominalDepth()) //
		    || (nominalDepth.getValueAsDouble() == otherSensor.getNominalDepth().getValueAsDouble()));
	    equals &= (samplingRateInSeconds == otherSensor.getSamplingRateInSeconds());
	    return equals;
	} else {
	    return super.equals(obj);
	}
    }

    /**
     * @return the distanceFromBottom
     */
    public DoubleValueAndQC getDistanceFromBottom() {
	return distanceFromBottom;
    }

    /**
     * @return the magneticDeclination
     */
    public double getMagneticDeclination() {
	return magneticDeclination;
    }

    /**
     * @return a new Sensor (without the observations)
     */
    public Sensor getNewEmptySensor() {
	final Sensor newSensor = new Sensor();

	newSensor.setDistanceFromBottom(getDistanceFromBottom());
	newSensor.setMagneticDeclination(getMagneticDeclination());
	newSensor.setNominalDepth(getNominalDepth());
	newSensor.setSamplingRateInSeconds(getSamplingRateInSeconds());

	return newSensor;
    }

    /**
     * @return the nominalDepth
     */
    public DoubleValueAndQC getNominalDepth() {
	return nominalDepth;
    }

    /**
     * @return the observations
     */
    public List<Observation> getObservations() {
	return observations;
    }

    /**
     * @return the platform
     */
    public Platform getPlatform() {
	return platform;
    }

    /**
     * @return the samplingRateInSeconds
     */
    public double getSamplingRateInSeconds() {
	return samplingRateInSeconds;
    }

    /**
     * @param distanceFromBottom
     *            the distanceFromBottom to set
     */
    public void setDistanceFromBottom(final DoubleValueAndQC distanceFromBottom) {
	this.distanceFromBottom = distanceFromBottom;
    }

    /**
     * @param magneticDeclination
     *            the magneticDeclination to set
     */
    public void setMagneticDeclination(final double magneticDeclination) {
	this.magneticDeclination = magneticDeclination;
    }

    /**
     * @param nominalDepth
     *            the nominalDepth to set
     */
    public void setNominalDepth(final DoubleValueAndQC nominalDepth) {
	this.nominalDepth = nominalDepth;
    }

    /**
     * @param platform
     *            the platform to set
     */
    public void setPlatform(final Platform platform) {
	this.platform = platform;
    }

    /**
     * @param samplingRateInSeconds
     *            the samplingRateInSeconds to set
     */
    public void setSamplingRateInSeconds(final double samplingRateInSeconds) {
	this.samplingRateInSeconds = samplingRateInSeconds;
    }

    /**
     * Sort observations by date then imm
     */
    public void sortObservationsByDateAndImm() {
	Collections.sort(observations, (final Observation o1, final Observation o2) -> {
	    int toReturn = 0;
	    if ((o1.getFirstDateTimeClone() != null) && (o2.getFirstDateTimeClone() != null)) {
		final long time1 = o1.getFirstDateTimeClone().getValueAsLong();
		final long time2 = o2.getFirstDateTimeClone().getValueAsLong();
		toReturn = (int) ((time1 - time2) / 1000);
		if (toReturn == 0) {
		    final double imm1 = o1.getZ().getValues().get(0);
		    final double imm2 = o2.getZ().getValues().get(0);

		    toReturn = (int) Math.round(imm1 - imm2);
		}
	    }
	    return toReturn;
	});
    }

    /**
     * Add a new observations.
     *
     * @param observation
     */
    void addObservation(final Observation observation) {
	// remove observation from previous sensor
	if (observation.getSensor() != null) {
	    observation.getSensor().removeObservation(observation);
	}
	// set the new sensor
	observation.setSensor(this);
	// add to observationList
	observations.add(observation);
	this.platform.getAllObservations().add(observation);
    }

    /**
     * Remove observations.
     *
     * @param observation
     */
    void removeObservation(final Observation observation) {
	observations.remove(observation);
	platform.getAllObservations().remove(observation);
    }
}
