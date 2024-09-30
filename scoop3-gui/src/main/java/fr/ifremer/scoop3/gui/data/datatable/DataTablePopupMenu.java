package fr.ifremer.scoop3.gui.data.datatable;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import fr.ifremer.scoop3.gui.core.Scoop3Frame;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableCellDeleted;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableCellQCAndValueUpdate;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableCellQCUpdate;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableQCUpdate;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableRowsDeleted;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.tools.Conversions;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.Parameter;
import fr.ifremer.scoop3.model.parameter.ParametersRelationships;

public class DataTablePopupMenu extends JPopupMenu {

    public enum POPUP_ITEM_ENUM {
	DELETE_LINE, //
	SET_TO_DEFAULT_VALUE, //
	UPDATE_QC, //
    }

    /**
     * Change QC Action
     */
    private class Scoop3DataTableChangeQCJMenuItem extends Scoop3DataTableJMenuItem {

	private static final long serialVersionUID = 5302049133589095187L;

	private final QCValues qcValue;

	public Scoop3DataTableChangeQCJMenuItem(final QCValues qcValue, final Scoop3Frame scoop3Frame) {
	    super((scoop3Frame.getTitle().contains(Messages.getMessage("bpc-controller.application-title")) ? "bpc-gui"
		    : "coriolis-gui") + ".tooltip-change_qc_to_" + qcValue.toString());
	    this.qcValue = qcValue;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {

	    final int minRowIndex = dataTableJTable.getSelectionModel().getMinSelectionIndex();
	    final int maxRowIndex = dataTableJTable.getSelectionModel().getMaxSelectionIndex();

	    final HashMap<String, ArrayList<DataTableCellQCUpdate>> updatesForVariables = new HashMap<>();

	    for (int rowIndex = maxRowIndex; rowIndex >= minRowIndex; rowIndex--) {

		// Check if the row is selected for at least one cell
		if (dataTableJTable.getSelectionModel().isSelectedIndex(rowIndex)) {
		    // Search which columns are selected
		    for (int columnIndex = 0; columnIndex < dataTableJTable.getColumnCount(); columnIndex++) {
			final int realColumnIndex = dataTableJTable.convertColumnIndexToModel(columnIndex);

			// Update the QC if the cell is selected
			if (dataTableJTable.isCellSelected(rowIndex, realColumnIndex)) {
			    // /!\ use variableName when calling updateQC(...) due to a random PB when moving
			    // columns
			    final String variableName = dataTableJTable.getColumnName(realColumnIndex);

			    final QCValues parameterQC = dataTableJTable.getDataTableModel()
				    .getQCValuesForCell(rowIndex, variableName);

			    if ((parameterQC != qcValue) && (parameterQC != null)) {
				final Parameter<? extends Number> parameter = dataTableJTable.getDataTableModel()
					.getParameter(variableName);

				// If the value is equal to the default value, it is not allowed to set a new QC
				if (dataTableJTable.getDataTableModel().getValueAt(rowIndex,
					dataTableJTable.getDataTableModel()
						.getColumnIndexForVariable(variableName)) instanceof Double) {
				    final double currentValue = (Double) dataTableJTable.getDataTableModel()
					    .getValueAt(rowIndex, dataTableJTable.getDataTableModel()
						    .getColumnIndexForVariable(variableName));
				    final double defaultValue = (parameter.getFillValue() == null) ? Double.MAX_VALUE
					    : (Double) parameter.getFillValue();
				    if ((parameter.getFillValue() == null) || (currentValue != defaultValue)) {
					changeQCForVariable(variableName, rowIndex, qcValue, updatesForVariables);

					updateQCForComputedParameters(parameter, rowIndex, updatesForVariables);

					updateQCForFathers(parameter, rowIndex, updatesForVariables);
				    }
				} else if (dataTableJTable.getDataTableModel().getValueAt(rowIndex, dataTableJTable
					.getDataTableModel().getColumnIndexForVariable(variableName)) instanceof Long) {
				    final long currentValue = (Long) dataTableJTable.getDataTableModel()
					    .getValueAt(rowIndex, dataTableJTable.getDataTableModel()
						    .getColumnIndexForVariable(variableName));
				    final long defaultValue = Long.MAX_VALUE;
				    if ((parameter.getFillValue() == null) || (currentValue != defaultValue)) {
					changeQCForVariable(variableName, rowIndex, qcValue, updatesForVariables);

					updateQCForComputedParameters(parameter, rowIndex, updatesForVariables);

					updateQCForFathers(parameter, rowIndex, updatesForVariables);
				    }
				}
			    }
			}
		    }
		}
	    }
	    if (!updatesForVariables.isEmpty()) {
		final DataTableQCUpdate dataTableQCUpdate = new DataTableQCUpdate(updatesForVariables);
		dataTableJTable.addUpdatesForVariables(dataTableQCUpdate);
	    }
	}

	/**
	 * @param fatherParameter
	 * @param rowIndex
	 * @param updatesForVariables
	 */
	private void updateQCForComputedParameters(final Parameter<? extends Number> fatherParameter,
		final int rowIndex, final HashMap<String, ArrayList<DataTableCellQCUpdate>> updatesForVariables) {

	    for (final Parameter<? extends Number> computedParameter : ParametersRelationships
		    .getLinkedParameters(fatherParameter)) {

		// If the value is equal to the default value, it is not allowed to set a new QC
		if (dataTableJTable.getDataTableModel().getValueAt(rowIndex, dataTableJTable.getDataTableModel()
			.getColumnIndexForVariable(computedParameter.getCode())) != computedParameter.getFillValue()) {

		    // Get fathers variable name
		    final List<String> fathersNames = new ArrayList<>();
		    for (final Parameter<? extends Number> father : ParametersRelationships
			    .getFathers(computedParameter)) {
			fathersNames.add(father.getCode());
		    }

		    // Compute worst QCValues
		    QCValues valueToSetForComputedParameter = qcValue;
		    for (final String fathersName : fathersNames) {
			final QCValues fatherQC = dataTableJTable.getDataTableModel().getQCValuesForCell(rowIndex,
				fathersName);
			if (fatherQC != null) {
			    valueToSetForComputedParameter = QCValues.getWorstQC(valueToSetForComputedParameter,
				    fatherQC);
			}
		    }

		    changeQCForVariable(computedParameter.getCode(), rowIndex, valueToSetForComputedParameter,
			    updatesForVariables);
		}
	    }
	}

	/**
	 * @param computedParameter
	 * @param rowIndex
	 * @param updatesForVariables
	 */
	private void updateQCForFathers(final Parameter<? extends Number> computedParameter, final int rowIndex,
		final HashMap<String, ArrayList<DataTableCellQCUpdate>> updatesForVariables) {
	    if (computedParameter instanceof OceanicParameter) {

		for (final Parameter<? extends Number> fatherParameter : ParametersRelationships
			.getFathers(computedParameter)) {

		    // If the value is equal to the default value, it is not allowed to set a new QC
		    if (dataTableJTable.getDataTableModel().getValueAt(rowIndex, dataTableJTable.getDataTableModel()
			    .getColumnIndexForVariable(fatherParameter.getCode())) != fatherParameter.getFillValue()) {

			// Get fathers variable name
			final List<String> computedParametersNames = new ArrayList<>();
			for (final Parameter<? extends Number> computedParameter2 : ParametersRelationships
				.getLinkedParameters(fatherParameter)) {
			    computedParametersNames.add(computedParameter2.getCode());
			}

			// Compute worst QCValues
			QCValues valueToSetForFatherParameter = qcValue;
			for (final String computedParametersName : computedParametersNames) {
			    final QCValues computedParameterQC = dataTableJTable.getDataTableModel()
				    .getQCValuesForCell(rowIndex, computedParametersName);

			    if (computedParameterQC != null) {
				valueToSetForFatherParameter = QCValues.getWorstQC(valueToSetForFatherParameter,
					computedParameterQC);
			    }
			}

			changeQCForVariable(fatherParameter.getCode(), rowIndex, valueToSetForFatherParameter,
				updatesForVariables);
		    }
		}
	    }
	}
    }

    /**
     * Definition of the JMenuItems used for this JPopupMenu
     */
    private abstract class Scoop3DataTableJMenuItem extends JMenuItem implements ActionListener {

	private static final long serialVersionUID = -2751693527869901537L;

	public Scoop3DataTableJMenuItem(final String title) {
	    super(Messages.getMessage(title));
	    addActionListener(this);
	}

	/**
	 * Update the Enable state.
	 *
	 * @return
	 */
	public final void updateEnabled() {
	    setEnabled(isMenuEnabled());
	}

	/**
	 * @return true if the menu is enabled. By default, the menu is enabled if there is at least 1 cell selected.
	 */
	protected boolean isMenuEnabled() {
	    boolean toReturn = false;
	    final int minRowIndex = dataTableJTable.getSelectionModel().getMinSelectionIndex();
	    final int maxRowIndex = dataTableJTable.getSelectionModel().getMaxSelectionIndex();

	    for (int rowIndex = maxRowIndex; (rowIndex >= minRowIndex) && (!toReturn); rowIndex--) {
		for (int columnIndex = 0; (columnIndex < dataTableJTable.getColumnCount())
			&& (!toReturn); columnIndex++) {
		    final int realColumnIndex = dataTableJTable.convertColumnIndexToModel(columnIndex);

		    // Update the QC if the cell is selected
		    if (dataTableJTable.isCellSelected(rowIndex, realColumnIndex)) {
			// To avoid problem with Latitude and Longitude
			final String variableName = dataTableJTable.getColumnName(realColumnIndex);

			// the variable name may contain additional "()" data, which is not in the datatable index.
			String parameterName = null;
			if (variableName.contains("(")) {
			    parameterName = variableName.split("\\(")[0].trim();
			} else {
			    parameterName = variableName;
			}

			final Parameter<? extends Number> parameter = dataTableJTable.getDataTableModel()
				.getParameter(parameterName);
			if (parameter != null) {
			    toReturn = true;
			}
		    }
		}
	    }
	    return toReturn;
	}
    }

    private static Set<POPUP_ITEM_ENUM> popupItemNotVisible = new HashSet<>();
    /**
     *
     */
    private static final long serialVersionUID = -5980873435818991665L;

    private final DataTableJTable dataTableJTable;

    private final DecimalFormat formatterForDbl = new DecimalFormat(DataTableJTable.getFormatForDouble());

    private final DecimalFormat formatterForRefDbl = new DecimalFormat(DataTableJTable.getFormatForRefParamDouble());

    /**
     * Set visible (or not) a specific Popup item. By default, an item is visible.
     *
     * @param popupItem
     * @param isVisible
     */
    public static void setVisiblePopupItem(final POPUP_ITEM_ENUM popupItem, final boolean isVisible) {
	if (isVisible) {
	    popupItemNotVisible.remove(popupItem);
	} else {
	    popupItemNotVisible.add(popupItem);
	}
    }

    /**
     * Default constructor
     *
     * @param dataTableJTable
     * @param scoop3Frame
     * @param qcValuesSettable
     */
    public DataTablePopupMenu(final DataTableJTable dataTableJTable, final Scoop3Frame scoop3Frame,
	    final QCValues[] qcValuesSettable) {
	super();
	this.dataTableJTable = dataTableJTable;

	initJMenuItems(scoop3Frame, qcValuesSettable);
    }

    /**
     * Update the QC and the value for the given variable
     *
     * @param variableName
     * @param rowIndex
     * @param qcValue
     * @param value
     * @param updatesForVariables
     */
    public void changeQCAndValueForVariable(final String variableName, final int rowIndex, final QCValues qcValue,
	    final Number value, final HashMap<String, ArrayList<DataTableCellQCAndValueUpdate>> updatesForVariables) {

	if (value != null) {
	    final DataTableCellQCAndValueUpdate dataTableUpdate = dataTableJTable.getDataTableModel()
		    .updateQCandValue(variableName, rowIndex, qcValue, value);
	    if (dataTableUpdate != null) {
		// Memorize update
		ArrayList<DataTableCellQCAndValueUpdate> updatesForVariable;
		if (updatesForVariables.containsKey(variableName)) {
		    updatesForVariable = updatesForVariables.get(variableName);
		} else {
		    updatesForVariable = new ArrayList<DataTableCellQCAndValueUpdate>();
		}
		updatesForVariable.add(dataTableUpdate);
		updatesForVariables.put(variableName, updatesForVariable);
	    }
	} else {
	    SC3Logger.LOGGER.error("changeQCAndValueForVariable " + variableName + " : value is NULL");
	}
    }

    /**
     * Update the QC for the given variable
     *
     * @param variableName
     * @param rowIndex
     * @param qcValue
     * @param updatesForVariables
     */
    public void changeQCForVariable(final String variableName, final int rowIndex, final QCValues qcValue,
	    final HashMap<String, ArrayList<DataTableCellQCUpdate>> updatesForVariables) {
	final DataTableCellQCUpdate dataTableUpdate = dataTableJTable.getDataTableModel().updateQC(variableName,
		rowIndex, qcValue);

	// Memorize update
	ArrayList<DataTableCellQCUpdate> updatesForVariable;
	if (updatesForVariables.containsKey(variableName)) {
	    updatesForVariable = updatesForVariables.get(variableName);
	} else {
	    updatesForVariable = new ArrayList<DataTableCellQCUpdate>();
	}
	updatesForVariable.add(dataTableUpdate);
	updatesForVariables.put(variableName, updatesForVariable);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
     */
    @Override
    public void show(final Component invoker, final int x, final int y) {
	for (final MenuElement menuElt : getSubElements()) {
	    if (menuElt instanceof Scoop3DataTableJMenuItem) {
		((Scoop3DataTableJMenuItem) menuElt).updateEnabled();
	    }
	}
	super.show(invoker, x, y);
    }

    /**
     * @param scoop3Frame
     * @param qcValuesSettable
     *
     */
    private void initJMenuItems(final Scoop3Frame scoop3Frame, final QCValues[] qcValuesSettable) {
	boolean addSeparator = false;

	JMenuItem item = new Scoop3DataTableJMenuItem("bpc-gui.tooltip-delete_values") {
	    private static final long serialVersionUID = 1446113703460329261L;

	    /*
	     * (non-Javadoc)
	     *
	     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	     */
	    @Override
	    public void actionPerformed(final ActionEvent e) {
		final int minIndex = dataTableJTable.getSelectionModel().getMinSelectionIndex();
		final int maxIndex = dataTableJTable.getSelectionModel().getMaxSelectionIndex();

		int nbRowsSelected = 0;
		for (int rowIndex = maxIndex; rowIndex >= minIndex; rowIndex--) {
		    if (dataTableJTable.getSelectionModel().isSelectedIndex(rowIndex)) {
			nbRowsSelected++;
		    }
		}
		final int nbTotalRows = dataTableJTable.getDataTableModel().getRowCount();

		if (nbRowsSelected == nbTotalRows) {
		    JOptionPane.showMessageDialog(scoop3Frame,
			    Messages.getMessage("gui.datatable-popup.delete-all-rows-forbidden-message"),
			    Messages.getMessage("gui.datatable-popup.delete-all-rows-forbidden-title"),
			    JOptionPane.WARNING_MESSAGE,
			    new ImageIcon(getClass().getClassLoader().getResource("icons/messagebox_warning.png")));
		} else {
		    final int answer = JOptionPane.showConfirmDialog(scoop3Frame,
			    MessageFormat.format(
				    Messages.getMessage("gui.datatable-popup.delete-rows-confirmation-message"),
				    nbRowsSelected),
			    Messages.getMessage("gui.datatable-popup.delete-rows-confirmation-title"),
			    JOptionPane.YES_NO_OPTION);

		    if (answer == JOptionPane.YES_OPTION) {
			final List<DataTableCellDeleted> cellsDeleted = new ArrayList<>();
			for (int rowIndex = maxIndex; rowIndex >= minIndex; rowIndex--) {
			    if (dataTableJTable.getSelectionModel().isSelectedIndex(rowIndex)) {
				final List<DataTableCellDeleted> rowDeleted = dataTableJTable.getDataTableModel()
					.removeDataForRowIndex(rowIndex);
				cellsDeleted.addAll(rowDeleted);
			    }
			}

			// Sort the cells for the "undo" order
			Collections.sort(cellsDeleted,
				(final DataTableCellDeleted o1, final DataTableCellDeleted o2) -> {
				    if (o1.getRowIndex() == o2.getRowIndex()) {
					final int columnIndexO1 = dataTableJTable.getDataTableModel()
						.getColumnIndexForVariable(o1.getVariableName());
					final int columnIndexO2 = dataTableJTable.getDataTableModel()
						.getColumnIndexForVariable(o2.getVariableName());
					return (columnIndexO1 - columnIndexO2);
				    }
				    return (o1.getRowIndex() - o2.getRowIndex());
				});
			final DataTableRowsDeleted dataTableRowsDeleted = new DataTableRowsDeleted(cellsDeleted);
			dataTableJTable.addUpdatesForVariables(dataTableRowsDeleted);
		    }
		}
	    }
	};
	if (!popupItemNotVisible.contains(POPUP_ITEM_ENUM.DELETE_LINE)) {
	    if (addSeparator) {
		addSeparator = false;
		addSeparator();
	    }
	    add(item);
	    addSeparator = true;
	}

	if (!popupItemNotVisible.contains(POPUP_ITEM_ENUM.UPDATE_QC)) {
	    if (addSeparator) {
		addSeparator = false;
		addSeparator();
	    }
	    for (final QCValues qcValues : qcValuesSettable) {
		if (!qcValues.equals(QCValues.QC_FILL_VALUE)) {
		    item = new Scoop3DataTableChangeQCJMenuItem(qcValues, scoop3Frame);

		    add(item);
		}
	    }
	    addSeparator = true;
	}

	if (addSeparator) {
	    addSeparator = false;
	    addSeparator();
	}
	/*
	 * Copy all lines in clipboard
	 */
	final URL resource = getClass().getClassLoader().getResource("icons/clipboard_all.png");
	final ImageIcon clipboardIcon = new ImageIcon(resource);

	item = new JMenuItem(Messages.getMessage("coriolis-gui.obs-metadata.popupmenu.copy-all-in-clipboard"),
		clipboardIcon);
	add(item);
	item.setHorizontalTextPosition(JMenuItem.RIGHT);
	item.addActionListener((final ActionEvent e) -> {
	    final StringBuilder strBuilder = new StringBuilder();
	    final int nbColumns = dataTableJTable.getColumnCount();
	    final int nbRows = dataTableJTable.getRowCount();

	    for (int currentColumn = 0; currentColumn < nbColumns; currentColumn++) {
		final Object value = dataTableJTable.getDataTableModel().getColumnName(currentColumn);

		if (value == null) {
		    strBuilder.append("");
		} else {
		    strBuilder.append(value);
		}
		if (currentColumn < (nbRows - 1)) {
		    strBuilder.append("\t");
		}
	    }
	    strBuilder.append("\n");

	    for (int currentRow = 0; currentRow < nbRows; currentRow++) {
		for (int currentColumn = 0; currentColumn < nbColumns; currentColumn++) {
		    final Object value = dataTableJTable.getValueAt(currentRow, currentColumn);

		    if (value == null) {
			strBuilder.append("");
		    } else {
			if (value.getClass().equals(Double.class)) {
			    if (currentColumn == 0) {
				// Ref Param
				strBuilder.append(formatterForRefDbl.format(value));
			    } else {
				strBuilder.append(formatterForDbl.format(value));
			    }
			} else if ((currentColumn == 0) && value.getClass().equals(Long.class)) {
			    // Ref Param
			    strBuilder.append(Conversions.formatDateAndHourMinSec(new Date((Long) value)));
			} else {
			    strBuilder.append(value);
			}
		    }
		    if (currentColumn < (nbRows - 1)) {
			strBuilder.append("\t");
		    }
		}
		strBuilder.append("\n");
	    }
	    final StringSelection contents = new StringSelection(strBuilder.toString());
	    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(contents, null);
	});
    }
}
