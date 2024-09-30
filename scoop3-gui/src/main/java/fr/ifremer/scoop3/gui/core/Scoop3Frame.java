package fr.ifremer.scoop3.gui.core;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.bushe.swing.event.EventBus;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;

import fr.ifremer.scoop3.events.GuiEventMiscEvent;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.io.WriterManager;

/**
 * @author Altran Main Scoop3 Frame
 *
 */
public class Scoop3Frame extends JRibbonFrame {

    protected transient ActionListener changeLanguageActionListener = null;
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public Scoop3Frame() {
	SC3Logger.LOGGER.info("application.version : " + FileConfig.getScoop3FileConfig().getApplicationVersion());

	setTitle(FileConfig.getScoop3FileConfig().getString("application.title") + "("
		+ FileConfig.getScoop3FileConfig().getApplicationVersion() + ")");
	// Get the size from configuration file
	// (Specifications say 1600 * 900)
	int initWidth;
	try {
	    initWidth = Integer.parseInt(FileConfig.getScoop3FileConfig().getString("application.init.width"));
	} catch (final NumberFormatException nfe) {
	    initWidth = 1600;
	}

	int initHeigth;
	try {
	    initHeigth = Integer.parseInt(FileConfig.getScoop3FileConfig().getString("application.init.heigth"));
	} catch (final NumberFormatException nfe) {
	    initHeigth = 900;
	}
	setSize(new Dimension(initWidth, initHeigth));

	// Set the default closing operation
	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	// Set the screen on the middle
	setLocationRelativeTo(null);
	// Set the frame resizable
	setResizable(true);

	addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(final WindowEvent we) {
		checkIfExitIsWished();
	    }
	});

	createRibbon();
    }

    /**
     * @return Scoop3 Frame
     */
    public JFrame getFrame() {
	return this;
    }

    public void checkIfExitIsWished() {
	final int answer = JOptionPane.showConfirmDialog(getFrame(), Messages.getMessage("gui.scoop3-exit-message"),
		Messages.getMessage("gui.scoop3-exit-title"), JOptionPane.YES_NO_OPTION);

	if (answer == JOptionPane.YES_OPTION) {
	    scoop3IsExiting();

	    if (WriterManager.isWrittenInProgress()) {
		WriterManager.closeScoop3AfterWriting();
		getFrame().setVisible(false);
		while (WriterManager.isWrittenInProgress()) {
		    JOptionPane.showMessageDialog(getFrame(),
			    Messages.getMessage("gui.scoop3-exit-after-writing-message"),
			    Messages.getMessage("gui.scoop3-exit-after-writing-title"), JOptionPane.WARNING_MESSAGE);
		}
	    } else {
		SC3Logger.LOGGER.info(Messages.getMessage("gui.scoop3-exit"));
		System.exit(0);
	    }
	}
    }

    protected void createRibbon() {

	// // Help button
	// // TODO : Definir la partie aide en ligne
	// final URL resource3 = getClass().getClassLoader().getResource("icons/help_contents.png");
	// getRibbon().configureHelp(ImageWrapperResizableIcon.getIcon(resource3, new Dimension(32, 32)),
	// new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent e) {
	// // TODO - Gérer l'aide
	// JOptionPane.getDefaultLocale();
	// JOptionPane.showInputDialog(Messages.getMessage("gui.ribbon-looking_in_help"), " ");
	// }
	// });

	// application menu
	final URL resource = getClass().getClassLoader().getResource("icons/scoopLogo.png");
	setApplicationIcon(ImageWrapperResizableIcon.getIcon(resource, new Dimension(32, 32)));

	final RibbonApplicationMenu applicationMenu = getApplicationMenu();
	getRibbon().setApplicationMenu(applicationMenu);
    }

    /**
     * Create the application Menu (upper left circle button) Contains : Exit button, ...
     */
    public RibbonApplicationMenu getApplicationMenu() {
	final RibbonApplicationMenu applicationMenu = new RibbonApplicationMenu();

	URL resource = getClass().getClassLoader().getResource("icons/translation.png");
	final RibbonApplicationMenuEntryPrimary amEntryChangeLanguage = new RibbonApplicationMenuEntryPrimary(
		ImageWrapperResizableIcon.getIcon(resource, new Dimension(32, 32)),
		Messages.getMessage("gui.ribbon-change-language"), changeLanguageActionListener,
		CommandButtonKind.ACTION_ONLY);
	applicationMenu.addMenuEntry(amEntryChangeLanguage);
	applicationMenu.addMenuSeparator();

	resource = getClass().getClassLoader().getResource("icons/system_log_out.png");
	final RibbonApplicationMenuEntryPrimary amEntryExit = new RibbonApplicationMenuEntryPrimary(
		ImageWrapperResizableIcon.getIcon(resource, new Dimension(32, 32)),
		Messages.getMessage("gui.ribbon-quit"), (final ActionEvent e) -> checkIfExitIsWished(),
		CommandButtonKind.ACTION_ONLY);
	amEntryExit.setActionKeyTip("X");
	applicationMenu.addMenuEntry(amEntryExit);

	return applicationMenu;
    }

    protected void scoop3IsExiting() {
	EventBus.publish(new GuiEventMiscEvent("scoop3Exiting"));
    }

    public void setChangeLanguageActionListener(final ActionListener changeLanguageActionListener) {
	this.changeLanguageActionListener = changeLanguageActionListener;
    }
}
