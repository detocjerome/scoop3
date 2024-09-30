package fr.ifremer.scoop3.gui.map;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.omGraphics.OMGraphicList;

import fr.ifremer.scoop3.core.report.utils.ReportUtils;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAMetadataErrorMessageItem;
import fr.ifremer.scoop3.data.SuperposedModeEnum;
import fr.ifremer.scoop3.gui.common.CommonViewController;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyMultipleErrorMessagesChangeEvent;
import fr.ifremer.scoop3.gui.utils.MapCommonFunctions;
import fr.ifremer.scoop3.infra.event.MapPropertyChangeEvent;
import fr.ifremer.scoop3.infra.event.MapPropertyChangeEvent.MAP_EVENT_ENUM;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.undo_redo.UndoRedoAction;
import fr.ifremer.scoop3.infra.undo_redo.data.QCValueChange;
import fr.ifremer.scoop3.infra.undo_redo.metadata.MetadataValueChange;
import fr.ifremer.scoop3.map.view.Scoop3MapPanel;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;

/**
 *
 *
 */
public class MapViewController extends CommonViewController {

    private long lastPropertyChangeTime = 0l;
    private Scoop3MapPanel scoop3MapPanel;
    private PropertyChangeListener scoop3MapPropertyChangeListener;

    /**
     * Constructor
     *
     * @param metaMapViewImpl
     *            The view
     * @param metadataViewModel
     *            The model
     */
    public MapViewController(final MapViewImpl metaMapViewImpl, final MapViewModel metadataViewModel) {
	super(metaMapViewImpl, metadataViewModel);

	createMapComponent(MapCommonFunctions.createOMListFromDataset(getCommonViewModel().getDataset()));
	createUndoRedoRibbonBandActionListener();
	createMapRibbonBandActionListener();

	((MapViewImpl) getCommonViewImpl()).setActionListenerForUpdatingQC((final ActionEvent ae) ->
	// ae.getSource() is an ARRAY [(String) observationReference, (Integer) currentLevel, (Integer)
	// newQCValue, lat, lon]
	getPropertyChangeSupport()
		.firePropertyChange(new MapPropertyChangeEvent(ae.getSource(), MAP_EVENT_ENUM.UPDATE_QC_FOR_OBS)));

	addKeyListener();

    }

    @Override
    public SuperposedModeEnum getSuperposedModeEnum() {
	return null;
    }

    @Override
    public void initViewAfterShow() {
	setSelectedObservationOnSpecificPanel(0);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewController#isSuperposedMode()
     */
    @Override
    public boolean isSuperposedMode() {
	return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewController#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
	if (!(evt instanceof SC3PropertyChangeEvent)) {
	    // Only SC3PropertyChangeEvent are managed here
	    return;
	}

	final SC3PropertyChangeEvent q11Evt = (SC3PropertyChangeEvent) evt;

	if (lastPropertyChangeTime != q11Evt.getCurrentTime()) {
	    lastPropertyChangeTime = q11Evt.getCurrentTime();

	    SC3Logger.LOGGER.trace(
		    "MapViewController - propertyChange - " + evt.getPropertyName() + " / " + lastPropertyChangeTime);

	    switch (q11Evt.getEventEnum()) {
	    case ALL_PROFILES:
		propertyChangeAllProfiles();
		break;
	    case CHANGE_AXIS_MIN_MAX:
		propertyChangeChangeAxisMinMax(q11Evt);
		break;
	    case CHANGE_COMMENT:
		propertyChangeChangeComment(q11Evt);
		break;
	    case CHANGE_IS_CHECKED:
		propertyChangeChangeIsChecked(q11Evt);
		break;
	    case CHANGE_METADATA:
		propertyChangeChangeMetadata(q11Evt);
		break;
	    case CHANGE_MULTIPLE:
		propertyChangeChangeMultipleErrorMessages(q11Evt);
		break;
	    case CHANGE_QC:
		propertyChangeChangeQC(q11Evt);
		break;
	    case DISPLAYED_QC:
		propertyChangeDisplayedQC(q11Evt);
		break;
	    case DISPLAY_DATA_TABLE:
		propertyChangeDisplayDataTable();
		break;
	    case DISPLAY_CIRCLE_ON_GRAPH:
		propertyChangeDisplayCircleOnGraph();
		break;
	    case DISPLAY_LINE_ON_GRAPH:
		propertyChangeDisplayLineOnGraph();
		break;
	    case DISPLAY_ONLY_QC:
		propertyChangeDisplayOnlyQCOnGraph(q11Evt);
		break;
	    case DISPLAY_POINTS_ON_GRAPH:
		propertyChangeDisplayPointsOnGraph();
		break;
	    case DISPLAY_STATION_TYPE:
		propertyChangeDisplayStationType(q11Evt);
		break;
	    case DISPLAY_STATISTICS:
		propertyChangeDisplayStatistics(q11Evt);
		break;
	    case EXCLUDE_ONLY_QC:
		propertyChangeExcludeOnlyQCOnGraph(q11Evt);
		break;
	    case KEEP_BOUNDS:
		propertyChangeKeepBounds(q11Evt);
		break;
	    case MOUSE_MODE_CHANGED:
		propertyChangeMouseModeChanged();
		break;
	    case REDO:
		propertyChangeRedo();
		break;
	    case RESET_NEAREST_PROFILES:
		break;
	    case SHIFT:
		propertyChangeShift();
		break;
	    case UNDO:
		propertyChangeUndo();
		break;
	    case UNDO_ALL:
		propertyChangeUndoAll();
		break;
	    case VALIDATE:
		propertyChangeValidate();
		break;
	    case ZOOM_IN:
		propertyChangeZoomIn(q11Evt);
		break;
	    case ZOOM_INITIAL:
		propertyChangeZoomInitial(q11Evt);
		break;
	    case ZOOM_OUT:
		propertyChangeZoomOut(q11Evt);
		break;
	    default:
		break;
	    }
	}
    }

    /**
     * Save A JPEG IMAGE Of the map view near the XML context file
     */
    @Override
    public void saveMapImage() {
	this.scoop3MapPanel.getObservationLayer().exportMapImage("map.jpg");
    }

    /**
     * Save A JPEG IMAGE Of the map in context
     */
    @Override
    public void saveMap() {
	this.scoop3MapPanel.getObservationLayer().saveMap();
    }

    /**
     * Get the initial latitude, longitude and zoom on map
     *
     * @return
     */
    @Override
    public double[][] getInitialMapBounds() {
	return this.scoop3MapPanel.getObservationLayer().getInitialMapBounds();
    }

    /**
     * Create a string of map object in geoJson
     *
     * @return
     */
    @Override
    public String createGeoJsonString() {
	return this.scoop3MapPanel.getObservationLayer().createGeoJsonString();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewController#setSelectedObservationOnSpecificPanel(int)
     */
    @Override
    public void setSelectedObservationOnSpecificPanel(final int index) {
	final Observation observation = getCommonViewModel().getObservation(index);

	if (getCommonViewImpl() instanceof MapViewImpl) {
	    ((MapViewImpl) getCommonViewImpl()).updateDepthChart(index);
	}

	// Send the event for the Map
	getPropertyChangeSupport().firePropertyChange(new MapPropertyChangeEvent(
		MapCommonFunctions.getPointModelForObs(observation, getCurrentLevel(),
			getCommonViewModel().getDataset().getDatasetType().toString(), true),
		MAP_EVENT_ENUM.SELECT_OBSERVATION_NAV));
    }

    /**
     * Create the component map
     *
     * @return The jpanel containing the map
     */
    private void createMapComponent(final OMGraphicList omList) {

	scoop3MapPanel = new Scoop3MapPanel(new PropertyHandler(new Properties()), true);
	final JPanel mapPanel = scoop3MapPanel.init(omList, false);
	scoop3MapPanel.setMapBackupDirectory(getCommonViewImpl().getScoop3Frame(), ReportUtils.getContextDir());

	scoop3MapPropertyChangeListener = (final PropertyChangeEvent evt) -> {
	    if (evt.getPropertyName().equals("SELECTED_OBSERVATION_MAP")) {
		SC3Logger.LOGGER
			.trace("MapViewController - scoop3MapPanel - propertyChangeEvent : SELECTED_OBSERVATION_MAP");
		setSelectedObservation(evt.getNewValue());
	    }
	    if (evt.getPropertyName().equals("TOGGLE_FULL_SCREEN_FOR_MAP")) {
		SC3Logger.LOGGER
			.trace("MapViewController - scoop3MapPanel - propertyChangeEvent : TOGGLE_FULL_SCREEN_FOR_MAP");
		final boolean setFullScreen = ((MapViewImpl) getCommonViewImpl()).toggleFullScreenForMap();
		scoop3MapPanel.setToggleFullScreen();
	    }
	};

	scoop3MapPanel.addPropertyChangeListener(scoop3MapPropertyChangeListener);

	getPropertyChangeSupport().addPropertyChangeListener(scoop3MapPanel);

	// Creation du Panel contenant le graphe de vitesse ou profondeur.
	getCommonViewImpl().setCenterSplitPane(new JSplitPane(
		(getCommonViewModel().getDataset().getDatasetType() == DatasetType.TIMESERIE)
			? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT,
		mapPanel, ((MapViewImpl) getCommonViewImpl()).createChart()));
	// map and meta-data split pane
	getCommonViewImpl().getCenterSplitPane().setDividerSize(8);
	// Add arrows (left and right) in the divider
	getCommonViewImpl().getCenterSplitPane().setOneTouchExpandable(true);

	if (getCommonViewModel().getDataset().getDatasetType() == DatasetType.TIMESERIE) {
	    // Try to set the divider correctly
	    final int frameWidth = getCommonViewImpl().getScoop3Frame().getWidth();
	    final int leftPanelWidth = 500;
	    final int dividerLocation = frameWidth - leftPanelWidth - 100;
	    getCommonViewImpl().getCenterSplitPane().setDividerLocation(dividerLocation);
	}

	getCommonViewImpl().getEastPanel().add(getCommonViewImpl().getCenterSplitPane(), BorderLayout.CENTER);
    }

    protected int getCurrentLevel() {
	return 0;
    }

    /**
     * Method called when the propertyChange receives the parameter ALL_PROFILES
     */
    protected void propertyChangeAllProfiles() {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter CHANGE_AXIS_MIN_MAX
     *
     * @param q11Evt
     */
    protected void propertyChangeChangeAxisMinMax(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter DIVIDE_TS
     *
     * @param q11Evt
     */
    protected void propertyChangeChangeDivideTimeserie(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter CHANGE_COMMENT
     *
     * @param q11Evt
     */
    protected void propertyChangeChangeComment(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter CHANGE_IS_CHECKED
     *
     * @param q11Evt
     */
    protected void propertyChangeChangeIsChecked(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter CHANGE_METADATA
     *
     * @param q11Evt
     */
    @SuppressWarnings("unchecked")
    protected void propertyChangeChangeMetadata(final SC3PropertyChangeEvent q11Evt) {
	if ((q11Evt.getSource() instanceof List)
		&& (((List<?>) q11Evt.getSource()).get(0) instanceof MetadataValueChange)) {
	    getCommonViewModel().updateMetadata((List<QCValueChange>) q11Evt.getSource());
	}
	getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		getCommonViewModel().isListOfRedoableChangesEmpty());
    }

    /**
     * Method called when the propertyChange receives the parameter CHANGE_MULTIPLE
     *
     * @param q11Evt
     */
    protected void propertyChangeChangeMultipleErrorMessages(final SC3PropertyChangeEvent q11Evt) {

	if (q11Evt instanceof SC3PropertyMultipleErrorMessagesChangeEvent) {
	    final SC3PropertyMultipleErrorMessagesChangeEvent sc3PropertyMultipleErrorMessagesChangeEvent = (SC3PropertyMultipleErrorMessagesChangeEvent) q11Evt;

	    final List<QCValueChange> changed = new ArrayList<>();

	    final int indexMax = sc3PropertyMultipleErrorMessagesChangeEvent.getErrorMessagesToUpdate().size();

	    for (int msgIndex = 0; msgIndex < indexMax; msgIndex++) {
		final CAErrorMessageItem caErrorMessageItem = sc3PropertyMultipleErrorMessagesChangeEvent
			.getErrorMessagesToUpdate().get(msgIndex);

		// Find corresponding observation
		int observationIndex = -1;
		int index = 0;
		for (final Observation observation : getCommonViewModel().getObservations()) {
		    if (observation.getId().equals(caErrorMessageItem.getObs1Id())) {
			observationIndex = index;
		    }
		    index++;
		}

		MetadataValueChange.QC_TO_UPDATE qcToUpdate = null;
		String metadata = null;
		if (caErrorMessageItem instanceof CAMetadataErrorMessageItem) {
		    qcToUpdate = ((CAMetadataErrorMessageItem) caErrorMessageItem)
			    .getQcToUpdateForMetadataValueChange();
		    metadata = ((CAMetadataErrorMessageItem) caErrorMessageItem).getMetadata();
		}

		final MetadataValueChange dataValueChange;
		if (sc3PropertyMultipleErrorMessagesChangeEvent.getQcToSet() != null) {
		    int oldQCToSet = QCValueChange.NO_NEW_QC;
		    if (caErrorMessageItem.getFlagManual() != null) {
			oldQCToSet = caErrorMessageItem.getFlagManual().getQCValue();
		    } else if (caErrorMessageItem.getFlagAuto() != null) {
			oldQCToSet = caErrorMessageItem.getFlagAuto().getQCValue();
		    }
		    dataValueChange = new MetadataValueChange(qcToUpdate, metadata, null, observationIndex,
			    caErrorMessageItem.getObs1Id(), //
			    oldQCToSet, //
			    sc3PropertyMultipleErrorMessagesChangeEvent.getQcToSet().getQCValue());
		    dataValueChange.setErrorMessage(caErrorMessageItem.getDetails());
		} else {
		    dataValueChange = new MetadataValueChange(qcToUpdate, metadata, observationIndex,
			    caErrorMessageItem.getObs1Id(), caErrorMessageItem.getDetails());
		}

		// add the first oldQC as the original QC
		if (getCommonViewModel().getOriginalQCs()
			.get(dataValueChange.getObsId() + "/" + dataValueChange.getMetadata()) == null) {
		    getCommonViewModel().getOriginalQCs().put(
			    dataValueChange.getObsId() + "/" + dataValueChange.getMetadata(),
			    dataValueChange.getOldQC());
		}

		dataValueChange.setIsErrorChecked(sc3PropertyMultipleErrorMessagesChangeEvent.isChecked());
		dataValueChange.setIsErrorCheckedOldValue(caErrorMessageItem.isErrorChecked());

		dataValueChange.setComment(sc3PropertyMultipleErrorMessagesChangeEvent.getCommentToSet());
		dataValueChange.setCommentOldValue(caErrorMessageItem.getComment());

		changed.add(dataValueChange);
	    }

	    // Update Metadata and QCs ...
	    getCommonViewModel().updateQCs(changed);

	    updateDatatableIfNeeded(changed);

	    getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		    getCommonViewModel().isListOfRedoableChangesEmpty());
	}

    }

    /**
     * Method called when the propertyChange receives the parameter CHANGE_QC
     *
     * @param q11Evt
     */
    protected void propertyChangeChangeQC(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_POINTS_AND_CIRCLE
     */
    protected void propertyChangeDisplayCircleOnGraph() {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_DATA_TABLE
     */
    protected void propertyChangeDisplayDataTable() {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter DISPLAYED_QC
     *
     * @param q11Evt
     */
    protected void propertyChangeDisplayedQC(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_LINE_ON_GRAPH
     */
    protected void propertyChangeDisplayLineOnGraph() {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_ONLY_QC
     *
     * @param q11Evt
     */
    protected void propertyChangeDisplayOnlyQCOnGraph(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter EXCLUDE_ONLY_QC
     *
     * @param q11Evt
     */
    protected void propertyChangeExcludeOnlyQCOnGraph(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_POINTS
     */
    protected void propertyChangeDisplayPointsOnGraph() {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_STATION_TYPE
     */
    protected void propertyChangeDisplayStationType(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_STATISTICS
     *
     * @param q11Evt
     */
    protected void propertyChangeDisplayStatistics(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter MOUSE_MODE_CHANGED
     */
    protected void propertyChangeMouseModeChanged() {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter KEEP_BOUNDS
     */
    protected void propertyChangeKeepBounds(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter REDO
     */
    protected void propertyChangeRedo() {
	final List<? extends UndoRedoAction> undoRedoActions = getCommonViewModel().redoLastChanges();
	updateDatatableIfNeeded(undoRedoActions);
    }

    /**
     * Method called when the propertyChange receives the parameter SHIFT
     */
    protected void propertyChangeShift() {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter UNDO
     */
    protected void propertyChangeUndo() {
	final List<? extends UndoRedoAction> undoRedoActions = getCommonViewModel().undoLastChanges(true);
	updateDatatableIfNeeded(undoRedoActions);
    }

    /**
     * Method called when the propertyChange receives the parameter UNDO_ALL
     */
    protected void propertyChangeUndoAll() {
	do {
	    final List<? extends UndoRedoAction> undoRedoActions = getCommonViewModel().undoLastChanges(false);
	    updateDatatableIfNeeded(undoRedoActions);
	} while (!getCommonViewModel().isListOfUndoableChangesEmpty());

	getCommonViewModel().clearRedoableChanges();
	getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		getCommonViewModel().isListOfRedoableChangesEmpty());
    }

    /**
     * Method called when the propertyChange receives the parameter VALIDATE
     */
    protected void propertyChangeValidate() {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter ZOOM_IN
     *
     * @param q11Evt
     */
    protected void propertyChangeZoomIn(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter ZOOM_INITIAL
     *
     * @param q11Evt
     */
    protected void propertyChangeZoomInitial(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    /**
     * Method called when the propertyChange receives the parameter ZOOM_OUT
     *
     * @param q11Evt
     */
    protected void propertyChangeZoomOut(final SC3PropertyChangeEvent q11Evt) {
	// empty method
    }

    @Override
    protected void setCurrentLevel(final int levelIndex) {
	// empty method
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewController#specificPrepareForDispose()
     */
    @Override
    protected void specificPrepareForDispose() {
	getPropertyChangeSupport().removePropertyChangeListener(scoop3MapPanel);
	scoop3MapPanel.removePropertyChangeListener(scoop3MapPropertyChangeListener);
	scoop3MapPanel.clean();
	scoop3MapPanel = null;
	scoop3MapPropertyChangeListener = null;
    }
}
