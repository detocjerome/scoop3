package fr.ifremer.scoop3.gui.common;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import fr.ifremer.scoop3.gui.common.jdialog.JTextAreaTableCellRenderer;
import fr.ifremer.scoop3.gui.map.MapViewImpl;
import fr.ifremer.scoop3.infra.mail.UnhandledException;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.valueAndQc.ValueAndQC;

public abstract class MetadataTable extends JTable {

    private class QCCellEditor extends DefaultCellEditor {

	private static final long serialVersionUID = 2066790821127245780L;

	public QCCellEditor() {
	    super(new JTextField());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.DefaultCellEditor#stopCellEditing()
	 */
	@Override
	public boolean stopCellEditing() {
	    try {
		// Check if the value is empty or a correct QC value
		final String strValue = ((String) getCellEditorValue()).trim();
		if (strValue.isEmpty()) {
		    return false;
		}

		final int intValue = Integer.parseInt(strValue);
		final QCValues qcValue = QCValues.getQCValues(intValue);
		// /!\ value 00 is OK if the length is not check
		if ((qcValue == null) || (strValue.length() > 1)) {
		    return false;
		}
	    } catch (final NumberFormatException e) {
		return false;
	    }
	    return super.stopCellEditing();
	}
    }

    /**
     *
     */
    private static final long serialVersionUID = 7453019664928415358L;

    /**
     * Default Title column max width
     */
    private static int titleColumnWidth;
    public static final int COLUMN_QC = 2;
    public static final int COLUMN_TITLE = 0;
    public static final int COLUMN_VALUE = 1;

    private transient MapViewImpl mapViewImpl;

    /**
     * To force the cells to be NOT editable.
     */
    protected final boolean globalCellIsEditable;

    protected MetadataSplitPane metadataSplitPane;

    static {
	try {
	    titleColumnWidth = Integer
		    .parseInt(FileConfig.getScoop3FileConfig().getString("gui.metadata-table.title-column-width"));
	} catch (final NumberFormatException nfe) {
	    titleColumnWidth = 120;
	}
    }

    /**
     * Get a new DefaultTableModel with standard columns
     *
     * @return
     */
    public static DefaultTableModel getDefaultTableModel() {
	return getDefaultTableModel(true);
    }

    /**
     * Get a new DefaultTableModel with standard columns
     *
     * @param displayQCColumn
     *            FALSE, return 2 columns. TRUE, return 3 columns
     * @return
     */
    public static DefaultTableModel getDefaultTableModel(final boolean displayQCColumn) {
	final DefaultTableModel dtm = new DefaultTableModel();
	dtm.addColumn("COLUMN_TITLE");
	dtm.addColumn("COLUMN_VALUE");
	if (displayQCColumn) {
	    dtm.addColumn("COLUMN_QC");
	}
	return dtm;
    }

    protected MetadataTable(final boolean globalCellIsEditable) {
	super();

	this.globalCellIsEditable = globalCellIsEditable;
	mapViewImpl = null;

	// Hide Table column header
	setTableHeader(null);

	// By default, the lines are wrapped
	if (wrapLinesByDefault()) {
	    setDefaultRenderer(Object.class, new JTextAreaTableCellRenderer());
	}

	// Stop editing if the focus on the JTable is lost
	// Source :
	// http://tips4java.wordpress.com/2008/12/12/table-stop-editing/
	putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    /**
     * Backup updates if needed. This method needs to be override.
     *
     * @param saveFile
     *
     * @return TRUE if at least one update has been done by the operator.
     */
    public boolean backupUpdates(final boolean saveFile) {
	return false;
    }

    /**
     * Check if there is at least 1 QC_4 in the metadata. This method needs to be override.
     *
     * @return TRUE if at least one QC_4 is present in the Metadata
     */
    public boolean containsAtLeastOneMetadataWithQC4() {
	return false;
    }

    /**
     * /!\ this method has to be override
     *
     * @return a Panel with buttons if needed. Or NULL if no buttons.
     */
    public JPanel getButtonsPanel() {
	return null;
    }

    @Override
    public Object getValueAt(final int row, final int column) {
	final int modelColumn = ((column <= (getColumnCount() - 1)) && (column >= 0))
		? convertColumnIndexToModel(column) : -1;

	Object toReturn = super.getValueAt(row, column);
	if ((modelColumn == COLUMN_QC) && (toReturn instanceof QCValues)) {
	    toReturn = ((QCValues) toReturn).getQCValue();
	}

	return toReturn;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {

	if (!globalCellIsEditable) {
	    return false;
	}

	int modelRow = Integer.MIN_VALUE;
	int modelColumn = Integer.MIN_VALUE;

	try {
	    modelColumn = convertColumnIndexToModel(columnIndex);
	    modelRow = convertRowIndexToModel(rowIndex);
	} catch (final Exception e) {
	    final UnhandledException exception = new UnhandledException(
		    "row  : " + rowIndex + " / column : " + columnIndex + " / getColumnCount() : " + getColumnCount(),
		    e);
	}

	if (modelColumn == COLUMN_TITLE) {
	    return false;
	}
	// QC cells are editable only if there is already a QC in the cell
	if (modelColumn == COLUMN_QC) {
	    final Object valueAt = getModel().getValueAt(modelRow, modelColumn);
	    return (valueAt != null);
	}
	return true;
    }

    @Override
    public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) {

	int modelRow = Integer.MIN_VALUE;
	int modelColumn = Integer.MIN_VALUE;

	try {
	    modelRow = convertRowIndexToModel(row);
	    modelColumn = convertColumnIndexToModel(column);
	} catch (final Exception e) {
	    // do nothing
	}

	try {
	    final Component comp = super.prepareRenderer(renderer, modelRow, modelColumn);
	    comp.setBackground(Color.white);
	    comp.setForeground(Color.black);

	    // Manage the color of the QC column
	    if (modelColumn == COLUMN_QC) {
		final Object valueAt = getModel().getValueAt(modelRow, modelColumn);

		if (valueAt != null) {
		    final QCValues qcValue = getQCValueFromValueInColumn(valueAt);
		    if (qcValue != null) {
			final Color backgroundColor = qcValue.getColor();
			final Color foregroundColor = qcValue.getForegroundColor();
			comp.setBackground(backgroundColor);
			comp.setForeground(foregroundColor);
		    } else {
			comp.setBackground(Color.BLACK);
			comp.setForeground(Color.RED);
		    }
		}
	    }

	    return comp;
	} catch (final Exception e) {
	    return null;
	}
    }

    /**
     * @param mapViewImpl
     *            the mapViewImpl to set
     */
    public void setMapViewImpl(final MapViewImpl mapViewImpl) {
	this.mapViewImpl = mapViewImpl;
    }

    /**
     * @param metadataSplitPane
     *            the metadataSplitPane to set
     */
    public void setMetadataSplitPane(final MetadataSplitPane metadataSplitPane) {
	this.metadataSplitPane = metadataSplitPane;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JTable#setModel(javax.swing.table.TableModel)
     */
    @Override
    public void setModel(final TableModel dataModel) {
	super.setModel(dataModel);

	if (getColumnModel().getColumnCount() > COLUMN_QC) {
	    // Set QC Column max size
	    getColumnModel().getColumn(COLUMN_QC).setMaxWidth(20);

	    // Align QC Column
	    final DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
	    getColumnModel().getColumn(COLUMN_QC).setCellRenderer(centerRenderer);

	    // Use the QC Cell Editor checker
	    final TableCellEditor qcCellEditor = getQCCellEditor();
	    if (qcCellEditor != null) {
		getColumnModel().getColumn(COLUMN_QC).setCellEditor(qcCellEditor);
	    }
	}

	if (getColumnModel().getColumnCount() > COLUMN_TITLE) {
	    getColumnModel().getColumn(COLUMN_TITLE).setMaxWidth(titleColumnWidth);
	    getColumnModel().getColumn(COLUMN_TITLE).setMinWidth(titleColumnWidth);
	    getColumnModel().getColumn(COLUMN_TITLE).setWidth(titleColumnWidth);
	}
    }

    /**
     * Update the model with the given dataset
     *
     * @param dataset
     */
    public abstract void updateTableWithDataset(final Dataset dataset);

    /**
     * Update the model with the given Observation
     *
     * @param observation
     */
    public abstract void updateTableWithObservation(final Observation observation);

    /**
     * @return the mapViewImpl
     */
    protected MapViewImpl getMapViewImpl() {
	return mapViewImpl;
    }

    /**
     * @return the QCCellEditor
     */
    protected TableCellEditor getQCCellEditor() {
	return new QCCellEditor();
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
     * @return TRUE if by default, the lines are wrapped.
     */
    protected boolean wrapLinesByDefault() {
	return true;
    }

    public void triggerUpdateTableWithDataset(final Dataset dataset, final String platformCode,
	    final List<HashMap<String, ValueAndQC>> metadatas) {
    }
}
