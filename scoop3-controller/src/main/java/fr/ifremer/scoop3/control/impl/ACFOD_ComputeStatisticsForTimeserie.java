package fr.ifremer.scoop3.control.impl;

import fr.ifremer.scoop3.control.AutomaticControlForObservationData;
import fr.ifremer.scoop3.controller.ParameterStatisticsForTimeSerie;
import fr.ifremer.scoop3.core.report.validation.model.messages.AutomaticControlStatusMessageItem;
import fr.ifremer.scoop3.gui.home.HomeViewController;
import fr.ifremer.scoop3.io.impl.AbstractDataBaseManager;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.Timeserie;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;

public class ACFOD_ComputeStatisticsForTimeserie extends AutomaticControlForObservationData {

    private int statFacteurSt;

    public ACFOD_ComputeStatisticsForTimeserie() {
	this(null);
    }

    public ACFOD_ComputeStatisticsForTimeserie(final String statFacteurStStr) {
	try {
	    statFacteurSt = Integer.valueOf(statFacteurStStr);
	    ParameterStatisticsForTimeSerie.setStatFacteurSt(statFacteurSt);
	} catch (final NumberFormatException nfe) {
	    statFacteurSt = 4;
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.control.AutomaticControlForReferenceParameterData#getAutomaticControlStatus()
     */
    @Override
    public AutomaticControlStatusMessageItem getAutomaticControlStatus() {
	final AutomaticControlStatusMessageItem message = new AutomaticControlStatusMessageItem(
		getClass().getSimpleName());
	message.setStatus("OK");
	message.addParameter("statFacteurSt", statFacteurSt);
	return message;
    }

    @Override
    public boolean performControl(final Observation obs, final AbstractDataBaseManager abstractDataBaseManager,
	    final HomeViewController homeViewController) {
	if (obs instanceof Timeserie) {
	    for (final OceanicParameter parameter : obs.getOceanicParameters().values()) {
		// Do not check the parameter "HCDT"
		if (!parameter.getCode().equalsIgnoreCase("HCDT")) {
		    final int indexMax = parameter.getValues().size();

		    final ParameterStatisticsForTimeSerie parameterStatisticsForTimeSerie = new ParameterStatisticsForTimeSerie(
			    parameter);

		    for (int index = 0; index < indexMax; index++) {

			final double value = parameter.getValues().get(index);
			final QCValues qc = parameter.getQcValues().get(index);

			// De manière générale, les contrôles automatiques ne doivent pas passer sur les données en
			// valeur par
			// défaut (et donc avec un flag 9).
			if (AutomaticControlForObservationData.isValueToControl(qc)) {

			    final double valueMinusAvg = Math.abs(value - parameterStatisticsForTimeSerie.getAverage());

			    if (valueMinusAvg <= parameterStatisticsForTimeSerie.getNXecartType()) {
				parameter.getQcValues().set(index,
					QCValues.getWorstQC(parameter.getQcValues().get(index), QCValues.QC_1));
			    } else {
				parameter.getQcValues().set(index,
					QCValues.getWorstQC(parameter.getQcValues().get(index), QCValues.QC_2));
			    }

			}
		    }
		}
	    }
	}
	// No error message
	return true;
    }

}
