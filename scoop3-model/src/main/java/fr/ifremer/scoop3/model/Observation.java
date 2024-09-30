package fr.ifremer.scoop3.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.model.parameter.LatitudeParameter;
import fr.ifremer.scoop3.model.parameter.LongitudeParameter;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.Parameter;
import fr.ifremer.scoop3.model.parameter.SpatioTemporalParameter;
import fr.ifremer.scoop3.model.parameter.TimeParameter;
import fr.ifremer.scoop3.model.parameter.ZParameter;
import fr.ifremer.scoop3.model.valueAndQc.DoubleValueAndQC;
import fr.ifremer.scoop3.model.valueAndQc.LongValueAndQC;
import fr.ifremer.scoop3.model.valueAndQc.ValueAndQC;

/**
 * Observation with metadata and data. An Observation corresponds to a Medatlas station.
 *
 * @author Altran
 *
 */
public abstract class Observation implements Serializable {

    public static final String LATITUDE_VAR_NAME = "Latitude";
    public static final String LONGITUDE_VAR_NAME = "Longitude";
    public static final String POSITION_VAR_NAME = "Position";
    public static final String TIME_VAR_NAME = "Time";
    public static final String Z_VAR_NAME = "Z";

    /**
     *
     */
    private static final long serialVersionUID = -8534317758237072946L;

    /**
     * Observation comment
     */
    private String comment;
    /**
     * observation unique ID local_cdi_id for Seadatanet files
     */
    private String id;
    /**
     * latitude parameter - in decimal degrees, WGS84
     */
    private LatitudeParameter latitude;
    /**
     * longitude parameter - in decimal degrees, WGS84
     */
    private LongitudeParameter longitude;
    /**
     * Misc metadata
     */
    private final HashMap<String, ValueAndQC> metadata;
    /**
     * The "bathy"
     */
    private DoubleValueAndQC oceanDepth;
    /**
     * Oceanic parameters ...
     */
    private Map<String, OceanicParameter> oceanicParameters;
    /**
     * Global QC for the observation
     */
    private QCValues qc;
    /**
     * observation reference filled with: medatlas reference ; odv ? ; CFPoint ? ; coriolis ?
     */
    private String reference;
    /**
     * Reference on the sensor
     */
    private Sensor sensor;
    /**
     * time parameter
     */
    private TimeParameter time;
    /**
     * z parameter. Can be a depth or a pressure
     */
    private ZParameter z;
    /**
     * corrupted boolean used to know if refParam dim is equal to the param. If there is an error in dimension check,
     * corrupted = true
     */
    private boolean corrupted;

    private boolean timeUnknown = false;

    private boolean usingScoop3 = false;

    protected Observation(final String id) {
	this(id, Parameter.DIMENSION_UNLIMITED);
    }

    protected Observation(final String id, final int size) {
	this.id = id;
	metadata = new HashMap<>();
	oceanicParameters = new HashMap<String, OceanicParameter>();
	initSpatioTemporalParameters(size);
	corrupted = false;
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

    public void addOceanicParameter(final OceanicParameter oceanicParameter) throws Exception {
	oceanicParameter.checkDimension();
	oceanicParameters.put(oceanicParameter.getCode(), oceanicParameter);
    }

    public void checkDimensions() throws Exception {
	SC3Logger.LOGGER.trace(getId() + " : checkDimensions");
	try {
	    getLongitude().checkDimension();
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	    throw e;
	}
	try {
	    getLatitude().checkDimension();
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	    throw e;
	}
	try {
	    getTime().checkDimension();
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	    throw e;
	}
	try {
	    if ((this instanceof Timeserie) && getZ().getValues().isEmpty()) {
		// In Coriolis, Timeseries may have no Z values in DB.
		getZ().setFillValue(Double.MAX_VALUE);
		getZ().addRecord(getZ().getFillValue(), QCValues.QC_9);
	    }
	    getZ().checkDimension();
	} catch (final Exception e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	    throw e;
	}
	Exception excToThrow = null;
	for (final OceanicParameter op : oceanicParameters.values()) {
	    try {
		op.checkDimension();
		if (op.getDimension() != this.getReferenceParameter().getDimension()) {
		    corrupted = true;
		    if (isUsingScoop3()) {
			SC3Logger.LOGGER.trace(op.getCode() + " : dimension (" + op.getDimension()
				+ ") does not fit reference parameter " + this.getReferenceParameter().getCode()
				+ " : dimension (" + getReferenceParameter().getDimension() + ") in the observation "
				+ this.getId());
		    } else {
			throw new Exception(op.getCode() + " : dimension (" + op.getDimension()
				+ ") does not fit reference parameter " + this.getReferenceParameter().getCode()
				+ " : dimension (" + getReferenceParameter().getDimension() + ") in the observation "
				+ this.getId());
		    }
		} else {
		    SC3Logger.LOGGER.trace(op.getCode() + " : dimension (" + op.getDimension()
			    + ") fits reference parameter " + this.getReferenceParameter().getCode() + " : dimension ("
			    + getReferenceParameter().getDimension() + ")");
		}
	    } catch (final Exception e) {
		SC3Logger.LOGGER.error(e.getMessage(), e);
		if (excToThrow == null) {
		    excToThrow = e;
		}
	    }
	}
	if (excToThrow != null) {
	    throw excToThrow;
	}
    }

    public String getComment() {
	return comment;
    }

    /**
     * @return the first Date (if exists) or NULL
     */
    public LongValueAndQC getFirstDateTimeClone() {
	if ((getTime() != null) && !getTime().getValues().isEmpty()) {
	    return new LongValueAndQC(getTime().getValues().get(0), getTime().getQcValues().get(0));
	}
	return null;
    }

    /**
     * @return the first Latitude (if exists) or NULL
     */
    public DoubleValueAndQC getFirstLatitudeClone() {
	if ((getLatitude() != null) && !getLatitude().getValues().isEmpty()) {
	    return new DoubleValueAndQC(getLatitude().getValues().get(0), getLatitude().getQcValues().get(0));
	}
	return null;
    }

    /**
     * @return the first Longitude (if exists) or NULL
     */
    public DoubleValueAndQC getFirstLongitudeClone() {
	if ((getLongitude() != null) && !getLongitude().getValues().isEmpty()) {
	    return new DoubleValueAndQC(getLongitude().getValues().get(0), getLongitude().getQcValues().get(0));
	}
	return null;
    }

    public String getId() {
	return id;
    }

    /**
     * @return the last Date (if exists) or NULL
     */
    public LongValueAndQC getLastDateTimeClone() {
	if ((getTime() != null) && !getTime().getValues().isEmpty()) {
	    return new LongValueAndQC(getTime().getValues().get(getTime().getValues().size() - 1),
		    getTime().getQcValues().get(getTime().getQcValues().size() - 1));
	}
	return null;
    }

    /**
     * @return the last Latitude (if exists) or NULL
     */
    public DoubleValueAndQC getLastLatitudeClone() {
	if ((getLatitude() != null) && !getLatitude().getValues().isEmpty()) {
	    return new DoubleValueAndQC(getLatitude().getValues().get(getLatitude().getValues().size() - 1),
		    getLatitude().getQcValues().get(getLatitude().getQcValues().size() - 1));
	}
	return null;
    }

    /**
     * @return the last Longitude (if exists) or NULL
     */
    public DoubleValueAndQC getLastLongitudeClone() {
	if ((getLongitude() != null) && !getLongitude().getValues().isEmpty()) {
	    return new DoubleValueAndQC(getLongitude().getValues().get(getLongitude().getValues().size() - 1),
		    getLongitude().getQcValues().get(getLongitude().getQcValues().size() - 1));
	}
	return null;
    }

    public LatitudeParameter getLatitude() {
	return latitude;
    }

    public LongitudeParameter getLongitude() {
	return longitude;
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
     * @return the metadata hashmap
     */
    public HashMap<String, ValueAndQC> getMetadatas() {
	return metadata;
    }

    /**
     * @return a copy of this observation, but without any Oceanic parameters or reference parameter.
     */
    public Observation getNewEmptyObservation() {
	final Observation newObservation = getEmptyObservation();

	newObservation.setComment(getComment());
	newObservation.setId(getId());
	newObservation.setLatitude(getLatitude());
	newObservation.setLongitude(getLongitude());

	/*
	 * Copy Metadata
	 */
	for (final String metedataKey : metadata.keySet()) {
	    newObservation.addMetadata(metedataKey, getMetadata(metedataKey));
	}

	newObservation.setOceanDepth(getOceanDepth());
	newObservation.setQc(getQc());
	newObservation.setReference(getReference());

	return newObservation;
    }

    /**
     * @return the oceanDepth
     */
    public DoubleValueAndQC getOceanDepth() {
	return oceanDepth;
    }

    public OceanicParameter getOceanicParameter(final String name) {
	return oceanicParameters.get(name);
    }

    public Map<String, OceanicParameter> getOceanicParameters() {
	return oceanicParameters;
    }

    public QCValues getQc() {
	return qc;
    }

    // /**
    // * @return the Worst QC between TIME, LATITUDE, LONGITUDE and DEPTH
    // */
    // public QCValues getWorstQCExcept9() {
    //
    // QCValues worstQC = QCValues.getWorstQCExcept9(getStartDateTime(), getStartLatitude());
    // worstQC = QCValues.getWorstQCExcept9(worstQC, getStartLongitude());
    // worstQC = QCValues.getWorstQCExcept9(worstQC, getOceanDepth());
    //
    // return worstQC;
    // }

    public String getReference() {
	return reference;
    }

    /**
     * reference parameter: z or time, depending on the datasetType
     */
    public abstract SpatioTemporalParameter<?> getReferenceParameter();

    public Sensor getSensor() {
	return sensor;
    }

    public TimeParameter getTime() {
	return time;
    }

    /**
     * @return the Worst QC between TIME, LATITUDE, LONGITUDE and DEPTH
     */
    public QCValues getWorstQCExcept9() {
	return getWorstQCExcept9ForIndex(0);
    }

    /**
     * @return the Worst QC between TIME, LATITUDE, LONGITUDE and DEPTH for a given Index
     */
    public QCValues getWorstQCExcept9ForIndex(final int index) {
	QCValues worstQC = QCValues.getWorstQCExcept9(//
		new DoubleValueAndQC(getLatitude().getValues().get(index), getLatitude().getQcValues().get(index)), //
		new DoubleValueAndQC(getLongitude().getValues().get(index), getLongitude().getQcValues().get(index)));

	if (getTime().getValues().size() > index) {
	    worstQC = QCValues.getWorstQCExcept9(worstQC,
		    new LongValueAndQC(getTime().getValues().get(index), getTime().getQcValues().get(index)));
	} else {
	    worstQC = QCValues.getWorstQCExcept9(worstQC,
		    new LongValueAndQC(getTime().getValues().get(0), getTime().getQcValues().get(0)));
	}

	if ((getZ() != null) && (getZ().getValues().size() > index)) {
	    worstQC = QCValues.getWorstQCExcept9(worstQC,
		    new DoubleValueAndQC(getZ().getValues().get(index), getZ().getQcValues().get(index)));
	} else {
	    worstQC = QCValues.getWorstQCExcept9(worstQC, getOceanDepth());
	}

	return worstQC;
    }

    public ZParameter getZ() {
	return z;
    }

    public void setComment(final String comment) {
	this.comment = comment;
    }

    public void setId(final String id) {
	this.id = id;
    }

    public void setLatitude(final LatitudeParameter latitude) {
	this.latitude = latitude;
    }

    public void setLongitude(final LongitudeParameter longitude) {
	this.longitude = longitude;
    }

    /**
     * @param oceanDepth
     *            the oceanDepth to set
     */
    public void setOceanDepth(final DoubleValueAndQC oceanDepth) {
	this.oceanDepth = oceanDepth;
    }

    public void setOceanicParameters(final Map<String, OceanicParameter> oceanicParameters) {
	this.oceanicParameters = oceanicParameters;
    }

    public void setQc(final QCValues qc) {
	this.qc = qc;
    }

    public void setReference(final String reference) {
	this.reference = reference;
    }

    public void setTime(final TimeParameter time) {
	this.time = time;
    }

    public void setZ(final ZParameter z) {
	this.z = z;
    }

    /**
     * Update the Dimension of all Oceanic Parameters and Reference Parameter.
     */
    public void updateDimension() {
	for (final OceanicParameter parameter : getOceanicParameters().values()) {
	    parameter.updateDimension();
	}
	getReferenceParameter().updateDimension();
    }

    protected abstract Observation getEmptyObservation();

    /**
     * initializes spatiotemporal parameters
     *
     * @param size
     *            could be Parameter.DIMENSION_UNLIMITED
     */
    protected abstract void initSpatioTemporalParameters(int size);

    /**
     * @param sensor
     */
    void setSensor(final Sensor sensor) {
	this.sensor = sensor;
    }

    public boolean hasParameter(final String name) {

	boolean hasParam = false;
	if ((this.getReferenceParameter().getCode().compareTo(name) == 0)
		|| (this.getOceanicParameters().containsKey(name))) {
	    hasParam = true;
	}
	return hasParam;
    }

    public Parameter<Double> getParameter(final String name) {

	Parameter<Double> parameter = null;
	if (this.getReferenceParameter().getCode().compareTo(name) == 0) {
	    parameter = getZ();
	} else if (this.getOceanicParameters().containsKey(name)) {
	    parameter = getOceanicParameter(name);
	}

	return parameter;
    }

    public void setParameterAsReference(final OceanicParameter param) {

	final ZParameter zParam = getZ();
	zParam.setCode(param.getCode());
	zParam.getDMs().clear();
	zParam.getQcValues().clear();
	zParam.getValues().clear();

	zParam.setValues(param.getValues());
	zParam.setQcValues(param.getQcValues());
	zParam.setDMs(param.getDMs());
    }

    /**
     * Remove all measures of a level
     *
     * @param index
     */
    public void removeLevel(final int index) {

	for (final OceanicParameter parameter : getOceanicParameters().values()) {
	    parameter.removeRecord(index);
	}
	getReferenceParameter().removeRecord(index);
    }

    public boolean isCorrupted() {
	return this.corrupted;
    }

    public boolean isTimeUnknown() {
	return timeUnknown;
    }

    public void setTimeUnknown(final boolean timeUnknown) {
	this.timeUnknown = timeUnknown;
    }

    public boolean isUsingScoop3() {
	return usingScoop3;
    }

    public void setUsingScoop3(final boolean usingScoop3) {
	this.usingScoop3 = usingScoop3;
    }
}
