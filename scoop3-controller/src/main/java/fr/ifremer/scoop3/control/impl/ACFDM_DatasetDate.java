package fr.ifremer.scoop3.control.impl;

import java.util.Date;

import fr.ifremer.scoop3.control.AutomaticControlForDatasetMetadata;
import fr.ifremer.scoop3.core.report.validation.model.messages.AutomaticControlStatusMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAMetadataErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAMetadataErrorMessageItem.QC_TO_UPDATE;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.QCValues;

public class ACFDM_DatasetDate extends AutomaticControlForDatasetMetadata {

    @Override
    public boolean performControl(final Dataset dataset) {
	boolean controlOK = true;

	// Init to QC_1 if the test is OK
	QCValues qcToSetForStartDate = QCValues.getWorstQC(dataset.getStartDate().getQc(), QCValues.QC_1);
	QCValues qcToSetForEndDate = QCValues.getWorstQC(dataset.getEndDate().getQc(), QCValues.QC_1);

	if ((dataset.getStartDate() != null) && (dataset.getEndDate() != null)) {
	    final Date startDate = new Date(dataset.getStartDate().getValueAsLong());
	    final Date endDate = new Date(dataset.getEndDate().getValueAsLong());
	    if (startDate.after(endDate)) {
		controlOK = false;
		qcToSetForStartDate = QCValues.getWorstQC(qcToSetForStartDate, QCValues.QC_4);
		qcToSetForEndDate = QCValues.getWorstQC(qcToSetForEndDate, QCValues.QC_4);

		addErrorMessageItem(new CAMetadataErrorMessageItem(QC_TO_UPDATE.DATASET_END_DATE, null,
			Messages.getMessage("controller.automatic-control-for-dataset.dataset-date") + " ("
				+ Messages.getMessage("bpc-gui.dataset-metadata.end_date") + ")", QCValues.QC_4));
		addErrorMessageItem(new CAMetadataErrorMessageItem(QC_TO_UPDATE.DATASET_START_DATE, null,
			Messages.getMessage("controller.automatic-control-for-dataset.dataset-date") + " ("
				+ Messages.getMessage("bpc-gui.dataset-metadata.start_date") + ")", QCValues.QC_4));
	    }
	}
	// Backup new QC
	dataset.getStartDate().setQc(qcToSetForStartDate);
	dataset.getEndDate().setQc(qcToSetForEndDate);

	return controlOK;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop3.control.AutomaticControlForReferenceParameterData#getAutomaticControlStatus()
     */
    @Override
    public AutomaticControlStatusMessageItem getAutomaticControlStatus() {
	final AutomaticControlStatusMessageItem message = new AutomaticControlStatusMessageItem(getClass()
		.getSimpleName());
	message.setStatus("OK");
	return message;
    }
}
