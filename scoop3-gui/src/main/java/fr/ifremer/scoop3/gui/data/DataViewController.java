package fr.ifremer.scoop3.gui.data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.bushe.swing.event.EventBus;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;

import com.bbn.openmap.omGraphics.OMGraphicList;

import fr.ifremer.scoop3.chart.model.ChartDataset;
import fr.ifremer.scoop3.chart.model.ChartMetaVariable;
import fr.ifremer.scoop3.chart.model.ChartPhysicalVariable;
import fr.ifremer.scoop3.chart.view.additionalGraphs.AdditionalGraph;
import fr.ifremer.scoop3.chart.view.panel.JScoop3ChartPanelAbstract;
import fr.ifremer.scoop3.chart.view.panel.JScoop3ChartPanelForProfile;
import fr.ifremer.scoop3.chart.view.panel.JScoop3ChartPanelForTimeSerie;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneForProfileOrdinateUp;
import fr.ifremer.scoop3.controller.worflow.StepCode;
import fr.ifremer.scoop3.data.SuperposedModeEnum;
import fr.ifremer.scoop3.events.GuiEventStepCompleted;
import fr.ifremer.scoop3.gui.common.CommonViewModel;
import fr.ifremer.scoop3.gui.common.DataOrReferenceViewController;
import fr.ifremer.scoop3.gui.common.MetadataSplitPane.InfoInObservationSubPanel;
import fr.ifremer.scoop3.gui.common.jdialog.DisplayObservationInfoForLevelDialog;
import fr.ifremer.scoop3.gui.common.jdialog.ReportJDialog;
import fr.ifremer.scoop3.gui.common.model.DisplayedQCEnum;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent.EVENT_ENUM;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyQCChangeEvent;
import fr.ifremer.scoop3.gui.data.commandButton.ChangeGraphsNumberCommandButton;
import fr.ifremer.scoop3.gui.data.commandButton.SelectAndChangeQCCommandButton;
import fr.ifremer.scoop3.gui.data.datatable.DataTableDialog;
import fr.ifremer.scoop3.gui.data.datatable.update.DataTableUpdateAbstract;
import fr.ifremer.scoop3.gui.data.popup.Scoop3ChartPanelChangeQCJMenuItem;
import fr.ifremer.scoop3.gui.jzy3dManager.Jzy3dManager.Type3d;
import fr.ifremer.scoop3.gui.utils.MapCommonFunctions;
import fr.ifremer.scoop3.infra.event.MapPropertyChangeEvent;
import fr.ifremer.scoop3.infra.event.MapPropertyChangeEvent.MAP_EVENT_ENUM;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.mail.UnhandledException;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.infra.tools.Conversions;
import fr.ifremer.scoop3.infra.undo_redo.UndoRedoAction;
import fr.ifremer.scoop3.infra.undo_redo.data.QCValueChange;
import fr.ifremer.scoop3.infra.undo_redo.metadata.MetadataValueChange;
import fr.ifremer.scoop3.infra.undo_redo.metadata.MetadataValueChange.QC_TO_UPDATE;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.Platform;
import fr.ifremer.scoop3.model.Profile;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.parameter.OceanicParameter;
import fr.ifremer.scoop3.model.parameter.Parameter;
import fr.ifremer.scoop3.model.parameter.Parameter.LINK_PARAM_TYPE;
import fr.ifremer.scoop3.model.parameter.ZParameter;
import javafx.geometry.Rectangle2D;

/**
 *
 *
 */
public class DataViewController extends DataOrReferenceViewController {

    /**
     * Separator for Combo Labels
     */
    public static final String SEPARATOR_FOR_COMBO_LABELS = "/";
    private static final Color NEAREST_PROFILES_FOR_ALL_PLATFORMS_COLOR = new Color(153, 51, 0); // Marron
    private static final Color NEAREST_PROFILES_FOR_CURRENT_PLATFORM_COLOR = new Color(189, 189, 189); // Gris clair

    private DisplayedQCEnum displayedQCEnum = DisplayedQCEnum.VALUES;
    private boolean forceUpdateChartPanel = false;
    protected ArrayList<ChartPanelWithComboBox> listGraphs;
    private final Map<String, double[]> keepBoundsMinAndMaxForVariables = new HashMap<>();

    private final ArrayList<String> paramOfDifferentGraphs = new ArrayList<>();
    private List<String> oldSelectedParameters = new ArrayList<>();
    private final ArrayList<Integer> removedIndex = new ArrayList<>();

    private static List<String> variableListComboBoxes = new ArrayList<>();
    private ChartPanelWithComboBox draggedGraph = null;

    private boolean askForValidation = false;
    private ArrayList<Double> fullLatitudeParameterValues = null;
    private ArrayList<Double> fullLongitudeParameterValues = null;
    private boolean timeserieDivided = false;
    private ChartDataset divideTimeserieChartDataset = null;

    public static final int MAX_INIT_POINT_NUMBER_FOR_TIMESERIE = 100;
    public static final int MAX_SECTION_NUMBER_FOR_TIMESERIE = 100;

    private Integer totalSectionNumberForTimeserie = null;
    private Integer currentSectionNumberForTimeserie = null;

    /**
     * Constructor
     *
     */
    public DataViewController(final DataViewImpl dataViewImpl, final DataViewModel dataViewModel,
	    final boolean isBPCVersion) {
	super(dataViewImpl, dataViewModel, isBPCVersion);

	createChangeGraphsNumberBandActionListener();

	// Default value
	dataViewImpl.setSuperposedModeEnum(SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET, true);
    }

    public void changeCurrentStation(final int[] newCurrentStation) {
	boolean observationChanged = false;
	if (getObservationNumber() != newCurrentStation[0]) {
	    observationChanged = true;
	    setObservationNumberAndSend(newCurrentStation[0]);
	    if (getCommonViewImpl().getMetadataSplitPane() != null) {
		getCommonViewImpl().getMetadataSplitPane().updateEnabledButtons();
	    }
	    setSelectedObservation(newCurrentStation[0]);
	    setSelectedObservationOnSpecificPanel(newCurrentStation[0]);
	}
	setCurrentLevel(newCurrentStation[1]);
	// FAE 26637
	if (JScoop3ChartScrollPaneAbstract.isDisplayCircle()) {
	    DisplayObservationInfoForLevelDialog.updateInfo(((DataViewImpl) getCommonViewImpl()).getScoop3Frame(),
		    getCommonViewModel().getObservation(getObservationNumber()), levelIndex,
		    ((DataViewModel) getCommonViewModel()).getParametersOrder());
	}
	if (observationChanged) {
	    // FAE 26637
	    // ((DataViewImpl)
	    // getCommonViewImpl()).updateDisplayOrHidePointsIcons(DataViewImpl.DISPLAY_POINTS_AND_CIRLCE);
	}
    }

    public void displayInfoPointDialog() {
	DisplayObservationInfoForLevelDialog.updateInfo(((DataViewImpl) getCommonViewImpl()).getScoop3Frame(),
		getCommonViewModel().getObservation(getObservationNumber()), levelIndex,
		((DataViewModel) getCommonViewModel()).getParametersOrder());
    }

    /**
     * Each graphs will be fit to visible data
     */
    public void fitToDataAllGraphs() {
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    chartPanelWithComboBox.fitToData();
	}
    }

    /**
     *
     * @param chartPanelWithComboBox
     * @return the LINK_PARAM_TYPE
     */
    public LINK_PARAM_TYPE getLinkParamTypeFor(final ChartPanelWithComboBox chartPanelWithComboBox) {
	final String variableName = chartPanelWithComboBox.getScoop3ChartPanel().getVariableNameToUpdate();
	final OceanicParameter variable = getCommonViewModel().getObservation(getObservationNumber())
		.getOceanicParameter(variableName);
	if (variable != null) {
	    return variable.getLinkParamType();
	}

	// variable == null
	return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewController#isSuperposedMode()
     */
    @Override
    public boolean isSuperposedMode() {
	return (getSuperposedModeEnum() != null)
		&& (getSuperposedModeEnum() != SuperposedModeEnum.CURRENT_OBSERVATION_ONLY);
    }

    @Override
    public void saveMapImage() {
	// Nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.data.DataViewController#selectionDoneWithOtherMouseMode(java.awt.Rectangle,
     * java.awt.Rectangle, java.awt.Point, java.awt.Point, fr.ifremer.scoop3.gui.data.ChartPanelWithComboBox)
     */
    public void selectionDoneWithOtherMouseMode(final Rectangle2D displayChartTotalArea,
	    final Rectangle2D displayChartSelectArea, final Point displayChartSelectAreaNewStartPoint,
	    final Point displayChartSelectAreaNewEndPoint, final ChartPanelWithComboBox chartPanelWithComboBox) {
	final Scoop3ChartPanelChangeQCJMenuItem sourceForChangeQC = new Scoop3ChartPanelChangeQCJMenuItem("",
		!(changeQCForAllStations()
			&& ((getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
				|| (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET))),
		QCValues.getQCValues(((DataViewImpl) getCommonViewImpl()).getMouseSubModeQC()), chartPanelWithComboBox,
		this);
	// block the QC change on PSAL/TEMP graph
	if (!(sourceForChangeQC.getChartPanelWithComboBox().getScoop3ChartPanel().getjScoop3ChartScrollPane()
		.getAbscissaPhysicalVar().getLabel().equals("PSAL")
		&& sourceForChangeQC.getChartPanelWithComboBox().getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.getOrdinatePhysicalVar().getLabel().equals("TEMP"))) {
	    if (isBPCVersion() && sourceForChangeQC.getQcToSet().equals(QCValues.QC_9)) {
		final int answer = JOptionPane.showConfirmDialog(null,
			Messages.getMessage("bpc-gui.confirm-qc-9.message"),
			Messages.getMessage("bpc-gui.confirm-qc-9.title"), JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
		    propertyChangeChangeQC(new SC3PropertyChangeEvent(sourceForChangeQC, EVENT_ENUM.CHANGE_QC));
		    // update graphique de tous les graphiques
		    for (final ChartPanelWithComboBox chartPanelWithComboBox1 : listGraphs) {
			chartPanelWithComboBox1.getScoop3ChartPanel().getjScoop3ChartScrollPane().repaint();
		    }
		} else {
		    removeSelectionBox();
		}
	    } else {
		propertyChangeChangeQC(new SC3PropertyChangeEvent(sourceForChangeQC, EVENT_ENUM.CHANGE_QC));
		// update graphique de tous les graphiques
		for (final ChartPanelWithComboBox chartPanelWithComboBox1 : listGraphs) {
		    chartPanelWithComboBox1.getScoop3ChartPanel().getjScoop3ChartScrollPane().repaint();
		}
	    }
	} else {
	    removeSelectionBox();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.data.DataViewController#selectionDoneWithRemovalMode(java.awt.Rectangle,
     * java.awt.Rectangle, java.awt.Point, java.awt.Point, fr.ifremer.scoop3.gui.data.ChartPanelWithComboBox)
     */
    public void selectionDoneWithRemovalMode(final Rectangle2D displayChartTotalArea,
	    final Rectangle2D displayChartSelectArea, final Point displayChartSelectAreaNewStartPoint,
	    final Point displayChartSelectAreaNewEndPoint, final ChartPanelWithComboBox chartPanelWithComboBox) {
	// use the same method than flag to get points user selected
	final Scoop3ChartPanelChangeQCJMenuItem sourceForChangeQC = new Scoop3ChartPanelChangeQCJMenuItem("",
		!(changeQCForAllStations()
			&& ((getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
				|| (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET))),
		QCValues.QC_9, chartPanelWithComboBox, this);
	// remove those points instead of flag them like in selectionDoneWithOtherMouseMode() method
	propertyChangeRemoveMeasure(new SC3PropertyChangeEvent(sourceForChangeQC, EVENT_ENUM.REMOVE_MEASURE));
	// update graphique de tous les graphiques
	for (final ChartPanelWithComboBox chartPanelWithComboBox1 : listGraphs) {
	    chartPanelWithComboBox1.getScoop3ChartPanel().getjScoop3ChartScrollPane().repaint();
	}
    }

    /**
     * Set the new Observation number and send
     *
     * @param observationNum
     */
    @Override
    public void setObservationNumberAndSend(final int observationNum) {
	final Platform prevPlatform = getCommonViewModel().getPlatformForObservation(getObservationNumber());
	super.setObservationNumberAndSend(observationNum);
	final Platform curPlatform = getCommonViewModel().getPlatformForObservation(getObservationNumber());

	if ((prevPlatform != curPlatform) && isSuperposedMode()) {
	    // Superposed mode is on ... but the Platform changed => compute again the image ...
	    firstObservationIndex = -1;
	    lastObservationIndex = -1;
	    setSuperposedModeEnum(null);
	    propertyChangeAllProfiles();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#setSelectedObservationOnSpecificPanel(int)
     */
    @Override
    public void setSelectedObservationOnSpecificPanel(final int observationIndex) {
	super.setSelectedObservationOnSpecificPanel(observationIndex);
	final int levelMax = getCommonViewModel().getObservation(observationIndex).getReferenceParameter().getValues()
		.size();
	if (levelMax <= levelIndex) {
	    levelIndex = 0;
	}
	setCurrentLevel(levelIndex);

	// // AFTER updateChartPanel()
	// if ((keepBounds_minAndMaxForVariables != null) && !keepBounds_minAndMaxForVariables.isEmpty()) {
	// zoomForVariables(keepBounds_minAndMaxForVariables, null);
	// }
    }

    public void updateChartDatasetForVariable(final String parameterCode, final Integer observationIndex) {
	ChartPhysicalVariable physicalVariable;
	if (isTimeserieDivided()) {
	    physicalVariable = divideTimeserieChartDataset.getPhysicalVariable(parameterCode);
	} else {
	    physicalVariable = ((DataViewModel) getCommonViewModel()).getChartDataset()
		    .getPhysicalVariable(parameterCode);
	}
	ArrayList<Parameter<? extends Number>> parameterList;
	if (!isSuperposedMode()) {
	    parameterList = getCommonViewModel().getParam(getCommonViewModel().getObservation(
		    observationIndex != null ? observationIndex : getObservationNumber()), parameterCode);
	    for (final Parameter<? extends Number> parameter : parameterList) {
		if ((physicalVariable != null) && (parameter != null) && (physicalVariable.getQcValuesByStation()
			.get(observationIndex != null ? observationIndex : getObservationNumber()).length == parameter
				.getQcValues().size())) {
		    physicalVariable.updateQCs(observationIndex != null ? observationIndex : getObservationNumber(),
			    QCValues.convertQCValuesListToCharArray(parameter.getQcValues()));
		}
	    }
	} else {
	    parameterList = getCommonViewModel().getParams(getCommonViewModel().getObservations(), parameterCode);
	    for (int i = 0; i < getCommonViewModel().getObservations().size(); i++) {
		final Parameter<? extends Number> parameter = parameterList.get(i);
		int[] parameterQcValues = null;
		if (parameter != null) {
		    parameterQcValues = new int[parameter.getQcValues().size()];
		    for (int index = 0; index < parameter.getQcValues().size(); index++) {
			if (parameter.getQcValues().get(index) != null) {
			    parameterQcValues[index] = parameter.getQcValues().get(index).getQCValue();
			} else {
			    parameterQcValues[index] = 9;
			}
		    }
		}
		try {
		    if ((physicalVariable != null) && (parameter != null) && (parameterQcValues != null)
			    && (physicalVariable.getQcValuesByStation().get(i).length == parameter.getQcValues().size())
			    && !Arrays.equals(parameterQcValues, physicalVariable.getQcValuesByStation().get(i))) {
			physicalVariable.updateQCs(i, QCValues.convertQCValuesListToCharArray(parameter.getQcValues()));
		    }
		} catch (final IndexOutOfBoundsException e) {
		    // may be useless after the realization of the mantis 50586
		    final UnhandledException exception = new UnhandledException(
			    "Nombre d'observations dans le dataset : " + getCommonViewModel().getObservations().size()
				    + " / paramètre : " + physicalVariable.getLabel()
				    + " / taille de QcValuesByStation pour le paramètre : "
				    + physicalVariable.getQcValuesByStation().size() + " / index i : " + i,
			    e);
		}
	    }
	}
    }

    /**
     * Update a ChartPanelWithComboBox with the selected Parameter
     *
     * @param chartPanelWithComboBox
     * @param backupOldAbscissaRange
     * @param forceDisplayAdditionalSeries
     * @param nearestProfiles
     *            list of nearest profiles
     */
    @Override
    public void updateChartPanelWithComboBox(final ChartPanelWithComboBox chartPanelWithComboBox,
	    final boolean backupOldAbscissaRange, final boolean forceDisplayAdditionalSeries,
	    final List<Profile> nearestProfiles) {

	if ((this.nearestProfiles == null) && (nearestProfiles == null)) {
	    this.nearestProfiles = computeNearestProfiles();
	} else if ((this.nearestProfiles == null) && (nearestProfiles != null)) {
	    this.nearestProfiles = nearestProfiles;
	}

	final String selectedValue = chartPanelWithComboBox.getSelectedValue();

	final String absis = selectedValue.split(SEPARATOR_FOR_COMBO_LABELS)[1].trim();
	final String ord = selectedValue.split(SEPARATOR_FOR_COMBO_LABELS)[0].trim();

	// update the absis variable to update QCs before draw the chart
	updateChartDatasetForVariable(absis, null);

	// Retrieve the reference parameter
	final ChartPhysicalVariable referencePhysicalVariable = ((DataViewModel) getCommonViewModel()).getChartDataset()
		.getPhysicalVariable(absis);

	// update the ord variable to update QCs before draw the chart
	updateChartDatasetForVariable(ord, null);

	// Retrieve the physical parameter
	ChartPhysicalVariable physicalVariable = ((DataViewModel) getCommonViewModel()).getChartDataset()
		.getPhysicalVariable(ord);

	// if physicalVariable is null, take the first param in the chartDataset (to avoid nullPointerException)
	if (physicalVariable == null) {
	    physicalVariable = ((DataViewModel) getCommonViewModel()).getChartDataset().getPhysicalVariable(0);
	}

	if (backupOldAbscissaRange) {
	    // Get the currentAbscissa Range
	    double currentMinAbscissaPhysVal = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .getMinAbscissaPhysValBeforeCoefX();
	    if (currentMinAbscissaPhysVal == Double.MIN_VALUE) {
		currentMinAbscissaPhysVal = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.getMinAbscissaPhysVal();
	    }
	    double currentMaxAbscissaPhysVal = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .getMaxAbscissaPhysValBeforeCoefX();
	    if (currentMaxAbscissaPhysVal == Double.MAX_VALUE) {
		currentMaxAbscissaPhysVal = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.getMaxAbscissaPhysVal();
	    }
	    JScoop3ChartScrollPaneAbstract.setLastMinAndMaxAbscissaPhysVal(currentMinAbscissaPhysVal,
		    currentMaxAbscissaPhysVal);
	}

	checkFirstAndLastObservationIndex();

	// Create the Chart
	final JScoop3ChartPanelAbstract scoop3ChartPanel;
	if (getCommonViewModel().doesDatasetContainProfiles()) {
	    scoop3ChartPanel = new JScoop3ChartPanelForProfile(physicalVariable, referencePhysicalVariable,
		    ((getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
			    || (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET))
				    ? firstObservationIndex
				    : getObservationNumber(),
		    ((getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
			    || (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET))
				    ? lastObservationIndex
				    : getObservationNumber());
	} else {
	    scoop3ChartPanel = new JScoop3ChartPanelForTimeSerie(physicalVariable, referencePhysicalVariable,
		    ((getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
			    || (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET))
				    ? firstObservationIndex
				    : getObservationNumber(),
		    ((getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
			    || (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET))
				    ? lastObservationIndex
				    : getObservationNumber(),
		    getObservationNumber(), timeserieDivided);

	    switch (displayedQCEnum) {
	    case DATE_QC:
		final int[] timeQCs = getTimeQCs();
		((JScoop3ChartPanelForTimeSerie) scoop3ChartPanel).setQCToUse(Observation.TIME_VAR_NAME, timeQCs);
		break;
	    case POSITION_QC:
		final int[] posQCs = getPositionQCs();
		((JScoop3ChartPanelForTimeSerie) scoop3ChartPanel).setQCToUse(Observation.LATITUDE_VAR_NAME, posQCs);
		break;
	    case VALUES:
		((JScoop3ChartPanelForTimeSerie) scoop3ChartPanel).setQCToUse(null, null);
		break;
	    }
	}
	scoop3ChartPanel.getjScoop3ChartScrollPane().setCurrentStation(getObservationNumber());
	scoop3ChartPanel.getjScoop3ChartScrollPane().setCurrentLevel(levelIndex);

	// Update the Chart in the Panel
	// chartPanelWithComboBox.updateJScoop3ChartPanel(scoop3ChartPanel,
	// getCommonViewModel().getObservation(getObservationNumber()).getReferenceParameter().getCode());
	chartPanelWithComboBox.updateJScoop3ChartPanel(scoop3ChartPanel, absis);

	updateAdditionalGraphsToDisplay(chartPanelWithComboBox, forceDisplayAdditionalSeries, this.nearestProfiles);

	if (listGraphs.size() > 1) {
	    // Try to set the JScrollPane as an other graph
	    final ChartPanelWithComboBox otherChartPanelWithComboBox = listGraphs.get(0) != chartPanelWithComboBox
		    ? listGraphs.get(0)
		    : listGraphs.get(1);

	    final JScoop3ChartScrollPaneAbstract otherJScoop3ChartScrollPane = otherChartPanelWithComboBox
		    .getScoop3ChartPanel().getjScoop3ChartScrollPane();

	    otherJScoop3ChartScrollPane.updateDataAreaForZoomLevelCurrent();

	    otherChartPanelWithComboBox.zoomOnDisplayArea(otherJScoop3ChartScrollPane.getDataAreaForZoomLevelOne(),
		    otherJScoop3ChartScrollPane.getDataAreaForZoomLevelCurrent(), null, null, true, null);

	    final int newValue = otherJScoop3ChartScrollPane.getReferenceScrollBar().getValue();
	    final int newExtent = otherJScoop3ChartScrollPane.getReferenceScrollBar().getModel().getExtent();
	    final int newMin = otherJScoop3ChartScrollPane.getReferenceScrollBar().getMinimum();
	    final int newMax = otherJScoop3ChartScrollPane.getReferenceScrollBar().getMaximum();
	    scoop3ChartPanel.getjScoop3ChartScrollPane().getReferenceScrollBar().setValues(newValue, newExtent, newMin,
		    newMax);
	    scoop3ChartPanel.getjScoop3ChartScrollPane().getReferenceScrollBar().setValue(newValue);
	    scoop3ChartPanel.getjScoop3ChartScrollPane().validate();
	    scoop3ChartPanel.getjScoop3ChartScrollPane().repaint();
	}

	updateMouseCursor();
    }

    public void updateJComboBoxes() {
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    chartPanelWithComboBox.updateJComboBox();
	}
    }

    /**
     * Call zoomForVariables on each ChartPanelWithComboBox which is != to oriChartPanelWithComboBox
     *
     * @param minMaxForVariables
     * @param oriChartPanelWithComboBox
     */
    public void zoomForVariables(final Map<String, double[]> minMaxForVariables,
	    final ChartPanelWithComboBox oriChartPanelWithComboBox, final boolean zoomOnGraph,
	    final String sourceClass) {

	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    if (chartPanelWithComboBox != oriChartPanelWithComboBox) {

		// algorithm that define id the horizontal scrollbar of TEMP/PRES graph has to be reversed or not
		boolean reverseHorizontalScrollBar = false;
		if ((((getVariableListComboBoxes().contains("TEMP / PRES")
			|| getVariableListComboBoxes().contains("TEMP / DEPH"))
			&& getVariableListComboBoxes().contains("PSAL / TEMP"))
			|| ((getVariableListComboBoxes().contains("TEMP_ADJUSTED / PRES")
				|| getVariableListComboBoxes().contains("TEMP_ADJUSTED / DEPH"))
				&& getVariableListComboBoxes().contains("PSAL_ADJUSTED / TEMP_ADJUSTED")))
			&& ((chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				.getAbscissaPhysicalVar().getLabel().equals("TEMP")
				|| (chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
					.getAbscissaPhysicalVar().getLabel().equals("TEMP_ADJUSTED")))
				&& (chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
					.getOrdinatePhysicalVar().getLabel().equals("PRES")
					|| chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
						.getOrdinatePhysicalVar().getLabel().equals("DEPH")))) {
		    for (final ChartPanelWithComboBox chartPanelWithComboBox1 : listGraphs) {
			if (chartPanelWithComboBox1.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				.getVerticalScrollBar().getValueIsAdjusting()
				&& ((chartPanelWithComboBox1.getScoop3ChartPanel().getjScoop3ChartScrollPane()
					.getAbscissaPhysicalVar().getLabel().equals("PSAL")
					&& chartPanelWithComboBox1.getScoop3ChartPanel().getjScoop3ChartScrollPane()
						.getOrdinatePhysicalVar().getLabel().equals("TEMP"))
					|| (chartPanelWithComboBox1.getScoop3ChartPanel().getjScoop3ChartScrollPane()
						.getAbscissaPhysicalVar().getLabel().equals("PSAL_ADJUSTED")
						&& chartPanelWithComboBox1.getScoop3ChartPanel()
							.getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getLabel()
							.equals("TEMP_ADJUSTED")))) {
			    draggedGraph = chartPanelWithComboBox1;
			    reverseHorizontalScrollBar = true;
			} else if ((chartPanelWithComboBox1.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				.getVerticalScrollBar().getValue() == 0)
				&& (chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
					.getHorizontalScrollBar().getValue() == 0)
				&& (sourceClass != null)
				&& sourceClass.equals(JScoop3ChartScrollPaneForProfileOrdinateUp.class.getName())
				&& ((chartPanelWithComboBox1.getScoop3ChartPanel().getjScoop3ChartScrollPane()
					.getAbscissaPhysicalVar().getLabel().equals("PSAL")
					&& chartPanelWithComboBox1.getScoop3ChartPanel().getjScoop3ChartScrollPane()
						.getOrdinatePhysicalVar().getLabel().equals("TEMP"))
					|| (chartPanelWithComboBox1.getScoop3ChartPanel().getjScoop3ChartScrollPane()
						.getAbscissaPhysicalVar().getLabel().equals("PSAL_ADJUSTED")
						&& chartPanelWithComboBox1.getScoop3ChartPanel()
							.getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getLabel()
							.equals("TEMP_ADJUSTED")))) {
			    reverseHorizontalScrollBar = true;
			} else {
			    if ((draggedGraph != null) && (draggedGraph == chartPanelWithComboBox1)) {
				reverseHorizontalScrollBar = true;
				draggedGraph = null;
			    }
			}
		    }
		}

		chartPanelWithComboBox.zoomForVariables(minMaxForVariables, zoomOnGraph, reverseHorizontalScrollBar,
			getPrecisionZoomOn()/* , getObservationNumber() */);
	    }
	}
    }

    private void checkFirstAndLastObservationIndex() {
	// Display all observations (darker) + current observation
	if ((getSuperposedModeEnum() != null)
		&& ((getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
			|| (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET))) {
	    // Retrieve the reference parameter
	    final String reference = ((DataViewModel) getCommonViewModel()).getChartDataset().getReferenceLabel();
	    final ChartPhysicalVariable referencePhysicalVariable = ((DataViewModel) getCommonViewModel())
		    .getChartDataset().getPhysicalVariable(reference);

	    firstObservationIndex = referencePhysicalVariable.getPhysicalValuesByStation().size() - 1;
	    lastObservationIndex = 0;
	    final String currentPlatformCode = referencePhysicalVariable.getPlatformsCodes()
		    .get(getObservationNumber());
	    for (int index = 0; index < referencePhysicalVariable.getPhysicalValuesByStation().size(); index++) {
		final String indexPlatformCode = referencePhysicalVariable.getPlatformsCodes().get(index);
		if ((getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET)
			|| ((getSuperposedModeEnum() != SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET)
				&& currentPlatformCode.equals(indexPlatformCode))) {
		    if (firstObservationIndex > index) {
			firstObservationIndex = index;
		    }
		    if (lastObservationIndex < index) {
			lastObservationIndex = index;
		    }
		}
	    }
	    if (firstObservationIndex > lastObservationIndex /* || (firstObservationIndex == lastObservationIndex) */) {
		firstObservationIndex = -1;
		lastObservationIndex = -1;
	    }
	}
    }

    /**
     * @param addInRedoable
     */
    protected void propertyChangeUndo(final boolean addInRedoable) {
	final List<? extends UndoRedoAction> undoRedoActions = getCommonViewModel().undoLastChanges(addInRedoable);

	updateDatatableIfNeeded(undoRedoActions);

	if ((undoRedoActions != null) && !undoRedoActions.isEmpty()) {
	    final Map<String, List<QCValueChange>> updatesPerVariables = new HashMap<>();
	    for (final UndoRedoAction undoRedoAction : undoRedoActions) {
		if (undoRedoAction instanceof QCValueChange) {
		    final QCValueChange qcChanged = (QCValueChange) undoRedoAction;
		    if (removingMeasure) {
			qcChanged.setNewQC(qcChanged.getOldQC());
			qcChanged.setOldParameterValue(qcChanged.getOldParameterValue());

			if (!qcChanged.getParameterName().equals(getCommonViewModel()
				.getObservation(getObservationNumber()).getReferenceParameter().getCode())) {
			    getCommonViewModel().getObservation(getObservationNumber())
				    .getOceanicParameter(qcChanged.getParameterName()).getValues()
				    .set(qcChanged.getObservationLevel(), Double
					    .parseDouble(Double.toString((double) qcChanged.getOldParameterValue())));
			} else {
			    ((ZParameter) getCommonViewModel().getObservation(getObservationNumber())
				    .getReferenceParameter()).getValues().set(qcChanged.getObservationLevel(),
					    Double.parseDouble(
						    Double.toString((double) qcChanged.getOldParameterValue())));
			}

			for (final ChartPanelWithComboBox chartPanelWithComboBox2 : listGraphs) {
			    if (chartPanelWithComboBox2.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				    .getAbscissaPhysicalVar().getLabel().equals(qcChanged.getParameterName())) {
				// abscissa
				chartPanelWithComboBox2.getScoop3ChartPanel().getjScoop3ChartScrollPane()
					.getAbscissaPhysicalVar().getPhysicalValuesByStation()
					.get(getObservationNumber())[qcChanged
						.getObservationLevel()] = (double) qcChanged.getOldParameterValue();
			    } else if (chartPanelWithComboBox2.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				    .getAbscissaPhysicalVar().getLabel().equals("measure_number")
				    && qcChanged.getParameterName()
					    .equals(getCommonViewModel().getObservation(getObservationNumber())
						    .getReferenceParameter().getCode())) {
				// ordinate
				chartPanelWithComboBox2.getScoop3ChartPanel().getjScoop3ChartScrollPane()
					.getOrdinatePhysicalVar().getPhysicalValuesByStation()
					.get(getObservationNumber())[qcChanged
						.getObservationLevel()] = (double) qcChanged.getOldParameterValue();
				break;
			    }
			}
		    }
		    if (qcChanged.getParameterName() != null) {
			if (!updatesPerVariables.containsKey(qcChanged.getParameterName())) {
			    updatesPerVariables.put(qcChanged.getParameterName(), new ArrayList<>());
			}
			final List<QCValueChange> changedForVariable = updatesPerVariables
				.get(qcChanged.getParameterName());
			changedForVariable.add(qcChanged);
		    }
		}
	    }

	    for (final String variableName : updatesPerVariables.keySet()) {
		for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
		    final String variableName2 = chartPanelWithComboBox.getScoop3ChartPanel().getVariableNameToUpdate();
		    if (variableName.equals(variableName2)) {
			chartPanelWithComboBox.revertQC(updatesPerVariables.get(variableName));
			chartPanelWithComboBox.repaintChart();
		    }
		}
	    }
	}
	getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		getCommonViewModel().isListOfRedoableChangesEmpty());
    }

    /**
     * to override if needed.
     *
     * @param parametersCombo
     * @param parametersWithUnitCombo
     * @param parametersNameWithUnit
     */
    protected void addSpecificValuesInParametersCombo(final ArrayList<String> parametersCombo,
	    final ArrayList<String> parametersWithUnitCombo, final Map<String, String> parametersNameWithUnit) {
	// empty method
    }

    /**
     * @return TRUE if the ChangeQC event is to change QC only for the current station.
     */
    protected boolean changeQCForAllStations() {
	return false;
    }

    public void createChangeGraphsNumberBandActionListener() {
	for (final ChangeGraphsNumberCommandButton changeGraphs : ((DataViewImpl) getCommonViewImpl())
		.getChangeGraphs()) {
	    changeGraphs.getChangeGraphsNumber().addActionListener((final ActionEvent e) -> {
		if (getCommonViewModel().doesDatasetContainProfiles()) {
		    DataViewImpl.profileGraphsCount = changeGraphs.getRowNb() * changeGraphs.getColumnNb();
		    DataViewImpl.profileGraphsCountGroup = changeGraphs.getColumnNb();
		} else {
		    DataViewImpl.timeserieGraphsCount = changeGraphs.getRowNb() * changeGraphs.getColumnNb();
		    DataViewImpl.timeserieGraphsCountGroup = changeGraphs.getColumnNb();
		}
		getDataViewImpl().setNbMaxGraphByGroup(changeGraphs.getColumnNb());

		setForceUpdateChartPanel(true);
		updateChartPanel((getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET)
			|| (getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET));

		updateMouseCursor();
	    });

	    // This is not allowed to have more than 2 lines for the Profiles
	    if (getCommonViewModel().doesDatasetContainProfiles() && (changeGraphs.getRowNb() > 2)) {
		changeGraphs.getChangeGraphsNumber().setEnabled(false);
	    }

	    // Change icon if needed ...
	    if (!changeGraphs.getChangeGraphsNumber().isEnabled()) {
		final URL resource = getClass().getClassLoader().getResource("icons/editor_grid_view_block_disabled_"
			+ changeGraphs.getRowNb() + "x" + changeGraphs.getColumnNb() + ".png");
		changeGraphs.getChangeGraphsNumber()
			.setIcon(ImageWrapperResizableIcon.getIcon(resource, new Dimension(32, 32)));
	    }
	}
    }

    /**
     * By default, the jComboBox do NOT display the Units of the Parameter.
     *
     * To override if needed.
     *
     * @return
     */
    protected boolean displayUnitsInCombo() {
	return false;
    }

    protected List<AdditionalGraph> getAdditionalGraphsToDisplayClimato(
	    final ChartPanelWithComboBox chartPanelWithComboBox, final boolean forceDisplayAdditionalSeries,
	    final Double maxDepthForOtherProfiles) {
	return null;
    }

    /**
     * return the nearest profiles list.
     *
     * @return a profiles list
     */
    protected List<Profile> computeNearestProfiles() {
	List<Profile> nearestProfiles = null;

	if (getSuperposedModeEnum() != null) {
	    OMGraphicList omList = null;
	    switch (getSuperposedModeEnum()) {
	    case CURRENT_OBSERVATION_ONLY:
		break;
	    case NEAREST_PROFILES_FOR_ALL_PLATFORMS:
		nearestProfiles = ((DataViewModel) getCommonViewModel())
			.getNearestProfilesForAllPlatforms(getObservationNumber());
		if (nearestProfiles != null) {
		    omList = MapCommonFunctions.createOMListWithNearestProfiles(
			    getCommonViewModel().getObservation(getObservationNumber()), nearestProfiles,
			    NEAREST_PROFILES_FOR_ALL_PLATFORMS_COLOR,
			    getCommonViewModel().getDataset().getDatasetType().toString());
		}
		break;
	    case NEAREST_PROFILES_FOR_CURRENT_PLATFORM:
		nearestProfiles = ((DataViewModel) getCommonViewModel())
			.getNearestProfilesForCurrentPlatform(getObservationNumber());
		if (nearestProfiles != null) {
		    omList = MapCommonFunctions.createOMListWithNearestProfiles(
			    getCommonViewModel().getObservation(getObservationNumber()), nearestProfiles,
			    NEAREST_PROFILES_FOR_CURRENT_PLATFORM_COLOR,
			    getCommonViewModel().getDataset().getDatasetType().toString());
		}
		break;
	    case PROFILES_FOR_PLATFORM_FROM_DATASET:
		// omList = MapCommonFunctions.createOMListFromDataset(getCommonViewModel().getDataset());
		final Observation currentObservation = getCommonViewModel().getObservation(getObservationNumber());
		omList = MapCommonFunctions.createOMListFromObservations(getCommonViewModel().getDataset(),
			currentObservation.getSensor().getPlatform().getAllObservations());
		break;
	    case ALL_OBSERVATIONS_FROM_DATASET:
		omList = MapCommonFunctions.createOMListFromDataset(getCommonViewModel().getDataset());
		break;
	    }
	    updateMouseCursor();

	    /**
	     * Update the Map
	     */
	    if (omList != null) {
		getScoop3MapPanel().getObservationLayer().init(omList);
		getScoop3MapPanel().setMapSelectedElement(MapCommonFunctions.getPointModelForObs(
			getCommonViewModel().getObservation(getObservationNumber()), levelIndex,
			getCommonViewModel().getDataset().getDatasetType().toString(), true));
	    }
	}
	return nearestProfiles;
    }

    /**
     * Get additional Stations from the Database
     *
     * @param listGraphs
     * @param forceDisplayAdditionalSeries
     * @return
     */
    protected List<AdditionalGraph> getAdditionalGraphsToDisplayStationType(
	    final ChartPanelWithComboBox chartPanelWithComboBox, final boolean forceDisplayAdditionalSeries,
	    final List<Profile> nearestProfiles) {

	List<AdditionalGraph> additionalGraphs = null;
	if (nearestProfiles != null) {
	    additionalGraphs = new ArrayList<>();
	    ZParameter refParam = null;
	    OceanicParameter realRefParam = null;

	    for (final Profile profile : nearestProfiles) {
		final OceanicParameter param = profile.getOceanicParameter(chartPanelWithComboBox.getScoop3ChartPanel()
			.getjScoop3ChartScrollPane().getAbscissaPhysicalVar().getLabel());
		if (chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane().getOrdinatePhysicalVar()
			.getLabel().equals("TEMP")
			|| chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				.getOrdinatePhysicalVar().getLabel().equals("TEMP_ADJUSTED")) {
		    realRefParam = profile.getOceanicParameter(chartPanelWithComboBox.getScoop3ChartPanel()
			    .getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getLabel());
		} else {
		    refParam = (ZParameter) profile.getReferenceParameter();
		}
		if ((param != null) && ((refParam != null) || (realRefParam != null))) {
		    /*
		     * DO NOT KEEP QC_9
		     */
		    for (int index = param.getQcValues().size() - 1; index >= 0; index--) {
			final QCValues qcValue = param.getQcValues().get(index);
			if (qcValue == QCValues.QC_9) {
			    try {
				param.getValues().remove(index);
				if (!removedIndex.contains(index)) {
				    if (refParam != null) {
					refParam.getValues().remove(index);
				    } else {
					realRefParam.getValues().remove(index);
				    }
				    removedIndex.add(index);
				}
				param.getQcValues().remove(index);
			    } catch (final IndexOutOfBoundsException e) {
				SC3Logger.LOGGER
					.debug("IndexOutOfBoundsException du à un QC_9 sur le paramètre concerné : "
						+ param + " => index : " + index + " / "
						+ (refParam != null ? refParam.getValues().size() : null)
						+ " qcValue = " + param.getQcValues().get(index));
			    }
			}
		    }
		    /*
		     * Create the AdditionalGraph
		     */
		    if (refParam != null) {
			final Double[] valuesDoubleArray = param.getValues()
				.toArray(new Double[param.getValues().size()]);
			final Double[] refValuesDoubleArray = refParam.getValues()
				.toArray(new Double[refParam.getValues().size()]);
			final AdditionalGraph additionalGraph = new AdditionalGraph(valuesDoubleArray,
				refValuesDoubleArray, getIntArrayFromListOfQCValues(param.getQcValues()));
			additionalGraphs.add(additionalGraph);
		    } else {
			final AdditionalGraph additionalGraph = new AdditionalGraph(
				param.getValues().toArray(new Double[param.getValues().size()]), /////////////////////////////////////////////
				realRefParam.getValues().toArray(new Double[realRefParam.getValues().size()]),
				getIntArrayFromListOfQCValues(param.getQcValues()));
			additionalGraphs.add(additionalGraph);
		    }
		}
	    }
	}

	return additionalGraphs;
    }

    /**
     * @return controlled data view
     *
     */
    protected DataViewImpl getDataViewImpl() {
	return (DataViewImpl) super.getCommonViewImpl();
    }

    /**
     * Convert a List of Double (Object) to an array of float (primitive)
     *
     * @param listOfDouble
     * @return
     */
    protected float[] getFloatArrayFromListOfDouble(final List<Double> listOfDouble) {
	final float[] floatArray = new float[listOfDouble.size()];
	int i = 0;

	for (final Double d : listOfDouble) {
	    floatArray[i++] = d != null ? d.floatValue() : Float.NaN; // Or whatever default you want.
	}
	return floatArray;
    }

    /**
     * Convert a List of Float (Object) to an array of float (primitive)
     *
     * @param listOfFloat
     * @return
     */
    protected float[] getFloatArrayFromListOfFloat(final List<Float> listOfFloat) {
	final float[] floatArray = new float[listOfFloat.size()];
	int i = 0;

	for (final Float f : listOfFloat) {
	    floatArray[i++] = f != null ? f : Float.NaN; // Or whatever default you want.
	}
	return floatArray;
    }

    /**
     * Convert a List of Float (Object) to an array of Double (Object)
     *
     * @param listOfFloat
     * @return
     */
    protected Double[] getDoubleArrayFromListOfFloat(final List<Float> listOfFloat) {
	final Double[] doubleArray = new Double[listOfFloat.size()];
	int i = 0;

	for (final Float f : listOfFloat) {
	    doubleArray[i++] = f != null ? f : Double.NaN; // Or whatever default you want.
	}
	return doubleArray;
    }

    /**
     * Convert a List of Long (Object) to an array of long (primitive)
     *
     * @param listOfLong
     * @return
     */
    protected long[] getLongArrayFromListOfLong(final List<Long> listOfLong) {
	final long[] longArray = new long[listOfLong.size()];
	int i = 0;

	for (final Long l : listOfLong) {
	    longArray[i++] = l != null ? l : Long.MIN_VALUE; // Or whatever default you want.
	}
	return longArray;
    }

    /**
     * Convert a List of QCValues to an array of int (primitive)
     *
     * @param listOfFloat
     * @return
     */
    protected int[] getIntArrayFromListOfQCValues(final List<QCValues> qcValues) {
	final int[] intArray = new int[qcValues.size()];
	int i = 0;

	for (final QCValues qcValue : qcValues) {
	    intArray[i++] = qcValue.getQCValue();
	}
	return intArray;
    }

    /**
     * @return the QCs for the Position to display
     */
    protected int[] getPositionQCs() {
	return null;
    }

    /**
     * @return the QCs for the Time to display
     */
    protected int[] getTimeQCs() {
	return QCValues.convertQCValuesListToIntArray(
		getCommonViewModel().getObservation(getObservationNumber()).getTime().getQcValues());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewController#observationNumberChanged(int, int)
     */
    @Override
    protected void observationNumberChanged(final int oldObservationIndex, final int newObservationIndex) {
	((DataViewImpl) getCommonViewImpl()).observationNumberChanged(oldObservationIndex, newObservationIndex);

	if (keepBounds && (listGraphs != null) && !listGraphs.isEmpty() && keepBoundsMinAndMaxForVariables.isEmpty()) {
	    keepBoundsComputeVisibleMinAndVisibleMaxForVariables();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_AllProfiles()
     */
    @Override
    protected void propertyChangeAllProfiles() {
	JScoop3ChartScrollPaneAbstract.setCoefX(0);
	if (!isSuperposedMode() && (null != ((DataViewImpl) getCommonViewImpl()).getSuperposedModeJComboBox())
		&& !((DataViewImpl) getCommonViewImpl()).getSuperposedModeJComboBox().getSelectedItem()
			.equals(SuperposedModeEnum.CURRENT_OBSERVATION_ONLY)) {
	    setSuperposedModeEnum(
		    ((DataViewImpl) getCommonViewImpl()).getSuperposedModeJComboBox().getSuperposedMode());
	} else if (!isSuperposedMode() && (null != ((DataViewImpl) getCommonViewImpl()).getSuperposedModeJComboBox())
		&& ((DataViewImpl) getCommonViewImpl()).getSuperposedModeJComboBox().getSelectedItem()
			.equals(SuperposedModeEnum.CURRENT_OBSERVATION_ONLY)) {
	    setSuperposedModeEnum(SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET);
	    ((DataViewImpl) getCommonViewImpl()).getSuperposedModeJComboBox()
		    .setSelectedItem(SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET);
	} else {
	    // Display only the current observation
	    firstObservationIndex = -1;
	    lastObservationIndex = -1;
	    setSuperposedModeEnum(null);
	    ((DataViewImpl) getCommonViewImpl()).getSuperposedModeJComboBox()
		    .setSelectedItem(SuperposedModeEnum.CURRENT_OBSERVATION_ONLY);
	    getScoop3MapPanel().getObservationLayer()
		    .init(MapCommonFunctions.createOMListFromDataset(getCommonViewModel().getDataset()));
	    getScoop3MapPanel().setMapSelectedElement(
		    MapCommonFunctions.getPointModelForObs(getCommonViewModel().getObservation(getObservationNumber()),
			    levelIndex, getCommonViewModel().getDataset().getDatasetType().toString(), true));
	}

	if (this.nearestProfiles == null) {
	    this.nearestProfiles = computeNearestProfiles();
	}
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    updateChartPanelWithComboBox(chartPanelWithComboBox, false, true, nearestProfiles);
	    chartPanelWithComboBox.zoomAll();
	    chartPanelWithComboBox.validate();
	    chartPanelWithComboBox.repaint();
	}
    }

    @Override
    protected void propertyChangeChangeSuperposedMode(final SC3PropertyChangeEvent q11Evt) {
	// nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ChangeAxisMinMax(fr.ifremer.scoop3.
     * gui.common.model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeChangeAxisMinMax(final SC3PropertyChangeEvent q11Evt) {
	if (q11Evt.getSource() instanceof ChartPanelWithComboBox) {
	    final ChartPanelWithComboBox chartPanelWithComboBox = (ChartPanelWithComboBox) q11Evt.getSource();
	    final ChangeAxisMinMaxDialog changeAxisDialog = new ChangeAxisMinMaxDialog(
		    ((DataViewImpl) getCommonViewImpl()).getScoop3Frame(),
		    chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane());
	    if (changeAxisDialog.updateAxis() == JOptionPane.YES_OPTION) {
		// Get the min and Max values
		final HashMap<String, double[]> minMaxForVariables = new HashMap<>();

		final String abscissaVar = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.getAbscissaPhysicalVar().getLabel();
		final Float minAbscissa = changeAxisDialog.getAbscissaMinValue();
		final Float maxAbscissa = changeAxisDialog.getAbscissaMaxValue();
		if ((minAbscissa != null) && (maxAbscissa != null)) {
		    minMaxForVariables.put(abscissaVar, new double[] { minAbscissa, maxAbscissa });
		}

		final String ordinateVar = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.getOrdinatePhysicalVar().getLabel();
		final Float minOrdinate = changeAxisDialog.getOrdinateMinValue();
		final Float maxOrdinate = changeAxisDialog.getOrdinateMaxValue();
		if ((minOrdinate != null) && (maxOrdinate != null)) {
		    minMaxForVariables.put(ordinateVar, new double[] { minOrdinate, maxOrdinate });
		}
		JScoop3ChartScrollPaneAbstract.allowsUpdateMinAndMaxValuesDisplayed(true);
		zoomForVariables(minMaxForVariables, null, true, null);
		JScoop3ChartScrollPaneAbstract.allowsUpdateMinAndMaxValuesDisplayed(false);
	    }
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DivideTimeserie(fr.ifremer.scoop3
     * .gui.common.model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeChangeDivideTimeserie(final SC3PropertyChangeEvent q11Evt) {
	if (q11Evt.getSource() instanceof ChartPanelWithComboBox) {
	    // Open the Dialog
	    final JDialog divideTimeseriePopup = new JDialog(((DataViewImpl) getCommonViewImpl()).getScoop3Frame(),
		    true);
	    divideTimeseriePopup.getContentPane().setLayout(new BorderLayout(10, 10));
	    divideTimeseriePopup.setTitle(Messages.getMessage("bpc-gui.ribbon-divide-timeserie"));

	    // North panel
	    final JPanel northPanel = new JPanel(new BorderLayout());
	    northPanel.setPreferredSize(new Dimension(400, 50));
	    // Label
	    final JPanel northPanel1 = new JPanel();
	    northPanel1.setPreferredSize(new Dimension(400, 30));
	    northPanel.add(northPanel1, BorderLayout.NORTH);
	    final JLabel northLabel = new JLabel(Messages.getMessage("coriolis-gui.divide-timeserie-section-number"));
	    northPanel1.add(northLabel);
	    // totalSectionNumberForTimeserie
	    final JPanel northPanel2 = new JPanel();
	    northPanel2.setPreferredSize(new Dimension(400, 25));
	    northPanel.add(northPanel2, BorderLayout.SOUTH);
	    final JTextField totalSectionNumberJTextField = new JTextField();
	    totalSectionNumberJTextField.setPreferredSize(new Dimension(40, 20));
	    if (totalSectionNumberForTimeserie == null) {
		totalSectionNumberJTextField.setText("1");
	    } else {
		totalSectionNumberJTextField.setText(String.valueOf(totalSectionNumberForTimeserie));
	    }
	    totalSectionNumberJTextField.addKeyListener(new KeyListener() {
		@Override
		public void keyPressed(final KeyEvent keyEvent) {
		    // do nothing
		}

		@Override
		public void keyReleased(final KeyEvent keyEvent) {
		    try {
			if (totalSectionNumberJTextField.getText().equals("")) {
			    // do nothing
			} else if (Integer.parseInt(totalSectionNumberJTextField.getText()) < 1) {
			    SC3Logger.LOGGER.error("Number of sections must be > 1");
			    totalSectionNumberJTextField.setText(""); // Make it blank
			} else if (Integer
				.parseInt(totalSectionNumberJTextField.getText()) > MAX_SECTION_NUMBER_FOR_TIMESERIE) {
			    SC3Logger.LOGGER.error("Number of sections must be <= " + MAX_SECTION_NUMBER_FOR_TIMESERIE);
			    totalSectionNumberJTextField.setText(""); // Make it blank
			}
		    } catch (final NumberFormatException e) {
			SC3Logger.LOGGER
				.error("Number of sections must be between 1 and " + MAX_SECTION_NUMBER_FOR_TIMESERIE);
			totalSectionNumberJTextField.setText(""); // Make it blank
		    }
		}

		@Override
		public void keyTyped(final KeyEvent keyEvent) {
		    // do nothing
		}
	    });

	    northPanel2.add(totalSectionNumberJTextField);

	    // South panel
	    final JPanel southPanel = new JPanel(new FlowLayout());
	    southPanel.setPreferredSize(new Dimension(400, 40));

	    // validate button
	    final JButton validateButton = new JButton(Messages.getMessage("bpc-gui.button-validate"));
	    validateButton.addActionListener((final ActionEvent e) -> {
		setTotalSectionNumberForTimeserie(Integer.parseInt(totalSectionNumberJTextField.getText()));
		setCurrentSectionNumberForTimeserie(1);
		divideTimeserie(getTotalSectionNumberForTimeserie(), getCurrentSectionNumberForTimeserie(), true);
		// update section label
		for (final ChartPanelWithComboBox chartPanelWithComboBox : getListGraphs()) {
		    chartPanelWithComboBox.updateSectionLabel();
		}
		divideTimeseriePopup.setVisible(false);
	    });
	    southPanel.add((new JPanel()).add(validateButton));

	    final JButton cancelButton = new JButton(Messages.getMessage("bpc-gui.button-cancel"));
	    cancelButton.addActionListener((final ActionEvent e) -> {
		divideTimeseriePopup.setVisible(false);
	    });

	    southPanel.add((new JPanel()).add(cancelButton));

	    divideTimeseriePopup.getContentPane().add(northPanel, BorderLayout.NORTH);
	    divideTimeseriePopup.getContentPane().add(southPanel, BorderLayout.SOUTH);

	    divideTimeseriePopup.pack();
	    divideTimeseriePopup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	    divideTimeseriePopup.setLocationRelativeTo(((DataViewImpl) getCommonViewImpl()).getScoop3Frame());
	    divideTimeseriePopup.setVisible(true);
	    // }
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ChangeMetadata(fr.ifremer.scoop3.gui
     * .common.model.SC3PropertyChangeEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void propertyChangeChangeMetadata(final SC3PropertyChangeEvent q11Evt) {
	if ((q11Evt.getSource() instanceof List)
		&& (((List<?>) q11Evt.getSource()).get(0) instanceof MetadataValueChange)) {
	    getCommonViewModel().updateMetadata((List<QCValueChange>) q11Evt.getSource());
	}
	getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		getCommonViewModel().isListOfRedoableChangesEmpty());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#changeQCOnMap(java.util.List)
     */
    @Override
    protected void changeQCOnMap(final List<String[]> source) {
	final List<QCValueChange> qcsChanged = new ArrayList<>();

	for (final String[] qcValueChangeStr : source) {
	    final String obsRef = qcValueChangeStr[0];
	    final String levelStr = qcValueChangeStr[1];
	    final String latitudeStr = qcValueChangeStr[2];
	    final String longitudeStr = qcValueChangeStr[3];
	    int level = -1;
	    Double latitude = -200.0;
	    Double longitude = -200.0;
	    try {
		level = Integer.parseInt(levelStr);
	    } catch (final NumberFormatException e) {
		e.printStackTrace();
	    }

	    try {
		latitude = Double.parseDouble(latitudeStr);
	    } catch (final NumberFormatException e) {
		e.printStackTrace();
	    }

	    try {
		longitude = Double.parseDouble(longitudeStr);
	    } catch (final NumberFormatException e) {
		e.printStackTrace();
	    }

	    final int newQC = ((DataViewImpl) getCommonViewImpl()).getMouseSubModeQC();

	    final int nbObs = getCommonViewModel().getObservations().size();
	    Observation obs = null;
	    int observationIndex = 0;
	    for (; (observationIndex < nbObs) && (obs == null);) {
		// if (getCommonViewModel().getObservation(observationIndex).getReference().equals(obsRef)
		// && (getCommonViewModel().getObservation(observationIndex).getFirstLatitudeClone()
		// .getValueAsDouble().compareTo(latitude) == 0)
		// && (getCommonViewModel().getObservation(observationIndex).getFirstLongitudeClone()
		// .getValueAsDouble().compareTo(longitude) == 0)) {
		if (getCommonViewModel().getObservation(observationIndex).getReference().equals(obsRef)
			&& ((getCommonViewModel().getObservation(observationIndex).getLatitude().getValues()
				.get(level) != null)
				&& (getCommonViewModel().getObservation(observationIndex).getLatitude().getValues()
					.get(level).compareTo(latitude) == 0))
			&& ((getCommonViewModel().getObservation(observationIndex).getLongitude().getValues()
				.get(level) != null)
				&& (getCommonViewModel().getObservation(observationIndex).getLongitude().getValues()
					.get(level).compareTo(longitude) == 0))) {
		    obs = getCommonViewModel().getObservation(observationIndex);
		} else {
		    observationIndex++;
		}
	    }

	    if ((obs != null) && (level >= 0)) {
		final int oldQC = obs.getLatitude().getQcValues().get(level).getQCValue();

		Long refValueStr = null;
		if (getCommonViewModel().getDataset().getDatasetType() == DatasetType.TIMESERIE) {
		    // add the refValueStr
		    refValueStr = obs.getTime().getValues().get(level);
		}

		MetadataValueChange metadataValueChange;
		if (refValueStr == null) {
		    metadataValueChange = new MetadataValueChange(QC_TO_UPDATE.OBS_METADATA, "POS$",
			    Messages.getMessage("bpc-gui.obs-metadata.position"), observationIndex, level,
			    obs.getReference(), oldQC, newQC);
		} else {
		    metadataValueChange = new MetadataValueChange(QC_TO_UPDATE.OBS_METADATA, "POS$",
			    Messages.getMessage("bpc-gui.obs-metadata.position"), observationIndex, level,
			    obs.getReference(), oldQC, newQC, refValueStr.toString());
		}

		// add the first oldQC as the original QC
		if (getCommonViewModel().getOriginalQCs()
			.get(metadataValueChange.getObsId() + "/" + metadataValueChange.getMetadata()) == null) {
		    getCommonViewModel().getOriginalQCs().put(
			    metadataValueChange.getObsId() + "/" + metadataValueChange.getMetadata(),
			    metadataValueChange.getOldQC());
		}

		qcsChanged.add(metadataValueChange);
	    }
	}

	if (!qcsChanged.isEmpty()) {
	    getCommonViewModel().updateQCs(qcsChanged);

	    final Map<String, List<QCValueChange>> updatesPerVariables = new HashMap<>();
	    for (final QCValueChange qcChanged : qcsChanged) {
		if (!(qcChanged instanceof MetadataValueChange)) {
		    if (!updatesPerVariables.containsKey(qcChanged.getParameterName())) {
			updatesPerVariables.put(qcChanged.getParameterName(), new ArrayList<>());
		    }
		    final List<QCValueChange> changedForVariable = updatesPerVariables
			    .get(qcChanged.getParameterName());
		    changedForVariable.add(qcChanged);
		} else {
		    if (qcChanged.getParameterName().equals(Observation.POSITION_VAR_NAME)) {
			// add lat
			if (!updatesPerVariables.containsKey(Observation.LATITUDE_VAR_NAME)) {
			    updatesPerVariables.put(Observation.LATITUDE_VAR_NAME, new ArrayList<>());
			}
			List<QCValueChange> changedForVariable = updatesPerVariables.get(Observation.LATITUDE_VAR_NAME);
			changedForVariable.add(qcChanged);
			// add lon
			if (!updatesPerVariables.containsKey(Observation.LONGITUDE_VAR_NAME)) {
			    updatesPerVariables.put(Observation.LONGITUDE_VAR_NAME, new ArrayList<>());
			}
			changedForVariable = updatesPerVariables.get(Observation.LONGITUDE_VAR_NAME);
			changedForVariable.add(qcChanged);
		    }
		}
	    }

	    for (final String variableNameAfterUpdateQCs : updatesPerVariables.keySet()) {
		boolean updateDoneOnGraph = false;

		for (final ChartPanelWithComboBox chartPanelWithComboBox2 : listGraphs) {
		    final String variableName2 = chartPanelWithComboBox2.getScoop3ChartPanel()
			    .getVariableNameToUpdate();
		    if (variableNameAfterUpdateQCs.equals(variableName2)) {
			updateDoneOnGraph = true;
			chartPanelWithComboBox2.qcsChange(updatesPerVariables.get(variableNameAfterUpdateQCs));
		    }
		}
		if (!updateDoneOnGraph) {
		    for (int i = 0; i < qcsChanged.size(); i++) {
			updateChartDatasetForVariable(variableNameAfterUpdateQCs,
				qcsChanged.get(i).getObservationIndex());
		    }
		}
	    }

	    getCommonViewImpl().getMetadataSplitPane().updateObservationMetadatas(
		    getCommonViewModel().getDataset().getObservations().get(getObservationNumber()),
		    (InfoInObservationSubPanel) getCommonViewImpl().getMetadataSplitPane()
			    .getInfoInObservationSubPanel(),
		    true);

	    getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		    getCommonViewModel().isListOfRedoableChangesEmpty());

	    DisplayObservationInfoForLevelDialog.updateInfoIfVisible(
		    getCommonViewModel().getObservation(getObservationNumber()), levelIndex,
		    ((DataViewModel) getCommonViewModel()).getParametersOrder());
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ChangeQC(fr.ifremer.scoop3.gui.common
     * .model.SC3PropertyChangeEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void propertyChangeChangeQC(final SC3PropertyChangeEvent q11Evt) {
	String variableName = null;
	boolean isCurrentStationOnly = true;
	int newQcValue = -1;

	ChartPanelWithComboBox chartPanelWithComboBoxSrc = null;
	List<QCValueChange> qcsChanged = null;

	if (q11Evt.getSource() instanceof Scoop3ChartPanelChangeQCJMenuItem) {
	    // This changeQC comes from a graph
	    final Scoop3ChartPanelChangeQCJMenuItem scoop3ChartPanelChangeQCJMenuItem = (Scoop3ChartPanelChangeQCJMenuItem) q11Evt
		    .getSource();
	    chartPanelWithComboBoxSrc = scoop3ChartPanelChangeQCJMenuItem.getChartPanelWithComboBox();
	    variableName = chartPanelWithComboBoxSrc.getScoop3ChartPanel().getVariableNameToUpdate();

	    isCurrentStationOnly = scoop3ChartPanelChangeQCJMenuItem.isCurrentStationOnly();
	    newQcValue = scoop3ChartPanelChangeQCJMenuItem.getQcToSet().getQCValue();

	    final String secondParam = chartPanelWithComboBoxSrc.getScoop3ChartPanel().getSecondVariableNameToUpdate();
	    final boolean secondParameterIsRef = secondParam.equals(
		    getCommonViewModel().getObservation(getObservationNumber()).getReferenceParameter().getCode());
	    final boolean secondParameterIsMeasureNumber = secondParam.equals(CommonViewModel.MEASURE_NUMBER);

	    List<List<? extends Number>> referenceValues = new ArrayList<>();

	    final List<String> obsIds = new ArrayList<>();
	    if (!secondParameterIsMeasureNumber) {

		/* Modifcation de QC d'une observation */
		if (isCurrentStationOnly) {
		    obsIds.add(getCommonViewModel().getObservation(getObservationNumber()).getId());
		    referenceValues.add(getCommonViewModel().getObservation(getObservationNumber())
			    .getReferenceParameter().getValues());
		    /* Modification de QC de l'ensemble des observations d'une plateforme */
		} else {
		    if (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET) {
			for (final Observation observation : getCommonViewModel().getObservations()) {
			    obsIds.add(observation.getId());
			    referenceValues.add(observation.getReferenceParameter().getValues());
			}
		    } else {
			for (final Observation observation : getCommonViewModel()
				.getCurrentPlatformObservations(getObservationNumber())) {
			    obsIds.add(observation.getId());
			    referenceValues.add(observation.getReferenceParameter().getValues());
			}
		    }
		}
	    } else {
		if (isCurrentStationOnly) {
		    obsIds.add(getCommonViewModel().getObservation(getObservationNumber()).getId());

		    final List<? extends Number> currentReferenceValues = new ArrayList<>();
		    final Double[] values = chartPanelWithComboBoxSrc.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			    .getOrdinatePhysicalVar().getPhysicalValuesByStation().get(getObservationNumber());
		    for (final double f : values) {
			((ArrayList<Double>) currentReferenceValues).add(f);
		    }
		    referenceValues.add(currentReferenceValues);
		} else {
		    referenceValues = new ArrayList<>();
		    int obsIndex = 0;

		    if (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET) {
			for (final Observation observation : getCommonViewModel().getObservations()) {
			    obsIds.add(observation.getId());

			    final Double[] values = chartPanelWithComboBoxSrc.getScoop3ChartPanel()
				    .getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getPhysicalValuesByStation()
				    .get(obsIndex);
			    final List<? extends Number> currentReferenceValues = new ArrayList<>();
			    for (final double f : values) {
				((ArrayList<Double>) currentReferenceValues).add(f);
			    }
			    referenceValues.add(currentReferenceValues);
			    obsIndex++;
			}
		    } else {
			for (final Observation observation : getCommonViewModel()
				.getCurrentPlatformObservations(getObservationNumber())) {
			    obsIds.add(observation.getId());

			    final Double[] values = chartPanelWithComboBoxSrc.getScoop3ChartPanel()
				    .getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getPhysicalValuesByStation()
				    .get(obsIndex);
			    final List<? extends Number> currentReferenceValues = new ArrayList<>();
			    for (final double f : values) {
				((ArrayList<Double>) currentReferenceValues).add(f);
			    }
			    referenceValues.add(currentReferenceValues);
			    obsIndex++;
			}
		    }
		}
	    }

	    // final List<Double> parameterValues = getCommonViewModel().getObservation(getObservationNumber())
	    // .getOceanicParameter(variableName) == null ? null
	    // : getCommonViewModel().getObservation(getObservationNumber())
	    // .getOceanicParameter(variableName).getValues();

	    qcsChanged = chartPanelWithComboBoxSrc.updateQCs(obsIds, isCurrentStationOnly, newQcValue,
		    secondParameterIsRef, referenceValues,
		    getSuperposedModeEnum() != null ? getSuperposedModeEnum().toString() : null, isBPCVersion);

	    // add the first oldQC as the original QC
	    for (final QCValueChange qcv : qcsChanged) {
		if (getCommonViewModel().getOriginalQCs().get(qcv.getObservationIndex() + "/"
			+ qcv.getObservationLevel() + "/" + qcv.getParameterName()) == null) {
		    getCommonViewModel().getOriginalQCs().put(
			    qcv.getObservationIndex() + "/" + qcv.getObservationLevel() + "/" + qcv.getParameterName(),
			    qcv.getOldQC());
		}
	    }

	} else if (q11Evt instanceof SC3PropertyQCChangeEvent) {
	    // This changeQC comes from the Error Dialog box
	    final SC3PropertyQCChangeEvent sc3PropertyQCChangeEvent = (SC3PropertyQCChangeEvent) q11Evt;

	    variableName = sc3PropertyQCChangeEvent.getVariableName();

	    for (final ChartPanelWithComboBox chartPanelWithComboBox2 : listGraphs) {
		final String variableName2 = chartPanelWithComboBox2.getScoop3ChartPanel().getVariableNameToUpdate();
		if ((chartPanelWithComboBoxSrc == null) && variableName.equals(variableName2)) {
		    chartPanelWithComboBoxSrc = chartPanelWithComboBox2;
		}
	    }

	    isCurrentStationOnly = true;
	    newQcValue = sc3PropertyQCChangeEvent.getNewQC().getQCValue();

	    int observationIndex = -1;
	    int index = 0;
	    for (final Observation observation : getCommonViewModel().getObservations()) {
		if (observation.getId().equals(sc3PropertyQCChangeEvent.getObsRef())) {
		    observationIndex = index;
		}
		index++;
	    }

	    final double refMinValue = getCommonViewModel().getObservation(observationIndex).getReferenceParameter()
		    .getValues().get(sc3PropertyQCChangeEvent.getRefLevel()).doubleValue();
	    final double refMaxValue = refMinValue;

	    final int levelMin = sc3PropertyQCChangeEvent.getRefLevel();
	    final int levelMax = levelMin;

	    final List<String> obsIds = new ArrayList<>();
	    obsIds.add(sc3PropertyQCChangeEvent.getObsRef());

	    if ((chartPanelWithComboBoxSrc != null) && (observationIndex == getObservationNumber())) {
		final double physValMin = chartPanelWithComboBoxSrc.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.getPhysicalValueForLevel(sc3PropertyQCChangeEvent.getRefLevel());
		final double physValMax = physValMin;

		final List<List<? extends Number>> referenceValues = new ArrayList<>();
		referenceValues
			.add(getCommonViewModel().getObservation(observationIndex).getReferenceParameter().getValues());

		qcsChanged = chartPanelWithComboBoxSrc.getScoop3ChartPanel().getjScoop3ChartScrollPane().updateQCs( //
			obsIds, //
			isCurrentStationOnly, //
			newQcValue, //
			refMinValue, //
			refMaxValue, //
			physValMin, //
			physValMax, //
			variableName, //
			referenceValues, //
			(getSuperposedModeEnum() != null ? getSuperposedModeEnum().toString()
				: SuperposedModeEnum.CURRENT_OBSERVATION_ONLY.toString()),
			isBPCVersion);

		// add the first oldQC as the original QC
		for (final QCValueChange qcv : qcsChanged) {
		    if (getCommonViewModel().getOriginalQCs().get(qcv.getObservationIndex() + "/"
			    + qcv.getObservationLevel() + "/" + qcv.getParameterName()) == null) {
			getCommonViewModel().getOriginalQCs().put(qcv.getObservationIndex() + "/"
				+ qcv.getObservationLevel() + "/" + qcv.getParameterName(), qcv.getOldQC());
		    }
		}
	    } else {
		qcsChanged = new ArrayList<>();
		final List<Double> values = getCommonViewModel().getObservation(observationIndex)
			.getOceanicParameter(variableName).getValues();
		final List<QCValues> qcValues = getCommonViewModel().getObservation(observationIndex)
			.getOceanicParameter(variableName).getQcValues();

		final double physValMin = values.get(sc3PropertyQCChangeEvent.getRefLevel());
		final double physValMax = physValMin;

		for (int level = levelMin; level <= levelMax; level++) {
		    if (values.size() > level) {
			final double value = values.get(level);
			if ((value >= physValMin) && (value <= physValMax) && (qcValues.get(level) != null)
				&& (qcValues.get(level).getQCValue() != newQcValue)) {
			    // Backup old values
			    final QCValueChange qcChanged = new QCValueChange(observationIndex, level,
				    qcValues.get(level).getQCValue(), newQcValue, sc3PropertyQCChangeEvent.getObsRef(),
				    variableName, value, String.valueOf(value),
				    String.valueOf(getCommonViewModel()
					    .getObservation(observationIndex).getReferenceParameter().getValues().get(
						    level)),
				    getCommonViewModel()
					    ./* getPlatformForObservation(observationIndex).getCode()) */getPlatformForObservationId(
						    sc3PropertyQCChangeEvent.getObsRef())
					    .getCode());
			    // add the first oldQC as the original QC
			    if (getCommonViewModel().getOriginalQCs().get(qcChanged.getObservationIndex() + "/"
				    + qcChanged.getObservationLevel() + "/" + qcChanged.getParameterName()) == null) {
				getCommonViewModel().getOriginalQCs().put(qcChanged.getObservationIndex() + "/"
					+ qcChanged.getObservationLevel() + "/" + qcChanged.getParameterName(),
					qcChanged.getOldQC());
			    }
			    qcsChanged.add(qcChanged);
			}
		    }
		}
	    }
	}

	if ((qcsChanged != null) && !qcsChanged.isEmpty()) {
	    getCommonViewModel().updateQCs(qcsChanged);

	    final Map<String, List<QCValueChange>> updatesPerVariables = new HashMap<>();
	    for (final QCValueChange qcChanged : qcsChanged) {

		if (!updatesPerVariables.containsKey(qcChanged.getParameterName())) {
		    updatesPerVariables.put(qcChanged.getParameterName(), new ArrayList<>());
		}
		final List<QCValueChange> changedForVariable = updatesPerVariables.get(qcChanged.getParameterName());
		changedForVariable.add(qcChanged);
	    }

	    for (final String variableNameAfterUpdateQCs : updatesPerVariables.keySet()) {
		boolean updateDoneOnGraph = false;

		for (final ChartPanelWithComboBox chartPanelWithComboBox2 : listGraphs) {
		    final String variableName2 = chartPanelWithComboBox2.getScoop3ChartPanel()
			    .getVariableNameToUpdate();
		    if ((chartPanelWithComboBoxSrc != chartPanelWithComboBox2)
			    && variableNameAfterUpdateQCs.equals(variableName2)) {
			updateDoneOnGraph = true;
			chartPanelWithComboBox2.qcsChange(updatesPerVariables.get(variableNameAfterUpdateQCs));
		    }
		}
		if (!updateDoneOnGraph) {
		    for (int i = 0; i < qcsChanged.size(); i++) {
			updateChartDatasetForVariable(variableNameAfterUpdateQCs,
				qcsChanged.get(i).getObservationIndex());
		    }
		}
	    }
	}
	getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		getCommonViewModel().isListOfRedoableChangesEmpty());

	DisplayObservationInfoForLevelDialog.updateInfoIfVisible(
		getCommonViewModel().getObservation(getObservationNumber()), levelIndex,
		((DataViewModel) getCommonViewModel()).getParametersOrder());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ChangeQC(fr.ifremer.scoop3.gui.common
     * .model.SC3PropertyChangeEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void propertyChangeRemoveMeasure(final SC3PropertyChangeEvent q11Evt) {
	String variableName = null;
	boolean isCurrentStationOnly = true;
	int newQcValue = -1;

	ChartPanelWithComboBox chartPanelWithComboBoxSrc = null;
	List<QCValueChange> qcsChanged = null;

	if (q11Evt.getSource() instanceof Scoop3ChartPanelChangeQCJMenuItem) {
	    // This changeQC comes from a graph
	    final Scoop3ChartPanelChangeQCJMenuItem scoop3ChartPanelChangeQCJMenuItem = (Scoop3ChartPanelChangeQCJMenuItem) q11Evt
		    .getSource();
	    chartPanelWithComboBoxSrc = scoop3ChartPanelChangeQCJMenuItem.getChartPanelWithComboBox();
	    variableName = chartPanelWithComboBoxSrc.getScoop3ChartPanel().getVariableNameToUpdate();

	    isCurrentStationOnly = scoop3ChartPanelChangeQCJMenuItem.isCurrentStationOnly();
	    newQcValue = scoop3ChartPanelChangeQCJMenuItem.getQcToSet().getQCValue();

	    final String secondParam = chartPanelWithComboBoxSrc.getScoop3ChartPanel().getSecondVariableNameToUpdate();
	    final boolean secondParameterIsRef = secondParam.equals(
		    getCommonViewModel().getObservation(getObservationNumber()).getReferenceParameter().getCode());
	    final boolean secondParameterIsMeasureNumber = secondParam.equals(CommonViewModel.MEASURE_NUMBER);

	    List<List<? extends Number>> referenceValues = new ArrayList<>();

	    final List<String> obsIds = new ArrayList<>();
	    if (!secondParameterIsMeasureNumber) {

		/* Modifcation de QC d'une observation */
		if (isCurrentStationOnly) {
		    obsIds.add(getCommonViewModel().getObservation(getObservationNumber()).getId());
		    referenceValues.add(getCommonViewModel().getObservation(getObservationNumber())
			    .getReferenceParameter().getValues());
		    /* Modification de QC de l'ensemble des observations d'une plateforme */
		} else {
		    if (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET) {
			for (final Observation observation : getCommonViewModel().getObservations()) {
			    obsIds.add(observation.getId());
			    referenceValues.add(observation.getReferenceParameter().getValues());
			}
		    } else {
			for (final Observation observation : getCommonViewModel()
				.getCurrentPlatformObservations(getObservationNumber())) {
			    obsIds.add(observation.getId());
			    referenceValues.add(observation.getReferenceParameter().getValues());
			}
		    }
		}
	    } else {
		if (isCurrentStationOnly) {
		    obsIds.add(getCommonViewModel().getObservation(getObservationNumber()).getId());

		    final List<? extends Number> currentReferenceValues = new ArrayList<>();
		    final Double[] values = chartPanelWithComboBoxSrc.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			    .getOrdinatePhysicalVar().getPhysicalValuesByStation().get(getObservationNumber());
		    for (final double f : values) {
			((ArrayList<Double>) currentReferenceValues).add(f);
		    }
		    referenceValues.add(currentReferenceValues);
		} else {
		    referenceValues = new ArrayList<>();
		    int obsIndex = 0;

		    if (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET) {
			for (final Observation observation : getCommonViewModel().getObservations()) {
			    obsIds.add(observation.getId());

			    final Double[] values = chartPanelWithComboBoxSrc.getScoop3ChartPanel()
				    .getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getPhysicalValuesByStation()
				    .get(obsIndex);
			    final List<? extends Number> currentReferenceValues = new ArrayList<>();
			    for (final double f : values) {
				((ArrayList<Double>) currentReferenceValues).add(f);
			    }
			    referenceValues.add(currentReferenceValues);
			    obsIndex++;
			}
		    } else {
			for (final Observation observation : getCommonViewModel()
				.getCurrentPlatformObservations(getObservationNumber())) {
			    obsIds.add(observation.getId());

			    final Double[] values = chartPanelWithComboBoxSrc.getScoop3ChartPanel()
				    .getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getPhysicalValuesByStation()
				    .get(obsIndex);
			    final List<? extends Number> currentReferenceValues = new ArrayList<>();
			    for (final double f : values) {
				((ArrayList<Double>) currentReferenceValues).add(f);
			    }
			    referenceValues.add(currentReferenceValues);
			    obsIndex++;
			}
		    }
		}
	    }

	    // final List<Double> parameterValues = getCommonViewModel().getObservation(getObservationNumber())
	    // .getOceanicParameter(variableName) == null ? null
	    // : getCommonViewModel().getObservation(getObservationNumber())
	    // .getOceanicParameter(variableName).getValues();

	    qcsChanged = chartPanelWithComboBoxSrc.updateQCs(obsIds, isCurrentStationOnly, newQcValue,
		    secondParameterIsRef, referenceValues, getSuperposedModeEnum().toString(), isBPCVersion);

	    final ArrayList<QCValueChange> tempQcsChanged = new ArrayList<>();
	    for (final QCValueChange qcv : qcsChanged) {
		final Observation currentObs = getCommonViewModel().getObservations().get(qcv.getObservationIndex());
		final String refParameter = currentObs.getReferenceParameter().getCode();
		for (final String param : currentObs.getOceanicParameters().keySet()) {
		    if (!qcv.getParameterName().equals(param)) {
			final Double parameterValue = currentObs.getOceanicParameter(param).getValues()
				.get(qcv.getObservationLevel());
			final QCValues oldQc = currentObs.getOceanicParameter(param).getQcValues()
				.get(qcv.getObservationLevel());
			final QCValueChange newQCV = new QCValueChange(qcv.getObservationIndex(),
				qcv.getObservationLevel(), oldQc.getByteQCValue(), 9, qcv.getObsId(), param,
				parameterValue, parameterValue.toString(), qcv.getRefValueStr(), qcv.getPlatformCode());
			tempQcsChanged.add(newQCV);
		    }
		}

		if (!qcv.getParameterName().equals(refParameter)) {
		    // looking for the right index in reference parameter values
		    Integer rightIndex = null;
		    for (int index = 0; index < currentObs.getReferenceParameter().getValues().size(); index++) {
			if (String.valueOf(currentObs.getReferenceParameter().getValues().get(index))
				.equals(qcv.getRefValueStr())) {
			    rightIndex = index;
			    break;
			}
		    }
		    try {
			final Number refParameterValue = currentObs.getReferenceParameter().getValues().get(rightIndex);
			final QCValues oldQc = currentObs.getReferenceParameter().getQcValues().get(rightIndex);
			final Double measureNumber = (double) (qcv.getObservationLevel() + 1);
			final QCValueChange newQCV = new QCValueChange(qcv.getObservationIndex(),
				qcv.getObservationLevel(), oldQc.getByteQCValue(), 9, qcv.getObsId(), refParameter,
				measureNumber, String.valueOf(measureNumber), refParameterValue.toString(),
				qcv.getPlatformCode());
			tempQcsChanged.add(newQCV);
		    } catch (final Exception e) {
			final UnhandledException exception = new UnhandledException(
				"Impossible de trouver la valeur recherchée du paramètre de référence : rightIndex = "
					+ rightIndex,
				e);
		    }
		}
	    }
	    qcsChanged.addAll(tempQcsChanged);

	    // add the first oldQC as the original QC
	    for (final QCValueChange qcv : qcsChanged) {
		if (getCommonViewModel().getOriginalQCs().get(qcv.getObservationIndex() + "/"
			+ qcv.getObservationLevel() + "/" + qcv.getParameterName()) == null) {
		    getCommonViewModel().getOriginalQCs().put(
			    qcv.getObservationIndex() + "/" + qcv.getObservationLevel() + "/" + qcv.getParameterName(),
			    qcv.getOldQC());
		}
	    }

	}

	if ((qcsChanged != null) && !qcsChanged.isEmpty()) {
	    for (final QCValueChange qcChanged : qcsChanged) {
		if (!qcChanged.getParameterName().equals(getCommonViewModel().getObservation(getObservationNumber())
			.getReferenceParameter().getCode())) {
		    // set an old parameter value to conserve it if the user undo/redo the removing action
		    qcChanged.setOldParameterValue(qcChanged.getParameterValue());
		    getCommonViewModel().getObservation(getObservationNumber())
			    .getOceanicParameter(qcChanged.getParameterName()).getValues()
			    .set(qcChanged.getObservationLevel(), Double.NaN);
		    getCommonViewModel().getObservation(getObservationNumber())
			    .getOceanicParameter(qcChanged.getParameterName()).getQcValues()
			    .set(qcChanged.getObservationLevel(), QCValues.QC_9);
		} else {
		    // set an old parameter value to conserve it if the user undo/redo the removing action
		    qcChanged.setOldParameterValue(Double.valueOf(qcChanged.getRefValueStr()));
		    ((List<Double>) getCommonViewModel().getObservation(getObservationNumber()).getReferenceParameter()
			    .getValues()).set(qcChanged.getObservationLevel(), Double.NaN);
		    getCommonViewModel().getObservation(getObservationNumber()).getReferenceParameter().getQcValues()
			    .set(qcChanged.getObservationLevel(), QCValues.QC_9);
		}

		for (final ChartPanelWithComboBox chartPanelWithComboBox2 : listGraphs) {
		    if (chartPanelWithComboBox2.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			    .getAbscissaPhysicalVar().getLabel().equals(qcChanged.getParameterName())) {

			// abscissa
			chartPanelWithComboBox2.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				.getAbscissaPhysicalVar().getPhysicalValuesByStation()
				.get(getObservationNumber())[qcChanged.getObservationLevel()] = Double.NaN;
			break;
		    } else if (chartPanelWithComboBox2.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			    .getAbscissaPhysicalVar().getLabel().equals("measure_number")
			    && qcChanged.getParameterName().equals(getCommonViewModel()
				    .getObservation(getObservationNumber()).getReferenceParameter().getCode())) {
			// ordinate
			chartPanelWithComboBox2.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				.getOrdinatePhysicalVar().getPhysicalValuesByStation()
				.get(getObservationNumber())[qcChanged.getObservationLevel()] = Double.NaN;
			break;
		    }
		}
	    }
	    getCommonViewModel().updateRemovedMeasures(qcsChanged);
	}

	// simulate click on scrollbar to fix shifting bug (FAE 49960)
	for (final ChartPanelWithComboBox c : listGraphs) {
	    if (c.getScoop3ChartPanel().getjScoop3ChartScrollPane().getAbscissaPhysicalVar().getLabel()
		    .equals("DENS")) {
		c.simulateAClickOnScrollbarToAvoidShifting();
		break;
	    }
	}

	getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		getCommonViewModel().isListOfRedoableChangesEmpty());

	DisplayObservationInfoForLevelDialog.updateInfoIfVisible(
		getCommonViewModel().getObservation(getObservationNumber()), levelIndex,
		((DataViewModel) getCommonViewModel()).getParametersOrder());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayPointsAndCircle()
     */
    @Override
    protected void propertyChangeDisplayCircleOnGraph() {
	((DataViewImpl) getCommonViewImpl()).updateDisplayOrHidePointsIcons(DataViewImpl.DISPLAY_POINTS_AND_CIRLCE);

	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    chartPanelWithComboBox.repaint();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayDataTable()
     */
    @Override
    protected void propertyChangeDisplayDataTable() {
	final Observation currentObs = getCommonViewModel().getDataset().getObservations().get(getObservationNumber());
	final DataTableDialog dataTableDialog = new DataTableDialog(
		((DataViewImpl) getCommonViewImpl()).getScoop3Frame(), currentObs,
		((DataViewModel) getCommonViewModel()).getParametersOrder(), getCommonViewImpl().getQCValuesSettable());

	final ArrayList<DataTableUpdateAbstract> updatesForVariables = dataTableDialog.getUpdates();
	if (!updatesForVariables.isEmpty()) {
	    // Update the SC3 Model
	    getCommonViewModel().updateDataTableChanges(updatesForVariables, getObservationNumber());

	    // Update the CartModel
	    ((DataViewModel) getCommonViewModel()).convertScoop3ModelToChartModel();

	    propertyChangeShift();

	    getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		    getCommonViewModel().isListOfRedoableChangesEmpty());
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayedQC(fr.ifremer.scoop3.gui.
     * common .model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeDisplayedQC(final SC3PropertyChangeEvent q11Evt) {
	if (q11Evt.getSource() instanceof DisplayedQCEnum) {
	    displayedQCEnum = (DisplayedQCEnum) q11Evt.getSource();
	}

	if (this.nearestProfiles == null) {
	    this.nearestProfiles = computeNearestProfiles();
	}
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    updateChartPanelWithComboBox(chartPanelWithComboBox, true, true, nearestProfiles);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayLineOnGraph()
     */
    @Override
    protected void propertyChangeDisplayLineOnGraph() {
	((DataViewImpl) getCommonViewImpl()).updateDisplayOrHidePointsIcons(DataViewImpl.DISPLAY_LINE_ON_GRAPH);

	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    chartPanelWithComboBox.repaint();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayOnlyQCOnGraph(fr.ifremer.scoop3
     * .gui.common.model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeDisplayOnlyQCOnGraph(final SC3PropertyChangeEvent q11Evt) {
	if (q11Evt.getSource() instanceof SelectAndChangeQCCommandButton) {
	    final SelectAndChangeQCCommandButton selectAndChangeQCCommandButton = (SelectAndChangeQCCommandButton) q11Evt
		    .getSource();

	    ((DataViewImpl) getCommonViewImpl()).updateDisplayOnlyQCOnGraph(selectAndChangeQCCommandButton);

	    for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
		chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane().getJScoop3Chart()
			.computeRange(false, getObservationNumber(), timeserieDivided);
		updateChartPanelWithComboBox(chartPanelWithComboBox, false, true, nearestProfiles);
	    }
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ExcludeOnlyQCOnGraph(fr.ifremer.scoop3
     * .gui.common.model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeExcludeOnlyQCOnGraph(final SC3PropertyChangeEvent q11Evt) {
	if (q11Evt.getSource() instanceof SelectAndChangeQCCommandButton) {
	    final SelectAndChangeQCCommandButton selectAndChangeQCCommandButton = (SelectAndChangeQCCommandButton) q11Evt
		    .getSource();

	    ((DataViewImpl) getCommonViewImpl()).updateExcludeOnlyQCOnGraph(selectAndChangeQCCommandButton);

	    for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
		chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane().getJScoop3Chart()
			.computeRange(false, getObservationNumber(), timeserieDivided);
		updateChartPanelWithComboBox(chartPanelWithComboBox, false, true, nearestProfiles);
	    }
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayPoints()
     */
    @Override
    protected void propertyChangeDisplayPointsOnGraph() {
	((DataViewImpl) getCommonViewImpl()).updateDisplayOrHidePointsIcons(DataViewImpl.DISPLAY_POINTS);

	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    chartPanelWithComboBox.repaint();
	}
    }

    @Override
    protected void propertyChangeDisplayStationType(final SC3PropertyChangeEvent q11Evt) {
	if (q11Evt.getSource() instanceof SuperposedModeEnum) {
	    setSuperposedModeEnum((SuperposedModeEnum) q11Evt.getSource());

	    boolean rebuildAllCharts = false;

	    // deactivate the shift mode if we activated it
	    if (JScoop3ChartScrollPaneAbstract.getCoefX() != 0) {
		JScoop3ChartScrollPaneAbstract.setCoefX(0);
		propertyChangeShift();
	    }

	    switch (getSuperposedModeEnum()) {
	    case CURRENT_OBSERVATION_ONLY:
		// Display only the current observation
		firstObservationIndex = -1;
		lastObservationIndex = -1;
		getScoop3MapPanel().getObservationLayer()
			.init(MapCommonFunctions.createOMListFromDataset(getCommonViewModel().getDataset()));
		getScoop3MapPanel().setMapSelectedElement(MapCommonFunctions.getPointModelForObs(
			getCommonViewModel().getObservation(getObservationNumber()), levelIndex,
			getCommonViewModel().getDataset().getDatasetType().toString(), true));

		removeCycleNumberFromComboBox();
		rebuildAllCharts = true;
		break;
	    case NEAREST_PROFILES_FOR_ALL_PLATFORMS:
		removeCycleNumberFromComboBox();
		break;
	    case NEAREST_PROFILES_FOR_CURRENT_PLATFORM:
		removeCycleNumberFromComboBox();
		break;
	    case PROFILES_FOR_PLATFORM_FROM_DATASET:
		addCycleNumberIntoComboBox();
		break;
	    case ALL_OBSERVATIONS_FROM_DATASET:
		addCycleNumberIntoComboBox();
		break;
	    }

	    if (rebuildAllCharts) {
		updateChartPanel((getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET)
			|| (getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET));
	    } else {
		if (this.nearestProfiles == null) {
		    this.nearestProfiles = computeNearestProfiles();
		}
		for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
		    updateChartPanelWithComboBox(chartPanelWithComboBox, false, true, nearestProfiles);
		    chartPanelWithComboBox.setScoop3ChartPanelPopupMenu(new Scoop3ChartPanelPopupMenu(
			    chartPanelWithComboBox, chartPanelWithComboBox.getDataViewController()));
		    chartPanelWithComboBox.zoomAll();
		}
	    }
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_DisplayStatistics(fr.ifremer.scoop3
     * .gui.common.model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeDisplayStatistics(final SC3PropertyChangeEvent q11Evt) {
	if (q11Evt.getSource() instanceof ChartPanelWithComboBox) {
	    updateAdditionalGraphsToDisplay((ChartPanelWithComboBox) q11Evt.getSource(), false,
		    computeNearestProfiles());
	}
    }

    @Override
    protected void propertyChangeEditClimatoAdditionalGraphs(final SC3PropertyChangeEvent q11Evt) {
	if (q11Evt.getSource() instanceof ChartPanelWithComboBox) {
	    // Create a new executor SINGLE thread service (thread are executed one by one)
	    final ExecutorService executor = Executors.newSingleThreadExecutor();

	    executor.execute(getRunnableForExecutor(q11Evt));

	    // Shutdown the executor
	    executor.shutdown();
	}
    }

    protected Runnable getRunnableForExecutor(final SC3PropertyChangeEvent q11Evt) {
	return () -> {
	    final List<AdditionalGraph> additionalGraphs = getAdditionalGraphsToDisplayClimato(
		    ((ChartPanelWithComboBox) q11Evt.getSource()), true, null);
	    ((ChartPanelWithComboBox) q11Evt.getSource()).setAdditionalSeriesToDisplay(additionalGraphs);
	};
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_KeepBounds(fr.ifremer.scoop3.gui.common
     * .model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeKeepBounds(final SC3PropertyChangeEvent q11Evt) {
	if (q11Evt.getSource() instanceof Boolean) {
	    keepBounds = (boolean) q11Evt.getSource();
	    if (!keepBounds) {
		keepBoundsMinAndMaxForVariables.clear();
	    }
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_Redo()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void propertyChangeRedo() {
	final List<? extends UndoRedoAction> undoRedoActions = getCommonViewModel().redoLastChanges();
	updateDatatableIfNeeded(undoRedoActions);
	if (undoRedoActions.get(0) instanceof QCValueChange) {
	    propertyChangeRedoOnGraphs((List<QCValueChange>) undoRedoActions);
	}
	getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		getCommonViewModel().isListOfRedoableChangesEmpty());

	DisplayObservationInfoForLevelDialog.updateInfoIfVisible(
		getCommonViewModel().getObservation(getObservationNumber()), levelIndex,
		((DataViewModel) getCommonViewModel()).getParametersOrder());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_RedoOnGraphs(java.util.List)
     */
    @Override
    protected void propertyChangeRedoOnGraphs(final List<QCValueChange> qcsChanged) {
	if ((qcsChanged != null) && !qcsChanged.isEmpty()) {
	    final Map<String, List<QCValueChange>> updatesPerVariables = new HashMap<>();
	    for (final QCValueChange qcChanged : qcsChanged) {
		if ((qcChanged.getParameterName() != null) && removingMeasure) {
		    if (!qcChanged.getParameterName().equals(getCommonViewModel().getObservation(getObservationNumber())
			    .getReferenceParameter().getCode())) {
			// set an old parameter value to conserve it if the user undo/redo the removing action
			qcChanged.setOldParameterValue(qcChanged.getOldParameterValue());
			getCommonViewModel().getObservation(getObservationNumber())
				.getOceanicParameter(qcChanged.getParameterName()).getValues()
				.set(qcChanged.getObservationLevel(), Double.NaN);
			getCommonViewModel().getObservation(getObservationNumber())
				.getOceanicParameter(qcChanged.getParameterName()).getQcValues()
				.set(qcChanged.getObservationLevel(), QCValues.QC_9);
		    } else {
			// set an old parameter value to conserve it if the user undo/redo the removing action
			qcChanged.setOldParameterValue(Double.valueOf(qcChanged.getRefValueStr()));
			((ZParameter) getCommonViewModel().getObservation(getObservationNumber())
				.getReferenceParameter()).getValues().set(qcChanged.getObservationLevel(), Double.NaN);
			getCommonViewModel().getObservation(getObservationNumber()).getReferenceParameter()
				.getQcValues().set(qcChanged.getObservationLevel(), QCValues.QC_9);
		    }

		    for (final ChartPanelWithComboBox chartPanelWithComboBox2 : listGraphs) {
			if (chartPanelWithComboBox2.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				.getAbscissaPhysicalVar().getLabel().equals(qcChanged.getParameterName())) {
			    // abscissa
			    chartPanelWithComboBox2.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				    .getAbscissaPhysicalVar().getPhysicalValuesByStation()
				    .get(getObservationNumber())[qcChanged.getObservationLevel()] = Double.NaN;
			} else if (chartPanelWithComboBox2.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				.getAbscissaPhysicalVar().getLabel().equals("measure_number")
				&& qcChanged.getParameterName().equals(getCommonViewModel()
					.getObservation(getObservationNumber()).getReferenceParameter().getCode())) {
			    // ordinate
			    chartPanelWithComboBox2.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				    .getOrdinatePhysicalVar().getPhysicalValuesByStation()
				    .get(getObservationNumber())[qcChanged.getObservationLevel()] = Double.NaN;
			    break;
			}
		    }
		}
		if (!updatesPerVariables.containsKey(qcChanged.getParameterName())) {
		    updatesPerVariables.put(qcChanged.getParameterName(), new ArrayList<>());
		}
		final List<QCValueChange> changedForVariable = updatesPerVariables.get(qcChanged.getParameterName());
		changedForVariable.add(qcChanged);
	    }

	    for (final String variableName : updatesPerVariables.keySet()) {
		for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
		    final String variableName2 = chartPanelWithComboBox.getScoop3ChartPanel().getVariableNameToUpdate();
		    if ((variableName != null) && variableName.equals(variableName2)) {
			chartPanelWithComboBox.qcsChange(updatesPerVariables.get(variableName));
			chartPanelWithComboBox.repaintChart();
		    }
		}
	    }
	}

    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_Shift()
     */
    @Override
    protected void propertyChangeShift() {
	if (this.nearestProfiles == null) {
	    this.nearestProfiles = computeNearestProfiles();
	}
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    updateChartPanelWithComboBox(chartPanelWithComboBox, true, true, nearestProfiles);

	    if (JScoop3ChartScrollPaneAbstract.getCoefX() != 0) {
		// Shift in progress
		final Rectangle2D dataTotalArea = chartPanelWithComboBox.getScoop3ChartPanel()
			.getjScoop3ChartScrollPane().getDataAreaForZoomLevelOne();

		Rectangle2D wishedZoom = new Rectangle2D(dataTotalArea.getMinX(), dataTotalArea.getMinY(),
			dataTotalArea.getWidth(), dataTotalArea.getHeight());
		// first 50% ...
		// wishedZoom.width = (int) ((wishedZoom.width * 50) / 100d);
		wishedZoom = new Rectangle2D(dataTotalArea.getMinX(), dataTotalArea.getMinY(),
			(wishedZoom.getWidth() * 50) / 100d, dataTotalArea.getHeight());
		// then, depends on the shift level
		// wishedZoom.width = (int) (wishedZoom.width /
		// JScoop3ChartScrollPaneAbstract.getCOEF_XWithoutFactor());
		wishedZoom = new Rectangle2D(dataTotalArea.getMinX(), dataTotalArea.getMinY(),
			wishedZoom.getWidth() / JScoop3ChartScrollPaneAbstract.getCoefXWithoutFactor(),
			dataTotalArea.getHeight());

		chartPanelWithComboBox.zoomRect(wishedZoom);
	    }
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_Undo()
     */
    @Override
    protected void propertyChangeUndo() {
	propertyChangeUndo(true);

	DisplayObservationInfoForLevelDialog.updateInfoIfVisible(
		getCommonViewModel().getObservation(getObservationNumber()), levelIndex,
		((DataViewModel) getCommonViewModel()).getParametersOrder());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_UndoAll()
     */
    @Override
    protected void propertyChangeUndoAll() {
	do {
	    propertyChangeUndo(false);
	} while (!getCommonViewModel().isListOfUndoableChangesEmpty());

	getCommonViewModel().clearRedoableChanges();

	for (final ChartPanelWithComboBox c : listGraphs) {
	    final JScoop3ChartPanelAbstract scoop3ChartPanel = c.getScoop3ChartPanel();
	    c.updateJScoop3ChartPanel(scoop3ChartPanel,
		    getCommonViewModel().getObservation(getObservationNumber()).getReferenceParameter().getCode());

	    // Create a new executor SINGLE thread service (thread are executed one by one)
	    final ExecutorService executor = Executors.newSingleThreadExecutor();

	    executor.execute(getRunnableForExecutor(c));

	    // Shutdown the executor
	    executor.shutdown();
	}

	getCommonViewImpl().updateUndoRedoButtons(getCommonViewModel().isListOfUndoableChangesEmpty(),
		getCommonViewModel().isListOfRedoableChangesEmpty());

	DisplayObservationInfoForLevelDialog.updateInfoIfVisible(
		getCommonViewModel().getObservation(getObservationNumber()), levelIndex,
		((DataViewModel) getCommonViewModel()).getParametersOrder());
    }

    protected Runnable getRunnableForExecutor(final ChartPanelWithComboBox c) {
	return () ->
	// redraw climato graphs
	c.setAdditionalSeriesToDisplay(getAdditionalGraphsToDisplayClimato(c, true, null));
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_UpdatePositionGraphs()
     */
    @Override
    protected void propertyChangeUpdatePositionGraphs() {
	boolean positionGraphDisplayed = false;
	for (final ChartPanelWithComboBox c : listGraphs) {
	    if (c.getScoop3ChartPanel().getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getLabel()
		    .equals(Observation.LATITUDE_VAR_NAME)
		    || c.getScoop3ChartPanel().getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getLabel()
			    .equals(Observation.LONGITUDE_VAR_NAME)
		    || c.getScoop3ChartPanel().getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getLabel()
			    .equals("SPEED")) {
		if (timeserieDivided) {
		    updateChartPanelAfterDivideTimeserie(c, false, computeNearestProfiles(),
			    getDivideTimeserieChartDataset());
		} else {
		    updateChartPanelWithComboBox(c, true, false, computeNearestProfiles());
		}
		positionGraphDisplayed = true;
	    }
	}
	if (positionGraphDisplayed) {
	    for (final ChartPanelWithComboBox c : listGraphs) {
		if (c.getScoop3ChartPanel().getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getLabel()
			.equals(Observation.LATITUDE_VAR_NAME)
			|| c.getScoop3ChartPanel().getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getLabel()
				.equals(Observation.LONGITUDE_VAR_NAME)
			|| c.getScoop3ChartPanel().getjScoop3ChartScrollPane().getOrdinatePhysicalVar().getLabel()
				.equals("SPEED")) {
		    c.simulateAClickOnScrollbarToAvoidShifting();
		    break;
		}
	    }
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_Validate()
     */
    @Override
    protected void propertyChangeValidate() {
	ReportJDialog.disposeIfExists();
	DisplayObservationInfoForLevelDialog.disposeIfExists();
	EventBus.publish(new GuiEventStepCompleted(StepCode.QC22));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ZoomIn(fr.ifremer.scoop3.gui.common
     * .model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeZoomIn(final SC3PropertyChangeEvent q11Evt) {
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    chartPanelWithComboBox.zoomIn();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ZoomInitial(fr.ifremer.scoop3.gui.
     * common .model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeZoomInitial(final SC3PropertyChangeEvent q11Evt) {
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    chartPanelWithComboBox.zoomAll();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#propertyChange_ZoomOut(fr.ifremer.scoop3.gui.common
     * .model.SC3PropertyChangeEvent)
     */
    @Override
    protected void propertyChangeZoomOut(final SC3PropertyChangeEvent q11Evt) {
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    chartPanelWithComboBox.zoomOut();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#removeSelectionBox()
     */
    @Override
    public void removeSelectionBox() {
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    chartPanelWithComboBox.removeSelectionBox();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewController#setCurrentLevel(int)
     */
    @Override
    protected void setCurrentLevel(final int levelIndex) {
	this.levelIndex = levelIndex;

	DisplayObservationInfoForLevelDialog.updateInfoIfVisible(
		getCommonViewModel().getObservation(getObservationNumber()), levelIndex,
		((DataViewModel) getCommonViewModel()).getParametersOrder());

	/*
	 * Update info
	 */
	final Observation obs = getCommonViewModel().getObservation(getObservationNumber());

	// Reference Parameter
	final StringBuilder newInfoToDisplay = new StringBuilder();

	newInfoToDisplay.append("<html>");
	newInfoToDisplay.append(obs.getReferenceParameter().getCode());
	newInfoToDisplay.append(" : ");
	if ((obs.getReferenceParameter().getValues().size() > levelIndex)
		&& (obs.getReferenceParameter().getQcValues().get(levelIndex) != null)) {
	    if (getCommonViewModel().getDataset().getDatasetType() != DatasetType.PROFILE) {
		newInfoToDisplay.append(
			"<font color='" + obs.getReferenceParameter().getQcValues().get(levelIndex).getHexaColorCode()
				+ "'>" + Conversions.formatDateAndHourMinSec(
					(Long) obs.getReferenceParameter().getValues().get(levelIndex))
				+ "</font>");
	    } else {
		newInfoToDisplay.append(
			"<font color='" + obs.getReferenceParameter().getQcValues().get(levelIndex).getHexaColorCode()
				+ "'>" + obs.getReferenceParameter().getValues().get(levelIndex) + "</font>");
	    }
	} else {
	    newInfoToDisplay.append("---");
	}

	final NumberFormat formatter = new DecimalFormat("#0.####");

	// Parameters
	final Map<String, List<String>> parametersOrder = ((DataViewModel) getCommonViewModel()).getParametersOrder();
	if ((parametersOrder == null) || !parametersOrder.containsKey(obs.getReference())) {
	    for (final OceanicParameter parameter : obs.getOceanicParameters().values()) {
		if (parameter.getValues().size() > levelIndex) {
		    if (!parameter.getValues().get(levelIndex).equals(Parameter.DOUBLE_EMPTY_VALUE)) {
			newInfoToDisplay.append(" | ");
			newInfoToDisplay.append(parameter.getCode());
			newInfoToDisplay.append(" : ");
			newInfoToDisplay
				.append("<font color='" + parameter.getQcValues().get(levelIndex).getHexaColorCode()
					+ "'>" + formatter.format(parameter.getValues().get(levelIndex)) + "</font>");
		    }
		} else {
		    newInfoToDisplay.append(" | ");
		    newInfoToDisplay.append(parameter.getCode());
		    newInfoToDisplay.append(" : ");
		    newInfoToDisplay.append("---");
		}
	    }
	} else {
	    // The order of the parameters are defined
	    for (final String parameterName : parametersOrder.get(obs.getReference())) {
		final OceanicParameter parameter = obs.getOceanicParameters().get(parameterName);
		if (parameter != null) {
		    if (parameter.getValues().size() > levelIndex) {
			if (!parameter.getValues().get(levelIndex).equals(Parameter.DOUBLE_EMPTY_VALUE)) {
			    newInfoToDisplay.append(" | ");
			    newInfoToDisplay.append(parameter.getCode());
			    newInfoToDisplay.append(" : ");
			    newInfoToDisplay.append(
				    "<font color='" + parameter.getQcValues().get(levelIndex).getHexaColorCode() + "'>"
					    + formatter.format(parameter.getValues().get(levelIndex)) + "</font>");
			}
		    } else {
			newInfoToDisplay.append(" | ");
			newInfoToDisplay.append(parameter.getCode());
			newInfoToDisplay.append(" : ");
			newInfoToDisplay.append("---");
		    }
		}
	    }
	}
	newInfoToDisplay.append("</html>");

	((DataViewImpl) getCommonViewImpl()).updateInfoLabel(newInfoToDisplay.toString());

	// FAE 26637
	// ((DataViewImpl) getCommonViewImpl()).updateDisplayOrHidePointsIcons(DataViewImpl.DISPLAY_POINTS_AND_CIRLCE);

	/*
	 * Update selected level on graphs
	 */
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    chartPanelWithComboBox.setCurrentLevel(levelIndex);
	}

	// Send the event
	getPropertyChangeSupport().firePropertyChange(new MapPropertyChangeEvent(
		MapCommonFunctions.getPointModelForObs(getCommonViewModel().getObservation(getObservationNumber()),
			levelIndex, getCommonViewModel().getDataset().getDatasetType().toString(), true),
		MAP_EVENT_ENUM.SELECT_OBSERVATION_NAV));
    }

    /**
     * To override if needed.
     *
     * Update the additional Graphs to display
     *
     * @param chartPanelWithComboBox
     * @param forceDisplayAdditionalSeries
     * @param list
     *            the nearest profiles list
     */
    protected final void updateAdditionalGraphsToDisplay(final ChartPanelWithComboBox chartPanelWithComboBox,
	    final boolean forceDisplayAdditionalSeries, final List<Profile> nearestProfiles) {

	final List<AdditionalGraph> additionalGraphsStationType = getAdditionalGraphsToDisplayStationType(
		chartPanelWithComboBox, forceDisplayAdditionalSeries, nearestProfiles);

	Double maxDepthForOtherProfiles = null;
	if (getSuperposedModeEnum() != null) {
	    switch (getSuperposedModeEnum()) {
	    case CURRENT_OBSERVATION_ONLY:
		// Nothing to do here
		break;
	    case NEAREST_PROFILES_FOR_ALL_PLATFORMS:
		break;
	    case NEAREST_PROFILES_FOR_CURRENT_PLATFORM:
		if (additionalGraphsStationType != null) {
		    maxDepthForOtherProfiles = 0d;
		    for (final AdditionalGraph additionalGraph : additionalGraphsStationType) {
			maxDepthForOtherProfiles = Math.max(maxDepthForOtherProfiles,
				additionalGraph.getOrdinateValues()[0]);
			maxDepthForOtherProfiles = Math.max(maxDepthForOtherProfiles,
				additionalGraph.getOrdinateValues()[additionalGraph.getOrdinateValues().length - 1]);
		    }
		}
		break;
	    case PROFILES_FOR_PLATFORM_FROM_DATASET:
		final Observation observation = getCommonViewModel().getObservation(getObservationNumber());
		if (observation instanceof Profile) {
		    maxDepthForOtherProfiles = 0d;
		    for (final Observation obs : observation.getSensor().getPlatform().getAllObservations()) {
			maxDepthForOtherProfiles = Math.max(maxDepthForOtherProfiles, obs.getZ().getValues().get(0));
			maxDepthForOtherProfiles = Math.max(maxDepthForOtherProfiles,
				obs.getZ().getValues().get(obs.getZ().getValues().size() - 1));
		    }
		}
		break;
	    case ALL_OBSERVATIONS_FROM_DATASET:
		final Observation currentObservation = getCommonViewModel().getObservation(getObservationNumber());
		if (currentObservation instanceof Profile) {
		    maxDepthForOtherProfiles = 0d;
		    for (final Observation obs : Dataset.getInstance().getObservations()) {
			maxDepthForOtherProfiles = Math.max(maxDepthForOtherProfiles, obs.getZ().getValues().get(0));
			maxDepthForOtherProfiles = Math.max(maxDepthForOtherProfiles,
				obs.getZ().getValues().get(obs.getZ().getValues().size() - 1));
		    }
		}
		break;
	    }
	}

	// Create a new executor SINGLE thread service (thread are executed one by one)
	final ExecutorService executor = Executors.newSingleThreadExecutor();

	executor.execute(getRunnableForExecutor(chartPanelWithComboBox, forceDisplayAdditionalSeries,
		maxDepthForOtherProfiles, additionalGraphsStationType));

	// Shutdown the executor
	executor.shutdown();

	final Map<String, double[]> minAndMaxForVariables = new HashMap<>();

	if ((listGraphs != null) && !listGraphs.isEmpty()) {
	    if (getSuperposedModeEnum() != SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET) {
		updateMinMaxForRefParameterComputeMinAndMax(minAndMaxForVariables);
	    } else {
		updateMinMaxForRefParameterComputeMinAndMaxForAllProfilesForPlatformFromDataset(chartPanelWithComboBox,
			minAndMaxForVariables);
	    }
	}

	updateMinMaxForRefParameter(chartPanelWithComboBox, minAndMaxForVariables);
    }

    protected Runnable getRunnableForExecutor(final ChartPanelWithComboBox chartPanelWithComboBox,
	    final boolean forceDisplayAdditionalSeries, final Double maxDepthForOtherProfiles,
	    final List<AdditionalGraph> additionalGraphsStationType) {
	return () -> {
	    final List<AdditionalGraph> additionalGraphsClimato = getAdditionalGraphsToDisplayClimato(
		    chartPanelWithComboBox, forceDisplayAdditionalSeries, maxDepthForOtherProfiles);

	    List<AdditionalGraph> additionalGraphs = null;
	    if ((additionalGraphsStationType != null) || (additionalGraphsClimato != null)) {
		additionalGraphs = new ArrayList<>();
		if (additionalGraphsStationType != null) {
		    additionalGraphs.addAll(additionalGraphsStationType);
		}
		if (additionalGraphsClimato != null) {
		    additionalGraphs.addAll(additionalGraphsClimato);
		}
	    }

	    chartPanelWithComboBox.setAdditionalSeriesToDisplay(additionalGraphs);
	};
    }

    /**
     * Definit l'aspect et le comportement du panneau java2D
     *
     * @param force
     */
    @Override
    public void updateChartPanel(final boolean chartsWithAllParameters) {

	// Retrieve the reference parameter
	String refParameterName = getCommonViewModel().getObservation(getObservationNumber()).getReferenceParameter()
		.getCode();
	ChartPhysicalVariable referencePhysicalVariable = ((DataViewModel) getCommonViewModel()).getChartDataset()
		.getPhysicalVariable(refParameterName);

	// If SuperposedModeEnum = PROFILES_FOR_PLATFORM_FROM_DATASET, check if same ref (fix #56855)
	boolean superPosedModeRecompute = false;
	if ((listGraphs != null) && (firstObservationIndex != -1) && (getSuperposedModeEnum() != null)
		&& (getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)) {
	    // && (getSuperposedModeEnum() != SuperposedModeEnum.CURRENT_OBSERVATION_ONLY)) {

	    for (final ChartPanelWithComboBox chart : listGraphs) {
		if (!refParameterName.equalsIgnoreCase(chart.getReferenceParameterCodeForThisObservation())) {
		    superPosedModeRecompute = true;
		    break;
		}
	    }

	}
	// superPosedModeRecompute = true;
	if ((listGraphs == null) || (firstObservationIndex == -1) || isForceUpdateChartPanel()
		|| superPosedModeRecompute) {

	    /*
	     * Memorize old selected parameters and
	     */
	    // if (listGraphs != null) {
	    // for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    // oldSelectedParameters.add(chartPanelWithComboBox.getSelectedValue());
	    // }
	    // }
	    if ((listGraphs != null) && (paramOfDifferentGraphs != null) && !paramOfDifferentGraphs.isEmpty()) {
		// if oldSelectedParameters is empty, it will be equals to the default.properties preference
		if (oldSelectedParameters.isEmpty()) {
		    String propertyName;
		    final DatasetType datasetType = getCommonViewModel().getDataset().getDatasetType();
		    switch (datasetType) {
		    case PROFILE:
			propertyName = "gui.profile-order-for-graphs";
			break;
		    case TIMESERIE:
			propertyName = "gui.timeserie-order-for-graphs";
			break;
		    case TRAJECTORY:
			propertyName = "gui.trajectory-order-for-graphs";
			break;
		    default:
			propertyName = "gui.default-order-for-graphs";
			break;
		    }

		    // Get the params order
		    final String paramsOrder = FileConfig.getScoop3FileConfig().getString(propertyName);
		    final String[] paramsOrderSplitted = paramsOrder == null ? new String[0] : paramsOrder.split(" ");
		    for (int i = 0; i < paramsOrderSplitted.length; i++) {
			// add the ref parameter /DEPH or /PRES to the String for the combobox
			paramsOrderSplitted[i] = paramsOrderSplitted[i] + "/" + getCommonViewModel().getDataset()
				.getObservations().get(0).getReferenceParameter().getCode();
		    }

		    for (final String s : paramsOrderSplitted) {
			oldSelectedParameters.add(s);
		    }
		}
		for (int index = 0; index < paramOfDifferentGraphs.size(); index++) {
		    if (!oldSelectedParameters.contains(paramOfDifferentGraphs.get(index))) {
			if (oldSelectedParameters.size() > index) {
			    oldSelectedParameters.add(index, paramOfDifferentGraphs.get(index));
			} else {
			    oldSelectedParameters.add(paramOfDifferentGraphs.get(index));
			}
		    } else {
			oldSelectedParameters.remove(paramOfDifferentGraphs.get(index));
			if (oldSelectedParameters.size() > index) {
			    oldSelectedParameters.add(index, paramOfDifferentGraphs.get(index));
			} else {
			    oldSelectedParameters.add(paramOfDifferentGraphs.get(index));
			}
		    }
		}

	    }

	    // For each physical variable of the chart dataset (except reference end level)
	    // Create a chart physical parameter / reference parameter
	    listGraphs = new ArrayList<>();

	    // Display graphs only if global QC != 4
	    if (getCommonViewModel().getObservation(getObservationNumber()).getQc() != QCValues.QC_4) {

		final int maxIndex = ((DataViewModel) getCommonViewModel()).getChartDataset().getPhysicalVariables()
			.size();
		final ArrayList<String> parametersName = new ArrayList<>();
		final Map<String, String> parametersNameWithUnit = new HashMap<>();
		final ArrayList<String> parametersCombo = new ArrayList<>();
		final ArrayList<String> parametersWithUnitCombo = new ArrayList<>();
		final ArrayList<Integer> parametersIndex = new ArrayList<>();

		/*
		 * Memorize new available parameters
		 */
		// First Oceanic Param
		for (int index = 0; index < maxIndex; index++) {
		    final ChartPhysicalVariable physicalVariable = ((DataViewModel) getCommonViewModel())
			    .getChartDataset().getPhysicalVariables().get(index);

		    // Check if data exist for this param & observationNumber
		    boolean isParamAcceptedByFilter = true;
		    if (((DataViewModel) getCommonViewModel()).isParameterListFiltered()
			    && (((DataViewModel) getCommonViewModel()).getParameterListFiltered() != null)) {

			isParamAcceptedByFilter = (((DataViewModel) getCommonViewModel()).getParameterListFiltered()
				.containsKey(physicalVariable.getPhysicalParameter()));
		    }

		    if (isParamAcceptedByFilter
			    && ((!physicalVariable.isReferenceParameter() && !physicalVariable.isLevelParameter())
				    && (physicalVariable.getPhysicalValuesByStation().size() > getObservationNumber())
				    && (chartsWithAllParameters
					    ? chartsWithAllParameters
					    : physicalVariable.getPhysicalValuesByStation()
						    .get(getObservationNumber()).length != 0)
				    && (chartsWithAllParameters ? chartsWithAllParameters
					    : (getCommonViewModel().getObservation(getObservationNumber())
						    .getOceanicParameters().keySet()
						    .contains(physicalVariable.getLabel())
						    || physicalVariable.getLabel().equals("Latitude")
						    || physicalVariable.getLabel().equals("Longitude"))))) {
			parametersName.add(physicalVariable.getLabel());
			parametersNameWithUnit.put(physicalVariable.getLabel(), physicalVariable.getLabel() + " ("
				+ (physicalVariable.getUnit() == null ? "---" : physicalVariable.getUnit()) + ")"
				+ (Dataset.getInstance().getParameterDataModeMap().get(
					getCommonViewModel().getObservation(getObservationNumber()).getId()) != null
						? (Dataset.getInstance().getParameterDataModeMap()
							.get(getCommonViewModel().getObservation(getObservationNumber())
								.getId())
							.get(physicalVariable.getLabel()) == null
								? ""
								: " (" + Dataset.getInstance().getParameterDataModeMap()
									.get(getCommonViewModel()
										.getObservation(getObservationNumber())
										.getId())
									.get(physicalVariable.getLabel()) + ")")
						: ""));

			parametersCombo.add(physicalVariable.getLabel() + " " + SEPARATOR_FOR_COMBO_LABELS + " "
				+ referencePhysicalVariable.getLabel());
			parametersWithUnitCombo.add(parametersNameWithUnit.get(physicalVariable.getLabel()) //
				+ " " + SEPARATOR_FOR_COMBO_LABELS //
				+ " " + referencePhysicalVariable.getLabel()
				+ (referencePhysicalVariable.getUnit() == null ? "" // no unit
					: " (" + referencePhysicalVariable.getUnit() + ")")
				+ (Dataset.getInstance().getParameterDataModeMap().get(
					getCommonViewModel().getObservation(getObservationNumber()).getId()) != null
						? (Dataset.getInstance().getParameterDataModeMap()
							.get(getCommonViewModel().getObservation(getObservationNumber())
								.getId())
							.get(referencePhysicalVariable.getLabel()) == null
								? ""
								: " (" + Dataset.getInstance().getParameterDataModeMap()
									.get(getCommonViewModel()
										.getObservation(getObservationNumber())
										.getId())
									.get(referencePhysicalVariable.getLabel())
									+ ")")
						: ""));

			parametersIndex.add(index);
		    }
		}
		// Then Ref param
		for (int index = 0; index < maxIndex; index++) {
		    final ChartPhysicalVariable physicalVariable = ((DataViewModel) getCommonViewModel())
			    .getChartDataset().getPhysicalVariables().get(index);
		    if ((physicalVariable.isReferenceParameter() || physicalVariable.isLevelParameter())
			    && !parametersName.contains(physicalVariable.getLabel())) {
			parametersName.add(physicalVariable.getLabel());
			parametersNameWithUnit.put(physicalVariable.getLabel(), physicalVariable.getLabel() + " ("
				+ (physicalVariable.getUnit() == null ? "---" : physicalVariable.getUnit()) + ")"
				+ (Dataset.getInstance().getParameterDataModeMap().get(
					getCommonViewModel().getObservation(getObservationNumber()).getId()) != null
						? (Dataset.getInstance().getParameterDataModeMap()
							.get(getCommonViewModel().getObservation(getObservationNumber())
								.getId())
							.get(physicalVariable.getLabel()) == null
								? ""
								: " (" + Dataset.getInstance().getParameterDataModeMap()
									.get(getCommonViewModel()
										.getObservation(getObservationNumber())
										.getId())
									.get(physicalVariable.getLabel()) + ")")
						: ""));
			parametersIndex.add(index);
		    }
		}

		/*
		 * Add special couples (defined in the default.properties)
		 */
		final String additionalParameters = FileConfig.getScoop3FileConfig()
			.getString(getCommonViewModel().getDataset().getDatasetType() == DatasetType.PROFILE
				? "gui.data-view.additional-parameters-couples-for-profiles"
				: "gui.data-view.additional-parameters-couples-for-timeseries");
		if (additionalParameters != null) {
		    for (final String additionalParams : additionalParameters.split(";")) {
			final String[] additionalParamsSplitted = additionalParams.split(SEPARATOR_FOR_COMBO_LABELS);
			if (additionalParamsSplitted.length == 2) {
			    final String parameterName = additionalParamsSplitted[0];
			    final String parameterName2 = additionalParamsSplitted[1];

			    // If both parameters exist, add the choice in the comboBox
			    if (parametersName.contains(parameterName) && parametersName.contains(parameterName2)) {
				parametersCombo
					.add(parameterName + " " + SEPARATOR_FOR_COMBO_LABELS + " " + parameterName2);
				parametersWithUnitCombo.add(
					parametersNameWithUnit.get(parameterName) + " " + SEPARATOR_FOR_COMBO_LABELS
						+ " " + parametersNameWithUnit.get(parameterName2));
			    }
			}
		    }
		}

		addSpecificValuesInParametersCombo(parametersCombo, parametersWithUnitCombo, parametersNameWithUnit);

		final int maxGraphs = getCommonViewModel().doesDatasetContainProfiles()
			? DataViewImpl.profileGraphsCount
			: DataViewImpl.timeserieGraphsCount;

		boolean takeOldParam = false;
		if (getCommonViewModel().getDataset().getDatasetType() == DatasetType.PROFILE) {
		    if (FileConfig.getScoop3FileConfig().getString("gui.local-profile-order-for-graphs").trim()
			    .equals("")) {
			takeOldParam = true;
		    } else {
			sortParameters(FileConfig.getScoop3FileConfig().getString("gui.local-profile-order-for-graphs"),
				parametersCombo, parametersWithUnitCombo, parametersName, parametersNameWithUnit,
				parametersIndex);
		    }
		} else if (getCommonViewModel().getDataset().getDatasetType() == DatasetType.TIMESERIE) {
		    if (FileConfig.getScoop3FileConfig().getString("gui.local-timeserie-order-for-graphs").trim()
			    .equals("")) {
			takeOldParam = true;
		    } else {
			sortParameters(
				FileConfig.getScoop3FileConfig().getString("gui.local-timeserie-order-for-graphs"),
				parametersCombo, parametersWithUnitCombo, parametersName, parametersNameWithUnit,
				parametersIndex);
		    }
		} else {
		    if (FileConfig.getScoop3FileConfig().getString("gui.local-trajectory-order-for-graphs").trim()
			    .equals("")) {
			takeOldParam = true;
		    } else {
			sortParameters(
				FileConfig.getScoop3FileConfig().getString("gui.local-trajectory-order-for-graphs"),
				parametersCombo, parametersWithUnitCombo, parametersName, parametersNameWithUnit,
				parametersIndex);
		    }
		}

		// init of oldSelectedParameters
		if (oldSelectedParameters == null) {
		    oldSelectedParameters = new ArrayList<>();
		}

		// set the comboBox values for PSAL/TEMP graph
		setVariableListComboBoxes(parametersCombo);

		for (int index = 0; (index < parametersIndex.size()) && (index < parametersCombo.size())
			&& (listGraphs.size() < maxGraphs); index++) {
		    final int parameterIndex = parametersIndex.get(index);

		    /*
		     * Select the same parameter if possible
		     */
		    int indexForCurrentChart = parameterIndex;
		    String selectedValue = parametersCombo.get(index);

		    boolean forceCreateChart = false;
		    // try to load parameters of the last graph, it does not depends on getSuperposedModeEnum()
		    if ((oldSelectedParameters.size() > index)
			    && parametersCombo.contains(oldSelectedParameters.get(index)) && takeOldParam) {
			selectedValue = parametersCombo.get(parametersCombo.indexOf(oldSelectedParameters.get(index)));
		    }
		    final String varLabel = selectedValue.split(DataViewController.SEPARATOR_FOR_COMBO_LABELS)[0]
			    .trim();
		    refParameterName = selectedValue.split(DataViewController.SEPARATOR_FOR_COMBO_LABELS)[1].trim();
		    referencePhysicalVariable = ((DataViewModel) getCommonViewModel()).getChartDataset()
			    .getPhysicalVariable(refParameterName);

		    indexForCurrentChart = parametersIndex.get(parametersName.indexOf(varLabel));
		    forceCreateChart = true;

		    /*
		     * Create the chart ...
		     */
		    final ChartPhysicalVariable physicalVariable = ((DataViewModel) getCommonViewModel())
			    .getChartDataset().getPhysicalVariables().get(indexForCurrentChart);

		    if (physicalVariable.getLabel().equals(referencePhysicalVariable.getLabel())) {
			referencePhysicalVariable = ((DataViewModel) getCommonViewModel()).getChartDataset()
				.getPhysicalVariable(CommonViewModel.MEASURE_NUMBER);
		    }

		    if (forceCreateChart || (/* !physicalVariable.isReferenceParameter() && */ !physicalVariable
			    .isLevelParameter())) {
			// Create graph only if needed
			SC3Logger.LOGGER.debug("addChartPanel : {} for observation {}", physicalVariable.getLabel(),
				getObservationNumber());
			final JScoop3ChartPanelAbstract scoop3ChartPanel;
			if (getCommonViewModel().doesDatasetContainProfiles()) {
			    scoop3ChartPanel = new JScoop3ChartPanelForProfile(physicalVariable,
				    referencePhysicalVariable,
				    ((getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
					    || (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET))
						    ? (firstObservationIndex == -1 ? 0 : firstObservationIndex)
						    : getObservationNumber(),
				    (getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
					    || (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET)
						    ? (lastObservationIndex == -1
							    ? (getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET
								    ? getObservationNumber()
								    : getCommonViewModel().getDataset()
									    .getObservations().size() - 1)
							    : lastObservationIndex)
						    : getObservationNumber());
			} else {
			    scoop3ChartPanel = new JScoop3ChartPanelForTimeSerie(physicalVariable,
				    referencePhysicalVariable,
				    (getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
					    || (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET)
						    ? (firstObservationIndex == -1 ? 0 : firstObservationIndex)
						    : getObservationNumber(),
				    (getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
					    || (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET)
						    ? (lastObservationIndex == -1
							    ? (getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET
								    ? getObservationNumber()
								    : getCommonViewModel().getDataset()
									    .getObservations().size() - 1)
							    : lastObservationIndex)
						    : getObservationNumber(),
				    getObservationNumber(), timeserieDivided);

			}
			scoop3ChartPanel.getjScoop3ChartScrollPane().setCurrentStation(getObservationNumber());

			/* Afficher le bon QC (valeur, date ou position) dans le cas des séries temporelles */
			/* TODO Rajouter sdans l'urgence par jerome Code a incorporer dans setCurrentStation */
			if (!getCommonViewModel().doesDatasetContainProfiles() && (displayedQCEnum != null)) {
			    switch (displayedQCEnum) {
			    case DATE_QC:
				final int[] timeQCs = getTimeQCs();
				((JScoop3ChartPanelForTimeSerie) scoop3ChartPanel).setQCToUse(Observation.TIME_VAR_NAME,
					timeQCs);
				break;
			    case POSITION_QC:
				final int[] posQCs = getPositionQCs();
				((JScoop3ChartPanelForTimeSerie) scoop3ChartPanel)
					.setQCToUse(Observation.LATITUDE_VAR_NAME, posQCs);
				break;
			    case VALUES:
				((JScoop3ChartPanelForTimeSerie) scoop3ChartPanel).setQCToUse(null, null);
				break;
			    }
			}

			listGraphs.add(
				new ChartPanelWithComboBox(this, scoop3ChartPanel, selectedValue, refParameterName));
		    }
		}

		for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
		    chartPanelWithComboBox.setComboBoxValues(parametersCombo.toArray(new String[0]),
			    displayUnitsInCombo() ? parametersWithUnitCombo.toArray(new String[0]) : null);
		    // Add the number of possibilities in ComboBox
		    chartPanelWithComboBox.setComboBoxNumber(parametersCombo.size());
		}
	    }

	    this.getDataViewImpl()
		    .updateChartPanel(
			    listGraphs, getCommonViewModel().getObservation(getObservationNumber())
				    .getOceanicParameters().values().isEmpty(),
			    getCommonViewModel().doesDatasetContainProfiles());

	    if (this.nearestProfiles == null) {
		this.nearestProfiles = computeNearestProfiles();
	    }

	    if (superPosedModeRecompute) {
		final ArrayList<ChartPanelWithComboBox> tempListGraphs = new ArrayList<>(listGraphs);
		for (final ChartPanelWithComboBox chartPanelWithComboBox : tempListGraphs) {
		    chartPanelWithComboBox.setCurrentStation(getObservationNumber());
		    updateAdditionalGraphsToDisplay(chartPanelWithComboBox, true, nearestProfiles);
		}
	    } else {
		final ArrayList<ChartPanelWithComboBox> tempListGraphs = new ArrayList<>(listGraphs);
		for (final ChartPanelWithComboBox chartPanelWithComboBox : tempListGraphs) {
		    updateAdditionalGraphsToDisplay(chartPanelWithComboBox, true, nearestProfiles);
		}

		updateMinMaxForRefParameter();
	    }
	} else {
	    if (this.nearestProfiles == null) {
		this.nearestProfiles = computeNearestProfiles();
	    }
	    final ArrayList<ChartPanelWithComboBox> tempListGraphs = new ArrayList<>(listGraphs);
	    for (final ChartPanelWithComboBox chartPanelWithComboBox : tempListGraphs) {
		chartPanelWithComboBox.setCurrentStation(getObservationNumber());
		updateAdditionalGraphsToDisplay(chartPanelWithComboBox, true, nearestProfiles);
	    }
	}
	setForceUpdateChartPanel(false);

	final DatasetType datasetTypeTest = getCommonViewModel().getDataset().getDatasetType();
	if (datasetTypeTest == DatasetType.TIMESERIE) {

	    // Compute sectionNumberForTimeSerie (Math.ceil(valuesLength / NbPointMAxParSerie))
	    final ChartPhysicalVariable ref = ((DataViewModel) getCommonViewModel()).getChartDataset()
		    .getPhysicalVariable(((DataViewModel) getCommonViewModel()).getChartDataset().getReferenceLabel());
	    final int refLength = ref.getPhysicalValuesByStation().get(getObservationNumber()).length;

	    final int sectionNumberForTimeSerie = (int) Math
		    .ceil((double) refLength / (double) MAX_INIT_POINT_NUMBER_FOR_TIMESERIE);

	    // Set sectionNumber and currentSectionNumber
	    setTotalSectionNumberForTimeserie(sectionNumberForTimeSerie);
	    setCurrentSectionNumberForTimeserie(1);
	    // Divide TS
	    divideTimeserie(sectionNumberForTimeSerie, 1, true);

	    setForceUpdateChartPanel(true);

	    for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
		// add player if time serie
		chartPanelWithComboBox.addPlayer();
	    }

	}

    }

    private void updateMinMaxForRefParameter() {

	if (listGraphs != null) {
	    final Map<String, double[]> minAndMaxForVariables = new HashMap<>();
	    if (!listGraphs.isEmpty()) {
		updateMinMaxForRefParameterComputeMinAndMax(minAndMaxForVariables);
	    }

	    for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
		final String varAbs = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.getAbscissaPhysicalVar().getLabel();
		chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.setMinAbscissaPhysVal(minAndMaxForVariables.get(varAbs)[0]);
		chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.setMaxAbscissaPhysVal(minAndMaxForVariables.get(varAbs)[1]);

		final String varOrd = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.getOrdinatePhysicalVar().getLabel();
		chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.setMinOrdinatePhysVal(minAndMaxForVariables.get(varOrd)[0]);
		chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
			.setMaxOrdinatePhysVal(minAndMaxForVariables.get(varOrd)[1]);

		chartPanelWithComboBox.validate();
		chartPanelWithComboBox.repaint();
	    }

	    if ((keepBoundsMinAndMaxForVariables != null) && !keepBoundsMinAndMaxForVariables.isEmpty()) {
		zoomForVariables(keepBoundsMinAndMaxForVariables, null, true, null);
	    }
	}
    }

    private void updateMinMaxForRefParameter(final ChartPanelWithComboBox chartPanelWithComboBox,
	    final Map<String, double[]> minAndMaxForVariables) {
	final String varAbs = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		.getAbscissaPhysicalVar().getLabel();
	if (minAndMaxForVariables.get(varAbs) != null) {
	    chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .setMinAbscissaPhysVal(minAndMaxForVariables.get(varAbs)[0]);
	    chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .setMaxAbscissaPhysVal(minAndMaxForVariables.get(varAbs)[1]);
	}

	final String varOrd = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		.getOrdinatePhysicalVar().getLabel();
	if (minAndMaxForVariables.get(varOrd) != null) {
	    chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .setMinOrdinatePhysVal(minAndMaxForVariables.get(varOrd)[0]);
	    chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .setMaxOrdinatePhysVal(minAndMaxForVariables.get(varOrd)[1]);
	}

	/*
	 *
	 */
	final HashMap<String, double[]> minMaxForVariables = /* listGraphs.get(0) */chartPanelWithComboBox
		.computeMinMaxForVariables(
			chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				.getDataAreaForZoomLevelOne(),
			chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
				.getDataAreaForZoomLevelCurrent());
	chartPanelWithComboBox.zoomForVariables(minMaxForVariables, true, false, false/*
										       * , getObservationNumber()
										       */);
	chartPanelWithComboBox.validate();
	chartPanelWithComboBox.repaint();
    }

    private void keepBoundsComputeVisibleMinAndVisibleMaxForVariables() {
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    /*
	     * Min/Max for Abscissa
	     */
	    final String varAbs = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .getAbscissaPhysicalVar().getLabel();
	    final float minAbs = (float) chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .computeMinViewableAbscissaForColumnHeader();
	    final float maxAbs = (float) chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .computeMaxViewableAbscissaForColumnHeader();
	    double[] minAndMaxAbs;
	    if (!keepBoundsMinAndMaxForVariables.containsKey(varAbs)) {
		minAndMaxAbs = new double[] { minAbs, maxAbs };
	    } else {
		minAndMaxAbs = keepBoundsMinAndMaxForVariables.get(varAbs);
		if (minAndMaxAbs[0] > minAbs) {
		    minAndMaxAbs[0] = minAbs;
		}
		if (minAndMaxAbs[1] < maxAbs) {
		    minAndMaxAbs[1] = maxAbs;
		}
	    }
	    keepBoundsMinAndMaxForVariables.put(varAbs, minAndMaxAbs);

	    /*
	     * Min/Max for Ordinate
	     */
	    final String varOrd = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .getOrdinatePhysicalVar().getLabel();
	    final float minOrd = (float) chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .computeMinViewableOrdinateForColumnHeader();
	    final float maxOrd = (float) chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .computeMaxViewableOrdinateForColumnHeader();
	    double[] minAndMaxOrd;
	    if (!keepBoundsMinAndMaxForVariables.containsKey(varOrd)) {
		minAndMaxOrd = new double[] { minOrd, maxOrd };
	    } else {
		minAndMaxOrd = keepBoundsMinAndMaxForVariables.get(varOrd);
		if (minAndMaxOrd[0] > minOrd) {
		    minAndMaxOrd[0] = minOrd;
		}
		if (minAndMaxOrd[1] < maxOrd) {
		    minAndMaxOrd[1] = maxOrd;
		}
	    }
	    keepBoundsMinAndMaxForVariables.put(varOrd, minAndMaxOrd);
	}
    }

    private void updateMinMaxForRefParameterComputeMinAndMax(final Map<String, double[]> minAndMaxForVariables) {
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    /*
	     * Min/Max for Abscissa
	     */
	    final String varAbs = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .getAbscissaPhysicalVar().getLabel();
	    final double minAbs = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .getMinAbscissaPhysVal();
	    final double maxAbs = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .getMaxAbscissaPhysVal();
	    double[] minAndMaxAbs;
	    if (!minAndMaxForVariables.containsKey(varAbs)) {
		minAndMaxAbs = new double[] { minAbs, maxAbs };
	    } else {
		minAndMaxAbs = minAndMaxForVariables.get(varAbs);
		if (minAndMaxAbs[0] > minAbs) {
		    minAndMaxAbs[0] = minAbs;
		}
		if (minAndMaxAbs[1] < maxAbs) {
		    minAndMaxAbs[1] = maxAbs;
		}
	    }
	    minAndMaxForVariables.put(varAbs, minAndMaxAbs);

	    /*
	     * Min/Max for Ordinate
	     */
	    final String varOrd = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .getOrdinatePhysicalVar().getLabel();
	    final double minOrd = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .getMinOrdinatePhysVal();
	    final double maxOrd = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		    .getMaxOrdinatePhysVal();
	    double[] minAndMaxOrd;
	    if (!minAndMaxForVariables.containsKey(varOrd)) {
		minAndMaxOrd = new double[] { minOrd, maxOrd };
	    } else {
		minAndMaxOrd = minAndMaxForVariables.get(varOrd);
		if (minAndMaxOrd[0] > minOrd) {
		    minAndMaxOrd[0] = minOrd;
		}
		if (minAndMaxOrd[1] < maxOrd) {
		    minAndMaxOrd[1] = maxOrd;
		}
	    }
	    minAndMaxForVariables.put(varOrd, minAndMaxOrd);
	}
    }

    private void updateMinMaxForRefParameterComputeMinAndMaxForAllProfilesForPlatformFromDataset(
	    final ChartPanelWithComboBox chartPanelWithComboBox, final Map<String, double[]> minAndMaxForVariables) {
	/*
	 * Min/Max for Abscissa
	 */
	final String varAbs = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		.getAbscissaPhysicalVar().getLabel();
	final double minAbs = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		.getMinAbscissaPhysVal();
	final double maxAbs = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		.getMaxAbscissaPhysVal();
	double[] minAndMaxAbs;
	if (!minAndMaxForVariables.containsKey(varAbs)) {
	    minAndMaxAbs = new double[] { minAbs, maxAbs };
	} else {
	    minAndMaxAbs = minAndMaxForVariables.get(varAbs);
	    if (minAndMaxAbs[0] > minAbs) {
		minAndMaxAbs[0] = minAbs;
	    }
	    if (minAndMaxAbs[1] < maxAbs) {
		minAndMaxAbs[1] = maxAbs;
	    }
	}
	minAndMaxForVariables.put(varAbs, minAndMaxAbs);

	/*
	 * Min/Max for Ordinate
	 */
	final String varOrd = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		.getOrdinatePhysicalVar().getLabel();
	final double minOrd = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		.getMinOrdinatePhysVal();
	final double maxOrd = chartPanelWithComboBox.getScoop3ChartPanel().getjScoop3ChartScrollPane()
		.getMaxOrdinatePhysVal();
	double[] minAndMaxOrd;
	if (!minAndMaxForVariables.containsKey(varOrd)) {
	    minAndMaxOrd = new double[] { minOrd, maxOrd };
	} else {
	    minAndMaxOrd = minAndMaxForVariables.get(varOrd);
	    if (minAndMaxOrd[0] > minOrd) {
		minAndMaxOrd[0] = minOrd;
	    }
	    if (minAndMaxOrd[1] < maxOrd) {
		minAndMaxOrd[1] = maxOrd;
	    }
	}
	minAndMaxForVariables.put(varOrd, minAndMaxOrd);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.DataOrReferenceViewController#updateMouseCursorForGraphs(java.awt.Cursor)
     */
    @Override
    protected void updateMouseCursorForGraphs(final Cursor newCursor) {
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    chartPanelWithComboBox.setCursor(newCursor);
	}
    }

    public boolean isForceUpdateChartPanel() {
	return forceUpdateChartPanel;
    }

    public void setForceUpdateChartPanel(final boolean forceUpdateChartPanel) {
	this.forceUpdateChartPanel = forceUpdateChartPanel;
    }

    @Override
    public void validateUpdates() {
	// empty method
    }

    @Override
    public void cancelUpdates() {
	// empty method
    }

    @Override
    public ArrayList<ChartPanelWithComboBox> getListGraphs() {
	return this.listGraphs;
    }

    public ArrayList<String> getParamOfDifferentGraphs() {
	return this.paramOfDifferentGraphs;
    }

    @Override
    public boolean isRemovingMeasure() {
	return removingMeasure;
    }

    @Override
    public void setRemovingMeasure(final boolean b) {
	this.removingMeasure = b;
    }

    public static List<String> getVariableListComboBoxes() {
	return variableListComboBoxes;
    }

    public void setVariableListComboBoxes(final List<String> variableListComboBoxes) {
	this.variableListComboBoxes = variableListComboBoxes;
    }

    public void resetFilter() {
	getDataViewImpl().getDisplayQcButtonList().clear();
	getDataViewImpl().getExcludeQcButtonList().clear();
    }

    public void sortParameters(final String parametersOrder, final ArrayList<String> parametersCombo,
	    final ArrayList<String> parametersWithUnitCombo, final ArrayList<String> parametersName,
	    final Map<String, String> parametersNameWithUnit, final ArrayList<Integer> parametersIndex) {
	// Get the params order in array
	final String[] paramsOrderSplitted = parametersOrder == null ? new String[0] : parametersOrder.split(" ");

	// index of the next param insertion
	int insertionParamIndex = 0;
	for (int index = 0; index < paramsOrderSplitted.length; index++) {
	    if (parametersName.contains(paramsOrderSplitted[index])) {

		// get the position of the param in the current order to sort
		final int paramIndex = parametersName.indexOf(paramsOrderSplitted[index]);

		// add in the new position
		parametersName.add(insertionParamIndex, parametersName.get(paramIndex));
		parametersCombo.add(insertionParamIndex, parametersCombo.get(paramIndex));
		parametersWithUnitCombo.add(insertionParamIndex, parametersWithUnitCombo.get(paramIndex));
		parametersIndex.add(insertionParamIndex, parametersIndex.get(paramIndex));

		// remove the old position (+1 because we shifted the param before his old position)
		parametersName.remove(paramIndex + 1);
		parametersCombo.remove(paramIndex + 1);
		parametersWithUnitCombo.remove(paramIndex + 1);
		parametersIndex.remove(paramIndex + 1);

		insertionParamIndex++;
	    }
	}
    }

    public void removeCycleNumberFromComboBox() {
	// remove Cycle Number from comboBox
	if ((getDataViewImpl().xComboBox != null) && (getDataViewImpl().yComboBox != null)
		&& (getDataViewImpl().zComboBox != null) && (getDataViewImpl().colorMapComboBox != null)
		&& (getDataViewImpl().type3d != Type3d.NONE)
		&& (getCommonViewModel().getDataset().getDatasetType() == DatasetType.PROFILE)) {
	    getDataViewImpl().xComboBox.removeItem(DataFrame3D.CYCLE_NUMBER);
	    getDataViewImpl().yComboBox.removeItem(DataFrame3D.CYCLE_NUMBER);
	    getDataViewImpl().zComboBox.removeItem(DataFrame3D.CYCLE_NUMBER);
	    getDataViewImpl().colorMapComboBox.removeItem(DataFrame3D.CYCLE_NUMBER);
	}
    }

    public void addCycleNumberIntoComboBox() {
	// add Cycle Number into comboBox
	if ((getDataViewImpl().xComboBox != null) && (getDataViewImpl().yComboBox != null)
		&& (getDataViewImpl().zComboBox != null) && (getDataViewImpl().colorMapComboBox != null)
		&& (getDataViewImpl().type3d != Type3d.NONE)
		&& (getCommonViewModel().getDataset().getDatasetType() == DatasetType.PROFILE)) {
	    // check if there is already the variable Cycle_number
	    boolean cycleNumberExisting = false;
	    for (int i = 0; i < getDataViewImpl().xComboBox.getItemCount(); i++) {
		final String item = getDataViewImpl().xComboBox.getItemAt(i);
		if (item.equals(DataFrame3D.CYCLE_NUMBER)) {
		    cycleNumberExisting = true;
		}
	    }

	    if (!cycleNumberExisting) {
		int index = getDataViewImpl().xComboBox.getItemCount() - 1;
		// get the correct index where Cycle_number is inserted
		for (int i = 0; i < getDataViewImpl().xComboBox.getItemCount(); i++) {
		    final String item = getDataViewImpl().xComboBox.getItemAt(i);
		    if (item.equals(DataFrame3D.DATE_KEY)) {
			index = i;
			break;
		    }
		}
		getDataViewImpl().xComboBox.insertItemAt(DataFrame3D.CYCLE_NUMBER, index);
		getDataViewImpl().yComboBox.insertItemAt(DataFrame3D.CYCLE_NUMBER, index);
		getDataViewImpl().zComboBox.insertItemAt(DataFrame3D.CYCLE_NUMBER, index);
		getDataViewImpl().colorMapComboBox.insertItemAt(DataFrame3D.CYCLE_NUMBER, index);
	    }
	}
    }

    /**
     *
     * This method needs to be override.
     *
     * @return the Map which contains the Order of the Parameters (for each Observation).
     */
    protected Map<String, List<String>> getParametersOrderForObservations() {
	// Select property depending the datasetType
	String propertyName;
	DatasetType datasetType = getCommonViewModel().getDataset().getDatasetType();
	while (null == datasetType) {
	    datasetType = getCommonViewModel().getDataset().getDatasetType();
	}
	switch (datasetType) {
	case PROFILE:
	    if (FileConfig.getScoop3FileConfig().getString("gui.local-profile-order-for-graphs").trim().equals("")) {
		propertyName = "gui.profile-order-for-graphs";
		break;
	    } else {
		propertyName = "gui.local-profile-order-for-graphs";
		break;
	    }

	case TIMESERIE:
	    if (FileConfig.getScoop3FileConfig().getString("gui.local-timeserie-order-for-graphs").trim().equals("")) {
		propertyName = "gui.timeserie-order-for-graphs";
		break;
	    } else {
		propertyName = "gui.local-timeserie-order-for-graphs";
		break;
	    }

	case TRAJECTORY:
	    if (FileConfig.getScoop3FileConfig().getString("gui.local-trajectory-order-for-graphs").trim().equals("")) {
		propertyName = "gui.trajectory-order-for-graphs";
		break;
	    } else {
		propertyName = "gui.local-trajectory-order-for-graphs";
		break;
	    }

	default:
	    propertyName = "gui.default-order-for-graphs";
	    break;
	}

	// Get the params order
	final String paramsOrder = FileConfig.getScoop3FileConfig().getString(propertyName);
	final String[] paramsOrderSplitted = paramsOrder == null ? new String[0] : paramsOrder.split(" ");

	final Map<String, List<String>> parametersOrderForObservations = new HashMap<>();
	final List<Observation> tempObservationList = new ArrayList<>(
		getCommonViewModel().getDataset().getObservations());

	for (final Observation observation : tempObservationList) {
	    final ArrayList<String> parametersOrder = new ArrayList<>();

	    // Add defaultParamOrder
	    for (final String defaultParamOrder : paramsOrderSplitted) {
		if ((observation.getReferenceParameter().getCode().equals(defaultParamOrder)
			|| (observation.getOceanicParameter(defaultParamOrder) != null))
			&& (!parametersOrder.contains(defaultParamOrder))) {
		    parametersOrder.add(defaultParamOrder);
		}
	    }

	    // Sort OceanicParameters by alphabetic
	    final List<String> oceanicParameterList = new ArrayList<>(observation.getOceanicParameters().keySet());
	    // remove null element if present in oceanicParameterList
	    for (int index = oceanicParameterList.size() - 1; index >= 0; index--) {
		if (oceanicParameterList.get(index) == null) {
		    oceanicParameterList.remove(index);
		}
	    }
	    Collections.sort(oceanicParameterList);

	    // Add others OceanicParameters
	    for (final String paramName : oceanicParameterList) {
		if (!parametersOrder.contains(paramName)) {
		    parametersOrder.add(paramName);
		}
	    }

	    parametersOrderForObservations.put(observation.getReference(), parametersOrder);
	}

	return parametersOrderForObservations;
    }

    public void updateDataset() {
	// empty method
    }

    public void setNearestProfiles(final List<Profile> nearestProfiles) {
	this.nearestProfiles = nearestProfiles;
    }

    public List<Profile> getNearestProfiles() {
	return this.nearestProfiles;
    }

    public void createActionListeners() {
	// empty method
    }

    public void updateChartPanelAfterDivideTimeserie(final ChartPanelWithComboBox chartPanelWithComboBox,
	    final boolean forceDisplayAdditionalSeries, final List<Profile> nearestProfiles,
	    final ChartDataset chartDataset) {

	if ((this.nearestProfiles == null) && (nearestProfiles == null)) {
	    this.nearestProfiles = computeNearestProfiles();
	} else if ((this.nearestProfiles == null) && (nearestProfiles != null)) {
	    this.nearestProfiles = nearestProfiles;
	}

	final String selectedValue = chartPanelWithComboBox.getSelectedValue();
	final String absis = selectedValue.split(SEPARATOR_FOR_COMBO_LABELS)[1].trim();
	final String ord = selectedValue.split(SEPARATOR_FOR_COMBO_LABELS)[0].trim();

	// update the absis variable to update QCs before draw the chart
	updateChartDatasetForVariableDivideTimeserie(absis, null, chartDataset);

	// Retrieve the reference parameter
	final ChartPhysicalVariable referencePhysicalVariable = chartDataset.getPhysicalVariable(absis);

	// update the ord variable to update QCs before draw the chart
	updateChartDatasetForVariableDivideTimeserie(ord, null, chartDataset);

	// Retrieve the physical parameter
	ChartPhysicalVariable physicalVariable = chartDataset.getPhysicalVariable(ord);

	// if physicalVariable is null, take the first param in the chartDataset (to avoid nullPointerException)
	if (physicalVariable == null) {
	    physicalVariable = chartDataset.getPhysicalVariable(0);
	}

	checkFirstAndLastObservationIndexDivideTimeserie(chartDataset);

	// Create the Chart
	final JScoop3ChartPanelAbstract scoop3ChartPanel;
	scoop3ChartPanel = new JScoop3ChartPanelForTimeSerie(physicalVariable, referencePhysicalVariable,
		((getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
			|| (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET))
				? firstObservationIndex
				: getObservationNumber(),
		((getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
			|| (getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET))
				? lastObservationIndex
				: getObservationNumber(),
		getObservationNumber(), isTimeserieDivided());

	if ((displayedQCEnum != null)) {
	    switch (displayedQCEnum) {
	    case DATE_QC:
		final int[] timeQCs = getTimeQCs();
		((JScoop3ChartPanelForTimeSerie) scoop3ChartPanel).setQCToUse(Observation.TIME_VAR_NAME, timeQCs);
		break;
	    case POSITION_QC:
		final int[] posQCs = getPositionQCs();
		((JScoop3ChartPanelForTimeSerie) scoop3ChartPanel).setQCToUse(Observation.LATITUDE_VAR_NAME, posQCs);
		break;
	    case VALUES:
		((JScoop3ChartPanelForTimeSerie) scoop3ChartPanel).setQCToUse(null, null);
		break;
	    }
	}
	scoop3ChartPanel.getjScoop3ChartScrollPane().setCurrentStation(getObservationNumber());
	scoop3ChartPanel.getjScoop3ChartScrollPane().setCurrentLevel(levelIndex);

	// Update the Chart in the Panel
	chartPanelWithComboBox.updateJScoop3ChartPanel(scoop3ChartPanel, absis);

	updateAdditionalGraphsToDisplay(chartPanelWithComboBox, forceDisplayAdditionalSeries, this.nearestProfiles);

	if (listGraphs.size() > 1) {
	    // Try to set the JScrollPane as an other graph
	    final ChartPanelWithComboBox otherChartPanelWithComboBox = listGraphs.get(0) != chartPanelWithComboBox
		    ? listGraphs.get(0)
		    : listGraphs.get(1);

	    final JScoop3ChartScrollPaneAbstract otherJScoop3ChartScrollPane = otherChartPanelWithComboBox
		    .getScoop3ChartPanel().getjScoop3ChartScrollPane();

	    otherJScoop3ChartScrollPane.updateDataAreaForZoomLevelCurrent();

	    otherChartPanelWithComboBox.zoomOnDisplayArea(otherJScoop3ChartScrollPane.getDataAreaForZoomLevelOne(),
		    otherJScoop3ChartScrollPane.getDataAreaForZoomLevelCurrent(), null, null, true, null);

	    final int newValue = otherJScoop3ChartScrollPane.getReferenceScrollBar().getValue();
	    final int newExtent = otherJScoop3ChartScrollPane.getReferenceScrollBar().getModel().getExtent();
	    final int newMin = otherJScoop3ChartScrollPane.getReferenceScrollBar().getMinimum();
	    final int newMax = otherJScoop3ChartScrollPane.getReferenceScrollBar().getMaximum();
	    scoop3ChartPanel.getjScoop3ChartScrollPane().getReferenceScrollBar().setValues(newValue, newExtent, newMin,
		    newMax);
	    scoop3ChartPanel.getjScoop3ChartScrollPane().getReferenceScrollBar().setValue(newValue);
	    scoop3ChartPanel.getjScoop3ChartScrollPane().validate();
	    scoop3ChartPanel.getjScoop3ChartScrollPane().repaint();
	}

	updateMouseCursor();
    }

    public void updateChartDatasetForVariableDivideTimeserie(final String parameterCode, final Integer observationIndex,
	    final ChartDataset chartDataset) {
	final ChartPhysicalVariable physicalVariable = chartDataset.getPhysicalVariable(parameterCode);
	ArrayList<Parameter<? extends Number>> parameterList;
	if (!isSuperposedMode()) {
	    parameterList = getCommonViewModel().getParam(getCommonViewModel().getObservation(
		    observationIndex != null ? observationIndex : getObservationNumber()), parameterCode);
	    for (final Parameter<? extends Number> parameter : parameterList) {
		if ((physicalVariable != null) && (parameter != null) && (physicalVariable.getQcValuesByStation()
			.get(observationIndex != null ? observationIndex : getObservationNumber()).length == parameter
				.getQcValues().size())) {
		    physicalVariable.updateQCs(observationIndex != null ? observationIndex : getObservationNumber(),
			    QCValues.convertQCValuesListToCharArray(parameter.getQcValues()));
		}
	    }
	} else {
	    parameterList = getCommonViewModel().getParams(getCommonViewModel().getObservations(), parameterCode);
	    for (int i = 0; i < getCommonViewModel().getObservations().size(); i++) {
		final Parameter<? extends Number> parameter = parameterList.get(i);
		int[] parameterQcValues = null;
		if (parameter != null) {
		    parameterQcValues = new int[parameter.getQcValues().size()];
		    for (int index = 0; index < parameter.getQcValues().size(); index++) {
			if (parameter.getQcValues().get(index) != null) {
			    parameterQcValues[index] = parameter.getQcValues().get(index).getQCValue();
			} else {
			    parameterQcValues[index] = 9;
			}
		    }
		}
		try {
		    if ((physicalVariable != null) && (parameter != null) && (parameterQcValues != null)
			    && (physicalVariable.getQcValuesByStation().get(i).length == parameter.getQcValues().size())
			    && !Arrays.equals(parameterQcValues, physicalVariable.getQcValuesByStation().get(i))) {
			physicalVariable.updateQCs(i, QCValues.convertQCValuesListToCharArray(parameter.getQcValues()));
		    }
		} catch (final IndexOutOfBoundsException e) {
		    // may be useless after the realization of the mantis 50586
		    final UnhandledException exception = new UnhandledException(
			    "Nombre d'observations dans le dataset : " + getCommonViewModel().getObservations().size()
				    + " / paramètre : " + physicalVariable.getLabel()
				    + " / taille de QcValuesByStation pour le paramètre : "
				    + physicalVariable.getQcValuesByStation().size() + " / index i : " + i,
			    e);
		}
	    }
	}
    }

    private void checkFirstAndLastObservationIndexDivideTimeserie(final ChartDataset chartDataset) {
	// Display all observations (darker) + current observation
	if ((getSuperposedModeEnum() != null)
		&& (getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)) {
	    // Retrieve the reference parameter
	    final String reference = chartDataset.getReferenceLabel();
	    final ChartPhysicalVariable referencePhysicalVariable = chartDataset.getPhysicalVariable(reference);

	    firstObservationIndex = referencePhysicalVariable.getPhysicalValuesByStation().size() - 1;
	    lastObservationIndex = 0;
	    final String currentPlatformCode = referencePhysicalVariable.getPlatformsCodes()
		    .get(getObservationNumber());
	    for (int index = 0; index < referencePhysicalVariable.getPhysicalValuesByStation().size(); index++) {
		final String indexPlatformCode = referencePhysicalVariable.getPlatformsCodes().get(index);
		if ((getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET)
			|| ((getSuperposedModeEnum() != SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET)
				&& currentPlatformCode.equals(indexPlatformCode))) {
		    if (firstObservationIndex > index) {
			firstObservationIndex = index;
		    }
		    if (lastObservationIndex < index) {
			lastObservationIndex = index;
		    }
		}
	    }
	    if (firstObservationIndex > lastObservationIndex /* || (firstObservationIndex == lastObservationIndex) */) {
		firstObservationIndex = -1;
		lastObservationIndex = -1;
	    }
	}
    }

    public ChartDataset getDivideTimeserieChartDataset() {
	return divideTimeserieChartDataset;
    }

    public void setDivideTimeserieChartDataset(final ChartDataset divideTimeserieChartDataset) {
	this.divideTimeserieChartDataset = divideTimeserieChartDataset;
    }

    public boolean isTimeserieDivided() {
	return timeserieDivided;
    }

    public void setTimeserieDivided(final boolean timeserieDivided) {
	this.timeserieDivided = timeserieDivided;
    }

    public Integer getCurrentSectionNumberForTimeserie() {
	return currentSectionNumberForTimeserie;
    }

    public void setCurrentSectionNumberForTimeserie(final Integer currentSectionNumberForTimeserie) {
	this.currentSectionNumberForTimeserie = currentSectionNumberForTimeserie;
    }

    public Integer getTotalSectionNumberForTimeserie() {
	return totalSectionNumberForTimeserie;
    }

    public void setTotalSectionNumberForTimeserie(final Integer totalSectionNumberForTimeserie) {
	this.totalSectionNumberForTimeserie = totalSectionNumberForTimeserie;
    }

    public void divideTimeserie(final Integer sectionNumberForTimeserie, final Integer currentSectionNumberForTimeserie,
	    final boolean refreshGraph) {
	if ((sectionNumberForTimeserie != null) && (currentSectionNumberForTimeserie != null)
		&& !Dataset.getInstance().getObservations().isEmpty()) {
	    // create the chartDataset copy
	    final ChartDataset chartDataset = new ChartDataset();
	    chartDataset
		    .setReferenceLabel(((DataViewModel) getCommonViewModel()).getChartDataset().getReferenceLabel());
	    for (final ChartMetaVariable cmv : ((DataViewModel) getCommonViewModel()).getChartDataset()
		    .getMetaVariables()) {
		chartDataset.addMetaVariable(cmv);
	    }
	    for (final ChartPhysicalVariable cpv : ((DataViewModel) getCommonViewModel()).getChartDataset()
		    .getPhysicalVariables()) {
		// create a copy of each physical variable
		final ChartPhysicalVariable localCpv = new ChartPhysicalVariable(cpv.getLabel(), cpv.getUnit());
		localCpv.setIsADate(cpv.isADate());
		localCpv.setLevelParameter(cpv.isLevelParameter());
		localCpv.setReferenceParameter(cpv.isReferenceParameter());
		localCpv.setMaxLevel(cpv.getMaxLevel());
		localCpv.setMaxLevelDepth(cpv.getMaxLevelDepth());
		localCpv.setObservationsList(cpv.getObervationsList());
		localCpv.setPlatformsCodes(cpv.getPlatformsCodes());
		localCpv.setQcValuesByStation(cpv.getQcValuesByStation());

		final ArrayList<Double[]> localPhysicalValuesByStation = new ArrayList<>();
		for (final Double[] array : cpv.getPhysicalValuesByStation()) {
		    final Double[] localArray = Arrays.copyOf(array, array.length);
		    localPhysicalValuesByStation.add(localArray);
		}
		localCpv.setPhysicalValuesByStation(localPhysicalValuesByStation);

		chartDataset.addPhysicalVariable(localCpv);
	    }
	    setDivideTimeserieChartDataset(chartDataset);

	    // divide and get the wanted section of each parameter
	    for (final ChartPhysicalVariable cpv : chartDataset.getPhysicalVariables()) {
		final int valuesLength = cpv.getPhysicalValuesByStation().get(getObservationNumber()).length;
		final int partValuesLength = valuesLength / sectionNumberForTimeserie;
		for (int index = 0; index < valuesLength; index++) {
		    if ((!sectionNumberForTimeserie.equals(currentSectionNumberForTimeserie)
			    && ((index < (partValuesLength * (currentSectionNumberForTimeserie - 1)))
				    || (index >= (partValuesLength * currentSectionNumberForTimeserie))))
			    || (sectionNumberForTimeserie.equals(currentSectionNumberForTimeserie)
				    && (index < (partValuesLength * (currentSectionNumberForTimeserie - 1))))) {
			cpv.getPhysicalValuesByStation().get(getObservationNumber())[index] = null;
		    }
		}
	    }

	    // divide latitude and longitude values which are not physical parameters
	    final Observation currentObs = Dataset.getInstance().getObservations().get(getObservationNumber());

	    if ((fullLatitudeParameterValues != null) && (fullLongitudeParameterValues != null)) {
		currentObs.getLatitude().setValues(fullLatitudeParameterValues);
		currentObs.getLongitude().setValues(fullLongitudeParameterValues);
	    }

	    fullLatitudeParameterValues = new ArrayList<>(currentObs.getLatitude().getValues());
	    int valuesLength = currentObs.getLatitude().getValues().size();
	    int partValuesLength = valuesLength / sectionNumberForTimeserie;
	    for (int index = 0; index < valuesLength; index++) {
		if ((!sectionNumberForTimeserie.equals(currentSectionNumberForTimeserie)
			&& ((index < (partValuesLength * (currentSectionNumberForTimeserie - 1)))
				|| (index >= (partValuesLength * currentSectionNumberForTimeserie))))
			|| (sectionNumberForTimeserie.equals(currentSectionNumberForTimeserie)
				&& (index < (partValuesLength * (currentSectionNumberForTimeserie - 1))))) {
		    currentObs.getLatitude().getValues().set(index, null);
		}
	    }

	    fullLongitudeParameterValues = new ArrayList<>(currentObs.getLongitude().getValues());
	    valuesLength = currentObs.getLongitude().getValues().size();
	    partValuesLength = valuesLength / sectionNumberForTimeserie;
	    for (int index = 0; index < valuesLength; index++) {
		if ((!sectionNumberForTimeserie.equals(currentSectionNumberForTimeserie)
			&& ((index < (partValuesLength * (currentSectionNumberForTimeserie - 1)))
				|| (index >= (partValuesLength * currentSectionNumberForTimeserie))))
			|| (sectionNumberForTimeserie.equals(currentSectionNumberForTimeserie)
				&& (index < (partValuesLength * (currentSectionNumberForTimeserie - 1))))) {
		    currentObs.getLongitude().getValues().set(index, null);
		}
	    }

	    setTimeserieDivided(true);

	    if (refreshGraph) {
		// update each graph with the new chart dataset
		for (final ChartPanelWithComboBox chartPanelWithComboBox : getListGraphs()) {
		    updateChartPanelAfterDivideTimeserie(chartPanelWithComboBox, true, null, chartDataset);
		    // chartPanelWithComboBox.updateSectionLabel();
		    chartPanelWithComboBox.zoomAll();
		}
	    }
	}

    }

    public boolean isAskForValidation() {
	return askForValidation;
    }

    public void setAskForValidation(final boolean askForValidation) {
	this.askForValidation = askForValidation;
    }

    public ArrayList<Double> getFullLatitudeParameterValues() {
	return fullLatitudeParameterValues;
    }

    public void setFullLatitudeParameterValues(final ArrayList<Double> fullLatitudeParameterValues) {
	this.fullLatitudeParameterValues = fullLatitudeParameterValues;
    }

    public ArrayList<Double> getFullLongitudeParameterValues() {
	return fullLongitudeParameterValues;
    }

    public void setFullLongitudeParameterValues(final ArrayList<Double> fullLongitudeParameterValues) {
	this.fullLongitudeParameterValues = fullLongitudeParameterValues;
    }
}
