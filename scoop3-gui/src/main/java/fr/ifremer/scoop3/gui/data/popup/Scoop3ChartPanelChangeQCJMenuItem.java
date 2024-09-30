package fr.ifremer.scoop3.gui.data.popup;

import java.awt.event.ActionEvent;

import fr.ifremer.scoop3.gui.common.DataOrReferenceViewController;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent.EVENT_ENUM;
import fr.ifremer.scoop3.gui.data.ChartPanelWithComboBox;
import fr.ifremer.scoop3.model.QCValues;

public class Scoop3ChartPanelChangeQCJMenuItem extends Scoop3ChartPanelJMenuItem {

    private static final long serialVersionUID = 8808528691616021872L;
    private final ChartPanelWithComboBox chartPanelWithComboBox;
    private final boolean currentStationOnly;
    private final QCValues qcToSet;

    public Scoop3ChartPanelChangeQCJMenuItem(final String title, final boolean currentStationOnly,
	    final QCValues qcToSet, final ChartPanelWithComboBox chartPanelWithComboBox,
	    final DataOrReferenceViewController dataOrReferenceViewController) {
	super(title + qcToSet.getQCValue(), "icons/" + (dataOrReferenceViewController.isBPCVersion() ? "bpc_" : "")
		+ "select_and_change_qc_" + qcToSet.getQCValue() + "_inactive.png", dataOrReferenceViewController);
	this.currentStationOnly = currentStationOnly;
	this.qcToSet = qcToSet;
	this.chartPanelWithComboBox = chartPanelWithComboBox;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
	// block the QC change on PSAL/TEMP graph
	if (!(this.chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane().getAbscissaPhysicalVar()
		.getLabel().equals("PSAL")
		&& this.chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.getOrdinatePhysicalVar().getLabel().equals("TEMP"))) {
	    dataOrReferenceViewController.getPropertyChangeSupport()
		    .firePropertyChange(new SC3PropertyChangeEvent(this, EVENT_ENUM.CHANGE_QC));
	} else {
	    dataOrReferenceViewController.removeSelectionBox();
	}
    }

    /**
     * @return the chartPanelWithComboBox
     */
    public ChartPanelWithComboBox getChartPanelWithComboBox() {
	return chartPanelWithComboBox;
    }

    /**
     * @return the qcToSet
     */
    public QCValues getQcToSet() {
	return qcToSet;
    }

    /**
     * @return the currentStationOnly
     */
    public boolean isCurrentStationOnly() {
	return currentStationOnly;
    }

    @Override
    public void updateEnabled() {
	// This submenu is always enabled (managed by the Menu)
    }
}
