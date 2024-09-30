package fr.ifremer.scoop3.gui.core;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

public class Scoop3ErrorDialog {

    private static final int DIALOG_DEFAULT_WIDTH = 500;
    private static final int DIALOG_WITH_DETAILS_HEIGHT = 500;

    public static void showDialog(final String title, final String details) {
	final JTextPane textPane = new JTextPane();

	textPane.setText(details);
	textPane.setEditable(false);

	final JScrollPane scrollPane = new JScrollPane(textPane);
	scrollPane.setAlignmentX(0);

	final JPanel content = new JPanel();
	content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));

	final JDialog dialog = new JOptionPane(content, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION)
		.createDialog(null, "Error!");

	final JLabel message = new JLabel(title);
	message.setBorder(new EmptyBorder(10, 10, 10, 10));
	message.setAlignmentX(0);
	final Dimension labelSize = message.getPreferredSize();
	labelSize.setSize(300, labelSize.height);
	message.setPreferredSize(labelSize);
	content.add(message);
	content.add(scrollPane);

	dialog.setPreferredSize(new Dimension(DIALOG_DEFAULT_WIDTH, DIALOG_WITH_DETAILS_HEIGHT));
	dialog.setResizable(false);
	dialog.pack();
	dialog.setLocationRelativeTo(null);
	dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	dialog.setVisible(true);
    }
}
