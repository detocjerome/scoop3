package fr.ifremer.scoop3.gui.common.jdialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem.ERROR_MESSAGE_TYPE;
import fr.ifremer.scoop3.gui.common.CommonViewImpl;
import fr.ifremer.scoop3.gui.common.jdialog.oxbowFilter.table.filter.TableRowFilterSupport;
import fr.ifremer.scoop3.gui.map.MapViewImpl;
import fr.ifremer.scoop3.gui.reference.ReferenceViewImpl;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.properties.FileConfig;

public class ReportJDialog extends JDialog {

    /**
     * Default height of the JDialog
     */
    private static int dialogMinHeight;
    /**
     * Default width of the JDialog
     */
    private static int dialogMinWidth;
    /**
     * Reference on the INSTANCE
     */
    private static ReportJDialog instance = null;
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5413480560603806679L;
    /**
     * TRUE if there is at least on error on the Data
     */
    private boolean atLeastOneErrorMessageForData;
    /**
     * TRUE if there is at least on error on the Metadata
     */
    private boolean atLeastOneErrorMessageForMetadata;
    /**
     * Reference on the ReportJTable for the Dataset Metadata
     */
    private ReportJTableForDatasetMetadata datasetMetadataJTable;
    /**
     * Reference on the Tab viewport which contains the datasetMetadatajTablePanel
     */
    private JPanel datasetMetadataViewportPanel;
    /**
     * Reference on the ReportJTable for the Observation Data
     */
    private ReportJTableForObsData observationDataJTable;
    /**
     * Reference on the Tab viewport which contains the observationDatajTablePanel
     */
    private JPanel observationDataViewportPanel;
    /**
     * Reference on the ReportJTable for the Observation Metadata
     */
    private ReportJTableForObsMetadata observationMetadataJTable;
    /**
     * Reference on the Tab viewport which contains the observationMetadatajTablePanel
     */
    private JPanel observationMetadataViewportPanel;
    /**
     * Reference on the ReportJTable for the Observation Reference parameter Data
     */
    public ReportJTableForObsRefParameterData observationRefParameterDataJTable;
    /**
     * Reference on the Tab viewport which contains the observationRefParameterDatajTablePanel
     */
    public JPanel observationRefParameterDataViewportPanel;
    /**
     * Reference on the JTabbedPane
     */
    private JTabbedPane tabbedPane;

    private JButton multiModifyButton;

    static {
	try {
	    dialogMinHeight = Integer
		    .parseInt(FileConfig.getScoop3FileConfig().getString("gui.report-dialog.min-heigth"));
	} catch (final NumberFormatException nfe) {
	    dialogMinHeight = 450;
	}
	try {
	    dialogMinWidth = Integer
		    .parseInt(FileConfig.getScoop3FileConfig().getString("gui.report-dialog.min-width"));
	} catch (final NumberFormatException nfe) {
	    dialogMinWidth = 600;
	}
    }

    public static void addErrorMessage(final CAErrorMessageItem caErrorMessageItem) {
	if (instance != null) {
	    ReportJTable reportJTable = null;
	    switch (caErrorMessageItem.getErrorMessageType()) {
	    case DATASET_METADATA:
		reportJTable = instance.datasetMetadataJTable;
		break;
	    case OBSERVATION_REFERENCE_DATA:
		reportJTable = instance.observationRefParameterDataJTable;
		break;
	    case OBSERVATION_DATA:
		reportJTable = instance.observationDataJTable;
		break;
	    case OBSERVATION_METADATA:
		reportJTable = instance.observationMetadataJTable;
		break;
	    }
	    if (reportJTable != null) {
		reportJTable.addErrorMessage(caErrorMessageItem);
	    }
	}
    }

    /**
     * If the Instance exists, dispose the JDialog
     */
    public static void disposeIfExists() {
	if (instance != null) {
	    instance.closeButtonAction();
	}
    }

    /**
     * Use of a Singleton to be sure that there is not twice this JDialog
     *
     * @param frameOwner
     * @param commonViewImpl
     * @param report
     * @return
     */
    public static ReportJDialog getReportJDialog(final JFrame frameOwner, final CommonViewImpl commonViewImpl,
	    final Report report) {
	if (instance == null) {
	    instance = new ReportJDialog(frameOwner, commonViewImpl, report);
	}
	instance.updateReportTables();

	return instance;
    }

    /**
     * refresh the report jdialog after a change in language
     *
     * @param frameOwner
     * @param commonViewImpl
     * @param report
     * @return
     */
    public static void refreshReportJDialog(final JFrame frameOwner, final CommonViewImpl commonViewImpl,
	    final Report report) {
	instance = new ReportJDialog(frameOwner, commonViewImpl, report);
    }

    public static void removeErrorMessage(final CAErrorMessageItem caErrorMessageItem) {
	if (instance != null) {
	    ReportJTable reportJTable = null;
	    switch (caErrorMessageItem.getErrorMessageType()) {
	    case DATASET_METADATA:
		reportJTable = instance.datasetMetadataJTable;
		break;
	    case OBSERVATION_REFERENCE_DATA:
		reportJTable = instance.observationRefParameterDataJTable;
		break;
	    case OBSERVATION_DATA:
		reportJTable = instance.observationDataJTable;
		break;
	    case OBSERVATION_METADATA:
		reportJTable = instance.observationMetadataJTable;
		break;
	    }
	    if (reportJTable != null) {
		reportJTable.removeErrorMessage(caErrorMessageItem);
	    }
	}
    }

    public static void updateErrorMessage(final CAErrorMessageItem caErrorMessageItem) {
	if (instance != null) {
	    instance.datasetMetadataJTable.updateErrorMessage(caErrorMessageItem);
	    instance.observationMetadataJTable.updateErrorMessage(caErrorMessageItem);
	    instance.observationRefParameterDataJTable.updateErrorMessage(caErrorMessageItem);
	    instance.observationDataJTable.updateErrorMessage(caErrorMessageItem);
	}
    }

    /**
     * Private constructor (call only with the static method)
     *
     * @param frameOwner
     * @param commonViewImpl
     * @param report
     */
    private ReportJDialog(final JFrame frameOwner, final CommonViewImpl commonViewImpl, final Report report) {
	// This JDialog is NOT modal
	super(frameOwner, false);

	initView(frameOwner, commonViewImpl, report);
    }

    /**
     * @return the First error message if exists (or NULL)
     */
    public CADataErrorMessageItem getFirstDataErrorMessage() {
	if (observationDataJTable != null) {
	    return observationDataJTable.getFirstDataErrorMessage();
	}
	return null;
    }

    /**
     * @return the atLeastOneErrorMessageForData
     */
    public boolean isThereAtLeastOneErrorMessageForData() {
	return atLeastOneErrorMessageForData;
    }

    /**
     * @return the atLeastOneErrorMessageForMetadata
     */
    public boolean isThereAtLeastOneErrorMessageForMetadata() {
	return atLeastOneErrorMessageForMetadata;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.Dialog#setVisible(boolean)
     */
    @Override
    public void setVisible(final boolean visible) {
	final int wishedHeight = Toolkit.getDefaultToolkit().getScreenSize().height - 50;
	if (getSize().height > wishedHeight) {
	    setSize(getSize().width, wishedHeight);
	}
	super.setVisible(visible);
    }

    private void addCenterPanel(final CommonViewImpl commonViewImpl, final Report report) {
	tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	getContentPane().add(tabbedPane, BorderLayout.CENTER);

	// FIRST TAB
	int tabToSelect = 3;
	atLeastOneErrorMessageForMetadata = false;
	atLeastOneErrorMessageForData = false;
	boolean errorMsgsPresents = createNewTab(tabbedPane, "gui.errors-dialog.tab-1-tab-title",
		"gui.errors-dialog.tab-1-title", commonViewImpl, report, ERROR_MESSAGE_TYPE.DATASET_METADATA);
	if (errorMsgsPresents && (commonViewImpl instanceof MapViewImpl)) {
	    atLeastOneErrorMessageForMetadata = true;
	    tabToSelect = Math.min(tabToSelect, 0);
	}

	// SECOND TAB
	errorMsgsPresents = createNewTab(tabbedPane, "gui.errors-dialog.tab-2-tab-title",
		"gui.errors-dialog.tab-2-title", commonViewImpl, report, ERROR_MESSAGE_TYPE.OBSERVATION_METADATA);
	if (errorMsgsPresents && (commonViewImpl instanceof MapViewImpl)) {
	    atLeastOneErrorMessageForMetadata = true;
	    tabToSelect = Math.min(tabToSelect, 1);
	}

	// THIRD TAB
	errorMsgsPresents = createNewTab(tabbedPane, "gui.errors-dialog.tab-3-tab-title",
		"gui.errors-dialog.tab-3-title", commonViewImpl, report, ERROR_MESSAGE_TYPE.OBSERVATION_REFERENCE_DATA);
	if (errorMsgsPresents && (commonViewImpl instanceof ReferenceViewImpl)) {
	    atLeastOneErrorMessageForData = true;
	    tabToSelect = Math.min(tabToSelect, 2);
	}

	// FORTH TAB
	errorMsgsPresents = createNewTab(tabbedPane, "gui.errors-dialog.tab-4-tab-title",
		"gui.errors-dialog.tab-4-title", commonViewImpl, report, ERROR_MESSAGE_TYPE.OBSERVATION_DATA);
	if (errorMsgsPresents) {
	    atLeastOneErrorMessageForData = true;
	    tabToSelect = Math.min(tabToSelect, 3);
	}

	// If there are no errors, select the first tab
	if (!atLeastOneErrorMessageForMetadata && !atLeastOneErrorMessageForData) {
	    tabToSelect = 0;
	}

	tabbedPane.setSelectedIndex(tabToSelect);
    }

    /**
     * Action done by the Close Button
     */
    private void closeButtonAction() {
	setVisible(false);
	dispose();
	instance = null;
    }

    /**
     * Action done by the Multi Modify Button
     */
    private void multiModifyButtonAction() {

	// select the good JTable in the jDialog
	ReportJTable reportJTable = null;
	if (instance.datasetMetadataJTable.isShowing()) {
	    reportJTable = instance.datasetMetadataJTable;
	} else if (instance.observationMetadataJTable.isShowing()) {
	    reportJTable = instance.observationMetadataJTable;
	} else if (instance.observationRefParameterDataJTable.isShowing()) {
	    reportJTable = instance.observationRefParameterDataJTable;
	} else if (instance.observationDataJTable.isShowing()) {
	    reportJTable = instance.observationDataJTable;
	}

	if (reportJTable != null) {
	    reportJTable.openMultiplesLineDialog(((reportJTable == instance.observationDataJTable)
		    || (reportJTable == instance.observationRefParameterDataJTable))
			    ? CommonViewImpl.getQCValuesSettable()
			    : CommonViewImpl.getQCValuesSettableForMetadatasAndErrorReport());
	}
    }

    /**
     * Create a new Tab
     *
     * @param scrollPane
     * @param panelTitle
     * @param commonViewImpl
     * @param report
     * @param errorMessageType
     * @return TRUE if there is at least 1 error message
     */
    private boolean createNewTab(final JTabbedPane tabbedPane, final String tabTitle, final String panelTitle,
	    final CommonViewImpl commonViewImpl, final Report report, final ERROR_MESSAGE_TYPE errorMessageType) {
	boolean errorMsgPresents = false;

	final JPanel panel = new JPanel();
	panel.setLayout(new BorderLayout(5, 5));

	// Add the ScrollPane as a new Tab in the JTabbedPane
	tabbedPane.addTab(Messages.getMessage(tabTitle), null, panel, null);
	// Add a title in the NORTH of the JPanel
	panel.add(new JLabel(Messages.getMessage(panelTitle), JLabel.CENTER), BorderLayout.NORTH);

	JTable jTable = null;
	switch (errorMessageType) {
	case DATASET_METADATA:
	    datasetMetadataJTable = new ReportJTableForDatasetMetadata(this, commonViewImpl, report);
	    datasetMetadataViewportPanel = panel;
	    jTable = TableRowFilterSupport.forTable(datasetMetadataJTable).actions(true).searchable(true)
		    .useTableRenderers(true).apply();
	    break;
	case OBSERVATION_REFERENCE_DATA:
	    observationRefParameterDataJTable = new ReportJTableForObsRefParameterData(this, commonViewImpl, report);
	    observationRefParameterDataViewportPanel = panel;
	    jTable = TableRowFilterSupport.forTable(observationRefParameterDataJTable).actions(true).searchable(true)
		    .useTableRenderers(true).apply();
	    break;
	case OBSERVATION_DATA:
	    observationDataJTable = new ReportJTableForObsData(this, commonViewImpl, report);
	    observationDataViewportPanel = panel;
	    jTable = TableRowFilterSupport.forTable(observationDataJTable).actions(true).searchable(true)
		    .useTableRenderers(true).apply();
	    break;
	case OBSERVATION_METADATA:
	    observationMetadataJTable = new ReportJTableForObsMetadata(this, commonViewImpl, report);
	    observationMetadataViewportPanel = panel;
	    jTable = TableRowFilterSupport.forTable(observationMetadataJTable).actions(true).searchable(true)
		    .useTableRenderers(true).apply();
	    break;
	}

	if (jTable != null) {
	    if ((jTable.getRowCount() > 0) || ((errorMessageType == ERROR_MESSAGE_TYPE.OBSERVATION_DATA)
		    || (errorMessageType == ERROR_MESSAGE_TYPE.OBSERVATION_REFERENCE_DATA))) {
		if (jTable.getRowCount() > 0) {
		    errorMsgPresents = true;
		}

		// Add the JTable's header and data in a JPanel
		final JPanel jTablePanel = new JPanel();
		jTablePanel.setLayout(new BorderLayout());
		jTablePanel.add(jTable, BorderLayout.CENTER);

		if (jTable.getRowCount() > 0) {
		    // Add the JTable with the Error Messages in the CENTER of the JPanel
		    final JScrollPane jScrollPane = new JScrollPane(jTablePanel);
		    jScrollPane.setColumnHeaderView(jTable.getTableHeader());
		    panel.add(jScrollPane, BorderLayout.CENTER);
		}
	    }
	    if (jTable.getRowCount() == 0) {
		errorMsgPresents = false;
		panel.add(new JLabel(Messages.getMessage("gui.errors-dialog.no-error")), BorderLayout.CENTER);
	    }
	}

	displayOrHideJTableIfNeeded();

	return errorMsgPresents;
    }

    /**
     *
     * @param frameOwner
     * @param commonViewImpl
     * @param report
     */
    private void initView(final JFrame frameOwner, final CommonViewImpl commonViewImpl, final Report report) {
	getContentPane().setLayout(new BorderLayout(5, 5));

	// Add frame's title
	this.setTitle(Messages.getMessage("gui.errors-dialog.title"));

	// Add North label
	/*
	 * final JLabel lblTitle = new JLabel(Messages.getMessage("gui.errors-dialog.title"));
	 * lblTitle.setHorizontalAlignment(SwingConstants.CENTER); getContentPane().add(lblTitle, BorderLayout.NORTH);
	 */

	addCenterPanel(commonViewImpl, report);

	// Add South East button panel
	final JPanel closeButtonPanel = new JPanel();
	closeButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	final JButton closeButton = new JButton(Messages.getMessage("bpc-gui.button-close"));
	closeButton.addActionListener((final ActionEvent e) -> closeButtonAction());
	closeButtonPanel.add(closeButton);

	// Add South West button panel
	final JPanel multiModifyButtonPanel = new JPanel();
	multiModifyButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
	multiModifyButton = new JButton(Messages.getMessage("bpc-gui.button-modify-multi-lines-report"));
	multiModifyButton.setEnabled(false);
	multiModifyButton.addActionListener((final ActionEvent e) -> multiModifyButtonAction());
	multiModifyButtonPanel.add(multiModifyButton);

	// Create south panel with CloseButton and EditTheSelectionButton
	final JPanel southPanel = new JPanel();
	southPanel.add(multiModifyButtonPanel);
	southPanel.add(closeButtonPanel);
	getContentPane().add(southPanel, BorderLayout.SOUTH);

	// End of the init
	setMinimumSize(new Dimension(dialogMinWidth, dialogMinHeight));
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	pack();
	setLocationRelativeTo(frameOwner);
    }

    /**
     * @param reportJTable
     * @param tableWasEmpty
     * @param tableIsEmpty
     */
    protected void displayOrHideJTableIfNeeded(final JPanel viewportPanel, final ReportJTable reportJTable,
	    final boolean tableIsEmpty, final int tabIndex) {
	if (!tableIsEmpty) {
	    viewportPanel.removeAll();
	    final JScrollPane jScrollPane = new JScrollPane(reportJTable);
	    jScrollPane.setColumnHeaderView(reportJTable.getTableHeader());
	    viewportPanel.add(jScrollPane, BorderLayout.CENTER);
	    viewportPanel.validate();
	    viewportPanel.repaint();
	    tabbedPane.setSelectedIndex(tabIndex);
	    reportJTable.fireTableRowsInserted();
	} else {
	    viewportPanel.removeAll();
	    viewportPanel.add(new JLabel(Messages.getMessage("gui.errors-dialog.no-error")), BorderLayout.CENTER);
	    viewportPanel.validate();
	    viewportPanel.repaint();
	}
    }

    public static void displayOrHideJTableIfNeeded() {
	if (instance != null) {
	    ReportJTable reportJTable = null;
	    JPanel viewportPanel = null;
	    int tabIndex = -1;

	    // DATASET_METADATA:
	    reportJTable = instance.datasetMetadataJTable;
	    viewportPanel = instance.datasetMetadataViewportPanel;
	    tabIndex = 0;
	    boolean tableIsEmpty = reportJTable.getRowCount() == 0;
	    instance.displayOrHideJTableIfNeeded(viewportPanel, reportJTable, tableIsEmpty, tabIndex);

	    // OBSERVATION_METADATA
	    reportJTable = instance.observationMetadataJTable;
	    viewportPanel = instance.observationMetadataViewportPanel;
	    tabIndex = 1;
	    tableIsEmpty = reportJTable.getRowCount() == 0;
	    instance.displayOrHideJTableIfNeeded(viewportPanel, reportJTable, tableIsEmpty, tabIndex);

	    // OBSERVATION_REFERENCE_DATA
	    reportJTable = instance.observationRefParameterDataJTable;
	    viewportPanel = instance.observationRefParameterDataViewportPanel;
	    tabIndex = 2;
	    tableIsEmpty = reportJTable.getRowCount() == 0;
	    instance.displayOrHideJTableIfNeeded(viewportPanel, reportJTable, tableIsEmpty, tabIndex);

	    // OBSERVATION_DATA
	    reportJTable = instance.observationDataJTable;
	    viewportPanel = instance.observationDataViewportPanel;
	    tabIndex = 3;
	    tableIsEmpty = reportJTable.getRowCount() == 0;
	    instance.displayOrHideJTableIfNeeded(viewportPanel, reportJTable, tableIsEmpty, tabIndex);

	}
    }

    private void updateReportTables() {
	if (datasetMetadataJTable != null) {
	    datasetMetadataJTable.setSpecificColumnRenderOrEditor();
	    ((DefaultTableModel) datasetMetadataJTable.getModel()).fireTableDataChanged();
	    datasetMetadataJTable.repaint();
	}
	if (observationDataJTable != null) {
	    observationDataJTable.setSpecificColumnRenderOrEditor();
	    ((DefaultTableModel) observationDataJTable.getModel()).fireTableDataChanged();
	    observationDataJTable.repaint();
	}
	if (observationMetadataJTable != null) {
	    observationMetadataJTable.setSpecificColumnRenderOrEditor();
	    ((DefaultTableModel) observationMetadataJTable.getModel()).fireTableDataChanged();
	    observationMetadataJTable.repaint();
	}
	if (observationRefParameterDataJTable != null) {
	    observationRefParameterDataJTable.setSpecificColumnRenderOrEditor();
	    ((DefaultTableModel) observationRefParameterDataJTable.getModel()).fireTableDataChanged();
	    observationRefParameterDataJTable.repaint();
	}
    }

    public JButton getMultiModifyButton() {
	return multiModifyButton;
    }

    public void setMultiModifyButton(final JButton multiModifyButton) {
	this.multiModifyButton = multiModifyButton;
    }
}
