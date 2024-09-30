package fr.ifremer.scoop3.gui.common;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.MessageItem;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataLightErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAMetadataErrorMessageItem;
import fr.ifremer.scoop3.gui.common.jdialog.ReportJDialog;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableCellDeleted;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableCellQCAndValueUpdate;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableCellQCUpdate;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableQCAndValueUpdate;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableQCUpdate;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableRowsDeleted;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableUpdateAbstract;
import fr.ifremer.scoop3.gui.reference.ReferenceViewModel;
import fr.ifremer.scoop3.gui.utils.Dialogs;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.tools.Conversions;
import fr.ifremer.scoop3.infra.undo_redo.CommentChange;
import fr.ifremer.scoop3.infra.undo_redo.UndoRedoAction;
import fr.ifremer.scoop3.infra.undo_redo.data.DataValueChange;
import fr.ifremer.scoop3.infra.undo_redo.data.QCValueChange;
import fr.ifremer.scoop3.infra.undo_redo.metadata.MetadataValueChange;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.Platform;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.Parameter;
import fr.ifremer.scoop3.model.parameter.Parameter.LINK_PARAM_TYPE;
import fr.ifremer.scoop3.model.parameter.ParametersRelationships;
import fr.ifremer.scoop3.model.valueAndQc.DoubleValueAndQC;
import fr.ifremer.scoop3.model.valueAndQc.FloatValueAndQC;
import fr.ifremer.scoop3.model.valueAndQc.IntValueAndQC;
import fr.ifremer.scoop3.model.valueAndQc.LongValueAndQC;
import fr.ifremer.scoop3.model.valueAndQc.StringValueAndQC;
import fr.ifremer.scoop3.model.valueAndQc.StringValuesAndQC;
import fr.ifremer.scoop3.model.valueAndQc.ValueAndQC;

public abstract class CommonViewModel {

    public static final String MEASURE_NUMBER = "measure_number";

    /**
     * Reference on the Dataset
     */
    protected Dataset dataset;
    /**
     * List of originalQCs by station and levels
     */
    private HashMap<String, Integer> originalQCs;
    /**
     * True if the first Observation is a Profile
     */
    private final boolean datasetContainsProfiles;
    /**
     *
     */
    private final List<List<? extends UndoRedoAction>> listOfRedoableChanges;
    /**
     *
     */
    private final List<List<? extends UndoRedoAction>> listOfUndoableChanges;

    private List<HashMap<String, ValueAndQC>> metadatas = new ArrayList<HashMap<String, ValueAndQC>>();

    /**
     * Reference on the current Report
     */
    private final Report report;

    protected CommonViewModel(final Dataset dataset, final Report report) {
	this.dataset = dataset;
	this.report = report;
	datasetContainsProfiles = (dataset.getDatasetType() == DatasetType.PROFILE);
	listOfUndoableChanges = new ArrayList<>();
	listOfRedoableChanges = new ArrayList<>();
	this.originalQCs = new HashMap<String, Integer>();
    }

    /**
     * @return FALSE if at least 1 error message is not checked
     */
    public boolean areAllErrorsValidated() {
	for (final MessageItem messageItem : report.getStep(STEP_TYPE.Q2_CONTROL_AUTO_DATA).getMessages()) {
	    if ((messageItem instanceof CADataErrorMessageItem)
		    && (!((CAErrorMessageItem) messageItem).isErrorChecked())) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Clear all redoable changes ...
     */
    public void clearRedoableChanges() {
	listOfRedoableChanges.clear();
    }

    /**
     * Clear all undoable changes ...
     */
    public void clearUndoableChanges() {
	listOfUndoableChanges.clear();
    }

    /**
     * @return the datasetContainsProfiles
     */
    public boolean doesDatasetContainProfiles() {
	return datasetContainsProfiles;
    }

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
	return dataset;
    }

    /**
     * Return the Observation for a given index
     *
     * @param index
     * @return
     */
    public Observation getObservation(final int index) {
	return getObservations().get(index);
    }

    /**
     * Return the Observations
     *
     * @return
     */
    public List<Observation> getObservations() {
	return getDataset().getObservations();
    }

    /**
     * Retourne les observations de la plateforme courante (a savoir la plateforme de l'observation courante)
     */
    public List<Observation> getCurrentPlatformObservations(final int currentObservationIndex) {
	return getPlatformForObservation(currentObservationIndex).getAllObservations();
    }

    /**
     * @param obs
     * @param paramName
     * @return
     */
    public ArrayList<Parameter<? extends Number>> getParam(final Observation obs, final String paramName) {
	final ArrayList<Parameter<? extends Number>> param = new ArrayList<Parameter<? extends Number>>();
	if (obs.getReferenceParameter().getCode().equals(paramName)) {
	    param.add(obs.getReferenceParameter());
	} else if (obs.getLatitude().getCode().equals(paramName)) {
	    param.add(obs.getLatitude());
	} else if (obs.getLongitude().getCode().equals(paramName)) {
	    param.add(obs.getLongitude());
	} else if (Observation.POSITION_VAR_NAME.equals(paramName)) {
	    param.add(obs.getLatitude());
	    param.add(obs.getLongitude());
	} else {
	    param.add(obs.getOceanicParameter(paramName));
	}
	return param;
    }

    /**
     * @param obs
     * @param paramName
     * @return
     */
    public ArrayList<Parameter<? extends Number>> getParams(final List<Observation> obsList, final String paramName) {
	final ArrayList<Parameter<? extends Number>> param = new ArrayList<Parameter<? extends Number>>();
	for (final Observation obs : obsList) {
	    if (obs.getReferenceParameter().getCode().equals(paramName)) {
		param.add(obs.getReferenceParameter());
	    } else if (obs.getLatitude().getCode().equals(paramName)) {
		param.add(obs.getLatitude());
	    } else if (obs.getLongitude().getCode().equals(paramName)) {
		param.add(obs.getLongitude());
	    } else if (Observation.POSITION_VAR_NAME.equals(paramName)) {
		param.add(obs.getLatitude());
		param.add(obs.getLongitude());
	    } else {
		param.add(obs.getOceanicParameter(paramName));
	    }
	}
	return param;
    }

    /**
     * Get the Platform which contains the current observation
     *
     * @param index
     * @return
     */
    public Platform getPlatformForObservation(final int index) {
	final Observation obs = getObservation(index);
	for (final Platform platform : getDataset().getPlatforms()) {
	    if (platform.getAllObservations().contains(obs)) {
		return platform;
	    }
	}
	return null;
    }

    /**
     * Get the Platform which contains the observation
     *
     * @param index
     * @return
     */
    public Platform getPlatformForObservationId(final String obsId) {
	for (final Platform platform : getDataset().getPlatforms()) {
	    for (final Observation o : platform.getAllObservations()) {
		if (o.getId().equals(obsId)) {
		    return platform;
		}
	    }
	}
	return null;
    }

    /**
     * @return true if the listOfRedoableChanges is empty
     */
    public boolean isListOfRedoableChangesEmpty() {
	return listOfRedoableChanges.isEmpty();
    }

    /**
     * @return true if the listOfUndoableChanges is empty
     */
    public boolean isListOfUndoableChangesEmpty() {
	return listOfUndoableChanges.isEmpty();
    }

    /**
     * @return TRUE if there is at least 1 update on a Metadata value (not QC)
     */
    public boolean isThereAtLeastOneUpdateOnMetadataValue() {
	for (final List<? extends UndoRedoAction> undoRedoActions : listOfUndoableChanges) {
	    if ((undoRedoActions != null) && !undoRedoActions.isEmpty()
		    && (undoRedoActions.get(0) instanceof MetadataValueChange)
		    && (((MetadataValueChange) undoRedoActions.get(0)).getNewValue() != null)) {
		return true;
	    }
	}
	// It is possible to have no undoableChanges but changes have been done
	// ...
	// return false;
	return true;
    }

    /**
     * @return TRUE if there is at least 1 Error message not checked
     */
    public boolean isThereUncheckedErrorMessages(final STEP_TYPE stepType) {
	if (stepType == STEP_TYPE.Q1_CONTROL_AUTO_METADATA) {
	    // For each Messages
	    for (final MessageItem messageItem : report.getStep(STEP_TYPE.Q1_CONTROL_AUTO_METADATA).getMessages()) {
		// If it is an CAMetadataErrorMessageItem
		if ((messageItem instanceof CAMetadataErrorMessageItem)
			&& (!((CAMetadataErrorMessageItem) messageItem).isErrorChecked())) {
		    // ==> There is at least 1 Error message "not checked"
		    return true;
		}
	    }
	} else if (stepType == STEP_TYPE.Q2_CONTROL_AUTO_DATA) {
	    // For each Messages
	    for (final MessageItem messageItem : report.getStep(STEP_TYPE.Q2_CONTROL_AUTO_DATA).getMessages()) {
		// If it is an ErrorMessage
		if (messageItem instanceof CADataErrorMessageItem) {
		    final CADataErrorMessageItem caDataErrorMessageItem = (CADataErrorMessageItem) messageItem;
		    // And this message corresponds to the current view
		    if (((this instanceof ReferenceViewModel) == caDataErrorMessageItem.isErrorOnReferenceParameter())
			    && !caDataErrorMessageItem.isErrorChecked()) {
			// And it is not checked
			// ==> There is at least 1 Error message not checked
			return true;
		    }
		}
	    }
	}
	return false;
    }

    /**
     * Unload data to save memory
     */
    public void prepareForDispose() {
	specificPrepareForDispose();
	dataset = null;

	if (listOfUndoableChanges != null) {
	    for (final List<? extends UndoRedoAction> QCValueChange : listOfUndoableChanges) {
		QCValueChange.clear();
	    }
	    listOfUndoableChanges.clear();
	}

	if (listOfRedoableChanges != null) {
	    for (final List<? extends UndoRedoAction> QCValueChange : listOfRedoableChanges) {
		QCValueChange.clear();
	    }
	    listOfRedoableChanges.clear();
	}
    }

    @SuppressWarnings("unchecked")
    public List<? extends UndoRedoAction> redoLastChanges() {
	List<? extends UndoRedoAction> lastChanges = null;
	if (!listOfRedoableChanges.isEmpty()) {
	    lastChanges = listOfRedoableChanges.remove(0);

	    if (lastChanges.get(0) instanceof QCValueChange) {
		updateQCsInSC3Model((List<QCValueChange>) lastChanges, true);
		updateMetadatasInSC3Model((List<QCValueChange>) lastChanges, true);
		redoChangesOnErrorMessage(getStepType(), (List<QCValueChange>) lastChanges);
	    } else if (lastChanges.get(0) instanceof CommentChange) {
		redoCommentChangesOnErrorMessage((List<CommentChange>) lastChanges);
	    }

	    updateQCsOnMap(lastChanges, true);

	    listOfUndoableChanges.add(0, lastChanges);
	}
	return lastChanges;
    }

    /**
     * To override if needed.
     */
    public void saveDataButtonPressed() {
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<? extends UndoRedoAction> undoLastChanges(final boolean addInRedoable) {
	List<? extends UndoRedoAction> lastChanges = null;
	if (!listOfUndoableChanges.isEmpty()) {
	    lastChanges = listOfUndoableChanges.remove(0);

	    if (!lastChanges.isEmpty()) {
		if (lastChanges.get(0) instanceof QCValueChange) {
		    updateQCsInSC3Model((List<QCValueChange>) lastChanges, false);
		    updateMetadatasInSC3Model((List<QCValueChange>) lastChanges, false);
		    undoChangesOnErrorMessage(getStepType(), (List<QCValueChange>) lastChanges);
		} else if (lastChanges.get(0) instanceof CommentChange) {
		    undoCommentChangesOnErrorMessage((List<CommentChange>) lastChanges);
		}

		updateQCsOnMap(lastChanges, false);

		if (addInRedoable) {
		    listOfRedoableChanges.add(0, lastChanges);
		}
	    }
	}
	return lastChanges;
    }

    /**
     * Backup new QCs change for the given variable name
     *
     * @param qcsChanged
     */
    public void updateComments(final List<CommentChange> commentChanged) {
	listOfUndoableChanges.add(0, commentChanged);

	redoCommentChangesOnErrorMessage(commentChanged);

	listOfRedoableChanges.clear();
    }

    /**
     * Update the QCs in the Scoop3 Model but not saving modification in a file
     *
     * @param updatesForVariables
     * @param observationNumber
     */
    // @SuppressWarnings("unchecked")
    public void updateDataTableChanges(final ArrayList<DataTableUpdateAbstract> updatesForVariables,
	    final int observationNumber) {
	SC3Logger.LOGGER.trace("updateDataTableChanges start");

	final List<QCValueChange> updateToApply = new ArrayList<QCValueChange>();

	while (!updatesForVariables.isEmpty()) {
	    final DataTableUpdateAbstract dataTableUpdateAbstract = updatesForVariables.get(0);
	    switch (dataTableUpdateAbstract.getDataTableUpdateType()) {
	    // case QC_AND_VALUE_UPDATE:
	    // // For each variables ...
	    // for (final String variableName : ((DataTableQCAndValueUpdate)
	    // dataTableUpdateAbstract)
	    // .getUpdatesForVariables().keySet()) {
	    // // ... set the new Value and QCValue
	    // for (final DataTableCellQCAndValueUpdate
	    // dataTableCellQCAndValueUpdate : ((DataTableQCAndValueUpdate)
	    // dataTableUpdateAbstract)
	    // .getUpdatesForVariables().get(variableName)) {
	    // final int rowIndex =
	    // dataTableCellQCAndValueUpdate.getRowIndex();
	    // final QCValues newQCValue =
	    // dataTableCellQCAndValueUpdate.getNewQCValue();
	    // // Force Double value
	    // final Double newValue =
	    // dataTableCellQCAndValueUpdate.getNewValue().doubleValue();
	    //
	    // final Observation obs =
	    // getDataset().getObservations().get(observationNumber);
	    //
	    // // Check if the parameter is the Reference parameter
	    // if
	    // (obs.getReferenceParameter().getCode().equalsIgnoreCase(variableName))
	    // {
	    // obs.getReferenceParameter().getQcValues().set(rowIndex,
	    // newQCValue);
	    // ((SpatioTemporalParameter<Double>)
	    // obs.getReferenceParameter()).getValues().set(rowIndex,
	    // newValue);
	    // } else {
	    // final Parameter<Double> parameter =
	    // obs.getOceanicParameter(variableName);
	    // parameter.getQcValues().set(rowIndex, newQCValue);
	    // parameter.getValues().set(rowIndex, newValue);
	    // }
	    // }
	    // }
	    // break;
	    case QC_AND_VALUE_UPDATE:
		// For each variables ...
		for (final String variableName : ((DataTableQCAndValueUpdate) dataTableUpdateAbstract)
			.getUpdatesForVariables().keySet()) {
		    // ... set the new Value and QCValue
		    for (final DataTableCellQCAndValueUpdate dataTableCellQCAndValueUpdate : ((DataTableQCAndValueUpdate) dataTableUpdateAbstract)
			    .getUpdatesForVariables().get(variableName)) {
			final int rowIndex = dataTableCellQCAndValueUpdate.getRowIndex();
			final QCValues newQCValue = dataTableCellQCAndValueUpdate.getNewQCValue();

			updateQCAndAddQCValue(updateToApply, variableName, observationNumber, rowIndex, newQCValue);
		    }
		}
		break;
	    case QC_UPDATE:
		// For each variables ...
		for (final String variableName : ((DataTableQCUpdate) dataTableUpdateAbstract).getUpdatesForVariables()
			.keySet()) {
		    // ... reset the old QCValue
		    for (final DataTableCellQCUpdate dataTableCellQCUpdate : ((DataTableQCUpdate) dataTableUpdateAbstract)
			    .getUpdatesForVariables().get(variableName)) {
			final int rowIndex = dataTableCellQCUpdate.getRowIndex();
			final QCValues newQCValue = dataTableCellQCUpdate.getNewQCValue();

			updateQCAndAddQCValue(updateToApply, variableName, observationNumber, rowIndex, newQCValue);
		    }
		}
		break;
	    case ROW_DELETE:
		final List<Integer> rowIndexes = new ArrayList<Integer>();
		// The DataTableCellDeleted are already sorted
		for (final DataTableCellDeleted dataTableCellDeleted : ((DataTableRowsDeleted) dataTableUpdateAbstract)
			.getCellsDeleted()) {
		    final int rowIndex = dataTableCellDeleted.getRowIndex();
		    if (!rowIndexes.contains(rowIndex)) {
			rowIndexes.add(rowIndex);
		    }
		}

		// Sort the rowIndexes DESC
		Collections.sort(rowIndexes, (final Integer o1, final Integer o2) -> {
		    return o2 - o1;
		});

		// Get the current Observation
		final Observation obs = getDataset().getObservations().get(observationNumber);

		// Delete rows
		for (final Integer rowIndex : rowIndexes) {
		    obs.getReferenceParameter().getValues().remove(rowIndex.intValue());
		    obs.getReferenceParameter().getQcValues().remove(rowIndex.intValue());
		    obs.getReferenceParameter().updateDimension();

		    for (final OceanicParameter parameter : obs.getOceanicParameters().values()) {
			parameter.getValues().remove(rowIndex.intValue());
			parameter.getQcValues().remove(rowIndex.intValue());
			parameter.updateDimension();
		    }

		    /*
		     * Remove Error messages (if exist)
		     */
		    if (report != null) {
			for (final STEP_TYPE stepType : STEP_TYPE.values()) {
			    final int nbErrorMessage = report.getStep(stepType).getMessages().size();
			    for (int index = nbErrorMessage - 1; index >= 0; index--) {
				final MessageItem messageItem = report.getStep(stepType).getMessages().get(index);

				if (messageItem instanceof CADataErrorMessageItem) {
				    final CADataErrorMessageItem caDataErrorMessageItem = (CADataErrorMessageItem) messageItem;

				    if ((caDataErrorMessageItem.getObs1Id() != null)
					    && caDataErrorMessageItem.getObs1Id().equals(obs.getId())
					    && (caDataErrorMessageItem.getReferenceIndex() == rowIndex)) {
					// Error message for the right
					// observation

					// right level
					report.removeMessage(caDataErrorMessageItem, stepType);
					ReportJDialog.removeErrorMessage(caDataErrorMessageItem);
				    }
				}
			    }
			}
			ReportJDialog.displayOrHideJTableIfNeeded();
		    }
		}

		break;
	    }
	    updatesForVariables.remove(0);
	}

	if (!updateToApply.isEmpty()) {
	    updateQCs(updateToApply);
	}

	SC3Logger.LOGGER.trace("updateDataTableChanges stop");
    }

    /**
     * A new update on the Metadata is done
     *
     * @param metadataValueChanges
     */
    public void updateMetadata(final List<QCValueChange> metadataValueChanges) {
	updateMetadatasInSC3Model(metadataValueChanges, true);
	redoChangesOnErrorMessage(getStepType(), metadataValueChanges);

	listOfUndoableChanges.add(0, metadataValueChanges);
	listOfRedoableChanges.clear();

	updateQCsOnMap(metadataValueChanges, true);
    }

    /**
     * Backup new QCs change for the given variable name
     *
     * @param qcsChanged
     */
    public void updateQCs(final List<QCValueChange> qcsChanged) {
	if (getStepType() == STEP_TYPE.Q2_CONTROL_AUTO_DATA) {
	    addQCChangedForComputedParametersIfNeeded(qcsChanged);
	}

	listOfUndoableChanges.add(0, qcsChanged);

	updateQCsInSC3Model(qcsChanged, true);
	updateMetadatasInSC3Model(qcsChanged, true);
	redoChangesOnErrorMessage(getStepType(), qcsChanged);
	updateQCsOnMap(qcsChanged, true);

	listOfRedoableChanges.clear();
    }

    /**
     * Backup removed measures for the given variable name
     *
     * @param qcsChanged
     */
    public void updateRemovedMeasures(final List<QCValueChange> qcsChanged) {
	listOfUndoableChanges.add(0, qcsChanged);
	redoChangesOnErrorMessage(getStepType(), qcsChanged);
	listOfRedoableChanges.clear();
    }

    private void addQCChangedForComputedParametersIfNeeded(final List<QCValueChange> qcsChanged) {
	final List<QCValueChange> qcsChangedToAdd = new ArrayList<>();

	// Remove update on computed parameters in the list...
	for (int index = qcsChanged.size() - 1; index >= 0; index--) {
	    final QCValueChange qcValueChange = qcsChanged.get(index);

	    // If the update is for a QC
	    if (qcValueChange.getNewQC() != QCValueChange.NO_NEW_QC) {

		Observation obs = null;

		// set the obs with the observationIndex of qcValueChange
		obs = getObservation(qcValueChange.getObservationIndex());
		if (!obs.getId().equals(qcValueChange.getObsId())) {
		    // if the id doesn't match, reset the obs
		    obs = null;
		}

		// find the obs with the correct id
		final int nbObs = getObservations().size();
		for (int indexObs = 0; (indexObs < nbObs) && (obs == null); indexObs++) {
		    if (getObservation(indexObs).getId().equals(qcValueChange.getObsId())) {
			obs = getObservation(indexObs);
		    }
		}

		if (obs != null) {
		    final OceanicParameter param = obs.getOceanicParameter(qcValueChange.getParameterName());
		    if ((param != null) && (param.getLinkParamType() == LINK_PARAM_TYPE.COMPUTED_CONTROL)) {
			// This should happen only if it is an update from the
			// data table
			qcsChanged.remove(index);
		    }
		}
	    }
	}

	// Apply update ...
	updateQCsInSC3Model(qcsChanged, true);

	// Add new update on Computed parameters if needed
	for (int index = qcsChanged.size() - 1; index >= 0; index--) {
	    final QCValueChange qcValueChange = qcsChanged.get(index);

	    // If the update is for a QC
	    if (qcValueChange.getNewQC() != QCValueChange.NO_NEW_QC) {

		Observation obs = null;

		// set the obs with the observationIndex of qcValueChange
		obs = getObservation(qcValueChange.getObservationIndex());
		if (!obs.getId().equals(qcValueChange.getObsId())) {
		    // if the id doesn't match, reset the obs
		    obs = null;
		}

		// find the obs with the correct id
		final int nbObs = getObservations().size();
		for (int indexObs = 0; (indexObs < nbObs) && (obs == null); indexObs++) {
		    if (getObservation(indexObs).getId().equals(qcValueChange.getObsId())) {
			obs = getObservation(indexObs);
		    }
		}

		if (obs != null) {
		    final ArrayList<Parameter<? extends Number>> paramList = getParam(obs,
			    qcValueChange.getParameterName());
		    for (final Parameter<? extends Number> param : paramList) {
			if (param != null) {
			    // If it is a father of Computed Parameter, the
			    // following loop will creates QCValueChange
			    for (final Parameter<? extends Number> computedParameter : ParametersRelationships
				    .getLinkedParameters(param)) {
				// Compute worst QCValues
				QCValues valueToSetForComputedParameter = QCValues
					.getQCValues(qcValueChange.getNewQC());
				for (final Parameter<? extends Number> father : ParametersRelationships
					.getFathers(computedParameter)) {
				    final String fathersName = father.getCode();
				    final ArrayList<Parameter<? extends Number>> fatherParamList = getParam(obs,
					    fathersName);
				    for (final Parameter<? extends Number> fatherParam : fatherParamList) {
					final QCValues fatherQC = fatherParam.getQcValues()
						.get(qcValueChange.getObservationLevel());
					valueToSetForComputedParameter = QCValues
						.getWorstQC(valueToSetForComputedParameter, fatherQC);
				    }
				}
				// If the new QC is different => create a new
				// QCValueChange
				if (computedParameter.getQcValues()
					.get(qcValueChange.getObservationLevel()) != valueToSetForComputedParameter) {
				    final QCValueChange newQCValueChange = new QCValueChange(
					    qcValueChange.getObservationIndex(), qcValueChange.getObservationLevel(),
					    computedParameter.getQcValues().get(qcValueChange.getObservationLevel())
						    .getQCValue(),
					    valueToSetForComputedParameter.getQCValue(), qcValueChange.getObsId(),
					    computedParameter.getCode(),
					    computedParameter.getValues().get(qcValueChange.getObservationLevel()),
					    qcValueChange.getParameterValueStr(), qcValueChange.getRefValueStr(),
					    qcValueChange.getPlatformCode());
				    newQCValueChange.setOldManualQC(newQCValueChange.getOldQC());
				    // add the first oldQC as the original QC
				    if (originalQCs.get(qcValueChange.getObservationIndex() + "/"
					    + qcValueChange.getObservationLevel() + "/"
					    + qcValueChange.getParameterName()) == null) {
					originalQCs.put(qcValueChange.getObservationIndex() + "/"
						+ qcValueChange.getObservationLevel() + "/"
						+ qcValueChange.getParameterName(), qcValueChange.getOldQC());
				    }
				    qcsChangedToAdd.add(newQCValueChange);
				}
			    }
			}
		    }
		}
	    }
	}

	// Undo update ...
	updateQCsInSC3Model(qcsChanged, false);

	qcsChanged.addAll(qcsChangedToAdd);
    }

    /**
     * As the observationIndex is not defined, search the Observation with its ID. (and fill the observationIndex for
     * the next time)
     *
     * @param qcValueChange
     * @return
     */
    protected Observation getObservationWithoutObsIndex(final QCValueChange qcValueChange) {
	int obsIndex = 0;
	for (final Observation observation : getDataset().getObservations()) {
	    if ((qcValueChange.getPlatformCode() != null) && !qcValueChange.getPlatformCode().equals("")) {
		if ((observation.getReference().equals(qcValueChange.getObsId())
			|| observation.getId().equals(qcValueChange.getObsId()))
			&& observation.getSensor().getPlatform().getCode().equals(qcValueChange.getPlatformCode())) {
		    qcValueChange.setObservationIndex(obsIndex);
		    return observation;
		}
	    } else {
		if (observation.getReference().equals(qcValueChange.getObsId())
			|| observation.getId().equals(qcValueChange.getObsId())) {
		    qcValueChange.setObservationIndex(obsIndex);
		    return observation;
		}
	    }
	    obsIndex++;
	}
	return null;
    }

    private void redoChangesOnErrorMessage(final STEP_TYPE stepType, final List<QCValueChange> qcsChanged) {

	for (final QCValueChange qcValueChange : qcsChanged) {
	    boolean errorMessageUpdated = false;
	    int nbErrorMessage;
	    if ((report == null) || (report.getStep(stepType) == null)
		    || (report.getStep(stepType).getMessages() == null)) {
		nbErrorMessage = 0;
	    } else {
		nbErrorMessage = report.getStep(stepType).getMessages().size();
	    }
	    for (int index = nbErrorMessage - 1; (index >= 0) /* && !errorMessageUpdated */; index--) {
		if (report != null) {
		    final MessageItem messageItem = report.getStep(stepType).getMessages().get(index);

		    if (messageItem instanceof CAMetadataErrorMessageItem) {
			final CAMetadataErrorMessageItem caErrorMessageItem = (CAMetadataErrorMessageItem) messageItem;
			if ((qcValueChange instanceof MetadataValueChange)
				&& (((caErrorMessageItem.getObs1Id() == null) && (qcValueChange.getObsId() == null))
					|| ((caErrorMessageItem.getObs1Id() != null)
						&& caErrorMessageItem.getObs1Id().equals(qcValueChange.getObsId())))) {
			    // Check if it is the same error message
			    final MetadataValueChange metadataValueChange = (MetadataValueChange) qcValueChange;
			    boolean sameMetadata = caErrorMessageItem
				    .getQcToUpdateForMetadataValueChange() == metadataValueChange.getQcToUpdate();
			    sameMetadata &= (((caErrorMessageItem.getMetadata() == null)
				    && (metadataValueChange.getMetadata() == null))
				    || ((caErrorMessageItem.getMetadata() != null) && caErrorMessageItem.getMetadata()
					    .equals(metadataValueChange.getMetadata())));
			    if (sameMetadata) {
				if ((qcValueChange.getNewQC() != QCValueChange.NO_NEW_QC)
					&& (caErrorMessageItem.getFlagAuto() != null)) {
				    // Backup old Manual QC
				    qcValueChange.setOldManualQC((caErrorMessageItem.getFlagManual() == null) ? -1
					    : caErrorMessageItem.getFlagManual().getQCValue());
				    // => update the message
				    caErrorMessageItem.setFlagManual(QCValues.getQCValues(qcValueChange.getNewQC()));
				    errorMessageUpdated = true;
				}

				if (metadataValueChange.isErrorChecked() != null) {
				    caErrorMessageItem.setErrorChecked(metadataValueChange.isErrorChecked());
				    errorMessageUpdated = true;
				}
				if (metadataValueChange.getComment() != null) {
				    caErrorMessageItem.setComment(metadataValueChange.getComment());
				    errorMessageUpdated = true;
				}

				ReportJDialog.updateErrorMessage(caErrorMessageItem);

				// If it is a "Manual Control" error and if the
				// QC values has been reset => remove the
				// message
				if (caErrorMessageItem.isManualControlMessage()
					&& (caErrorMessageItem.getFlagAuto() == caErrorMessageItem.getFlagManual())) {
				    report.removeMessage(caErrorMessageItem, stepType);
				    ReportJDialog.removeErrorMessage(caErrorMessageItem);
				}
			    }
			}

		    } else if (messageItem instanceof CADataErrorMessageItem) {
			final CADataErrorMessageItem caDataErrorMessageItem = (CADataErrorMessageItem) messageItem;
			// Manage only right messages
			if ((this instanceof ReferenceViewModel) == caDataErrorMessageItem
				.isErrorOnReferenceParameter()) {
			    // Check if it is the same error message
			    final boolean sameRefValue = ((caDataErrorMessageItem.getReferenceValue() == null
				    ? caDataErrorMessageItem.getReferenceValue() == qcValueChange.getRefValueStr()
				    : caDataErrorMessageItem.getReferenceValue().equals(qcValueChange.getRefValueStr()))
				    || (caDataErrorMessageItem.getReferenceValueToDisplayInReportDialog()
					    .equals(qcValueChange.getRefValueStr())))
				    && (caDataErrorMessageItem.getReferenceIndex() == qcValueChange
					    .getObservationLevel());

			    if (caDataErrorMessageItem.getObs1Id().equals(qcValueChange.getObsId())
				    && caDataErrorMessageItem.getParamCode().equals(qcValueChange.getParameterName())
				    && ((caDataErrorMessageItem.getParamValueStr() == null) || caDataErrorMessageItem
					    .getParamValueStr().equals(qcValueChange.getParameterValueStr()))
				    && sameRefValue) {

				if (qcValueChange.getNewQC() != QCValueChange.NO_NEW_QC) {
				    if (!caDataErrorMessageItem.getParamCode().equals("-")
					    && !caDataErrorMessageItem.getReferenceValue().equals("-")) {
					// Backup old Manual QC
					qcValueChange.setOldManualQC((caDataErrorMessageItem.getFlagManual() == null)
						? -1 : caDataErrorMessageItem.getFlagManual().getQCValue());
					// => update the message
					caDataErrorMessageItem
						.setFlagManual(QCValues.getQCValues(qcValueChange.getNewQC()));
				    }
				    errorMessageUpdated = true;
				}
				if (qcValueChange instanceof DataValueChange) {
				    final DataValueChange dataValueChange = (DataValueChange) qcValueChange;

				    if ((dataValueChange.getErrorMessage() == null) || caDataErrorMessageItem
					    .getDetails().equals(dataValueChange.getErrorMessage())) {
					if (dataValueChange.isErrorChecked() != null) {
					    caDataErrorMessageItem.setErrorChecked(dataValueChange.isErrorChecked());
					    errorMessageUpdated = true;
					}
					if (dataValueChange.getComment() != null) {
					    caDataErrorMessageItem.setComment(dataValueChange.getComment());
					    errorMessageUpdated = true;
					}
				    }
				}

				ReportJDialog.updateErrorMessage(caDataErrorMessageItem);

				// If it is a "Manual Control" error and if the QC
				// values has been reset => remove the
				// message
				if (caDataErrorMessageItem.isManualControlMessage() && (caDataErrorMessageItem
					.getFlagAuto() == caDataErrorMessageItem.getFlagManual())) {
				    report.removeMessage(caDataErrorMessageItem, stepType);
				    ReportJDialog.removeErrorMessage(caDataErrorMessageItem);
				}
			    }
			}
		    }
		}
	    }

	    if (!errorMessageUpdated && (report != null)) {
		boolean addMessage = true;

		// Check if the update is done on a COMPUTED_PARAMETER
		for (final Observation observation : getObservations()) {
		    if (observation.getId().equals(qcValueChange.getObsId())) {
			final OceanicParameter parameter = observation
				.getOceanicParameter(qcValueChange.getParameterName());
			if ((parameter != null) && (parameter.getLinkParamType() == LINK_PARAM_TYPE.COMPUTED_CONTROL)) {
			    addMessage = false;
			}
		    }
		}

		final QCValues newQC = ((qcValueChange.getNewQC() == -1)
			|| (qcValueChange.getNewQC() == QCValueChange.NO_NEW_QC)) ? null
				: QCValues.getQCValues(qcValueChange.getNewQC());
		if (addMessage && (newQC != null)) {
		    CAErrorMessageItem newCAErrorMessageItem;
		    if (stepType == STEP_TYPE.Q1_CONTROL_AUTO_METADATA) {
			if (qcValueChange instanceof MetadataValueChange) {
			    final QCValues oldQC = ((qcValueChange.getOldQC() == -1)
				    || (qcValueChange.getOldQC() == QCValueChange.NO_NEW_QC)) ? null
					    : QCValues.getQCValues(qcValueChange.getOldQC());
			    final MetadataValueChange metadataValueChange = (MetadataValueChange) qcValueChange;
			    newCAErrorMessageItem = new CAMetadataErrorMessageItem(metadataValueChange.getQcToUpdate(),
				    metadataValueChange.getMetadata(), metadataValueChange.getObsId(),
				    Messages.getMessage("gui.errors-dialog.new-error-visual-control")
					    + metadataValueChange.getErrorMessageSuffix(),
				    oldQC);
			    qcValueChange.setOldManualQC(qcValueChange.getOldQC());
			    newCAErrorMessageItem.setFlagManual(newQC);
			    newCAErrorMessageItem.setIsManualControlMessage(true);
			    newCAErrorMessageItem.setErrorChecked(true);
			} else {
			    newCAErrorMessageItem = null; // Could never happens
			}
		    } else {
			final boolean isErrorOnReferenceParameter = (this instanceof ReferenceViewModel);
			newCAErrorMessageItem = new CADataLightErrorMessageItem(qcValueChange.getObsId(),
				Messages.getMessage("gui.errors-dialog.new-error-visual-control"),
				qcValueChange.getParameterName(), qcValueChange.getRefValueStr(),
				qcValueChange.getObservationLevel(), qcValueChange.getParameterValue(),
				qcValueChange.getParameterValueStr(), QCValues.getQCValues(qcValueChange.getOldQC()),
				isErrorOnReferenceParameter);
			qcValueChange.setOldManualQC(qcValueChange.getOldQC());
			newCAErrorMessageItem.setFlagManual(QCValues.getQCValues(qcValueChange.getNewQC()));
			newCAErrorMessageItem.setIsManualControlMessage(true);
			newCAErrorMessageItem.setErrorChecked(true);
		    }

		    report.getStep(stepType).addMessage(newCAErrorMessageItem);
		    ReportJDialog.addErrorMessage(newCAErrorMessageItem);
		}
	    }
	}
	ReportJDialog.displayOrHideJTableIfNeeded();
    }

    private void redoCommentChangesOnErrorMessage(final List<CommentChange> commentChanged) {
	if (report != null) {
	    for (final CommentChange commentChange : commentChanged) {
		final int nbErrorMessage = report.getStep(STEP_TYPE.Q1_CONTROL_AUTO_METADATA).getMessages().size();
		for (int index = 0; index < nbErrorMessage; index++) {
		    final MessageItem messageItem = report.getStep(STEP_TYPE.Q1_CONTROL_AUTO_METADATA).getMessages()
			    .get(index);
		    if (messageItem instanceof CAErrorMessageItem) {
			final CAErrorMessageItem caErrorMessageItem = (CAErrorMessageItem) messageItem;
			if (caErrorMessageItem.getDetails().equals(commentChange.getDetails())
				&& ((commentChange.getObsId() == null)
					|| commentChange.getObsId().equals(caErrorMessageItem.getObs1Id()))) {
			    caErrorMessageItem.setComment(commentChange.getComment());
			    ReportJDialog.updateErrorMessage(caErrorMessageItem);
			}
		    }
		}
	    }
	}
    }

    private void undoChangesOnErrorMessage(final STEP_TYPE stepType, final List<QCValueChange> qcsChanged) {
	if (report != null) {
	    for (final QCValueChange qcValueChange : qcsChanged) {
		boolean errorMessageUpdated = false;
		final int nbErrorMessage = report.getStep(stepType).getMessages().size();
		for (int index = nbErrorMessage - 1; (index >= 0); index--) {

		    final MessageItem messageItem = report.getStep(stepType).getMessages().get(index);

		    if ((messageItem instanceof CAMetadataErrorMessageItem)
			    && (qcValueChange instanceof MetadataValueChange)) {
			final MetadataValueChange metadataValueChange = (MetadataValueChange) qcValueChange;
			final CAMetadataErrorMessageItem caObsMetadataErrorMessageItem = (CAMetadataErrorMessageItem) messageItem;

			final boolean sameObsId = ((metadataValueChange.getObsId() == null)
				&& (caObsMetadataErrorMessageItem.getObs1Id() == null))
				|| ((metadataValueChange.getObsId() != null) && metadataValueChange.getObsId()
					.equals(caObsMetadataErrorMessageItem.getObs1Id()));
			final boolean sameErrorMsg = (metadataValueChange
				.getQcToUpdate() == caObsMetadataErrorMessageItem.getQcToUpdateForMetadataValueChange())
				&& (((metadataValueChange.getMetadata() == null)
					&& (caObsMetadataErrorMessageItem.getMetadata() == null))
					|| ((metadataValueChange.getMetadata() != null) && metadataValueChange
						.getMetadata().equals(caObsMetadataErrorMessageItem.getMetadata())));
			// Manage only right messages
			if (sameObsId && sameErrorMsg) {

			    if (metadataValueChange.getOldQC() != QCValueChange.NO_NEW_QC) {
				// => update the message
				QCValues flagToSet = (metadataValueChange.getOldManualQC() == -1) ? null
					: QCValues.getQCValues(metadataValueChange.getOldManualQC());
				if (!caObsMetadataErrorMessageItem.isManualControlMessage()
					&& (flagToSet == caObsMetadataErrorMessageItem.getFlagAuto())) {
				    flagToSet = null;
				}
				caObsMetadataErrorMessageItem.setFlagManual(flagToSet);
				errorMessageUpdated = true;
			    }
			    if (metadataValueChange.isErrorCheckedOldValue() != null) {
				caObsMetadataErrorMessageItem
					.setErrorChecked(metadataValueChange.isErrorCheckedOldValue());
				errorMessageUpdated = true;
			    }
			    if (metadataValueChange.getCommentOldValue() != null) {
				caObsMetadataErrorMessageItem.setComment(metadataValueChange.getCommentOldValue());
				errorMessageUpdated = true;
			    }

			    ReportJDialog.updateErrorMessage(caObsMetadataErrorMessageItem);

			    // If it is a "Manual Control" error and if the QC
			    // values has been reset => remove the
			    // message
			    if (caObsMetadataErrorMessageItem.isManualControlMessage() && (caObsMetadataErrorMessageItem
				    .getFlagAuto() == caObsMetadataErrorMessageItem.getFlagManual())) {
				report.removeMessage(caObsMetadataErrorMessageItem, stepType);
				ReportJDialog.removeErrorMessage(caObsMetadataErrorMessageItem);

				errorMessageUpdated = true;
			    }
			}
		    } else if (messageItem instanceof CADataErrorMessageItem) {
			final CADataErrorMessageItem caDataErrorMessageItem = (CADataErrorMessageItem) messageItem;
			// Manage only right messages
			if (((this instanceof ReferenceViewModel) == caDataErrorMessageItem
				.isErrorOnReferenceParameter())
				&& (caDataErrorMessageItem.getObs1Id().equals(qcValueChange.getObsId())
					&& caDataErrorMessageItem.getParamCode()
						.equals(qcValueChange.getParameterName())
					&& (((caDataErrorMessageItem.getParamValueStr() == null)
						&& "-".equals(qcValueChange.getParameterValueStr()))
						|| ((caDataErrorMessageItem.getParamValueStr() != null)
							&& caDataErrorMessageItem.getParamValueStr()
								.equals(qcValueChange.getParameterValueStr())))
					&& caDataErrorMessageItem.getReferenceValue()
						.equals(qcValueChange.getRefValueStr())
					&& (caDataErrorMessageItem.getReferenceIndex() == qcValueChange
						.getObservationLevel()))) {
			    // Check if it is the same error message
			    if (qcValueChange.getOldQC() != QCValueChange.NO_NEW_QC) {
				if (!caDataErrorMessageItem.getParamCode().equals("-")
					&& !caDataErrorMessageItem.getReferenceValue().equals("-")) {
				    // => update the message
				    caDataErrorMessageItem.setFlagManual((qcValueChange.getOldManualQC() == -1) ? null
					    : QCValues.getQCValues(qcValueChange.getOldManualQC()));
				}
				errorMessageUpdated = true;
			    }

			    if (qcValueChange instanceof DataValueChange) {
				final DataValueChange dataValueChange = (DataValueChange) qcValueChange;

				if ((dataValueChange.getErrorMessage() == null) || caDataErrorMessageItem.getDetails()
					.equals(dataValueChange.getErrorMessage())) {
				    if (dataValueChange.isErrorCheckedOldValue() != null) {
					caDataErrorMessageItem
						.setErrorChecked(dataValueChange.isErrorCheckedOldValue());
					errorMessageUpdated = true;
				    }
				    if (dataValueChange.getCommentOldValue() != null) {
					caDataErrorMessageItem.setComment(dataValueChange.getCommentOldValue());
					errorMessageUpdated = true;
				    }
				}
			    }

			    ReportJDialog.updateErrorMessage(caDataErrorMessageItem);

			    // If it is a "Manual Control" error and if the
			    // QC values has been reset => remove the
			    // message
			    if (caDataErrorMessageItem.isManualControlMessage() && (caDataErrorMessageItem
				    .getFlagAuto() == caDataErrorMessageItem.getFlagManual())) {
				report.removeMessage(caDataErrorMessageItem, stepType);
				ReportJDialog.removeErrorMessage(caDataErrorMessageItem);
			    }
			}
		    }
		}

		if (!errorMessageUpdated) {

		    boolean addMessage = true;

		    // Check if the update is done on a COMPUTED_PARAMETER
		    for (final Observation observation : getObservations()) {
			if (observation.getId().equals(qcValueChange.getObsId())) {
			    final OceanicParameter parameter = observation
				    .getOceanicParameter(qcValueChange.getParameterName());
			    if ((parameter != null)
				    && (parameter.getLinkParamType() == LINK_PARAM_TYPE.COMPUTED_CONTROL)) {
				addMessage = false;
			    }
			}
		    }

		    final QCValues newQC = ((qcValueChange.getNewQC() == -1)
			    || (qcValueChange.getNewQC() == QCValueChange.NO_NEW_QC)) ? null
				    : QCValues.getQCValues(qcValueChange.getNewQC());
		    if (addMessage && (newQC != null)) {
			CAErrorMessageItem newCAErrorMessageItem;
			if (stepType == STEP_TYPE.Q1_CONTROL_AUTO_METADATA) {
			    if (qcValueChange instanceof MetadataValueChange) {
				final QCValues oldQC = ((qcValueChange.getOldQC() == -1)
					|| (qcValueChange.getOldQC() == QCValueChange.NO_NEW_QC)) ? null
						: QCValues.getQCValues(qcValueChange.getOldQC());
				final MetadataValueChange metadataValueChange = (MetadataValueChange) qcValueChange;
				newCAErrorMessageItem = new CAMetadataErrorMessageItem(
					metadataValueChange.getQcToUpdate(), metadataValueChange.getMetadata(),
					metadataValueChange.getObsId(),
					Messages.getMessage("gui.errors-dialog.new-error-visual-control")
						+ metadataValueChange.getErrorMessageSuffix(),
					newQC);
				qcValueChange.setOldManualQC(qcValueChange.getOldQC());
				newCAErrorMessageItem.setFlagManual(oldQC);
				newCAErrorMessageItem.setIsManualControlMessage(true);
				newCAErrorMessageItem.setErrorChecked(true);
			    } else {
				newCAErrorMessageItem = null; // Could never
				// happen
			    }
			} else {
			    final boolean isErrorOnReferenceParameter = (this instanceof ReferenceViewModel);
			    newCAErrorMessageItem = new CADataLightErrorMessageItem(qcValueChange.getObsId(),
				    Messages.getMessage("gui.errors-dialog.new-error-visual-control"),
				    qcValueChange.getParameterName(), qcValueChange.getRefValueStr(),
				    qcValueChange.getObservationLevel(), qcValueChange.getParameterValue(),
				    qcValueChange.getParameterValueStr(),
				    QCValues.getQCValues(qcValueChange.getNewQC()), isErrorOnReferenceParameter);
			    qcValueChange.setOldManualQC(qcValueChange.getOldQC());
			    newCAErrorMessageItem.setFlagManual(QCValues.getQCValues(qcValueChange.getOldQC()));
			    newCAErrorMessageItem.setIsManualControlMessage(true);
			    newCAErrorMessageItem.setErrorChecked(true);
			}

			report.addMessage(newCAErrorMessageItem, stepType);
			ReportJDialog.addErrorMessage(newCAErrorMessageItem);
		    }
		}
	    }
	    ReportJDialog.displayOrHideJTableIfNeeded();
	}
    }

    private void undoCommentChangesOnErrorMessage(final List<CommentChange> commentChanged) {
	if (report != null) {
	    for (final CommentChange commentChange : commentChanged) {
		final int nbErrorMessage = report.getStep(STEP_TYPE.Q1_CONTROL_AUTO_METADATA).getMessages().size();
		for (int index = 0; index < nbErrorMessage; index++) {
		    final MessageItem messageItem = report.getStep(STEP_TYPE.Q1_CONTROL_AUTO_METADATA).getMessages()
			    .get(index);
		    if (messageItem instanceof CAErrorMessageItem) {
			final CAErrorMessageItem caErrorMessageItem = (CAErrorMessageItem) messageItem;
			if (caErrorMessageItem.getDetails().equals(commentChange.getDetails())
				&& ((commentChange.getObsId() == null)
					|| commentChange.getObsId().equals(caErrorMessageItem.getObs1Id()))) {
			    caErrorMessageItem.setComment(commentChange.getCommentOldValue());
			    ReportJDialog.updateErrorMessage(caErrorMessageItem);
			}
		    }
		}
	    }
	}
    }

    /**
     * @param updateToApply
     * @param variableName
     * @param observationNumber
     * @param rowIndex
     * @param newQCValue
     */
    private void updateQCAndAddQCValue(final List<QCValueChange> updateToApply, final String variableName,
	    final int observationNumber, final int rowIndex, final QCValues newQCValue) {
	final Observation obs = getDataset().getObservations().get(observationNumber);

	int oldQC;
	Number paramValue;
	String paramValueStr;
	final String refValueStr = String.valueOf(obs.getReferenceParameter().getValues().get(rowIndex));
	// Check if the parameter is the Reference parameter
	if (obs.getReferenceParameter().getCode().equalsIgnoreCase(variableName)) {
	    // obs.getReferenceParameter().getQcValues().set(rowIndex,
	    // newQCValue);
	    paramValue = null;
	    paramValueStr = "-";
	    oldQC = obs.getReferenceParameter().getQcValues().get(rowIndex).getQCValue();
	} else {
	    Parameter<Double> parameter;
	    if (obs.getLatitude().getCode().equalsIgnoreCase(variableName)) {
		parameter = obs.getLatitude();
	    } else if (obs.getLongitude().getCode().equalsIgnoreCase(variableName)) {
		parameter = obs.getLongitude();
	    } else {
		parameter = obs.getOceanicParameter(variableName);
	    }
	    // parameter.getQcValues().set(rowIndex, newQCValue);
	    paramValue = parameter.getValues().get(rowIndex);
	    paramValueStr = String.valueOf(paramValue);
	    oldQC = parameter.getQcValues().get(rowIndex).getQCValue();
	}

	final QCValueChange qcValueChange = new QCValueChange(observationNumber, rowIndex, oldQC,
		newQCValue.getQCValue(), obs.getId(), variableName, paramValue, paramValueStr, refValueStr,
		getPlatformForObservation(observationNumber).getCode());
	// add the first oldQC as the original QC
	if (originalQCs.get(qcValueChange.getObservationIndex() + "/" + qcValueChange.getObservationLevel() + "/"
		+ qcValueChange.getParameterName()) == null) {
	    originalQCs.put(qcValueChange.getObservationIndex() + "/" + qcValueChange.getObservationLevel() + "/"
		    + qcValueChange.getParameterName(), qcValueChange.getOldQC());
	}
	updateToApply.add(qcValueChange);
    }

    /**
     * @return the listOfRedoableChanges
     */
    protected List<List<? extends UndoRedoAction>> getListOfRedoableChanges() {
	return listOfRedoableChanges;
    }

    /**
     * @return the listOfUndoableChanges
     */
    protected List<List<? extends UndoRedoAction>> getListOfUndoableChanges() {
	return listOfUndoableChanges;
    }

    /**
     * @return the report
     */
    protected Report getReport() {
	return report;
    }

    protected abstract STEP_TYPE getStepType();

    /**
     *
     * @return
     */
    protected boolean isSetFillValueOnQC9() {
	// FAE 28415
	return false;
    }

    /**
     * Unload data to save memory (in specific Controller)
     */
    protected abstract void specificPrepareForDispose();

    /**
     * @param qcsChanged
     * @param updateWithNewValue
     */
    protected void updateMetadatasInSC3Model(final List<QCValueChange> qcsChanged, final boolean updateWithNewValue) {
	SC3Logger.LOGGER.trace("updateMetadatasInSC3Model");
	final Platform platform = (getDataset().getPlatforms().isEmpty()) ? null : getDataset().getPlatforms().get(0);
	ValueAndQC metadata;

	for (final QCValueChange qcValueChange : qcsChanged) {
	    if ((qcValueChange instanceof MetadataValueChange)
		    && (((MetadataValueChange) qcValueChange).getQcToUpdate() != null)) {
		final MetadataValueChange metadataValueChange = (MetadataValueChange) qcValueChange;

		final int qcToSet = (updateWithNewValue) ? metadataValueChange.getNewQC()
			: metadataValueChange.getOldQC();
		final QCValues qcValuesToSet = ((qcToSet != -1) && (qcToSet != QCValueChange.NO_NEW_QC))
			? QCValues.getQCValues(qcToSet) : null;

		final String value = (updateWithNewValue) ? metadataValueChange.getNewValue()
			: metadataValueChange.getOldValue();

		final int observationIndex = metadataValueChange.getObservationIndex();
		final Observation obs = (observationIndex == -1) ? getObservationWithoutObsIndex(metadataValueChange)
			: getDataset().getObservations().get(observationIndex);

		try {
		    switch (metadataValueChange.getQcToUpdate()) {
		    case DATASET_END_DATE:
			if (qcValuesToSet != null) {
			    updateQCWithNewValueIfPossible(updateWithNewValue, getDataset().getEndDate(), qcValuesToSet,
				    metadataValueChange);
			}
			if (value != null) {
			    final Date date = Conversions.parseDate(value);
			    getDataset().getEndDate().setValue(date.getTime());
			}
			break;
		    case DATASET_METADATA:
			metadata = getDataset().getMetadata(metadataValueChange.getMetadata());
			if (metadata != null) {
			    if (qcValuesToSet != null) {
				updateQCWithNewValueIfPossible(updateWithNewValue, metadata, qcValuesToSet,
					metadataValueChange);
			    }
			    if (value != null) {
				updateObsMetadataValue(metadataValueChange.getMetadata(), metadata, value);
			    }
			}
			break;
		    case DATASET_PTF_CODE:
			if ((platform != null) && (value != null)) {
			    // Value only
			    platform.setCode(value);
			}
			break;
		    case DATASET_PTF_LABEL:
			if ((platform != null) && (value != null)) {
			    // Value only
			    platform.setLabel(value);
			}
			break;
		    case DATASET_REFERENCE:
			if (qcValuesToSet != null) {
			    updateQCWithNewValueIfPossible(updateWithNewValue, getDataset().getReference(),
				    qcValuesToSet, metadataValueChange);
			}
			if (value != null) {
			    getDataset().getReference().setValue(value);
			}
			break;
		    case DATASET_START_DATE:
			if (qcValuesToSet != null) {
			    updateQCWithNewValueIfPossible(updateWithNewValue, getDataset().getStartDate(),
				    qcValuesToSet, metadataValueChange);
			}
			if (value != null) {
			    final Date date = Conversions.parseDate(value);
			    getDataset().getStartDate().setValue(date.getTime());
			}
			break;
		    case OBS_METADATA:
			metadata = obs.getMetadata(metadataValueChange.getMetadata());
			if (metadata != null) {
			    if (qcValuesToSet != null) {
				updateQCWithNewValueIfPossible(updateWithNewValue, metadata, qcValuesToSet,
					metadataValueChange);
			    }
			    if (value != null) {
				updateObsMetadataValue(metadataValueChange.getMetadata(), metadata, value);
			    }
			}
			break;
		    case OBS_OCEAN_DEPTH:
			if (qcValuesToSet != null) {
			    updateQCWithNewValueIfPossible(updateWithNewValue, obs.getOceanDepth(), qcValuesToSet,
				    metadataValueChange);
			}
			if (value != null) {
			    obs.getOceanDepth().setValue(Double.parseDouble(value));
			}
			break;
		    case OBS_SENSOR_DIST_FROM_BOTTOM:
			if (qcValuesToSet != null) {
			    updateQCWithNewValueIfPossible(updateWithNewValue, obs.getSensor().getDistanceFromBottom(),
				    qcValuesToSet, metadataValueChange);
			}
			if (value != null) {
			    obs.getSensor().getDistanceFromBottom().setValue(Double.parseDouble(value));
			}
			break;
		    case OBS_SENSOR_NOMINAL_DEPTH:
			if (qcValuesToSet != null) {
			    updateQCWithNewValueIfPossible(updateWithNewValue, obs.getSensor().getNominalDepth(),
				    qcValuesToSet, metadataValueChange);
			}
			if (value != null) {
			    obs.getSensor().getNominalDepth().setValue(Double.parseDouble(value));
			}
			break;
		    case OBS_SENSOR_SAMPLING_RATE:
			// Value only
			if (value != null) {
			    obs.getSensor().setSamplingRateInSeconds(Double.parseDouble(value));
			}
			break;
		    }
		} catch (final NumberFormatException | ParseException e) {
		    SC3Logger.LOGGER.error(e.getMessage(), e);
		}
	    }
	}
    }

    /**
     * @param metadata
     *            used in override
     * @param valueAndQC
     * @param newValue
     */
    protected void updateObsMetadataValue(final String metadata, final ValueAndQC valueAndQC, final String newValue) {
	if (valueAndQC instanceof StringValueAndQC) {
	    ((StringValueAndQC) valueAndQC).setValue(newValue);
	} else if (valueAndQC instanceof DoubleValueAndQC) {
	    ((DoubleValueAndQC) valueAndQC).setValue(Double.valueOf(newValue));
	} else if (valueAndQC instanceof FloatValueAndQC) {
	    ((FloatValueAndQC) valueAndQC).setValue(Float.valueOf(newValue));
	} else if (valueAndQC instanceof LongValueAndQC) {
	    ((LongValueAndQC) valueAndQC).setValue(Long.valueOf(newValue));
	} else if (valueAndQC instanceof IntValueAndQC) {
	    ((IntValueAndQC) valueAndQC).setValue(Integer.valueOf(newValue));
	}
    }

    /**
     * @param lastChanges
     * @param updateWithNewQC
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected void updateQCsInSC3Model(final List<QCValueChange> lastChanges, final boolean updateWithNewQC) {
	SC3Logger.LOGGER.trace("updateQCsInSC3Model");
	for (final QCValueChange qcValueChange : lastChanges) {
	    final int qcToSet = (updateWithNewQC) ? qcValueChange.getNewQC() : qcValueChange.getOldQC();
	    // final int oldQC = (updateWithNewQC) ? qcValueChange.getOldQC() : qcValueChange.getNewQC();
	    // special case for POS$
	    if ((qcValueChange instanceof MetadataValueChange)
		    && (((MetadataValueChange) qcValueChange).getMetadata() != null)
		    && ((MetadataValueChange) qcValueChange).getMetadata().equals("POS$")) {
		qcValueChange.setParameterName(Observation.POSITION_VAR_NAME);
	    }
	    if ((qcToSet != QCValueChange.NO_NEW_QC) && (qcValueChange.getParameterName() != null)) {
		final QCValues qcValuesToSet = QCValues.getQCValues(qcToSet);
		// final QCValues oldQCValues = (oldQC != QCValueChange.NO_NEW_QC) ?
		// QCValues.getQCValuesMantis32532(oldQC)
		// : null;
		// final int observationIndex = qcValueChange.getObservationIndex();
		// it was like that before but the observationIndex is the observationIndex of the currentPlatform
		// the obs was the observationIndex of all observations, so it's the observationIndex of the first
		// platform
		// even if we are flagging on other platforms
		/*
		 * final Observation obs = (observationIndex == -1) ? getObservationWithoutObsIndex(qcValueChange) :
		 * getDataset().getObservations().get(observationIndex);
		 */
		Observation obs = null;

		// check if there is observations with the same id
		boolean duplicatedById = false;
		final ArrayList<String> obsIds = new ArrayList<String>();
		for (final Observation observation : getDataset().getObservations()) {
		    if (!obsIds.contains(observation.getId())) {
			obsIds.add(observation.getId());
		    } else {
			duplicatedById = true;
			break;
		    }
		}

		// if there is a duplicate with id in the list of observations, do not use
		// getObservationWithoutObsIndex() function that returns the first duplicate in the list of observations
		if (!duplicatedById) {
		    obs = getObservationWithoutObsIndex(qcValueChange);
		}
		// if obs is null, get the obs with the index, now it's exist
		if (obs == null) {
		    obs = getDataset().getObservations().get(qcValueChange.getObservationIndex());
		}

		final ArrayList<Parameter<? extends Number>> parameterList = getParam(obs,
			qcValueChange.getParameterName());
		for (Parameter<? extends Number> parameter : parameterList) {
		    if ((parameter == null) && qcValueChange.getParameterName().equals(Observation.TIME_VAR_NAME)) {
			parameter = obs.getTime();
		    }
		    if (parameter != null) {
			// exception thrown if the data's index is out of the parameter qcValues size
			try {
			    parameter.getQcValues().set(qcValueChange.getObservationLevel(), qcValuesToSet);
			} catch (final IndexOutOfBoundsException e) {
			    try {
				Dialogs.showErrorMessage(
					Messages.getMessage("coriolis-parameter.outOfBoundsException.title"),
					MessageFormat.format(
						Messages.getMessage("coriolis-parameter.outOfBoundsException.message"),
						parameter.getCode(), qcValueChange.getObservationLevel(),
						qcValuesToSet));

				throw new Exception(MessageFormat.format(
					Messages.getMessage("coriolis-parameter.outOfBoundsException.message"),
					parameter.getCode(), qcValueChange.getObservationLevel(), qcValuesToSet));
			    } catch (final Exception e1) {
			    }
			}

			/*
			 * if ((qcValuesToSet == QCValues.QC_9) || ((oldQCValues == QCValues.QC_9) &&
			 * isSetFillValueOnQC9())) { // Update value Number valueToSet; if (updateWithNewQC) {
			 * valueToSet = parameter.getFillValue(); } else { // Reset the Old Value valueToSet =
			 * qcValueChange.getParameterValue(); } if (valueToSet != null) { if (valueToSet instanceof
			 * Double) { ((List<Double>) parameter.getValues()).set(qcValueChange. getObservationLevel(),
			 * (Double) valueToSet); } else if (valueToSet instanceof Float) { ((List<Double>)
			 * parameter.getValues()).set(qcValueChange. getObservationLevel(), ((Float)
			 * valueToSet).doubleValue()); } else if (valueToSet instanceof Long) { ((List<Long>)
			 * parameter.getValues()).set(qcValueChange. getObservationLevel(), (Long) valueToSet); } } }
			 */
		    }
		}
	    }
	}
    }

    /**
     * @param undoRedoActions
     * @param updateWithNewValue
     */
    protected void updateQCsOnMap(final List<? extends UndoRedoAction> undoRedoActions,
	    final boolean updateWithNewValue) {
    }

    /**
     * /!\ DO NOT UPDATE QC_5 WITH NEW VALUE /!\
     *
     * @param updateWithNewValue
     * @param valueAndQCToUpdate
     * @param qcValuesToSet
     * @param metadataValueChange
     */
    protected void updateQCWithNewValueIfPossible(final boolean updateWithNewValue, final ValueAndQC valueAndQCToUpdate,
	    final QCValues qcValuesToSet, final MetadataValueChange metadataValueChange) {
	if (!(updateWithNewValue && (valueAndQCToUpdate.getQc() == QCValues.QC_5))) {
	    valueAndQCToUpdate.setQc(qcValuesToSet);
	} else {
	    metadataValueChange.resetNewAndOldQC();
	}
    }

    /**
     * Update the value and QC with new values
     *
     * @param currentVariableName
     * @param variableName
     * @param newValueStr
     * @param newQCValue
     * @param currentValueAndQC
     */
    protected void updateValueAndQC(final String currentVariableName, final String variableName,
	    final String newValueStr, final QCValues newQCValue, final ValueAndQC currentValueAndQC) {
	if ((currentValueAndQC != null)
		&& ((variableName.equals(currentVariableName)) || ((currentValueAndQC instanceof StringValuesAndQC)
			&& currentVariableName.startsWith(variableName)))) {
	    if (newValueStr != null) {
		if (currentValueAndQC instanceof DoubleValueAndQC) {
		    ((DoubleValueAndQC) currentValueAndQC).setValue(Double.parseDouble(newValueStr));
		} else if (currentValueAndQC instanceof FloatValueAndQC) {
		    ((FloatValueAndQC) currentValueAndQC).setValue(Float.parseFloat(newValueStr));
		} else if (currentValueAndQC instanceof IntValueAndQC) {
		    ((IntValueAndQC) currentValueAndQC).setValue(Integer.parseInt(newValueStr));
		} else if (currentValueAndQC instanceof LongValueAndQC) {
		    ((LongValueAndQC) currentValueAndQC).setValue(Long.parseLong(newValueStr));
		} else if (currentValueAndQC instanceof StringValueAndQC) {
		    ((StringValueAndQC) currentValueAndQC).setValue(newValueStr);
		} else if (currentValueAndQC instanceof StringValuesAndQC) {
		    try {
			final int index = Integer.parseInt(currentVariableName.replace(variableName + " ", ""));
			((StringValuesAndQC) currentValueAndQC).getValuesAsString().set(index, newValueStr);
			if (index == 0) {
			    ((StringValuesAndQC) currentValueAndQC).setQc(newQCValue);
			}
		    } catch (final NumberFormatException nfe) {
			SC3Logger.LOGGER.error(nfe.getMessage(), nfe);
		    }
		}
	    }

	    if (newQCValue != null) {
		currentValueAndQC.setQc(newQCValue);
	    }
	}
    }

    public HashMap<String, Integer> getOriginalQCs() {
	return this.originalQCs;
    }

    public void resetOriginalQCs() {
	this.originalQCs = new HashMap<String, Integer>();
    }

    public List<HashMap<String, ValueAndQC>> getMetadatas() {
	return this.metadatas;
    }

    public void transferMetadatas(final List<HashMap<String, ValueAndQC>> metadatas) {
	this.metadatas = metadatas;
    }

    public void addBlankMetadata() {
	metadatas.add(new HashMap<String, ValueAndQC>());
    }

    public void addMetadata(final int index, final String key, final ValueAndQC value) {
	if (metadatas.size() >= (index + 1)) {
	    metadatas.get(index).put(key, value);
	} else {
	    metadatas.add(new HashMap<String, ValueAndQC>());
	    metadatas.get(index).put(key, value);
	}
    }
}
