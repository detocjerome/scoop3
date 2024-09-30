/**
 *
 */
package fr.ifremer.scoop3.gui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.NumberFormatter;

import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.data.SuperposedModeEnum;
import fr.ifremer.scoop3.gui.data.ChartPanelWithComboBox;
import fr.ifremer.scoop3.gui.data.DataViewController;
import fr.ifremer.scoop3.gui.utils.Dialogs;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.Platform;
import fr.ifremer.scoop3.model.valueAndQc.ValueAndQC;
import javafx.geometry.Rectangle2D;

public class MetadataSplitPane extends JSplitPane {

    protected static final Dimension BUTTON_PREF_DIM = new Dimension(25, 25);
    private static final long serialVersionUID = 2728543537167180519L;
    protected static final Dimension PANEL_PREF_DIM = new Dimension(300, 300);

    // private JButton backButton;
    // protected JButton firstObsButton;
    // private JButton forwardButton;
    // private JLabel indexLabel;
    // private JFormattedTextField indexTextField;
    private JPanel infoInObservationPanel;
    private JPanel infoInObservationSubPanel;
    // protected JButton lastObsButton;
    protected JPanel observationPanel;
    protected JLabel referenceLabel;
    protected transient CommonViewController commonViewController;
    protected transient DataOrReferenceViewController dataOrReferenceViewController;
    protected MetadataTable datasetMetadatasTable;
    protected JPanel datasetPanel;
    protected MetadataTable observationMetadatasTable;
    protected JButton validateButton;
    protected JLabel cruiseLabel;

    protected Double keepBoundsMinY = null;
    protected Double keepBoundsMaxY = null;

    private transient DataViewController dataViewController;

    public MetadataSplitPane(final MetadataTable datasetMetadatasTable, final MetadataTable observationMetadatasTable) {
	super(JSplitPane.VERTICAL_SPLIT);

	this.datasetMetadatasTable = datasetMetadatasTable;
	this.observationMetadatasTable = observationMetadatasTable;

	if (datasetMetadatasTable != null) {
	    datasetMetadatasTable.setMetadataSplitPane(this);
	}
	observationMetadatasTable.setMetadataSplitPane(this);

	initDatasetPanel();
	initObservationPanel();
	setDividerSize(8);
	// Add arrows (up and down) in the divider
	setOneTouchExpandable(true);
	// Set the divider to the middle of the JSplitPane
	setResizeWeight(0.5);
    }

    protected MetadataSplitPane() {
	super(JSplitPane.VERTICAL_SPLIT);
    }

    /**
     * Backup updates if needed.
     *
     * @param saveFile
     *
     * @return TRUE if at least one update has been done by the operator.
     */
    public boolean backupUpdates(final boolean saveFile) {
	boolean toReturn = (datasetMetadatasTable != null) && datasetMetadatasTable.backupUpdates(saveFile);
	toReturn |= (observationMetadatasTable != null) && observationMetadatasTable.backupUpdates(saveFile);
	return toReturn;
    }

    /**
     * Check if there is at least 1 QC_4 in the metadata
     *
     * @return
     */
    public boolean containsAtLeastOneMetadataWithQC4() {
	boolean toReturn = (datasetMetadatasTable != null) && datasetMetadatasTable.containsAtLeastOneMetadataWithQC4();
	toReturn |= (observationMetadatasTable != null)
		&& observationMetadatasTable.containsAtLeastOneMetadataWithQC4();
	return toReturn;
    }

    /**
     * Unload data to save memory
     */
    public void prepareForDispose() {
	commonViewController = null;
	dataOrReferenceViewController = null;
	((InfoInObservationSubPanel) infoInObservationSubPanel).getFirstObsButton().removeActionListener(
		((InfoInObservationSubPanel) infoInObservationSubPanel).getFirstObsButton().getActionListeners()[0]);
	((InfoInObservationSubPanel) infoInObservationSubPanel).setFirstObsButton(null);
	((InfoInObservationSubPanel) infoInObservationSubPanel).getBackButton().removeActionListener(
		((InfoInObservationSubPanel) infoInObservationSubPanel).getBackButton().getActionListeners()[0]);
	((InfoInObservationSubPanel) infoInObservationSubPanel).setBackButton(null);
	((InfoInObservationSubPanel) infoInObservationSubPanel).getForwardButton().removeActionListener(
		((InfoInObservationSubPanel) infoInObservationSubPanel).getForwardButton().getActionListeners()[0]);
	((InfoInObservationSubPanel) infoInObservationSubPanel).setForwardButton(null);
	((InfoInObservationSubPanel) infoInObservationSubPanel).getLastObsButton().removeActionListener(
		((InfoInObservationSubPanel) infoInObservationSubPanel).getLastObsButton().getActionListeners()[0]);
	((InfoInObservationSubPanel) infoInObservationSubPanel).setLastObsButton(null);
	if (datasetMetadatasTable != null) {
	    datasetMetadatasTable.removeAll();
	}
	datasetMetadatasTable = null;
	if (observationMetadatasTable != null) {
	    observationMetadatasTable.removeAll();
	}
	observationMetadatasTable = null;
	if (datasetPanel != null) {
	    datasetPanel.removeAll();
	}
	datasetPanel = null;
	observationPanel.removeAll();
	observationPanel = null;
    }

    /**
     */
    public void selectedNextObservationWithButton(final boolean withKeyboard) {
	int observationIndex = commonViewController.getObservationNumber();
	if (withKeyboard && (commonViewController
		.getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)) {
	    if (commonViewController.getLastObservationIndexForCurrentPlatform() == observationIndex) {
		observationIndex = commonViewController.getFirstObservationIndexForCurrentPlatform();
	    } else {
		observationIndex++;
	    }
	} else {
	    if (observationIndex < (commonViewController.getMaxObservationsNumber() - 1)) {
		observationIndex++;
	    } else {
		observationIndex = 0;
	    }
	}
	if (((InfoInObservationSubPanel) infoInObservationSubPanel).getForwardButton().isEnabled()) {
	    newObservationSelectedWithButton(observationIndex);
	} else {
	    // update the metadatas of the current station or location
	    commonViewController.setSelectedObservation(observationIndex);
	}

	// special case for Scoop3Explorer - get Metadatas of dataset for each platform and refresh the
	// datasetMetadataPanel
	updateDatasetMetadataPanel(observationIndex);

	if (dataOrReferenceViewController != null) {
	    dataOrReferenceViewController.updateMouseCursor();
	}
    }

    /**
     */
    public void selectedPrevObservationWithButton(final boolean withKeyboard) {
	int observationIndex = commonViewController.getObservationNumber();
	if (withKeyboard && (commonViewController
		.getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)) {
	    if (commonViewController.getFirstObservationIndexForCurrentPlatform() == observationIndex) {
		observationIndex = commonViewController.getLastObservationIndexForCurrentPlatform();
	    } else {
		observationIndex--;
	    }
	} else {
	    if (observationIndex > 0) {
		observationIndex--;
	    } else {
		observationIndex = commonViewController.getMaxObservationsNumber() - 1;
	    }
	}
	if (((InfoInObservationSubPanel) infoInObservationSubPanel).getBackButton().isEnabled()) {
	    newObservationSelectedWithButton(observationIndex);
	}

	// special case for Scoop3Explorer - get Metadatas of dataset for each platform and refresh the
	// datasetMetadataPanel
	updateDatasetMetadataPanel(observationIndex);

	if (dataOrReferenceViewController != null) {
	    dataOrReferenceViewController.updateMouseCursor();
	}
    }

    /**
     * @param commonViewController
     *            the commonViewController to set
     */
    public void setCommonViewController(final CommonViewController commonViewController) {
	this.commonViewController = commonViewController;
    }

    /**
     * @param commonViewController
     *            the commonViewController to set
     */
    public void setDataOrReferenceViewController(final DataOrReferenceViewController dataOrReferenceViewController) {
	this.dataOrReferenceViewController = dataOrReferenceViewController;
    }

    /**
     * Update the enabled state of the buttons First Obs, Back, Forward and Last Obs
     */
    public void updateEnabledButtons() {
	final int observationCount = commonViewController.getMaxObservationsNumber();
	if (observationCount <= 1) {
	    ((InfoInObservationSubPanel) infoInObservationSubPanel).getBackButton().setEnabled(false);
	    ((InfoInObservationSubPanel) infoInObservationSubPanel).getForwardButton().setEnabled(false);
	    ((InfoInObservationSubPanel) infoInObservationSubPanel).getFirstObsButton().setEnabled(false);
	    ((InfoInObservationSubPanel) infoInObservationSubPanel).getLastObsButton().setEnabled(false);
	    ((InfoInObservationSubPanel) commonViewController.getCommonViewImpl().getInfoInObservationSubPanel())
		    .getBackButton().setEnabled(false);
	    ((InfoInObservationSubPanel) commonViewController.getCommonViewImpl().getInfoInObservationSubPanel())
		    .getForwardButton().setEnabled(false);
	    ((InfoInObservationSubPanel) commonViewController.getCommonViewImpl().getInfoInObservationSubPanel())
		    .getFirstObsButton().setEnabled(false);
	    ((InfoInObservationSubPanel) commonViewController.getCommonViewImpl().getInfoInObservationSubPanel())
		    .getLastObsButton().setEnabled(false);
	} else {
	    ((InfoInObservationSubPanel) infoInObservationSubPanel).getBackButton().setEnabled(true);
	    ((InfoInObservationSubPanel) infoInObservationSubPanel).getForwardButton().setEnabled(true);
	    ((InfoInObservationSubPanel) infoInObservationSubPanel).getFirstObsButton().setEnabled(true);
	    ((InfoInObservationSubPanel) infoInObservationSubPanel).getLastObsButton().setEnabled(true);
	    ((InfoInObservationSubPanel) commonViewController.getCommonViewImpl().getInfoInObservationSubPanel())
		    .getBackButton().setEnabled(true);
	    ((InfoInObservationSubPanel) commonViewController.getCommonViewImpl().getInfoInObservationSubPanel())
		    .getForwardButton().setEnabled(true);
	    ((InfoInObservationSubPanel) commonViewController.getCommonViewImpl().getInfoInObservationSubPanel())
		    .getFirstObsButton().setEnabled(true);
	    ((InfoInObservationSubPanel) commonViewController.getCommonViewImpl().getInfoInObservationSubPanel())
		    .getLastObsButton().setEnabled(true);
	}
    }

    /**
     * Update the Observation displayed info
     *
     * @param observation
     */
    public void updateObservationMetadatas(final Observation observation,
	    final InfoInObservationSubPanel infoInObservationSubPanel, final boolean updateTableWithObservation) {
	referenceLabel.setText(observation.getReference());

	if (infoInObservationSubPanel.getIndexTextField().getFormatterFactory() == null) {
	    final NumberFormatter nf = new NumberFormatter();
	    nf.setMinimum(1);
	    nf.setMaximum(commonViewController.getMaxObservationsNumber());
	    infoInObservationSubPanel.getIndexTextField().setFormatterFactory(new DefaultFormatterFactory(nf));
	} else {
	    // change maximum range due to the dynamic dataset's load
	    ((InternationalFormatter) infoInObservationSubPanel.getIndexTextField().getFormatter())
		    .setMaximum(commonViewController.getMaxObservationsNumber());
	}

	infoInObservationSubPanel.getIndexTextField().setBackground(null);
	infoInObservationSubPanel.getIndexLabel().setText(" / " + commonViewController.getMaxObservationsNumber());
	if (updateTableWithObservation) {
	    infoInObservationSubPanel.getIndexTextField()
		    .setText(String.valueOf(commonViewController.getObservationNumber() + 1));
	    if (observationMetadatasTable != null) {
		observationMetadatasTable.updateTableWithObservation(observation);
	    }

	    computePanelSize(observationPanel, observationMetadatasTable, 30);
	}
    }

    /**
     * Update the Dataset displayed info
     *
     * @param dataset
     */
    public void updateTableWithDataset(final Dataset dataset) {
	if (datasetMetadatasTable != null) {
	    datasetMetadatasTable.updateTableWithDataset(dataset);

	    computePanelSize(datasetPanel, datasetMetadatasTable, 0);
	}
    }

    /**
     * Compute the Panel size
     *
     * @param jScrollPane
     *
     * @param metadataTable
     * @param panel
     *
     */
    protected void computePanelSize(final JPanel panel, final MetadataTable metadataTable, final int topPanelHeigth) {
	final Dimension actualSize = panel.getSize();
	if (actualSize.width == 0) {
	    actualSize.width = PANEL_PREF_DIM.width;
	}
	// Top heigth
	actualSize.height = topPanelHeigth;
	// Table heigth
	if (metadataTable != null) {
	    actualSize.height += metadataTable.getRowCount()
		    * (metadataTable.getRowHeight() + metadataTable.getRowMargin() + 1);
	}
	panel.setPreferredSize(actualSize);
    }

    /**
     * Create and Add First Obs Button
     */
    protected JButton initFirstObsButton(JButton firstObsButton) {
	final URL resourceBack = getClass().getClassLoader().getResource("icons/2leftarrow.png");
	firstObsButton = new JButton(new ImageIcon(resourceBack));
	firstObsButton.setPreferredSize(BUTTON_PREF_DIM);

	firstObsButton.addActionListener((final ActionEvent e) -> {
	    final int observationIndex = commonViewController.getFirstObservationIndexForPreviousPlatform();
	    newObservationSelectedWithButton(observationIndex);

	    // special case for Scoop3Explorer - get Metadatas of dataset for each platform and refresh
	    // the datasetMetadataPanel
	    updateDatasetMetadataPanel(commonViewController.getObservationNumber());
	});

	return firstObsButton;
    }

    /**
     * Create and Add Last Obs Button
     */
    protected JButton initLastObsButton(JButton lastObsButton) {
	final URL resourceForward = getClass().getClassLoader().getResource("icons/2rightarrow.png");
	lastObsButton = new JButton(new ImageIcon(resourceForward));
	lastObsButton.setPreferredSize(BUTTON_PREF_DIM);

	lastObsButton.addActionListener((final ActionEvent e) -> {
	    final int observationIndex = commonViewController.getLastObservationIndexForDataset();
	    newObservationSelectedWithButton(observationIndex);

	    // special case for Scoop3Explorer - get Metadatas of dataset for each platform and refresh
	    // the datasetMetadataPanel
	    updateDatasetMetadataPanel(commonViewController.getObservationNumber());
	});

	return lastObsButton;
    }

    /**
     * @return TRUE if the reference label is added to the split pane
     */
    protected boolean displayReferenceLabel() {
	return true;
    }

    /**
     * @return the title of the Dataset Metadata panel
     */
    public JLabel getDatasetTitle() {
	return new JLabel(Messages.getMessage("gui.metadata-dataset-label"));
    }

    protected JPanel getIndexPanelForInfoInObservationPanel(final InfoInObservationSubPanel infoInObservationSubPanel) {
	final JPanel indexPanel = new JPanel();
	indexPanel.setBackground(null);
	indexPanel.add(infoInObservationSubPanel.getIndexTextField());
	indexPanel.add(infoInObservationSubPanel.getIndexLabel());
	return indexPanel;
    }

    /**
     * @return the infoInObservationPanel
     */
    public JPanel getInfoInObservationPanel() {
	return infoInObservationPanel;
    }

    /**
     * @return the infoInObservationSubPanel
     */
    public JPanel getInfoInObservationSubPanel() {
	return infoInObservationSubPanel;
    }

    /**
     * Init Dataset Metadata panel
     */
    protected void initDatasetPanel() {
	if (datasetMetadatasTable != null) {
	    datasetPanel = new JPanel();
	    datasetPanel.setLayout(new BorderLayout(5, 5));
	    datasetPanel.setBackground(Color.WHITE);
	    datasetPanel.setPreferredSize(PANEL_PREF_DIM);

	    final JLabel cruiseLabel = getDatasetTitle();
	    final JPanel labelPanel = new JPanel();
	    labelPanel.add(cruiseLabel);
	    labelPanel.setBackground(Color.WHITE);
	    datasetPanel.add(labelPanel, BorderLayout.NORTH);

	    // Only the JTable is in the JScrollPane
	    final JScrollPane datasetJScrollPane = new JScrollPane(datasetMetadatasTable);
	    datasetJScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    datasetJScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

	    datasetPanel.add(datasetJScrollPane, BorderLayout.CENTER);

	    add(datasetPanel);
	}
    }

    /**
     * Init the Info Observation Metadata panel
     */
    protected void initInfoObservationPanel() {
	infoInObservationPanel = new JPanel();
	infoInObservationPanel.setBackground(Color.WHITE);
	infoInObservationPanel.setLayout(new BorderLayout());

	infoInObservationSubPanel = new InfoInObservationSubPanel(new BorderLayout());
	infoInObservationSubPanel.setBackground(Color.WHITE);
	infoInObservationPanel.add(infoInObservationSubPanel, BorderLayout.CENTER);

	/*
	 * FIRST LINE IS THE STATION REF LABEL (is exists)
	 */
	// Add Observation reference label
	referenceLabel = new JLabel();
	if (displayReferenceLabel()) {
	    final JPanel refPanel = new JPanel();
	    refPanel.setBackground(Color.WHITE);
	    refPanel.add(referenceLabel);
	    infoInObservationPanel.add(refPanel, BorderLayout.NORTH);
	}
    }

    public class InfoInObservationSubPanel extends JPanel {

	private static final long serialVersionUID = -7617895501110634379L;
	private final JFormattedTextField indexTextField;
	private final JLabel indexLabel;
	private JButton backButton;
	protected JButton firstObsButton;
	private JButton forwardButton;
	protected JButton lastObsButton;
	protected JButton validateButton;

	/**
	 * Create and Add Back Button
	 */
	private JButton initBackButton(JButton backButton) {
	    final URL resourceBack = getClass().getClassLoader().getResource("icons/1leftarrow.png");
	    backButton = new JButton(new ImageIcon(resourceBack));
	    backButton.setPreferredSize(BUTTON_PREF_DIM);

	    backButton.addActionListener((final ActionEvent e) -> selectedPrevObservationWithButton(false));

	    return backButton;
	}

	/**
	 * Create and Add Forward Button
	 */
	private JButton initForwardButton(JButton forwardButton) {
	    final URL resourceForward = getClass().getClassLoader().getResource("icons/1rightarrow.png");
	    forwardButton = new JButton(new ImageIcon(resourceForward));
	    forwardButton.setPreferredSize(BUTTON_PREF_DIM);

	    forwardButton.addActionListener((final ActionEvent e) -> selectedNextObservationWithButton(false));

	    return forwardButton;
	}

	public InfoInObservationSubPanel(final BorderLayout borderLayout) {
	    super(borderLayout);
	    firstObsButton = initFirstObsButton(firstObsButton);
	    backButton = initBackButton(backButton);

	    // to create the field
	    indexTextField = new JFormattedTextField();

	    // TODO HERE VERIF

	    indexTextField.setColumns(4);
	    indexTextField.addFocusListener(new FocusAdapter() {
		/*
		 * (non-Javadoc)
		 *
		 * @see java.awt.event.FocusAdapter#focusLost(java.awt.event.FocusEvent)
		 */
		@Override
		public void focusLost(final FocusEvent e) {
		    super.focusLost(e);
		    int index = 0;
		    boolean errorInSelection = false;
		    final int lastIndex = commonViewController.getObservationNumber();
		    try {
			index = Integer.parseInt(indexTextField.getText()) - 1;
			// check if index is in fair range
			checkIndexAsked(index);
		    } catch (final Exception e1) {
			if (commonViewController != null) {
			    if (commonViewController
				    .getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET) {
				final int currentObs = commonViewController.getObservationNumber();
				final Platform currentPlatform = commonViewController.getCommonViewModel()
					.getPlatformForObservation(currentObs + 1);
				for (int i = 0; i < commonViewController.getCommonViewModel().getDataset()
					.getPlatforms().size(); i++) {
				    if (commonViewController.getCommonViewModel().getDataset().getPlatforms()
					    .get(i) == currentPlatform) {
					index = i;
				    }
				}
			    } else {
				index = commonViewController.getObservationNumber();
			    }
			    indexTextField.setText(String.valueOf(index + 1));
			    Dialogs.showErrorMessage(Messages.getMessage("Erreur"), "Wrong index");
			    errorInSelection = true;
			}
		    }

		    // select a new observation, depends on the version of the software, Coriolis is different than ARGO
		    // and BPC
		    selectNewObservation(index, lastIndex, errorInSelection);

		    // special case for Scoop3Explorer - get Metadatas of dataset for each platform and refresh
		    // the datasetMetadataPanel
		    updateDatasetMetadataPanel(commonViewController.getObservationNumber());
		}
	    });
	    indexTextField.addKeyListener(new KeyAdapter() {
		/*
		 * (non-Javadoc)
		 *
		 * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
		 */
		@Override
		public void keyReleased(final KeyEvent e) {
		    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			// Force to loose focus on indexTextField
			commonViewController.getCommonViewImpl().getDisplayComponent().requestFocusInWindow();
		    }
		}
	    });

	    indexLabel = new JLabel();

	    forwardButton = initForwardButton(forwardButton);
	    lastObsButton = initLastObsButton(lastObsButton);

	    final JPanel leftButtonsPanel = new JPanel();
	    leftButtonsPanel.setBackground(null);
	    leftButtonsPanel.add(firstObsButton);
	    leftButtonsPanel.add(backButton);
	    // Center the vertically with the buttons
	    final JPanel leftButtonsDblPanel = new JPanel(new GridBagLayout());
	    leftButtonsDblPanel.setBackground(null);
	    leftButtonsDblPanel.add(leftButtonsPanel, new GridBagConstraints());
	    this.add(leftButtonsDblPanel, BorderLayout.WEST);

	    this.add(getIndexPanelForInfoInObservationPanel(this), BorderLayout.CENTER);

	    final JPanel rightButtonsPanel = new JPanel();
	    rightButtonsPanel.setBackground(null);
	    rightButtonsPanel.add(forwardButton);
	    rightButtonsPanel.add(lastObsButton);
	    // Center the vertically with the buttons
	    final JPanel rightButtonsDblPanel = new JPanel(new GridBagLayout());
	    rightButtonsDblPanel.setBackground(null);
	    rightButtonsDblPanel.add(rightButtonsPanel, new GridBagConstraints());
	    this.add(rightButtonsDblPanel, BorderLayout.EAST);
	}

	public JLabel getIndexLabel() {
	    return this.indexLabel;
	}

	public JFormattedTextField getIndexTextField() {
	    return this.indexTextField;
	}

	public JButton getBackButton() {
	    return backButton;
	}

	public JButton getForwardButton() {
	    return forwardButton;
	}

	public JButton getFirstObsButton() {
	    return firstObsButton;
	}

	public JButton getLastObsButton() {
	    return lastObsButton;
	}

	public JButton getValidateButton() {
	    return validateButton;
	}

	public void setBackButton(final JButton backButton) {
	    this.backButton = backButton;
	}

	public void setForwardButton(final JButton forwardButton) {
	    this.forwardButton = forwardButton;
	}

	public void setFirstObsButton(final JButton firstObsButton) {
	    this.firstObsButton = firstObsButton;
	}

	public void setLastObsButton(final JButton lastObsButton) {
	    this.lastObsButton = lastObsButton;
	}

	public void setValidateButton(final JButton validateButton) {
	    this.validateButton = validateButton;
	}

    }

    /**
     * Init Observation Metadata panel
     */
    protected void initObservationPanel() {
	observationPanel = new JPanel();
	observationPanel.setBackground(Color.WHITE);
	observationPanel.setLayout(new BorderLayout());
	observationPanel.setPreferredSize(PANEL_PREF_DIM);

	initInfoObservationPanel();

	observationPanel.add(infoInObservationPanel, BorderLayout.NORTH);

	// Only the JTable is in the JScrollPane
	final JScrollPane observationJScrollPane = new JScrollPane(observationMetadatasTable);
	observationJScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	observationJScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

	observationPanel.add(observationJScrollPane, BorderLayout.CENTER);

	if (observationMetadatasTable != null) {
	    final JPanel buttonsPanel = observationMetadatasTable.getButtonsPanel();
	    if (buttonsPanel != null) {
		observationPanel.add(buttonsPanel, BorderLayout.SOUTH);
	    }
	}
	add(observationPanel);
    }

    /**
     * @param observationIndex
     */
    public void newObservationSelectedWithButton(final int observationIndex) {
	Double localKeepBoundsMinY = null;
	Double localKeepBoundsMaxY = null;
	if (dataOrReferenceViewController != null) {
	    localKeepBoundsMinY = saveKeepBoundsMinY(dataOrReferenceViewController);
	    localKeepBoundsMaxY = saveKeepBoundsMaxY(dataOrReferenceViewController);
	}

	// if there is a shift in progress, reset the shift before change the current observation
	if (JScoop3ChartScrollPaneAbstract.getCoefX() != 0) {
	    JScoop3ChartScrollPaneAbstract.setCoefX(0);
	}

	commonViewController.setObservationNumberAndSend(observationIndex);
	updateEnabledButtons();
	commonViewController.setSelectedObservation(observationIndex);
	commonViewController.setSelectedObservationOnSpecificPanel(observationIndex);

	if (dataOrReferenceViewController != null) {
	    keepBoundsForOtherObservation(dataOrReferenceViewController, true, localKeepBoundsMinY,
		    localKeepBoundsMaxY);
	}
    }

    /**
     * @param dataViewController
     *            the dataViewController to set
     */
    public void setDataViewController(final DataViewController dataViewController) {
	this.dataViewController = dataViewController;
    }

    /**
     * @return the validateButton
     */
    public JButton getValidateButton() {
	return validateButton;
    }

    public DataOrReferenceViewController getDataOrReferenceViewController() {
	return this.dataOrReferenceViewController;
    }

    public void checkIndexAsked(final int index) throws Exception {
	if ((index < 0) || (index >= commonViewController.getMaxObservationsNumber())) {
	    throw new Exception();
	}
    }

    public void selectNewObservation(final int index, final int lastIndex, boolean errorInSelection) {
	if (commonViewController.getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET) {
	    if (errorInSelection) {
		if ((index - 1) >= 0) {
		    newObservationSelectedWithButton(index - 1);
		} else {
		    newObservationSelectedWithButton(lastIndex);
		}
	    } else {
		newObservationSelectedWithButton(index);
	    }
	} else {
	    newObservationSelectedWithButton(index);
	}
	errorInSelection = false;
    }

    public MetadataTable getDatasetMetadatasTable() {
	return this.datasetMetadatasTable;
    }

    public MetadataTable getObservationMetadatasTable() {
	return this.observationMetadatasTable;
    }

    private void updateDatasetMetadataPanel(final int observationIndex) {
	if (datasetMetadatasTable != null) {
	    final List<HashMap<String, ValueAndQC>> metadatas = commonViewController.getCommonViewModel()
		    .getMetadatas();
	    final String platformCode = commonViewController.getCommonViewModel().getObservation(observationIndex)
		    .getSensor().getPlatform().getCode();
	    datasetMetadatasTable.triggerUpdateTableWithDataset(commonViewController.getCommonViewModel().getDataset(),
		    platformCode, metadatas);
	}
    }

    protected Double saveKeepBoundsMinY(final DataOrReferenceViewController dataOrReferenceViewController) {
	// get min y bound on the old graph or if null from previous variable
	if (dataOrReferenceViewController.getListGraphs() != null) {
	    Double localKeepBoundsMinY = dataOrReferenceViewController.getListGraphs().get(0).getScoop3ChartPanel()
		    .getjScoop3ChartScrollPane().getKeepBoundsMinY();
	    if (localKeepBoundsMinY == null) {
		localKeepBoundsMinY = keepBoundsMinY;
	    } else {
		keepBoundsMinY = localKeepBoundsMinY;
	    }

	    return localKeepBoundsMinY;
	} else {
	    return null;
	}
    }

    protected Double saveKeepBoundsMaxY(final DataOrReferenceViewController dataOrReferenceViewController) {
	// get max y bound on the old graph or if null from previous variable
	if (dataOrReferenceViewController.getListGraphs() != null) {
	    Double localKeepBoundsMaxY = dataOrReferenceViewController.getListGraphs().get(0).getScoop3ChartPanel()
		    .getjScoop3ChartScrollPane().getKeepBoundsMaxY();
	    if (localKeepBoundsMaxY == null) {
		localKeepBoundsMaxY = keepBoundsMaxY;
	    } else {
		keepBoundsMaxY = localKeepBoundsMaxY;
	    }

	    return localKeepBoundsMaxY;
	} else {
	    return null;
	}
    }

    protected void keepBoundsForOtherObservation(final DataOrReferenceViewController dataOrReferenceViewController,
	    final boolean refreshGraphs, final Double localKeepBoundsMinY, final Double localKeepBoundsMaxY) {
	if (!dataOrReferenceViewController.getKeepBounds()) {
	    if (dataOrReferenceViewController.getListGraphs() != null) {
		final ArrayList<ChartPanelWithComboBox> tempListGraphs = new ArrayList<ChartPanelWithComboBox>(
			dataOrReferenceViewController.getListGraphs());
		for (final ChartPanelWithComboBox chartPanelWithComboBox : tempListGraphs) {
		    // refresh the new observation graphs
		    // if (dataOrReferenceViewController != null) {
		    // dataOrReferenceViewController.updateChartPanelWithComboBox(chartPanelWithComboBox, false, true,
		    // null);
		    // }
		    chartPanelWithComboBox.zoomAll();
		}
	    }
	} else {
	    // compute minY and maxY to keep as bounds for the next observation
	    for (final ChartPanelWithComboBox chartPanelWithComboBox : dataOrReferenceViewController.getListGraphs()) {
		final double minOrdinateNextObs = chartPanelWithComboBox.getScoop3ChartPanel()
			.getjScoop3ChartScrollPane().getMinOrdinatePhysVal();
		final double maxOrdinateNextObs = chartPanelWithComboBox.getScoop3ChartPanel()
			.getjScoop3ChartScrollPane().getMaxOrdinatePhysVal();

		if (((localKeepBoundsMinY <= minOrdinateNextObs) && (localKeepBoundsMaxY >= maxOrdinateNextObs))
			|| (localKeepBoundsMinY > maxOrdinateNextObs) || (localKeepBoundsMaxY < minOrdinateNextObs)) {
		    chartPanelWithComboBox.zoomAll();
		} else {
		    Double newMinY = null;
		    Double newMaxY = null;
		    Rectangle2D newRect2D = null;
		    if (localKeepBoundsMinY > minOrdinateNextObs) {
			newMinY = localKeepBoundsMinY;
		    }
		    if (localKeepBoundsMaxY < maxOrdinateNextObs) {
			newMaxY = localKeepBoundsMaxY;
		    }
		    if (newMinY == null) {
			newMinY = minOrdinateNextObs;
		    }
		    if (newMaxY == null) {
			newMaxY = maxOrdinateNextObs;
		    }

		    Double newRect2DY = ((newMinY - minOrdinateNextObs) / (maxOrdinateNextObs - minOrdinateNextObs))
			    * chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				    .getDataAreaForZoomLevelOne().getHeight();

		    // fix for scrollbar
		    newRect2DY = newRect2DY + ((newRect2DY / 4424) * chartPanelWithComboBox.getScoop3ChartPanel()
			    .getjScoop3ChartScrollPane().getDataAreaForZoomLevelOne().getHeight());

		    final Double newRect2DHeight = ((newMaxY - newMinY) / (maxOrdinateNextObs - minOrdinateNextObs))
			    * chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				    .getDataAreaForZoomLevelOne().getHeight();
		    newRect2D = new Rectangle2D(0, newRect2DY, chartPanelWithComboBox.getScoop3ChartPanel()
			    .getjScoop3ChartScrollPane().getDataAreaForZoomLevelOne().getWidth(), newRect2DHeight);

		    // refresh the new observation graphs
		    // if ((dataOrReferenceViewController != null) && refreshGraphs) {
		    // dataOrReferenceViewController.updateChartPanelWithComboBox(chartPanelWithComboBox, false, true,
		    // null);
		    // }

		    chartPanelWithComboBox.zoomOnDisplayArea(chartPanelWithComboBox.getScoop3ChartPanel()
			    .getjScoop3ChartScrollPane().getDataAreaForZoomLevelOne(), newRect2D, null, null, false,
			    null);
		}
	    }
	}
    }

    public JLabel getCruiseLabel() {
	return this.cruiseLabel;
    }
}
