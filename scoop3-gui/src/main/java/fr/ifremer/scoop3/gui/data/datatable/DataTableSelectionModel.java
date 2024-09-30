package fr.ifremer.scoop3.gui.data.datatable;

import java.util.HashMap;

import javax.swing.DefaultListSelectionModel;

import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.Parameter;
import fr.ifremer.scoop3.model.parameter.Parameter.LINK_PARAM_TYPE;

public class DataTableSelectionModel extends DefaultListSelectionModel {

    /**
     *
     */
    private static final long serialVersionUID = -3688611235534807027L;

    /**
     * The column selectability
     */
    private final HashMap<Integer, Boolean> columnSelectability;
    /**
     * Reference on the DataTableJTable
     */
    private final DataTableJTable dataTableJTable;
    /**
     * Used by the selection model
     */
    private Boolean mouseNewSelection = true;

    /**
     * Default constructor
     *
     * @param dataTableJTable
     */
    public DataTableSelectionModel(final DataTableJTable dataTableJTable) {
	this.dataTableJTable = dataTableJTable;

	columnSelectability = new HashMap<>();

	initSelectability(dataTableJTable.getDataTableModel());
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.DefaultListSelectionModel#addSelectionInterval(int, int)
     */
    @Override
    public void addSelectionInterval(final int index0, final int index1) {
	for (int index = index0; index <= index1; index++) {
	    if (isSelectable(index)) {
		super.addSelectionInterval(index, index);
	    }
	}
    }

    /**
     * @param mouseNewSelection
     *            the mouseNewSelection to set
     */
    public void setMouseNewSelection(final boolean mouseNewSelection) {
	synchronized (this.mouseNewSelection) {
	    this.mouseNewSelection = mouseNewSelection;
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.DefaultListSelectionModel#setSelectionInterval(int, int)
     */
    @Override
    public void setSelectionInterval(final int index0, final int index1) {
	if (mouseNewSelection.booleanValue()) {
	    setMouseNewSelection(false);
	    if (isSelectable(index0)) {
		super.setSelectionInterval(index0, index0);
	    }
	}
	if (index0 <= index1) {
	    addSelectionInterval(index0, index1);
	} else {
	    addSelectionInterval(index1, index0);
	}
    }

    /**
     * Init the selectability of the column
     *
     * @param dataTableModel
     */
    private void initSelectability(final DataTableModel dataTableModel) {
	final int nbColumn = dataTableModel.getColumnCount();
	for (int column = 0; column < nbColumn; column++) {
	    final Parameter<? extends Number> parameter = dataTableModel.getParameter(column);

	    boolean isSelectable = true;
	    if (parameter instanceof OceanicParameter) {
		isSelectable = (((OceanicParameter) parameter).getLinkParamType() != LINK_PARAM_TYPE.COMPUTED_CONTROL);
	    }

	    columnSelectability.put(column, isSelectable);
	}
    }

    /**
     * @param columnIndex
     * @return true if the column can be selected
     */
    private boolean isSelectable(final int columnIndex) {
	final int realColumnIndex = dataTableJTable.convertColumnIndexToModel(columnIndex);
	return columnSelectability.get(realColumnIndex);
    }

}
