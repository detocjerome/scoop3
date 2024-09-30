package fr.ifremer.scoop3.gui.reference;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.bushe.swing.event.EventBus;

import fr.ifremer.scoop3.chart.model.ChartPhysicalVariable;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneForProfile;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneForTimeserie;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3GraphPanelListener;
import fr.ifremer.scoop3.controller.worflow.StepCode;
import fr.ifremer.scoop3.controller.worflow.SubStep;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataErrorMessageItem;
import fr.ifremer.scoop3.data.SuperposedModeEnum;
import fr.ifremer.scoop3.events.GuiEventChangeMainPanelToStep;
import fr.ifremer.scoop3.gui.common.CommonViewModel;
import fr.ifremer.scoop3.gui.common.DataOrReferenceViewController;
import fr.ifremer.scoop3.gui.common.jdialog.DisplayObservationInfoForLevelDialog;
import fr.ifremer.scoop3.gui.common.jdialog.ReportJDialog;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent.EVENT_ENUM;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyQCChangeEvent;
import fr.ifremer.scoop3.gui.data.ChangeAxisMinMaxDialog;
import fr.ifremer.scoop3.gui.data.DataViewImpl;
import fr.ifremer.scoop3.gui.data.commandButton.SelectAndChangeQCCommandButton;
import fr.ifremer.scoop3.gui.data.datatable.DataTableDialog;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableUpdateAbstract;
import fr.ifremer.scoop3.gui.data.popup.Scoop3ChartPanelChangeQCJMenuItem;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.infra.tools.Conversions;
import fr.ifremer.scoop3.infra.undo_redo.UndoRedoAction;
import fr.ifremer.scoop3.infra.undo_redo.data.QCValueChange;
import fr.ifremer.scoop3.infra.undo_redo.metadata.MetadataValueChange;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.Platform;
import fr.ifremer.scoop3.model.QCValues;
import javafx.geometry.Rectangle2D;

/**
 *
 *
 */
public class ReferenceViewController extends DataOrReferenceViewController {

    /**
     * Mouse Listener defined in BPC-GUI project
     */
    private class Scoop3ChartPanelMouseListener extends MouseAdapter implements JScoop3GraphPanelListener {

	@Override
	public void changeCurrentStation(final int[] newCurrentStation) {
	    boolean observationChanged = false;
	    if (getObservationNumber() != newCurrentStation[0]) {
		observationChanged = true;
		setObservationNumberAndSend(newCurrentStation[0]);
		if (getCommonViewImpl().getMetadataSplitPane() != null) {
		    getCommonViewImpl().getMetadataSplitPane().updateEnabledButtons();
		}
		setSelectedObservation(newCurrentStation[0]);
		setSelectedObservationOnSpecificPanel(newCurrentStation[0]);
	    }
	    setCurrentLevel(newCurrentStation[1]);
	    // FAE 26637
	    if (JScoop3ChartScrollPaneAbstract.isDisplayCircle()) {
		DisplayObservationInfoForLevelDialog.updateInfo(((DataViewImpl) getCommonViewImpl()).getScoop3Frame(),
			getCommonViewModel().getObservation(getObservationNumber()), levelIndex,
			((ReferenceViewModel) getCommonViewModel()).getParametersOrder());
	    }
	    if (observationChanged) {
		// FAE 26637
		// ((DataViewImpl)
		// getCommonViewImpl()).updateDisplayOrHidePointsIcons(DataViewImpl.DISPLAY_POINTS_AND_CIRLCE);
	    }
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(final MouseEvent e) {
	    // Only for right click
	    if (e.getButton() == MouseEvent.BUTTON3) {
		scoop3ChartPanelPopupMenu.show(e.getComponent(), e.getX(), e.getY());
	    }
	}

	@Override
	public void selectionDoneWithOtherMouseMode(final Rectangle2D displayChartTotalArea,
		final Rectangle2D displayChartSelectArea, final Point displayChartSelectAreaNewStartPoint,
		final Point displayChartSelectAreaNewEndPoint) {
	    final Scoop3ChartPanelChangeQCJMenuItem sourceForChangeQC = new Scoop3ChartPanelChangeQCJMenuItem("", true,
		    QCValues.getQCValues(((DataViewImpl) getCommonViewImpl()).getMouseSubModeQC()), null,
		    ReferenceViewController.this);
	    propertyChangeChangeQC(new SC3PropertyChangeEvent(sourceForChangeQC, EVENT_ENUM.CHANGE_QC));
	}

	@Override
	public void selectionDoneWithRemovalMode(final Rectangle2D displayChartTotalArea,
		final Rectangle2D displayChartSelectArea, final Point displayChartSelectAreaNewStartPoint,
		final Point displayChartSelectAreaNewEndPoint) {
	    // empty method
	}

	@Override
	public void zoomAll() {
	    // Not used
	}

	@Override
	public void zoomOnDisplayArea(final Rectangle2D displayChartTotalArea, final Rectangle2D displayChartSelectArea,
		final Point displayChartSelectAreaNewStartPoint, final Point displayChartSelectAreaNewEndPoint,
		final boolean zoomOnGraph, final String sourceClass) {
	    // not used
	}
    }

    /**
     * Height of 1 Graph
     */
    private static int graphHeight;

    /**
     * Width of 1 Graph
     */
    private static int graphWidth;

    private JScoop3ChartScrollPaneAbstract scoop3ChartPanel;

    private Scoop3ChartPanelMouseListener scoop3ChartPanelMouseListener;
    /**
     * Reference on the Popup Menu used by this component
     */
    private Scoop3RefChartPanelPopupMenu scoop3ChartPanelPopupMenu;
    static {
	try {
	    graphHeight = Integer
		    .parseInt(FileConfig.getScoop3FileConfig().getString("bpc-gui.reference-view.graph-heigth"));
	} catch (final NumberFormatException nfe) {
	    graphHeight = 600;
	}
	try {
	    graphWidth = Integer
		    .parseInt(FileConfig.getScoop3FileConfig().getString("bpc-gui.reference-view.graph-width"));
	} catch (final NumberFormatException nfe) {
	    graphWidth = 600;
	}
    }

    /**
     * Constructor
     *
     * @param referenceView
     *            The view
     * @param referenceViewModel
     *            The model
     */
    public ReferenceViewController(final ReferenceViewImpl referenceView, final ReferenceViewModel referenceViewModel,
	    final boolean isBPCVersion) {
	super(referenceView, referenceViewModel, isBPCVersion);

	scoop3ChartPanelPopupMenu = new Scoop3RefChartPanelPopupMenu(this);
	scoop3ChartPanelPopupMenu.setSource(scoop3ChartPanel);

	// Select the Observation with the first error message
	final CADataErrorMessageItem caDataErrorMessageItem = referenceView.getFirstDataErrorMessage();
	if (caDataErrorMessageItem != null) {
	    for (final Observation observation : referenceViewModel.getObservations()) {
		if (observation.getId().equals(caDataErrorMessageItem.getObs1Id())) {
		    setSelectedObservation(observation);
		}
	    }
	    setSelectedObservationOnSpecificPanel(getObservationNumber());
	}
    }

    /**
     * Set the new Observation number and send
     *
     * @param observationNum
     */
    @Override
    public void setObservationNumberAndSend(final int observationNum) {
	final Platform prevPlatform = getCommonViewModel().getPlatformForObservation(getObservationNumber());
	super.setObservationNumberAndSend(observationNum);
	final Platform curPlatform = getCommonViewModel().getPlatformForObservation(getObservationNumber());

	if ((prevPlatform != curPlatform) && isSuperposedMode()) {
	    // Superposed mode is on ... but the Platform changed => compute again the image ...
	    firstObservationIndex = -1;
	    lastObservationIndex = -1;
	    setSuperposedModeEnum(null);
	    propertyChangeAllProfiles();
	}
    }

    /**
     *
     * @param addInRedoable
     */
    private void propertyChangeUndo(final boolean addInRedoable) {
	final List<? extends UndoRedoAction> undoRedoActions = getCommonViewModel().undoLastChanges(addInRedoable);
	if (undoRedoActions != null) {
	    final List<QCValueChange> updatesForRefVariable = new ArrayList<>();
	    for (final UndoRedoAction undoRedoAction : undoRedoActions) {
		if (undoRedoAction instanceof QCValueChange) {
		    final QCValueChange qcChanged = (QCValueChange) undoRedoAction;
		    if (getCommonViewModel().getObservation(getObservationNumber()).getReferenceParameter().getCode()
			    .equals(qcChanged.getParameterName())) {
			updatesForRefVariable.add(qcChanged);
		    }
		}
	    }
	    scoop3ChartPanel.revertQC(updatesForRefVariable);
	}
	updateDatatableIfNeeded(undoRedoActions);
	getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		getCommonViewModel().isListOfRedoableChangesEmpty());
    }

    /**
     *
     * @param refreshOnly
     */
    public void updateChartPanelWithRefresh(final boolean refreshOnly) {
	if ((scoop3ChartPanel == null) || (!isSuperposedMode()) || !refreshOnly) {
	    if ((scoop3ChartPanel != null) && (scoop3ChartPanelMouseListener != null)) {
		scoop3ChartPanel.removeMouseListener(scoop3ChartPanelMouseListener);
		scoop3ChartPanel.removeJScoop3GraphPanelListener(scoop3ChartPanelMouseListener);
	    }

	    // Create a chart measure number / reference parameter
	    final String reference = ((ReferenceViewModel) getCommonViewModel()).getChartDataset().getReferenceLabel();
	    final ChartPhysicalVariable referencePhysicalVariable = ((ReferenceViewModel) getCommonViewModel())
		    .getChartDataset().getPhysicalVariable(reference);

	    final ChartPhysicalVariable levelPhysicalVariable = ((ReferenceViewModel) getCommonViewModel())
		    .getChartDataset().getPhysicalVariable(CommonViewModel.MEASURE_NUMBER);

	    final String abscissaLabel = Messages.getMessage(levelPhysicalVariable.getLabel());
	    String ordonneeLabel;

	    if (getCommonViewModel().doesDatasetContainProfiles()) {
		scoop3ChartPanel = new JScoop3ChartScrollPaneForProfile(levelPhysicalVariable,
			referencePhysicalVariable, graphWidth, graphHeight,
			((isSuperposedMode()) ? firstObservationIndex : getObservationNumber()),
			((isSuperposedMode()) ? lastObservationIndex : getObservationNumber()));
		ordonneeLabel = Messages.getMessage(referencePhysicalVariable.getLabel()) + " ("
			+ getCommonViewModel().getObservation(getObservationNumber()).getReferenceParameter().getUnit()
			+ ")";
	    } else {
		scoop3ChartPanel = new JScoop3ChartScrollPaneForTimeserie(levelPhysicalVariable,
			referencePhysicalVariable, graphWidth, graphHeight,
			((isSuperposedMode()) ? firstObservationIndex : getObservationNumber()),
			((isSuperposedMode()) ? lastObservationIndex : getObservationNumber()), getObservationNumber(),
			false);
		ordonneeLabel = Messages.getMessage(referencePhysicalVariable.getLabel());
	    }

	    if (scoop3ChartPanelMouseListener == null) {
		scoop3ChartPanelMouseListener = new Scoop3ChartPanelMouseListener();
	    }
	    scoop3ChartPanel.addMouseListener(scoop3ChartPanelMouseListener);
	    scoop3ChartPanel.addJScoop3GraphPanelListener(scoop3ChartPanelMouseListener);

	    if (scoop3ChartPanelPopupMenu != null) {
		scoop3ChartPanelPopupMenu.setSource(scoop3ChartPanel);
	    }
	    ((ReferenceViewImpl) getCommonViewImpl()).updateChartPanel(scoop3ChartPanel, abscissaLabel, ordonneeLabel);
	}

	scoop3ChartPanel.setCurrentStation(getObservationNumber());
	scoop3ChartPanel.paintChart();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_AllProfiles()
     */
    @Override
    protected void propertyChangeAllProfiles() {
	JScoop3ChartScrollPaneAbstract.setCoefX(0);
	if (!isSuperposedMode()) {
	    // Display all observations (darker) + current observation

	    // Retrieve the reference parameter
	    final String reference = ((ReferenceViewModel) getCommonViewModel()).getChartDataset().getReferenceLabel();
	    final ChartPhysicalVariable referencePhysicalVariable = ((ReferenceViewModel) getCommonViewModel())
		    .getChartDataset().getPhysicalVariable(reference);

	    firstObservationIndex = referencePhysicalVariable.getPhysicalValuesByStation().size() - 1;
	    lastObservationIndex = 0;
	    final String currentPlatformCode = referencePhysicalVariable.getPlatformsCodes()
		    .get(getObservationNumber());
	    for (int index = 0; index < referencePhysicalVariable.getPhysicalValuesByStation().size(); index++) {
		final String indexPlatformCode = referencePhysicalVariable.getPlatformsCodes().get(index);
		if (currentPlatformCode.equals(indexPlatformCode)) {
		    if (firstObservationIndex > index) {
			firstObservationIndex = index;
		    }
		    if (lastObservationIndex < index) {
			lastObservationIndex = index;
		    }
		}
	    }
	    if ((firstObservationIndex > lastObservationIndex) /* || (firstObservationIndex == lastObservationIndex) */) {
		firstObservationIndex = -1;
		lastObservationIndex = -1;
	    }
	} else {
	    // Display only the current observation only
	    firstObservationIndex = -1;
	    lastObservationIndex = -1;
	}

	updateChartPanelWithRefresh(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ChangeAxisMinMax(fr.ifremer.scoop3.
     * gui.common.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeChangeAxisMinMax(final SC3PropertyChangeEvent q11Evt) {
	final ChangeAxisMinMaxDialog changeAxisDialog = new ChangeAxisMinMaxDialog(
		((DataViewImpl) getCommonViewImpl()).getScoop3Frame(), scoop3ChartPanel);
	if (changeAxisDialog.updateAxis() == JOptionPane.YES_OPTION) {
	    // Get the min and Max values
	    final HashMap<String, double[]> minMaxForVariables = new HashMap<>();

	    final String abscissaVar = scoop3ChartPanel.getAbscissaPhysicalVar().getLabel();
	    final Float minAbscissa = changeAxisDialog.getAbscissaMinValue();
	    final Float maxAbscissa = changeAxisDialog.getAbscissaMaxValue();
	    if ((minAbscissa != null) && (maxAbscissa != null)) {
		minMaxForVariables.put(abscissaVar, new double[] { minAbscissa, maxAbscissa });
	    }

	    final String ordinateVar = scoop3ChartPanel.getOrdinatePhysicalVar().getLabel();
	    final Float minOrdinate = changeAxisDialog.getOrdinateMinValue();
	    final Float maxOrdinate = changeAxisDialog.getOrdinateMaxValue();
	    if ((minOrdinate != null) && (maxOrdinate != null)) {
		minMaxForVariables.put(ordinateVar, new double[] { minOrdinate, maxOrdinate });
	    }
	    scoop3ChartPanel.zoomForVariables(minMaxForVariables, true, false, false/* , getObservationNumber() */);
	}
    }

    /**
     * Method called when the propertyChange receives the parameter CHANGE_METADATA
     *
     * @param q11Evt
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void propertyChangeChangeMetadata(final SC3PropertyChangeEvent q11Evt) {
	if ((q11Evt.getSource() instanceof List)
		&& (((List<?>) q11Evt.getSource()).get(0) instanceof MetadataValueChange)) {
	    getCommonViewModel().updateMetadata((List<QCValueChange>) q11Evt.getSource());
	}
	getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		getCommonViewModel().isListOfRedoableChangesEmpty());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ChangeQC(fr.ifremer.scoop3.gui.common
     * .SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeChangeQC(final SC3PropertyChangeEvent q11Evt) {

	String variableName = null;
	boolean isCurrentStationOnly = true;
	int newQcValue = -1;
	double refMinValue = -1;
	double refMaxValue = -1;
	double physValMin = -1.0d;
	double physValMax = -1.0d;
	int observationIndex = -1;

	if (q11Evt.getSource() instanceof Scoop3ChartPanelChangeQCJMenuItem) {
	    // This changeQC comes from a graph
	    final Scoop3ChartPanelChangeQCJMenuItem scoop3ChartPanelChangeQCJMenuItem = (Scoop3ChartPanelChangeQCJMenuItem) q11Evt
		    .getSource();
	    variableName = scoop3ChartPanel.getVariableNameToUpdate();

	    isCurrentStationOnly = scoop3ChartPanelChangeQCJMenuItem.isCurrentStationOnly();
	    newQcValue = scoop3ChartPanelChangeQCJMenuItem.getQcToSet().getQCValue();

	    refMinValue = scoop3ChartPanel.updateQCsGetRefvalMin();
	    refMaxValue = scoop3ChartPanel.updateQCsGetRefvalMax();

	    physValMin = scoop3ChartPanel.updateQCsGetPhysvalMin();
	    physValMax = scoop3ChartPanel.updateQCsGetPhysvalMax();

	    observationIndex = getObservationNumber();
	} else if (q11Evt instanceof SC3PropertyQCChangeEvent) {
	    // This changeQC comes from the Error Dialog box
	    final SC3PropertyQCChangeEvent sc3PropertyQCChangeEvent = (SC3PropertyQCChangeEvent) q11Evt;

	    int index = 0;
	    for (final Observation observation : getCommonViewModel().getObservations()) {
		if (observation.getId().equals(sc3PropertyQCChangeEvent.getObsRef())) {
		    observationIndex = index;
		}
		index++;
	    }

	    variableName = sc3PropertyQCChangeEvent.getVariableName();

	    isCurrentStationOnly = true;
	    newQcValue = sc3PropertyQCChangeEvent.getNewQC().getQCValue();

	    refMinValue = getCommonViewModel().getObservation(observationIndex).getReferenceParameter().getValues()
		    .get(sc3PropertyQCChangeEvent.getRefLevel()).doubleValue();
	    refMaxValue = refMinValue;

	    physValMin = scoop3ChartPanel.getPhysicalValueForLevel(sc3PropertyQCChangeEvent.getRefLevel());
	    physValMax = physValMin;
	}

	final List<String> obsIds = new ArrayList<>();
	if (isCurrentStationOnly) {
	    obsIds.add(getCommonViewModel().getObservation(observationIndex).getId());
	} else {
	    for (final Observation observation : getCommonViewModel().getObservations()) {
		obsIds.add(observation.getId());
	    }
	}

	if (variableName != null) {
	    final List<List<? extends Number>> referenceValues = new ArrayList<>();
	    referenceValues
		    .add(getCommonViewModel().getObservation(observationIndex).getReferenceParameter().getValues());

	    final List<QCValueChange> qcsChanged = scoop3ChartPanel.updateQCs(obsIds, isCurrentStationOnly, newQcValue,
		    refMinValue, refMaxValue, physValMin, physValMax, variableName, referenceValues,
		    (getSuperposedModeEnum() != null ? getSuperposedModeEnum().toString()
			    : SuperposedModeEnum.CURRENT_OBSERVATION_ONLY.toString()),
		    isBPCVersion);

	    // add the first oldQC as the original QC
	    for (final QCValueChange qcv : qcsChanged) {
		if (getCommonViewModel().getOriginalQCs().get(qcv.getObservationIndex() + "/"
			+ qcv.getObservationLevel() + "/" + qcv.getParameterName()) == null) {
		    getCommonViewModel().getOriginalQCs().put(
			    qcv.getObservationIndex() + "/" + qcv.getObservationLevel() + "/" + qcv.getParameterName(),
			    qcv.getOldQC());
		}
	    }

	    if (!qcsChanged.isEmpty()) {
		getCommonViewModel().updateQCs(qcsChanged);

		final List<QCValueChange> updatesForRefVariable = new ArrayList<>();
		for (final QCValueChange qcChanged : qcsChanged) {
		    if (getCommonViewModel().getObservation(observationIndex).getReferenceParameter().getCode()
			    .equals(qcChanged.getParameterName())) {
			updatesForRefVariable.add(qcChanged);
		    }
		}
		scoop3ChartPanel.qcsChange(updatesForRefVariable);
	    }
	    getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		    getCommonViewModel().isListOfRedoableChangesEmpty());
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayPointsAndCircle()
     */
    @Override
    protected void propertyChangeDisplayCircleOnGraph() {
	((DataViewImpl) getCommonViewImpl()).updateDisplayOrHidePointsIcons(DataViewImpl.DISPLAY_POINTS_AND_CIRLCE);

	updateChartPanel(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayDataTable()
     */
    @Override
    protected void propertyChangeDisplayDataTable() {
	final Observation currentObs = getCommonViewModel().getDataset().getObservations().get(getObservationNumber());
	final DataTableDialog dataTableDialog = new DataTableDialog(
		((ReferenceViewImpl) getCommonViewImpl()).getScoop3Frame(), currentObs,

		((ReferenceViewModel) getCommonViewModel()).getParametersOrder(),
		getCommonViewImpl().getQCValuesSettable());

	final ArrayList<DataTableUpdateAbstract> updatesForVariables = dataTableDialog.getUpdates();
	if (!updatesForVariables.isEmpty()) {
	    // Update the SC3 Model
	    getCommonViewModel().updateDataTableChanges(updatesForVariables, getObservationNumber());

	    // Update the CartModel
	    ((ReferenceViewModel) getCommonViewModel()).convertScoop3ModelToChartModel();

	    propertyChangeShift();

	    getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		    getCommonViewModel().isListOfRedoableChangesEmpty());
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayedQC(fr.ifremer.scoop3.gui.
     * common .model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeDisplayedQC(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayLineOnGraph()
     */
    @Override
    protected void propertyChangeDisplayLineOnGraph() {
	((DataViewImpl) getCommonViewImpl()).updateDisplayOrHidePointsIcons(DataViewImpl.DISPLAY_LINE_ON_GRAPH);

	updateChartPanel(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayPoints()
     */
    @Override
    protected void propertyChangeDisplayPointsOnGraph() {
	((DataViewImpl) getCommonViewImpl()).updateDisplayOrHidePointsIcons(DataViewImpl.DISPLAY_POINTS);

	updateChartPanel(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayStationType(fr.ifremer.scoop3
     * .gui.common.model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeDisplayStationType(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayStatistics(fr.ifremer.scoop3
     * .gui.common.model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeDisplayStatistics(final SC3PropertyChangeEvent q11Evt) {
	// nothing to do here ...
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DivideTimeserie(fr.ifremer.scoop3
     * .gui.common.model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeChangeDivideTimeserie(final SC3PropertyChangeEvent q11Evt) {
	// nothing to do here ...
    }

    @Override
    protected void propertyChangeKeepBounds(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_Redo()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void propertyChangeRedo() {
	final List<? extends UndoRedoAction> redoRedoActions = getCommonViewModel().redoLastChanges();
	if (redoRedoActions.get(0) instanceof QCValueChange) {
	    propertyChangeRedoOnGraphs((List<QCValueChange>) redoRedoActions);
	}
	updateDatatableIfNeeded(redoRedoActions);
	getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		getCommonViewModel().isListOfRedoableChangesEmpty());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_RedoOnGraphs(java.util.List)
     */
    @Override
    protected void propertyChangeRedoOnGraphs(final List<QCValueChange> qcsChanged) {
	if (qcsChanged != null) {
	    final List<QCValueChange> updatesForRefVariable = new ArrayList<>();
	    for (final QCValueChange qcChanged : qcsChanged) {
		if (getCommonViewModel().getObservation(getObservationNumber()).getReferenceParameter().getCode()
			.equals(qcChanged.getParameterName())) {
		    updatesForRefVariable.add(qcChanged);
		}
	    }
	    scoop3ChartPanel.qcsChange(updatesForRefVariable);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_Shift()
     */
    @Override
    protected void propertyChangeShift() {
	// Get the currentAbscissa Range
	double currentMinAbscissaPhysVal = scoop3ChartPanel.getMinAbscissaPhysValBeforeCoefX();
	if (currentMinAbscissaPhysVal == Double.MIN_VALUE) {
	    currentMinAbscissaPhysVal = scoop3ChartPanel.getMinAbscissaPhysVal();
	}
	double currentMaxAbscissaPhysVal = scoop3ChartPanel.getMaxAbscissaPhysValBeforeCoefX();
	if (currentMaxAbscissaPhysVal == Double.MAX_VALUE) {
	    currentMaxAbscissaPhysVal = scoop3ChartPanel.getMaxAbscissaPhysVal();
	}
	JScoop3ChartScrollPaneAbstract.setLastMinAndMaxAbscissaPhysVal(currentMinAbscissaPhysVal,
		currentMaxAbscissaPhysVal);

	updateChartPanelWithRefresh(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_Undo()
     */
    @Override
    protected void propertyChangeUndo() {
	propertyChangeUndo(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_UndoAll()
     */
    @Override
    protected void propertyChangeUndoAll() {
	do {
	    propertyChangeUndo(false);
	} while (!getCommonViewModel().isListOfUndoableChangesEmpty());

	getCommonViewModel().clearRedoableChanges();

	getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		getCommonViewModel().isListOfRedoableChangesEmpty());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_Validate()
     */
    @Override
    protected void propertyChangeValidate() {
	ReportJDialog.disposeIfExists();
	// Check if all error messages are checked
	if (getCommonViewModel().isThereUncheckedErrorMessages(STEP_TYPE.Q2_CONTROL_AUTO_DATA)) {
	    // Do NOT go to Q21_DATA
	    EventBus.publish(new GuiEventChangeMainPanelToStep(StepCode.START, SubStep.GOHOME));
	} else {
	    // Go to Q21_DATA
	    EventBus.publish(new GuiEventChangeMainPanelToStep(StepCode.QC21, SubStep.QC21_DATA));
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ZoomIn(fr.ifremer.scoop3.gui.common
     * .SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeZoomIn(final SC3PropertyChangeEvent q11Evt) {
	scoop3ChartPanel.zoomIn();
    }

    @Override
    protected void propertyChangeChangeSuperposedMode(final SC3PropertyChangeEvent q11Evt) {
	// nothing to do
    }

    /*
     * (non-Javadoc) <<<<<<< .mine
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ZoomInitial(fr.ifremer.scoop3.gui.
     * common .SC3PropertyChangeEvent) =======
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ZoomInitial(fr.ifremer.scoop3.gui.
     * common .SC3PropertyChangeEvent) >>>>>>> .r1493
     */
    @Override
    protected void propertyChangeZoomInitial(final SC3PropertyChangeEvent q11Evt) {
	scoop3ChartPanel.zoomAll();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ZoomOut(fr.ifremer.scoop3.gui.common
     * .SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeZoomOut(final SC3PropertyChangeEvent q11Evt) {
	scoop3ChartPanel.zoomOut();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#removeSelectionBox()
     */
    @Override
    public void removeSelectionBox() {
	scoop3ChartPanel.removeSelectionBox();
    }

    @Override
    protected void setCurrentLevel(final int levelIndex) {
	this.levelIndex = levelIndex;

	DisplayObservationInfoForLevelDialog.updateInfoIfVisible(
		getCommonViewModel().getObservation(getObservationNumber()), levelIndex,
		((ReferenceViewModel) getCommonViewModel()).getParametersOrder());

	/*
	 * Update info
	 */
	final Observation obs = getCommonViewModel().getObservation(getObservationNumber());

	final StringBuffer newInfoToDisplay = new StringBuffer();
	newInfoToDisplay.append(obs.getReferenceParameter().getCode());
	newInfoToDisplay.append(" : ");
	if (obs.getReferenceParameter().getValues().size() > levelIndex) {
	    if (getCommonViewModel().getDataset().getDatasetType() != DatasetType.PROFILE) {
		newInfoToDisplay.append(Conversions.formatDateAndHourMinSec(
			new Date((Long) obs.getReferenceParameter().getValues().get(levelIndex))));
	    } else {
		newInfoToDisplay.append(obs.getReferenceParameter().getValues().get(levelIndex));
	    }
	} else {
	    newInfoToDisplay.append("---");
	}

	((DataViewImpl) getCommonViewImpl()).updateInfoLabel(newInfoToDisplay.toString());

	scoop3ChartPanel.setCurrentLevel(levelIndex);
	scoop3ChartPanel.paintChart();

    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#specificPrepareForDispose()
     */
    @Override
    protected void specificPrepareForDispose() {
	super.specificPrepareForDispose();

	if ((scoop3ChartPanel != null) && (scoop3ChartPanelMouseListener != null)) {
	    scoop3ChartPanel.removeMouseListener(scoop3ChartPanelMouseListener);
	    scoop3ChartPanelMouseListener = null;
	}
	scoop3ChartPanel = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#updateChartPanel()
     */
    @Override
    protected void updateChartPanel(final boolean chartsWithAllParameters) {
	updateChartPanelWithRefresh(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#updateMouseCursorForGraphs(java.awt.Cursor)
     */
    @Override
    protected void updateMouseCursorForGraphs(final Cursor newCursor) {
	scoop3ChartPanel.setCursor(newCursor);
    }

    @Override
    public void validateUpdates() {
	// empty method
    }

    @Override
    public void cancelUpdates() {
	// empty method
    }

    @Override
    protected void propertyChangeRemoveMeasure(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    @Override
    protected void propertyChangeEditClimatoAdditionalGraphs(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    @Override
    protected void propertyChangeUpdatePositionGraphs() {
	// empty method
    }

    public void setScoop3ChartPanelPopupMenu(final Scoop3RefChartPanelPopupMenu scoop3ChartPanelPopupMenu) {
	this.scoop3ChartPanelPopupMenu = scoop3ChartPanelPopupMenu;
    }

    public JScoop3ChartScrollPaneAbstract getScoop3ChartPanel() {
	return this.scoop3ChartPanel;
    }

    @Override
    protected void propertyChangeDisplayOnlyQCOnGraph(final SC3PropertyChangeEvent q11Evt) {
	if (q11Evt.getSource() instanceof SelectAndChangeQCCommandButton) {
	    final SelectAndChangeQCCommandButton selectAndChangeQCCommandButton = (SelectAndChangeQCCommandButton) q11Evt
		    .getSource();

	    ((DataViewImpl) getCommonViewImpl()).updateDisplayOnlyQCOnGraph(selectAndChangeQCCommandButton);

	    this.scoop3ChartPanel.repaint();
	}
    }

    @Override
    protected void propertyChangeExcludeOnlyQCOnGraph(final SC3PropertyChangeEvent q11Evt) {
	if (q11Evt.getSource() instanceof SelectAndChangeQCCommandButton) {
	    final SelectAndChangeQCCommandButton selectAndChangeQCCommandButton = (SelectAndChangeQCCommandButton) q11Evt
		    .getSource();

	    ((DataViewImpl) getCommonViewImpl()).updateExcludeOnlyQCOnGraph(selectAndChangeQCCommandButton);

	    this.scoop3ChartPanel.repaint();
	}
    }
}
