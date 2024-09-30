package fr.ifremer.scoop3.gui.data.popup;

import javax.swing.JMenu;

import fr.ifremer.scoop3.infra.i18n.Messages;

/**
 * Definition of the JMenuItems used for this JPopupMenu
 */
public abstract class Scoop3ChartPanelJMenu extends JMenu {

    /**
    	 * 
    	 */
    private static final long serialVersionUID = -2751693527869901537L;

    protected Scoop3ChartPanelJMenu(final String title) {
	super(Messages.getMessage(title));
    }

    /**
     * Update the Enable state
     */
    public abstract void updateEnabled();
}
