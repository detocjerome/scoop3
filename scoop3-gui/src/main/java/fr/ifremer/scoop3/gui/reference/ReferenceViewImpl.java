package fr.ifremer.scoop3.gui.reference;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.bushe.swing.event.EventBus;

import fr.ifremer.scoop3.chart.view.panel.JScoop3ChartPanelAbstract;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.controller.worflow.StepCode;
import fr.ifremer.scoop3.controller.worflow.SubStep;
import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.messages.CADataErrorMessageItem;
import fr.ifremer.scoop3.events.GuiEventChangeMainPanelToStep;
import fr.ifremer.scoop3.gui.common.MetadataTable;
import fr.ifremer.scoop3.gui.common.jdialog.ReportJDialog;
import fr.ifremer.scoop3.gui.core.Scoop3Frame;
import fr.ifremer.scoop3.gui.data.DataViewImpl;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.model.Dataset;

/**
 * 
 *
 */
public class ReferenceViewImpl extends DataViewImpl {

    private JPanel scoop3ChartPanel;
    private JScrollPane scoop3ChartPanelScrollPane;

    /**
     * 
     * @param scoop3Frame
     * @param datasetMetadatasTable
     * @param observationMetadatasTable
     * @param dataset
     * @param report
     */
    public ReferenceViewImpl(final Scoop3Frame scoop3Frame, final MetadataTable datasetMetadatasTable,
	    final MetadataTable observationMetadatasTable, final Dataset dataset, final Report report) {
	super(scoop3Frame, datasetMetadatasTable, observationMetadatasTable, dataset, report);
	scoop3ChartPanel = null;

	validateButton.setText(Messages.getMessage("bpc-gui.button-validate"));
    }

    /**
     * Update the Chart Panel with a new Scoop3CharlPanel
     * 
     * @param newScoop3ChartPanel
     */
    public void updateChartPanel(final JScoop3ChartScrollPaneAbstract newScoop3ChartPanel, final String abscissaLabel,
	    final String ordonneeLabel) {
	if (scoop3ChartPanel != null) {
	    specificPrepareForDispose();
	}
	getEastPanel().setBackground(Color.white);

	scoop3ChartPanel = new JPanel();
	scoop3ChartPanel.setLayout(new GridBagLayout());

	final GridBagConstraints c = new GridBagConstraints();
	c.gridx = 0;
	c.gridy = 0;

	scoop3ChartPanel.add(new JLabel(), c);

	c.gridx = 1;
	c.gridy = 0;
	scoop3ChartPanel.add(new JLabel(abscissaLabel), c);

	c.gridx = 0;
	c.gridy = 1;
	scoop3ChartPanel.add(new JLabel(JScoop3ChartPanelAbstract.transformStringToVerticalWithHTML(ordonneeLabel)), c);

	c.gridx = 1;
	c.gridy = 1;
	scoop3ChartPanel.add(newScoop3ChartPanel, c);

	scoop3ChartPanelScrollPane = new JScrollPane(scoop3ChartPanel);
	scoop3ChartPanelScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	scoop3ChartPanelScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	getEastPanel().add(scoop3ChartPanelScrollPane, BorderLayout.CENTER);

	getEastPanel().revalidate();
	getEastPanel().repaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop3.gui.common.CommonViewImpl#cancelButtonClicked()
     */
    @Override
    protected void cancelButtonClicked() {
	EventBus.publish(new GuiEventChangeMainPanelToStep(StepCode.START, SubStep.GOHOME));

    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ifremer.scoop3.gui.common.CommonViewImpl#specificPrepareForDispose()
     */
    @Override
    protected void specificPrepareForDispose() {
	if (scoop3ChartPanelScrollPane != null) {
	    getEastPanel().remove(scoop3ChartPanelScrollPane);
	    scoop3ChartPanelScrollPane.removeAll();
	    scoop3ChartPanelScrollPane = null;
	}
	scoop3ChartPanel.removeAll();
	scoop3ChartPanel = null;
    }

    protected void repaintPanel() {
	getEastPanel().revalidate();
	getEastPanel().repaint();

	scoop3ChartPanel.revalidate();
	scoop3ChartPanel.repaint();
    }

    /**
     * @return the First error message if exists (or NULL)
     */
    public CADataErrorMessageItem getFirstDataErrorMessage() {
	if (ReportJDialog.getReportJDialog(getScoop3Frame(), this, report) != null) {
	    return ReportJDialog.getReportJDialog(getScoop3Frame(), this, report).getFirstDataErrorMessage();
	}
	return null;
    }
}
