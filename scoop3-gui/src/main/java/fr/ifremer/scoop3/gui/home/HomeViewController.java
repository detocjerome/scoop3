package fr.ifremer.scoop3.gui.home;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.bushe.swing.event.EventBus;

import fr.ifremer.scoop3.controller.worflow.StepCode;
import fr.ifremer.scoop3.core.report.utils.ReportUtils;
import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.MessageItem;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.ITEM_STATE;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;
import fr.ifremer.scoop3.events.GuiEventBackupIsComplete;
import fr.ifremer.scoop3.events.GuiEventConfigManager;
import fr.ifremer.scoop3.events.GuiEventStartStep;
import fr.ifremer.scoop3.events.GuiEventUpdateButtonsEnabled;
import fr.ifremer.scoop3.events.GuiEventUpdateFrameTitle;
import fr.ifremer.scoop3.gui.core.Scoop3Frame;
import fr.ifremer.scoop3.gui.home.dialog.WaitingDialog;
import fr.ifremer.scoop3.gui.utils.Dialogs;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.io.WriterManager;
import fr.ifremer.scoop3.io.driver.DriverException;
import fr.ifremer.scoop3.io.driver.DriverManager;
import fr.ifremer.scoop3.io.driver.IDriver;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.parameter.Parameter;
import fr.ifremer.scoop3.model.parameter.ParametersRelationships;

/**
 * Controller for the Home view
 */
public abstract class HomeViewController {

    /**
     * Result of Thread reader
     */
    protected Future<Boolean> futureReader;
    /**
     * Home view Model
     */
    protected final HomeViewModel model;
    /**
     * Home View
     */
    protected final HomeViewImpl view;

    /**
     * Driver Manager
     */
    protected final DriverManager driverManager;

    /**
     *
     */
    protected Scoop3Frame scoop3Frame = null;

    public static boolean isIhmDone = false;

    private boolean resetOK = true;

    /**
     * Constructor
     *
     * @param homeView
     * @param homeViewModel
     */
    protected HomeViewController(final HomeViewImpl homeView, final HomeViewModel homeViewModel,
	    final DriverManager driverManager) {
	super();
	view = homeView;
	model = homeViewModel;
	this.driverManager = driverManager;

	// Initialize events for the view
	initEventListeners();
    }

    protected HomeViewController(final Scoop3Frame scoop3Frame, final HomeViewImpl homeView,
	    final HomeViewModel homeViewModel, final DriverManager driverManager) {
	this(homeView, homeViewModel, driverManager);
	this.scoop3Frame = scoop3Frame;
    }

    /**
     * Add all error messages in the Report (if not NULL)
     *
     * @param observation
     * @param errorMessageItems
     */
    public void addAllMessages(final Observation observation, final ArrayList<CAErrorMessageItem> errorMessageItems,
	    final STEP_TYPE stepType) {
	if (errorMessageItems != null) {
	    for (final CAErrorMessageItem caErrorMessageItem : errorMessageItems) {
		final CAErrorMessageItem messageEqual = getReport().addMessage(caErrorMessageItem, stepType);

		// Try to apply old manual QC update
		if ((observation != null) && (messageEqual != null)
			&& (messageEqual instanceof CADataErrorMessageItem)) {
		    final CADataErrorMessageItem caDataErrorMessageItem = (CADataErrorMessageItem) messageEqual;
		    if (caDataErrorMessageItem.getFlagManual() != null) {
			final List<QCValues> qcValues = observation
				.getOceanicParameter(caDataErrorMessageItem.getParamCode()) != null
					? observation.getOceanicParameter(caDataErrorMessageItem.getParamCode())
						.getQcValues()
					: observation.getReferenceParameter().getQcValues();
			qcValues.set(caDataErrorMessageItem.getReferenceIndex(),
				caDataErrorMessageItem.getFlagManual());

			if (observation.getOceanicParameter(caDataErrorMessageItem.getParamCode()) != null) {
			    // If it is a father of Computed Parameter, the following loop will update Computed
			    // Parameter
			    for (final Parameter<? extends Number> computedParameter : ParametersRelationships
				    .getLinkedParameters(
					    observation.getOceanicParameter(caDataErrorMessageItem.getParamCode()))) {
				// Compute worst QCValues
				QCValues valueToSetForComputedParameter = caDataErrorMessageItem.getFlagManual();
				for (final Parameter<? extends Number> father : ParametersRelationships
					.getFathers(computedParameter)) {
				    final String fathersName = father.getCode();
				    final Parameter<? extends Number> fatherParam = observation
					    .getOceanicParameter(fathersName) == null
						    ? observation.getReferenceParameter()
						    : observation.getOceanicParameter(fathersName);
				    final QCValues fatherQC = fatherParam.getQcValues()
					    .get(caDataErrorMessageItem.getReferenceIndex());
				    valueToSetForComputedParameter = QCValues.getWorstQC(valueToSetForComputedParameter,
					    fatherQC);
				}

				// SET valueToSetForComputedParameter
				computedParameter.getQcValues().set(caDataErrorMessageItem.getReferenceIndex(),
					valueToSetForComputedParameter);
			    }
			}
		    }
		}
	    }
	}
    }

    /**
     * Concat report with current report
     *
     * @param medatlasReport
     */
    public void concatReport(final Report medatlasReport) {
	model.getReport().concat(medatlasReport);
    }

    /**
     * @return the path
     */
    public String getPath() {
	return model.getPath();
    }

    /**
     * @return the paths of Scoop3Explorer
     */
    public ArrayList<String> getPaths() {
	return model.getPaths();
    }

    /**
     * @return the report
     */
    public Report getReport() {
	return model.getReport();
    }

    /**
     * @return the model
     */
    public HomeViewModel getModel() {
	return this.model;
    }

    /**
     * Return the home view
     *
     * @return the View
     */
    public HomeViewImpl getView() {
	return view;
    }

    /**
     * Persist report
     *
     * @return
     */
    public boolean persistReport() {
	if ((model != null) && (model.getReport() != null)) {
	    model.getReport().persist();
	    return true;
	}
	return false;
    }

    /**
     * Set the Choose file button enabled state
     *
     * @param enabled
     */
    public void setChooseFileButtonEnabled(final boolean enabled) {
	view.getChooseFileButton().setEnabled(enabled);
    }

    /**
     * Set the Data button enabled state
     *
     * @param enabled
     */
    public void setDataButtonEnabled(final boolean enabled) {
	view.getDataButton().setEnabled(enabled);
    }

    /**
     * Set the Format button enabled state
     *
     * @param enabled
     */
    public void setFormatButtonEnabled(final boolean enabled) {
	view.getFormatButton().setEnabled(enabled);
    }

    /**
     * Set the Metadata button enabled state
     *
     * @param enabled
     */
    public void setMetadataButtonEnabled(final boolean enabled) {
	view.getMetadataButton().setEnabled(enabled);
    }

    /**
     * @param pathStr
     *            the path to set
     * @throws DatasetLoadException
     */
    public void setPath(final String pathStr) throws HomeControllerException {
	model.setPath(pathStr);

	view.getFileChooser().setSelectedFile(new File(pathStr));
	view.getFileChooseTextField().setText(Paths.get(pathStr).getFileName().toString());
    }

    /**
     * Display the error dialog
     *
     * @param title
     */
    public void showErrorMessages(final String title, final STEP_TYPE stepType) {
	Dialogs.showErrorMessage(title, model.getReport().getErrorMessagesInStep(stepType).toString());
    }

    /**
     * Add the Action listener on the Configuration button
     */
    private void createConfigurationButtonsActionListener() {
	if (view.getConfigurationButton() != null) {
	    view.getConfigurationButton()
		    .addActionListener((final ActionEvent event) -> EventBus.publish(new GuiEventConfigManager()));
	}

	if (view.getConfigurationJComboBox() != null) {
	    view.getConfigurationJComboBox().addActionListener((final ActionEvent e) -> configurationFileChanged());
	}
    }

    /**
     * Add the Action listener on the Data button
     */
    private void createDataActionListener() {
	// Set an action listener on the format button
	view.getDataButton().addActionListener((final ActionEvent event) ->
	// Start Step Q21
	EventBus.publish(new GuiEventStartStep(StepCode.QC21)));
    }

    /**
     * Add a listener for the file chooser button
     */
    private void createFileButtonActionListener() {
	// Open a file chooser on a click
	view.getChooseFileButton().addActionListener((final ActionEvent e) -> openFile());
    }

    public void openFile() {
	if (WriterManager.isWrittenInProgress()) {
	    final WaitingDialog waitingDialog = new WaitingDialog(scoop3Frame,
		    Messages.getMessage("gui.waiting-dialog-backup-in-progress"));

	    ExecutorService executor;
	    executor = Executors.newSingleThreadExecutor();
	    executor.execute(() -> {
		while (WriterManager.isWrittenInProgress()) {
		    try {
			Thread.sleep(500);
		    } catch (final InterruptedException e) {
			SC3Logger.LOGGER.error(e.getMessage(), e);
		    }
		}

		waitingDialog.setVisible(false);
	    });
	    // Shutdown the executor
	    executor.shutdown();

	    waitingDialog.setVisible(true);
	    waitingDialog.dispose();
	}

	SC3Logger.LOGGER.debug("Choose a file to open");

	// Get the return value to know if user approve or cancer the file chooser
	final int returnVal = view.getFileChooser().showOpenDialog(null);

	// If the user approve the file chooser
	if (returnVal == JFileChooser.APPROVE_OPTION) {

	    // Always disable the meta data button before parsing the file
	    view.getFormatButton().setEnabled(false);
	    view.getMetadataButton().setEnabled(false);
	    view.getDataButton().setEnabled(false);

	    // Retrieve the selected file
	    final File file = view.getFileChooser().getSelectedFile();

	    if (!canOperatorWorkWithThisFile(file.getName())) {
		// Do not load this file ...
		return;
	    }

	    // Fill the file chooser text field with the name of the selected file
	    view.getFileChooseTextField().setText(file.getName());

	    // If future != null, a thread is already launched to parse a file
	    // So, we cancel the future and hide the progress bar
	    if (futureReader != null) {
		futureReader.cancel(true);
	    }

	    // Try to read the file
	    boolean canOpen = false;
	    IDriver driver = null;
	    try {

		final List<IDriver> driversCanRead = getDriverManager().findDriverForFile(file.getAbsolutePath());

		driver = driversCanRead.size() == 1 ? driversCanRead.get(0) : null;
		canOpen = driver != null;
	    } catch (final DriverException e1) {
		Dialogs.showInfoMessage(Messages.getMessage("bpc-gui.unknown-file-title"),
			Messages.getMessage("bpc-gui.unknown-file-message"));
		// Prepare the report for the first time
		model.setReport(new Report(file.getAbsolutePath()));
		model.getReport().addMessage(
			new MessageItem(ITEM_STATE.ERROR, Messages.getMessage("bpc-gui.unknown-file-message")),
			STEP_TYPE.Q0_LOADING_FILE);
	    }
	    // if driver found
	    if (canOpen) {
		SC3Logger.LOGGER.debug(Messages.getMessage("bpc-gui.open-file"));
		final String newFilePath = getFilePath(file.getAbsolutePath());

		displayDialogForKnownFile();

		// Prepare the report for the first time
		model.setReport(new Report(newFilePath));

		// Read the setup.properties file if setup.txt exists
		final String filePath = ReportUtils.contextDir;
		String configFile = "";
		if (new File(filePath + "\\setup.txt").exists()) {
		    try {
			final FileReader reader = new FileReader(filePath + "\\setup.txt");
			int character;

			while ((character = reader.read()) != -1) {
			    configFile += (char) character;
			}
			reader.close();
		    } catch (final IOException e) {
			e.printStackTrace();
		    }
		}

		// select it in the combobox if it contains it
		if (!configFile.equals("")) {
		    // Get number of items
		    final int num = getView().getConfigurationJComboBox().getItemCount();
		    boolean configFilePresent = false;

		    // Get items
		    for (int i = 0; i < num; i++) {
			final String item = getView().getConfigurationJComboBox().getItemAt(i);
			if (item.equals(configFile)) {
			    configFilePresent = true;
			}
		    }
		    if (configFilePresent) {
			getView().getConfigurationJComboBox().setSelectedItem(configFile);
		    } else {
			JOptionPane.showMessageDialog(scoop3Frame,
				"Le fichier de configuration " + configFile + " est introuvable dans la liste",
				"Configuration introuvable", JOptionPane.WARNING_MESSAGE);
		    }
		}

		// 1 - If it's OK the file is recognized
		// /!\ Use file.getAbsolutePath() and not newFilePath /!\
		model.setPath(file.getAbsolutePath());

		//
		model.getReport().addMessage(
			new MessageItem(ITEM_STATE.INFO, Messages.getMessage("bpc-gui.known-file-message")),
			STEP_TYPE.Q0_LOADING_FILE);

		// 4 - create the context directory
		ReportUtils.createContextDirectory(Paths.get(newFilePath));
		// 5 - copy the file to context directory
		ReportUtils.copyFileToContextDirectory(newFilePath, null);

		// Notify that the backup is complete ...
		EventBus.publish(new GuiEventBackupIsComplete());

		// update IHM state
		EventBus.publish(new GuiEventUpdateButtonsEnabled());

		// 6 - launch Format Action
		startFormatAction();

		/*
		 * TODO a faire plus tard le fichier n'est pas repertorie a ce moment / Verrouillage du fichier pour
		 * l'utilisateur courant vis a vis des autres utilisateurs;
		 * lockDataset(view.getFileChooseTextField().getText());
		 */

		SC3Logger.LOGGER.trace("#### FILE LOADED : " + file.getName());

		// Unable to read input file
	    } else {
		Dialogs.showInfoMessage(Messages.getMessage("bpc-gui.unknown-file-title"),
			Messages.getMessage("bpc-gui.unknown-file-message"));
		if ((model != null) && (model.getReport() != null)) {
		    model.getReport().addMessage(
			    new MessageItem(ITEM_STATE.ERROR, Messages.getMessage("bpc-gui.unknown-file-message")),
			    STEP_TYPE.Q0_LOADING_FILE);
		}
	    }

	    persistReport();

	    // Update the title of the Frame
	    EventBus.publish(new GuiEventUpdateFrameTitle(file.getName()));
	}
    }

    /**
     * Add a listener for the Format button
     */
    private void createFormatActionListener() {
	// Set an action listener on the format button
	view.getFormatButton().addActionListener((final ActionEvent event) -> {
	    // Remove old messages
	    getReport().getStep(STEP_TYPE.Q0_LOADING_FILE).clearStepItems();

	    startFormatAction();
	});
    }

    /**
     * Add the Action listener on the Metadata button
     */
    private void createMetadataActionListener() {
	// Set an action listener on the format button
	view.getMetadataButton().addActionListener((final ActionEvent event) ->
	// Start Step Q11
	EventBus.publish(new GuiEventStartStep(StepCode.QC11))
	// EventBus.publish(new GuiEventStepComplete(StepCode.Q11, model.getReport()));
	);
    }

    /**
     * Initialize events
     */
    private void initEventListeners() {
	// Add listener to the view

	// Initialize event for file chooser button
	createFileButtonActionListener();

	// Initialize event for format button
	createFormatActionListener();

	// Initialize event for metadata button
	createMetadataActionListener();

	// Initialize event for data button
	createDataActionListener();

	// Initialize event for configuration button
	createConfigurationButtonsActionListener();
    }

    /**
     * Launch the Action linked to the Format button
     */
    protected void startFormatAction() {
	EventBus.publish(new GuiEventStartStep(StepCode.QC0));
    }

    /**
     * Check if the Operator can work with this file.
     *
     * @param newFilename
     * @return TRUE if the operator can work with it. FALSE otherwise ...
     */
    protected boolean canOperatorWorkWithThisFile(final String newFilename) {
	return true;
    }

    /**
     * A new file will be loaded ...
     *
     * @throws HomeControllerException
     */
    protected void lockDataset(final String newFilename) throws HomeControllerException {
	// Nothing to do here ...
    }

    /**
     * The configuration file selected has been changed
     */
    protected void configurationFileChanged() {
    }

    /**
     *
     */
    protected void displayDialogForKnownFile() {
	Dialogs.showInfoMessage(Messages.getMessage("bpc-gui.known-file-title"),
		Messages.getMessage("bpc-gui.known-file-message"));
    }

    /**
    *
    */
    protected void displayDialogForKnownFiles() {
	Dialogs.showInfoMessage(Messages.getMessage("bpc-gui.known-files-title"),
		Messages.getMessage("bpc-gui.known-files-message"));
    }

    /**
     * @return
     */
    protected String getFilePath(final String filePath) {
	return filePath;
    }

    /**
     * @return the driverManager
     */
    public DriverManager getDriverManager() {
	return driverManager;
    }

    public boolean isResetOK() {
	return this.resetOK;
    }

    public void setResetOK(final boolean resetOK) {
	this.resetOK = resetOK;
    }

    public void updateHomeGUIAfterChangeLanguage() {
    }
}
