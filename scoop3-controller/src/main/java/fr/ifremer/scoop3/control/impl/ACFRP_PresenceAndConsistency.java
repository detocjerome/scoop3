package fr.ifremer.scoop3.control.impl;

import fr.ifremer.scoop3.control.AutomaticControlForReferenceParameterData;
import fr.ifremer.scoop3.core.report.validation.model.messages.AutomaticControlStatusMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataLightErrorMessageItem;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;

public class ACFRP_PresenceAndConsistency extends AutomaticControlForReferenceParameterData {

    /**
     * Default constructor
     */
    public ACFRP_PresenceAndConsistency() {
	// empty constructor
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
	return message;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.control.AutomaticControlForReferenceParameterData#isPresenceTest()
     */
    @Override
    public boolean isPresenceTest() {
	return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.control.AutomaticControlForReferenceParameter#performControl(fr.ifremer.scoop3.model.
     * Observation )
     */
    @Override
    public boolean performControl(final Observation obs, final Dataset dataset) {
	boolean controlOK = true;

	if (dataset.getDatasetType() == DatasetType.PROFILE) {
	    if ((obs.getReferenceParameter() != obs.getZ()) || (obs.getReferenceParameter() == null)) {
		controlOK = false;
	    }
	} else if ((dataset.getDatasetType() == DatasetType.TIMESERIE)
		&& ((obs.getReferenceParameter() != obs.getTime()) || (obs.getReferenceParameter() == null))) {
	    controlOK = false;
	}

	if (controlOK) {
	    final int numberOfValuesForReferenceParam = obs.getReferenceParameter().getValues().size();

	    for (final OceanicParameter oceanicParameter : obs.getOceanicParameters().values()) {
		final int numberOfValuesForOceanicParameter = oceanicParameter.getValues().size();
		if (numberOfValuesForOceanicParameter != numberOfValuesForReferenceParam) {
		    controlOK = false;
		}
	    }

	    if (!controlOK) {
		final String paramCode = obs.getReferenceParameter().getCode();
		final String referenceValue = "-";
		final int referenceIndex = -1;
		final QCValues flagAuto = QCValues.QC_4;
		final boolean isErrorOnReferenceParameter = true;
		addErrorMessageItem(new CADataLightErrorMessageItem(obs.getId(),
			Messages.getMessage("controller.automatic-control-for-reference-param.consistency"), paramCode,
			referenceValue, referenceIndex, null, "-", flagAuto, isErrorOnReferenceParameter));

		// Backup new QC
		obs.setQc(QCValues.QC_4);
	    }
	} else {
	    final String paramCode = obs.getReferenceParameter().getCode();
	    final String referenceValue = "-";
	    final int referenceIndex = -1;
	    final QCValues flagAuto = QCValues.QC_4;
	    final boolean isErrorOnReferenceParameter = true;
	    addErrorMessageItem(new CADataLightErrorMessageItem(obs.getId(),
		    Messages.getMessage("controller.automatic-control-for-reference-param.presence"), paramCode,
		    referenceValue, referenceIndex, null, "-", flagAuto, isErrorOnReferenceParameter));

	    // Backup new QC
	    obs.setQc(QCValues.QC_4);
	}

	return controlOK;
    }
}
