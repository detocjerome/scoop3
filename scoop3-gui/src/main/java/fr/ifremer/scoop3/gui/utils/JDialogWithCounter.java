package fr.ifremer.scoop3.gui.utils;

import java.awt.BorderLayout;
import java.text.MessageFormat;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import fr.ifremer.scoop3.events.GuiEventDisplayDialog;
import fr.ifremer.scoop3.infra.i18n.Messages;

public class JDialogWithCounter extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6364293802526818407L;

	private JLabel delay = new JLabel("0");
	private long startTime = System.currentTimeMillis();

	public JDialogWithCounter(JFrame jframe, GuiEventDisplayDialog event) {
		super(jframe, event.getDialogTitle(), true);

		Timer timer = new Timer();
		timer.schedule(new DisplayDelay(), 0, 1000);

		JLabel label = new JLabel(event.getDialogMessage());
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);

		setLayout(new BorderLayout());
		add(label, BorderLayout.NORTH);
		add(delay, BorderLayout.CENTER);
		add(progressBar, BorderLayout.SOUTH);

		pack();
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	/**
	 * @return the elapsed time message
	 */
	public String getElapsedTimeMessage() {
		int nbSeconds = (int) ((System.currentTimeMillis() - startTime) / ((long) 1000));
		return MessageFormat.format(
				Messages.getMessage("bpc-controller.elapsed-time"), nbSeconds);
	}

	private class DisplayDelay extends TimerTask {

		@Override
		public void run() {
			delay.setText(getElapsedTimeMessage());
		}
	}
}
