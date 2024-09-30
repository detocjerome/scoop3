package fr.ifremer.scoop3.gui.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.MessageItem;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAMetadataErrorMessageItem;
import fr.ifremer.scoop3.gui.common.MetadataSplitPane.InfoInObservationSubPanel;
import fr.ifremer.scoop3.gui.common.jdialog.ReportJDialog;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyMultipleErrorMessagesChangeEvent;
import fr.ifremer.scoop3.gui.core.Scoop3Frame;
import fr.ifremer.scoop3.gui.core.View;
import fr.ifremer.scoop3.gui.data.DataViewImpl;
import fr.ifremer.scoop3.gui.map.MapViewImpl;
import fr.ifremer.scoop3.gui.reference.ReferenceViewImpl;
import fr.ifremer.scoop3.gui.utils.Dialogs;
import fr.ifremer.scoop3.infra.event.MapPropertyChangeEvent;
import fr.ifremer.scoop3.infra.event.MapPropertyChangeEvent.MAP_EVENT_ENUM;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.infra.undo_redo.metadata.MetadataValueChange;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;

public abstract class CommonViewImpl implements View {

    protected static final int SPLITPANE_DIVIDERSIZE = 8;
    private static final int TOOLBAR_HEIGHT = 150;
    protected static final int WESTPANEL_DEFAULT_WIDTH = 400;
    /**
     * The JPanel which contains all components to display
     */
    protected JPanel displayComponent;
    /**
     * This Panel is specific to each ViewImpl
     */
    protected JPanel eastPanel;

    /**
     * This Panel is a duplicate of MetadataSplitPane.infoInObservationSubPanel
     */
    protected JPanel infoInObservationSubPanel;

    /**
     * This Panel is specific to each ViewImpl
     */
    protected JSplitPane westMapJSplitPane;

    /**
     * Contains the name of the trated file and the MetadataSplitPane
     */
    protected JPanel westPanel;

    /**
     * The Main Split Pane. It contains WestPanel and EastPanel
     */
    protected JSplitPane mainSplitPane;

    protected JSplitPane centerSplitPane;

    // map ribbon band buttons
    private JCommandButton mapLinkUnlinkButton;

    /**
     * Panel with the Map (if needed)
     */
    protected JPanel mapPanel;
    private JCommandButton mapShowHideLabelButton;
    private JCommandButton mapZoomInButton;
    private JCommandButton mapZoomInitialButton;
    private JCommandButton mapZoomOutButton;
    private JCommandToggleButton mapZoomRectButton;
    private JCommandButton mapZoomWorldButton;

    // /**
    // * QC Report Panel
    // */
    // private JScrollPane qCReportPanel;
    /**
     * Memory Progress Bar ...
     */
    private final JProgressBar memoryProgressBar = new JProgressBar();
    /**
     * To send message to the controler
     */
    private PropertyChangeListener propertyChangeListener;
    /**
     * Reference on the Scoop3Frame
     */
    private Scoop3Frame scoop3Frame;
    private Timer timer;
    /**
     * Reference on the Cancel Button
     */
    protected JButton cancelButton;
    /**
     * Reference on the Dataset
     */
    protected Dataset dataset;

    /**
     * Split pane for the Metadata
     */
    protected MetadataSplitPane metadataSplitPane;
    protected PropertyChangeSupport propertyChangeSupport;
    protected JCommandButton redoButton;
    /**
     * Reference on the report
     */
    protected final Report report;
    /**
     * Reference on the Report Button
     */
    protected JCommandButton reportButton;
    /**
     * Reference on the STEP_TYPE
     */
    protected final STEP_TYPE stepType;
    protected JCommandButton undoAllButton;
    protected JCommandButton undoButton;
    /**
     * Reference on the Validate Button
     */
    protected JButton validateButton;

    /**
     * Moving info label
     */
    int infoLabelX;
    int infoLabelY;
    double infoLabelLimit;
    String currentObsIndex;

    /**
     * Default constructor
     *
     * @param scoop3Frame
     * @param datasetMetadatasTable
     * @param observationMetadatasTable
     * @param dataset
     * @param report
     */
    protected CommonViewImpl(final Scoop3Frame scoop3Frame, final MetadataSplitPane metadataSplitPane,
	    final Dataset dataset, final Report report, final STEP_TYPE stepType) {

	this(scoop3Frame, dataset, report, stepType);
	this.metadataSplitPane = metadataSplitPane;
	init();

	if (((this instanceof MapViewImpl)
		&& ReportJDialog.getReportJDialog(scoop3Frame, this, report).isThereAtLeastOneErrorMessageForMetadata())
		|| (((this instanceof DataViewImpl) || (this instanceof ReferenceViewImpl)) && ReportJDialog
			.getReportJDialog(scoop3Frame, this, report).isThereAtLeastOneErrorMessageForData())) {
	    ReportJDialog.getReportJDialog(scoop3Frame, this, report).setVisible(true);
	} else {
	    ReportJDialog.disposeIfExists();
	}

	timer = new Timer();
	timer.schedule(new TimerTask() {
	    @Override
	    public void run() {

		SwingUtilities.invokeLater(() -> {
		    final long current = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024
			    / 1024;
		    memoryProgressBar.setValue((int) current);
		    // Remove following line to display in %
		    memoryProgressBar.setString(current + " / " + memoryProgressBar.getMaximum() + " MB");
		});
	    }
	}, 0, 1000);

	displayComponent.requestFocusInWindow();
    }

    /**
     * Default constructor
     *
     * @param scoop3Frame
     * @param datasetMetadatasTable
     * @param observationMetadatasTable
     * @param dataset
     * @param report
     */
    protected CommonViewImpl(final Scoop3Frame scoop3Frame, final MetadataTable datasetMetadatasTable,
	    final MetadataTable observationMetadatasTable, final Dataset dataset, final Report report,
	    final STEP_TYPE stepType) {

	this(scoop3Frame, dataset, report, stepType);

	init(datasetMetadatasTable, observationMetadatasTable);

	if (((this instanceof MapViewImpl)
		&& ReportJDialog.getReportJDialog(scoop3Frame, this, report).isThereAtLeastOneErrorMessageForMetadata())
		|| (((this instanceof DataViewImpl) || (this instanceof ReferenceViewImpl)) && ReportJDialog
			.getReportJDialog(scoop3Frame, this, report).isThereAtLeastOneErrorMessageForData())) {
	    ReportJDialog.getReportJDialog(scoop3Frame, this, report).setVisible(true);
	} else {
	    ReportJDialog.disposeIfExists();
	}

	displayComponent.requestFocusInWindow();
    }

    /**
     * Default constructor
     *
     * @param scoop3Frame
     * @param datasetMetadatasTable
     * @param observationMetadatasTable
     * @param dataset
     * @param report
     */
    private CommonViewImpl(final Scoop3Frame scoop3Frame, final Dataset dataset, final Report report,
	    final STEP_TYPE stepType) {
	this.scoop3Frame = scoop3Frame;
	this.dataset = dataset;
	this.stepType = stepType;
	this.report = report;

	displayComponent = new JPanel();
	displayComponent
		.setPreferredSize(new Dimension(FileConfig.getScoop3FileConfig().getInt("application.data.width"),
			FileConfig.getScoop3FileConfig().getInt("application.data.height")));
	displayComponent.setSize(new Dimension(FileConfig.getScoop3FileConfig().getInt("application.data.width"),
		FileConfig.getScoop3FileConfig().getInt("application.data.height")));
	displayComponent.setLayout(new BorderLayout());
	timer = new Timer();
	timer.schedule(new TimerTask() {
	    @Override
	    public void run() {

		SwingUtilities.invokeLater(() -> {
		    final long current = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024
			    / 1024;
		    memoryProgressBar.setValue((int) current);
		    // Remove following line to display in %
		    memoryProgressBar.setString(current + " / " + memoryProgressBar.getMaximum() + " MB");
		});
	    }
	}, 0, 1000);

    }

    @Override
    public Component getDisplayComponent() {
	return displayComponent;
    }

    /**
     * @return the eastPanel
     */
    public JPanel getEastPanel() {
	return eastPanel;
    }

    /**
     * @return the mapLinkUnlinkButton
     */
    public JCommandButton getMapLinkUnlinkButton() {
	return mapLinkUnlinkButton;
    }

    /**
     * @return the mapShowHideLabelButton
     */
    public JCommandButton getMapShowHideLabelButton() {
	return mapShowHideLabelButton;
    }

    /**
     * @return the mapZoomInButton
     */
    public JCommandButton getMapZoomInButton() {
	return mapZoomInButton;
    }

    /**
     * @return the mapZoomInitialButton
     */
    public JCommandButton getMapZoomInitialButton() {
	return mapZoomInitialButton;
    }

    /**
     * @return the mapZoomOutButton
     */
    public JCommandButton getMapZoomOutButton() {
	return mapZoomOutButton;
    }

    /**
     * @return the mapZoomRectButton
     */
    public JCommandToggleButton getMapZoomRectButton() {
	return mapZoomRectButton;
    }

    /**
     * @return the mapZoomWorldButton
     */
    public JCommandButton getMapZoomWorldButton() {
	return mapZoomWorldButton;
    }

    /**
     * @return the metadataSplitPane
     */
    public MetadataSplitPane getMetadataSplitPane() {
	return metadataSplitPane;
    }

    public void setMetadataSplitPane(final MetadataSplitPane metadataSplitPane) {
	this.metadataSplitPane = metadataSplitPane;
    }

    /**
     * @return the list of the QCValues which can be set
     */
    public static QCValues[] getQCValuesSettable() {
	return new QCValues[] { QCValues.QC_1, QCValues.QC_3, QCValues.QC_4, QCValues.QC_6, QCValues.QC_7,
		QCValues.QC_Q, QCValues.QC_A, QCValues.QC_B, QCValues.QC_9 };
    }

    /**
     * @return the list of the QCValues which can be set
     */
    public static QCValues[] getQCValuesSettableForMetadatasAndErrorReport() {
	return new QCValues[] { QCValues.QC_1, QCValues.QC_3, QCValues.QC_4 };
    }

    /**
     * @return the redoButton
     */
    public JCommandButton getRedoButton() {
	return redoButton;
    }

    /**
     * @return the scoop3Frame
     */
    public Scoop3Frame getScoop3Frame() {
	return scoop3Frame;
    }

    /**
     * @return the Report
     */
    public Report getReport() {
	return report;
    }

    /**
     * @return the undoAllButton
     */
    public JCommandButton getUndoAllButton() {
	return undoAllButton;
    }

    /**
     * @return the undoButton
     */
    public JCommandButton getUndoButton() {
	return undoButton;
    }

    /**
     * Check if there is at least one blocking error in the current Step of the Report
     *
     * @return
     */
    public boolean isThereAtLeastOneBlockingError() {
	for (final MessageItem messageItem : report.getStep(stepType).getMessages()) {
	    if ((messageItem instanceof CAErrorMessageItem) && ((CAErrorMessageItem) messageItem).isBlockingError()) {
		switch (stepType) {
		case Q0_LOADING_FILE:
		    // Could not happen ...
		    break;
		case Q1_CONTROL_AUTO_METADATA:
		    if ((messageItem instanceof CAMetadataErrorMessageItem)
			    && !((CAMetadataErrorMessageItem) messageItem).isErrorChecked() //
			    // FAE 29771
			    && ((CAMetadataErrorMessageItem) messageItem).isGenerateDuringLastControl() //
		    ) {
			return true;
		    }
		    break;
		case Q2_CONTROL_AUTO_DATA:
		    return true;
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

	scoop3Frame = null;
	if (metadataSplitPane != null) {
	    metadataSplitPane.prepareForDispose();
	}
	metadataSplitPane = null;
	westPanel.removeAll();
	westPanel = null;
	eastPanel.removeAll();
	eastPanel = null;
	mainSplitPane.removeAll();
	mainSplitPane = null;
	displayComponent.removeAll();
	displayComponent = null;
	dataset = null;
	propertyChangeListener = null;

	if (mapLinkUnlinkButton != null) {
	    mapLinkUnlinkButton.removeAll();
	}
	mapLinkUnlinkButton = null;
	if (mapShowHideLabelButton != null) {
	    mapShowHideLabelButton.removeAll();
	}
	mapShowHideLabelButton = null;
	if (mapZoomInButton != null) {
	    mapZoomInButton.removeAll();
	}
	mapZoomInButton = null;
	if (mapZoomInitialButton != null) {
	    mapZoomInitialButton.removeAll();
	}
	mapZoomInitialButton = null;
	if (mapZoomOutButton != null) {
	    mapZoomOutButton.removeAll();
	}
	mapZoomOutButton = null;
	if (mapZoomRectButton != null) {
	    mapZoomRectButton.removeAll();
	}
	mapZoomRectButton = null;
	if (mapZoomWorldButton != null) {
	    mapZoomWorldButton.removeAll();
	}
	mapZoomWorldButton = null;

	timer.cancel();
	timer = null;
    }

    /**
     * @param mapPanel
     *            the mapPanel to set
     */
    public void setMapPanel(final JPanel mapPanel) {
	this.mapPanel = mapPanel;
	westMapJSplitPane.setRightComponent(mapPanel);
    }

    /**
     * @param propertyChangeListener
     *            the propertyChangeListener to set
     */
    public void setPropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
	this.propertyChangeListener = propertyChangeListener;
    }

    /**
     * @param propertyChangeSupport
     *            the propertyChangeSupport to set
     */
    public void setPropertyChangeSupport(final PropertyChangeSupport propertyChangeSupport) {
	this.propertyChangeSupport = propertyChangeSupport;
    }

    /**
     * Select an Observation by its reference
     *
     * @param observationReference
     */
    public void setSelectedObservation(final String observationReference) {
	setSelectedObservation(observationReference, -1);
    }

    /**
     * Select an Observation by its reference, and set a level too
     *
     * @param observationReference
     * @param levelIndex
     */
    public void setSelectedObservation(final String observationReference, final int levelIndex) {
	int index = 0;
	final List<Observation> tempDatasetObservations = new ArrayList<Observation>(dataset.getObservations());
	for (final Observation observation : tempDatasetObservations) {
	    if (observation.getId().equals(observationReference)) {
		propertyChangeListener.propertyChange(
			(new MapPropertyChangeEvent(index, MAP_EVENT_ENUM.SELECT_OBSERVATION_BY_REFERENCE))
				.setLevelIndex(levelIndex));
	    }
	    index++;
	}
    }

    /**
     * To override if needed
     *
     * @param metadataValueChanges
     */
    public void updateMetadata(final List<MetadataValueChange> metadataValueChanges) {
    }

    public void updateMultipleErrorMessages(final STEP_TYPE stepType,
	    final List<CAErrorMessageItem> errorMessagesToUpdate, final QCValues qcToSet, final String commentToSet,
	    final Boolean isChecked) {
	propertyChangeSupport.firePropertyChange(new SC3PropertyMultipleErrorMessagesChangeEvent(stepType,
		errorMessagesToUpdate, qcToSet, commentToSet, isChecked));
    }

    /**
     * @param isListOfUndoableChangesEmpty
     * @param isListOfRedoableChangesEmpty
     */
    public void updateUndoRedoButtons(final boolean isListOfUndoableChangesEmpty,
	    final boolean isListOfRedoableChangesEmpty) {
	undoButton.setEnabled(!isListOfUndoableChangesEmpty);
	undoAllButton.setEnabled(!isListOfUndoableChangesEmpty);
	redoButton.setEnabled(!isListOfRedoableChangesEmpty);
    }

    /**
     * Init panels and Ribbon
     *
     * @param observationMetadatasTable
     * @param datasetMetadatasTable
     */
    private void init() {

	// create the ribbon bands
	createRibbonBands();

	createWestSplitPane();

	// east panel
	eastPanel = new JPanel();
	eastPanel.setLayout(new BorderLayout());

	final JPanel buttonsAndMemoryGaugePanel = new JPanel();
	buttonsAndMemoryGaugePanel.setLayout(new BorderLayout());
	infoInObservationSubPanel = getMetadataSplitPane().new InfoInObservationSubPanel(new BorderLayout());
	((InfoInObservationSubPanel) infoInObservationSubPanel).getIndexLabel();
	buttonsAndMemoryGaugePanel.add(infoInObservationSubPanel, BorderLayout.WEST);
	final JPanel infoPanel = new JPanel(new BorderLayout());
	infoPanel.add(getInfoPanel(), BorderLayout.WEST);

	// settings of moving label
	infoLabelX = getInfoLabel().getX();
	infoLabelY = getInfoLabel().getY();
	infoLabelLimit = infoPanel.getWidth() - getInfoLabel().getPreferredSize().getWidth();
	currentObsIndex = "0";
	final TimerTask task = new TimerTask() {
	    @Override
	    public void run() {
		// if change observation, then reset the animation
		if (!currentObsIndex.equals(
			((InfoInObservationSubPanel) infoInObservationSubPanel).getIndexTextField().getText())) {
		    currentObsIndex = ((InfoInObservationSubPanel) infoInObservationSubPanel).getIndexTextField()
			    .getText();
		    infoLabelX = 0;
		    getInfoLabel().setLocation(infoLabelX, infoLabelY);
		    // force a pause to see the start of the label
		    try {
			Thread.sleep(700);
		    } catch (final InterruptedException e) {
			e.printStackTrace();
		    }
		}
		infoLabelLimit = infoPanel.getWidth() - getInfoLabel().getPreferredSize().getWidth();
		// if label size > infoPanel size
		if (infoLabelLimit < 0) {
		    // if label finished to scroll, reset the animation
		    if (infoLabelX < infoLabelLimit) {
			infoLabelX = 0;
			getInfoLabel().setLocation(infoLabelX, infoLabelY);
			// force a pause to see the start of the label
			try {
			    Thread.sleep(700);
			} catch (final InterruptedException e) {
			    e.printStackTrace();
			}
		    }
		    // run animation
		    getInfoLabel().setLocation(infoLabelX--, infoLabelY);
		}
	    }
	};
	timer.schedule(task, 0, 50);

	buttonsAndMemoryGaugePanel.add(infoPanel, BorderLayout.CENTER);
	buttonsAndMemoryGaugePanel.add(getButtonsPanel(), BorderLayout.EAST);
	buttonsAndMemoryGaugePanel.add(getMemoryGaugePanel(), BorderLayout.EAST);

	eastPanel.add(buttonsAndMemoryGaugePanel, BorderLayout.SOUTH);

	createMainSplitPane();

	displayComponent.add(mainSplitPane, BorderLayout.CENTER);
    }

    protected abstract void createWestSplitPane();

    protected abstract void createMainSplitPane();

    /**
     * Init panels and Ribbon
     *
     * @param observationMetadatasTable
     * @param datasetMetadatasTable
     */
    private void init(final MetadataTable datasetMetadatasTable, final MetadataTable observationMetadatasTable) {

	metadataSplitPane = new MetadataSplitPane(datasetMetadatasTable, observationMetadatasTable);
	if (datasetMetadatasTable == null) {
	    metadataSplitPane.getLeftComponent().setMinimumSize(new Dimension());
	    metadataSplitPane.setDividerLocation(0.0d);
	    metadataSplitPane.validate();
	}
	if (observationMetadatasTable == null) {
	    metadataSplitPane.getRightComponent().setMinimumSize(new Dimension());
	    metadataSplitPane.setDividerLocation(1.0d);
	    metadataSplitPane.validate();
	}

	init();

    }

    /**
     * Add a new JCommandButton to the Ribon Band and return it
     *
     * @param ribbonBand
     * @param imagePath
     * @param message
     * @param eltPriority
     * @return
     */
    protected JCommandButton addJCommandButtonToRibbonBand(final JRibbonBand ribbonBand, final String imagePath,
	    final String message, final RibbonElementPriority eltPriority) {

	final URL resource = getClass().getClassLoader().getResource(imagePath);
	final JCommandButton jCommandButton = new JCommandButton(Messages.getMessage(message),
		ImageWrapperResizableIcon.getIcon(resource, new Dimension(32, 32)));
	if (eltPriority != null) {
	    ribbonBand.addCommandButton(jCommandButton, eltPriority);
	}

	return jCommandButton;
    }

    /**
     * Add a new JCommandToggleButton to the Ribon Band and return it
     *
     * @param ribbonBand
     * @param imagePath
     * @param message
     * @param eltPriority
     * @return
     */
    protected JCommandToggleButton addJCommandToggleButtonToRibbonBand(final JRibbonBand ribbonBand,
	    final String imagePath, final String message, final RibbonElementPriority eltPriority) {

	final URL resource = getClass().getClassLoader().getResource(imagePath);
	final JCommandToggleButton jCommandToggleButton = new JCommandToggleButton(Messages.getMessage(message),
		ImageWrapperResizableIcon.getIcon(resource, new Dimension(32, 32)));
	if (eltPriority != null) {
	    ribbonBand.addCommandButton(jCommandToggleButton, eltPriority);
	}

	return jCommandToggleButton;
    }

    /**
     * Method called when the Cancel button is clicked and confirmed
     */
    protected abstract void cancelButtonClicked();

    /**
     * Create the ribbon band (like the Office 2007 menu)
     */

    protected abstract void createRibbonBands();

    /**
     * @return the buttons Panel. By default, with the Validate and Cancel buttons
     */
    protected JPanel getButtonsPanel() {
	final JPanel buttonsPanel = new JPanel();

	validateButton = new JButton(Messages.getMessage("bpc-gui.button-validate"));
	buttonsPanel.add(validateButton);
	validateButton.addActionListener((final ActionEvent e) -> {
	    final int result = Dialogs.showConfirmDialog(scoop3Frame,
		    Messages.getMessage("bpc-gui.button-validate-confirm-title"),
		    Messages.getMessage("bpc-gui.button-validate-confirm-message"));
	    if (result == JOptionPane.YES_OPTION) {
		validateButtonClicked();
	    }
	});

	cancelButton = new JButton(Messages.getMessage("bpc-gui.button-cancel"));
	cancelButton.addActionListener((final ActionEvent e) -> {
	    final int result = Dialogs.showConfirmDialog(scoop3Frame,
		    Messages.getMessage("bpc-gui.button-cancel-confirm-title"),
		    Messages.getMessage("bpc-gui.button-cancel-confirm-message"));
	    if (result == JOptionPane.YES_OPTION) {
		cancelButtonClicked();
	    }
	});
	buttonsPanel.add(cancelButton);

	return buttonsPanel;
    }

    /**
     * @return la largeur disponible en pixel pour la partie est de l'IHM
     */
    protected int getEastPanelAvailableHeight() {
	return scoop3Frame.getHeight() - TOOLBAR_HEIGHT;
    }

    /**
     * @return la largeur disponible en pixel pour la partie est de l'IHM
     */
    protected int getEastPanelAvailableWidth() {
	return scoop3Frame.getWidth() - mainSplitPane.getDividerLocation();
    }

    /**
     * @return the globalWestPanel
     */
    protected JSplitPane getGlobalWestJSplitPane() {
	return westMapJSplitPane;
    }

    protected JPanel getInfoPanel() {
	return new JPanel();
    }

    /**
     * @return the mainSplitPane
     */
    protected JSplitPane getMainJSplitPane() {
	return mainSplitPane;
    }

    /**
     * @return the mainSplitPane
     */
    public JSplitPane getCenterSplitPane() {
	return centerSplitPane;
    }

    /**
     * set the mainSplitPane
     */
    public void setCenterSplitPane(final JSplitPane centerSplitPane) {
	this.centerSplitPane = centerSplitPane;
    }

    /**
     * Create the map ribbon band
     */
    protected JRibbonBand getMapRibbonBand() {

	final JRibbonBand mapRibbonBand = new JRibbonBand(Messages.getMessage("bpc-gui.ribbon-navigation"), null);

	initMapButtonsAndAddInRibonBand(mapRibbonBand, true, true, true, true, true, true, true);

	final List<RibbonBandResizePolicy> resizePolicies = new ArrayList<RibbonBandResizePolicy>();
	resizePolicies.add(new CoreRibbonResizePolicies.Mirror(mapRibbonBand.getControlPanel()));
	resizePolicies.add(new IconRibbonBandResizePolicy(mapRibbonBand.getControlPanel()));
	mapRibbonBand.setResizePolicies(resizePolicies);

	return mapRibbonBand;
    }

    /**
     * @param ribbonBand
     */
    protected void initMapButtonsAndAddInRibonBand(final JRibbonBand ribbonBand, final boolean addMapZoomRectButton,
	    final boolean addMapZoomInButton, final boolean addMapZoomOutButton, final boolean addMapZoomInitialButton,
	    final boolean addMapZoomWorldButton, final boolean addMapLinkUnlinkButton,
	    final boolean addMapShowHideLabelButton) {

	if (addMapZoomRectButton) {
	    mapZoomRectButton = addJCommandToggleButtonToRibbonBand(ribbonBand, "icons/zoom_fit_best.png",
		    "bpc-gui.ribbon-zoom_rectangle", RibbonElementPriority.TOP);
	}
	if (addMapZoomInButton) {
	    mapZoomInButton = addJCommandButtonToRibbonBand(ribbonBand, "icons/zoom_in.png", "bpc-gui.ribbon-zoom_in",
		    RibbonElementPriority.LOW);
	}
	if (addMapZoomOutButton) {
	    mapZoomOutButton = addJCommandButtonToRibbonBand(ribbonBand, "icons/zoom_out.png",
		    "bpc-gui.ribbon-zoom_out", RibbonElementPriority.LOW);
	}
	if (addMapZoomInitialButton) {
	    mapZoomInitialButton = addJCommandButtonToRibbonBand(ribbonBand, "icons/zoom_original.png",
		    "bpc-gui.ribbon-zoom_initial", RibbonElementPriority.LOW);
	}
	if (addMapZoomWorldButton) {
	    mapZoomWorldButton = addJCommandButtonToRibbonBand(ribbonBand, "icons/network.png",
		    "bpc-gui.ribbon-zoom_world", RibbonElementPriority.LOW);
	}
	if (addMapLinkUnlinkButton) {
	    mapLinkUnlinkButton = addJCommandButtonToRibbonBand(ribbonBand, "icons/linkButton.jpg",
		    "bpc-gui.ribbon-link_observation", RibbonElementPriority.LOW);
	}
	if (addMapShowHideLabelButton) {
	    mapShowHideLabelButton = addJCommandButtonToRibbonBand(ribbonBand, "icons/123.png",
		    "bpc-gui.ribbon-show_observation_label", RibbonElementPriority.LOW);
	}

	/*
	 * Add tooltips
	 */
	if (addMapZoomInButton) {
	    mapZoomInButton.setActionRichTooltip(new RichTooltip(Messages.getMessage("bpc-gui.ribbon-navigation"),
		    Messages.getMessage("bpc-gui.ribbon-zoom_in")));
	}
	if (addMapZoomOutButton) {
	    mapZoomOutButton.setActionRichTooltip(new RichTooltip(Messages.getMessage("bpc-gui.ribbon-navigation"),
		    Messages.getMessage("bpc-gui.ribbon-zoom_out")));
	}
	if (addMapZoomInitialButton) {
	    mapZoomInitialButton.setActionRichTooltip(new RichTooltip(Messages.getMessage("bpc-gui.ribbon-navigation"),
		    Messages.getMessage("bpc-gui.ribbon-zoom_initial")));
	}
	if (addMapZoomWorldButton) {
	    mapZoomWorldButton.setActionRichTooltip(new RichTooltip(Messages.getMessage("bpc-gui.ribbon-navigation"),
		    Messages.getMessage("bpc-gui.ribbon-zoom_world")));
	}
	if (addMapLinkUnlinkButton) {
	    mapLinkUnlinkButton.setActionRichTooltip(new RichTooltip(Messages.getMessage("bpc-gui.ribbon-navigation"),
		    Messages.getMessage("bpc-gui.ribbon-link_observation")));
	}
	if (addMapShowHideLabelButton) {
	    mapShowHideLabelButton
		    .setActionRichTooltip(new RichTooltip(Messages.getMessage("bpc-gui.ribbon-navigation"),
			    Messages.getMessage("bpc-gui.ribbon-show_observation_label")));
	}
    }

    protected JPanel getMemoryGaugePanel() {
	final JPanel gaugePanel = new JPanel();
	memoryProgressBar.setStringPainted(true);
	final int maxInMB = (Runtime.getRuntime().maxMemory() == Long.MAX_VALUE) ? 0
		: Math.round(Runtime.getRuntime().maxMemory() / (float) 1024 / 1024);
	memoryProgressBar.setMaximum(maxInMB);
	gaugePanel.add(memoryProgressBar);
	return gaugePanel;
    }

    /**
     * Create the report ribbon band
     */
    protected JRibbonBand getReportActionBand() {
	final JRibbonBand reportRibbonBand = new JRibbonBand(Messages.getMessage("gui.ribbon-report"), null);

	final URL resource = getClass().getClassLoader().getResource("icons/format_list_ordered.png");
	reportButton = new JCommandButton(Messages.getMessage("gui.ribbon-report"),
		ImageWrapperResizableIcon.getIcon(resource, new Dimension(32, 32)));
	reportButton.addActionListener((final ActionEvent ae) -> ReportJDialog
		.getReportJDialog(scoop3Frame, CommonViewImpl.this, report).setVisible(true));
	reportRibbonBand.addCommandButton(reportButton, RibbonElementPriority.TOP);

	final List<RibbonBandResizePolicy> resizePolicies = new ArrayList<RibbonBandResizePolicy>();
	resizePolicies.add(new CoreRibbonResizePolicies.Mirror(reportRibbonBand.getControlPanel()));
	resizePolicies.add(new CoreRibbonResizePolicies.Mid2Low(reportRibbonBand.getControlPanel()));
	resizePolicies.add(new IconRibbonBandResizePolicy(reportRibbonBand.getControlPanel()));
	reportRibbonBand.setResizePolicies(resizePolicies);

	return reportRibbonBand;
    }

    /**
     * Create the Ribbon Band for the Graphs buttons
     *
     * @return
     */
    protected JRibbonBand getUndoRedoRibbonBand() {
	final JRibbonBand ribbonBand = new JRibbonBand(Messages.getMessage("bpc-gui.ribbon-undo-redo"), null);

	undoButton = addJCommandButtonToRibbonBand(ribbonBand, "icons/undo.png", "bpc-gui.ribbon-undo",
		RibbonElementPriority.TOP);

	redoButton = addJCommandButtonToRibbonBand(ribbonBand, "icons/redo.png", "bpc-gui.ribbon-redo",
		RibbonElementPriority.TOP);

	undoAllButton = addJCommandButtonToRibbonBand(ribbonBand, "icons/undo_all.png", "bpc-gui.ribbon-undo-all",
		RibbonElementPriority.TOP);

	final List<RibbonBandResizePolicy> resizePolicies = new ArrayList<RibbonBandResizePolicy>();
	resizePolicies.add(new CoreRibbonResizePolicies.Mirror(ribbonBand.getControlPanel()));
	resizePolicies.add(new IconRibbonBandResizePolicy(ribbonBand.getControlPanel()));
	ribbonBand.setResizePolicies(resizePolicies);

	return ribbonBand;
    }

    /**
     * Unload data to save memory (in specific Controller)
     */
    protected abstract void specificPrepareForDispose();

    /**
     * Method called when the Validate button is clicked and confirmed
     */
    protected abstract void validateButtonClicked();

    /**
     * @return the mapPanel
     */
    protected JPanel getMapPanel() {
	return mapPanel;
    }

    public abstract void updateAllProfilesIcons(final boolean allProfileIsActive);

    public void setMaximumSpinnerNextValue(final int maximumSpinnerNextValue) {
    }

    public void setMaximumSpinnerPrevValue(final int maximumSpinnerPrevValue) {
    }

    public JSpinner getjSpinnerNext() {
	return null;
    }

    public JSpinner getjSpinnerPrev() {
	return null;
    }

    public JPanel getInfoInObservationSubPanel() {
	return this.infoInObservationSubPanel;
    }

    public enum FlagMode {
	ALL_OBS, // All visible Observations will be flagged
	CURRENT_OBS_ONLY, // Only the current Observation will be flagged
    }

    public FlagMode getFlagMode() {
	return null;
    }

    public JLabel getInfoLabel() {
	return new JLabel();
    }
}
