package fr.ifremer.scoop3.gui.common.jdialog;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.MessageItem;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;
import fr.ifremer.scoop3.gui.common.CommonViewImpl;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.model.QCValues;

public abstract class ReportJTable extends JTable implements MouseListener {

    private static final long serialVersionUID = -6198809981588208662L;

    /**
     * Default Checkbox column width
     */
    protected static int checkboxColumnWidth;
    protected static final int NB_COLUMNS_DATASET_METADATA = 5;
    protected static final int NB_COLUMNS_OBSERVATION_DATA = 9;
    protected static final int NB_COLUMNS_OBSERVATION_REF_PARAMETER_DATA = 9;
    protected static final int NB_COLUMNS_OBSERVATION_METADATA = 7;
    /**
     * Default Parameter code column width
     */
    protected static int paramColumnWidth;
    /**
     * Default QC columns width
     */
    protected static int qcColumnWidth;
    /**
     * Default Reference column width
     */
    protected static int refColumnWidth;
    /**
     * Default Station column width
     */
    protected static int stationColumnWidth;

    private final String[] columnNames;
    private final ReportJDialog reportJDialog;
    protected final transient List<Class<?>> classes = new ArrayList<>();
    protected final transient CommonViewImpl commonViewImpl;
    protected final transient List<CAErrorMessageItem> errorMessages = new ArrayList<>();
    protected final transient Report report;
    protected final List<Object[]> values = new ArrayList<>();

    // when user pres CTRL + A, select all rows and update MultiModify button
    private static final String SOLVE = "Solve";
    KeyStroke controlA = KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK);

    static {
	try {
	    checkboxColumnWidth = Integer
		    .parseInt(FileConfig.getScoop3FileConfig().getString("gui.report-dialog.checkbox-column-width"));
	} catch (final NumberFormatException nfe) {
	    checkboxColumnWidth = 20;
	}
	try {
	    paramColumnWidth = Integer.parseInt(
		    FileConfig.getScoop3FileConfig().getString("gui.report-dialog.parameter-code-column-width"));
	} catch (final NumberFormatException nfe) {
	    paramColumnWidth = 55;
	}
	try {
	    refColumnWidth = Integer.parseInt(
		    FileConfig.getScoop3FileConfig().getString("gui.report-dialog.ref-parameter-column-width"));
	} catch (final NumberFormatException nfe) {
	    refColumnWidth = 50;
	}
	try {
	    qcColumnWidth = Integer
		    .parseInt(FileConfig.getScoop3FileConfig().getString("gui.report-dialog.qc-column-width"));
	} catch (final NumberFormatException nfe) {
	    qcColumnWidth = 40;
	}
	try {
	    stationColumnWidth = Integer
		    .parseInt(FileConfig.getScoop3FileConfig().getString("gui.report-dialog.station-column-width"));
	} catch (final NumberFormatException nfe) {
	    stationColumnWidth = 50;
	}
    }

    protected ReportJTable(final ReportJDialog reportJDialog, final CommonViewImpl commonViewImpl,
	    final Report report) {
	super();

	this.reportJDialog = reportJDialog;
	this.commonViewImpl = commonViewImpl;
	this.report = report;

	// Create the JTable Model
	final DefaultTableModel dtm = getDefaultTableModel();

	// Get the columns names ...
	columnNames = getColumnNames();
	for (final String columnName : columnNames) {
	    dtm.addColumn(columnName);
	}

	if (report != null) {
	    // Put each rows in the "values"
	    for (final MessageItem messageItem : report.getStep(getStepType()).getMessages()) {
		if (messageItem instanceof CAErrorMessageItem) {
		    final CAErrorMessageItem caErrorMessageItem = (CAErrorMessageItem) messageItem;
		    addMessageIfNeeded(commonViewImpl, caErrorMessageItem);
		}
	    }
	}

	// Add rows to the Data Table Model
	for (final Object[] rowAsList : values) {
	    dtm.addRow(rowAsList);
	}

	setModel(dtm);

	setColumnsWidth();

	addMouseListener(this);

	// By default, Strings and Integer are centered
	final DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	centerRenderer.setHorizontalAlignment(JLabel.CENTER);
	setDefaultRenderer(Integer.class, centerRenderer);
	setDefaultRenderer(String.class, centerRenderer);

	setSpecificColumnRenderOrEditor();

	// Disable the movement of columns
	getTableHeader().setReorderingAllowed(false);

	// Stop editing if the focus on the JTable is lost
	// Source : http://tips4java.wordpress.com/2008/12/12/table-stop-editing/
	putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

	// Source : http://tips4java.wordpress.com/2009/06/07/table-cell-listener/
	// Add a Cell Editor to catch an Event when a Cell has been edited and the value modified
	final Action action = new AbstractAction() {
	    private static final long serialVersionUID = -871423692572106517L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		final TableCellListener tcl = (TableCellListener) e.getSource();

		// update the "values"
		values.get(tcl.getRow())[tcl.getColumn()] = tcl.getNewValue();

		// Eventually, propagate the update ...
		cellUpdatedSpecific(tcl.getRow(), tcl.getColumn(), tcl.getOldValue(), tcl.getNewValue());
	    }
	};
	// final TableCellListener tcl =
	new TableCellListener(this, action);

	// when user pres CTRL + A, select all rows and update MultiModify button
	getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(controlA, SOLVE);
	getActionMap().put(SOLVE, new ControlAAction());
    }

    /**
     * Add a new message in the JTable
     *
     * @param caErrorMessageItem
     */
    public abstract void addErrorMessage(final CAErrorMessageItem caErrorMessageItem);

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JTable#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(final int column) {
	if (classes.isEmpty()) {
	    return super.getColumnClass(column);
	}
	return classes.get(convertColumnIndexToModel(column));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
	JTable target = null;
	// Add mouse listener for Simple Click
	if (e.getClickCount() == 1) {
	    target = (JTable) e.getSource();

	    final int row = target.getSelectedRow();
	    // select observation
	    if (row != -1) {
		selectObservation(row);
	    }
	}

	reportJDialog.getMultiModifyButton().setEnabled(getSelectedRows().length > 1);

	if (SwingUtilities.isRightMouseButton(e)) {
	    openMultiplesLineDialog(((target instanceof ReportJTableForObsData)
		    || (target instanceof ReportJTableForObsRefParameterData)) ? CommonViewImpl.getQCValuesSettable()
			    : CommonViewImpl.getQCValuesSettableForMetadatasAndErrorReport());
	}
    }

    // when user pres CTRL + A, select all rows and update MultiModify button
    private class ControlAAction extends AbstractAction {

	private static final long serialVersionUID = -7683566134943656185L;

	@Override
	public void actionPerformed(final ActionEvent e) {
	    setRowSelectionInterval(0, getRowCount() - 1);
	    reportJDialog.getMultiModifyButton().setEnabled(getSelectedRows().length > 1);
	}
    }

    public void openMultiplesLineDialog(final QCValues[] qcValuesSettable) {
	if (getSelectedRows().length > 1) {
	    final UpdateMultipleLineForReportDialog updateMultipleLineDialog = new UpdateMultipleLineForReportDialog(
		    reportJDialog, qcValuesSettable);
	    if (updateMultipleLineDialog.updateLines()) {
		QCValues qcToSet = null;
		for (final JRadioButton radioButton : updateMultipleLineDialog.getRadioButtons()) {
		    if (radioButton.isSelected()) {
			qcToSet = QCValues.getQCValuesFromString(radioButton.getText());
		    }
		}

		final String commentToSet = updateMultipleLineDialog.getCommentTextArea().getText().trim();

		final boolean isChecked = updateMultipleLineDialog.getCheckBox().isSelected();

		final List<CAErrorMessageItem> errorMessagesToUpdate = getSelectedErrorMessages(getSelectedRows());

		commonViewImpl.updateMultipleErrorMessages(getStepType(), errorMessagesToUpdate, qcToSet, commentToSet,
			isChecked);
	    }
	    updateMultipleLineDialog.dispose();
	}
    }

    /**
     * Select observation
     *
     * @param row
     */
    protected void selectObservation(final int row) {
	if (row < getRowCount()) {
	    try {
		final int modelRow = convertRowIndexToModel(row);

		final CAErrorMessageItem caErrorMessageItem = getSelectedErrorMessage(modelRow);
		final String obsRef = caErrorMessageItem.getObs1Id();
		final int level = (caErrorMessageItem instanceof CADataErrorMessageItem)
			? ((CADataErrorMessageItem) caErrorMessageItem).getReferenceIndex() : -1;

		if (obsRef != null) {
		    commonViewImpl.setSelectedObservation(obsRef, level);
		}
	    } catch (final IndexOutOfBoundsException ioobe) {
		SC3Logger.LOGGER.error(ioobe.getMessage(), ioobe);
	    }
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(final MouseEvent e) {
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(final MouseEvent e) {
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
    }

    public void removeErrorMessage(final CAErrorMessageItem caErrorMessageItem) {
	final int index = errorMessages.indexOf(caErrorMessageItem);
	report.removeMessage(caErrorMessageItem, getStepType());
	errorMessages.remove(index);
	values.remove(index);

	// direct call to DefaultTableModel.getDataVector().removeElementAt because DefaultTableModel.removeRow(index)
	// is not optimized
	((DefaultTableModel) getModel()).getDataVector().removeElementAt(index);
    }

    public abstract void updateErrorMessage(final CAErrorMessageItem caErrorMessageItem);

    /**
     * Add the message in the Table if needed
     *
     * @param commonViewImpl
     *
     * @param caErrorMessageItem
     */
    protected abstract void addMessageIfNeeded(CommonViewImpl commonViewImpl, CAErrorMessageItem caErrorMessageItem);

    /**
     * A cell has been updated.
     *
     * @param row
     * @param column
     * @param oldValue
     * @param newValue
     */
    protected abstract void cellUpdatedSpecific(int row, int column, Object oldValue, Object newValue);

    /**
     * @return the Column Names
     */
    protected abstract String[] getColumnNames();

    /**
     * @return the DefaultTableModel depending of the type of the
     */
    protected abstract DefaultTableModel getDefaultTableModel();

    /**
     * @return the selectable QC values in a ComboBox
     */
    protected JComboBox<String> getQCComboBoxString() {
	final JComboBox<String> comboBox = new JComboBox<>();
	for (final QCValues qcValue : CommonViewImpl.getQCValuesSettable()) {
	    comboBox.addItem(qcValue.getStringQCValue());
	}
	return comboBox;
    }

    /**
     * @return the selectable QC values in a ComboBox
     */
    protected JComboBox<String> getQCComboBoxStringForMetadatasAndErrorReport() {
	final JComboBox<String> comboBox = new JComboBox<>();
	for (final QCValues qcValue : CommonViewImpl.getQCValuesSettableForMetadatasAndErrorReport()) {
	    comboBox.addItem(qcValue.getStringQCValue());
	}
	return comboBox;
    }

    /**
     * @param valueInColumn
     * @return
     */
    protected QCValues getQCValueFromValueInColumn(final Object valueInColumn) {
	QCValues qcValue;
	if (valueInColumn instanceof String) {
	    if (((String) valueInColumn).isEmpty()) {
		qcValue = null;
	    } else {
		qcValue = QCValues.getQCValuesFromString((String) valueInColumn);
	    }
	} else if (valueInColumn instanceof Integer) {
	    qcValue = QCValues.getQCValues((int) valueInColumn);
	} else {
	    qcValue = (QCValues) valueInColumn;
	}
	return qcValue;
    }

    /**
     * @param selectedRows
     * @return the current selected rows
     */
    protected List<CAErrorMessageItem> getSelectedErrorMessages(final int[] selectedRows) {
	final List<CAErrorMessageItem> selectedErrorMessages = new ArrayList<>();
	for (final int row : selectedRows) {
	    final CAErrorMessageItem selectedMsg = getSelectedErrorMessage(convertRowIndexToModel(row));
	    if (selectedMsg != null) {
		selectedErrorMessages.add(selectedMsg);
	    }
	}
	return selectedErrorMessages;
    }

    /**
     * @param row
     * @return the current selected row
     */
    protected abstract CAErrorMessageItem getSelectedErrorMessage(int row);

    /**
     * @return the STEP_TYPE for the error messages
     */
    protected abstract STEP_TYPE getStepType();

    /**
     * Set the width of the column if needed
     */
    protected abstract void setColumnsWidth();

    /**
     * Eventually set specific render or specific editor
     */
    protected abstract void setSpecificColumnRenderOrEditor();

    /**
     * Refresh the whole table. Method from DefaultTableModel.fireTableRowsInserted()
     *
     * @param rowVector
     */
    public void fireTableRowsInserted() {
	final DefaultTableModel tableModel = ((DefaultTableModel) getModel());
	tableModel.fireTableRowsInserted(0, tableModel.getRowCount() - 1);
    }
}
