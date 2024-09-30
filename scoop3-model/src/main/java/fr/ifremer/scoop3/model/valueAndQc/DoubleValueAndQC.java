package fr.ifremer.scoop3.model.valueAndQc;

import java.io.Serializable;

import fr.ifremer.scoop3.model.QCValues;

public class DoubleValueAndQC extends ValueAndQC implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7452017090681080726L;

    /**
     * If the value is the default value. /!\ MUST BE DIFFERENT TO NaN !!
     */
    public static final double DEFAULT_VALUE = Double.NEGATIVE_INFINITY;

    /**
     * The value of the Metadata
     */
    private Double value;

    /**
     * Constructor without QC
     *
     * @param value
     */
    public DoubleValueAndQC(final double value) {
	this(value, null);
    }

    /**
     * Default constructor
     *
     * @param value
     * @param qc
     */
    public DoubleValueAndQC(final Double value, final QCValues qc) {
	super(qc);
	setValue(value);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.model.valueAndQc.ValueAndQC#clone()
     */
    @Override
    public ValueAndQC clone() {
	return new DoubleValueAndQC(value, getQc());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.model.valueAndQc.ValueAndQC#getValue()
     */
    @Override
    public Object getValue() {
	return getValueAsDouble();
    }

    /**
     * @return the value as a double
     */
    public Double getValueAsDouble() {
	return value;
    }

    @Override
    public void setValue(final String newValue) {
	setValue(Double.valueOf(newValue));
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(final Double value) {
	this.value = value;
	qualifyNotSetValue();
    }

    @Override
    /**
     * le QC des metadonnees est positionnees a 9 si non definie ou definie par defaut
     */
    public void qualifyNotSetValue() {
	if (isDefaultValue()) {
	    setQc(QCValues.QC_9);
	}
    }

    @Override
    public boolean isDefaultValue() {
	return (getValue() == null) || (getValueAsDouble() == DEFAULT_VALUE);
    }

}
