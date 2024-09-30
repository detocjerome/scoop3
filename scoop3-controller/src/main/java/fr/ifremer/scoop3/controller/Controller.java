package fr.ifremer.scoop3.controller;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;

import fr.ifremer.scoop3.controller.startStep.StartStepAbstract;
import fr.ifremer.scoop3.controller.worflow.StepCode;
import fr.ifremer.scoop3.controller.worflow.SubStep;
import fr.ifremer.scoop3.core.report.validation.model.messages.ComputedParameterMessageItem;
import fr.ifremer.scoop3.core.validateParam.ValidatedDataParameterManager;
import fr.ifremer.scoop3.events.GuiEvent;
import fr.ifremer.scoop3.events.GuiEventBackupFileAndReport;
import fr.ifremer.scoop3.events.GuiEventBackupIsComplete;
import fr.ifremer.scoop3.events.GuiEventChangeMainPanelToStep;
import fr.ifremer.scoop3.events.GuiEventConfigManager;
import fr.ifremer.scoop3.events.GuiEventDisplayDialog;
import fr.ifremer.scoop3.events.GuiEventMiscEvent;
import fr.ifremer.scoop3.events.GuiEventStartStep;
import fr.ifremer.scoop3.events.GuiEventStepCompleted;
import fr.ifremer.scoop3.events.GuiEventUpdateFileChooseName;
import fr.ifremer.scoop3.events.GuiEventUpdateFrameTitle;
import fr.ifremer.scoop3.gui.common.CommonViewController;
import fr.ifremer.scoop3.gui.common.MetadataSplitPane.InfoInObservationSubPanel;
import fr.ifremer.scoop3.gui.common.MetadataTable;
import fr.ifremer.scoop3.gui.core.Scoop3Frame;
import fr.ifremer.scoop3.gui.data.DataViewController;
import fr.ifremer.scoop3.gui.data.DataViewImpl;
import fr.ifremer.scoop3.gui.data.DataViewModel;
import fr.ifremer.scoop3.gui.home.HomeControllerException;
import fr.ifremer.scoop3.gui.home.HomeViewController;
import fr.ifremer.scoop3.gui.map.MapViewController;
import fr.ifremer.scoop3.gui.map.MapViewImpl;
import fr.ifremer.scoop3.gui.map.MapViewModel;
import fr.ifremer.scoop3.gui.reference.ReferenceViewController;
import fr.ifremer.scoop3.gui.reference.ReferenceViewImpl;
import fr.ifremer.scoop3.gui.reference.ReferenceViewModel;
import fr.ifremer.scoop3.gui.utils.Dialogs;
import fr.ifremer.scoop3.gui.utils.JDialogWithCounter;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.io.WriterManager;
import fr.ifremer.scoop3.io.datasetCache.DatasetCache;
import fr.ifremer.scoop3.io.datasetCache.DatasetCacheException;
import fr.ifremer.scoop3.io.driver.DriverException;
import fr.ifremer.scoop3.io.driver.DriverManager;
import fr.ifremer.scoop3.io.driver.IDriver;
import fr.ifremer.scoop3.io.impl.AbstractDataBaseManager;
import fr.ifremer.scoop3.io.impl.AbstractDataManager;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Dataset.CorruptionType;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.Parameter;
import fr.ifremer.scoop3.model.parameter.ParametersRelationships;
import fr.ifremer.scoop3.model.valueAndQc.ValueAndQC;

/**
 * abstract controller class, must be implemented for Coriolis and BPC
 */
public abstract class Controller implements EventSubscriber<GuiEvent> {

    /**
     * The date of the last DisplayDialogEvent
     */
    private Long lastDisplayDialogEvent = Long.MIN_VALUE;

    /*
     * Manager utiliser pour charge les donnnees referencee par l'URI
     */
    AbstractDataManager dataManager = null;

    /**
     * Driver Manager
     */
    private DriverManager driverManager;

    /**
     * Reference on the Dataset
     */
    protected Dataset dataset = null;

    /**
     * JDialog to inform the user
     */
    private JDialogWithCounter dialog;

    /**
     * Reference on the Q11 view controller (could be Metadata, Reference or Data)
     */
    protected CommonViewController commonViewController = null;

    /**
     * Scoop can use an ehCache cache for decoonected mode, snapshot mode, improved performance
     */
    protected DatasetCache datasetCache;

    /**
     * Scoop3 Frame
     */
    protected Scoop3Frame frame = getNewScoop3Frame();

    /**
     * Home view controller
     */
    protected final HomeViewController homeViewController;

    // boolean at true when there is no data in the database for the request asked
    public boolean emptyDatasetWithThisQuery = false;

    // boolean at true when the dataViewController is created and the frame to display datas is ready (no need to
    // refresh before that)
    public boolean readyToRefresh = false;

    /**
     * Default constructor
     */
    protected Controller() {
	createAndInitDriverManager();

	homeViewController = getNewHomeViewController();
	homeViewController.getView().updateCarouselWithDrivers(homeViewController.getDriverManager().getDrivers());

	// subscribe to events published by the Gui
	EventBus.subscribe(GuiEvent.class, this);
    }

    /**
     * Go directly to the given step
     *
     * @param event
     * @return
     */
    public void changeMainPanelToStep(final GuiEventChangeMainPanelToStep event) {
	// on verifie que la Step implique un changement de contenu du mainPanel
	if ((event.getStep() == StepCode.START) || (event.getStep() == StepCode.QC11)
		|| (event.getStep() == StepCode.QC21)) {
	    ExecutorService executor;

	    executor = Executors.newSingleThreadExecutor();
	    // This thread is executed just after the thread above (because it's a SingleThreadExecutor and not a
	    // pool of thread)
	    executor.execute(() -> {
		//
		removeAllPanelsAndTasks();
		Component toDisplayComponent = null;
		// Certaines subStep souhaite une frame non affichee
		boolean showFrame = true;

		/*
		 * changeMainPanelStart if ((getDataset() != null) && homeViewController.persistReport()) {
		 * WriterManager.requestWriteFile(getDataset()); }
		 */

		//
		boolean reportIsPersist = false;
		if (getDataset() != null) {
		    reportIsPersist = homeViewController.persistReport();
		}
		// Creation des composants a afficher
		switch (event.getSubStep()) {

		case GOHOME:
		    if ((getFrame().getExtendedState() != Frame.MAXIMIZED_BOTH)) {
			getFrame().setSize(
				new Dimension(FileConfig.getScoop3FileConfig().getInt("application.home.width"),
					FileConfig.getScoop3FileConfig().getInt("application.home.height")));
		    }
		    toDisplayComponent = homeViewController.getView().getDisplayComponent();
		    if (reportIsPersist) {
			WriterManager.requestWriteFile(getDataset());
		    }
		    break;

		case QC21_INITDATASET:
		    toDisplayComponent = homeViewController.getView().getDisplayComponent();
		    showFrame = false;
		    showFrame = true; // To avoid the freeze of the app ...
		    if (reportIsPersist) {
			WriterManager.requestWriteFile(getDataset());
		    }
		    break;

		case QC11_START:
		    if ((getFrame().getExtendedState() != Frame.MAXIMIZED_BOTH)) {
			getFrame().setSize(
				new Dimension(FileConfig.getScoop3FileConfig().getInt("application.data.width"),
					FileConfig.getScoop3FileConfig().getInt("application.data.height")));
		    }
		    commonViewController = createMapViewController();
		    toDisplayComponent = commonViewController.getCommonViewImpl().getDisplayComponent();
		    createBackupCopy();
		    break;

		case QC21_DATA:
		    if ((getFrame().getExtendedState() != Frame.MAXIMIZED_BOTH)) {
			getFrame().setSize(
				new Dimension(FileConfig.getScoop3FileConfig().getInt("application.data.width"),
					FileConfig.getScoop3FileConfig().getInt("application.data.height")));
		    }
		    commonViewController = createDataViewController();

		    // transfers Metadatas of the dataset from the HomeViewModel into the CommonViewModel
		    commonViewController.getCommonViewModel()
			    .transferMetadatas(homeViewController.getModel().getMetadatas());

		    // special case for Scoop3Explorer - get Metadatas of dataset for each platform and refresh
		    // the datasetMetadataPanel
		    if (commonViewController.getCommonViewImpl().getMetadataSplitPane()
			    .getDatasetMetadatasTable() != null) {
			final List<HashMap<String, ValueAndQC>> metadatas = homeViewController.getModel()
				.getMetadatas();
			final String platformCode = commonViewController.getCommonViewModel()
				.getObservation(commonViewController.getObservationNumber()).getSensor().getPlatform()
				.getCode();
			commonViewController.getCommonViewImpl().getMetadataSplitPane().getDatasetMetadatasTable()
				.triggerUpdateTableWithDataset(commonViewController.getCommonViewModel().getDataset(),
					platformCode, metadatas);

			// refresh params and chartDataset to attribute the fair parameters to the observations
			// without reference & platformCode
			((DataViewModel) commonViewController.getCommonViewModel())
				.setParametersOrder(getParametersOrderForObservations());
			((DataViewModel) commonViewController.getCommonViewModel()).fillAllParameterscode();
			((DataViewModel) commonViewController.getCommonViewModel()).convertScoop3ModelToChartModel();
		    }

		    toDisplayComponent = commonViewController.getCommonViewImpl().getDisplayComponent();
		    break;

		case QC21_REFERENCE:
		    if ((getFrame().getExtendedState() != Frame.MAXIMIZED_BOTH)) {
			getFrame().setSize(
				new Dimension(FileConfig.getScoop3FileConfig().getInt("application.data.width"),
					FileConfig.getScoop3FileConfig().getInt("application.data.height")));
		    }
		    commonViewController = createReferenceViewController();
		    toDisplayComponent = commonViewController.getCommonViewImpl().getDisplayComponent();
		    break;
		default:
		    break;
		}

		// Affichage de la fenetre
		frame.getFrame().getContentPane().add(toDisplayComponent);
		if (!frame.getFrame().isVisible() && showFrame) {
		    frame.getFrame().setVisible(true);
		}
		frame.getFrame().validate();
		frame.getFrame().repaint();
		if (commonViewController != null) {
		    commonViewController.initViewAfterShow();
		}

		// Hide Dialog as the view is created
		try {
		    SwingUtilities.invokeAndWait(() -> manageJDialogEvent(new GuiEventDisplayDialog()));
		} catch (InvocationTargetException | InterruptedException e) {
		    // do nothing
		}
	    });
	    // Display Dialog while DataViewController is creating
	    try {
		SwingUtilities.invokeLater(() -> manageJDialogEvent(
			new GuiEventDisplayDialog(Messages.getMessage("bpc-controller.loading-panel-in-progress-title"),
				Messages.getMessage("bpc-controller.loading-panel-in-progress-message"))));
	    } catch (final Exception e) {
		// do nothing
	    }
	    // Shutdown the executor
	    executor.shutdown();

	}

    }

    /**
     * @return the AbstractDataBaseManager
     */
    public abstract AbstractDataBaseManager getDataBaseManager();

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
	return dataset;
    }

    public DatasetCache getDatasetCache() {
	return datasetCache;
    }

    /**
     * @return the frame
     */
    public Scoop3Frame getFrame() {
	return frame;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.bushe.swing.event.EventSubscriber#onEvent(java.lang.Object)
     */
    @Override
    public void onEvent(final GuiEvent event) {
	SC3Logger.LOGGER.debug("reveived event " + event.getGuiEventEnum());

	switch (event.getGuiEventEnum()) {
	case BACKUP_FILE_AND_REPORT:
	    backupFileAndReport((GuiEventBackupFileAndReport) event);
	    break;
	case BACKUP_IS_COMPLETE:
	    backupIsComplete((GuiEventBackupIsComplete) event);
	    break;
	case CHANGE_MAIN_PANEL_TO_STEP:
	    changeMainPanelToStep((GuiEventChangeMainPanelToStep) event);
	    break;
	case CONFIG_MANAGER:
	    launchConfigManager((GuiEventConfigManager) event);
	    break;
	case CREATE_FINAL_PREVIEW:
	    break;
	case CREATE_HTML_REPORT:
	    break;
	case DISPLAY_DIALOG:
	    try {
		SwingUtilities.invokeLater(() -> manageJDialogEvent((GuiEventDisplayDialog) event));
	    } catch (final Exception e) {
		// do nothing
	    }
	    break;
	case MISC_EVENT:
	    manageMiscEvent((GuiEventMiscEvent) event);
	    break;
	case REPLACE_CURRENT_DATASET:
	    // useful only with Scoop3-Argo to replace the current dataset with another one without restarting the
	    // application
	    loadFileOrDirectory(new Properties(), false);
	    break;
	case RESET_DRIVERS:
	    break;
	case RESTORE_BACKUP_FILE_AND_GO_HOME:
	    restoreAndDeleteBackupCopyIfExists();
	    changeMainPanelToStep(new GuiEventChangeMainPanelToStep(StepCode.START, SubStep.GOHOME));
	    break;
	case START_STEP:
	    startStep((GuiEventStartStep) event);
	    break;
	case STEP_COMPLETED:
	    stepCompleted((GuiEventStepCompleted) event);
	    break;
	case TRANSCODE_PARAMETERS:
	    break;
	case UPDATE_BUTTONS_ENABLED:
	    updateButtonsEnabled();
	    break;
	case UPDATE_CONFIGURATION_COMBO:
	    updateConfigurationCombo();
	    break;
	case UPDATE_FRAME_TITLE:
	    updateFrameTitle((GuiEventUpdateFrameTitle) event);
	    break;
	case UPDATE_FILE_CHOOSE_NAME:
	    updateFileChooseName((GuiEventUpdateFileChooseName) event);
	    break;
	default:
	    break;
	}
    }

    /**
     * A parameter has been computed for a given observation.
     *
     * @param observation
     * @param computedParameter
     * @param fathers
     */
    public void parameterComputedForObservation(final Observation observation, final OceanicParameter computedParameter,
	    final ArrayList<Parameter<? extends Number>> fathers) {
	final ComputedParameterMessageItem mess = new ComputedParameterMessageItem(observation, computedParameter,
		fathers);
	if (homeViewController.getReport() != null) {
	    homeViewController.getReport().addComputedParameterMessage(mess);
	}
    }

    /**
     * @throws HomeControllerException
     *
     */
    protected void reset() throws HomeControllerException {

	// Clear the relationships between old parameters
	ParametersRelationships.clearRelations();
	// Clear previous Validated parameters
	ValidatedDataParameterManager.clearValidatedParameters();
	// remove dataset observer
	if ((this.dataset != null) && (this.dataset.countObservers() > 0)) {
	    this.dataset.deleteObservers();
	}

	/*
	 * Set the dataset to null before reading to improve heap size performance ! This is very useful to let the
	 * garbage collector clear the old dataset (if there has been a reading previously) Without this, we will have 2
	 * heavy instance of dataset in memory (one in the bpc controller, and an other one just below from the medatlas
	 * reader manager. The risk is to explode the java heap size !
	 */
	this.dataset = null;
    }

    /**
     * @param dataset
     *            the dataset to set Deprecated : Il faudra refondre loadDataset et l'utiliser
     * @deprecated
     */
    @Deprecated
    public void setDataset(final Dataset dataset) {
	this.dataset = dataset;
	try {
	    if ((this.dataset != null) && (this.dataset.getURI() != null) && (datasetCache != null)
		    && (datasetCache.getDatasetKey(this.dataset.getURI()) == null)) {
		datasetCache.addNewToCache(dataset);
	    }
	} catch (final DatasetCacheException e) {
	    SC3Logger.LOGGER.warn("Cannot add an element to local cache:" + e);
	}
    }

    /**
     * Backup the Dataset and Report. This method needs to be override if needed.
     */
    protected void backupFileAndReport(final GuiEventBackupFileAndReport event) {
    }

    /**
     * Notify that the Backup is complete. This method needs to be override if needed.
     */
    protected void backupIsComplete(final GuiEventBackupIsComplete event) {
    }

    /**
     * Create a backup of the current working copy to restore it if needed. This method needs to be override if needed.
     */
    protected abstract void createBackupCopy();

    /**
     * @return the DataViewController to use. This method can be easily override
     */
    protected abstract DataViewController createDataViewController();

    /**
     * @return the MapViewController to use. This method can be easily override
     */
    protected MapViewController createMapViewController() {
	return new MapViewController(
		new MapViewImpl(frame, getMetadataTable(true), getMetadataTable(true), getDataset(),
			homeViewController.getReport()),
		new MapViewModel(getDataset(), homeViewController.getReport()));
    }

    /**
     * @return the ReferenceViewController to use. This method can be easily override
     */
    protected ReferenceViewController createReferenceViewController() {
	return new ReferenceViewController(
		new ReferenceViewImpl(frame, getMetadataTable(false), getMetadataTable(false), getDataset(),
			homeViewController.getReport()),
		new ReferenceViewModel(getDataset(), homeViewController.getReport(),
			getParametersOrderForObservations()),
		true);
    }

    protected String getApplicationTitle() {
	return FileConfig.getScoop3FileConfig().getString("application.title") + "("
		+ FileConfig.getScoop3FileConfig().getApplicationVersion() + ")" + "user:"
		+ System.getProperty("user.name");
    }

    /**
     * @param globalCellIsEditable
     * @return a MetadataTable
     */
    protected abstract MetadataTable getMetadataTable(boolean globalCellIsEditable);

    /**
     * @return the HomeViewController
     */
    protected abstract HomeViewController getNewHomeViewController();

    /**
     * @return the Scoop3Frame
     */
    protected Scoop3Frame getNewScoop3Frame() {
	final Scoop3Frame newScoop3Frame = new Scoop3Frame();
	newScoop3Frame.setTitle(getApplicationTitle());
	newScoop3Frame.setChangeLanguageActionListener((final ActionEvent e) -> {
	    SC3Logger.LOGGER.info(Messages.getMessage("gui.ribbon-change-language"));
	    changeLanguage();
	});
	final RibbonApplicationMenu applicationMenu = newScoop3Frame.getApplicationMenu();
	newScoop3Frame.getRibbon().setApplicationMenu(applicationMenu);
	return newScoop3Frame;
    }

    /**
     *
     * This method needs to be override.
     *
     * @return the Map which contains the Order of the Parameters (for each Observation).
     */
    protected synchronized Map<String, List<String>> getParametersOrderForObservations() {

	// Select property depending the datasetType
	String propertyName;
	DatasetType datasetType = this.dataset.getDatasetType();
	while (null == datasetType) {
	    datasetType = this.dataset.getDatasetType();
	}
	switch (datasetType) {
	case PROFILE:
	    if (FileConfig.getScoop3FileConfig().getString("gui.local-profile-order-for-graphs").trim().equals("")) {
		propertyName = "gui.profile-order-for-graphs";
		break;
	    } else {
		propertyName = "gui.local-profile-order-for-graphs";
		break;
	    }

	case TIMESERIE:
	    if (FileConfig.getScoop3FileConfig().getString("gui.local-timeserie-order-for-graphs").trim().equals("")) {
		propertyName = "gui.timeserie-order-for-graphs";
		break;
	    } else {
		propertyName = "gui.local-timeserie-order-for-graphs";
		break;
	    }

	case TRAJECTORY:
	    if (FileConfig.getScoop3FileConfig().getString("gui.local-trajectory-order-for-graphs").trim().equals("")) {
		propertyName = "gui.trajectory-order-for-graphs";
		break;
	    } else {
		propertyName = "gui.local-trajectory-order-for-graphs";
		break;
	    }

	default:
	    propertyName = "gui.default-order-for-graphs";
	    break;
	}

	// Get the params order
	final String paramsOrder = FileConfig.getScoop3FileConfig().getString(propertyName);
	final String[] paramsOrderSplitted = paramsOrder == null ? new String[0] : paramsOrder.split(" ");

	final Map<String, List<String>> parametersOrderForObservations = new HashMap<>();
	final List<Observation> tempObservationList = new ArrayList<>(getDataset().getObservations());

	for (final Observation observation : tempObservationList) {
	    final ArrayList<String> parametersOrder = new ArrayList<>();

	    // Add defaultParamOrder
	    for (final String defaultParamOrder : paramsOrderSplitted) {
		if ((observation.getReferenceParameter().getCode().equals(defaultParamOrder)
			|| (observation.getOceanicParameter(defaultParamOrder) != null))
			&& !parametersOrder.contains(defaultParamOrder)) {
		    parametersOrder.add(defaultParamOrder);
		}
	    }

	    // Sort OceanicParameters by alphabetic
	    final List<String> oceanicParameterList = new ArrayList<String>(
		    observation.getOceanicParameters().keySet());
	    // remove null element if present in oceanicParameterList
	    for (int index = oceanicParameterList.size() - 1; index >= 0; index--) {
		if (oceanicParameterList.get(index) == null) {
		    oceanicParameterList.remove(index);
		}
	    }
	    Collections.sort(oceanicParameterList);

	    // Add others OceanicParameters
	    for (final String paramName : oceanicParameterList) {
		if (!parametersOrder.contains(paramName)) {
		    parametersOrder.add(paramName);
		}
	    }

	    parametersOrderForObservations.put(observation.getReference(), parametersOrder);
	}

	return parametersOrderForObservations;
    }

    /**
     * Get the StartStep for Q0 (or null if there is nothing to do).
     *
     * @param controller
     * @param homeViewController
     * @return
     */
    protected abstract StartStepAbstract getStartStepQ0(final Controller controller,
	    final HomeViewController homeViewController);

    /**
     * Get the StartStep for Q11 (or null if there is nothing to do).
     *
     * @param controller
     * @param homeViewController
     * @return
     */
    protected abstract StartStepAbstract getStartStepQ11(final Controller controller,
	    final HomeViewController homeViewController);

    /**
     * Get the StartStep for Q12 (or null if there is nothing to do).
     *
     * @param controller
     * @param homeViewController
     * @return
     */
    protected abstract StartStepAbstract getStartStepQ12(final Controller controller,
	    final HomeViewController homeViewController);

    /**
     * Get the StartStep for Q21 (or null if there is nothing to do).
     *
     * @param controller
     * @param homeViewController
     * @return
     */
    protected abstract StartStepAbstract getStartStepQ21(final Controller controller,
	    final HomeViewController homeViewController);

    /**
     * Get the StartStep for Q22 (or null if there is nothing to do).
     *
     * @param controller
     * @param homeViewController
     * @return
     */
    protected abstract StartStepAbstract getStartStepQ22(final Controller controller,
	    final HomeViewController homeViewController);

    /**
     * Launch the configuration manager
     *
     * To be ovveride
     *
     * @param event
     */
    protected void launchConfigManager(final GuiEventConfigManager event) {
    }

    /**
     * GuiEventDisplayDialog management
     *
     * @param guiEventDisplayDialog
     */
    protected void manageJDialogEvent(final GuiEventDisplayDialog guiEventDisplayDialog) {
	SC3Logger.LOGGER.trace("manageJDialogEvent : " + guiEventDisplayDialog.displayDialog() + " - "
		+ guiEventDisplayDialog.getTimeOfEvent() + ", old time : " + lastDisplayDialogEvent);

	synchronized (lastDisplayDialogEvent) {
	    if (guiEventDisplayDialog.displayDialog().booleanValue()
		    && (lastDisplayDialogEvent < guiEventDisplayDialog.getTimeOfEvent())) {
		// Create a new JDialog to inform the user (and to lock the SCOOP3 Frame)
		lastDisplayDialogEvent = guiEventDisplayDialog.getTimeOfEvent();
		if (dialog == null) {
		    dialog = new JDialogWithCounter(frame.getFrame(), guiEventDisplayDialog);
		    try {
			dialog.setVisible(true);
		    } catch (final Exception e) {
			// do nothing
		    }
		}
	    } else {
		// Close the JDialog
		lastDisplayDialogEvent = guiEventDisplayDialog.getTimeOfEvent();
		if (dialog != null) {
		    dialog.setVisible(false);
		    dialog.dispose();
		    SC3Logger.LOGGER.debug(dialog.getElapsedTimeMessage());
		    dialog = null;
		}
	    }
	}
    }

    /**
     * @param event
     */
    protected void manageMiscEvent(final GuiEventMiscEvent event) {
	// Nothing to do here.
    }

    /**
     * Remove last main panel from the frame, and reinit the Ribbon
     */
    protected void removeAllPanelsAndTasks() {
	frame.getRibbon().removeAllTasks();

	frame.getFrame().getContentPane().remove(homeViewController.getView().getDisplayComponent());

	if (commonViewController != null) {
	    frame.getFrame().getContentPane().remove(commonViewController.getCommonViewImpl().getDisplayComponent());
	    commonViewController.prepareForDispose();
	    commonViewController = null;
	}
    }

    /**
     * Restore the backup of the current working copy if it exists. This method needs to be override if needed.
     */
    protected abstract void restoreAndDeleteBackupCopyIfExists();

    /**
     * Start a new step
     *
     */
    protected void startStep(final GuiEventStartStep guiEventStartStep) {
	StartStepAbstract startStepAbstract = null;
	switch (guiEventStartStep.getStepToStart()) {
	case QC0:
	    startStepAbstract = getStartStepQ0(this, homeViewController);
	    break;
	case QC11:
	    startStepAbstract = getStartStepQ11(this, homeViewController);
	    break;
	case QC12:
	    startStepAbstract = getStartStepQ12(this, homeViewController);
	    break;
	case QC21:
	    startStepAbstract = getStartStepQ21(this, homeViewController);
	    break;
	case QC22:
	    startStepAbstract = getStartStepQ22(this, homeViewController);
	    break;
	case START:
	    // Never called
	    break;
	default:
	    break;
	}
	if (startStepAbstract != null) {
	    startStepAbstract.start();
	}
    }

    /**
     * Default behavior when a Step is completed
     *
     * @param event
     */
    protected void stepCompleted(final GuiEventStepCompleted event) {
	StepCode stepCode = null;

	GuiEventChangeMainPanelToStep targetStep = null;

	switch (event.getStepCompleted()) {
	case QC0:
	    stepCode = StepCode.QC0;
	    targetStep = new GuiEventChangeMainPanelToStep(StepCode.QC11, SubStep.QC11_START);
	    break;
	case QC11:
	    // Nothing to do by default
	    break;
	case QC12:
	    stepCode = StepCode.QC12;
	    targetStep = new GuiEventChangeMainPanelToStep(StepCode.QC21, SubStep.QC21_REFERENCE);
	    break;
	case QC21:
	    // Nothing to do by default
	    break;
	case QC22:
	    stepCode = StepCode.QC22;
	    break;
	case START:
	    // Nothing to do by default
	    break;
	default:
	    break;
	}

	if (stepCode != null) {
	    // Update view
	    removeAllPanelsAndTasks();
	    updateButtonsEnabled();
	    if (targetStep == null) {
		frame.getFrame().getContentPane().add(homeViewController.getView().getDisplayComponent());
		frame.getFrame().getContentPane().validate();
		frame.getFrame().getContentPane().repaint();
	    } else {
		changeMainPanelToStep(targetStep);
	    }
	}
    }

    /**
     * Update the buttons enabled state
     */
    protected abstract void updateButtonsEnabled();

    /**
     * Update the configuration combo values
     */
    protected void updateConfigurationCombo() {
	homeViewController.getView().updateConfigurationJComboBox();
    }

    protected void updateFrameTitle(final GuiEventUpdateFrameTitle event) {
	String newTitle = getApplicationTitle();
	if ((event != null) && (event.getNewTitle() != null)) {
	    newTitle += " - " + event.getNewTitle();
	}
	getFrame().setTitle(newTitle);
    }

    /**
     * Update the file name in the File Choose text field
     *
     * @param event
     */
    protected void updateFileChooseName(final GuiEventUpdateFileChooseName event) {
	if ((event != null) && (event.getNewFileName() != null) && !event.getNewFileName().isEmpty()) {
	    homeViewController.getView().getFileChooseTextField().setText(event.getNewFileName());
	} else {
	    homeViewController.getView().getFileChooseTextField()
		    .setText(Messages.getMessage("bpc-gui.choose-file-label"));
	}
    }

    /**
     *
     * @return true si le controller peut restituer au moins une observation
     */
    public boolean datasetIsNotEmpty() {
	return (dataset != null) && !dataset.getObservations().isEmpty();
    }

    /**
     * Charge le dataset en fonction du homeViewController(Driver et URI)
     */
    public abstract boolean loadDataset();

    public abstract boolean getErrorOccured();

    /**
     * Check if the DriverManager can (or not) open the given file
     *
     * @param file
     * @return
     * @throws IOException
     */
    protected boolean canOpen(final String file) throws DriverException {
	final List<IDriver> driversPossible = driverManager.findDriverForFile(file);
	final IDriver driver = driversPossible.size() == 1 ? driversPossible.get(0) : null;

	return driver != null;
    }

    /**
     * This method have to be override to create and init the Driver Manager
     */
    protected abstract void createAndInitDriverManager();

    /**
     * @return the driverManager
     */
    public DriverManager getDriverManager() {
	return driverManager;
    }

    /**
     * @param driverManager
     *            the driverManager to set
     */
    protected void setDriverManager(final DriverManager driverManager) {
	this.driverManager = driverManager;
    }

    /**
     * Display an error message with ids of corrupted observations
     */
    public void displayCorruptedObservationsMessage() {
	if (!dataset.getCorruptedObservationMap().isEmpty()) {
	    String messageBody = "";
	    String corruptedDimObsIds = "";
	    String corruptedQcObsIds = "";

	    for (final Entry<Observation, CorruptionType> entry : dataset.getCorruptedObservationMap().entrySet()) {
		if (entry.getValue().equals(CorruptionType.CORRUPTED_DIM)) {
		    corruptedDimObsIds += entry.getKey().getId() + ", ";
		} else if (entry.getValue().equals(CorruptionType.CORRUPTED_QC)) {
		    corruptedQcObsIds += entry.getKey().getId() + ", ";
		}
	    }
	    if (corruptedDimObsIds.length() > 2) {
		corruptedDimObsIds = corruptedDimObsIds.substring(0, corruptedDimObsIds.length() - 2);
	    }
	    if (corruptedQcObsIds.length() > 2) {
		corruptedQcObsIds = corruptedQcObsIds.substring(0, corruptedQcObsIds.length() - 2);
	    }

	    if (!corruptedDimObsIds.isEmpty()) {
		messageBody += MessageFormat.format(
			Messages.getMessage("coriolis-controller.corrupted-dim-observations.message"),
			corruptedDimObsIds);
	    }

	    if (!corruptedQcObsIds.isEmpty()) {
		messageBody += MessageFormat.format(
			Messages.getMessage("coriolis-controller.corrupted-qc-observations.message"),
			corruptedQcObsIds);
	    }

	    Dialogs.showErrorMessage(Messages.getMessage("coriolis-controller.corrupted-observations.title"),
		    messageBody);
	}
    }

    // overrided and used only in argoController
    public void loadFileOrDirectory(final Properties properties, final boolean firstLoading) {
    }

    protected RibbonApplicationMenu getApplicationMenu() {
	return null;
    }

    protected void changeLanguage() {
	// language locale
	Locale locale;

	if (Messages.getResourceBundle().getLocale().getLanguage().equals("fr")) {
	    locale = new Locale("en", "EN");
	} else {
	    locale = new Locale("fr", "FR");
	}
	Messages.setResourceBundle(ResourceBundle.getBundle("messages", locale));
	// On change la locale par défaut de la JVM :
	Locale.setDefault(locale);
	// On change la locale par défaut du LookAndFeel :
	UIManager.getDefaults().setDefaultLocale(locale);
	// On change la locale par défaut des nouveaux composants :
	JComponent.setDefaultLocale(locale);

	if (homeViewController != null) {
	    homeViewController.updateHomeGUIAfterChangeLanguage();
	}
	updateGUIAfterChangeLanguage();
    }

    private void updateGUIAfterChangeLanguage() {
	// update application menu
	RibbonApplicationMenu applicationMenu = null;
	if (getApplicationMenu() == null) {
	    applicationMenu = frame.getApplicationMenu();
	} else {
	    applicationMenu = getApplicationMenu();
	}
	frame.getRibbon().setApplicationMenu(applicationMenu);

	// update ribbon bands
	if (getFrame().getRibbon().getTaskCount() > 0) {
	    getFrame().getRibbon().removeTask(getFrame().getRibbon().getTask(0));
	    final Thread t = new Thread(() -> commonViewController.updateRibbonBandAfterChangeLanguage());
	    t.start();
	}

	if (commonViewController != null) {
	    // update Metadata panels
	    commonViewController.updateDatasetMetadataTable();
	    if (commonViewController.getCommonViewImpl().getMetadataSplitPane().getCruiseLabel() != null) {
		commonViewController.getCommonViewImpl().getMetadataSplitPane().getCruiseLabel().setText(
			commonViewController.getCommonViewImpl().getMetadataSplitPane().getDatasetTitle().getText());
	    }
	    commonViewController.updateObservationMetadatas(commonViewController.getCommonViewModel()
		    .getObservation(commonViewController.getObservationNumber()), true);
	    if ((commonViewController.getCommonViewModel() instanceof DataViewModel)
		    && (((DataViewModel) commonViewController.getCommonViewModel())
			    .getValidatedObservationsNumber() != null)) {
		// update ValidateButton1 for Coriolis
		((InfoInObservationSubPanel) ((DataViewImpl) commonViewController.getCommonViewImpl())
			.getInfoInObservationSubPanel())
				.getValidateButton()
				.setText(Messages.getMessage("bpc-gui.button-validate") + " ("
					+ ((DataViewModel) commonViewController.getCommonViewModel())
						.getValidatedObservationsNumber()
					+ ")");
		// update ValidateButton2 for Coriolis
		((InfoInObservationSubPanel) ((DataViewImpl) commonViewController.getCommonViewImpl())
			.getMetadataSplitPane().getInfoInObservationSubPanel())
				.getValidateButton()
				.setText(Messages.getMessage("bpc-gui.button-validate") + " ("
					+ ((DataViewModel) commonViewController.getCommonViewModel())
						.getValidatedObservationsNumber()
					+ ")");
	    }

	    // update graphs name
	    commonViewController.updateGraphNamesAfterChangeLanguage();
	}
    }
}
