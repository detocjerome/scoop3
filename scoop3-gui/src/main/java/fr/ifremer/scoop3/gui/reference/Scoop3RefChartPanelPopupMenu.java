package fr.ifremer.scoop3.gui.reference;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.gui.common.DataOrReferenceViewController;
import fr.ifremer.scoop3.gui.data.Scoop3ChartPanelPopupMenu;
import fr.ifremer.scoop3.infra.i18n.Messages;

public class Scoop3RefChartPanelPopupMenu extends Scoop3ChartPanelPopupMenu {

    /**
     *
     */
    private static final long serialVersionUID = -8794453378018440340L;
    private JScoop3ChartScrollPaneAbstract scoop3ChartPanel;

    public Scoop3RefChartPanelPopupMenu(final DataOrReferenceViewController dataOrReferenceViewController) {
	super(null, dataOrReferenceViewController);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.data.Scoop3ChartPanelPopupMenu#changeQCMenuIsEnabled()
     */
    @Override
    protected boolean changeQCMenuIsEnabled() {
	return scoop3ChartPanel.isSelectionBoxActive() && (JScoop3ChartScrollPaneAbstract.getCoefX() == 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.data.Scoop3ChartPanelPopupMenu#getSource()
     */
    @Override
    protected Object getSource() {
	return scoop3ChartPanel;
    }

    /**
     * Set the new Source
     *
     * @param scoop3ChartPanel
     */
    public void setSource(final JScoop3ChartScrollPaneAbstract scoop3ChartPanel) {
	this.scoop3ChartPanel = scoop3ChartPanel;
    }

    @Override
    public void addSaveImagePart() {
	final URL saveIconURL = getClass().getClassLoader().getResource("icons/save_16x16.png");
	final ImageIcon saveIcon = new ImageIcon(saveIconURL);
	final JMenuItem item = new JMenuItem(Messages.getMessage("gui.dataview-popup.save-image"), saveIcon);
	item.addActionListener(
		(final ActionEvent e) -> dataOrReferenceViewController.saveImage(scoop3ChartPanel));
	add(item);
    }

}
