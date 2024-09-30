package fr.ifremer.scoop3.gui.data.datatable.update;

import fr.ifremer.scoop3.model.QCValues;

public class DataTableCellDeleted {

    private final QCValues qc;
    private final Object value;
    private final String variableName;
    final int rowIndex;

    public DataTableCellDeleted(final String variableName, final int rowIndex, final Object value, final QCValues qc) {
	this.variableName = variableName;
	this.rowIndex = rowIndex;
	this.value = value;
	this.qc = qc;
    }

    /**
     * @return the qc
     */
    public QCValues getQc() {
	return qc;
    }

    /**
     * @return the rowIndex
     */
    public int getRowIndex() {
	return rowIndex;
    }

    /**
     * @return the value
     */
    public Object getValue() {
	return value;
    }

    /**
     * @return the variableName
     */
    public String getVariableName() {
	return variableName;
    }

}
