package fr.ifremer.scoop3.gui.common.jdialog;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem.ERROR_MESSAGE_TYPE;
import fr.ifremer.scoop3.gui.common.CommonViewImpl;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.model.QCValues;

public class ReportJTableForDatasetMetadata extends ReportJTable {

    private static final int COLUMN_CHECKBOX = 0;
    private static final int COLUMN_COMMENT = 4;
    private static final int COLUMN_ERROR_MESSAGE = 1;
    private static final int COLUMN_QC_AUTO = 2;
    private static final int COLUMN_QC_MANUAL = 3;

    private static final long serialVersionUID = 3943182256018179539L;

    /**
     * @param commonViewImpl
     * @param report
     */
    public ReportJTableForDatasetMetadata(final ReportJDialog reportJDialog, final CommonViewImpl commonViewImpl,
	    final Report report) {
	super(reportJDialog, commonViewImpl, report);
    }

    /**
     * Add a new message in the JTable
     *
     * @param caDataErrorMessageItem
     */
    @Override
    public void addErrorMessage(final CAErrorMessageItem caErrorMessageItem) {
	addValues(caErrorMessageItem, true);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JTable#prepareRenderer(javax.swing.table.TableCellRenderer, int, int)
     */
    @Override
    public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) {

	// final int modelRow = convertRowIndexToModel(row);
	// final int modelColumn = convertColumnIndexToModel(column);

	final Component comp = super.prepareRenderer(renderer, row, column);

	final boolean isSelected = isCellSelected(row, 0);
	if (!isSelected) {
	    comp.setBackground(Color.white);
	    comp.setForeground(Color.black);
	}

	// Manage the color of the QC column
	if ((column == COLUMN_QC_AUTO) || (column == COLUMN_QC_MANUAL)) {
	    final Object valueAt = getValueAt(row, column);

	    if (valueAt != null) {
		final QCValues qcValue = getQCValueFromValueInColumn(valueAt);
		if (qcValue != null) {
		    Color backgroundColor = qcValue.getColor();
		    final Color foregroundColor = qcValue.getForegroundColor();
		    if (isSelected) {
			backgroundColor = backgroundColor.darker();
		    }
		    comp.setBackground(backgroundColor);
		    comp.setForeground(foregroundColor);
		} else {
		    comp.setBackground(Color.BLACK);
		    comp.setForeground(Color.RED);
		}
	    }
	}

	return comp;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.jdialog.ReportJTable#updateErrorMessage(fr.ifremer.scoop3.core.report.validation
     * .model.messages.CAErrorMessageItem)
     */
    @Override
    public void updateErrorMessage(final CAErrorMessageItem caErrorMessageItem) {
	for (int rowIndex = 0; rowIndex < getModel().getRowCount(); rowIndex++) {
	    if (getModel().getValueAt(rowIndex, COLUMN_ERROR_MESSAGE).equals(caErrorMessageItem.getDetails())) {
		getModel().setValueAt((caErrorMessageItem.getFlagManual() == null) ? null
			: /*
			   * ((caErrorMessageItem.getFlagManual().equals(QCValues.QC_A) ||
			   * caErrorMessageItem.getFlagManual().equals(QCValues.QC_Q)) ?
			   * (caErrorMessageItem.getFlagManual().equals(QCValues.QC_A) ? "A" : "Q") :
			   * caErrorMessageItem.getFlagManual().getQCValue())
			   */caErrorMessageItem.getFlagManual().getStringQCValue(), rowIndex, COLUMN_QC_MANUAL);
		getModel().setValueAt(caErrorMessageItem.isErrorChecked(), rowIndex, COLUMN_CHECKBOX);
		getModel().setValueAt((caErrorMessageItem.getComment() == null) ? "" : caErrorMessageItem.getComment(),
			rowIndex, COLUMN_COMMENT);
	    }
	}
    }

    /**
     * @param caErrorMessageItem
     */
    private void addValues(final CAErrorMessageItem caErrorMessageItem, final boolean updateDataModel) {
	final boolean addClasses = (classes.isEmpty());

	final Object[] row = new Object[NB_COLUMNS_DATASET_METADATA];

	row[COLUMN_CHECKBOX] = caErrorMessageItem.isErrorChecked();
	if (addClasses) {
	    classes.add(Boolean.class);
	}

	row[COLUMN_ERROR_MESSAGE] = caErrorMessageItem.getDetails();
	if (addClasses) {
	    classes.add(String.class);
	}

	// row[COLUMN_QC_AUTO] = (caErrorMessageItem.getFlagAuto() != null)
	// ? ((caErrorMessageItem.getFlagAuto().equals(QCValues.QC_A)
	// || caErrorMessageItem.getFlagAuto().equals(QCValues.QC_Q))
	// ? caErrorMessageItem.getFlagAuto().toString().substring(
	// caErrorMessageItem.getFlagAuto().toString().length() - 1,
	// caErrorMessageItem.getFlagAuto().toString().length())
	// : caErrorMessageItem.getFlagAuto().getQCValue())
	// : null;
	row[COLUMN_QC_AUTO] = (caErrorMessageItem.getFlagAuto() != null)
		? caErrorMessageItem.getFlagAuto().getStringQCValue() : null;
	if (addClasses) {
	    classes.add(Integer.class);
	}

	// row[COLUMN_QC_MANUAL] = (caErrorMessageItem.getFlagManual() != null)
	// ? ((caErrorMessageItem.getFlagAuto().equals(QCValues.QC_A)
	// || caErrorMessageItem.getFlagAuto().equals(QCValues.QC_Q))
	// ? caErrorMessageItem.getFlagAuto().toString().substring(
	// caErrorMessageItem.getFlagAuto().toString().length() - 1,
	// caErrorMessageItem.getFlagAuto().toString().length())
	// : caErrorMessageItem.getFlagAuto().getQCValue())
	// : null;
	row[COLUMN_QC_MANUAL] = (caErrorMessageItem.getFlagManual() != null)
		? caErrorMessageItem.getFlagAuto().getStringQCValue() : null;
	if (addClasses) {
	    classes.add(Integer.class);
	}

	row[COLUMN_COMMENT] = caErrorMessageItem.getComment();
	if (row[COLUMN_COMMENT] == null) {
	    row[COLUMN_COMMENT] = "";
	}
	if (addClasses) {
	    classes.add(String.class);
	}

	values.add(row);

	errorMessages.add(caErrorMessageItem);

	if (updateDataModel) {
	    ((DefaultTableModel) getModel()).addRow(row);
	}

	try {
	    getColumnModel().getColumn(COLUMN_QC_MANUAL)
		    .setCellEditor(new DefaultCellEditor(getQCComboBoxStringForMetadatasAndErrorReport()));
	} catch (final Exception e) {
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.jdialog.ReportJTable#addMessageIfNeeded(fr.ifremer.scoop3.gui.common.CommonViewImpl,
     * fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem)
     */
    @Override
    protected void addMessageIfNeeded(final CommonViewImpl commonViewImpl,
	    final CAErrorMessageItem caErrorMessageItem) {
	if (caErrorMessageItem.getErrorMessageType() == ERROR_MESSAGE_TYPE.DATASET_METADATA) {
	    addValues(caErrorMessageItem, false);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.jdialog.ReportJTable#cellUpdated(int, int, java.lang.Object, java.lang.Object)
     */
    @Override
    protected void cellUpdatedSpecific(final int row, final int column, final Object oldValue, final Object newValue) {
	// set old value as it is the ViewControler which manage this update
	((DefaultTableModel) getModel()).setValueAt(oldValue, row, column);

	final List<CAErrorMessageItem> errorMessagesToUpdate = getSelectedErrorMessages(new int[] { row });

	switch (column) {
	case COLUMN_CHECKBOX:
	    commonViewImpl.updateMultipleErrorMessages(getStepType(), errorMessagesToUpdate, null, null,
		    (Boolean) newValue);
	    break;
	case COLUMN_QC_MANUAL:
	    final QCValues newQC = QCValues.getQCValuesFromString(newValue.toString());
	    // if (newValue.equals("A")) {
	    // newQC = QCValues.QC_A;
	    // } else if (newValue.equals("Q")) {
	    // newQC = QCValues.QC_Q;
	    // } else {
	    // newQC = QCValues.getQCValues(Integer.valueOf(newValue.toString()));
	    // }
	    commonViewImpl.updateMultipleErrorMessages(getStepType(), errorMessagesToUpdate, newQC, null, null);
	    break;
	case COLUMN_COMMENT:
	    commonViewImpl.updateMultipleErrorMessages(getStepType(), errorMessagesToUpdate, null, (String) newValue,
		    null);
	    break;
	default:
	    // nothing to do here
	    break;
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.jdialog.ReportJTable#getColumnNames()
     */
    @Override
    protected String[] getColumnNames() {
	final String[] columnNames = new String[NB_COLUMNS_DATASET_METADATA];
	// column 0 : contains the checkbox
	columnNames[COLUMN_CHECKBOX] = Messages.getMessage("gui.errors-dialog.table-title.checked");
	// column 1 : contains the error message
	columnNames[COLUMN_ERROR_MESSAGE] = Messages.getMessage("gui.errors-dialog.table-title.error-message");
	// column 2 : contains the flag auto
	columnNames[COLUMN_QC_AUTO] = Messages.getMessage("gui.errors-dialog.table-title.flag-auto");
	// column 3 : contains the flag manual
	columnNames[COLUMN_QC_MANUAL] = Messages.getMessage("gui.errors-dialog.table-title.flag-manual");
	// column 4 : contains the comment
	columnNames[COLUMN_COMMENT] = Messages.getMessage("gui.errors-dialog.table-title.comment");

	return columnNames;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.jdialog.ReportJTable#getDefaultTableModel()
     */
    @Override
    protected DefaultTableModel getDefaultTableModel() {
	return new DefaultTableModel() {
	    private static final long serialVersionUID = -1166697740373933720L;

	    @Override
	    public boolean isCellEditable(final int row, final int column) {
		// The columns editables are : the first (checkbox), the Flag Manual and the Comment
		boolean isEditable;

		switch (column) {
		case COLUMN_CHECKBOX:
		case COLUMN_COMMENT:
		    isEditable = true;
		    break;
		case COLUMN_QC_MANUAL:
		    isEditable = true;
		    // Do NOT allowed to modify QC_5 or QC_9
		    final Integer value = (Integer) getValueAt(row, column);
		    if ((value != null) && ((QCValues.getQCValues(value) == QCValues.QC_5)
			    || (QCValues.getQCValues(value) == QCValues.QC_9))) {
			isEditable = false;
		    }
		    // Do NOT allowed to modify MANUAL_QC if there is no AUTO_QC
		    if (getValueAt(row, COLUMN_QC_AUTO) == null) {
			isEditable = false;
		    }
		    break;
		default:
		    isEditable = false;
		    break;
		}

		return isEditable;
	    }
	};
    }

    /**
     * @param row
     * @return
     */
    @Override
    protected CAErrorMessageItem getSelectedErrorMessage(final int row) {
	final String errorMsg = (String) getValueAt(convertRowIndexToView(row), COLUMN_ERROR_MESSAGE);

	for (final CAErrorMessageItem caErrorMessageItem : errorMessages) {
	    if (caErrorMessageItem.getDetails().equals(errorMsg)) {
		return caErrorMessageItem;
	    }
	}

	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.jdialog.ReportJTable#getStepType()
     */
    @Override
    protected STEP_TYPE getStepType() {
	return STEP_TYPE.Q1_CONTROL_AUTO_METADATA;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.jdialog.ReportJTable#setColumnsWidth()
     */
    @Override
    protected void setColumnsWidth() {
	getColumnModel().getColumn(COLUMN_CHECKBOX).setMinWidth(checkboxColumnWidth);
	getColumnModel().getColumn(COLUMN_CHECKBOX).setPreferredWidth(checkboxColumnWidth);
	getColumnModel().getColumn(COLUMN_CHECKBOX).setMaxWidth(checkboxColumnWidth);

	getColumnModel().getColumn(COLUMN_QC_AUTO).setMinWidth(qcColumnWidth);
	getColumnModel().getColumn(COLUMN_QC_AUTO).setPreferredWidth(qcColumnWidth);
	getColumnModel().getColumn(COLUMN_QC_AUTO).setMaxWidth(qcColumnWidth);

	getColumnModel().getColumn(COLUMN_QC_MANUAL).setMinWidth(qcColumnWidth);
	getColumnModel().getColumn(COLUMN_QC_MANUAL).setPreferredWidth(qcColumnWidth);
	getColumnModel().getColumn(COLUMN_QC_MANUAL).setMaxWidth(qcColumnWidth);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.jdialog.ReportJTable#setSpecificColumnRenderOrEditor()
     */
    @Override
    protected void setSpecificColumnRenderOrEditor() {
	// By default, the lines are wrapped for the error message and the comment
	getColumnModel().getColumn(COLUMN_ERROR_MESSAGE).setCellRenderer(new JTextAreaTableCellRenderer());
	getColumnModel().getColumn(COLUMN_COMMENT).setCellRenderer(new JTextAreaTableCellRenderer());

	getColumnModel().getColumn(COLUMN_QC_MANUAL)
		.setCellEditor(new DefaultCellEditor(getQCComboBoxStringForMetadatasAndErrorReport()));
    }
}
