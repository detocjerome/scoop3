package fr.ifremer.scoop3.gui.data.datatable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import fr.ifremer.scoop3.gui.common.CommonViewController;
import fr.ifremer.scoop3.gui.core.Scoop3Frame;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableUpdateAbstract;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;

public class DataTableDialog extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = 8805410027643181977L;

    private final JButton cancelAllModifButton;
    private transient ActionListener cancelAllModifButtonActionListener;
    private final JButton closeButton;
    private transient ActionListener closeButtonActionListener;
    private DataTableJTable dataTableJTable;
    private final transient ArrayList<DataTableUpdateAbstract> updatesInProgress;
    private final transient ArrayList<DataTableUpdateAbstract> updatesToReturn;
    private final JButton validateButton;
    private transient ActionListener validateButtonActionListener;

    public DataTableDialog(final Scoop3Frame scoop3Frame, final Observation currentObservation,
	    final Map<String, List<String>> parametersOrder, final QCValues[] qcValuesSettable) {
	super(scoop3Frame, true);

	updatesInProgress = new ArrayList<>();
	updatesToReturn = new ArrayList<>();

	setLayout(new BorderLayout(5, 5));

	add(new JLabel("Station : " + (!currentObservation.getId().equals("") ? currentObservation.getId()
		: currentObservation.getReference()), JLabel.CENTER), BorderLayout.NORTH);

	final JPanel centerPanel = new JPanel();
	centerPanel.setLayout(new BorderLayout());

	addJTable(scoop3Frame, centerPanel, currentObservation, parametersOrder, qcValuesSettable);
	add(centerPanel, BorderLayout.CENTER);

	final JPanel southPanel = new JPanel();
	validateButton = new JButton(Messages.getMessage("bpc-gui.button-validate"));
	cancelAllModifButton = new JButton(Messages.getMessage("bpc-gui.button-cancel"));
	closeButton = new JButton(Messages.getMessage("bpc-gui.button-close"));
	southPanel.add((new JPanel()).add(validateButton));
	southPanel.add((new JPanel()).add(cancelAllModifButton));
	southPanel.add((new JPanel()).add(closeButton));
	add(southPanel, BorderLayout.SOUTH);

	addListeners();
	updateEnabledState();

	// FIXME
	final Dimension dialogDim = new Dimension(1100, 600);
	setPreferredSize(dialogDim);

	pack();
	setLocationRelativeTo(null);

	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    /**
     * Memorize a new update
     */
    public void addUpdatesForVariables(final DataTableUpdateAbstract newUpdatesForVariables) {
	updatesInProgress.add(newUpdatesForVariables);
	updateEnabledState();
    }

    /**
     * Display the JDialog and return the list of the updates to apply to the model
     *
     * @return the updatesToReturn
     */
    public ArrayList<DataTableUpdateAbstract> getUpdates() {
	CommonViewController.setKeyListenerEnabled(false);
	setVisible(true);
	CommonViewController.setKeyListenerEnabled(true);
	return updatesToReturn;
    }

    /**
     * Create the JTable and add it in the centerPanel
     *
     * @param scoop3Frame
     * @param centerPanel
     * @param currentObservation
     * @param parametersOrder
     * @param qcValuesSettable
     */
    private void addJTable(final Scoop3Frame scoop3Frame, final JPanel centerPanel,
	    final Observation currentObservation, final Map<String, List<String>> parametersOrder,
	    final QCValues[] qcValuesSettable) {

	final DataTableModel dataTableModel = new DataTableModel(currentObservation, parametersOrder);

	dataTableJTable = new DataTableJTable(this, scoop3Frame, dataTableModel, qcValuesSettable);

	centerPanel.add(dataTableJTable.getTableHeader(), BorderLayout.NORTH);
	centerPanel.add(new JScrollPane(dataTableJTable), BorderLayout.CENTER);
    }

    /**
     * Add all listeners
     */
    private void addListeners() {
	validateButtonActionListener = (final ActionEvent e) -> {
	    updatesToReturn.addAll(updatesInProgress);
	    updatesInProgress.clear();
	    updateEnabledState();
	};
	validateButton.addActionListener(validateButtonActionListener);

	cancelAllModifButtonActionListener = (final ActionEvent e) -> {
	    while (!updatesInProgress.isEmpty()) {
		final DataTableUpdateAbstract lastUpdatesForVariables = updatesInProgress
			.remove(updatesInProgress.size() - 1);
		dataTableJTable.cancelOneUpdate(lastUpdatesForVariables);
	    }
	    updateEnabledState();
	};
	cancelAllModifButton.addActionListener(cancelAllModifButtonActionListener);

	closeButtonActionListener = (final ActionEvent e) -> {
	    removeListeners();
	    setVisible(false);
	};
	closeButton.addActionListener(closeButtonActionListener);
    }

    /**
     * Remove listeners
     */
    private void removeListeners() {
	validateButton.removeActionListener(validateButtonActionListener);
	validateButtonActionListener = null;

	cancelAllModifButton.removeActionListener(cancelAllModifButtonActionListener);
	cancelAllModifButtonActionListener = null;

	closeButton.removeActionListener(closeButtonActionListener);
	closeButtonActionListener = null;
    }

    /**
     * Update the enabled state of buttons
     */
    private void updateEnabledState() {
	validateButton.setEnabled(!updatesInProgress.isEmpty());
	cancelAllModifButton.setEnabled(!updatesInProgress.isEmpty());
	closeButton.setEnabled(updatesInProgress.isEmpty());
    }
}
