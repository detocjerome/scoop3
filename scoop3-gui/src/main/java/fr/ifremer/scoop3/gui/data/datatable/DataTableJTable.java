package fr.ifremer.scoop3.gui.data.datatable;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import fr.ifremer.scoop3.core.validateParam.ValidatedDataParameterManager;
import fr.ifremer.scoop3.gui.core.Scoop3Frame;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableUpdateAbstract;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.model.QCValues;

public class DataTableJTable extends JTable implements MouseListener {

    private static String formatForDouble = "#0.000";
    private static String formatForRefParamDouble = "#0.000";

    private static final long serialVersionUID = -906144891192763851L;

    private final DataTableDialog dataTableDialog;
    private final DataTableModel dataTableModel;
    private final DataTablePopupMenu dataTablePopupMenu;
    private final DataTableSelectionModel dataTableSelectionModel;

    /**
     * @return the formatForDouble
     */
    public static String getFormatForDouble() {
	return formatForDouble;
    }

    /**
     * @return the formatForRefParamDouble
     */
    public static String getFormatForRefParamDouble() {
	return formatForRefParamDouble;
    }

    /**
     * @param formatForDouble
     *            the formatForDouble to set
     * @param formatForRefParamDouble
     *            the formatForRefParamDouble to set
     */
    public static void setFormatForDouble(final String formatForDouble, final String formatForRefParamDouble) {
	DataTableJTable.formatForDouble = formatForDouble;
	DataTableJTable.formatForRefParamDouble = formatForRefParamDouble;
    }

    /**
     * Default constructor
     *
     * @param dataTableDialog
     * @param scoop3Frame
     * @param dataTableModel
     * @param qcValuesSettable
     */
    public DataTableJTable(final DataTableDialog dataTableDialog, final Scoop3Frame scoop3Frame,
	    final DataTableModel dataTableModel, final QCValues[] qcValuesSettable) {
	super(dataTableModel);

	this.dataTableDialog = dataTableDialog;
	this.dataTableModel = dataTableModel;

	if (!FileConfig.getScoop3FileConfig().getString("application.title").trim().equals("Scoop3Explorer")) {
	    dataTablePopupMenu = new DataTablePopupMenu(this, scoop3Frame, qcValuesSettable);
	} else {
	    dataTablePopupMenu = null;
	}

	initRenderer();

	setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	setRowSelectionAllowed(true);
	setColumnSelectionAllowed(true);

	addMouseListener(this);

	dataTableSelectionModel = new DataTableSelectionModel(this);
	getColumnModel().setSelectionModel(dataTableSelectionModel);

	final TableCellRenderer renderer = (final JTable table, final Object value, final boolean isSelected,
		final boolean hasFocus, final int row, final int column) -> {
	    return (JLabel) value;
	};

	final int nbColumns = getColumnCount();
	for (int columnIndex = 0; columnIndex < nbColumns; columnIndex++) {
	    final TableColumn column = getColumnModel().getColumn(columnIndex);
	    final String parameterName = dataTableModel.getColumnName(columnIndex);

	    final JLabel title = new JLabel(parameterName, JLabel.CENTER);
	    title.setHorizontalTextPosition(JLabel.LEFT);
	    title.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	    column.setHeaderRenderer(renderer);
	    column.setHeaderValue(title);

	    if (ValidatedDataParameterManager.getInstance().isValidated(parameterName)) {
		title.setIcon(ValidatedDataParameterManager.getValidatedImageIcon());
	    }
	}
    }

    /**
     * Memorize a new update
     *
     * @param updatesForVariables
     */
    public void addUpdatesForVariables(final DataTableUpdateAbstract updatesForVariables) {
	dataTableDialog.addUpdatesForVariables(updatesForVariables);
    }

    /**
     * Cancel the last updates for this variables
     *
     * @param lastUpdatesForVariables
     */
    public void cancelOneUpdate(final DataTableUpdateAbstract lastUpdatesForVariables) {
	dataTableModel.cancelOneUpdate(lastUpdatesForVariables);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JTable#getCellRenderer(int, int)
     */
    @Override
    public TableCellRenderer getCellRenderer(final int row, final int column) {
	if ((column == 0) && dataTableModel.getColumnClass(column).equals(Double.class)) {
	    return new DataTableCellRendererForDouble(dataTableModel, formatForRefParamDouble);
	}
	return super.getCellRenderer(row, column);
    }

    /**
     * @return the dataTableModel
     */
    public DataTableModel getDataTableModel() {
	return dataTableModel;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
	if (SwingUtilities.isRightMouseButton(e) && (dataTablePopupMenu != null)) {
	    dataTablePopupMenu.show(e.getComponent(), e.getX(), e.getY());
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
	// empty method
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(final MouseEvent e) {
	// empty method
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(final MouseEvent e) {
	if (SwingUtilities.isLeftMouseButton(e) && (!(e.isControlDown() || e.isAltDown()))) {
	    dataTableSelectionModel.setMouseNewSelection(true);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
	dataTableSelectionModel.setMouseNewSelection(true);
    }

    /**
     * Set the default renderer depending on the column class (see DataTableModel.getColumnClass(...))
     */
    private void initRenderer() {
	setDefaultRenderer(Object.class, new DataTableCellRendererForObject(dataTableModel));
	setDefaultRenderer(Double.class, new DataTableCellRendererForDouble(dataTableModel, formatForDouble));
	setDefaultRenderer(Date.class, new DataTableCellRendererForDate(dataTableModel));
    }
}
