package fr.ifremer.scoop3.gui.home.dialog;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class WaitingDialog extends JDialog {

    private static final long serialVersionUID = 489928598240303156L;

    public WaitingDialog(final JDialog ownerDialog, final String message) {
	super(ownerDialog, true);

	fillDialog(message);
	setLocationRelativeTo(ownerDialog);

    }

    public WaitingDialog(final JFrame ownerFrame, final String message) {
	super(ownerFrame, true);

	fillDialog(message);
	setLocationRelativeTo(ownerFrame);

    }

    /**
     * @param ownerDialog
     * @param message
     */
    private void fillDialog(final String message) {
	getContentPane().setLayout(new BorderLayout(10, 10));

	getContentPane().add(new JLabel(message), BorderLayout.CENTER);

	final JProgressBar progressBar = new JProgressBar();
	progressBar.setIndeterminate(true);
	getContentPane().add(progressBar, BorderLayout.SOUTH);

	pack();
	validate();
	repaint();

	setResizable(false);
	setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }
}
