package fr.ifremer.scoop3.control.impl;

import java.text.MessageFormat;

import fr.ifremer.scoop3.control.AutomaticControlForObservationData;
import fr.ifremer.scoop3.core.report.validation.model.messages.AutomaticControlStatusMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataLightErrorMessageItem;
import fr.ifremer.scoop3.gui.home.HomeViewController;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.io.impl.AbstractDataBaseManager;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.Timeserie;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;

public class ACFOD_StationPressureConsistencyForTimeserie extends AutomaticControlForObservationData {

    private double pourcentageDistfond;
    private double incertitudeProf;

    public ACFOD_StationPressureConsistencyForTimeserie() {
	this(null);
    }

    public ACFOD_StationPressureConsistencyForTimeserie(final String pourcentageDistfondStr) {
	this(pourcentageDistfondStr, null);
    }

    public ACFOD_StationPressureConsistencyForTimeserie(final String pourcentageDistfondStr,
	    final String incertitudeProfStr) {
	try {
	    pourcentageDistfond = Double.valueOf(pourcentageDistfondStr) / 100d;
	} catch (final NumberFormatException nfe) {
	    pourcentageDistfond = 0.05d;
	}

	try {
	    incertitudeProf = Double.valueOf(incertitudeProfStr) / 100d;
	} catch (final NumberFormatException nfe) {
	    incertitudeProf = 0.05d;
	}
    }

    @Override
    public boolean performControl(final Observation obs, final AbstractDataBaseManager abstractDataBaseManager,
	    final HomeViewController homeViewController) throws Exception {
	boolean controlOK = true;

	final OceanicParameter presParameter = obs.getOceanicParameter("PRES");
	if ((obs instanceof Timeserie) && (presParameter != null)
		&& (obs.getSensor().getDistanceFromBottom() != null)) {
	    final double X = (obs.getSensor().getDistanceFromBottom().getValueAsDouble() * pourcentageDistfond)
		    + (obs.getOceanDepth().getValueAsDouble() * incertitudeProf);
	    final double sensorDepth = obs.getSensor().getNominalDepth().getValueAsDouble();
	    final double minValidPres = sensorDepth - X;
	    final double maxValidPres = sensorDepth + X;

	    for (final Integer index : getIndexesToControl(presParameter)) {
		final double presValue = presParameter.getValues().get(index);
		final QCValues presQC = presParameter.getQcValues().get(index);

		if ((presValue >= minValidPres) && (presValue <= maxValidPres)) {
		    presParameter.getQcValues().set(index, QCValues.getWorstQC(presQC, QCValues.QC_1));
		} else {
		    controlOK = false;

		    presParameter.getQcValues().set(index, QCValues.getWorstQC(presQC, QCValues.QC_3));

		    // Add 1 Message for each error
		    final String paramCode = presParameter.getCode();
		    final int referenceIndex = index;
		    final QCValues flagAuto = QCValues.QC_3;
		    final boolean isErrorOnReferenceParameter = false;
		    addErrorMessageItem(new CADataLightErrorMessageItem(obs,
			    MessageFormat.format(
				    Messages.getMessage(
					    "bpc-controller.automatic-control-for-observation.station-pressure-consistency-for-timeserie"),
				    presValue, minValidPres, maxValidPres),
			    paramCode, referenceIndex, flagAuto, isErrorOnReferenceParameter));
		}
	    }

	}

	return controlOK;
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
	message.addParameter("pourcentageDistfond", pourcentageDistfond);
	message.addParameter("incertitudeProf", incertitudeProf);
	return message;
    }
}
