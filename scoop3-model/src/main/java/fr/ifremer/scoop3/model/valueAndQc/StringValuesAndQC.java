package fr.ifremer.scoop3.model.valueAndQc;

import java.io.Serializable;
import java.util.ArrayList;

import fr.ifremer.scoop3.model.QCValues;

public class StringValuesAndQC extends ValueAndQC implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -9130870352249311782L;
    /**
     * The values of the Metadata
     */
    private final ArrayList<String> values;

    /**
     * constructor
     *
     */
    public StringValuesAndQC() {
	super(null);
	values = new ArrayList<>();
    }

    /**
     * Constructor without QC
     *
     * @param firstValue
     */
    public StringValuesAndQC(final String firstValue) {
	this(firstValue, null);
    }

    /**
     * Default constructor
     *
     * @param firstValue
     * @param qc
     */
    public StringValuesAndQC(final String firstValue, final QCValues qc) {
	super(qc);
	values = new ArrayList<>();
	addValue(firstValue);
    }

    /**
     * @param value
     *            the value to add
     */
    public void addValue(final String value) {
	values.add(value);
    }

    /**
     * @param value
     *            the value to add
     * @param index
     */
    public void addValueAtIndex(final int index, final String value) {
	values.add(index, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.model.valueAndQc.ValueAndQC#clone()
     */
    @Override
    public ValueAndQC clone() {
	final StringValuesAndQC toReturn = new StringValuesAndQC(values.get(0), getQc());
	for (int index = 1; index < values.size(); index++) {
	    toReturn.addValue(values.get(index));
	}
	return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.model.valueAndQc.ValueAndQC#getValue()
     */
    @Override
    public Object getValue() {
	return getValuesAsString().get(0);
    }

    /**
     * @return the values
     */
    public ArrayList<String> getValuesAsString() {
	return values;
    }

    @Override
    /**
     * le QC des metadonnees est positionnees a 9 si non definie ou definie par defaut
     */
    public void qualifyNotSetValue() {
	// TODO use in Dyfamed Driver Voir avec Mick Garo

    }

    @Override
    public boolean isDefaultValue() {
	// TODO use in Dyfamed Driver Voir avec Mick Garo
	return false;
    }

    @Override
    public void setValue(final String newValue) {
	// empty method
    }

}
