package fr.ifremer.scoop3.model.valueAndQc;

import java.io.Serializable;

import fr.ifremer.scoop3.model.QCValues;

/**
 * Valeur de type chaine de caractère avec un QC NULL
 *
 * @author jdetoc
 *
 */
public class StringValueNoQC extends ValueAndQC implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7595132320900478312L;

    public static final String DEFAULT_VALUE = null;

    /**
     * The value of the Metadata
     */
    private String value;

    /**
     * Constructor without QC
     *
     * @param value
     */
    public StringValueNoQC(final String value) {
	this(value, null);
    }

    /**
     * Default constructor
     *
     * @param value
     * @param qc
     */
    public StringValueNoQC(final String value, QCValues qc) {
	super(qc);
	setValue(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop3.model.valueAndQc.ValueAndQC#getValue()
     */
    @Override
    public Object getValue() {
	return getValueAsString();
    }

    /**
     * @return the value as a String
     */
    public String getValueAsString() {
	return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(final String value) {
	this.value = value;
    }

    @Override
    /**
     * Valeur non qualifiable : Mise du QC a NULL
     */
    public void qualifyNotSetValue() {

	setQc(null);

    }

    @Override
    public boolean isDefaultValue() {
	return getValue() == null || getValue().equals(QCValues.QC_FILL_VALUE);
    }

    @Override
    public ValueAndQC clone() {
	return new StringValueNoQC(value, getQc());
    }

}
