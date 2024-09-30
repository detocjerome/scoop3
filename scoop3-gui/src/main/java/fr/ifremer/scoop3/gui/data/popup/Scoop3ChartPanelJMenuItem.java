package fr.ifremer.scoop3.gui.data.popup;

import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import fr.ifremer.scoop3.gui.common.DataOrReferenceViewController;
import fr.ifremer.scoop3.infra.i18n.Messages;

/**
 * Definition of the JMenuItems used for this JPopupMenu
 */
public abstract class Scoop3ChartPanelJMenuItem extends JMenuItem implements ActionListener {

    private static final long serialVersionUID = -2751693527869901537L;

    protected transient DataOrReferenceViewController dataOrReferenceViewController;

    /**
     * @param imagePath
     * @return
     */
    public static ImageIcon getImageIconForPopupMenu(final String imagePath) {
	final URL resource = Scoop3ChartPanelJMenuItem.class.getClassLoader().getResource(imagePath);
	return new ImageIcon(new ImageIcon(resource).getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
    }

    protected Scoop3ChartPanelJMenuItem(final String title,
	    final DataOrReferenceViewController dataOrReferenceViewController) {
	super(Messages.getMessage(title));

	this.dataOrReferenceViewController = dataOrReferenceViewController;

	addActionListener(this);
    }

    protected Scoop3ChartPanelJMenuItem(final String title, final String imagePath,
	    final DataOrReferenceViewController dataOrReferenceViewController) {
	super(Messages.getMessage(title), getImageIconForPopupMenu(imagePath));

	this.dataOrReferenceViewController = dataOrReferenceViewController;

	addActionListener(this);
    }

    /**
     * Update the Enable state
     */
    public abstract void updateEnabled();
}
