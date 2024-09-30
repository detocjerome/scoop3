package fr.ifremer.scoop3.model.valueAndQc;

import java.io.Serializable;

import fr.ifremer.scoop3.model.QCValues;

public class IntValueAndQC extends ValueAndQC implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5864150526605906719L;

    /**
     * If the value is the default value
     */
    public static final int DEFAULT_VALUE = Integer.MIN_VALUE;

    /**
     * The value of the Metadata
     */
    private int value;

    /**
     * Constructor without QC
     *
     * @param value
     */
    public IntValueAndQC(final int value) {
	this(value, null);
    }

    /**
     * Default constructor
     *
     * @param value
     * @param qc
     */
    public IntValueAndQC(final int value, final QCValues qc) {
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
	return new IntValueAndQC(value, getQc());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.model.valueAndQc.ValueAndQC#getValue()
     */
    @Override
    public Object getValue() {
	return getValueAsInt();
    }

    /**
     * @return the value as a int
     */
    public int getValueAsInt() {
	return value;
    }

    @Override
    public void setValue(String newValue) {
	setValue(Integer.valueOf(newValue));

    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(final int value) {
	this.value = value;
	qualifyNotSetValue();
    }

    @Override
    /**
     * le QC des metadonnees est positionnees a 9 si non definie ou definie par defaut
     */
    public void qualifyNotSetValue() {
	if (getValue() == null || getValueAsInt() == DEFAULT_VALUE) {
	    setQc(QCValues.QC_9);
	}
    }

    @Override
    public boolean isDefaultValue() {
	return getValue() == null || getValueAsInt() == DEFAULT_VALUE;
    }

}
