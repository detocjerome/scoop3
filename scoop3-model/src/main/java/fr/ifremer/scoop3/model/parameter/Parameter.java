package fr.ifremer.scoop3.model.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.model.DataMode;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.valueAndQc.ValueAndQC;

/**
 * Abstract class defining Scoop3 Parameters
 *
 * @param <T>
 */

public abstract class Parameter<T extends Number> implements Serializable {

    /**
     * Used by Coriolis. Empty value is NOT a Fillvalue. This means that there is no value in the database.
     */
    public static final Double DOUBLE_EMPTY_VALUE = Double.POSITIVE_INFINITY;

    /**
     * Define the link for this OceanicParameter.
     *
     * The relationship is managed in the class ParametersRelationships
     */
    public enum LINK_PARAM_TYPE {
	COMPUTED_CONTROL, //
	COMPUTED_MODIFIABLE, //
	LINKED_PARAMETER, //
    }

    /**
     * constant for unlimited dimension. To be used when initializing a new parameter with unknown size.
     */
    public static final int DIMENSION_UNLIMITED = -1;
    /**
     *
     */
    private static final long serialVersionUID = 894251344774373459L;

    /**
     * Dimension number (could be DIMENSION_UNLIMITED)
     */
    private int dimension;

    /**
     * List of the DataModes
     */
    private List<DataMode> dms;

    /**
     * List of the QCs as a String
     */
    private String qc;

    /**
     * List of the QCs
     */
    protected transient List<QCValues> qcValues;

    /**
     * List of the values
     */
    protected transient List<T> values;

    /**
     * Variable associated to the parameter
     */
    protected Variable<T> variable;

    /**
     * Constructor for DIMENSION_UNLIMITED parameter
     *
     * @param code
     */
    protected Parameter(final String code) {
	this(code, DIMENSION_UNLIMITED);
    }

    /**
     * Default constructor
     *
     * @param code
     * @param dimension
     */
    protected Parameter(final String code, final int dimension) {
	this();
	variable = new Variable<>();
	setCode(code);
	setDimension(dimension);
    }

    /**
     * Copy constructor
     *
     * @param parameter
     * @param code
     */
    protected Parameter(final Parameter<T> parameter, final String code) {
	this(code, DIMENSION_UNLIMITED);
	this.values = parameter.getValues();
	this.qcValues = parameter.getQcValues();
	this.dms = parameter.getDMs();
    }

    protected Parameter() {
	values = new ArrayList<>(); // TODO Here, discuss about LinkedList or ArrayList
	qcValues = new ArrayList<>(); // TODO Here, discuss about LinkedList or ArrayList
	dms = new ArrayList<>();
    }

    /**
     * Add a new Metadata
     *
     * @param key
     * @param value
     */
    public void addMetadata(final String key, final ValueAndQC value) {
	variable.getMetadata().put(key, value);
    }

    /**
     * Add a new record to the parameter
     *
     * @param value
     * @param qcValue
     */
    public void addRecord(final T value, final QCValues qcValue) {
	values.add(value);
	qcValues.add(qcValue);
    }

    /**
     * Add a new record to the parameter at the desired index
     *
     * @param value
     * @param qcValue
     * @param index
     */
    public void addRecordAtIndex(final int index, final T value, final QCValues qcValue) {
	values.add(index, value);
	qcValues.add(index, qcValue);
    }

    /**
     * Remove a record from the parameter
     *
     * @param index
     * @param qcValue
     */
    public void removeRecord(final int index) {
	values.remove(index);
	qcValues.remove(index);
    }

    /**
     * Add a new record to the parameter and its DataMode
     *
     * @param value
     * @param qcValue
     */
    public void addRecord(final T value, final QCValues qcValue, final DataMode dm) {
	addRecord(value, qcValue);
	dms.add(dm);
    }

    /**
     * Set a current record to the parameter
     *
     * @param index
     * @param value
     * @param qcValue
     */
    public void setRecord(final int index, final T value, final QCValues qcValue) {
	values.set(index, value);
	qcValues.set(index, qcValue);
    }

    /**
     * Get first valid value index
     *
     * @return index
     */
    public Integer getFirstValidValueIndex() {
	Integer index = Integer.MAX_VALUE;
	int loop = 0;

	for (final T value : getValues()) {
	    if (!((Double) value.doubleValue()).isNaN() && !((Double) value.doubleValue()).isInfinite()) {
		index = loop;
		break;
	    }
	    loop++;
	}
	return index;
    }

    /**
     * Check if the dimension is OK
     *
     * @throws Exception
     */
    public void checkDimension() throws Exception {
	if (values.size() != qcValues.size()) {
	    throw new Exception(getCode() + " " + Messages.getMessage("model.parameter-error") + " - "
		    + Messages.getMessage("model.values-error"));
	}
	if (getDimension() == 1) {
	    if (values.size() != 1) {
		throw new Exception(getCode() + " " + Messages.getMessage("model.parameter-error") + " - "
			+ Messages.getMessage("model.dimension-error") + " (" + values.size() + ")");
	    }
	} else if (getDimension() == DIMENSION_UNLIMITED) {
	    updateDimension();
	} else if (getDimension() != values.size()) {
	    SC3Logger.LOGGER.trace(getCode() + " : " + Messages.getMessage("model.dimension-not-fit"));
	    throw new Exception(getCode() + " " + Messages.getMessage("model.parameter-error") + " : "
		    + Messages.getMessage("model.dimension-not-fit") + " : " + Messages.getMessage("model.dimension")
		    + "=" + getDimension() + "," + Messages.getMessage("model.value-size") + "=" + values.size());
	} else if (getDimension() == values.size()) {
	    SC3Logger.LOGGER.trace(getCode() + " : " + Messages.getMessage("model.dimension-ok"));
	} else {
	    throw new Exception(getCode() + "  " + Messages.getMessage("model.unknown-exception"));
	}
    }

    /**
     * Compute the sum of the measure values
     *
     * @throws Exception
     */
    public Double getCkecksum() {
	Double checksum = 0.0;
	for (final T value : getValues()) {
	    if (!((Double) value.doubleValue()).isNaN() && !((Double) value.doubleValue()).isInfinite()) {
		checksum += value.doubleValue();
	    }
	}
	return checksum;
    }

    /**
     * @return the code
     */
    public String getCode() {
	return variable.getCode();
    }

    /**
     * @return the dimension
     */
    public int getDimension() {
	return dimension;
    }

    /**
     * @return the dMs
     */
    public List<DataMode> getDMs() {
	return dms;
    }

    /**
     *
     * @param indexMin
     * @param indexMax
     * @return a sublist of the DM list between indexes min and max
     */
    public List<DataMode> getDMs(final int indexMin, final int indexMax) {
	return dms.subList(indexMin, indexMax);
    }

    /**
     * @return the fillValue
     */
    public T getFillValue() {
	return variable.getFillValue();
    }

    /**
     * Get the metadata associated to the given key
     *
     * @param key
     * @return
     */
    public ValueAndQC getMetadata(final String key) {
	return variable.getMetadata().get(key);
    }

    /**
     * @return the qc
     */
    public String getQc() {
	return qc;
    }

    /**
     * @return the qcValues
     */
    public List<QCValues> getQcValues() {
	return this.qcValues;
    }

    /**
     *
     * @param indexMin
     * @param indexMax
     * @return a sublist of the QCvalues list between indexes min and max
     */
    public List<QCValues> getQcValues(final int indexMin, final int indexMax) {
	return this.qcValues.subList(indexMin, indexMax);
    }

    /**
     * @return the unit
     */
    public String getUnit() {
	return variable.getUnit();
    }

    /**
     * @return the validMax
     */
    public T getValidMax() {
	return variable.getValidMax();
    }

    /**
     * @return the validMin
     */
    public T getValidMin() {
	return variable.getValidMin();
    }

    /**
     * @return the values
     */
    public List<T> getValues() {
	return values;
    }

    public void setDMs(final List<DataMode> dMs) {
	dms = dMs;
    }

    public void setQcValues(final List<QCValues> qcValues) {
	this.qcValues = qcValues;
    }

    public void setValues(final List<T> values) {
	this.values = values;
    }

    /**
     *
     * @param indexMin
     * @param indexMax
     * @return a sublist of the values list between indexes min and max
     */
    public List<T> getValues(final int indexMin, final int indexMax) {
	return values.subList(indexMin, indexMax);
    }

    /**
     * @return the variable
     */
    public Variable<T> getVariable() {
	return variable;
    }

    /**
     * @param code
     *            the code to set
     */
    public void setCode(final String code) {
	variable.setCode(code);
    }

    /**
     * @param fillValue
     *            the fillValue to set
     */
    public void setFillValue(final T fillValue) {
	variable.setFillValue(fillValue);
    }

    /**
     * @param linkParamType
     *            the linkParamType to set
     */
    public void setLinkParamType(final LINK_PARAM_TYPE linkParamType) {
	variable.setLinkParamType(linkParamType);
    }

    /**
     * @param qc
     *            the qc to set
     */
    public void setQc(final String qc) {
	this.qc = qc;
    }

    /**
     * @param unit
     *            the unit to set
     */
    public void setUnit(final String unit) {
	variable.setUnit(unit);
    }

    /**
     * @param validMax
     *            the validMax to set
     */
    public void setValidMax(final T validMax) {
	variable.setValidMax(validMax);
    }

    /**
     * @param validMin
     *            the validMin to set
     */
    public void setValidMin(final T validMin) {
	variable.setValidMin(validMin);
    }

    /**
     * @param variable
     *            the variable to set
     */
    public void setVariable(final Variable<T> variable) {
	this.variable = variable;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "code:" + getCode() + " unit:" + getUnit() + " dimension: " + getDimension();
    }

    /**
     * Update the dimension value
     */
    public void updateDimension() {
	setDimension(values.size());
    }

    /**
     * @param dimension
     *            the dimension to set
     */
    protected void setDimension(final int dimension) {
	this.dimension = dimension;
    }
}
