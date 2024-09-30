package fr.ifremer.scoop3.gui.data.datatable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import fr.ifremer.scoop3.gui.data.datatable.update.DataTableCellDeleted;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableCellQCAndValueUpdate;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableCellQCUpdate;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableQCAndValueUpdate;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableQCUpdate;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableRowsDeleted;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableUpdateAbstract;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.Timeserie;
import fr.ifremer.scoop3.model.parameter.Parameter;
import fr.ifremer.scoop3.model.parameter.TimeParameter;

public class DataTableModel extends AbstractTableModel {

    /**
     *
     */
    private static final long serialVersionUID = 4533694903918784568L;
    /**
     * Current observation ...
     */
    private final Observation currentObservation;
    /**
     * Data
     */
    private final transient List<List<Object>> data;
    /**
     * Columns header
     */
    private final String[] header;
    /**
     * QCs
     */
    private final List<List<QCValues>> qcs;

    // TODO Calcule une liste headerList a partir de parameterOrder passe en parametre. SI parameterOrder ne sert qu'a
    // ca il faudra supprimer le code inherent et modifier le calcul de headerList en consequence.
    public DataTableModel(final Observation currentObservation, final Map<String, List<String>> parametersOrder) {

	this.currentObservation = currentObservation;
	data = new ArrayList<>();
	qcs = new ArrayList<>();

	/*
	 * Construction de la liste de parametre a afficher Parametre de reference Parametres mesurees
	 */

	final List<String> headerList = new ArrayList<>();

	if ((parametersOrder == null) || !parametersOrder.containsKey(currentObservation.getReference())) {
	    headerList.add(currentObservation.getReferenceParameter().getCode()
		    + (Dataset.getInstance().getParameterDataModeMap().get(currentObservation.getId())
			    .get(currentObservation.getReferenceParameter().getCode()) == null ? ""
				    : " (" + (Dataset.getInstance().getParameterDataModeMap()
					    .get(currentObservation.getId()) != null
						    ? (Dataset.getInstance()
							    .getParameterDataModeMap().get(currentObservation.getId())
							    .get(currentObservation.getReferenceParameter().getCode())
							    + ")")
						    : "")));
	    for (final String parameterName : currentObservation.getOceanicParameters().keySet()) {
		if (!headerList.contains(parameterName)) {
		    headerList.add(parameterName
			    + (Dataset.getInstance().getParameterDataModeMap().get(currentObservation.getId()) != null
				    ? (Dataset.getInstance().getParameterDataModeMap().get(currentObservation.getId())
					    .get(parameterName) == null ? ""
						    : " (" + Dataset.getInstance().getParameterDataModeMap()
							    .get(currentObservation.getId()).get(parameterName) + ")")
				    : ""));
		}
	    }
	} else {
	    // TODO ce if compare un code parametre a un code observation donc toujours vrai ?
	    if (!parametersOrder.containsKey(currentObservation.getReferenceParameter().getCode())) {
		headerList
			.add(currentObservation.getReferenceParameter().getCode()
				+ (Dataset.getInstance().getParameterDataModeMap()
					.get(currentObservation.getId()) != null
						? (Dataset
							.getInstance().getParameterDataModeMap()
							.get(currentObservation.getId())
							.get(currentObservation.getReferenceParameter()
								.getCode()) == null ? ""
									: " (" + Dataset.getInstance()
										.getParameterDataModeMap()
										.get(currentObservation.getId())
										.get(currentObservation
											.getReferenceParameter()
											.getCode())
										+ ")")
						: ""));
	    }
	    if (currentObservation instanceof Timeserie) {
		if (!parametersOrder.containsKey(Observation.LATITUDE_VAR_NAME)) {
		    headerList.add(currentObservation.getLatitude().getCode()
			    + (Dataset.getInstance().getParameterDataModeMap().get(currentObservation.getId()) != null
				    ? (Dataset.getInstance().getParameterDataModeMap().get(currentObservation.getId())
					    .get(currentObservation.getLatitude().getCode()) == null ? ""
						    : " (" + Dataset.getInstance().getParameterDataModeMap()
							    .get(currentObservation.getId())
							    .get(currentObservation.getLatitude().getCode()) + ")")
				    : ""));
		}
		if (!parametersOrder.containsKey(Observation.LONGITUDE_VAR_NAME)) {
		    headerList.add(currentObservation.getLongitude().getCode()
			    + (Dataset.getInstance().getParameterDataModeMap().get(currentObservation.getId()) != null
				    ? (Dataset.getInstance().getParameterDataModeMap().get(currentObservation.getId())
					    .get(currentObservation.getLongitude().getCode()) == null ? ""
						    : " (" + Dataset.getInstance().getParameterDataModeMap()
							    .get(currentObservation.getId())
							    .get(currentObservation.getLongitude().getCode()) + ")")
				    : ""));
		}
	    }

	    for (final String parameterName : parametersOrder.get(currentObservation.getReference())) {
		// Gestion des parametres spatio temporel
		if (currentObservation.getOceanicParameters().get(parameterName) == null) {
		    if ((((currentObservation instanceof Timeserie) && parameterName.equalsIgnoreCase("YEAR"))
			    || !(currentObservation instanceof Timeserie))
			    && (!headerList.contains(currentObservation.getReferenceParameter().getCode()))) {
			headerList
				.add(currentObservation.getReferenceParameter().getCode() + (Dataset.getInstance()
					.getParameterDataModeMap().get(currentObservation.getId()) != null
						? (Dataset
							.getInstance().getParameterDataModeMap()
							.get(currentObservation.getId())
							.get(currentObservation.getReferenceParameter()
								.getCode()) == null ? ""
									: " (" + Dataset.getInstance()
										.getParameterDataModeMap()
										.get(currentObservation.getId())
										.get(currentObservation
											.getReferenceParameter()
											.getCode())
										+ ")")
						: ""));
		    }
		    // Gestion des parametres observes
		} else {
		    if (!headerList.contains(parameterName)) {
			headerList.add(parameterName + (Dataset.getInstance().getParameterDataModeMap()
				.get(currentObservation.getId()) != null
					? (Dataset.getInstance().getParameterDataModeMap()
						.get(currentObservation.getId()).get(parameterName) == null ? ""
							: " (" + Dataset.getInstance().getParameterDataModeMap()
								.get(currentObservation.getId()).get(parameterName)
								+ ")")
					: ""));
		    }
		}
	    }
	}

	final int maxLevelIndex = currentObservation.getReferenceParameter().getValues().size();

	for (int levelIndex = 0; levelIndex < maxLevelIndex; levelIndex++) {
	    final List<Object> dataForRow = new ArrayList<Object>();
	    final List<QCValues> qcsForRow = new ArrayList<QCValues>();

	    for (int index = 0; index < headerList.size(); index++) {
		String parameterName = null;
		if (headerList.get(index).contains("(")) {
		    parameterName = headerList.get(index).split("\\(")[0].trim();
		} else {
		    parameterName = headerList.get(index);
		}

		Parameter<?> oceanicParameter;
		if (currentObservation.getReferenceParameter().getCode().equals(parameterName)) {
		    oceanicParameter = currentObservation.getReferenceParameter();
		} else if (Observation.LATITUDE_VAR_NAME.equals(parameterName)) {
		    oceanicParameter = currentObservation.getLatitude();
		} else if (Observation.LONGITUDE_VAR_NAME.equals(parameterName)) {
		    oceanicParameter = currentObservation.getLongitude();
		} else {
		    oceanicParameter = currentObservation.getOceanicParameter(parameterName);
		}

		if (levelIndex < oceanicParameter.getValues().size()) {
		    dataForRow.add(oceanicParameter.getValues().get(levelIndex));
		    qcsForRow.add(oceanicParameter.getQcValues().get(levelIndex));
		} else {
		    // Used by BPC
		    dataForRow.add(Parameter.DOUBLE_EMPTY_VALUE);
		    qcsForRow.add(QCValues.QC_9);
		}

	    }
	    data.add(dataForRow);
	    qcs.add(qcsForRow);
	}

	header = headerList.toArray(new String[0]);
    }

    /**
     * Cancel the last updates for this variables
     *
     * @param lastUpdatesForVariables
     */
    public void cancelOneUpdate(final DataTableUpdateAbstract lastUpdatesForVariables) {
	SC3Logger.LOGGER.trace("cancelOneUpdate start");

	switch (lastUpdatesForVariables.getDataTableUpdateType()) {
	case QC_AND_VALUE_UPDATE:
	    // For each variables
	    for (final String variableName : ((DataTableQCAndValueUpdate) lastUpdatesForVariables)
		    .getUpdatesForVariables().keySet()) {
		final int columnIndex = getColumnIndexForVariable(variableName);

		// reset the old QCValue
		for (final DataTableCellQCAndValueUpdate dataTableCellQCAndValueUpdate : ((DataTableQCAndValueUpdate) lastUpdatesForVariables)
			.getUpdatesForVariables().get(variableName)) {
		    final int rowIndex = dataTableCellQCAndValueUpdate.getRowIndex();
		    final QCValues oldQCValue = dataTableCellQCAndValueUpdate.getOldQCValue();
		    qcs.get(rowIndex).set(columnIndex, oldQCValue);

		    final Object oldValue = dataTableCellQCAndValueUpdate.getOldValue();
		    data.get(rowIndex).set(columnIndex, oldValue);

		    fireTableCellUpdated(rowIndex, columnIndex);
		}
	    }
	    break;
	case QC_UPDATE:
	    // For each variables
	    for (final String variableName : ((DataTableQCUpdate) lastUpdatesForVariables).getUpdatesForVariables()
		    .keySet()) {
		final int columnIndex = getColumnIndexForVariable(variableName);

		// reset the old QCValue
		for (final DataTableCellQCUpdate dataTableCellQCUpdate : ((DataTableQCUpdate) lastUpdatesForVariables)
			.getUpdatesForVariables().get(variableName)) {
		    final int rowIndex = dataTableCellQCUpdate.getRowIndex();
		    final QCValues oldQCValue = dataTableCellQCUpdate.getOldQCValue();
		    qcs.get(rowIndex).set(columnIndex, oldQCValue);

		    fireTableCellUpdated(rowIndex, columnIndex);
		}
	    }
	    break;
	case ROW_DELETE:
	    int lastRowIndex = -1;
	    List<Object> oldValues = new ArrayList<>();
	    List<QCValues> oldQCs = new ArrayList<>();

	    // The DataTableCellDeleted are already sorted
	    for (final DataTableCellDeleted dataTableCellDeleted : ((DataTableRowsDeleted) lastUpdatesForVariables)
		    .getCellsDeleted()) {
		final int rowIndex = dataTableCellDeleted.getRowIndex();
		final QCValues qcValue = dataTableCellDeleted.getQc();
		final Object value = dataTableCellDeleted.getValue();

		if ((lastRowIndex != rowIndex) && (lastRowIndex != -1)) {
		    data.add(lastRowIndex, oldValues);
		    qcs.add(lastRowIndex, oldQCs);
		    fireTableRowsInserted(lastRowIndex, lastRowIndex);

		    oldValues = new ArrayList<>();
		    oldQCs = new ArrayList<>();
		}

		oldValues.add(value);
		oldQCs.add(qcValue);

		lastRowIndex = rowIndex;
	    }
	    if (!oldValues.isEmpty()) {
		data.add(lastRowIndex, oldValues);
		qcs.add(lastRowIndex, oldQCs);
		fireTableRowsInserted(lastRowIndex, lastRowIndex);
	    }
	    break;
	}
	SC3Logger.LOGGER.trace("cancelOneUpdate stop");
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
	if (columnIndex == 0) {
	    if (currentObservation.getReferenceParameter() instanceof TimeParameter) {
		return Date.class;
	    }
	    return Double.class;
	} else {
	    return Double.class;
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
	return header.length;
    }

    /**
     * @param variableName
     * @return the column index
     */
    public int getColumnIndexForVariable(final String variableName) {
	int columnIndex = -1;
	for (int index = 0; (index < header.length) && (columnIndex == -1); index++) {
	    if (header[index].equals(variableName)) {
		columnIndex = index;
	    }
	}
	return columnIndex;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(final int column) {
	return header[column];
    }

    /**
     * @param column
     * @return the Parameter linked to the given column number
     */
    public Parameter<? extends Number> getParameter(final int column) {
	return getParameter(getColumnName(column));
    }

    /**
     * @param variableName
     * @return
     */
    public Parameter<? extends Number> getParameter(final String variableName) {
	Parameter<? extends Number> toReturn;

	if (currentObservation.getReferenceParameter().getCode().equalsIgnoreCase(variableName)) {
	    toReturn = currentObservation.getReferenceParameter();
	} else if (currentObservation.getLatitude().getCode().equalsIgnoreCase(variableName)) {
	    toReturn = currentObservation.getLatitude();
	} else if (currentObservation.getLongitude().getCode().equalsIgnoreCase(variableName)) {
	    toReturn = currentObservation.getLongitude();
	} else {
	    toReturn = currentObservation.getOceanicParameter(variableName);
	}

	return toReturn;
    }

    /**
     * Return the QCValue of the Cell
     *
     * @param row
     * @param column
     * @return
     */
    public QCValues getQCValuesForCell(final int row, final int column) {
	try {
	    return qcs.get(row).get(column);
	} catch (final IndexOutOfBoundsException i) {
	    return null;
	}
    }

    /**
     * Return the QCValue of the Cell
     *
     * @param row
     * @param variableName
     * @return
     */
    public QCValues getQCValuesForCell(final int row, final String variableName) {
	return getQCValuesForCell(row, getColumnIndexForVariable(variableName));
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
	return data.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
	return data.get(rowIndex).get(columnIndex);
    }

    /**
     * @param variableName
     * @return
     */
    public boolean isReferenceParameter(final String variableName) {
	return (currentObservation.getReferenceParameter().getCode().equalsIgnoreCase(variableName));
    }

    /**
     * Remove data and QC for a given row index
     *
     * @param rowIndex
     */
    public List<DataTableCellDeleted> removeDataForRowIndex(final int rowIndex) {
	final List<DataTableCellDeleted> rowDeleted = new ArrayList<>();

	// Memorize old values
	for (int index = 0; index < header.length; index++) {
	    final DataTableCellDeleted dataTableRowDeleted = new DataTableCellDeleted(header[index], rowIndex,
		    data.get(rowIndex).get(index), qcs.get(rowIndex).get(index));
	    rowDeleted.add(dataTableRowDeleted);
	}

	// Update model
	data.remove(rowIndex);
	qcs.remove(rowIndex);

	// Update table
	fireTableRowsDeleted(rowIndex, rowIndex);

	return rowDeleted;
    }

    /**
     * Update the QC
     *
     * @param variableName
     * @param rowIndex
     * @param newQCValue
     *
     * @return the DataTableCellQCUpdate
     */
    public DataTableCellQCUpdate updateQC(final String variableName, final int rowIndex, final QCValues newQCValue) {
	final int columnIndex = getColumnIndexForVariable(variableName);

	// Backup the old QC
	final QCValues oldQCValue = qcs.get(rowIndex).get(columnIndex);
	// Set the new QC
	qcs.get(rowIndex).set(columnIndex, newQCValue);

	// Update the table
	fireTableCellUpdated(rowIndex, columnIndex);

	// Update the table
	return new DataTableCellQCUpdate(rowIndex, oldQCValue, newQCValue);
    }

    /**
     * Update the QC and the value
     *
     * @param variableName
     * @param rowIndex
     * @param newQCValue
     * @param newValue
     * @return
     */
    public DataTableCellQCAndValueUpdate updateQCandValue(final String variableName, final int rowIndex,
	    final QCValues newQCValue, final Number newValue) {
	final int columnIndex = getColumnIndexForVariable(variableName);

	// Backup the old QC
	final QCValues oldQCValue = qcs.get(rowIndex).get(columnIndex);
	// Backup the old Value
	final Object oldValue = data.get(rowIndex).get(columnIndex);

	if ((oldQCValue != newQCValue) || (oldValue != newValue)) {
	    // Set the new QC
	    qcs.get(rowIndex).set(columnIndex, newQCValue);
	    // Set the new Value
	    data.get(rowIndex).set(columnIndex, newValue);

	    // Update the table
	    fireTableCellUpdated(rowIndex, columnIndex);

	    return new DataTableCellQCAndValueUpdate(rowIndex, oldQCValue, newQCValue, oldValue, newValue);
	} else {
	    return null;
	}
    }
}
