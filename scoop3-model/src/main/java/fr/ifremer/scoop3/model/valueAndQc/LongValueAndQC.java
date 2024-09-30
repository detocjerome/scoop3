/**
 *
 */
package fr.ifremer.scoop3.model.valueAndQc;

import java.io.Serializable;

import fr.ifremer.scoop3.model.QCValues;

public class LongValueAndQC extends ValueAndQC implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6257024822159911723L;

    /**
     * If the value is the default value
     */
    public static final long DEFAULT_VALUE = Long.MIN_VALUE;

    /**
     * The value of the Metadata
     */
    private long value;

    /**
     * Constructor without QC
     *
     * @param value
     */
    public LongValueAndQC(final long value) {
	this(value, null);
    }

    /**
     * Default constructor
     *
     * @param value
     * @param qc
     */
    public LongValueAndQC(final long value, final QCValues qc) {
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
	return new LongValueAndQC(value, getQc());
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop3.model.valueAndQc.ValueAndQC#getValue()
     */
    @Override
    public Object getValue() {
	return getValueAsLong();
    }

    /**
     * @return the value as a long
     */
    public long getValueAsLong() {
	return value;
    }

    @Override
    public void setValue(String newValue) {
	setValue(Long.valueOf(newValue));

    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(final long value) {
	this.value = value;
	qualifyNotSetValue();
    }

    @Override
    /**
     * le QC des metadonnees est positionnees a 9 si non definie ou definie par defaut
     */
    public void qualifyNotSetValue() {
	if (getValue() == null || getValueAsLong() == DEFAULT_VALUE) {
	    setQc(QCValues.QC_9);
	}
    }

    @Override
    public boolean isDefaultValue() {
	return getValue() == null || getValueAsLong() == DEFAULT_VALUE;
    }
}
