package fr.ifremer.scoop3.gui.data.datatable;

import java.awt.Component;
import java.util.Date;

import javax.swing.JTable;

import fr.ifremer.scoop3.infra.tools.Conversions;

public class DataTableCellRendererForDate extends DataTableCellRendererForObject {

    /**
     * 
     */
    private static final long serialVersionUID = 4549803224340821924L;

    public DataTableCellRendererForDate(final DataTableModel dataTableModel) {
	super(dataTableModel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.ifremer.scoop3.gui.data.datatable.DataTableCellRendererForObject#getTableCellRendererComponent(javax.swing
     * .JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(final JTable table, Object value, final boolean isSelected,
	    final boolean hasFocus, final int row, final int column) {
	value = Conversions.formatDateAndHourMin(new Date((Long) value));
	return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

}
