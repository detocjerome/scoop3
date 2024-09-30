package fr.ifremer.scoop3.control.impl;

import fr.ifremer.scoop3.control.AutomaticControlCheckParametersPresence;
import fr.ifremer.scoop3.core.report.validation.model.messages.AutomaticControlStatusMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataLightErrorMessageItem;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;

public class ACCPP_OnlyReferenceParameter extends AutomaticControlCheckParametersPresence {

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.control.AutomaticControlCheckParametersPresence#performControl(fr.ifremer.scoop3.model.
     * Observation )
     */
    @Override
    public boolean performControl(final Observation obs) {
	final boolean controlOK = !obs.getOceanicParameters().isEmpty();

	if (!controlOK) {
	    final String paramCode = obs.getReferenceParameter().getCode();
	    final String referenceValue = "-";
	    final int referenceIndex = -1;
	    final QCValues flagAuto = QCValues.QC_4;
	    final boolean isErrorOnReferenceParameter = false;
	    addErrorMessageItem(new CADataLightErrorMessageItem(obs.getId(),
		    Messages.getMessage(
			    "controller.automatic-control-check-parameters-presence.only-reference-parameter"),
		    paramCode, referenceValue, referenceIndex, null, "-", flagAuto, isErrorOnReferenceParameter));

	    // Backup new QC
	    obs.setQc(QCValues.QC_4);
	}

	return controlOK;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.control.AutomaticControl#getAutomaticControlStatus()
     */
    @Override
    public AutomaticControlStatusMessageItem getAutomaticControlStatus() {
	final AutomaticControlStatusMessageItem message = new AutomaticControlStatusMessageItem(
		getClass().getSimpleName());
	message.setStatus("OK");
	return message;
    }
}
