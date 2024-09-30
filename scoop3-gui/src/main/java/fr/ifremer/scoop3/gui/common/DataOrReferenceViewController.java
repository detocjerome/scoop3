package fr.ifremer.scoop3.gui.common;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.bushe.swing.event.EventBus;

import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.omGraphics.OMGraphicList;

import fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane;
import fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane.MouseMode;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.core.report.utils.ReportUtils;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataErrorMessageItem;
import fr.ifremer.scoop3.data.SuperposedModeEnum;
import fr.ifremer.scoop3.events.GuiEventDisplayDialog;
import fr.ifremer.scoop3.events.GuiEventMiscEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent.EVENT_ENUM;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyCommentChangeEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyIsCheckedChangeEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyMultipleErrorMessagesChangeEvent;
import fr.ifremer.scoop3.gui.data.ChartPanelWithComboBox;
import fr.ifremer.scoop3.gui.data.DataViewController;
import fr.ifremer.scoop3.gui.data.DataViewImpl;
import fr.ifremer.scoop3.gui.data.commandButton.SelectAndChangeQCCommandButton;
import fr.ifremer.scoop3.gui.utils.MapCommonFunctions;
import fr.ifremer.scoop3.infra.event.MapPropertyChangeEvent;
import fr.ifremer.scoop3.infra.event.MapPropertyChangeEvent.MAP_EVENT_ENUM;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.undo_redo.data.DataValueChange;
import fr.ifremer.scoop3.infra.undo_redo.data.QCValueChange;
import fr.ifremer.scoop3.map.view.Scoop3MapPanel;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.Profile;
import fr.ifremer.scoop3.model.QCValues;

public abstract class DataOrReferenceViewController extends CommonViewController implements PropertyChangeListener {

    private Scoop3MapPanel scoop3MapPanel;

    private PropertyChangeListener scoop3MapPropertyChangeListener;

    private SuperposedModeEnum superposedModeEnum = null;

    /** index of the first observation */
    protected int firstObservationIndex = -1;

    /** index of the last observation */
    protected int lastObservationIndex = -1;

    protected long lastPropertyChangeTime = System.currentTimeMillis();

    protected EVENT_ENUM lastPropertyChangeEnum;

    protected boolean removingMeasure = false;

    protected boolean precisionZoomOn = false;

    protected boolean keepBounds = false;

    protected List<Profile> nearestProfiles = null;

    protected boolean isBPCVersion = false;

    /**
     * @param commonViewModel
     */
    protected DataOrReferenceViewController(final DataViewImpl dataViewImpl, final CommonViewModel dataViewModel,
	    final boolean isBPCVersion) {
	super(dataViewImpl, dataViewModel);
	this.isBPCVersion = isBPCVersion;

	// Affichage des graphiques dépend du type du jeu de donnees
	dataViewImpl.setNbMaxGraphByGroup(dataViewModel.doesDatasetContainProfiles()
		? DataViewImpl.profileGraphsCountGroup : DataViewImpl.timeserieGraphsCountGroup);
	dataViewImpl.setGraphGroupAddGraphSplitType(JSplitPane.HORIZONTAL_SPLIT);
	dataViewImpl.setGraphGroupAddGroupSplitType(JSplitPane.VERTICAL_SPLIT);

	createMapComponent(MapCommonFunctions.createOMListFromDataset(getCommonViewModel().getDataset()));
	setSelectedObservationOnSpecificPanel(0);

	createRibbonBandActionListener();
	createUndoRedoRibbonBandActionListener();
	createMapRibbonBandActionListener();
	createSelectAndChangeQCBandActionListener();

	// Add Property Change Listener
	// duplicata of addPropertyChangeListener
	// getPropertyChangeSupport().addPropertyChangeListener(this);

	if (dataViewImpl.getMetadataSplitPane() != null) {
	    dataViewImpl.getMetadataSplitPane().setDataOrReferenceViewController(this);
	}

	// Send the Property Change Listener in the view
	dataViewImpl.setPropertyChangeSupport(getPropertyChangeSupport());

	addKeyListener();

	updateMouseCursor();
    }

    /**
     * @return the list of the QCValues which can be set
     */
    public QCValues[] getQCValuesSettable() {
	return CommonViewImpl.getQCValuesSettable();
    }

    /**
     * @return the superposedModeEnum
     */
    @Override
    public SuperposedModeEnum getSuperposedModeEnum() {
	return superposedModeEnum;
    }

    @Override
    public void initViewAfterShow() {

	// Init Map view correctly
	getPropertyChangeSupport().firePropertyChange(new MapPropertyChangeEvent(
		(getCommonViewImpl()).getMapZoomInitialButton(), MAP_EVENT_ENUM.ZOOM_INITIAL));

	// Init Graph Correctly
	this.propertyChangeZoomInitial(new SC3PropertyChangeEvent(this, EVENT_ENUM.ZOOM_INITIAL));

    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewController#isSuperposedMode()
     */
    @Override
    public boolean isSuperposedMode() {
	return (firstObservationIndex != lastObservationIndex);
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

	if ((lastPropertyChangeEnum == null) || ((lastPropertyChangeTime != q11Evt.getCurrentTime())
		|| !lastPropertyChangeEnum.equals(q11Evt.getEventEnum()))) {
	    lastPropertyChangeTime = q11Evt.getCurrentTime();
	    lastPropertyChangeEnum = q11Evt.getEventEnum();

	    SC3Logger.LOGGER.trace("DataOrReferenceViewController - propertyChange - " + evt.getPropertyName() + " / "
		    + lastPropertyChangeTime);

	    switch (q11Evt.getEventEnum()) {
	    case ALL_PROFILES:
		propertyChangeAllProfiles();
		((DataViewImpl) getCommonViewImpl()).updateAllProfilesIcons(isSuperposedMode());
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
	    case CHANGE_SUPERPOSED_MODE:
		propertyChangeChangeSuperposedMode(q11Evt);
		break;
	    case DISPLAYED_QC:
		propertyChangeDisplayedQC(q11Evt);
		break;
	    case DISPLAY_CIRCLE_ON_GRAPH:
		propertyChangeDisplayCircleOnGraph();
		break;
	    case DISPLAY_DATA_TABLE:
		propertyChangeDisplayDataTable();
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
		// creating a thread for the JDialog
		final ExecutorService executor = Executors.newSingleThreadExecutor();

		executor.execute(() -> {
		    propertyChangeDisplayStatistics(q11Evt);

		    // Remove the JDialog to inform the user that the current file is editing
		    EventBus.publish(new GuiEventDisplayDialog());

		    // Shutdown the executor
		    executor.shutdown();
		});

		EventBus.publish(new GuiEventDisplayDialog(
			Messages.getMessage("coriolis-gui.waiting-message-load-climatology.title"),
			Messages.getMessage("coriolis-gui.waiting-message-load-climatology.message")));

		// Shutdown the executor
		executor.shutdown();
		break;
	    case DIVIDE_TS:
		propertyChangeChangeDivideTimeserie(q11Evt);
		break;
	    case EDIT_CLIMATO_ADDITIONAL_GRAPHS:
		// creating a thread for the JDialog
		final ExecutorService executor1 = Executors.newSingleThreadExecutor();

		executor1.execute(() -> {
		    propertyChangeEditClimatoAdditionalGraphs(q11Evt);// Remove the JDialog to inform the user that
								      // the current file is editing
		    EventBus.publish(new GuiEventDisplayDialog());

		    // Shutdown the executor
		    executor1.shutdown();
		});

		EventBus.publish(new GuiEventDisplayDialog(
			Messages.getMessage("coriolis-gui.waiting-message-load-climatology.title"),
			Messages.getMessage("coriolis-gui.waiting-message-load-climatology.message")));

		// Shutdown the executor
		executor1.shutdown();
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
		actionWithProgress(q11Evt);
		break;
	    case REMOVE_MEASURE:
		propertyChangeRemoveMeasure(q11Evt);
		break;
	    case RESET_NEAREST_PROFILES:
		this.nearestProfiles = null;
		break;
	    case SHIFT:
		propertyChangeShift();
		break;
	    case UNDO:
		actionWithProgress(q11Evt);
		break;
	    case UNDO_ALL:
		actionWithProgress(q11Evt);
		break;
	    case UPDATE_POSITION_GRAPHS:
		propertyChangeUpdatePositionGraphs();
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
     * Execute action with progress dialog
     *
     * @param q11Evt
     */
    private void actionWithProgress(final SC3PropertyChangeEvent q11Evt) {
	// execute thread to launch progress bar
	final ExecutorService executor = Executors.newSingleThreadExecutor();
	executor.execute(() -> {
	    switch (q11Evt.getEventEnum()) {
	    case REDO:
		propertyChangeRedo();
		break;
	    case UNDO:
		propertyChangeUndo();
		break;
	    case UNDO_ALL:
		propertyChangeUndoAll();
		break;
	    default:
		break;
	    }

	    // Remove the JDialog to inform the user that the Metadata control is in progress
	    EventBus.publish(new GuiEventDisplayDialog());

	    executor.shutdown();
	});
	try {
	    // Add a JDialog to inform the user that the Format is in progress
	    EventBus.publish(new GuiEventDisplayDialog(Messages.getMessage("bpc-controller.action-in-progress-title"),
		    Messages.getMessage("bpc-controller.action-in-progress-message")));
	} catch (final Exception e) {
	    // do nothing
	}
    }

    /**
     * Save a Graph as an image
     *
     * @param chartPanelWithComboBox
     */
    public void saveImage(final ChartPanelWithComboBox chartPanelWithComboBox) {
	final BufferedImage bufferedImage = new BufferedImage(chartPanelWithComboBox.getWidth(),
		chartPanelWithComboBox.getHeight(), BufferedImage.TYPE_INT_ARGB);
	// Paint a JPanel directly in the Buffered Image
	chartPanelWithComboBox.paint(bufferedImage.getGraphics());

	final String firstVar = chartPanelWithComboBox.getScoop3ChartPanel().getVariableNameToUpdate();
	final String secondVar = chartPanelWithComboBox.getSelectedValue()
		.split(DataViewController.SEPARATOR_FOR_COMBO_LABELS)[1].trim();

	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

	String contextDir = ReportUtils.getContextDir();
	contextDir = "".equals(contextDir) ? System.getProperty("user.home") : contextDir;

	final String imageFilename = contextDir + "/graph_"
		+ getCommonViewModel().getObservation(getObservationNumber()).getReference() + "_" + secondVar + "_"
		+ firstVar + "_" + dateFormat.format(new Date()) + ".png";
	final File outputfile = new File(imageFilename);
	try {
	    ImageIO.write(bufferedImage, "png", outputfile);
	} catch (final IOException e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	}

	// Popup pour informer
	JOptionPane.showMessageDialog(getCommonViewImpl().getScoop3Frame(),
		MessageFormat.format(Messages.getMessage("gui.save-graph.dialog"), imageFilename));

	// FAE 29424: J11.15.10 : enregistrement des graphes un par un sans ouverture du dossier
	// // Ouverture d'un explorateur de fichier
	// try {
	// Desktop.getDesktop().open(new File(contextDir));
	// } catch (final IOException e) {
	// e.printStackTrace();
	// }
    }

    /**
     * Save a Graph as an image for Scoop3RefChartPanelPopupMenu
     *
     * @param scoop3ChartPanel
     */
    public void saveImage(final JScoop3ChartScrollPaneAbstract scoop3ChartPanel) {
	final BufferedImage bufferedImage = new BufferedImage(scoop3ChartPanel.getWidth(), scoop3ChartPanel.getHeight(),
		BufferedImage.TYPE_INT_ARGB);
	// Paint a JPanel directly in the Buffered Image
	scoop3ChartPanel.paint(bufferedImage.getGraphics());

	final String firstVar = scoop3ChartPanel.getVariableNameToUpdate();
	final String secondVar = scoop3ChartPanel.getSecondVariableNameToUpdate();

	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

	String contextDir = ReportUtils.getContextDir();
	contextDir = "".equals(contextDir) ? System.getProperty("user.home") : contextDir;

	final String imageFilename = contextDir + "/graph_"
		+ getCommonViewModel().getObservation(getObservationNumber()).getReference() + "_" + secondVar + "_"
		+ firstVar + "_" + dateFormat.format(new Date()) + ".png";
	final File outputfile = new File(imageFilename);
	try {
	    ImageIO.write(bufferedImage, "png", outputfile);
	} catch (final IOException e) {
	    SC3Logger.LOGGER.error(e.getMessage(), e);
	}

	// Popup pour informer
	JOptionPane.showMessageDialog(getCommonViewImpl().getScoop3Frame(),
		MessageFormat.format(Messages.getMessage("gui.save-graph.dialog"), imageFilename));

	// FAE 29424: J11.15.10 : enregistrement des graphes un par un sans ouverture du dossier
	// // Ouverture d'un explorateur de fichier
	// try {
	// Desktop.getDesktop().open(new File(contextDir));
	// } catch (final IOException e) {
	// e.printStackTrace();
	// }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewController#setSelectedObservationOnSpecificPanel(int)
     */
    @Override
    public void setSelectedObservationOnSpecificPanel(final int observationIndex) {
	final Observation observation = getCommonViewModel().getObservation(observationIndex);
	final int levelMax = getCommonViewModel().getObservation(observationIndex).getReferenceParameter().getValues()
		.size();
	if (levelMax <= levelIndex) {
	    levelIndex = 0;
	}
	// Send the event
	getPropertyChangeSupport().firePropertyChange(new MapPropertyChangeEvent(
		MapCommonFunctions.getPointModelForObs(observation, levelIndex,
			getCommonViewModel().getDataset().getDatasetType().toString(), true),
		MAP_EVENT_ENUM.SELECT_OBSERVATION_NAV));

	updateChartPanel((getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET)
		|| (getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET));
    }

    /**
     * Create the component map
     *
     * @return The jpanel containing the map
     */
    private void createMapComponent(final OMGraphicList omList) {

	scoop3MapPanel = new Scoop3MapPanel(new PropertyHandler(new Properties()), true, 100, 100);
	final JPanel mapPanel = scoop3MapPanel.init(omList, false);
	scoop3MapPanel.setMapBackupDirectory(getCommonViewImpl().getScoop3Frame(), ReportUtils.getContextDir());

	scoop3MapPropertyChangeListener = new PropertyChangeListener() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("SELECTED_OBSERVATION_MAP")) {
		    SC3Logger.LOGGER.trace(
			    "DataOrReferenceViewController - createMapComponent - propertyChangeEvent : SELECTED_OBSERVATION_MAP");
		    final int oldObservationNumber = getObservationNumber();
		    setSelectedObservation(evt.getNewValue());
		    if (oldObservationNumber != getObservationNumber()) {
			setSelectedObservationOnSpecificPanel(getObservationNumber());
		    }
		}
		if (evt.getPropertyName().equals("TOGGLE_FULL_SCREEN_FOR_MAP")) {
		    SC3Logger.LOGGER.trace(
			    "DataOrReferenceViewController - createMapComponent - propertyChangeEvent : TOGGLE_FULL_SCREEN_FOR_MAP");
		    final boolean setFullScreen = ((DataViewImpl) getCommonViewImpl()).toggleFullScreenForMap();
		    scoop3MapPanel.setToggleFullScreen();
		}
		if (evt.getPropertyName().equals("CHANGE_QC_ON_MAP")) {
		    SC3Logger.LOGGER.trace(
			    "DataOrReferenceViewController - createMapComponent - propertyChangeEvent : CHANGE_QC_ON_MAP");
		    // Source is a List<String[Reference, Level]>
		    List<String[]> source = new ArrayList<String[]>();
		    if (superposedModeEnum == SuperposedModeEnum.CURRENT_OBSERVATION_ONLY) {
			for (int index = 0; index < ((List<String[]>) evt.getNewValue()).size(); index++) {
			    if (Dataset.getInstance().getObservations().get(getObservationNumber()).getReference()
				    .equals(((List<String[]>) evt.getNewValue()).get(index)[0])) {
				source.add(((List<String[]>) evt.getNewValue()).get(index));
			    }
			}
		    } else {
			source = (List<String[]>) evt.getNewValue();
		    }
		    changeQCOnMap(source);
		}
		if (evt.getPropertyName().equals("CHANGE_ZOOM_RECT_MODE")) {
		    // select/unselect zoom rect button if exist (exist not in Coriolis)
		    if (getCommonViewImpl().getMapZoomRectButton() != null) {
			getCommonViewImpl().getMapZoomRectButton().getActionModel()
				.setSelected((boolean) evt.getNewValue());
		    }

		    // change Mouse mode
		    if ((boolean) evt.getNewValue()) {
			AbstractChartScrollPane.setMouseMode(MouseMode.ZOOM);
		    }
		    ((DataViewImpl) getCommonViewImpl()).updateMouseModeIcons();
		    updateMouseCursor();
		}
	    }
	};

	scoop3MapPanel.addPropertyChangeListener(scoop3MapPropertyChangeListener);

	getPropertyChangeSupport().addPropertyChangeListener(scoop3MapPanel);

	getCommonViewImpl().setMapPanel(mapPanel);
    }

    /**
     * @param source
     *            is a List<String[Reference, Level]>
     */
    protected void changeQCOnMap(final List<String[]> source) {
	// empty method
    }

    /**
     *
     */
    protected void createRibbonBandActionListener() {
	if (((DataViewImpl) getCommonViewImpl()).getSaveAllGraphsButton() != null) {
	    ((DataViewImpl) getCommonViewImpl()).getSaveAllGraphsButton().addActionListener(
		    (final ActionEvent ae) -> EventBus.publish(new GuiEventMiscEvent("saveAllGraphs")));
	}
	if (((DataViewImpl) getCommonViewImpl()).getAllProfilesButton() != null) {
	    ((DataViewImpl) getCommonViewImpl()).getAllProfilesButton()
		    .addActionListener((final ActionEvent ae) -> getPropertyChangeSupport().firePropertyChange(
			    new SC3PropertyChangeEvent(((DataViewImpl) getCommonViewImpl()).getAllProfilesButton(),
				    EVENT_ENUM.ALL_PROFILES)));
	    ((DataViewImpl) getCommonViewImpl()).getAllProfilesButton()
		    .setEnabled(getCommonViewModel().getObservations().size() > 1);
	}

	((DataViewImpl) getCommonViewImpl()).getDisplayLineButton()
		.addActionListener((final ActionEvent ae) -> getPropertyChangeSupport().firePropertyChange(
			new SC3PropertyChangeEvent(((DataViewImpl) getCommonViewImpl()).getDisplayLineButton(),
				EVENT_ENUM.DISPLAY_LINE_ON_GRAPH)));

	((DataViewImpl) getCommonViewImpl()).getDisplayPointsButton()
		.addActionListener((final ActionEvent ae) -> getPropertyChangeSupport().firePropertyChange(
			new SC3PropertyChangeEvent(((DataViewImpl) getCommonViewImpl()).getDisplayPointsButton(),
				EVENT_ENUM.DISPLAY_POINTS_ON_GRAPH)));

	((DataViewImpl) getCommonViewImpl()).getDisplayPointsAndCircleButton()
		.addActionListener((final ActionEvent ae) -> getPropertyChangeSupport()
			.firePropertyChange(new SC3PropertyChangeEvent(
				((DataViewImpl) getCommonViewImpl()).getDisplayPointsAndCircleButton(),
				EVENT_ENUM.DISPLAY_CIRCLE_ON_GRAPH)));

	if (((DataViewImpl) getCommonViewImpl()).getZoomInButton() != null) {
	    ((DataViewImpl) getCommonViewImpl()).getZoomInButton()
		    .addActionListener((final ActionEvent ae) -> getPropertyChangeSupport().firePropertyChange(
			    new SC3PropertyChangeEvent(((DataViewImpl) getCommonViewImpl()).getZoomInButton(),
				    EVENT_ENUM.ZOOM_IN)));
	}

	if (((DataViewImpl) getCommonViewImpl()).getZoomInitialButton() != null) {
	    ((DataViewImpl) getCommonViewImpl()).getZoomInitialButton()
		    .addActionListener((final ActionEvent ae) -> getPropertyChangeSupport().firePropertyChange(
			    new SC3PropertyChangeEvent(((DataViewImpl) getCommonViewImpl()).getZoomInitialButton(),
				    EVENT_ENUM.ZOOM_INITIAL)));
	}

	if (((DataViewImpl) getCommonViewImpl()).getZoomOutButton() != null) {
	    ((DataViewImpl) getCommonViewImpl()).getZoomOutButton()
		    .addActionListener((final ActionEvent ae) -> getPropertyChangeSupport().firePropertyChange(
			    new SC3PropertyChangeEvent(((DataViewImpl) getCommonViewImpl()).getZoomOutButton(),
				    EVENT_ENUM.ZOOM_OUT)));
	}

	((DataViewImpl) getCommonViewImpl()).getMouseModeSelectionButton().addActionListener((final ActionEvent ae) -> {
	    AbstractChartScrollPane.setMouseMode(MouseMode.SELECTION);
	    propertyChangeMouseModeChanged();
	    // precisionZoomOn = false;
	});

	((DataViewImpl) getCommonViewImpl()).getMouseModeZoomButton().addActionListener((final ActionEvent ae) -> {
	    if (AbstractChartScrollPane.getMouseMode() != MouseMode.ZOOM) {
		AbstractChartScrollPane.setMouseMode(MouseMode.ZOOM);
		propertyChangeMouseModeChanged();
	    }
	    // precisionZoomOn = false;
	});

	// ((DataViewImpl) getCommonViewImpl()).getMouseModePrecisionZoomButton().addActionListener(new ActionListener()
	// {
	// @Override
	// public void actionPerformed(final ActionEvent ae) {
	// if (AbstractChartScrollPane.getMouseMode() != MouseMode.ZOOM_WITH_PRECISION) {
	// AbstractChartScrollPane.setMouseMode(MouseMode.ZOOM_WITH_PRECISION);
	// propertyChange_MouseModeChanged();
	// }
	// precisionZoomOn = true;
	// }
	// });

	((DataViewImpl) getCommonViewImpl()).getDataTableButton()
		.addActionListener((final ActionEvent ae) -> getPropertyChangeSupport().firePropertyChange(
			new SC3PropertyChangeEvent(((DataViewImpl) getCommonViewImpl()).getDataTableButton(),
				EVENT_ENUM.DISPLAY_DATA_TABLE)));
    }

    protected void createSelectAndChangeQCBandActionListener() {
	if (((DataViewImpl) getCommonViewImpl()).getSelectAndChangeQCs() != null) {
	    for (final SelectAndChangeQCCommandButton selectAndChangeQC : ((DataViewImpl) getCommonViewImpl())
		    .getSelectAndChangeQCs()) {
		selectAndChangeQC.getSelectAndChange().addActionListener((final ActionEvent e) -> {
		    if ((AbstractChartScrollPane.getMouseMode() != MouseMode.OTHER)
			    || (((DataViewImpl) getCommonViewImpl()).getMouseSubModeQC() != selectAndChangeQC
				    .getQc())) {
			((DataViewImpl) getCommonViewImpl()).setMouseSubModeQC(selectAndChangeQC.getQc());
			AbstractChartScrollPane.setMouseMode(MouseMode.OTHER);
			propertyChangeMouseModeChanged();
		    }
		});
	    }
	}
    }

    /**
     * @return the scoop3MapPanel
     */
    public Scoop3MapPanel getScoop3MapPanel() {
	return scoop3MapPanel;
    }

    /**
     * Method called when the propertyChange receives the parameter ALL_PROFILES
     */
    protected abstract void propertyChangeAllProfiles();

    /**
     * Method called when the propertyChange receives the parameter CHANGE_AXIS_MIN_MAX
     *
     * @param q11Evt
     */
    protected abstract void propertyChangeChangeAxisMinMax(final SC3PropertyChangeEvent q11Evt);

    /**
     * Method called when the propertyChange receives the parameter CHANGE_COMMENT
     *
     * @param q11Evt
     */
    protected void propertyChangeChangeComment(final SC3PropertyChangeEvent q11Evt) {
	if (q11Evt instanceof SC3PropertyCommentChangeEvent) {
	    final SC3PropertyCommentChangeEvent sc3PropertyCommentChangeEvent = (SC3PropertyCommentChangeEvent) q11Evt;

	    int observationIndex = -1;
	    int index = 0;
	    for (final Observation observation : getCommonViewModel().getObservations()) {
		if (observation.getId().equals(sc3PropertyCommentChangeEvent.getObsRef())) {
		    observationIndex = index;
		}
		index++;
	    }

	    final DataValueChange dataValueChange = new DataValueChange(observationIndex,
		    sc3PropertyCommentChangeEvent.getRefLevel(), sc3PropertyCommentChangeEvent.getObsRef(),
		    sc3PropertyCommentChangeEvent.getVariableName(), sc3PropertyCommentChangeEvent.variableValue(),
		    sc3PropertyCommentChangeEvent.variableValueStr(), sc3PropertyCommentChangeEvent.getRefValStr(),
		    sc3PropertyCommentChangeEvent.getErrorMessage());
	    dataValueChange.setComment(sc3PropertyCommentChangeEvent.getNewValue());
	    dataValueChange.setCommentOldValue(sc3PropertyCommentChangeEvent.getOldValue());

	    final List<QCValueChange> changed = new ArrayList<>();
	    changed.add(dataValueChange);
	    getCommonViewModel().updateQCs(changed);

	    getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		    getCommonViewModel().isListOfRedoableChangesEmpty());
	}
    }

    /**
     * Method called when the propertyChange receives the parameter CHANGE_IS_CHECKED
     *
     * @param q11Evt
     */
    protected void propertyChangeChangeIsChecked(final SC3PropertyChangeEvent q11Evt) {
	if (q11Evt instanceof SC3PropertyIsCheckedChangeEvent) {
	    final SC3PropertyIsCheckedChangeEvent sc3PropertyIsCheckedChangeEvent = (SC3PropertyIsCheckedChangeEvent) q11Evt;

	    int observationIndex = -1;
	    int index = 0;
	    for (final Observation observation : getCommonViewModel().getObservations()) {
		if (observation.getId().equals(sc3PropertyIsCheckedChangeEvent.getObsRef())) {
		    observationIndex = index;
		}
		index++;
	    }

	    final DataValueChange dataValueChange = new DataValueChange(observationIndex,
		    sc3PropertyIsCheckedChangeEvent.getRefLevel(), sc3PropertyIsCheckedChangeEvent.getObsRef(),
		    sc3PropertyIsCheckedChangeEvent.getVariableName(),
		    sc3PropertyIsCheckedChangeEvent.getVariableValue(),
		    sc3PropertyIsCheckedChangeEvent.getVariableValueStr(),
		    sc3PropertyIsCheckedChangeEvent.getRefValStr(), sc3PropertyIsCheckedChangeEvent.getErrorMessage());
	    dataValueChange.setIsErrorChecked(sc3PropertyIsCheckedChangeEvent.getNewValue());
	    dataValueChange.setIsErrorCheckedOldValue(sc3PropertyIsCheckedChangeEvent.getOldValue());

	    final List<QCValueChange> changed = new ArrayList<>();
	    changed.add(dataValueChange);
	    getCommonViewModel().updateQCs(changed);

	    getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		    getCommonViewModel().isListOfRedoableChangesEmpty());
	}
    }

    /**
     * Method called when the propertyChange receives the parameter CHANGE_METADATA
     *
     * @param q11Evt
     */
    protected abstract void propertyChangeChangeMetadata(final SC3PropertyChangeEvent q11Evt);

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
		if (sc3PropertyMultipleErrorMessagesChangeEvent.getErrorMessagesToUpdate()
			.get(msgIndex) instanceof CADataErrorMessageItem) {
		    final CADataErrorMessageItem caDataErrorMessageItem = (CADataErrorMessageItem) sc3PropertyMultipleErrorMessagesChangeEvent
			    .getErrorMessagesToUpdate().get(msgIndex);

		    // Find corresponding observation
		    int observationIndex = -1;
		    int index = 0;
		    for (final Observation observation : getCommonViewModel().getObservations()) {
			if (observation.getId().equals(caDataErrorMessageItem.getObs1Id())) {
			    observationIndex = index;
			}
			index++;
		    }

		    final DataValueChange dataValueChange;
		    if (sc3PropertyMultipleErrorMessagesChangeEvent.getQcToSet() != null) {
			dataValueChange = new DataValueChange(observationIndex,
				caDataErrorMessageItem.getReferenceIndex(),
				(caDataErrorMessageItem.getFlagManual() == null) ? -1
					: caDataErrorMessageItem.getFlagManual().getQCValue(),
				sc3PropertyMultipleErrorMessagesChangeEvent.getQcToSet().getQCValue(),
				caDataErrorMessageItem.getObs1Id(), caDataErrorMessageItem.getParamCode(),
				caDataErrorMessageItem.getParamValue(), caDataErrorMessageItem.getParamValueStr(),
				caDataErrorMessageItem.getReferenceValue());
			dataValueChange.setErrorMessage(caDataErrorMessageItem.getDetails());
		    } else {
			dataValueChange = new DataValueChange(observationIndex,
				caDataErrorMessageItem.getReferenceIndex(), caDataErrorMessageItem.getObs1Id(),
				caDataErrorMessageItem.getParamCode(), caDataErrorMessageItem.getParamValue(),
				caDataErrorMessageItem.getParamValueStr(), caDataErrorMessageItem.getReferenceValue(),
				caDataErrorMessageItem.getDetails());
		    }

		    dataValueChange.setIsErrorChecked(sc3PropertyMultipleErrorMessagesChangeEvent.isChecked());
		    dataValueChange.setIsErrorCheckedOldValue(caDataErrorMessageItem.isErrorChecked());

		    dataValueChange.setComment(sc3PropertyMultipleErrorMessagesChangeEvent.getCommentToSet());
		    dataValueChange.setCommentOldValue(caDataErrorMessageItem.getComment());

		    changed.add(dataValueChange);
		}
	    }

	    getCommonViewModel().updateQCs(changed);

	    // Update graphs ...
	    propertyChangeRedoOnGraphs(changed);

	    getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		    getCommonViewModel().isListOfRedoableChangesEmpty());
	}
    }

    /**
     * Method called when the propertyChange receives the parameter CHANGE_QC
     *
     * @param q11Evt
     */
    protected abstract void propertyChangeChangeQC(final SC3PropertyChangeEvent q11Evt);

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_ONLY_QC
     *
     * @param q11Evt
     */
    protected void propertyChangeDisplayOnlyQCOnGraph(final SC3PropertyChangeEvent q11Evt) {
    }

    /**
     * Method called when the propertyChange receives the parameter EXCLUDE_ONLY_QC
     *
     * @param q11Evt
     */
    protected void propertyChangeExcludeOnlyQCOnGraph(final SC3PropertyChangeEvent q11Evt) {
    }

    /**
     * Method called when the propertyChange receives the parameter DISPLAYED_QC
     *
     * @param q11Evt
     */
    protected abstract void propertyChangeDisplayedQC(final SC3PropertyChangeEvent q11Evt);

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_DATA_TABLE
     */
    protected abstract void propertyChangeDisplayDataTable();

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_POINTS
     */
    protected abstract void propertyChangeDisplayPointsOnGraph();

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_POINTS_AND_CIRCLE
     */
    protected abstract void propertyChangeDisplayCircleOnGraph();

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_STATION_TYPE
     */
    protected abstract void propertyChangeDisplayStationType(final SC3PropertyChangeEvent q11Evt);

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_STATISTICS
     *
     * @param q11Evt
     */
    protected abstract void propertyChangeDisplayStatistics(final SC3PropertyChangeEvent q11Evt);

    /**
     * Method called when the propertyChange receives the parameter DIVIDE_TS
     *
     * @param q11Evt
     */
    protected abstract void propertyChangeChangeDivideTimeserie(final SC3PropertyChangeEvent q11Evt);

    /**
     * Method called when the propertyChange receives the parameter EDIT_CLIMATO_ADDITIONAL_GRAPHS
     *
     * @param q11Evt
     */
    protected abstract void propertyChangeEditClimatoAdditionalGraphs(final SC3PropertyChangeEvent q11Evt);

    /**
     * Method called when the propertyChange receives the parameter DISPLAY_LINE_ON_GRAPH
     */
    protected abstract void propertyChangeDisplayLineOnGraph();

    /**
     * Method called when the propertyChange receives the parameter KEEP_BOUNDS
     */
    protected abstract void propertyChangeKeepBounds(final SC3PropertyChangeEvent q11Evt);

    /**
     * Method called when the propertyChange receives the parameter MOUSE_MODE_CHANGED
     */
    protected void propertyChangeMouseModeChanged() {
	((DataViewImpl) getCommonViewImpl()).updateMouseModeIcons();
	updateMouseCursor();
	removeSelectionBox();

	// Mode zoom on map
	boolean isZoomModeEnabled = false;
	if (AbstractChartScrollPane.getMouseMode() == MouseMode.ZOOM) {
	    isZoomModeEnabled = true;
	} else {
	    isZoomModeEnabled = false;
	}
	final MapPropertyChangeEvent zoomMapEvent = new MapPropertyChangeEvent(
		((DataViewImpl) getCommonViewImpl()).getMouseModeZoomButton(), MAP_EVENT_ENUM.ZOOM_RECTANGLE, null,
		isZoomModeEnabled);
	getPropertyChangeSupport().firePropertyChange(zoomMapEvent);
    }

    /**
     * Method called when the propertyChange receives the parameter REDO
     */
    protected abstract void propertyChangeRedo();

    /**
     * @param qcsChanged
     */
    protected abstract void propertyChangeRedoOnGraphs(List<QCValueChange> qcsChanged);

    /**
     * Method called when the propertyChange receives the parameter REMOVE_MEASURE
     *
     * @param q11Evt
     */
    protected abstract void propertyChangeRemoveMeasure(final SC3PropertyChangeEvent q11Evt);

    /**
     * Method called when the propertyChange receives the parameter SHIFT
     */
    protected abstract void propertyChangeShift();

    /**
     * Method called when the propertyChange receives the parameter UNDO
     */
    protected abstract void propertyChangeUndo();

    /**
     * Method called when the propertyChange receives the parameter UNDO_ALL
     */
    protected abstract void propertyChangeUndoAll();

    /**
     * Method called when the propertyChange receives the parameter UPDATE_POSITION_GRAPHS
     */
    protected abstract void propertyChangeUpdatePositionGraphs();

    /**
     * Method called when the propertyChange receives the parameter VALIDATE
     */
    protected abstract void propertyChangeValidate();

    /**
     * Method called when the propertyChange receives the parameter ZOOM_IN
     *
     * @param q11Evt
     */
    protected abstract void propertyChangeZoomIn(final SC3PropertyChangeEvent q11Evt);

    /**
     * Method called when the propertyChange receives the parameter ZOOM_INITIAL
     *
     * @param q11Evt
     */
    protected abstract void propertyChangeZoomInitial(final SC3PropertyChangeEvent q11Evt);

    /**
     * Method called when the propertyChange receives the parameter ZOOM_OUT
     *
     * @param q11Evt
     */
    protected abstract void propertyChangeZoomOut(final SC3PropertyChangeEvent q11Evt);

    /**
     * Remove the selction box if needed
     */
    public abstract void removeSelectionBox();

    /**
     * @param superposedModeEnum
     *            the superposedModeEnum to set
     */
    protected void setSuperposedModeEnum(final SuperposedModeEnum superposedModeEnum) {
	this.superposedModeEnum = superposedModeEnum;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewController#specificPrepareForDispose()
     */
    @Override
    protected void specificPrepareForDispose() {
	getPropertyChangeSupport().removePropertyChangeListener(this);
	if (((DataViewImpl) getCommonViewImpl()).getAllProfilesButton() != null) {
	    ((DataViewImpl) getCommonViewImpl()).getAllProfilesButton().removeAll();
	}

	getPropertyChangeSupport().removePropertyChangeListener(scoop3MapPanel);
	scoop3MapPanel.removePropertyChangeListener(scoop3MapPropertyChangeListener);
	scoop3MapPanel.clean();
	scoop3MapPanel = null;
	scoop3MapPropertyChangeListener = null;
    }

    /**
     * Update the Chart Panel
     */
    protected abstract void updateChartPanel(final boolean chartsWithAllParameters);

    /**
     * Update the Mouse Cursor
     *
     * (FAE 26307 : 15. Le design du curseur sur les graphes doit représenter l'action qui sera effectuée sur une
     * sélection rectangulaire (loupe pour zoom et drapeau pour changement de QC))
     */
    protected void updateMouseCursor() {
	final Toolkit toolkit = Toolkit.getDefaultToolkit();
	Cursor c;
	if ((AbstractChartScrollPane.getMouseMode() == MouseMode.ZOOM)
	/* || (AbstractChartScrollPane.getMouseMode() == MouseMode.ZOOM_WITH_PRECISION) */) {
	    // /!\ Use GIF instead of PNG
	    final ImageIcon imgIcon = new ImageIcon(getClass().getClassLoader().getResource("icons/cursor_zoom.gif"));
	    c = toolkit.createCustomCursor(imgIcon.getImage(), new Point(0, 0), "cursor_zoom");
	} else if (AbstractChartScrollPane.getMouseMode() == MouseMode.SELECTION) {
	    c = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	} else {
	    // /!\ Use GIF instead of PNG
	    final String qcFlag = Integer.toString(((DataViewImpl) getCommonViewImpl()).getMouseSubModeQC());
	    final ImageIcon imgIcon = new ImageIcon(getClass().getClassLoader()
		    .getResource("icons/" + (isBPCVersion() ? "bpc_" : "") + "cursor_flag_" + qcFlag + ".gif"));
	    c = toolkit.createCustomCursor(imgIcon.getImage(), new Point(0, 0), "cursor_flag");
	}

	updateMouseCursorForGraphs(c);
    }

    /**
     * @param newCursor
     *            the cursor to use on graphs
     */
    protected abstract void updateMouseCursorForGraphs(Cursor newCursor);

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

    public abstract void validateUpdates();

    public abstract void cancelUpdates();

    public Profile[] getTempProfilesArray() {
	return null;
    }

    public void updateChartPanelWithComboBox(final ChartPanelWithComboBox chartPanelWithComboBox, final boolean a,
	    final boolean b, final List<Profile> profileList) {
    }

    public ArrayList<ChartPanelWithComboBox> getListGraphs() {
	return null;
    }

    public boolean isRemovingMeasure() {
	return removingMeasure;
    }

    public void setRemovingMeasure(final boolean b) {
	this.removingMeasure = b;
    }

    public void updateValidationCounts(final boolean b) {
	// nothing to do
    }

    protected abstract void propertyChangeChangeSuperposedMode(final SC3PropertyChangeEvent q11Evt);

    public boolean getPrecisionZoomOn() {
	return this.precisionZoomOn;
    }

    public boolean getKeepBounds() {
	return this.keepBounds;
    }

    public boolean isBPCVersion() {
	return isBPCVersion;
    }
}
