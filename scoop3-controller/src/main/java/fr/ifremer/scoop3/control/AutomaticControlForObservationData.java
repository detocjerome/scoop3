package fr.ifremer.scoop3.control;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;

import fr.ifremer.scoop3.gui.home.HomeViewController;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.io.impl.AbstractDataBaseManager;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;

public abstract class AutomaticControlForObservationData extends AutomaticControl {

    /**
     * Controls Data for a single Observation
     *
     * @param obs
     * @param abstractDataBaseManager
     * @param homeViewController
     * @return true if the Control is OK
     * @throws Exception
     */
    public abstract boolean performControl(Observation obs, AbstractDataBaseManager abstractDataBaseManager,
	    HomeViewController homeViewController) throws Exception;

    /**
     * • Les contrôles automatiques ne doivent pas passer sur les données flaggés comme valeur par défaut (flag 9)
     */
    public static boolean isValueToControl(final QCValues qc) {
	return !(qc.equals(QCValues.QC_9));
    }

    /**
     * Pour un parametre donné , Retourne la liste d'index de mesures a controler De manière générale, les contrôles
     * automatiques ne doivent pas passer sur les données en valeur par défaut (et donc avec un flag 9).
     *
     * @throws Exception
     */
    public ArrayList<Integer> getIndexesToControl(final OceanicParameter op) throws Exception {

	final ArrayList<Integer> indexToControl = new ArrayList<Integer>();

	final Iterator<QCValues> qcIt = op.getQcValues().iterator();
	int curIndex = 0;
	QCValues curQc = null;
	for (final Double curValue : op.getValues()) {

	    // Les qc sont dans une autre liste "parallèle au valeurs
	    if (qcIt.hasNext()) {
		curQc = qcIt.next();

	    } else {
		// Observation {0} Paramètre {1} : Le nombre de valeurs {2} et de qcs ne sont pas identiques {3}
		throw new Exception(
			MessageFormat.format(Messages.getMessage("controller.automatic-control.nbValuesAQcsNotSimilar"),
				null, op.getCode(), op.getValues().size(), op.getQcValues().size()));
	    }

	    // Algorithmie pour ajouter ou non l'index
	    if (AutomaticControlForObservationData.isValueToControl(curQc)) {
		indexToControl.add(curIndex);
	    }
	    curIndex++;

	}

	return indexToControl;

    }
}