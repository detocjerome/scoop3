package fr.ifremer.scoop3.model.valueAndQc;

import java.io.Serializable;

import fr.ifremer.scoop3.model.QCValues;

public abstract class ValueAndQC implements Cloneable, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -302294275232804985L;
    /**
     * The QC of the Metadata. If not exists: qc == null
     */
    private QCValues qc;

    /**
     * Default constructor
     *
     * @param qc
     */
    protected ValueAndQC(final QCValues qc) {
	setQc(qc);
    }

    /**
     * @return the qc
     */
    public QCValues getQc() {
	return qc;
    }

    /**
     *
     * @return the int qcValue. Attention au mapping interne scoop
     */
    public int getQCIntValue() {
	return getQc().getQCValue();
    }

    /**
     * @return the value
     */
    public abstract Object getValue();

    /**
     * @param qc
     *            the qc to set
     */
    public void setQc(final QCValues qc) {
	this.qc = qc;
    }

    /**
     * Clone the ValueAndQC
     */
    @Override
    public abstract ValueAndQC clone();

    /**
     * le QC des metadonnees est positionnees a 9 si non definie ou definie par defaut
     */
    public abstract void qualifyNotSetValue();

    /**
     *
     * @return VRAI si la Valeur qualifiable est a la valeur par defaut
     */
    public abstract boolean isDefaultValue();

    /**
     * Met a jour la valeur à partir d'une chaine dont le traitement pour iobtenir une valeur compatible est déléguée au
     * classe fille
     * 
     * @param newValue
     */
    public abstract void setValue(String newValue);

}
