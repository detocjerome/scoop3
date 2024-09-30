/**
 *
 */
package fr.ifremer.scoop3.gui.common;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.swing.JComponent;

import fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane;
import fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane.MouseMode;
import fr.ifremer.scoop3.data.SuperposedModeEnum;
import fr.ifremer.scoop3.gui.common.MetadataSplitPane.InfoInObservationSubPanel;
import fr.ifremer.scoop3.gui.common.jdialog.ReportJDialog;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent.EVENT_ENUM;
import fr.ifremer.scoop3.gui.data.DataViewController;
import fr.ifremer.scoop3.gui.map.MapViewController;
import fr.ifremer.scoop3.gui.map.MapViewImpl;
import fr.ifremer.scoop3.gui.reference.ReferenceViewController;
import fr.ifremer.scoop3.gui.reference.Scoop3RefChartPanelPopupMenu;
import fr.ifremer.scoop3.infra.event.MapPropertyChangeEvent;
import fr.ifremer.scoop3.infra.event.MapPropertyChangeEvent.MAP_EVENT_ENUM;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.infra.undo_redo.UndoRedoAction;
import fr.ifremer.scoop3.infra.undo_redo.metadata.MetadataValueChange;
import fr.ifremer.scoop3.map.model.PointModel;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.Platform;

/**
 * This class is used by MetadataSplitPane
 */
public abstract class CommonViewController implements PropertyChangeListener {

    private static long keyPressedDelayBeforeChangingStation;

    public static Boolean keyListenerEnabled = true;

    /**
     * Listener
     */
    protected AWTEventListener awtEventListener;

    /**
     * Reference on the View
     */
    private CommonViewImpl commonViewImpl;
    /**
     * Reference on the Model
     */
    private CommonViewModel commonViewModel;
    /**
     * Reference on the selected Observation
     */
    private int observationIndex = 0;

    /**
     *
     */
    private final PropertyChangeSupport propertyChangeSupport;
    /**
     * Level selected
     */
    protected int levelIndex = 0;
    static {
	try {
	    keyPressedDelayBeforeChangingStation = Long.parseLong(
		    FileConfig.getScoop3FileConfig().getString("gui.key-pressed-delay-before-changing-station"));
	} catch (final NumberFormatException nfe) {
	    keyPressedDelayBeforeChangingStation = 500l;
	}
    }

    /**
     * Enabled (or disabled) the Key Listener
     *
     * @param keyListenerEnabled
     */
    public static void setKeyListenerEnabled(final boolean keyListenerEnabled) {
	synchronized (CommonViewController.keyListenerEnabled) {
	    CommonViewController.keyListenerEnabled = keyListenerEnabled;
	}
    }

    /**
     * @param commonViewImpl
     * @param commonViewModel
     */
    protected CommonViewController(final CommonViewImpl commonViewImpl, final CommonViewModel commonViewModel) {
	this.commonViewImpl = commonViewImpl;
	this.commonViewModel = commonViewModel;

	levelIndex = 0;

	if (commonViewImpl.getMetadataSplitPane() != null) {
	    commonViewImpl.getMetadataSplitPane().setCommonViewController(this);
	}

	propertyChangeSupport = new PropertyChangeSupport(this);
	propertyChangeSupport.addPropertyChangeListener(this);

	final PropertyChangeListener propertyChangeListener = (final PropertyChangeEvent evt) -> {
	    if ((evt instanceof MapPropertyChangeEvent) && (((MapPropertyChangeEvent) evt)
		    .getEventEnum() == MAP_EVENT_ENUM.SELECT_OBSERVATION_BY_REFERENCE)) {
		SC3Logger.LOGGER.trace("CommonViewController " + evt.getPropertyName());
		final int localObservationIndex = (int) evt.getSource();
		if (localObservationIndex != getObservationNumber()) {
		    setObservationNumberAndSend(localObservationIndex);
		    setBackAndForwardButtons();
		    setSelectedObservation(localObservationIndex);
		    setSelectedObservationOnSpecificPanel(localObservationIndex);
		}
		final int localLevelIndex = ((MapPropertyChangeEvent) evt).getLevelIndex();
		if ((localLevelIndex != CommonViewController.this.levelIndex) && (localLevelIndex > 0)
			&& (localLevelIndex < commonViewModel.getObservations().get(localObservationIndex)
				.getReferenceParameter().getValues().size())) {
		    setCurrentLevel(localLevelIndex);
		}
	    }
	};
	commonViewImpl.setPropertyChangeListener(propertyChangeListener);

	updateDatasetMetadataTable();
	final Observation observation = commonViewModel.getObservation(0);
	updateObservationMetadatas(observation, true);

	setBackAndForwardButtons();

	// Send the Property Change Listener in the view
	commonViewImpl.setPropertyChangeSupport(propertyChangeSupport);
    }

    /**
     *
     * @return the commonViewImpl
     */
    public CommonViewImpl getCommonViewImpl() {
	return commonViewImpl;
    }

    /**
     * @return the commonViewModel
     */
    public CommonViewModel getCommonViewModel() {
	return commonViewModel;
    }

    /**
     * @return the index of the first Observation for the current platform
     */
    public int getFirstObservationIndexForCurrentPlatform() {
	final Platform curPlatform = getCommonViewModel().getPlatformForObservation(getObservationNumber());

	for (int index = getObservationNumber() - 1; index >= 0; index--) {
	    final Platform prevPlatform = getCommonViewModel().getPlatformForObservation(index);
	    if (prevPlatform != curPlatform) {
		// Platform changes .... return the previous index
		return index + 1;
	    }
	}

	return 0;
    }

    /**
     * @return the index of the first Observation for the next platform (if exists). If there is no next platform,
     *         return the first one.
     */
    public int getFirstObservationIndexForNextPlatform() {
	final Platform curPlatform = getCommonViewModel().getPlatformForObservation(getObservationNumber());

	final int maxIndex = getMaxObservationsNumber() - 1;
	for (int index = getObservationNumber() + 1; index <= maxIndex; index++) {
	    final Platform nextPlatform = getCommonViewModel().getPlatformForObservation(index);
	    if (nextPlatform != curPlatform) {
		// Platform changes .... return the index
		return index;
	    }
	}

	// If there is no next platform, return the first one.
	return 0;
    }

    /**
     * @return the index of the first Observation for a choosen platform (if exists). If there is no platform, return
     *         the first one.
     */
    public int getFirstObservationIndexForChoosenPlatform(final int platformIndex) {
	Platform choosenPlatform;
	if (platformIndex < getCommonViewModel().getDataset().getPlatforms().size()) {
	    choosenPlatform = getCommonViewModel().getDataset().getPlatforms().get(platformIndex);
	} else {
	    choosenPlatform = getCommonViewModel().getDataset().getPlatforms()
		    .get(getCommonViewModel().getDataset().getPlatforms().size() - 1);
	}

	final int maxIndex = getMaxObservationsNumber() - 1;
	for (int index = getObservationNumber() + 1; index <= maxIndex; index++) {
	    final Platform nextPlatform = getCommonViewModel().getPlatformForObservation(index);
	    if (nextPlatform == choosenPlatform) {
		// Platform changes .... return the index
		return index;
	    }
	}

	// If there is no next platform, return the first one.
	return 0;
    }

    /**
     * @return the index of the first Observation for the current platform (if exists). If already on first observation,
     *         return the previous. If there is no previous platform, return the last one.
     */
    public int getFirstObservationIndexForPreviousPlatform() {
	Platform curPlatform = getCommonViewModel().getPlatformForObservation(getObservationNumber());

	boolean isFirsLoop = true;
	for (int index = getObservationNumber() - 1; index >= -1; index--) {
	    if (index == -1) { // if under 0, go to end of obs
		if (getCommonViewModel().getDataset().getPlatforms().size() > 1) {
		    index = getMaxObservationsNumber() - 1;
		} else {
		    return 0;
		}
	    }

	    final Platform prevPlatform = getCommonViewModel().getPlatformForObservation(index);

	    if (prevPlatform != curPlatform) {
		if (isFirsLoop) { // if first loop, change the current plateform
		    curPlatform = getCommonViewModel().getPlatformForObservation(index);
		} else {
		    // Platform changes .... return the previous index
		    if (index != (getMaxObservationsNumber() - 1)) {
			return index + 1;
		    } else {
			return 0;
		    }
		}
	    }
	    isFirsLoop = false;
	}
	return 0;

    }

    /**
     * @return the index of the last Observation for the current platform
     */
    public int getLastObservationIndexForCurrentPlatform() {
	final Platform curPlatform = getCommonViewModel().getPlatformForObservation(getObservationNumber());

	final int maxIndex = getMaxObservationsNumber() - 1;
	for (int index = getObservationNumber() + 1; index <= maxIndex; index++) {
	    final Platform nextPlatform = getCommonViewModel().getPlatformForObservation(index);
	    if (nextPlatform != curPlatform) {
		// Platform changes .... return the previous index
		return index - 1;
	    }
	}

	if (maxIndex < 0) {
	    // Should never happen
	    return 0;
	}

	return maxIndex;
    }

    /**
     * @return the index of the last Observation for the dataset
     */
    public int getLastObservationIndexForDataset() {
	return getMaxObservationsNumber() - 1;
    }

    /**
     * @return the propertyChangeSupport
     */
    public PropertyChangeSupport getPropertyChangeSupport() {
	return propertyChangeSupport;
    }

    /**
     * @return the SuperposedModeEnum
     */
    public abstract SuperposedModeEnum getSuperposedModeEnum();

    /**
     * Initialise(taille, selection courante) les composant de la vue une fois affichée
     */
    public abstract void initViewAfterShow();

    /**
     * @return TRUE if the Superposed mode is on
     */
    public abstract boolean isSuperposedMode();

    /**
     * Unload data to save memory
     */
    public void prepareForDispose() {
	specificPrepareForDispose();

	if (awtEventListener != null) {
	    Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener);
	}
	awtEventListener = null;

	if (commonViewImpl != null) {
	    commonViewImpl.prepareForDispose();
	    commonViewImpl = null;
	}
	if (commonViewModel != null) {
	    commonViewModel.prepareForDispose();
	    commonViewModel = null;
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans. PropertyChangeEvent)
     */
    @Override
    public void propertyChange(final PropertyChangeEvent arg0) {
	SC3Logger.LOGGER.trace("CommonViewController propertyChange : " + arg0.getSource());
    }

    /**
     * Save A JPEG IMAGE Of the map view near the XML context file
     */
    public abstract void saveMapImage();

    /**
     * Save A JPEG IMAGE Of the map in context
     */
    public abstract void saveMap();

    /**
     * Get the initial latitude, longitude cornres of map
     *
     * @return
     */
    public abstract double[][] getInitialMapBounds();

    /**
     * Create a string of map object in geoJson
     *
     * @return
     */
    public abstract String createGeoJsonString();

    /**
     * Set the new Observation number and send
     *
     * @param observationNum
     */
    public void setObservationNumberAndSend(final int observationNum) {
	final int lastObservationIndex = observationIndex;
	observationIndex = observationNum;
	if (lastObservationIndex != observationNum) {
	    observationNumberChanged(lastObservationIndex, observationNum);
	}
    }

    /**
     * Set the new Observation number
     *
     * @param observationNum
     */
    public void setObservationNumber(final int observationNum) {
	this.observationIndex = observationNum;
    }

    /**
     * Set the selected observation by index
     */
    public void setSelectedObservation(final int observationIndex) {
	final Observation observation = getCommonViewModel().getObservation(observationIndex);
	updateObservationMetadatas(observation, true);

	// reset HashMap originalQCs
	getCommonViewModel().resetOriginalQCs();
    }

    /**
     * Set the selected observation on the specific panel of the view
     */
    public abstract void setSelectedObservationOnSpecificPanel(int observationIndex);

    /**
     * Add global key listener for LEFT and RIGHT arrows
     */
    protected void addKeyListener() {
	// Source :
	// http://www.developpez.net/forums/d266788/java/interfaces-graphiques-java/awt-swing/debutant-keylistener-jframe-ne-repond/#post3707630
	awtEventListener = new AWTEventListener() {
	    /**
	     * Used to know if the e
	     */
	    private long keyPressedStart = -1l;

	    @Override
	    public void eventDispatched(final AWTEvent event) {

		if (event instanceof KeyEvent) {
		    if (!CommonViewController.keyListenerEnabled.booleanValue()) {
			return;
		    }

		    final KeyEvent ke = (KeyEvent) event;
		    if (ke.getID() == KeyEvent.KEY_PRESSED) {
			final long keyPressedNow = System.currentTimeMillis();
			final long keyPressedDiff = keyPressedNow - keyPressedStart;
			switch (ke.getKeyCode()) {
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_KP_LEFT:
			    if (keyPressedStart == -1) {
				// When pressing the key, change the Station ...
				keyPressedStart = keyPressedNow;
				if ((getCommonViewImpl() != null)
					&& (getCommonViewImpl().getMetadataSplitPane() != null)) {
				    getCommonViewImpl().getMetadataSplitPane().selectedPrevObservationWithButton(true);
				}
			    } else {
				// When keeping down the key pressed, change the
				// Station if the delay is passed
				if (keyPressedDiff > keyPressedDelayBeforeChangingStation) {
				    keyPressedStart = keyPressedNow;
				    if ((getCommonViewImpl() != null)
					    && (getCommonViewImpl().getMetadataSplitPane() != null)) {
					getCommonViewImpl().getMetadataSplitPane()
						.selectedPrevObservationWithButton(true);
				    }
				}
			    }
			    break;
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_KP_RIGHT:
			    if (keyPressedStart == -1) {
				// When pressing the key, change the Station ...
				keyPressedStart = keyPressedNow;
				if ((getCommonViewImpl() != null)
					&& (getCommonViewImpl().getMetadataSplitPane() != null)) {
				    getCommonViewImpl().getMetadataSplitPane().selectedNextObservationWithButton(true);
				}
			    } else {
				// When keeping down the key pressed, change the
				// Station if the delay is passed
				if (keyPressedDiff > keyPressedDelayBeforeChangingStation) {
				    keyPressedStart = keyPressedNow;
				    if ((getCommonViewImpl() != null)
					    && (getCommonViewImpl().getMetadataSplitPane() != null)) {
					getCommonViewImpl().getMetadataSplitPane()
						.selectedNextObservationWithButton(true);
				    }
				}
			    }
			    break;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_KP_UP:
			    if (keyPressedStart == -1) {
				// When pressing the key, change the Level ...
				keyPressedStart = keyPressedNow;
				if ((getCommonViewImpl() != null)
					&& (getCommonViewImpl().getMetadataSplitPane() != null)) {
				    selectedPrevLevelWithButton();
				}
			    } else {
				// When keeping down the key pressed, change the
				// Station if the delay is passed
				if (keyPressedDiff > keyPressedDelayBeforeChangingStation) {
				    keyPressedStart = keyPressedNow;
				    if ((getCommonViewImpl() != null)
					    && (getCommonViewImpl().getMetadataSplitPane() != null)) {
					selectedPrevLevelWithButton();
				    }
				}
			    }
			    break;
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_KP_DOWN:
			    if (keyPressedStart == -1) {
				// When pressing the key, change the Level ...
				keyPressedStart = keyPressedNow;
				if ((getCommonViewImpl() != null)
					&& (getCommonViewImpl().getMetadataSplitPane() != null)) {
				    selectedNextLevelWithButton();
				}
			    } else {
				// When keeping down the key pressed, change the
				// Station if the delay is passed
				if (keyPressedDiff > keyPressedDelayBeforeChangingStation) {
				    keyPressedStart = keyPressedNow;
				    if ((getCommonViewImpl() != null)
					    && (getCommonViewImpl().getMetadataSplitPane() != null)) {
					selectedNextLevelWithButton();
				    }
				}
			    }
			    break;
			default:
			    break;
			}
		    } else if (ke.getID() == KeyEvent.KEY_RELEASED) {
			// No more memorize the key pressed time
			keyPressedStart = -1l;
		    }
		} else if (event instanceof MouseEvent) {

		    final MouseEvent me = (MouseEvent) event;
		    if ((me.getID() == MouseEvent.MOUSE_CLICKED) && (event.getSource() instanceof JComponent)) {
			// Give the focus
			((JComponent) event.getSource()).requestFocusInWindow();
		    }

		}
	    }
	};

	Toolkit.getDefaultToolkit().addAWTEventListener(awtEventListener,
		AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * Set the action listener for map ribbon band
     */
    protected void createMapRibbonBandActionListener() {
	if (getCommonViewImpl().getMapZoomRectButton() != null) {
	    getCommonViewImpl().getMapZoomRectButton().addActionListener((final ActionEvent ae) -> {
		if (getCommonViewImpl().getMapZoomRectButton().getActionModel().isSelected()) {
		    AbstractChartScrollPane.setMouseMode(MouseMode.ZOOM);
		} else {
		    AbstractChartScrollPane.setMouseMode(MouseMode.SELECTION);
		}
		getPropertyChangeSupport().firePropertyChange(new MapPropertyChangeEvent(
			getCommonViewImpl().getMapZoomRectButton(), MAP_EVENT_ENUM.ZOOM_RECTANGLE, null,
			getCommonViewImpl().getMapZoomRectButton().getActionModel().isSelected()));
	    });
	}
	if (getCommonViewImpl().getMapZoomInButton() != null) {
	    getCommonViewImpl().getMapZoomInButton()
		    .addActionListener((final ActionEvent ae) -> getPropertyChangeSupport()
			    .firePropertyChange(new MapPropertyChangeEvent(getCommonViewImpl().getMapZoomInButton(),
				    MAP_EVENT_ENUM.ZOOM_IN)));
	}
	if (getCommonViewImpl().getMapZoomOutButton() != null) {
	    getCommonViewImpl().getMapZoomOutButton()
		    .addActionListener((final ActionEvent ae) -> getPropertyChangeSupport()
			    .firePropertyChange(new MapPropertyChangeEvent(getCommonViewImpl().getMapZoomOutButton(),
				    MAP_EVENT_ENUM.ZOOM_OUT)));
	}
	if (

	getCommonViewImpl().getMapZoomInitialButton() != null) {
	    getCommonViewImpl().getMapZoomInitialButton()
		    .addActionListener((final ActionEvent ae) -> getPropertyChangeSupport().firePropertyChange(
			    new MapPropertyChangeEvent(getCommonViewImpl().getMapZoomInitialButton(),
				    MAP_EVENT_ENUM.ZOOM_INITIAL)));
	}
	if (getCommonViewImpl().getMapZoomWorldButton() != null) {
	    getCommonViewImpl().getMapZoomWorldButton()
		    .addActionListener((final ActionEvent ae) -> getPropertyChangeSupport()
			    .firePropertyChange(new MapPropertyChangeEvent(getCommonViewImpl().getMapZoomWorldButton(),
				    MAP_EVENT_ENUM.ZOOM_WORLD)));
	}
	if (getCommonViewImpl().getMapLinkUnlinkButton() != null) {
	    getCommonViewImpl().getMapLinkUnlinkButton()
		    .addActionListener((final ActionEvent ae) -> getPropertyChangeSupport()
			    .firePropertyChange(new MapPropertyChangeEvent(getCommonViewImpl().getMapLinkUnlinkButton(),
				    MAP_EVENT_ENUM.LINK_UNLINK_OBSERVATIONS)));
	}
	if (

	getCommonViewImpl().getMapShowHideLabelButton() != null) {
	    getCommonViewImpl().getMapShowHideLabelButton()
		    .addActionListener((final ActionEvent ae) -> getPropertyChangeSupport().firePropertyChange(
			    new MapPropertyChangeEvent(getCommonViewImpl().getMapShowHideLabelButton(),
				    MAP_EVENT_ENUM.SHOW_HIDE_LABELS)));
	}
    }

    /**
     *
     */
    protected void createUndoRedoRibbonBandActionListener() {
	if (getCommonViewImpl().getUndoButton() != null) {
	    getCommonViewImpl().getUndoButton().addActionListener((final ActionEvent ae) -> {
		getCommonViewImpl().getUndoButton().setEnabled(false);
		getPropertyChangeSupport().firePropertyChange(
			new SC3PropertyChangeEvent(getCommonViewImpl().getUndoButton(), EVENT_ENUM.UNDO));
	    });
	    getCommonViewImpl().getUndoButton().setEnabled(false);
	}

	if (getCommonViewImpl().getUndoAllButton() != null) {
	    getCommonViewImpl().getUndoAllButton().addActionListener((final ActionEvent ae) -> {
		getCommonViewImpl().getUndoAllButton().setEnabled(false);
		getPropertyChangeSupport().firePropertyChange(
			new SC3PropertyChangeEvent(getCommonViewImpl().getUndoAllButton(), EVENT_ENUM.UNDO_ALL));
	    });
	    getCommonViewImpl().getUndoAllButton().setEnabled(false);
	}

	if (getCommonViewImpl().getRedoButton() != null) {
	    getCommonViewImpl().getRedoButton().addActionListener((final ActionEvent ae) -> {
		getCommonViewImpl().getRedoButton().setEnabled(false);
		getPropertyChangeSupport().firePropertyChange(
			new SC3PropertyChangeEvent(getCommonViewImpl().getRedoButton(), EVENT_ENUM.REDO));
	    });
	    getCommonViewImpl().getRedoButton().setEnabled(false);
	}
    }

    public void createValidateRibbonBandActionListener() {
    }

    /**
     * @return the total number of Observations
     */
    public int getMaxObservationsNumber() {
	return commonViewModel.getObservations().size();
    }

    /**
     * @return the actual Observation number
     */
    public int getObservationNumber() {
	return observationIndex;
    }

    /**
     * Method called when the current observation is changed
     *
     * @param oldObservationIndex
     * @param newObservationIndex
     */
    protected void observationNumberChanged(final int oldObservationIndex, final int newObservationIndex) {
    }

    protected void selectedNextLevelWithButton() {
	if (levelIndex < (commonViewModel.getObservation(observationIndex).getReferenceParameter().getValues().size()
		- 1)) {
	    levelIndex++;
	} else {
	    levelIndex = 0;
	}
	setCurrentLevel(levelIndex);
    }

    protected void selectedPrevLevelWithButton() {
	if (levelIndex > 0) {
	    levelIndex--;
	} else {
	    levelIndex = commonViewModel.getObservation(observationIndex).getReferenceParameter().getValues().size()
		    - 1;
	}
	setCurrentLevel(levelIndex);
    }

    /**
     * Update the back and forward navigation buttons
     */
    protected void setBackAndForwardButtons() {
	if (getCommonViewImpl().getMetadataSplitPane() != null) {
	    commonViewImpl.getMetadataSplitPane().updateEnabledButtons();
	}
    }

    /**
     * Set a new level to display
     *
     * @param levelIndex
     */
    protected abstract void setCurrentLevel(int levelIndex);

    /**
     * Set the selected observation by object
     */
    protected void setSelectedObservation(final Object object) {
	Observation observation = null;
	if (object instanceof Observation) {
	    observation = (Observation) object;
	} else if (object instanceof PointModel) {
	    final PointModel pointModel = (PointModel) object;
	    for (final Observation obs : getCommonViewModel().getObservations()) {
		if ((obs.getReference() == null) && (pointModel.getReference() == null)) {
		    SC3Logger.LOGGER.error("References of observation and wished point are equal to null");
		} else if ((obs.getReference() + "_" + pointModel.getLevel() + "_"
			+ obs.getFirstLatitudeClone().getValueAsDouble() + "_"
			+ obs.getFirstLongitudeClone().getValueAsDouble())
				.equals(pointModel.getReference() + "_" + pointModel.getLevel() + "_"
					+ pointModel.getLatitude() + "_" + pointModel.getLongitude())) {
		    observation = obs;
		    setCurrentLevel(pointModel.getLevel());
		    break;
		}
	    }
	}
	final int index = getCommonViewModel().getObservations().indexOf(observation);
	if ((index >= 0) && (getObservationNumber() != index)) {
	    setObservationNumberAndSend(index);
	    updateObservationMetadatas(observation, true);
	    setBackAndForwardButtons();
	}
    }

    /**
     * Unload data to save memory (in specific Controller)
     */
    protected abstract void specificPrepareForDispose();

    /**
     * Update dataset metadata
     */
    public void updateDatasetMetadataTable() {
	if (commonViewImpl.getMetadataSplitPane() != null) {
	    commonViewImpl.getMetadataSplitPane().updateTableWithDataset(commonViewModel.getDataset());
	}
    }

    /**
     * @param addInRedoable
     */
    protected void updateDatatableIfNeeded(final List<? extends UndoRedoAction> undoRedoActions) {
	if (undoRedoActions != null) {
	    boolean updateDatasetMetadataTable = false;
	    boolean updateObsMetadataTable = false;
	    for (final UndoRedoAction undoRedoAction : undoRedoActions) {
		if (undoRedoAction instanceof MetadataValueChange) {
		    if (((MetadataValueChange) undoRedoAction).getObsId() == null) {
			updateDatasetMetadataTable = true;
		    } else {
			updateObsMetadataTable = true;
		    }
		}
	    }
	    if (updateDatasetMetadataTable) {
		updateDatasetMetadataTable();
	    }
	    if (updateObsMetadataTable) {
		updateObservationMetadatas(getCommonViewModel().getObservation(getObservationNumber()), true);
	    }
	}
	getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		getCommonViewModel().isListOfRedoableChangesEmpty());
    }

    /**
     * Update the observation table data
     *
     * @param observation
     */
    public void updateObservationMetadatas(final Observation observation, final boolean updateTableWithObservation) {
	if (commonViewImpl.getMetadataSplitPane() != null) {
	    commonViewImpl.getMetadataSplitPane().updateObservationMetadatas(observation,
		    (InfoInObservationSubPanel) commonViewImpl.getMetadataSplitPane().getInfoInObservationSubPanel(),
		    updateTableWithObservation);
	    commonViewImpl.getMetadataSplitPane().updateObservationMetadatas(observation,
		    (InfoInObservationSubPanel) commonViewImpl.getInfoInObservationSubPanel(),
		    updateTableWithObservation);
	    displayOtherProfilesOfPlatform(observationIndex);
	}
    }

    public void displayOtherProfilesOfPlatform(final int observationIndex) {
    }

    public void setCommonViewModel(final CommonViewModel commonViewModel) {
	this.commonViewModel = commonViewModel;
    }

    public void setCommonViewImpl(final CommonViewImpl commonViewImpl) {
	this.commonViewImpl = commonViewImpl;
    }

    public void updateRibbonBandAfterChangeLanguage() {
	// # 55265 KeepBoundsCheckbox problem
	// if ((getCommonViewImpl() instanceof DataViewImpl)
	// && (((DataViewImpl) getCommonViewImpl()).getKeepBoundsCheckbox() != null)) {
	// final boolean isActivated = ((DataViewImpl) getCommonViewImpl()).getKeepBoundsCheckbox().isSelected();
	// ((DataViewImpl) getCommonViewImpl()).resetKeepBoundsCheckbox();
	// if (isActivated) {
	// ((DataViewImpl) getCommonViewImpl()).getKeepBoundsCheckbox().setSelected(isActivated);
	// }
	// }
	getCommonViewImpl().createRibbonBands();
	if (this instanceof DataViewController) {
	    // add action listeners to the new ribbon band
	    ((DataViewController) this).createRibbonBandActionListener();
	    ((DataViewController) this).createUndoRedoRibbonBandActionListener();
	    ((DataViewController) this).createMapRibbonBandActionListener();
	    ((DataViewController) this).createSelectAndChangeQCBandActionListener();
	    ((DataViewController) this).createChangeGraphsNumberBandActionListener();
	    ((DataViewController) this).createActionListeners();
	} else if (this instanceof MapViewController) { // for QC11 BPC
	    // add action listeners to the new ribbon band
	    this.createUndoRedoRibbonBandActionListener();
	    this.createMapRibbonBandActionListener();
	    this.createValidateRibbonBandActionListener();
	    // update report
	    ReportJDialog.refreshReportJDialog(commonViewImpl.getScoop3Frame(), commonViewImpl, commonViewImpl.report);
	} else if (this instanceof ReferenceViewController) { // for QC21 BPC
	    // add action listeners to the new ribbon band
	    ((ReferenceViewController) this).createRibbonBandActionListener();
	    ((ReferenceViewController) this).createUndoRedoRibbonBandActionListener();
	    ((ReferenceViewController) this).createMapRibbonBandActionListener();
	    ((ReferenceViewController) this).createSelectAndChangeQCBandActionListener();
	    ((ReferenceViewController) this).createValidateRibbonBandActionListener();
	    // update report
	    ReportJDialog.refreshReportJDialog(commonViewImpl.getScoop3Frame(), commonViewImpl, commonViewImpl.report);
	}
    }

    public void updateGraphNamesAfterChangeLanguage() {
	// update graphs name
	if (this instanceof DataViewController) {
	    ((DataViewController) this)
		    .updateChartPanel((getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET)
			    || (getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET));
	} else if (this instanceof MapViewController) { // for QC11 BPC
	    ((MapViewImpl) ((MapViewController) this).getCommonViewImpl()).updateSpeedChart();
	} else if (this instanceof ReferenceViewController) { // for QC21 BPC
	    ((ReferenceViewController) this)
		    .setScoop3ChartPanelPopupMenu(new Scoop3RefChartPanelPopupMenu(((ReferenceViewController) this)));
	    ((ReferenceViewController) this).updateChartPanelWithRefresh(false);
	}
    }
}
