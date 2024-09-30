package fr.ifremer.scoop3.model.valueAndQc;

import java.io.Serializable;

import fr.ifremer.scoop3.model.QCValues;

public class FloatValueAndQC extends ValueAndQC implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1016755232316167893L;

    /**
     * If the value is the default value /!\ MUST BE DIFFERENT TO NaN !!
     */
    public static final float DEFAULT_VALUE = Float.MIN_VALUE;

    /**
     * The value
     */
    private float value;

    /**
     * Constructor without QC
     *
     * @param value
     */
    public FloatValueAndQC(final float value) {
	this(value, null);
    }

    /**
     * Default constructor
     *
     * @param value
     * @param qc
     */
    public FloatValueAndQC(final float value, final QCValues qc) {
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
	return new FloatValueAndQC(value, getQc());
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop3.model.valueAndQc.ValueAndQC#getValue()
     */
    @Override
    public Object getValue() {
	return getValueAsFloat();
    }

    /**
     * @return the value as a float
     */
    public float getValueAsFloat() {
	return value;
    }

    @Override
    public void setValue(String newValue) {
	setValue(Float.valueOf(newValue));

    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(final float value) {
	this.value = value;
	qualifyNotSetValue();
    }

    @Override
    /**
     * le QC des metadonnees est positionnees a 9 si non definie ou definie par defaut
     */
    public void qualifyNotSetValue() {
	if (getValue() == null || getValueAsFloat() == DEFAULT_VALUE) {
	    setQc(QCValues.QC_9);
	}
    }

    @Override
    public boolean isDefaultValue() {
	return getValue() == null || getValueAsFloat() == DEFAULT_VALUE;
    }

}
