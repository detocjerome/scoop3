package fr.ifremer.scoop3.gui.data.datatable.update;

import fr.ifremer.scoop3.model.QCValues;

public class DataTableCellQCUpdate {

    private final QCValues newQCValue;
    private final QCValues oldQCValue;
    private final int rowIndex;

    /**
     * Default constructor
     * 
     * @param rowIndex
     * @param oldQCValue
     * @param newQCValue
     */
    public DataTableCellQCUpdate(final int rowIndex, final QCValues oldQCValue, final QCValues newQCValue) {
	this.rowIndex = rowIndex;
	this.oldQCValue = oldQCValue;
	this.newQCValue = newQCValue;
    }

    /**
     * @return the newQCValue
     */
    public QCValues getNewQCValue() {
	return newQCValue;
    }

    /**
     * @return the oldQCValue
     */
    public QCValues getOldQCValue() {
	return oldQCValue;
    }

    /**
     * @return the rowIndex
     */
    public int getRowIndex() {
	return rowIndex;
    }
}
