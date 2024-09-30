package fr.ifremer.scoop3.gui.common.jdialog;

import java.awt.Color;
import java.awt.Component;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem.ERROR_MESSAGE_TYPE;
import fr.ifremer.scoop3.gui.common.CommonViewImpl;
import fr.ifremer.scoop3.gui.data.DataViewImpl;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.mail.UnhandledException;
import fr.ifremer.scoop3.model.QCValues;

public class ReportJTableForObsRefParameterData extends ReportJTable {

    /**
     *
     */
    private static final long serialVersionUID = 216932109641900819L;
    private static final int COLUMN_CHECKBOX = 0;
    private static final int COLUMN_COMMENT = 8;
    private static final int COLUMN_ERROR_MESSAGE = 5;
    private static final int COLUMN_OBSERVATION_REF = 1;
    private static final int COLUMN_PARAMETER_CODE = 2;
    private static final int COLUMN_PARAMETER_VALUE = 3;
    private static final int COLUMN_QC_AUTO = 6;
    private static final int COLUMN_QC_MANUAL = 7;
    private static final int COLUMN_REF_VALUE = 4;

    private transient Vector<Object> rowVector;

    /**
     * @param reportJDialog
     * @param commonViewImpl
     * @param report
     */
    public ReportJTableForObsRefParameterData(final ReportJDialog reportJDialog, final CommonViewImpl commonViewImpl,
	    final Report report) {
	super(reportJDialog, commonViewImpl, report);
	setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	setRowSelectionAllowed(true);
    }

    /**
     * Add a new message in the JTable
     *
     * @param caDataErrorMessageItem
     */
    @Override
    public void addErrorMessage(final CAErrorMessageItem caErrorMessageItem) {
	if (caErrorMessageItem instanceof CADataErrorMessageItem) {
	    addValues((CADataErrorMessageItem) caErrorMessageItem, true);
	}
    }

    /**
     * @return the First error message if exists (or NULL)
     */
    public CADataErrorMessageItem getFirstDataErrorMessage() {
	if ((errorMessages != null) && !errorMessages.isEmpty()) {
	    return (CADataErrorMessageItem) errorMessages.get(0);
	}
	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JTable#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(final int row, final int column) {
	if (column == COLUMN_QC_MANUAL) {
	    // If there is no parameter code, it is not possible to update the QC
	    if ((((String) getValueAt(row, COLUMN_PARAMETER_CODE)) == null)
		    || ((String) getValueAt(row, COLUMN_PARAMETER_CODE)).equals("-")
		    || ((String) getValueAt(row, COLUMN_REF_VALUE)).equals("-")
		    || ((((String) getValueAt(row, COLUMN_QC_MANUAL)) != null)
			    && ((String) getValueAt(row, COLUMN_QC_MANUAL)).equals("5"))
		    || ((((String) getValueAt(row, COLUMN_QC_MANUAL)) != null)
			    && ((String) getValueAt(row, COLUMN_QC_MANUAL)).equals("9"))) {
		return false;
	    } else {
		// select observation event if click on dropdown menu
		selectObservation(row);
	    }
	}
	return super.isCellEditable(row, column);
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
	final int index = errorMessages.indexOf(caErrorMessageItem);
	if (index >= 0) {
	    if (caErrorMessageItem instanceof CADataErrorMessageItem) {
		final CADataErrorMessageItem caDataErrorMessageItem = (CADataErrorMessageItem) caErrorMessageItem;
		getModel().setValueAt((caDataErrorMessageItem.getFlagManual() == null) ? null
			: /*
			   * ((caDataErrorMessageItem.getFlagManual().equals(QCValues.QC_A) ||
			   * caDataErrorMessageItem.getFlagManual().equals(QCValues.QC_Q)) ?
			   * (caDataErrorMessageItem.getFlagManual().equals(QCValues.QC_A) ? "A" : "Q") :
			   * caDataErrorMessageItem.getFlagManual().getQCValue())
			   */caDataErrorMessageItem.getFlagManual().getStringQCValue(), index, COLUMN_QC_MANUAL);
		getModel().setValueAt(caDataErrorMessageItem.isErrorChecked(), index, COLUMN_CHECKBOX);
	    }
	    getModel().setValueAt((caErrorMessageItem.getComment() == null) ? "" : caErrorMessageItem.getComment(),
		    index, COLUMN_COMMENT);
	}
    }

    /**
     * @param caDataErrorMessageItemToInsert
     */
    private void addValues(final CADataErrorMessageItem caDataErrorMessageItemToInsert, final boolean updateDataModel) {
	final boolean addClasses = classes.isEmpty();

	// store rowVectors
	final DefaultTableModel tempTableModel = (DefaultTableModel) getModel();
	final Vector<Object> tempDataVector = new Vector<Object>();
	for (int i = 0; i < tempTableModel.getRowCount(); i++) {
	    final Vector<Object> newVector = new Vector<Object>();
	    for (int j = 0; j < tempTableModel.getColumnCount(); j++) {
		newVector.add(tempTableModel.getValueAt(i, j));
	    }
	    tempDataVector.add(newVector);
	}
	final Vector<Object> tempColumnsVector = new Vector<Object>();
	for (int i = 0; i < tempTableModel.getColumnCount(); i++) {
	    tempColumnsVector.add(tempTableModel.getColumnName(i));
	}

	if (rowVector == null) { // avoid creation of new instance
	    rowVector = new Vector<Object>(NB_COLUMNS_OBSERVATION_REF_PARAMETER_DATA);
	} else {
	    rowVector.clear();
	}

	rowVector.add(COLUMN_CHECKBOX, caDataErrorMessageItemToInsert.isErrorChecked());
	if (addClasses) {
	    classes.add(Boolean.class);
	}

	rowVector.add(COLUMN_OBSERVATION_REF, caDataErrorMessageItemToInsert.getObs1Id());
	if (addClasses) {
	    classes.add(String.class);
	}

	rowVector.add(COLUMN_PARAMETER_CODE, caDataErrorMessageItemToInsert.getParamCode());
	if (addClasses) {
	    classes.add(String.class);
	}

	rowVector.add(COLUMN_PARAMETER_VALUE, caDataErrorMessageItemToInsert.getParamValueStr());
	if (addClasses) {
	    classes.add(String.class);
	}

	rowVector.add(COLUMN_REF_VALUE, caDataErrorMessageItemToInsert.getReferenceValueToDisplayInReportDialog());
	if (addClasses) {
	    classes.add(String.class);
	}

	rowVector.add(COLUMN_ERROR_MESSAGE, caDataErrorMessageItemToInsert.getDetails());
	if (addClasses) {
	    classes.add(String.class);
	}

	rowVector.add(COLUMN_QC_AUTO, caDataErrorMessageItemToInsert.getFlagAuto().getStringQCValue());
	if (addClasses) {
	    classes.add(String.class);
	}

	rowVector.add(COLUMN_QC_MANUAL, (caDataErrorMessageItemToInsert.getFlagManual() != null)
		? caDataErrorMessageItemToInsert.getFlagManual().getStringQCValue() : null);
	if (addClasses) {
	    classes.add(String.class);
	}

	rowVector.add(COLUMN_COMMENT,
		caDataErrorMessageItemToInsert.getComment() != null ? caDataErrorMessageItemToInsert.getComment() : "");
	if (addClasses) {
	    classes.add(String.class);
	}

	int indexForInsert = -1;
	final int nbErrorMessage = errorMessages.size();
	for (int index = 0; (index < nbErrorMessage) && (indexForInsert < 0); index++) {
	    final CADataErrorMessageItem caDataErrorMessageItem = (CADataErrorMessageItem) errorMessages.get(index);
	    final int compareTo = compareCADataErrorMessageItem(caDataErrorMessageItem, caDataErrorMessageItemToInsert);
	    if (compareTo > 0) {
		indexForInsert = index;
	    }
	}

	// insert saved dataVector
	final DefaultTableModel tableModel = ((DefaultTableModel) getModel());
	tableModel.setDataVector(tempDataVector, tempColumnsVector);

	if (indexForInsert < 0) {
	    errorMessages.add(caDataErrorMessageItemToInsert);
	    values.add(rowVector.toArray());
	    if (updateDataModel) {
		addRow(rowVector);
	    }
	} else {
	    errorMessages.add(indexForInsert, caDataErrorMessageItemToInsert);
	    values.add(indexForInsert, rowVector.toArray());
	    if (updateDataModel) {
		insertRow(indexForInsert, rowVector);
	    }
	}

	try {
	    getColumnModel().getColumn(COLUMN_QC_MANUAL).setCellEditor(new DefaultCellEditor(getQCComboBoxString()));
	} catch (final Exception e) {
	}
    }

    @SuppressWarnings("unchecked")
    /**
     * Optimized method from DefaultTableModel.addRow()
     *
     * @param rowVector
     */
    private void addRow(final Vector<Object> rowVector) {
	final DefaultTableModel tableModel = ((DefaultTableModel) getModel());
	final int row = tableModel.getRowCount();
	tableModel.getDataVector().insertElementAt(rowVector, row);
    }

    @SuppressWarnings("unchecked")
    /**
     * Optimized method from DefaultTableModel.insertRow()
     *
     * @param rowVector
     */
    private void insertRow(final int indexForInsert, final Vector<Object> rowVector) {
	final DefaultTableModel tableModel = ((DefaultTableModel) getModel());
	tableModel.getDataVector().insertElementAt(rowVector, indexForInsert);
    }

    /**
     *
     * @param o1
     * @param o2
     * @return
     */
    private int compareCADataErrorMessageItem(final CADataErrorMessageItem o1, final CADataErrorMessageItem o2) {
	try {
	    if ((o1.getObs1Id() != null) && (o2.getObs1Id() != null) && !o1.getObs1Id().equals(o2.getObs1Id())) {
		return o1.getObs1Id().compareTo(o2.getObs1Id());
	    }
	    if ((o1.getParamCode() != null) && (o2.getParamCode() != null)
		    && (!o1.getParamCode().equals(o2.getParamCode()))) {
		return o1.getParamCode().compareTo(o2.getParamCode());
	    }
	    if ((o1.getReferenceValue() != null) && (o2.getReferenceValue() != null)
		    && !o1.getReferenceValue().equals(o2.getReferenceValue())) {
		Double o1Ref = null;
		try {
		    o1Ref = Double.parseDouble(o1.getReferenceValue());
		} catch (final NumberFormatException nfe) {
		    return -1;
		}
		Double o2Ref = null;
		try {
		    o2Ref = Double.parseDouble(o2.getReferenceValue());
		} catch (final NumberFormatException nfe) {
		    return 1;
		}
		return o1Ref.compareTo(o2Ref);
	    }
	    if ((o1.getParamValueStr() != null) && (o2.getParamValueStr() != null)
		    && (!o1.getParamValueStr().equals(o2.getParamValueStr()))) {
		return o1.getParamValueStr().compareTo(o2.getParamValueStr());
	    }
	    if ((o1.getDetails() != null) && (o2.getDetails() != null) && !o1.getDetails().equals(o2.getDetails())) {
		return o1.getDetails().compareTo(o2.getDetails());
	    }
	    if (o1.getFlagAuto() != o2.getFlagAuto()) {
		return ((Integer) o1.getFlagAuto().getQCValue()).compareTo(o2.getFlagAuto().getQCValue());
	    }
	    return ((Boolean) o1.isErrorChecked()).compareTo(o2.isErrorChecked());
	} catch (final NullPointerException e) {
	    final UnhandledException exception = new UnhandledException("o1.getObs1Id() : " + o1.getObs1Id()
		    + " / o2.getObs1Id() : " + o2.getObs1Id() + " / o1.getParamCode() : " + o1.getParamCode()
		    + " / o2.getParamCode() : " + o2.getParamCode() + " / o1.getReferenceValue() : "
		    + o1.getReferenceValue() + " / o2.getReferenceValue() : " + o2.getReferenceValue()
		    + " / o1.getParamValueStr() : " + o1.getParamValueStr() + " / o2.getParamValueStr() : "
		    + o2.getParamValueStr() + " / o1.getDetails() : " + o1.getDetails() + " / o2.getDetails() : "
		    + o2.getDetails() + " / o1.getFlagAuto() : " + o1.getFlagAuto() + " / o2.getFlagAuto() : "
		    + o2.getFlagAuto() + " / o1.isErrorChecked() : " + o1.isErrorChecked() + " / o2.isErrorChecked() : "
		    + o2.isErrorChecked(), e);
	    return ((Boolean) o1.isErrorChecked()).compareTo(o2.isErrorChecked());
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
	if (caErrorMessageItem.getErrorMessageType() == ERROR_MESSAGE_TYPE.OBSERVATION_REFERENCE_DATA) {
	    final CADataErrorMessageItem caDataErrorMessageItem = (CADataErrorMessageItem) caErrorMessageItem;
	    final boolean displayError = caDataErrorMessageItem.isErrorOnReferenceParameter();

	    if (displayError) {
		addValues(caDataErrorMessageItem, false);
	    }
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.jdialog.ReportJTable#cellUpdatedSpecific(int, int, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    protected void cellUpdatedSpecific(final int row, final int column, final Object oldValue, final Object newValue) {

	// set old value as it is the ViewControler which manage this update
	((DefaultTableModel) getModel()).setValueAt(oldValue, row, column);

	final String obsRef = (String) values.get(row)[COLUMN_OBSERVATION_REF];
	final String variableName = (String) values.get(row)[COLUMN_PARAMETER_CODE];
	final String variableValueStr = (String) values.get(row)[COLUMN_PARAMETER_VALUE];
	final String refValStr = (String) values.get(row)[COLUMN_REF_VALUE];
	final String errorMessage = (String) values.get(row)[COLUMN_ERROR_MESSAGE];
	final int refLevel = ((CADataErrorMessageItem) errorMessages.get(row)).getReferenceIndex();

	Number variableValue = null;
	if ((variableValueStr != null) && !"-".equals(variableValueStr)) {
	    try {
		variableValue = Double.parseDouble(variableValueStr);
	    } catch (final NumberFormatException e) {
		// TODO : Parse Date to get Long
	    }
	}

	switch (column) {
	case COLUMN_CHECKBOX:
	    try {
		((DataViewImpl) commonViewImpl).updateIsCheckedforErrorMessage(obsRef, variableName, variableValue,
			variableValueStr, refLevel, refValStr, errorMessage, (Boolean) newValue, (Boolean) oldValue);
	    } catch (final Exception e) {
		SC3Logger.LOGGER.error(Messages.getMessage("bpc-controller.metadata-report-changes-error"));
	    }
	    break;
	case COLUMN_QC_MANUAL:
	    final QCValues newQC = QCValues.getQCValuesFromString(newValue.toString());

	    try {
		((DataViewImpl) commonViewImpl).updateQCforVariable(obsRef, variableName, refLevel, newQC);
	    } catch (final Exception e) {
		SC3Logger.LOGGER.error(Messages.getMessage("bpc-controller.metadata-report-changes-error"));
	    }
	    break;
	case COLUMN_COMMENT:
	    try {
		((DataViewImpl) commonViewImpl).updateCommentErrorMessage(obsRef, variableName, variableValue,
			variableValueStr, refLevel, refValStr, errorMessage, (String) newValue, (String) oldValue);
	    } catch (final Exception e) {
		SC3Logger.LOGGER.error(Messages.getMessage("bpc-controller.metadata-report-changes-error"));
	    }
	    break;
	default:
	    // nothing to do here
	    break;
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.jdialog.ReportJTable#getSelectedErrorMessage(int)
     */
    @Override
    protected CAErrorMessageItem getSelectedErrorMessage(final int row) {

	final String obsRef = (String) values.get(row)[COLUMN_OBSERVATION_REF];
	final String variableName = (String) values.get(row)[COLUMN_PARAMETER_CODE];
	final String variableValue = (String) values.get(row)[COLUMN_PARAMETER_VALUE];
	final String refValStr = (String) values.get(row)[COLUMN_REF_VALUE];
	final String errorMessage = (String) values.get(row)[COLUMN_ERROR_MESSAGE];
	final int refLevel = ((CADataErrorMessageItem) errorMessages.get(row)).getReferenceIndex();

	for (final CAErrorMessageItem caErrorMessageItem : errorMessages) {
	    if (caErrorMessageItem instanceof CADataErrorMessageItem) {
		final CADataErrorMessageItem caDataErrorMessageItem = (CADataErrorMessageItem) caErrorMessageItem;
		if (caDataErrorMessageItem.getObs1Id().equals(obsRef)
			&& caDataErrorMessageItem.getParamCode().equals(variableName)
			&& ((caDataErrorMessageItem.getParamValueStr() == variableValue)
				|| ((caDataErrorMessageItem.getParamValueStr() != null)
					&& caDataErrorMessageItem.getParamValueStr().equals(variableValue)))
			&& (caDataErrorMessageItem.getReferenceValue().equals(refValStr)
				|| caDataErrorMessageItem.getReferenceValueToDisplayInReportDialog().equals(refValStr))
			&& caDataErrorMessageItem.getDetails().equals(errorMessage)
			&& (caDataErrorMessageItem.getReferenceIndex() == refLevel)) {
		    return caErrorMessageItem;
		}
	    }
	}
	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.jdialog.ReportJTable#getColumnNames()
     */
    @Override
    protected String[] getColumnNames() {
	final String[] columnNames = new String[NB_COLUMNS_OBSERVATION_REF_PARAMETER_DATA];
	// column 0 : contains the checkbox
	columnNames[COLUMN_CHECKBOX] = Messages.getMessage("gui.errors-dialog.table-title.checked");
	// column 1 : contains the station number
	columnNames[COLUMN_OBSERVATION_REF] = Messages.getMessage("gui.errors-dialog.table-title.observation");
	// column 2 : contains the parameter code
	columnNames[COLUMN_PARAMETER_CODE] = Messages.getMessage("gui.errors-dialog.table-title.parameter-code");
	// column 3 : contains the parameter code
	columnNames[COLUMN_PARAMETER_VALUE] = Messages.getMessage("gui.errors-dialog.table-title.parameter-value");
	// column 4 : contains the reference value
	columnNames[COLUMN_REF_VALUE] = Messages.getMessage("gui.errors-dialog.table-title.reference-value");
	// column 5 : contains the error message
	columnNames[COLUMN_ERROR_MESSAGE] = Messages.getMessage("gui.errors-dialog.table-title.error-message");
	// column 6 : contains the flag auto
	columnNames[COLUMN_QC_AUTO] = Messages.getMessage("gui.errors-dialog.table-title.flag-auto");
	// column 7 : contains the flag manual
	columnNames[COLUMN_QC_MANUAL] = Messages.getMessage("gui.errors-dialog.table-title.flag-manual");
	// column 8 : contains the comment
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
		case COLUMN_QC_MANUAL:
		case COLUMN_COMMENT:
		    isEditable = true;
		    break;
		default:
		    isEditable = false;
		    break;
		}

		return isEditable;
	    }
	};
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.jdialog.ReportJTable#getStepType()
     */
    @Override
    protected STEP_TYPE getStepType() {
	return STEP_TYPE.Q2_CONTROL_AUTO_DATA;
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

	getColumnModel().getColumn(COLUMN_OBSERVATION_REF).setMinWidth(stationColumnWidth);
	getColumnModel().getColumn(COLUMN_OBSERVATION_REF).setPreferredWidth(stationColumnWidth);
	getColumnModel().getColumn(COLUMN_OBSERVATION_REF).setMaxWidth(stationColumnWidth);

	getColumnModel().getColumn(COLUMN_PARAMETER_CODE).setMinWidth(paramColumnWidth);
	getColumnModel().getColumn(COLUMN_PARAMETER_CODE).setPreferredWidth(paramColumnWidth);
	getColumnModel().getColumn(COLUMN_PARAMETER_CODE).setMaxWidth(paramColumnWidth);

	getColumnModel().getColumn(COLUMN_PARAMETER_VALUE).setMinWidth(refColumnWidth);
	getColumnModel().getColumn(COLUMN_PARAMETER_VALUE).setPreferredWidth(refColumnWidth);
	getColumnModel().getColumn(COLUMN_PARAMETER_VALUE).setMaxWidth(refColumnWidth);

	if ((errorMessages == null) || errorMessages.isEmpty()
		|| (((CADataErrorMessageItem) errorMessages.get(0)).getReferenceValue() == null
			? ((CADataErrorMessageItem) errorMessages.get(0))
				.getReferenceValue() == ((CADataErrorMessageItem) errorMessages.get(0))
					.getReferenceValueToDisplayInReportDialog()
			: ((CADataErrorMessageItem) errorMessages.get(0)).getReferenceValue()
				.equals(((CADataErrorMessageItem) errorMessages.get(0))
					.getReferenceValueToDisplayInReportDialog()))) {
	    getColumnModel().getColumn(COLUMN_REF_VALUE).setMinWidth(refColumnWidth);
	    getColumnModel().getColumn(COLUMN_REF_VALUE).setPreferredWidth(refColumnWidth);
	    getColumnModel().getColumn(COLUMN_REF_VALUE).setMaxWidth(refColumnWidth);
	} else {
	    // The column is larger if the Reference value displayed is a date
	    getColumnModel().getColumn(COLUMN_REF_VALUE).setMinWidth(refColumnWidth * 2);
	    getColumnModel().getColumn(COLUMN_REF_VALUE).setPreferredWidth(refColumnWidth * 2);
	    getColumnModel().getColumn(COLUMN_REF_VALUE).setMaxWidth(refColumnWidth * 2);
	}
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

	getColumnModel().getColumn(COLUMN_QC_MANUAL).setCellEditor(new DefaultCellEditor(getQCComboBoxString()));
    }

}
