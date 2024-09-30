package fr.ifremer.scoop3.gui.common.jdialog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import fr.ifremer.scoop3.gui.core.Scoop3Frame;
import fr.ifremer.scoop3.gui.utils.SpringUtilities;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.tools.Conversions;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.Profile;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.Parameter;

public class DisplayObservationInfoForLevelDialog extends JDialog {

    private static final long serialVersionUID = 4055742863902302290L;

    private static DisplayObservationInfoForLevelDialog instance = null;

    private final JLabel titleLabel = new JLabel();
    private final JPanel centerPanel = new JPanel();
    private final NumberFormat formatter = new DecimalFormat("#0.0000");

    public static void disposeIfExists() {
	if (instance != null) {
	    instance.dispose();
	    instance = null;
	}
    }

    public static void updateInfo(final Scoop3Frame scoop3Frame, final Observation observation, final int levelIndex,
	    final Map<String, List<String>> parametersOrder) {
	boolean setLocation = false;
	if (instance == null) {
	    setLocation = true;
	    instance = new DisplayObservationInfoForLevelDialog(scoop3Frame);
	}
	instance.updateInfo(observation, levelIndex, parametersOrder);
	if (setLocation) {
	    instance.setLocationRelativeTo(scoop3Frame);
	    instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	instance.setVisible(true);
    }

    public static void updateInfoIfVisible(final Observation observation, final int levelIndex,
	    final Map<String, List<String>> parametersOrder) {
	if (instance != null) {
	    instance.updateInfo(observation, levelIndex, parametersOrder);
	    instance.setVisible(true);
	}
    }

    private DisplayObservationInfoForLevelDialog(final Scoop3Frame scoop3Frame) {
	super(scoop3Frame);

	this.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(final WindowEvent e) {
		instance.setVisible(false);
		instance.dispose();
		instance = null;
		scoop3Frame.requestFocusInWindow();
	    }
	});

	getContentPane().setLayout(new BorderLayout());

	/*
	 * Add title
	 */
	getContentPane().add(titleLabel, BorderLayout.NORTH);

	/*
	 * Center panel ...
	 */
	getContentPane().add(centerPanel, BorderLayout.CENTER);
	// http://docs.oracle.com/javase/tutorial/uiswing/layout/spring.html
	centerPanel.setLayout(new SpringLayout());

	/*
	 * Add close button
	 */
	final JButton closeButton = new JButton(Messages.getMessage("bpc-gui.button-close"));
	closeButton.addActionListener((final ActionEvent e) -> {
	    instance.setVisible(false);
	    instance.dispose();
	    instance = null;
	    scoop3Frame.requestFocusInWindow();
	});

	final JPanel buttonPanel = new JPanel();
	buttonPanel.add(closeButton);
	getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * @param b
     * @param parameter
     * @param levelIndex
     */
    private void addParameterInCenterPanel(final boolean valueIsADate, final Parameter<? extends Number> parameter,
	    final int levelIndex) {
	/*
	 * Add parameter code
	 */
	centerPanel.add(new JLabel(parameter.getCode()));

	/*
	 * Add QC_VALUE
	 */
	JLabel qcLabel;
	if ((parameter.getQcValues().size() <= levelIndex) || (parameter.getQcValues().get(levelIndex) == null)) {
	    // No QC defined ...
	    qcLabel = new JLabel();
	} else {
	    final QCValues qc = parameter.getQcValues().get(levelIndex);
	    // if (qc.equals(QCValues.QC_A) || qc.equals(QCValues.QC_Q)) {
	    // qcLabel = new JLabel(
	    // String.valueOf(qc.toString().substring(qc.toString().length() - 1, qc.toString().length())),
	    // SwingConstants.CENTER);
	    // } else {
	    // qcLabel = new JLabel(String.valueOf(qc.getQCValue()), SwingConstants.CENTER);
	    // }
	    qcLabel = new JLabel(qc.getStringQCValue(), SwingConstants.CENTER);
	    qcLabel.setOpaque(true);
	    qcLabel.setBackground(qc.getColor());
	    qcLabel.setForeground(qc.getForegroundColor());
	}
	centerPanel.add(qcLabel);

	/*
	 * Add value
	 */
	if (parameter.getValues().size() > levelIndex) {
	    final Number value = parameter.getValues().get(levelIndex);
	    if (valueIsADate && (value instanceof Long)) {
		centerPanel.add(new JLabel(Conversions.formatDateAndHourMinSec((Long) value), SwingConstants.RIGHT));
	    } else {
		centerPanel.add(new JLabel(formatter.format(value), SwingConstants.RIGHT));
	    }
	} else {
	    centerPanel.add(new JLabel("---"));
	}
    }

    private void updateInfo(final Observation observation, final int levelIndex,
	    final Map<String, List<String>> parametersOrder) {

	titleLabel.setText(MessageFormat.format(Messages.getMessage("gui.display-obs-info.title"), observation.getId(),
		(levelIndex + 1)));

	centerPanel.removeAll();
	int rows = 0;

	rows++;
	centerPanel.add(new JLabel());
	centerPanel.add(new JLabel(Messages.getMessage("gui.display-obs-info.qc")));
	centerPanel.add(new JLabel(Messages.getMessage("gui.display-obs-info.value"), SwingConstants.CENTER));

	rows++;
	addParameterInCenterPanel(!(observation instanceof Profile), observation.getReferenceParameter(), levelIndex);

	if ((parametersOrder == null) || !parametersOrder.containsKey(observation.getReference())) {
	    for (final OceanicParameter parameter : observation.getOceanicParameters().values()) {
		rows++;
		addParameterInCenterPanel(false, parameter, levelIndex);
	    }
	} else {
	    // The order of the parameters are defined
	    for (final String parameterName : parametersOrder.get(observation.getReference())) {
		final OceanicParameter parameter = observation.getOceanicParameters().get(parameterName);
		if (parameter != null) {
		    rows++;
		    addParameterInCenterPanel(false, parameter, levelIndex);
		}
	    }
	}

	// Lay out the panel.
	SpringUtilities.makeCompactGrid(centerPanel, // parent
		rows, 3, // rows, cols,
		3, 3, // initX, initY
		5, 3); // xPad, yPad

	validate();
	pack();
    }
}
