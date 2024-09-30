package fr.ifremer.scoop3.gui.common.jdialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import fr.ifremer.scoop3.gui.utils.SpringUtilities;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.model.QCValues;

public class UpdateMultipleLineForReportDialog extends JDialog {

    private static final long serialVersionUID = -6268509778152341372L;

    private boolean applyUpdate;

    private final JCheckBox checkBox;
    private final JTextArea commentTextArea;
    private final ArrayList<JRadioButton> radioButtons;

    public UpdateMultipleLineForReportDialog(final ReportJDialog reportJDialog, final QCValues[] qcValuesSettable) {
	// Modal
	super(reportJDialog, true);

	// Main panel
	getContentPane().setLayout(new BorderLayout(10, 10));

	// Add frame's title
	this.setTitle(Messages.getMessage("gui.errors-dialog.popup-menu.update-rows.title"));

	// Add a title in the North of the main panel
	/*
	 * getContentPane().add(new JLabel(Messages.getMessage("gui.errors-dialog.popup-menu.update-rows.title")),
	 * BorderLayout.NORTH);
	 */

	// Add CENTER of the main panel
	final JPanel centerPanel = new JPanel();
	// http://docs.oracle.com/javase/tutorial/uiswing/layout/spring.html
	centerPanel.setLayout(new SpringLayout());

	centerPanel.add(new JLabel(Messages.getMessage("gui.errors-dialog.popup-menu.update-rows.checkbox")));
	checkBox = new JCheckBox();
	centerPanel.add(checkBox);
	centerPanel.add(new JLabel(Messages.getMessage("gui.errors-dialog.popup-menu.update-rows.qc")));
	final ButtonGroup bg = new ButtonGroup();
	final JPanel radioButtonsPanel = new JPanel();
	radioButtonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
	radioButtons = new ArrayList<JRadioButton>();

	for (final QCValues qcValues : qcValuesSettable) {
	    JRadioButton qcRadio = null;
	    // if (qcValues.equals(QCValues.QC_A) || qcValues.equals(QCValues.QC_Q)) {
	    // qcRadio = new JRadioButton(String.valueOf(
	    // qcValues.toString().substring(qcValues.toString().length() - 1, qcValues.toString().length())));
	    // } else {
	    // qcRadio = new JRadioButton(String.valueOf(qcValues.getQCValue()));
	    // }
	    qcRadio = new JRadioButton(qcValues.getStringQCValue());
	    qcRadio.setBackground(qcValues.getColor());
	    radioButtons.add(qcRadio);
	    bg.add(qcRadio);
	    radioButtonsPanel.add(qcRadio);
	}

	// final JRadioButton qc1 = new JRadioButton("1");
	// qc1.setBackground(QCValues.QC_1.getColor());
	// radioButtons.add(qc1);
	// bg.add(qc1);
	// radioButtonsPanel.add(qc1);
	//
	// final JRadioButton qc3 = new JRadioButton("3");
	// qc3.setBackground(QCValues.QC_3.getColor());
	// radioButtons.add(qc3);
	// bg.add(qc3);
	// radioButtonsPanel.add(qc3);
	//
	// final JRadioButton qc4 = new JRadioButton("4");
	// qc4.setBackground(QCValues.QC_4.getColor());
	// radioButtons.add(qc4);
	// bg.add(qc4);
	// radioButtonsPanel.add(qc4);
	//
	// final JRadioButton qc6 = new JRadioButton("6");
	// qc6.setBackground(QCValues.QC_6.getColor());
	// radioButtons.add(qc6);
	// bg.add(qc6);
	// radioButtonsPanel.add(qc6);

	centerPanel.add(radioButtonsPanel);
	centerPanel.add(new JLabel(Messages.getMessage("gui.errors-dialog.popup-menu.update-rows.comment")));
	commentTextArea = new JTextArea(3, 30);
	commentTextArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
	centerPanel.add(commentTextArea);
	getContentPane().add(centerPanel, BorderLayout.CENTER);

	// Lay out the panel.
	SpringUtilities.makeCompactGrid(centerPanel, // parent
		3, 2, // rows, cols,
		3, 3, // initX, initY
		3, 3); // xPad, yPad

	// Add buttons in the SOUTH of the main panel
	final JPanel buttonsPanel = new JPanel();

	final JPanel validateButtonPanel = new JPanel();
	final JButton validateButton = new JButton(Messages.getMessage("bpc-gui.button-validate"));
	validateButtonPanel.add(validateButton);
	validateButton.addActionListener((final ActionEvent e) -> {
	    applyUpdate = true;
	    setVisible(false);
	});

	final JPanel cancelButtonPanel = new JPanel();
	final JButton cancelButton = new JButton(Messages.getMessage("bpc-gui.button-cancel"));
	cancelButtonPanel.add(cancelButton);
	cancelButton.addActionListener((final ActionEvent e) -> {
	    applyUpdate = false;
	    setVisible(false);
	});

	buttonsPanel.add(validateButtonPanel);
	buttonsPanel.add(cancelButtonPanel);
	getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

	//
	pack();
	setLocationRelativeTo(null);
	setResizable(false);
    }

    /**
     * @return the checkBox
     */
    public JCheckBox getCheckBox() {
	return checkBox;
    }

    /**
     * @return the commentTextArea
     */
    public JTextArea getCommentTextArea() {
	return commentTextArea;
    }

    /**
     * @return the radioButtons
     */
    public ArrayList<JRadioButton> getRadioButtons() {
	return radioButtons;
    }

    public boolean updateLines() {
	applyUpdate = false;

	setVisible(true);

	return applyUpdate;
    }

}
