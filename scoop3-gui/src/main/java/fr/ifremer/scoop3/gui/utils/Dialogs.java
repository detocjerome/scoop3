package fr.ifremer.scoop3.gui.utils;

import java.awt.Component;

import javax.swing.JOptionPane;

import fr.ifremer.scoop3.gui.core.Scoop3ErrorDialog;

public abstract class Dialogs {

	/**
	 * Show a message in a dialog box.
	 * 
	 * @param title
	 * @param message
	 */
	public static void showInfoMessage(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Show an error messsage with details
	 * 
	 * @param title
	 * @param message
	 */
	public static void showErrorMessage(String title, String message) {
		Scoop3ErrorDialog.showDialog(title, message);
	}

	/**
	 * Show a confirm dialog with YES or NO choices.
	 * 
	 * @param parent
	 * @param title
	 * @param message
	 * @return
	 */
	public static int showConfirmDialog(Component parent, String title,
			String message) {
		return JOptionPane.showConfirmDialog(parent, message, title,
				JOptionPane.YES_NO_OPTION);
	}
}
