package fr.ifremer.scoop3.model.valueAndQc;

import java.io.Serializable;

import fr.ifremer.scoop3.model.QCValues;

/**
 * Valeur de type chaine de caractère avec un QC non null
 *
 * @author jdetoc
 *
 */
public class StringValueAndQC extends ValueAndQC implements Serializable {

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
    public StringValueAndQC(final String value) {
	this(value, null);
    }

    /**
     * Default constructor
     *
     * @param value
     * @param qc
     */
    public StringValueAndQC(final String value, final QCValues qc) {
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
	return new StringValueAndQC(value, getQc());
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
    @Override
    public void setValue(final String value) {
	this.value = value;
	qualifyNotSetValue();
    }

    @Override
    /**
     * le QC des metadonnees est positionnees a 9 si non definie ou definie par defaut
     */
    public void qualifyNotSetValue() {
	if ((getValue() == null) || "".equals(value) || value.equals(DEFAULT_VALUE)) {
	    setQc(QCValues.QC_9);
	}
    }

    @Override
    public boolean isDefaultValue() {
	return (getValue() == null) || getValue().equals(DEFAULT_VALUE);
    }

}
