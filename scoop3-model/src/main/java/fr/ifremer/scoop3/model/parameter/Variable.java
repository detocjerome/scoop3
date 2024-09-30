package fr.ifremer.scoop3.model.parameter;

import java.io.Serializable;
import java.util.HashMap;

import fr.ifremer.scoop3.model.parameter.Parameter.LINK_PARAM_TYPE;
import fr.ifremer.scoop3.model.valueAndQc.ValueAndQC;

public class Variable<T extends Number> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6832739088702601034L;

    /**
     * Parameter code
     */
    private String code;

    /**
     * The fillValue should be out of the range validMin:validMax
     */
    private T fillValue;

    /**
     * 
     */
    private LINK_PARAM_TYPE linkParamType;
    /**
     * Dataset metadata
     */
    private HashMap<String, ValueAndQC> metadata;

    /**
     * Unit of the Parameter
     */
    private String unit;

    /**
     * The maximal valid value. A value could be greater than the validMax, but its QC is set to "wrong value".
     */
    private T validMax;
    /**
     * The minimal valid value. A value could be smaller than the validMin, but its QC is set to "wrong value".
     */
    private T validMin;

    /**
     * Default constructor
     * 
     */
    public Variable() {
	validMax = null;
	validMin = null;
	fillValue = null;
	metadata = new HashMap<>();
	setLinkParamType(null);
    }

    public String getCode() {
	return code;
    }

    public T getFillValue() {
	return fillValue;
    }

    /**
     * @return the computedType
     */
    public LINK_PARAM_TYPE getLinkParamType() {
	return linkParamType;
    }

    public HashMap<String, ValueAndQC> getMetadata() {
	return metadata;
    }

    public String getUnit() {
	return unit;
    }

    public T getValidMax() {
	return validMax;
    }

    public T getValidMin() {
	return validMin;
    }

    public void setCode(final String code) {
	this.code = code;
    }

    public void setFillValue(final T fillValue) {
	this.fillValue = fillValue;
    }

    /**
     * @param linkParamType
     *            the linkParamType to set
     */
    public void setLinkParamType(final LINK_PARAM_TYPE linkParamType) {
	this.linkParamType = linkParamType;
    }

    public void setMetadata(final HashMap<String, ValueAndQC> metadata) {
	this.metadata = metadata;
    }

    public void setUnit(final String unit) {
	this.unit = unit;
    }

    public void setValidMax(final T validMax) {
	this.validMax = validMax;
    }

    public void setValidMin(final T validMin) {
	this.validMin = validMin;
    }

}
