package fr.ifremer.scoop3.gui.data.popup;

import java.awt.event.ActionEvent;

import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.gui.common.DataOrReferenceViewController;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent.EVENT_ENUM;

public class Scoop3ChartPanelShiftJMenuItem extends Scoop3ChartPanelJMenuItem {

    /**
		 * 
		 */
    private static final long serialVersionUID = 2953619061565003272L;

    private final int shiftIndex;

    public Scoop3ChartPanelShiftJMenuItem(final String title, final String imagePath, final int shiftIndex,
	    final DataOrReferenceViewController dataOrReferenceViewController) {
	super(title, imagePath, dataOrReferenceViewController);
	this.shiftIndex = shiftIndex;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
	JScoop3ChartScrollPaneAbstract.setCoefX(shiftIndex - 1);
	dataOrReferenceViewController.getPropertyChangeSupport().firePropertyChange(
		new SC3PropertyChangeEvent(this, EVENT_ENUM.SHIFT));
    }

    @Override
    public void updateEnabled() {
	// This submenu is always enabled
    }
}