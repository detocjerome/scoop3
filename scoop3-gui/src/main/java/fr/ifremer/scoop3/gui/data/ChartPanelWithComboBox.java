package fr.ifremer.scoop3.gui.data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import fr.ifremer.scoop3.chart.view.additionalGraphs.AdditionalGraph;
import fr.ifremer.scoop3.chart.view.panel.JScoop3ChartPanelAbstract;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneForProfile;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3GraphPanelListener;
import fr.ifremer.scoop3.core.validateParam.ValidatedDataParameterManager;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.infra.undo_redo.data.QCValueChange;
import fr.ifremer.scoop3.model.Profile;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.Parameter.LINK_PARAM_TYPE;
import javafx.geometry.Rectangle2D;

public class ChartPanelWithComboBox extends JPanel implements ActionListener, JScoop3GraphPanelListener {

    /**
     * Mouse Listener defined in BPC-GUI project
     */
    private class Scoop3ChartPanelMouseListener extends MouseAdapter {

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(final MouseEvent e) {
	    // Only for right click
	    if (SwingUtilities.isRightMouseButton(e)) {
		scoop3ChartPanelPopupMenu.show(e.getComponent(), e.getX(), e.getY());
	    }
	}

    }

    class ComboBoxRenderer extends JLabel implements ListCellRenderer<Object> {

	private static final long serialVersionUID = -5769977600864424314L;

	public ComboBoxRenderer() {
	    setOpaque(true);
	    setVerticalAlignment(CENTER);
	}

	/*
	 * This method add an image if needed and text corresponding to the selected value and returns the label, set up
	 * to display the text and image.
	 */
	@Override
	public Component getListCellRendererComponent(final JList<? extends Object> list, final Object value,
		final int index, final boolean isSelected, final boolean cellHasFocus) {
	    // Get the selected index. (The index param isn't always valid, so
	    // just use the value.)
	    final int selectedIndex = ((Integer) value).intValue();

	    if (isSelected) {
		setBackground(list.getSelectionBackground());
		setForeground(list.getSelectionForeground());
	    } else {
		setBackground(list.getBackground());
		setForeground(list.getForeground());
	    }

	    final String parameterCode = comboBoxValues[selectedIndex]
		    .split(DataViewController.SEPARATOR_FOR_COMBO_LABELS)[0].trim();
	    final String refParameterCode = comboBoxValues[selectedIndex]
		    .split(DataViewController.SEPARATOR_FOR_COMBO_LABELS)[1].trim();

	    // Set the icon and text. If icon was null, say so.
	    if (ABLE_TO_VALIDATE_PARAMETERS_IN_DATA_VIEW) {
		final OceanicParameter param = dataViewController.getCommonViewModel().getObservation(observationNumber)
			.getOceanicParameter(parameterCode);
		// If it is a parameter of type COMPUTED_CONTROL => add an
		// asterisk OR if time series with lat/time or lon/time
		if (((param != null) && (param.getLinkParamType() == LINK_PARAM_TYPE.COMPUTED_CONTROL))
			|| ((parameterCode.equalsIgnoreCase("Latitude") || parameterCode.equalsIgnoreCase("Longitude"))
				&& refParameterCode.equalsIgnoreCase("Time"))) {
		    setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/asterisk_16x16.png")));

		}
		// Else if the "refParameterCode" is not the reference parameter
		// => add a red icon
		else if (!dataViewController.getCommonViewModel().getObservation(observationNumber)
			.getReferenceParameter().getCode().equals(refParameterCode)) {
		    setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/dialog_cancel_16x16.png")));

		}
		// Else if it is checked => add a green check icon
		else if (ValidatedDataParameterManager.getInstance().isValidated(parameterCode)
			&& refParameterCode.equals(referenceParameterCodeForThisObservation)) {
		    setIcon(ValidatedDataParameterManager.getValidatedImageIcon());

		}
		// Else ... a blank icon
		else {
		    setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/transparent_16x16.png")));
		}
	    }

	    setText(comboBoxValuesToDisplay[selectedIndex]);
	    // Set the Text on the LEFT of the Icon
	    setHorizontalTextPosition(SwingConstants.LEFT);

	    return this;
	}
    }

    private static final boolean ABLE_TO_VALIDATE_PARAMETERS_IN_DATA_VIEW;

    /**
     *
     */
    private static final long serialVersionUID = 700004794812411731L;

    private boolean additionalSeriesIsDisplayed;

    private boolean adjustValuePropagation = true;

    /**
     * Combobox values ...
     */
    private String[] comboBoxValues;
    /**
     * Combobox values to display ...
     */
    private String[] comboBoxValuesToDisplay;

    /**
     * Reference on the DataViewController
     */
    private final transient DataViewController dataViewController;

    private int observationNumber = 0;
    /**
     * List of all available parameters
     */
    private JComboBox<Integer> parametersComboBox;
    /**
     * Panel for ComboBox
     */
    private JPanel parametersComboBoxPanel;
    /**
     * Panel for ComboBox number
     */
    private JPanel parametersNumberComboBoxPanel;

    /**
     * Panel for ComboBox number
     */
    private JPanel timeSeriePlayerPanel;

    private String referenceParameterCodeForThisObservation;

    /**
     * Reference on the JScoop3ChartPanelAbstract
     */
    private JScoop3ChartPanelAbstract scoop3ChartPanel;

    /**
     * Reference on the Mouse Listener added to the Scoop3ChartPanel
     */
    private transient Scoop3ChartPanelMouseListener scoop3ChartPanelMouseListener;

    /**
     * Reference on the Popup Menu used by this component
     */
    private Scoop3ChartPanelPopupMenu scoop3ChartPanelPopupMenu;

    private transient MouseAdapter scoop3ChartScrollPaneMouseListener;

    private transient MouseMotionAdapter scoop3ChartScrollPaneMouseMotionListener;
    /**
     * Selected value in the ComboBox
     */
    private String selectedValue;
    private JCheckBox validateParamCheckBox;

    private JLabel totalSectionLabel;
    private JLabel currentSectionLabel;

    static {
	ABLE_TO_VALIDATE_PARAMETERS_IN_DATA_VIEW = "true".equalsIgnoreCase(
		FileConfig.getScoop3FileConfig().getString("gui.able-to-validate-parameters-in-data-view"));
    }

    public ChartPanelWithComboBox(final DataViewController dataViewController,
	    final JScoop3ChartPanelAbstract scoop3ChartPanel, final String selectedValue, final String refParamCode) {

	this.dataViewController = dataViewController;
	this.selectedValue = selectedValue;

	scoop3ChartPanelPopupMenu = new Scoop3ChartPanelPopupMenu(this, dataViewController);

	validateParamCheckBox = new JCheckBox();
	validateParamCheckBox.setOpaque(false);
	validateParamCheckBox.addActionListener(this);
	validateParamCheckBox.setVisible(ABLE_TO_VALIDATE_PARAMETERS_IN_DATA_VIEW);

	setBackground(Color.WHITE);
	setLayout(new BorderLayout());

	updateJScoop3ChartPanel(scoop3ChartPanel, refParamCode);

	additionalSeriesIsDisplayed = false;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {

	// Management of the JComboBox update
	if (ABLE_TO_VALIDATE_PARAMETERS_IN_DATA_VIEW) {
	    // Update the Checkbox state
	    final String parameterCode = selectedValue.split(DataViewController.SEPARATOR_FOR_COMBO_LABELS)[0].trim();
	    final String refParameterCode = selectedValue.split(DataViewController.SEPARATOR_FOR_COMBO_LABELS)[1]
		    .trim();
	    if (refParameterCode.equals(referenceParameterCodeForThisObservation)) {
		ValidatedDataParameterManager.getInstance().setIsValidated(parameterCode,
			validateParamCheckBox.isSelected());
	    }
	}

	// Management of the JComboBox update
	final String newSelectedValue = comboBoxValues[(int) parametersComboBox.getSelectedItem()];

	if (!selectedValue.equals(newSelectedValue)) {
	    selectedValue = newSelectedValue;

	    // put all the selected values in the list paramOfDifferentGraphs
	    dataViewController.getParamOfDifferentGraphs().removeAll(dataViewController.getParamOfDifferentGraphs());
	    for (int index = 0; index < dataViewController.listGraphs.size(); index++) {
		dataViewController.getParamOfDifferentGraphs()
			.add(dataViewController.listGraphs.get(index).getSelectedValue());
	    }
	    if (dataViewController.isTimeserieDivided()) {
		dataViewController.updateChartPanelAfterDivideTimeserie(this, true, null,
			dataViewController.getDivideTimeserieChartDataset());
	    } else {
		dataViewController.updateChartPanelWithComboBox(this, false, true, null);
	    }
	    if ((dataViewController.getCommonViewImpl().getjSpinnerPrev() != null)
		    && (dataViewController.getCommonViewImpl().getjSpinnerNext() != null)
		    && (((Integer) dataViewController.getCommonViewImpl().getjSpinnerPrev().getValue() != 0)
			    || ((Integer) dataViewController.getCommonViewImpl().getjSpinnerNext().getValue() != 0))) {
		final Profile[] tempProfilesArray = dataViewController.getTempProfilesArray();
		final List<Profile> tempProfiles = new ArrayList<>();
		if (tempProfilesArray != null) {
		    if (tempProfilesArray[0] != null) {
			tempProfiles.add(tempProfilesArray[0]);
		    }
		    if (tempProfilesArray[1] != null) {
			tempProfiles.add(tempProfilesArray[1]);
		    }
		}
		if (dataViewController.isTimeserieDivided()) {
		    dataViewController.updateChartPanelAfterDivideTimeserie(this, true, tempProfiles,
			    dataViewController.getDivideTimeserieChartDataset());
		} else {
		    dataViewController.updateChartPanelWithComboBox(this, false, true, tempProfiles);
		}
	    }
	}

	updateCheckboxState(true);

	simulateAClickOnScrollbarToAvoidShifting();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3GraphPanelListener# changeCurrentStation(int[])
     */
    @Override
    public void changeCurrentStation(final int[] newCurrentStation) {
	dataViewController.changeCurrentStation(newCurrentStation);
    }

    /**
     * @param displayChartTotalArea
     * @param displayChartSelectArea
     * @return
     */
    public HashMap<String, double[]> computeMinMaxForVariables(final Rectangle2D displayChartTotalArea,
	    final Rectangle2D displayChartSelectArea) {
	final HashMap<String, double[]> minMaxForVariables = new HashMap<>();
	final String abscissaVar = scoop3ChartPanel.getjScoop3ChartScrollPane().getAbscissaPhysicalVar().getLabel();
	final double minAbscissa = scoop3ChartPanel.getjScoop3ChartScrollPane().getMinAbscissaPhysVal();
	final double maxAbscissa = scoop3ChartPanel.getjScoop3ChartScrollPane().getMaxAbscissaPhysVal();
	final String ordinateVar = scoop3ChartPanel.getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getLabel();
	final double minOrdinate = scoop3ChartPanel.getjScoop3ChartScrollPane().getMinOrdinatePhysVal();
	final double maxOrdinate = scoop3ChartPanel.getjScoop3ChartScrollPane().getMaxOrdinatePhysVal();

	final float minAbscissaSelected = (float) (minAbscissa + ((maxAbscissa - minAbscissa)
		* ((float) displayChartSelectArea.getMinX() / displayChartTotalArea.getWidth())));
	final float maxAbscissaSelected = (float) (minAbscissa + ((maxAbscissa - minAbscissa)
		* (((float) displayChartSelectArea.getMinX() + displayChartSelectArea.getWidth())
			/ displayChartTotalArea.getWidth())));
	float minOrdinateSelected;
	float maxOrdinateSelected;

	if (scoop3ChartPanel.getjScoop3ChartScrollPane() instanceof JScoop3ChartScrollPaneForProfile) {
	    minOrdinateSelected = (float) (minOrdinate + ((maxOrdinate - minOrdinate)
		    * ((float) displayChartSelectArea.getMinY() / displayChartTotalArea.getHeight())));
	    maxOrdinateSelected = (float) (minOrdinate + ((maxOrdinate - minOrdinate)
		    * (((float) displayChartSelectArea.getMinY() + displayChartSelectArea.getHeight())
			    / displayChartTotalArea.getHeight())));
	} else {
	    // /!\ ordinate is reversed
	    minOrdinateSelected = (float) (minOrdinate
		    + (((maxOrdinate - minOrdinate) * (displayChartTotalArea.getHeight()
			    - (displayChartSelectArea.getMinY() + displayChartSelectArea.getHeight())))
			    / displayChartTotalArea.getHeight()));
	    maxOrdinateSelected = (float) (minOrdinate + (((maxOrdinate - minOrdinate)
		    * (displayChartTotalArea.getHeight() - displayChartSelectArea.getMinY()))
		    / displayChartTotalArea.getHeight()));
	}

	minMaxForVariables.put(abscissaVar, new double[] { minAbscissaSelected, maxAbscissaSelected });
	minMaxForVariables.put(ordinateVar, new double[] { minOrdinateSelected, maxOrdinateSelected });
	return minMaxForVariables;
    }

    public void fitToData() {

	final Rectangle2D viewableArea = scoop3ChartPanel.getjScoop3ChartScrollPane().getDataAreaForZoomLevelCurrent();
	final Rectangle2D totalArea = scoop3ChartPanel.getjScoop3ChartScrollPane().getDataAreaForZoomLevelOne();

	final String abscissaVar = scoop3ChartPanel.getjScoop3ChartScrollPane().getAbscissaPhysicalVar().getLabel();
	final String ordinateVar = scoop3ChartPanel.getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getLabel();

	final double minAbscissa = scoop3ChartPanel.getjScoop3ChartScrollPane().getMinAbscissaPhysVal();
	final double maxAbscissa = scoop3ChartPanel.getjScoop3ChartScrollPane().getMaxAbscissaPhysVal();
	final double minOrdinate = scoop3ChartPanel.getjScoop3ChartScrollPane().getMinOrdinatePhysVal();
	final double maxOrdinate = scoop3ChartPanel.getjScoop3ChartScrollPane().getMaxOrdinatePhysVal();

	if (scoop3ChartPanel.getjScoop3ChartScrollPane() instanceof JScoop3ChartScrollPaneForProfile) {
	    final float minOrdinateSelected = (float) (minOrdinate
		    + ((maxOrdinate - minOrdinate) * ((float) viewableArea.getMinY() / totalArea.getHeight())));
	    final float maxOrdinateSelected = (float) (minOrdinate + ((maxOrdinate - minOrdinate)
		    * (((float) viewableArea.getMinY() + viewableArea.getHeight()) / totalArea.getHeight())));

	    Double[] values = scoop3ChartPanel.getjScoop3ChartScrollPane().getOrdinatePhysicalVar()
		    .getPhysicalValuesByStation().get(scoop3ChartPanel.getjScoop3ChartScrollPane().getCurrentStation());
	    final int maxOrdinateIndex = values.length;

	    // Compute min and max index for the Reference parameter
	    int minIndex = -1;
	    int maxIndex = -1;
	    for (int index = 0; (index < maxOrdinateIndex) && (maxIndex == -1); index++) {
		final double currentValue = values[index];
		if (currentValue < minOrdinateSelected) {
		    minIndex = index;
		}
		if ((currentValue > maxOrdinateSelected) && (maxIndex == -1)) {
		    maxIndex = index;
		}
	    }
	    minIndex--;

	    // Compute the real Min and Max values to display with the
	    values = scoop3ChartPanel.getjScoop3ChartScrollPane().getAbscissaPhysicalVar().getPhysicalValuesByStation()
		    .get(scoop3ChartPanel.getjScoop3ChartScrollPane().getCurrentStation());
	    double computedMinAbscissa = Double.MAX_VALUE;
	    double computedMaxAbscissa = Double.MIN_VALUE;
	    final int maxIndexForParam = Math.min(values.length - 1, maxIndex);
	    for (int index = Math.max(minIndex, 0); index <= maxIndexForParam; index++) {
		final double currentValue = values[index];
		computedMinAbscissa = Math.min(computedMinAbscissa, currentValue);
		computedMaxAbscissa = Math.max(computedMaxAbscissa, currentValue);
	    }
	    if ((computedMinAbscissa != Double.MAX_VALUE) && (computedMaxAbscissa != Double.MIN_VALUE)) {
		final double diffAbscissa = Math.abs(computedMaxAbscissa - computedMinAbscissa);

		computedMinAbscissa -= diffAbscissa * 0.05f;
		computedMaxAbscissa += diffAbscissa * 0.05f;

		final HashMap<String, double[]> minMaxForVariables = new HashMap<>();
		minMaxForVariables.put(abscissaVar, new double[] { computedMinAbscissa, computedMaxAbscissa });
		scoop3ChartPanel.getjScoop3ChartScrollPane().zoomForVariables(minMaxForVariables, true, false,
			false/* , observationNumber */);
	    }
	} else {
	    final float minAbscissaSelected = (float) (minAbscissa
		    + ((maxAbscissa - minAbscissa) * ((float) viewableArea.getMinX() / totalArea.getWidth())));
	    final float maxAbscissaSelected = (float) (minAbscissa + ((maxAbscissa - minAbscissa)
		    * (((float) viewableArea.getMinX() + viewableArea.getWidth()) / totalArea.getWidth())));

	    Double[] values = scoop3ChartPanel.getjScoop3ChartScrollPane().getAbscissaPhysicalVar()
		    .getPhysicalValuesByStation().get(scoop3ChartPanel.getjScoop3ChartScrollPane().getCurrentStation());
	    final int maxAbscissaIndex = values.length;

	    // Compute min and max index for the Reference parameter
	    int minIndex = -1;
	    int maxIndex = -1;
	    for (int index = 0; (index < maxAbscissaIndex) && (maxIndex == -1); index++) {
		final double currentValue = values[index];
		if (currentValue < minAbscissaSelected) {
		    minIndex = index;
		}
		if ((currentValue > maxAbscissaSelected) && (maxIndex == -1)) {
		    maxIndex = index;
		}
	    }
	    minIndex--;

	    // Compute the real Min and Max values to display with the
	    values = scoop3ChartPanel.getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getPhysicalValuesByStation()
		    .get(scoop3ChartPanel.getjScoop3ChartScrollPane().getCurrentStation());
	    double computedMinOrdinate = Double.MAX_VALUE;
	    double computedMaxOrdinate = Double.MIN_VALUE;
	    final int maxIndexForParam = Math.min(values.length - 1, maxIndex);
	    for (int index = Math.max(minIndex, 0); index <= maxIndexForParam; index++) {
		final double currentValue = values[index];
		computedMinOrdinate = Math.min(computedMinOrdinate, currentValue);
		computedMaxOrdinate = Math.max(computedMaxOrdinate, currentValue);
	    }
	    if ((computedMinOrdinate != Double.MAX_VALUE) && (computedMaxOrdinate != Double.MIN_VALUE)) {
		final double diffOrdinate = Math.abs(computedMaxOrdinate - computedMinOrdinate);

		computedMinOrdinate -= diffOrdinate * 0.05f;
		computedMaxOrdinate += diffOrdinate * 0.05f;

		final HashMap<String, double[]> minMaxForVariables = new HashMap<>();
		minMaxForVariables.put(ordinateVar, new double[] { computedMinOrdinate, computedMaxOrdinate });
		scoop3ChartPanel.getjScoop3ChartScrollPane().zoomForVariables(minMaxForVariables, true, false,
			false/* , observationNumber */);
	    }
	}
    }

    /**
     * @return the LINK_PARAM_TYPE
     */
    public LINK_PARAM_TYPE getLinkParamType() {
	return dataViewController.getLinkParamTypeFor(this);
    }

    /**
     * @return the scoop3ChartPanel
     */
    public JScoop3ChartPanelAbstract getScoop3ChartPanel() {
	return scoop3ChartPanel;
    }

    /**
     * @return the selectedValue
     */
    public String getSelectedValue() {
	return selectedValue;
    }

    public boolean isAdditionalSeriesDisplayed() {
	return additionalSeriesIsDisplayed;
    }

    /**
     * @return true is the scoop3ChartPanel has an active Selection Box
     */
    public boolean isSelectionBoxActive() {
	return scoop3ChartPanel.isSelectionBoxActive();
    }

    /**
     * Unload data to save memory
     */
    public void prepareForDispose() {
	if (scoop3ChartPanel != null) {
	    if (scoop3ChartPanelMouseListener != null) {
		scoop3ChartPanel.removeMouseListener(scoop3ChartPanelMouseListener);
		scoop3ChartPanelMouseListener = null;
	    }
	    if (scoop3ChartScrollPaneMouseListener != null) {
		scoop3ChartPanel.getjScoop3ChartScrollPane().getReferenceScrollBar()
			.removeMouseListener(scoop3ChartScrollPaneMouseListener);
		scoop3ChartScrollPaneMouseListener = null;
	    }
	    if (scoop3ChartScrollPaneMouseMotionListener != null) {
		scoop3ChartPanel.getjScoop3ChartScrollPane().getReferenceScrollBar()
			.removeMouseMotionListener(scoop3ChartScrollPaneMouseMotionListener);
		scoop3ChartScrollPaneMouseMotionListener = null;
	    }
	    remove(scoop3ChartPanel);
	    scoop3ChartPanel.prepareForDispose();
	    scoop3ChartPanel = null;
	}
	if (parametersComboBoxPanel != null) {
	    parametersComboBoxPanel.removeAll();
	    parametersComboBoxPanel = null;
	}
	if (parametersComboBox != null) {
	    parametersComboBox.removeActionListener(this);
	    parametersComboBox = null;
	}

	if (validateParamCheckBox != null) {
	    validateParamCheckBox.removeActionListener(this);
	    validateParamCheckBox = null;
	}
    }

    /**
     * QCs have changed for this variable in an other Panel
     *
     * @param qcsChanged
     */
    public void qcsChange(final List<QCValueChange> qcsChanged) {
	scoop3ChartPanel.qcsChange(qcsChanged);
    }

    /**
     * Remove the Selection Box if exists
     */
    public void removeSelectionBox() {
	scoop3ChartPanel.getjScoop3ChartScrollPane().removeSelectionBox();
    }

    /**
     * Repaint chart
     */
    public void repaintChart() {
	scoop3ChartPanel.repaintChart();
    }

    /**
     * Revert QCs
     *
     * @param qcsChanged
     */
    public void revertQC(final List<QCValueChange> qcsChanged) {
	scoop3ChartPanel.revertQC(qcsChanged);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3GraphPanelListener# selectionDoneWithOtherMouseMode(java.awt.
     * Rectangle , java.awt.Rectangle, java.awt.Point, java.awt.Point)
     */
    @Override
    public void selectionDoneWithOtherMouseMode(final Rectangle2D displayChartTotalArea,
	    final Rectangle2D displayChartSelectArea, final Point displayChartSelectAreaNewStartPoint,
	    final Point displayChartSelectAreaNewEndPoint) {
	if ((JScoop3ChartScrollPaneAbstract.getCoefX() == 0)
		// If the Parameter is a computed parameter (with type CONTROL),
		// it is not possible to change the
		// QCValue
		&& (getLinkParamType() != LINK_PARAM_TYPE.COMPUTED_CONTROL)) {
	    dataViewController.selectionDoneWithOtherMouseMode(displayChartTotalArea, displayChartSelectArea,
		    displayChartSelectAreaNewStartPoint, displayChartSelectAreaNewEndPoint, this);
	} else {
	    dataViewController.removeSelectionBox();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3GraphPanelListener# selectionDoneWithRemovalMode(java.awt.
     * Rectangle , java.awt.Rectangle, java.awt.Point, java.awt.Point)
     */
    @Override
    public void selectionDoneWithRemovalMode(final Rectangle2D displayChartTotalArea,
	    final Rectangle2D displayChartSelectArea, final Point displayChartSelectAreaNewStartPoint,
	    final Point displayChartSelectAreaNewEndPoint) {
	if ((JScoop3ChartScrollPaneAbstract.getCoefX() == 0)
		&& (getLinkParamType() != LINK_PARAM_TYPE.COMPUTED_CONTROL)) {
	    dataViewController.selectionDoneWithRemovalMode(displayChartTotalArea, displayChartSelectArea,
		    displayChartSelectAreaNewStartPoint, displayChartSelectAreaNewEndPoint, this);
	} else {
	    dataViewController.removeSelectionBox();
	}
    }

    public void setAdditionalSeriesToDisplay(final List<AdditionalGraph> additionalGraphs) {
	additionalSeriesIsDisplayed = additionalGraphs != null;
	scoop3ChartPanel.setAdditionalSeriesToDisplay(additionalGraphs);
    }

    /**
     * @param adjustValuePropagation
     *            the adjustValuePropagation to set
     */
    public void setAdjustValuePropagation(final boolean adjustValuePropagation) {
	this.adjustValuePropagation = adjustValuePropagation;
    }

    /**
     * Set the number of parameters in Combo Box
     *
     * @param parametersComboNumber
     */
    public void setComboBoxNumber(final int parametersComboNumber) {
	if (parametersNumberComboBoxPanel != null) {
	    remove(parametersNumberComboBoxPanel);
	    parametersNumberComboBoxPanel.removeAll();
	    parametersNumberComboBoxPanel = null;
	}
	parametersNumberComboBoxPanel = new JPanel();
	parametersNumberComboBoxPanel.setOpaque(false);

	final JLabel parametersComboNumberString = new JLabel("(" + parametersComboNumber + ")");
	parametersNumberComboBoxPanel.add(parametersComboNumberString);
	parametersComboBoxPanel.add(parametersNumberComboBoxPanel);
    }

    /**
     * Set the Combo Box values
     *
     * @param comboBoxValues
     */
    public void setComboBoxValues(final String[] comboBoxValues, final String[] comboBoxValuesWithUnit) {
	if (parametersComboBoxPanel != null) {
	    remove(parametersComboBoxPanel);
	    parametersComboBoxPanel.removeAll();
	    parametersComboBoxPanel = null;
	    parametersComboBox.removeActionListener(this);
	    parametersComboBox = null;
	}

	this.comboBoxValues = comboBoxValues;
	this.comboBoxValuesToDisplay = comboBoxValuesWithUnit == null ? comboBoxValues : comboBoxValuesWithUnit;
	final Integer[] intArray = new Integer[comboBoxValues.length];
	for (int i = 0; i < comboBoxValues.length; i++) {
	    intArray[i] = i;
	}
	parametersComboBox = new JComboBox<>(intArray);
	final ComboBoxRenderer renderer = new ComboBoxRenderer();
	parametersComboBox.setRenderer(renderer);

	int index = 0;
	for (final String comboBoxValue : comboBoxValues) {
	    if (comboBoxValue.equals(selectedValue)) {
		parametersComboBox.setSelectedItem(index);
	    }
	    index++;
	}

	parametersComboBox.addActionListener(this);
	parametersComboBoxPanel = new JPanel();
	parametersComboBoxPanel.setOpaque(false);

	validateParamCheckBox.setVisible(ABLE_TO_VALIDATE_PARAMETERS_IN_DATA_VIEW);
	parametersComboBoxPanel.add(validateParamCheckBox);

	parametersComboBoxPanel.add(parametersComboBox);
	add(parametersComboBoxPanel, BorderLayout.NORTH);
    }

    /**
     * Add graphic player for divided timeseries
     */
    public void addPlayer() {
	if (timeSeriePlayerPanel != null) {
	    remove(timeSeriePlayerPanel);
	    timeSeriePlayerPanel.removeAll();
	    timeSeriePlayerPanel = null;
	}
	timeSeriePlayerPanel = new JPanel();
	timeSeriePlayerPanel.setOpaque(false);

	// icons
	final ImageIcon leftArrowIcon = new ImageIcon(getClass().getClassLoader().getResource("icons/3leftarrow.png"));
	final ImageIcon rightArrowIcon = new ImageIcon(
		getClass().getClassLoader().getResource("icons/3rightarrow.png"));

	// Left button
	final JButton leftNavigationButton = new JButton(leftArrowIcon);
	leftNavigationButton.addActionListener((final ActionEvent e) -> {
	    final int sectionNumber = dataViewController.getTotalSectionNumberForTimeserie();
	    final int currentSectionNumber = dataViewController.getCurrentSectionNumberForTimeserie();
	    if (currentSectionNumber > 1) {
		dataViewController.setCurrentSectionNumberForTimeserie(currentSectionNumber - 1);
	    } else if (currentSectionNumber == 1) {
		dataViewController.setCurrentSectionNumberForTimeserie(sectionNumber);
	    }

	    playerActionPerformed();
	});

	final Integer currentSectionNumberValue = dataViewController.getCurrentSectionNumberForTimeserie();
	currentSectionLabel = new JLabel();
	if (currentSectionNumberValue == null) {
	    currentSectionLabel.setText("1");
	} else {
	    currentSectionLabel.setText(String.valueOf(currentSectionNumberValue));
	}

	final JLabel separationLabel = new JLabel("/");

	final Integer totalSectionNumberValue = dataViewController.getTotalSectionNumberForTimeserie();
	totalSectionLabel = new JLabel();
	if (totalSectionNumberValue == null) {
	    totalSectionLabel.setText("1");
	} else {
	    totalSectionLabel.setText(String.valueOf(totalSectionNumberValue));
	}

	// right button
	final JButton rightNavigationButton = new JButton(rightArrowIcon);
	rightNavigationButton.addActionListener((final ActionEvent e) -> {
	    final int totalSectionNumber = dataViewController.getTotalSectionNumberForTimeserie();
	    final int currentSectionNumber = dataViewController.getCurrentSectionNumberForTimeserie();

	    if (currentSectionNumber < totalSectionNumber) {
		// currentSectionCopy.setText(String.valueOf(Integer.parseInt(currentSectionCopy.getText()) + 1));
		dataViewController.setCurrentSectionNumberForTimeserie(currentSectionNumber + 1);
	    } else if (totalSectionNumber == currentSectionNumber) {
		// currentSectionCopy.setText("1");
		dataViewController.setCurrentSectionNumberForTimeserie(1);
	    }

	    playerActionPerformed();
	});

	timeSeriePlayerPanel.add(leftNavigationButton);
	timeSeriePlayerPanel.add(currentSectionLabel);
	timeSeriePlayerPanel.add(separationLabel);
	timeSeriePlayerPanel.add(totalSectionLabel);
	timeSeriePlayerPanel.add(rightNavigationButton);

	parametersComboBoxPanel.add(timeSeriePlayerPanel);

    }

    /**
     * Update section on timeserie
     */
    private void playerActionPerformed() {
	final Integer totalSectionNumberValue = dataViewController.getTotalSectionNumberForTimeserie();
	final Integer currentSectionNumberValue = dataViewController.getCurrentSectionNumberForTimeserie();

	if (dataViewController.getTotalSectionNumberForTimeserie() == 0) {
	    SC3Logger.LOGGER.error("Number of sections must be between 1 and ");
	} else if (dataViewController.getCurrentSectionNumberForTimeserie() > totalSectionNumberValue) {
	    SC3Logger.LOGGER
		    .error("The current section value must be a number between 1 and " + totalSectionNumberValue);
	} else {
	    SC3Logger.LOGGER.info("Timeserie sections number : " + totalSectionNumberValue
		    + " / Current section index : " + currentSectionNumberValue);
	    dataViewController.divideTimeserie(dataViewController.getTotalSectionNumberForTimeserie(),
		    dataViewController.getCurrentSectionNumberForTimeserie(), true);
	}

	// update sectionCopy
	if (totalSectionNumberValue == null) {
	    totalSectionLabel.setText("1");
	} else {
	    totalSectionLabel.setText(String.valueOf(totalSectionNumberValue));
	}

	// update currentSectionCopy
	if (currentSectionNumberValue == null) {
	    currentSectionLabel.setText("1");
	} else {
	    currentSectionLabel.setText(String.valueOf(currentSectionNumberValue));
	}

    }

    public void setCurrentLevel(final int levelIndex) {
	scoop3ChartPanel.setCurrentLevel(levelIndex);
    }

    /**
     * Update the selected Station
     *
     * @param observationNumber
     */
    public void setCurrentStation(final int observationNumber) {
	this.observationNumber = observationNumber;
	scoop3ChartPanel.getjScoop3ChartScrollPane().setCurrentStation(observationNumber);
	validate();
	repaint();

	// zoomoff if obs isnt in the same plateform.
	final int firstObsIndex = scoop3ChartPanel.getjScoop3ChartScrollPane().getFirstObservationIndex();
	final int lastObsIndex = scoop3ChartPanel.getjScoop3ChartScrollPane().getLastObservationIndex();
	if ((observationNumber < firstObsIndex) || (observationNumber > lastObsIndex)) {
	    zoomAll();
	}
    }

    public void updateJComboBox() {
	if (parametersComboBox != null) {
	    parametersComboBox.revalidate();
	    parametersComboBox.repaint();
	}
	updateCheckboxState(false);
    }

    /**
     * Update scoop3ChartPanel and refresh panel
     *
     * @param newScoop3ChartPanel
     * @param refParamCode
     */
    public void updateJScoop3ChartPanel(final JScoop3ChartPanelAbstract newScoop3ChartPanel,
	    final String refParamCode) {
	if (scoop3ChartPanel != null) {
	    scoop3ChartPanel.getjScoop3ChartScrollPane().removeJScoop3GraphPanelListener(this);
	    if (scoop3ChartPanelMouseListener != null) {
		scoop3ChartPanel.removeMouseListener(scoop3ChartPanelMouseListener);
	    }
	    if (scoop3ChartScrollPaneMouseListener != null) {
		scoop3ChartPanel.getjScoop3ChartScrollPane().getReferenceScrollBar()
			.removeMouseListener(scoop3ChartScrollPaneMouseListener);
	    }
	    if (scoop3ChartScrollPaneMouseMotionListener != null) {
		scoop3ChartPanel.getjScoop3ChartScrollPane().getReferenceScrollBar()
			.removeMouseMotionListener(scoop3ChartScrollPaneMouseMotionListener);
	    }

	    remove(scoop3ChartPanel);
	}

	scoop3ChartPanel = newScoop3ChartPanel;

	scoop3ChartPanel.getjScoop3ChartScrollPane().addJScoop3GraphPanelListener(this);

	if (scoop3ChartPanelMouseListener == null) {
	    scoop3ChartPanelMouseListener = new Scoop3ChartPanelMouseListener();
	}
	scoop3ChartPanel.addMouseListener(scoop3ChartPanelMouseListener);

	if (scoop3ChartScrollPaneMouseListener == null) {
	    scoop3ChartScrollPaneMouseListener = new MouseAdapter() {

		/*
		 * (non-Javadoc)
		 *
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event. MouseEvent)
		 */
		@Override
		public void mousePressed(final MouseEvent e) {
		    super.mousePressed(e);
		    adjustValuePropagation = true;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event. MouseEvent)
		 */
		@Override
		public void mouseReleased(final MouseEvent e) {
		    super.mouseReleased(e);
		    adjustValuePropagation = false;
		    scoop3ChartPanel.getjScoop3ChartScrollPane().updateDataAreaForZoomLevelCurrent();
		    zoomOnDisplayArea(scoop3ChartPanel.getjScoop3ChartScrollPane().getDataAreaForZoomLevelOne(),
			    scoop3ChartPanel.getjScoop3ChartScrollPane().getDataAreaForZoomLevelCurrent(), null, null,
			    false, null);
		    // dataViewController.fitToDataAllGraphs();
		}
	    };
	}
	scoop3ChartPanel.getjScoop3ChartScrollPane().getReferenceScrollBar()
		.addMouseListener(scoop3ChartScrollPaneMouseListener);

	if (scoop3ChartScrollPaneMouseMotionListener == null) {
	    scoop3ChartScrollPaneMouseMotionListener = new MouseMotionAdapter() {
		/*
		 * (non-Javadoc)
		 *
		 * @see java.awt.event.MouseMotionAdapter#mouseDragged(java.awt.event .MouseEvent)
		 */
		@Override
		public void mouseDragged(final MouseEvent e) {
		    super.mouseDragged(e);
		    if (adjustValuePropagation /*
					        * && !dataViewController. isSuperposedMode()
					        */) {
			scoop3ChartPanel.getjScoop3ChartScrollPane().updateDataAreaForZoomLevelCurrent();
			zoomOnDisplayArea(scoop3ChartPanel.getjScoop3ChartScrollPane().getDataAreaForZoomLevelOne(),
				scoop3ChartPanel.getjScoop3ChartScrollPane().getDataAreaForZoomLevelCurrent(), null,
				null, false, null);
			// dataViewController.fitToDataAllGraphs();
		    }
		}
	    };
	}
	scoop3ChartPanel.getjScoop3ChartScrollPane().getReferenceScrollBar()
		.addMouseMotionListener(scoop3ChartScrollPaneMouseMotionListener);

	add(scoop3ChartPanel, BorderLayout.CENTER);

	referenceParameterCodeForThisObservation = refParamCode;
	updateCheckboxState(false);

	revalidate();
	repaint();
    }

    /**
     * Update the QC in the scoop3ChartPanel
     *
     * @param obsId
     *
     * @param currentStationOnly
     *            true means only for Current Station, false means for all Stations
     * @param qcToSet
     *            the QCValues to set
     * @param secondParameterIsRef
     */
    public List<QCValueChange> updateQCs(final List<String> obsIds, final boolean currentStationOnly, final int qcToSet,
	    final boolean secondParameterIsRef, final List<List<? extends Number>> referenceValues,
	    final String superposedModeEnum, final boolean isBPCVersion) {
	return scoop3ChartPanel.updateQCs(obsIds, currentStationOnly, qcToSet, secondParameterIsRef, referenceValues,
		superposedModeEnum, isBPCVersion);
    }

    /**
     * Set Zoom to initial for the JScoop3ChartPanel
     */
    @Override
    public void zoomAll() {
	scoop3ChartPanel.getjScoop3ChartScrollPane().zoomAll();
    }

    /**
     * Set Zoom to the given variables Min/Max for the JScoop3ChartPanel
     */
    public void zoomForVariables(final Map<String, double[]> minMaxForVariables, final boolean zoomOnGraph,
	    final boolean reverseHorizontalScrollBar, final boolean precisionZoomOn/*
										    * , final int observationNumber
										    */) {
	scoop3ChartPanel.getjScoop3ChartScrollPane().zoomForVariables(minMaxForVariables, zoomOnGraph,
		reverseHorizontalScrollBar, precisionZoomOn/* , observationNumber */);
    }

    /**
     * Zoom In the JScoop3ChartPanel
     */
    public void zoomIn() {
	scoop3ChartPanel.getjScoop3ChartScrollPane().zoomIn();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.chart.view.scrollpane.JScoop3GraphPanelListener# zoomOnDisplayArea(java.awt.Rectangle,
     * java.awt.Rectangle, java.awt.Point, java.awt.Point)
     */
    @Override
    public void zoomOnDisplayArea(final Rectangle2D displayChartTotalArea, final Rectangle2D displayChartSelectArea,
	    final Point displayChartSelectAreaNewStartPoint, final Point displayChartSelectAreaNewEndPoint,
	    final boolean zoomOnGraph, final String sourceClass) {

	final HashMap<String, double[]> minMaxForVariables = computeMinMaxForVariables(displayChartTotalArea,
		displayChartSelectArea);

	dataViewController.zoomForVariables(minMaxForVariables, null, zoomOnGraph, sourceClass);

	if (displayChartSelectAreaNewStartPoint != null) {
	    dataViewController.fitToDataAllGraphs();
	}
    }

    /**
     * Zoom Out the JScoop3ChartPanel
     */
    public void zoomOut() {
	scoop3ChartPanel.getjScoop3ChartScrollPane().zoomOut();
    }

    /**
     * Set Zoom to the given rectangle for the JScoop3ChartPanel
     */
    public void zoomRect(final Rectangle2D zoomRect) {
	scoop3ChartPanel.getjScoop3ChartScrollPane().zoomToRectangle(zoomRect);
    }

    private void updateCheckboxState(final boolean propagate) {
	if (ABLE_TO_VALIDATE_PARAMETERS_IN_DATA_VIEW) {
	    final String parameterCode = selectedValue.split(DataViewController.SEPARATOR_FOR_COMBO_LABELS)[0].trim();
	    final String refParameterCode = selectedValue.split(DataViewController.SEPARATOR_FOR_COMBO_LABELS)[1]
		    .trim();

	    boolean isEnabled = refParameterCode.equals(referenceParameterCodeForThisObservation);
	    if (isEnabled) {
		final OceanicParameter param = dataViewController.getCommonViewModel().getObservation(observationNumber)
			.getOceanicParameter(parameterCode);
		if (param != null) {
		    isEnabled = param.getLinkParamType() != LINK_PARAM_TYPE.COMPUTED_CONTROL;
		}
	    }

	    validateParamCheckBox.setEnabled(isEnabled);
	    // Update the Checkbox state
	    validateParamCheckBox.setSelected(ValidatedDataParameterManager.getInstance().isValidated(parameterCode)
		    && refParameterCode.equals(referenceParameterCodeForThisObservation));

	    if (propagate) {
		dataViewController.updateJComboBoxes();
	    }
	}
    }

    public DataViewController getDataViewController() {
	return this.dataViewController;
    }

    public void setScoop3ChartPanelPopupMenu(final Scoop3ChartPanelPopupMenu scoop3ChartPanelPopupMenu) {
	this.scoop3ChartPanelPopupMenu = scoop3ChartPanelPopupMenu;
    }

    public JComboBox<Integer> getComboBox() {
	return parametersComboBox;
    }

    public String[] getComboBoxValues() {
	return comboBoxValues;
    }

    public void simulateAClickOnScrollbarToAvoidShifting() {
	// fix shift in graphs scrollbar after zoomed in and select another graph (FAE 49960)
	final boolean isDefaultView = ((this.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		.getDataAreaForZoomLevelOne().getWidth() == this.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.getDataAreaForZoomLevelCurrent().getWidth())
		&& (this.getScoop3ChartPanel().getjScoop3ChartScrollPane().getDataAreaForZoomLevelOne()
			.getHeight() == this.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				.getDataAreaForZoomLevelCurrent().getHeight()));
	if ((dataViewController.listGraphs.size() > 1) && !isDefaultView) {
	    Point oldMousePosition;
	    final int scrollBarMargin = 15;
	    if (referenceParameterCodeForThisObservation.equalsIgnoreCase("time")
		    || referenceParameterCodeForThisObservation.equalsIgnoreCase("year")) { // for timeseries and
											    // trajectories
		// save the old position of the mouse cursor
		oldMousePosition = MouseInfo.getPointerInfo().getLocation();
		// simulate a click on the horizontal scrollbar of another graph
		boolean graphChoosen = false;
		Robot r = null;
		try {
		    for (final ChartPanelWithComboBox c : dataViewController.getListGraphs()) {
			if (!graphChoosen && (c != this)) {
			    final JScrollBar horizontalScrollBar = c.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				    .getHorizontalScrollBar();
			    final BoundedRangeModel model = c.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				    .getHorizontalScrollBar().getModel();
			    final Point p = horizontalScrollBar.getLocationOnScreen();
			    r = new Robot();
			    final double ratioScrollbar = (double) model.getValue() / (double) model.getMaximum();
			    final double zoomFactor = c.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				    .getDataAreaForZoomLevelOne().getWidth()
				    / c.getScoop3ChartPanel().getjScoop3ChartScrollPane()
					    .getDataAreaForZoomLevelCurrent().getWidth();
			    r.mouseMove(
				    p.x + (int) Math.round((-2 * scrollBarMargin * ratioScrollbar)
					    + ((zoomFactor < 165) ? (1.2 * scrollBarMargin) : (scrollBarMargin)))
					    + (int) Math.round(ratioScrollbar * horizontalScrollBar.getWidth()),
				    p.y + (horizontalScrollBar.getHeight() / 2));
			    r.mousePress(InputEvent.BUTTON1_MASK);
			    Thread.sleep(10);
			    r.mouseRelease(InputEvent.BUTTON1_MASK);
			    graphChoosen = true;
			}
		    }
		    // move the mouse cursor to the saved position
		    if (r != null) {
			r.mouseMove(oldMousePosition.x, oldMousePosition.y);
		    }
		} catch (final Exception e2) {
		    e2.printStackTrace();
		}
	    } else { // for profiles
		// save the old position of the mouse cursor
		oldMousePosition = MouseInfo.getPointerInfo().getLocation();
		// simulate a click on the vertical scrollbar of another graph
		boolean graphChoosen = false;
		Robot r = null;
		try {
		    for (final ChartPanelWithComboBox c : dataViewController.getListGraphs()) {
			if (!graphChoosen && (c != this)) {
			    final JScrollBar verticalScrollBar = c.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				    .getVerticalScrollBar();
			    final BoundedRangeModel model = c.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				    .getVerticalScrollBar().getModel();
			    final Point p = verticalScrollBar.getLocationOnScreen();
			    r = new Robot();
			    final double ratioScrollbar = (double) model.getValue() / (double) model.getMaximum();
			    final double zoomFactor = c.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				    .getDataAreaForZoomLevelOne().getHeight()
				    / c.getScoop3ChartPanel().getjScoop3ChartScrollPane()
					    .getDataAreaForZoomLevelCurrent().getHeight();
			    r.mouseMove(p.x + (verticalScrollBar.getWidth() / 2), p.y
				    + (int) Math.round((-2 * scrollBarMargin * ratioScrollbar)
					    + (((zoomFactor < 165) && (ratioScrollbar < 0.9)) ? (1.2 * scrollBarMargin)
						    : (scrollBarMargin)))
				    + (int) Math.round(ratioScrollbar * verticalScrollBar.getHeight()));
			    r.mousePress(InputEvent.BUTTON1_MASK);
			    Thread.sleep(10);
			    r.mouseRelease(InputEvent.BUTTON1_MASK);
			    graphChoosen = true;
			}
		    }
		    // move the mouse cursor to the saved position
		    if (r != null) {
			r.mouseMove(oldMousePosition.x, oldMousePosition.y);
		    }
		} catch (final Exception e2) {
		    e2.printStackTrace();
		}
	    }
	}
    }

    public String getReferenceParameterCodeForThisObservation() {
	return referenceParameterCodeForThisObservation;
    }

    // update Timeserie sections label
    public void updateSectionLabel() {
	if (totalSectionLabel != null) {
	    this.totalSectionLabel.setText(String.valueOf(dataViewController.getTotalSectionNumberForTimeserie()));
	}
	if (currentSectionLabel != null) {
	    this.currentSectionLabel.setText(String.valueOf(dataViewController.getCurrentSectionNumberForTimeserie()));
	}
    }

}
