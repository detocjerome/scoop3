package fr.ifremer.scoop3.gui.data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.bushe.swing.event.EventBus;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

import climato.Climatology.CLIMATOLOGY_ID;
import climato.ClimatologyFactory;
import fr.ifremer.scoop3.bathyClimato.BathyClimatologyManager;
import fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane;
import fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane.MouseMode;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.controller.worflow.StepCode;
import fr.ifremer.scoop3.controller.worflow.SubStep;
import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.data.SuperposedModeEnum;
import fr.ifremer.scoop3.events.GuiEventChangeMainPanelToStep;
import fr.ifremer.scoop3.gui.bathyClimato.SC3_Climato.CLIMATO_ENUM;
import fr.ifremer.scoop3.gui.common.CommonViewImpl;
import fr.ifremer.scoop3.gui.common.MetadataSplitPane;
import fr.ifremer.scoop3.gui.common.MetadataSplitPane.InfoInObservationSubPanel;
import fr.ifremer.scoop3.gui.common.MetadataTable;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent.EVENT_ENUM;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyCommentChangeEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyIsCheckedChangeEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyQCChangeEvent;
import fr.ifremer.scoop3.gui.core.Scoop3Frame;
import fr.ifremer.scoop3.gui.data.commandButton.ChangeGraphsNumberCommandButton;
import fr.ifremer.scoop3.gui.data.commandButton.SelectAndChangeQCCommandButton;
import fr.ifremer.scoop3.gui.jzy3dManager.Jzy3dManager;
import fr.ifremer.scoop3.gui.jzy3dManager.Jzy3dManager.DataFrameType;
import fr.ifremer.scoop3.gui.jzy3dManager.Jzy3dManager.Type3d;
import fr.ifremer.scoop3.gui.utils.PlotGraph3DException;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.model.Dataset;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.Observation;
import fr.ifremer.scoop3.model.QCValues;

/**
 *
 *
 */
public abstract class DataViewImpl extends CommonViewImpl {

    private class ClimatoJComboBox extends JComboBox<CLIMATO_ENUM> {

	private static final long serialVersionUID = -4010246203082169315L;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ClimatoJComboBox(final CLIMATO_ENUM[] availableClimatoArray) {
	    super(availableClimatoArray);

	    /*
	     * ADD TOOLTIP
	     */
	    setRenderer(new BasicComboBoxRenderer() {
		private static final long serialVersionUID = 8088792832251894286L;

		/*
		 * (non-Javadoc)
		 *
		 * @see javax.swing.plaf.basic.BasicComboBoxRenderer# getListCellRendererComponent(javax.swing.JList,
		 * java.lang.Object, int, boolean, boolean)
		 */
		@Override
		public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean isSelected, final boolean cellHasFocus) {
		    final Component component = super.getListCellRendererComponent(list, value, index, isSelected,
			    cellHasFocus);
		    if (isSelected && (-1 < index)) {
			list.setToolTipText(((CLIMATO_ENUM) value).getLabel());
		    }
		    return component;
		}

	    });

	    /*
	     * Add listener
	     */
	    addActionListener((final ActionEvent e) -> {
		if (propertyChangeSupport != null) {
		    propertyChangeSupport.firePropertyChange(
			    new SC3PropertyChangeEvent(getSelectedItem(), EVENT_ENUM.DISPLAY_STATISTICS));
		}
	    });
	}
    }

    // # 55265 KeepBoundsCheckbox problem
    // public class KeepBoundsJCheckbox extends JCheckBox {
    // private static final long serialVersionUID = 2976963266530976527L;
    //
    // public KeepBoundsJCheckbox() {
    // super(Messages.getMessage("bpc-gui.ribbon-displayed-info.keep-bounds"));
    //
    // if (dataset.getDatasetType() != DatasetType.PROFILE) {
    // setEnabled(false);
    // }
    //
    // /*
    // * Add listener
    // */
    // addActionListener((final ActionEvent e) -> {
    // if (propertyChangeSupport != null) {
    // propertyChangeSupport
    // .firePropertyChange(new SC3PropertyChangeEvent(isSelected(), EVENT_ENUM.KEEP_BOUNDS));
    // }
    // });
    // }
    // }

    public static final int DISPLAY_LINE_ON_GRAPH = 2;
    public static final int DISPLAY_POINTS = 0;
    public static final int DISPLAY_POINTS_AND_CIRLCE = 1;

    public static int profileGraphsCount;
    public static int profileGraphsCountGroup;
    public static int timeserieGraphsCount;
    public static int timeserieGraphsCountGroup;

    private List<ChangeGraphsNumberCommandButton> changeGraphs;
    /**
     * Scroll panel for charts
     */
    private JPanel chartsPanel;
    private JCommandButton dataTableButton;
    private boolean displayCircle = false;
    private boolean displayLine = true;
    private JCommandButton displayLineButton;
    private ResizableIcon displayLineIcon;
    private ResizableIcon displayLineIconGreyed;
    private boolean displayPoints = true;
    private JCommandButton displayPointsAndCircleButton;
    private ResizableIcon displayPointsAndCircleIcon;
    private ResizableIcon displayPointsAndCircleIconGreyed;
    private JCommandButton displayPointsButton;
    private ResizableIcon displayPointsIcon;
    private ResizableIcon displayPointsIconGreyed;
    private JLabel infoLabel;
    private JPanel infoPanel;
    private List<ChartPanelWithComboBox> listGraphs = new ArrayList<>();
    private int mouseSubModeQC = -128;
    protected JCommandButton saveAllGraphsButton;
    private SelectAndChangeQCCommandButton selectAndChangeQCCommandButton;
    private List<SelectAndChangeQCCommandButton> selectAndChangeQCs;
    protected JCommandButton zoomInButton;
    protected JCommandButton zoomInitialButton;
    protected JCommandButton zoomOutButton;
    protected int graphGroupAddGraphSplitType;
    protected int graphGroupAddGroupSplitType;
    protected JCommandButton mouseModeSelectionButton;
    protected JCommandButton mouseModeZoomButton;
    // protected JCommandButton mouseModePrecisionZoomButton;
    protected List<Integer> displayQcButtonList;
    protected List<Integer> excludeQcButtonList;
    // Parametrage de l'affichage
    protected int nbMaxGraphByGroup;
    private JComboBox<CLIMATO_ENUM> climatojComboBox;
    // # 55265 KeepBoundsCheckbox problem
    // private KeepBoundsJCheckbox keepBoundsCheckbox;
    private int climatoStd = -1;
    private int climatoDMean = -1;
    private static int spinnerClimato1MaxValue = 100;
    private static int spinnerClimato2MaxValue = 1000;
    /* Ajout d'un ribbon button pour la 3D */
    protected JComboBox<String> xComboBox;
    protected JComboBox<String> yComboBox;
    protected JComboBox<String> zComboBox;
    protected JComboBox<String> colorMapComboBox;
    private Jzy3dManager jzy3dManager;
    private final List<JPanel> panelList = new ArrayList<JPanel>(); // liste qui va stocker les différents panels (2d et
								    // 3d)
    private final List<Component> tempList = new ArrayList<Component>(); // liste servant a stocker les graphs 2d après
									 // chaque utilisation des graphs 3d
    private static Color defaultColorBoxColor = new Color(238, 238, 238);
    private static Color offColorBoxColor = new Color(150, 150, 150);
    private JPanel graphs3DPanel;
    private static int comboBoxesGraphs3DPanelHeight = 35;
    private String xParameter = null;
    private String yParameter = null;
    private String zParameter = null;
    private String colorParameter = null;
    protected Type3d type3d;
    protected List<String> sortedKeyset;

    static {
	try {
	    if (FileConfig.getScoop3FileConfig().getString("gui.data-view.local-profile-graphs-count").trim()
		    .equals("")) {
		profileGraphsCount = Integer
			.parseInt(FileConfig.getScoop3FileConfig().getString("bpc-gui.data-view.profile-graphs-count"));
	    } else {
		profileGraphsCount = Integer.parseInt(
			FileConfig.getScoop3FileConfig().getString("gui.data-view.local-profile-graphs-count").trim());
	    }
	} catch (final NumberFormatException nfe) {
	    profileGraphsCount = 3;
	}

	try {
	    if (FileConfig.getScoop3FileConfig().getString("gui.data-view.local-timeserie-graphs-count").trim()
		    .equals("")) {
		timeserieGraphsCount = Integer.parseInt(
			FileConfig.getScoop3FileConfig().getString("bpc-gui.data-view.timeserie-graphs-count"));
	    } else {
		timeserieGraphsCount = Integer.parseInt(FileConfig.getScoop3FileConfig()
			.getString("gui.data-view.local-timeserie-graphs-count").trim());
	    }
	} catch (final NumberFormatException nfe) {
	    timeserieGraphsCount = 4;
	}

	try {
	    profileGraphsCountGroup = Integer.parseInt(
		    FileConfig.getScoop3FileConfig().getString("bpc-gui.data-view.profile-graphs-number-per-line"));
	} catch (final NumberFormatException nfe) {
	    profileGraphsCountGroup = 3;
	}

	try {
	    timeserieGraphsCountGroup = Integer.parseInt(FileConfig.getScoop3FileConfig()
		    .getString("bpc-gui.data-view.timeserie-graphs-number-per-row").trim());
	} catch (final NumberFormatException nfe) {
	    timeserieGraphsCountGroup = 2;
	}
    }

    /**
     *
     */
    protected DataViewImpl(final Scoop3Frame scoop3Frame, final MetadataSplitPane metadataSplitPane,
	    final Dataset dataSet, final Report report) {
	super(scoop3Frame, metadataSplitPane, dataSet, report, STEP_TYPE.Q2_CONTROL_AUTO_DATA);
	initButtons();
	if (displayQcButtonList == null) {
	    displayQcButtonList = new ArrayList<Integer>();
	}
	if (excludeQcButtonList == null) {
	    excludeQcButtonList = new ArrayList<Integer>();
	}
    }

    /**
     *
     */
    protected DataViewImpl(final Scoop3Frame scoop3Frame, final MetadataTable datasetMetadatasTable,
	    final MetadataTable observationMetadatasTable, final Dataset dataset, final Report report) {
	super(scoop3Frame, datasetMetadatasTable, observationMetadatasTable, dataset, report,
		STEP_TYPE.Q2_CONTROL_AUTO_DATA);
	initButtons();
	if (displayQcButtonList == null) {
	    displayQcButtonList = new ArrayList<Integer>();
	}
	if (excludeQcButtonList == null) {
	    excludeQcButtonList = new ArrayList<Integer>();
	}
    }

    /**
     * The operator wants to change the points displayed on the graphs
     *
     * @param selectAndChangeQCCommandButton
     */
    public void displayOnlyQCCheckboxStateChanged(final SelectAndChangeQCCommandButton selectAndChangeQCCommandButton) {
	propertyChangeSupport.firePropertyChange(
		new SC3PropertyChangeEvent(selectAndChangeQCCommandButton, EVENT_ENUM.DISPLAY_ONLY_QC));
    }

    /**
     * The operator wants to change the points displayed on the graphs
     *
     * @param selectAndChangeQCCommandButton
     */
    public void excludeOnlyQCCheckboxStateChanged(final SelectAndChangeQCCommandButton selectAndChangeQCCommandButton) {
	propertyChangeSupport.firePropertyChange(
		new SC3PropertyChangeEvent(selectAndChangeQCCommandButton, EVENT_ENUM.EXCLUDE_ONLY_QC));
    }

    /**
     * @return the allProfilesButton
     */
    public JCommandButton getAllProfilesButton() {
	return null;
    }

    /**
     * @return the changeGraphs
     */
    public List<ChangeGraphsNumberCommandButton> getChangeGraphs() {
	return changeGraphs;
    }

    /**
     * @return the selected Climato
     */
    public CLIMATO_ENUM getClimatoSelected() {
	return (CLIMATO_ENUM) climatojComboBox.getSelectedItem();
    }

    /*
     *
     */
    public Dimension getChartsPanelDimension() {

	return ((chartsPanel != null) ? new Dimension(chartsPanel.getWidth(), chartsPanel.getHeight()) : null);

    }

    /**
     * @return the dataTableButton
     */
    public JCommandButton getDataTableButton() {
	return dataTableButton;
    }

    /**
     * @return the hidePointsButton
     */
    public JCommandButton getDisplayLineButton() {
	return displayLineButton;
    }

    /**
     * @return the displayPointsAndCircleButton
     */
    public JCommandButton getDisplayPointsAndCircleButton() {
	return displayPointsAndCircleButton;
    }

    /**
     * @return the displayPointsButton
     */
    public JCommandButton getDisplayPointsButton() {
	return displayPointsButton;
    }

    public int getGraphGroupAddGraphSplitType() {
	return graphGroupAddGraphSplitType;
    }

    public int getGraphGroupAddGroupSplitType() {
	return graphGroupAddGroupSplitType;
    }

    /**
     * @return the mouseModeSelectionButton
     */
    public synchronized JCommandButton getMouseModeSelectionButton() {
	return mouseModeSelectionButton;
    }

    /**
     * @return the mouseModeZoomButton
     */
    public synchronized JCommandButton getMouseModeZoomButton() {
	return mouseModeZoomButton;
    }

    // /**
    // * @return the mouseModeZoomButton
    // */
    // public synchronized JCommandButton getMouseModePrecisionZoomButton() {
    // return mouseModePrecisionZoomButton;
    // }

    /**
     * @return the mouseSubModeQC
     */
    public int getMouseSubModeQC() {
	return mouseSubModeQC;
    }

    public int getNbMaxGraphByGroup() {
	return nbMaxGraphByGroup;
    }

    /**
     * @return the saveAllGraphsButton
     */
    public JCommandButton getSaveAllGraphsButton() {
	return saveAllGraphsButton;
    }

    /**
     * @return the selectAndChangeQCs
     */
    public List<SelectAndChangeQCCommandButton> getSelectAndChangeQCs() {
	return selectAndChangeQCs;
    }

    /**
     * @return the superposedModeJComboBox
     */
    public SuperposedModeJComboBox getSuperposedModeJComboBox() {
	// implemented in child class
	return null;
    }

    /**
     * @return the zoomInButton
     */
    public JCommandButton getZoomInButton() {
	return zoomInButton;
    }

    /**
     * @return the zoomInitialButton
     */
    public JCommandButton getZoomInitialButton() {
	return zoomInitialButton;
    }

    /**
     * @return the zoomOutButton
     */
    public JCommandButton getZoomOutButton() {
	return zoomOutButton;
    }

    /**
     * Called when the current observation is changed
     *
     * @param oldObservationIndex
     * @param newObservationIndex
     */
    public void observationNumberChanged(final int oldObservationIndex, final int newObservationIndex) {
	// for reset the display or exclude Qcs list
	// updateDisplayOnlyQCOnGraph(null);
	// updateExcludeOnlyQCOnGraph(null);
    }

    public void setGraphGroupAddGraphSplitType(final int graphGroupAddGraphSplitType) {
	this.graphGroupAddGraphSplitType = graphGroupAddGraphSplitType;
    }

    public void setGraphGroupAddGroupSplitType(final int graphGroupAddGroupSplitType) {
	this.graphGroupAddGroupSplitType = graphGroupAddGroupSplitType;
    }

    /**
     * @param qc
     *            the mouseSubModeQC to set
     */
    public void setMouseSubModeQC(final int qc) {
	mouseSubModeQC = qc;
    }

    public void setNbMaxGraphByGroup(final int nbMaxGraphByGroup) {
	this.nbMaxGraphByGroup = nbMaxGraphByGroup;
    }

    /**
     * @param superposedModeEnum
     *            the displayStationType to set
     */
    public void setSuperposedModeEnum(final SuperposedModeEnum superposedModeEnum, final boolean stopActionListener) {
	// implemented in child class
    }

    /**
     * Toggle the map in Full Screen Mode
     *
     * @return
     */
    public boolean toggleFullScreenForMap() {
	// TODO FIX FULLL MAP WITH CONSOL
	final int currentLocationForGlobalWestJSplitPane = getGlobalWestJSplitPane().getDividerLocation();
	final int currentLocationForMainJSplitPane = getMainJSplitPane().getDividerLocation();
	if ((currentLocationForGlobalWestJSplitPane > 1) || (currentLocationForMainJSplitPane < 1)) {
	    // MAXIMIZE
	    getGlobalWestJSplitPane().setDividerLocation(0);
	    getGlobalWestJSplitPane().setLastDividerLocation(currentLocationForGlobalWestJSplitPane);

	    getMainJSplitPane().setDividerLocation(1d);
	    getMainJSplitPane().setLastDividerLocation(currentLocationForMainJSplitPane);

	    return true;
	} else {
	    getGlobalWestJSplitPane().setDividerLocation(getGlobalWestJSplitPane().getLastDividerLocation());
	    getGlobalWestJSplitPane().setLastDividerLocation(-1);

	    getMainJSplitPane().setDividerLocation(getMainJSplitPane().getLastDividerLocation());
	    getMainJSplitPane().setLastDividerLocation(-1);

	    return false;
	}
    }

    @Override
    public void updateAllProfilesIcons(final boolean allProfileIsActive) {
	// implemented in child class
    }

    /**
     * Update the Chart Panel with a new Scoop3CharlPanel
     *
     * @param newlistGraphs
     * @param isThereOnlyReferenceParam
     */
    public void updateChartPanel(final ArrayList<ChartPanelWithComboBox> newlistGraphs,
	    final boolean isThereOnlyReferenceParam, final boolean isProfileCharts) {

	/* unload data to save memory */
	if (listGraphs != null) {
	    specificPrepareForDispose(false);
	}

	/* Add a panel into the east panel to view charts */
	chartsPanel = new JPanel();
	chartsPanel.setLayout(new BorderLayout());
	chartsPanel.setBackground(Color.white);

	/* jdetoc Replace by gridBagLayout */
	/*
	 * chartsPanelScrollPane = new JScrollPane(chartsPanel);
	 * chartsPanelScrollPane.setHorizontalScrollBarPolicy(JScrollPane. HORIZONTAL_SCROLLBAR_AS_NEEDED);
	 * chartsPanelScrollPane.setVerticalScrollBarPolicy(JScrollPane. VERTICAL_SCROLLBAR_AS_NEEDED);
	 * getEastPanel().add(chartsPanelScrollPane, BorderLayout.CENTER);
	 */

	/**
	 * Add graphs
	 *
	 */
	listGraphs = newlistGraphs;

	if (listGraphs.isEmpty()) {

	    final String labelTxt = (isThereOnlyReferenceParam)
		    ? "controller.automatic-control-check-parameters-presence.only-reference-parameter"
		    : "gui.no-reference-parameter";
	    chartsPanel.add(new JLabel(Messages.getMessage(labelTxt), JLabel.CENTER), BorderLayout.CENTER);
	} else {
	    /* Compute one chartPanelWithComboBox dimension */
	    // final int chartPanelWithComboBoxWidth = (int) Math
	    // .floor(this.getChartsPanelDimension().width / nbGraphARow);
	    // final int chartPanelWithComboBoxHeight = (int)
	    // Math.floor(this.getChartsPanelDimension().height
	    // / nbGraphRows);

	    /* GridBagConstraint */
	    final GridBagConstraints gbc = new GridBagConstraints();
	    gbc.gridx = gbc.gridy = 0; // la grille commence en (0, 0)
	    // gbc.fill=GridBagConstraints.BOTH;

	    /* Ajout des graphs au panneau d'affichage */

	    // Calcul du nombre de groupe (ligne ou colonne) et du nombre de
	    // graphe par ligne
	    final int nbGroups = (int) Math.ceil((float) listGraphs.size() / this.nbMaxGraphByGroup);
	    final int nbGraphAGroup = (int) Math.ceil((float) listGraphs.size() / nbGroups);
	    double curNbGraphAddToCurGroup = 0;

	    // JSplitPane pour séparation des graphiques d'un groupe
	    JSplitPane curChartJSplitPane = new JSplitPane(this.getGraphGroupAddGraphSplitType(), false);
	    curChartJSplitPane.setOneTouchExpandable(true);
	    curChartJSplitPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	    curChartJSplitPane.setBackground(Color.white);

	    // JSplitPane initial pour séparation des groupes
	    JSplitPane curGroupChartJSplitPane = new JSplitPane(this.getGraphGroupAddGroupSplitType(), false);
	    curGroupChartJSplitPane.setOneTouchExpandable(true);

	    for (int graphsIndex = listGraphs.size() - 1; graphsIndex >= 0; graphsIndex--) {
		final ChartPanelWithComboBox chartPanelWithComboBox = listGraphs.get(graphsIndex);

		if (curChartJSplitPane.getRightComponent() == null) {
		    // Empilement a la premiere place du splitPaneCourant
		    curChartJSplitPane.setRightComponent(chartPanelWithComboBox);
		} else {
		    // graph number is even
		    if ((listGraphs.size() % 2) == 0) {
			// if last graph of 3 (the left one), create a new
			// JSplitPane with the graph and a blank panel
			if ((nbGraphAGroup == 3) && ((graphsIndex % 3) == 0)) {
			    final JSplitPane curLeftChartJSplitPane = new JSplitPane(
				    this.getGraphGroupAddGraphSplitType(), false, null, null);

			    final JPanel invisiblePanel = new JPanel();
			    // need a dimension and setInvisible
			    invisiblePanel.setPreferredSize(new Dimension(100, 100));
			    invisiblePanel.setVisible(false);

			    curLeftChartJSplitPane.setRightComponent(invisiblePanel);
			    curLeftChartJSplitPane.setLeftComponent(chartPanelWithComboBox);
			    curLeftChartJSplitPane.setDividerSize(0);

			    curChartJSplitPane.setLeftComponent(curLeftChartJSplitPane);
			    final int location = getDividerLocation(curChartJSplitPane, nbGraphAGroup);
			    curChartJSplitPane.setDividerLocation(location);
			    // Nouveau splitPane avec les graphes précédent a la
			    // premiere place et une place libre
			    curChartJSplitPane = new JSplitPane(this.getGraphGroupAddGraphSplitType(), false, null,
				    curChartJSplitPane);
			    curChartJSplitPane.setOneTouchExpandable(true);
			    curChartJSplitPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			    curChartJSplitPane.setBackground(Color.white);
			} else {
			    // Empilement a la derniere place du
			    // splitPaneCourant
			    curChartJSplitPane.setLeftComponent(chartPanelWithComboBox);
			    final int location = getDividerLocation(curChartJSplitPane, nbGraphAGroup);
			    curChartJSplitPane.setDividerLocation(location);
			    // Nouveau splitPane avec les graphes précédent a la
			    // premiere place et une place libre
			    curChartJSplitPane = new JSplitPane(this.getGraphGroupAddGraphSplitType(), false, null,
				    curChartJSplitPane);
			    curChartJSplitPane.setOneTouchExpandable(true);
			    curChartJSplitPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			    curChartJSplitPane.setBackground(Color.white);
			}
		    }
		    // graph number is odd
		    else {
			// if last graph of 3 (the left one), create a new
			// JSplitPane with the graph and a blank panel
			if ((nbGraphAGroup == 3) && (graphsIndex == (listGraphs.size() - 3))) {
			    final JSplitPane curLeftChartJSplitPane = new JSplitPane(
				    this.getGraphGroupAddGraphSplitType(), false, null, null);

			    final JPanel invisiblePanel = new JPanel();
			    // need a dimension and setInvisible
			    invisiblePanel.setPreferredSize(new Dimension(100, 100));
			    invisiblePanel.setVisible(false);

			    curLeftChartJSplitPane.setRightComponent(invisiblePanel);
			    curLeftChartJSplitPane.setLeftComponent(chartPanelWithComboBox);
			    curLeftChartJSplitPane.setDividerSize(0);

			    curChartJSplitPane.setLeftComponent(curLeftChartJSplitPane);
			    final int location = getDividerLocation(curChartJSplitPane, nbGraphAGroup);
			    curChartJSplitPane.setDividerLocation(location);
			    // Nouveau splitPane avec les graphes précédent a la
			    // premiere place et une place libre
			    curChartJSplitPane = new JSplitPane(this.getGraphGroupAddGraphSplitType(), false, null,
				    curChartJSplitPane);
			    curChartJSplitPane.setOneTouchExpandable(true);
			    curChartJSplitPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			    curChartJSplitPane.setBackground(Color.white);
			} else {
			    // Empilement a la derniere place du
			    // splitPaneCourant
			    curChartJSplitPane.setLeftComponent(chartPanelWithComboBox);
			    final int location = getDividerLocation(curChartJSplitPane, nbGraphAGroup);
			    curChartJSplitPane.setDividerLocation(location);
			    // Nouveau splitPane avec les graphes précédent a la
			    // premiere place et une place libre
			    curChartJSplitPane = new JSplitPane(this.getGraphGroupAddGraphSplitType(), false, null,
				    curChartJSplitPane);
			    curChartJSplitPane.setOneTouchExpandable(true);
			    curChartJSplitPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			    curChartJSplitPane.setBackground(Color.white);
			}
		    }
		}
		curNbGraphAddToCurGroup++;

		// Ajout d'un groupe si nécessaire
		if (curNbGraphAddToCurGroup >= nbGraphAGroup) {

		    if (curGroupChartJSplitPane.getRightComponent() == null) {
			// Empilement a la premiere place du splitPaneCourant.
			// Il faut enlever la place libre
			curGroupChartJSplitPane.setRightComponent(curChartJSplitPane.getRightComponent());
		    } else {
			// Empilement a la derniere place du splitPaneCourant.
			// Il faut enlever la place libre
			curGroupChartJSplitPane.setLeftComponent(curChartJSplitPane.getRightComponent());
			final int location = getDividerLocation(curGroupChartJSplitPane, nbGroups);
			curGroupChartJSplitPane.setDividerLocation(location);
			// Nouveau splitPane avec les groupes précédents a la
			// premiere place et une place libre
			curGroupChartJSplitPane = new JSplitPane(this.getGraphGroupAddGroupSplitType(), false, null,
				curGroupChartJSplitPane);
			curGroupChartJSplitPane.setOneTouchExpandable(true);
		    }

		    curNbGraphAddToCurGroup = 0;
		}

		/*
		 * if (isProfileCharts) { gbc.gridx++; } else { if (gbc.gridx == 1) { gbc.gridx = 0; gbc.gridy++; } else
		 * { gbc.gridx++; } }
		 */
	    }

	    // Ajout du groupe reliquat si existant
	    if (curNbGraphAddToCurGroup > 0) {
		if (curGroupChartJSplitPane.getRightComponent() == null) {
		    // Empilement a la premiere place du splitPaneCourant
		    curGroupChartJSplitPane.setRightComponent(curChartJSplitPane.getRightComponent());
		} else {
		    // Empilement a la derniere place du splitPaneCourant
		    curGroupChartJSplitPane.setLeftComponent(curChartJSplitPane.getRightComponent());
		    final int location = getDividerLocation(curGroupChartJSplitPane, nbGroups);
		    curGroupChartJSplitPane.setDividerLocation(location);
		}
	    }

	    // Il faut dépiler la dernier groupe libre
	    chartsPanel.add(curGroupChartJSplitPane.getLeftComponent() == null
		    ? curGroupChartJSplitPane.getRightComponent() : curGroupChartJSplitPane, BorderLayout.CENTER);
	}
	getEastPanel().add(chartsPanel, BorderLayout.CENTER);
	getEastPanel().revalidate();
	getEastPanel().repaint();
    }

    public void updateCommentErrorMessage(final String obsRef, final String variableName, final Number variableValue,
	    final String variableValueStr, final int refLevel, final String refValStr, final String errorMessage,
	    final String newValue, final String oldValue) {
	propertyChangeSupport.firePropertyChange(new SC3PropertyCommentChangeEvent(obsRef, variableName, variableValue,
		variableValueStr, refLevel, refValStr, errorMessage, newValue, oldValue));
    }

    /**
     * @param selectAndChangeQCCommandButton
     */
    public void updateDisplayOnlyQCOnGraph(final SelectAndChangeQCCommandButton selectAndChangeQCCommandButton) {
	if ((this.selectAndChangeQCCommandButton == selectAndChangeQCCommandButton)
		&& (this.selectAndChangeQCCommandButton != null)
		&& (selectAndChangeQCCommandButton.getExcludeOnlyQCCheckboxValue() == -1)) {
	    this.selectAndChangeQCCommandButton = null;
	} else {
	    if (selectAndChangeQCCommandButton != null) {
		if ((this.selectAndChangeQCCommandButton != null)
			&& (selectAndChangeQCCommandButton.getDisplayOnlyQCCheckboxValue() != -1)) {
		    // do nothing
		} else if (selectAndChangeQCCommandButton.getDisplayOnlyQCCheckboxValue() == -1) {
		    this.selectAndChangeQCCommandButton = null;
		} else {
		    this.selectAndChangeQCCommandButton = selectAndChangeQCCommandButton;
		}
	    }
	}

	if (displayQcButtonList == null) {
	    displayQcButtonList = new ArrayList<Integer>();
	}

	if (selectAndChangeQCCommandButton != null) {
	    if (displayQcButtonList.contains(selectAndChangeQCCommandButton.getQc())) {
		for (int j = 0; j < displayQcButtonList.size(); j++) {
		    if (displayQcButtonList.get(j) == selectAndChangeQCCommandButton.getQc()) {
			displayQcButtonList.remove(j);
		    }
		}
	    } else {
		displayQcButtonList.add(selectAndChangeQCCommandButton.getQc());
	    }
	}

	excludeQcButtonList = new ArrayList<Integer>();
	JScoop3ChartScrollPaneAbstract.setDisplayOnlyQCOnGraph(displayQcButtonList);
	JScoop3ChartScrollPaneAbstract.setExcludeOnlyQCOnGraph(excludeQcButtonList);
    }

    /**
     * @param selectAndChangeQCCommandButton
     */
    public void updateExcludeOnlyQCOnGraph(final SelectAndChangeQCCommandButton selectAndChangeQCCommandButton) {
	if ((this.selectAndChangeQCCommandButton == selectAndChangeQCCommandButton)
		&& (this.selectAndChangeQCCommandButton != null)
		&& (selectAndChangeQCCommandButton.getDisplayOnlyQCCheckboxValue() == -1)) {
	    this.selectAndChangeQCCommandButton = null;
	} else {
	    if (selectAndChangeQCCommandButton != null) {
		if ((this.selectAndChangeQCCommandButton != null)
			&& (selectAndChangeQCCommandButton.getExcludeOnlyQCCheckboxValue() != -1)) {
		    // do nothing
		} else if (selectAndChangeQCCommandButton.getExcludeOnlyQCCheckboxValue() == -1) {
		    this.selectAndChangeQCCommandButton = null;
		} else {
		    this.selectAndChangeQCCommandButton = selectAndChangeQCCommandButton;
		}
	    }
	}

	if (excludeQcButtonList == null) {
	    excludeQcButtonList = new ArrayList<Integer>();
	}

	if (selectAndChangeQCCommandButton != null) {
	    if (excludeQcButtonList.contains(selectAndChangeQCCommandButton.getQc())) {
		for (int j = 0; j < excludeQcButtonList.size(); j++) {
		    if (excludeQcButtonList.get(j) == selectAndChangeQCCommandButton.getQc()) {
			excludeQcButtonList.remove(j);
		    }
		}
	    } else {
		excludeQcButtonList.add(selectAndChangeQCCommandButton.getQc());
	    }
	}

	displayQcButtonList = new ArrayList<Integer>();
	JScoop3ChartScrollPaneAbstract.setExcludeOnlyQCOnGraph(excludeQcButtonList);
	JScoop3ChartScrollPaneAbstract.setDisplayOnlyQCOnGraph(displayQcButtonList);
    }

    public void updateDisplayOrHidePointsIcons(final int buttonSelected) {

	switch (buttonSelected) {
	case DISPLAY_POINTS:
	    displayPoints = !displayPoints;
	    break;
	case DISPLAY_POINTS_AND_CIRLCE:
	    displayCircle = !displayCircle;
	    break;
	case DISPLAY_LINE_ON_GRAPH:
	    displayLine = !displayLine;
	    break;
	default:
	    break;
	}

	JScoop3ChartScrollPaneAbstract.setDisplayPointsAndCircle(displayLine, displayPoints, displayCircle);

	displayLineButton.setIcon((displayLine) ? displayLineIcon : displayLineIconGreyed);
	displayPointsButton.setIcon((displayPoints) ? displayPointsIcon : displayPointsIconGreyed);
	displayPointsAndCircleButton
		.setIcon((displayCircle) ? displayPointsAndCircleIcon : displayPointsAndCircleIconGreyed);

	displayPointsButton.repaint();
	displayPointsAndCircleButton.repaint();
	displayLineButton.repaint();
    }

    public void updateInfoLabel(final String infoText) {
	infoLabel.setText(infoText);
    }

    public void updateIsCheckedforErrorMessage(final String obsRef, final String variableName,
	    final Number variableValue, final String variableValueStr, final int refLevel, final String refValStr,
	    final String errorMessage, final Boolean newValue, final Boolean oldValue) {
	propertyChangeSupport.firePropertyChange(new SC3PropertyIsCheckedChangeEvent(obsRef, variableName,
		variableValue, variableValueStr, refLevel, refValStr, errorMessage, newValue, oldValue));
    }

    public void updateMouseModeIcons() {

	switch (AbstractChartScrollPane.getMouseMode()) {
	case SELECTION:
	    mouseModeZoomButton.getActionModel().setSelected(false);
	    // mouseModePrecisionZoomButton.getActionModel().setSelected(false);
	    mouseModeSelectionButton.getActionModel().setSelected(true);
	    if (selectAndChangeQCs != null) {
		for (final SelectAndChangeQCCommandButton selectAndChangeQC : selectAndChangeQCs) {
		    selectAndChangeQC.getSelectAndChange().getActionModel().setSelected(false);
		}
	    }
	    mouseSubModeQC = -128;
	    break;
	case ZOOM:
	    mouseModeZoomButton.getActionModel().setSelected(true);
	    // mouseModePrecisionZoomButton.getActionModel().setSelected(false);
	    mouseModeSelectionButton.getActionModel().setSelected(false);
	    if (selectAndChangeQCs != null) {
		for (final SelectAndChangeQCCommandButton selectAndChangeQC : selectAndChangeQCs) {
		    selectAndChangeQC.getSelectAndChange().getActionModel().setSelected(false);
		}
	    }
	    mouseSubModeQC = -128;
	    break;
	// case ZOOM_WITH_PRECISION:
	// mouseModeZoomButton.getActionModel().setSelected(false);
	// mouseModePrecisionZoomButton.getActionModel().setSelected(true);
	// mouseModeSelectionButton.getActionModel().setSelected(false);
	// if (selectAndChangeQCs != null) {
	// for (final SelectAndChangeQCCommandButton selectAndChangeQC : selectAndChangeQCs) {
	// selectAndChangeQC.getSelectAndChange().getActionModel().setSelected(false);
	// }
	// }
	// mouseSubModeQC = -128;
	// break;
	case OTHER:
	    mouseModeZoomButton.getActionModel().setSelected(false);
	    // mouseModePrecisionZoomButton.getActionModel().setSelected(false);
	    mouseModeSelectionButton.getActionModel().setSelected(false);
	    if (selectAndChangeQCs != null) {
		for (final SelectAndChangeQCCommandButton selectAndChangeQC : selectAndChangeQCs) {
		    selectAndChangeQC.getSelectAndChange().getActionModel()
			    .setSelected(selectAndChangeQC.getQc() == mouseSubModeQC);
		    selectAndChangeQC.getSelectAndChange().repaint();
		}
	    }
	    break;
	default:
	    break;
	}
    }

    /**
     *
     * @param variableName
     *            /!\ could be NULL
     * @param newQC
     */
    public void updateQCforVariable(final String obsRef, final String variableName, final int refLevel,
	    final QCValues newQC) {
	propertyChangeSupport.firePropertyChange(new SC3PropertyQCChangeEvent(obsRef, variableName, refLevel, newQC));
    }

    /**
     * Compute the divider location
     *
     * @param curSplitPane
     * @param nbGraphs
     * @return
     */
    private int getDividerLocation(final JSplitPane curSplitPane, final int nbGraphs) {
	if (true) {
	    // return 150;
	}
	return (curSplitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) //
		// Get the available width for all graphs ... divided by the
		// number of graphs horizontally
		? ((getEastPanelAvailableWidth() / nbGraphs) //
			// Minus the number of dividers
			- (8 * (nbGraphs - 1))) //
		// Get the total height (minus south panel) for all graphs ...
		// divided by the number of graphs
		// vertically
		: (((getEastPanelAvailableHeight() - 65) / nbGraphs)
			// Minus the number of dividers
			- (8 * (nbGraphs - 1)));
    }

    private void initButtons() {
	validateButton.setText(Messages.getMessage("bpc-gui.button-save-and-close"));
	final URL location = getClass().getClassLoader().getResource("icons/save_all.png");
	validateButton.setIcon(new ImageIcon(location));

	cancelButton.setVisible(false);
    }

    /**
     * Unload data to save memory
     *
     * @param propagePrepareDispose
     */
    private void specificPrepareForDispose(final boolean propagePrepareDispose) {
	for (final ChartPanelWithComboBox chartPanelWithComboBox : listGraphs) {
	    if (chartsPanel != null) {
		chartsPanel.remove(chartPanelWithComboBox);
	    }
	    if (propagePrepareDispose) {
		chartPanelWithComboBox.prepareForDispose();
	    }
	}
	listGraphs.clear();
	listGraphs = null;

	/**
	 * jdetoc replace by gridBagLayout
	 */
	/*
	 * if (chartsPanelScrollPane != null) { getEastPanel().remove(chartsPanelScrollPane);
	 * chartsPanelScrollPane.removeAll(); chartsPanelScrollPane = null; } if (chartsPanel != null) {
	 * chartsPanel.removeAll(); chartsPanel = null; }
	 */
	if (chartsPanel != null) {
	    getEastPanel().remove(chartsPanel);
	    chartsPanel.removeAll();
	    chartsPanel = null;
	}
    }

    /**
     * @return TRUE if the button AllProfilesButton is added in the band.
     */
    protected boolean addAllProfilesButtonInJRibbonBand() {
	return true;
    }

    /**
     * @return TRUE if the button SaveAllGraphsButton is added in the band.
     */
    protected boolean addSaveAllGraphsButtonInJRibbonBand() {
	return true;
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
     * @see fr.ifremer.scoop3.gui.common.CommonViewImpl#createRibbonBands()
     */
    @Override
    protected void createRibbonBands() {
	// implemented in child class
    }

    /**
     * @return
     */
    protected JRibbonBand getDataTableRibbonBand() {
	final JRibbonBand dataTableRibbonBand = new JRibbonBand(Messages.getMessage("bpc-gui.ribbon-data_table"), null);

	dataTableButton = addJCommandButtonToRibbonBand(dataTableRibbonBand, "icons/table.png",
		"bpc-gui.ribbon-data_table", RibbonElementPriority.TOP);

	final List<RibbonBandResizePolicy> resizePolicies = new ArrayList<RibbonBandResizePolicy>();
	resizePolicies.add(new CoreRibbonResizePolicies.Mirror(dataTableRibbonBand.getControlPanel()));
	resizePolicies.add(new IconRibbonBandResizePolicy(dataTableRibbonBand.getControlPanel()));
	dataTableRibbonBand.setResizePolicies(resizePolicies);

	return dataTableRibbonBand;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewImpl#getInfoPanel()
     */
    @Override
    protected JPanel getInfoPanel() {
	if (infoPanel == null) {
	    infoPanel = new JPanel(new BorderLayout());

	    if (infoLabel == null) {
		infoLabel = new JLabel();
	    }
	    infoPanel.add(infoLabel, BorderLayout.CENTER);
	}
	return infoPanel;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewImpl#getInfoLabel()
     */
    @Override
    public JLabel getInfoLabel() {
	return infoLabel;
    }

    protected JRibbonBand getMouseModeBand(final QCValues[] qcValuesSettable) {
	final JRibbonBand selectAndChangeQCBand = new JRibbonBand(Messages.getMessage("coriolis-gui.ribbon-mouse-mode"),
		null);

	/*
	 * MOUSE MODES BUTTONS - BEGIN
	 */
	mouseModeSelectionButton = addJCommandButtonToRibbonBand(selectAndChangeQCBand, "icons/cursor_select.png",
		"bpc-gui.ribbon-mouse_mode_selection", RibbonElementPriority.LOW);

	mouseModeZoomButton = addJCommandButtonToRibbonBand(selectAndChangeQCBand, "icons/zoom_fit_best.png",
		"bpc-gui.ribbon-mouse_mode_zoom", RibbonElementPriority.LOW);

	// mouseModePrecisionZoomButton = addJCommandButtonToRibbonBand(selectAndChangeQCBand,
	// "icons/zoom_fit_best_precision.png", "bpc-gui.ribbon-mouse_mode_zoom", RibbonElementPriority.LOW);

	AbstractChartScrollPane.setMouseMode(MouseMode.SELECTION);
	updateMouseModeIcons();
	/*
	 * MOUSE MODES BUTTONS - END
	 */

	selectAndChangeQCs = new ArrayList<>();
	for (final QCValues qcValue : qcValuesSettable) {
	    final JCommandButton selectAndChange = addJCommandButtonToRibbonBand(selectAndChangeQCBand,
		    "icons/" + (getScoop3Frame().getTitle()
			    .contains(Messages.getMessage("bpc-controller.application-title")) ? "bpc_" : "")
			    + "select_and_change_qc_" + qcValue.getQCValue() + "_inactive.png",
		    Messages.getMessage((getScoop3Frame().getTitle().contains(
			    Messages.getMessage("bpc-controller.application-title")) ? "bpc-gui" : "coriolis-gui")
			    + ".ribbon-change_qc_cur_obs_QC_" + qcValue.getQCValue()),
		    RibbonElementPriority.LOW);
	    selectAndChange.setActionRichTooltip(new RichTooltip("QC",
		    Messages.getMessage((getScoop3Frame().getTitle().contains(
			    Messages.getMessage("bpc-controller.application-title")) ? "bpc-gui" : "coriolis-gui")
			    + ".ribbon-change_qc_cur_obs_QC_" + qcValue.getQCValue())));
	    selectAndChangeQCs.add(new SelectAndChangeQCCommandButton(this, selectAndChange, qcValue.getQCValue()));
	}

	final List<RibbonBandResizePolicy> resizePolicies = new ArrayList<RibbonBandResizePolicy>();
	resizePolicies.add(new CoreRibbonResizePolicies.Mirror(selectAndChangeQCBand.getControlPanel()));
	resizePolicies.add(new IconRibbonBandResizePolicy(selectAndChangeQCBand.getControlPanel()));
	selectAndChangeQCBand.setResizePolicies(resizePolicies);

	return selectAndChangeQCBand;
    }

    /**
     * @param ribbonBand
     */
    protected void initChangeNumberOfGraphsButtons(final JRibbonBand ribbonBand) {
	/*
	 * CHANGE NUMBER OF GRAPHS - BEGIN
	 */
	changeGraphs = new ArrayList<>();
	for (int columnNb = 1; columnNb <= 3; columnNb++) {
	    for (int rowNb = 1; rowNb <= 3; rowNb++) {
		final String title = rowNb + "x" + columnNb;
		final JCommandButton changeGraphsNumber = addJCommandButtonToRibbonBand(ribbonBand,
			"icons/editor_grid_view_block_" + title + ".png", title, RibbonElementPriority.LOW);
		changeGraphs.add(new ChangeGraphsNumberCommandButton(changeGraphsNumber, rowNb, columnNb));
	    }
	}
	/*
	 * CHANGE NUMBER OF GRAPHS - END
	 */
    }

    /**
     * @param ribbonBand
     */
    protected void initDisplayOrHidePointsButtonsOnGraphs(final JRibbonBand ribbonBand) {
	URL resource;
	/*
	 * DISPLAY / HIDE POINTS BUTTONS - BEGIN
	 */
	displayLineButton = addJCommandButtonToRibbonBand(ribbonBand, "icons/hide_point.png",
		"bpc-gui.ribbon-hide_points", RibbonElementPriority.LOW);
	displayLineIcon = displayLineButton.getIcon();
	resource = getClass().getClassLoader().getResource("icons/hide_point_inactive.png");
	displayLineIconGreyed = ImageWrapperResizableIcon.getIcon(resource, new Dimension(32, 32));

	displayPointsButton = addJCommandButtonToRibbonBand(ribbonBand, "icons/display_point.png",
		"bpc-gui.ribbon-display_points", RibbonElementPriority.LOW);
	displayPointsIcon = displayPointsButton.getIcon();
	resource = getClass().getClassLoader().getResource("icons/display_point_inactive.png");
	displayPointsIconGreyed = ImageWrapperResizableIcon.getIcon(resource, new Dimension(32, 32));

	displayPointsAndCircleButton = addJCommandButtonToRibbonBand(ribbonBand, "icons/display_point_and_circle.png",
		"bpc-gui.ribbon-display_points_and_circle", RibbonElementPriority.LOW);
	displayPointsAndCircleIcon = displayPointsAndCircleButton.getIcon();
	resource = getClass().getClassLoader().getResource("icons/display_point_and_circle_inactive.png");
	displayPointsAndCircleIconGreyed = ImageWrapperResizableIcon.getIcon(resource, new Dimension(32, 32));

	displayLine = true;
	displayPoints = true;
	displayCircle = false;
	updateDisplayOrHidePointsIcons(-1);
	/*
	 * DISPLAY / HIDE POINTS BUTTONS - END
	 */
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewImpl#specificPrepareForDispose()
     */
    @Override
    protected void specificPrepareForDispose() {
	specificPrepareForDispose(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.ifremer.scoop3.gui.common.CommonViewImpl#validateButtonClicked()
     */
    @Override
    protected void validateButtonClicked() {
	propertyChangeSupport.firePropertyChange(new SC3PropertyChangeEvent(this, EVENT_ENUM.VALIDATE));
    }

    protected JComboBox<CLIMATO_ENUM> getClimatojComboBox() {
	if (climatojComboBox == null) {
	    // Create the list of available climatos
	    final CLIMATO_ENUM[] fullClimatoArray = CLIMATO_ENUM.values();
	    final List<CLIMATO_ENUM> availableClimatoList = new ArrayList<CLIMATO_ENUM>();

	    if (dataset.getDatasetType() == DatasetType.TIMESERIE) {
		availableClimatoList.add(CLIMATO_ENUM.SANS);
	    }

	    for (final CLIMATO_ENUM climatoEnum : fullClimatoArray) {
		final CLIMATOLOGY_ID climatologyId = BathyClimatologyManager.getSingleton()
			.getClimatoId(climatoEnum.getClimatologyCode());

		if ((climatologyId != null) && (ClimatologyFactory.getDirectory(climatologyId) != null)) {
		    availableClimatoList.add(climatoEnum);
		}
	    }
	    // Add historical climatologies
	    availableClimatoList.add(CLIMATO_ENUM.BOBY);
	    availableClimatoList.add(CLIMATO_ENUM.L83);
	    availableClimatoList.add(CLIMATO_ENUM.L94);
	    availableClimatoList.add(CLIMATO_ENUM.L01);
	    availableClimatoList.add(CLIMATO_ENUM.L05);
	    availableClimatoList.add(CLIMATO_ENUM.M02M);
	    availableClimatoList.add(CLIMATO_ENUM.M02B);
	    // availableClimatoList.add(CLIMATO_ENUM.REYN);

	    final CLIMATO_ENUM[] availableClimatoArray = new CLIMATO_ENUM[availableClimatoList.size()];
	    // Create climato combo box
	    climatojComboBox = new ClimatoJComboBox(availableClimatoList.toArray(availableClimatoArray));
	}
	return climatojComboBox;
    }

    // # 55265 KeepBoundsCheckbox problem
    // public KeepBoundsJCheckbox getKeepBoundsCheckbox() {
    // if (keepBoundsCheckbox == null) {
    // keepBoundsCheckbox = new KeepBoundsJCheckbox();
    // }
    // return keepBoundsCheckbox;
    // }
    //
    // public void resetKeepBoundsCheckbox() {
    // this.keepBoundsCheckbox = null;
    // }

    // /**
    // * Create the Ribbon Band for the Graphs buttons
    // *
    // * @return
    // */
    // protected JRibbonBand getGraphsRibbonBand() {
    // // implemented in child class
    // return null;
    // }

    public class SuperposedModeJComboBox extends JComboBox<SuperposedModeEnum> {

	private static final long serialVersionUID = -4010246203082169315L;
	private boolean stopActionListener;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SuperposedModeJComboBox(final SuperposedModeEnum[] superposedModeValues) {
	    super(superposedModeValues);

	    stopActionListener = false;

	    /*
	     * Change Text displayed
	     */
	    setRenderer(new BasicComboBoxRenderer() {
		private static final long serialVersionUID = 8088792832251894286L;

		/*
		 * (non-Javadoc)
		 *
		 * @see javax.swing.plaf.basic.BasicComboBoxRenderer# getListCellRendererComponent(javax.swing.JList,
		 * java.lang.Object, int, boolean, boolean)
		 */
		@Override
		public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean isSelected, final boolean cellHasFocus) {
		    final Component component = super.getListCellRendererComponent(list,
			    ((SuperposedModeEnum) value).getLabel(), index, isSelected, cellHasFocus);
		    // if (isSelected && (-1 < index)) {
		    // list.setToolTipText(((DisplayStationType)
		    // value).getLabel());
		    // }
		    return component;
		}

	    });

	    /*
	     * Add listener
	     */
	    addActionListener((final ActionEvent e) -> {
		if ((propertyChangeSupport != null) && !stopActionListener) {
		    propertyChangeSupport.firePropertyChange(
			    new SC3PropertyChangeEvent(getSelectedItem(), EVENT_ENUM.RESET_NEAREST_PROFILES));
		    propertyChangeSupport.firePropertyChange(
			    new SC3PropertyChangeEvent(getSelectedItem(), EVENT_ENUM.DISPLAY_STATION_TYPE));
		}
	    });
	}

	/**
	 * @return the selected mode
	 */
	public SuperposedModeEnum getSuperposedMode() {
	    return (SuperposedModeEnum) getSelectedItem();
	}

	/**
	 * @param stopActionListener
	 *            the stopActionListener to set. if TRUE, the ActionListener do nothing.
	 */
	public void stopActionListener(final boolean stopActionListener) {
	    this.stopActionListener = stopActionListener;
	}
    }

    @Override
    protected void createWestSplitPane() {
	westPanel = new JPanel();
	westPanel.setLayout(new BorderLayout());
	westPanel.setBorder(BorderFactory.createLineBorder(Color.black));
	westPanel.add(this.metadataSplitPane, BorderLayout.CENTER);

	// The MapPanel will be filled later by the Controller
	mapPanel = new JPanel();
	westMapJSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, westPanel, mapPanel);

	westMapJSplitPane.setDividerSize(SPLITPANE_DIVIDERSIZE);
	// Add arrows (up and down) in the divider
	westMapJSplitPane.setOneTouchExpandable(true);

	// Set the divider to the middle of the JSplitPane
	final double weight = 0.7d;
	westMapJSplitPane.setResizeWeight(weight);
    }

    @Override
    protected void createMainSplitPane() {
	mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westMapJSplitPane, eastPanel);

	// map and meta-data split pane
	mainSplitPane.setDividerSize(SPLITPANE_DIVIDERSIZE);
	// Add arrows (left and right) in the divider
	mainSplitPane.setOneTouchExpandable(true);
	mainSplitPane.setDividerLocation(WESTPANEL_DEFAULT_WIDTH);

	displayComponent.add(mainSplitPane, BorderLayout.CENTER);

    }

    @Override
    public void setMaximumSpinnerNextValue(final int maximumSpinnerNextValue) {
    }

    @Override
    public void setMaximumSpinnerPrevValue(final int maximumSpinnerPrevValue) {
    }

    public List<Integer> getDisplayQcButtonList() {
	return this.displayQcButtonList;
    }

    public List<Integer> getExcludeQcButtonList() {
	return this.excludeQcButtonList;
    }

    public List<ChartPanelWithComboBox> getListGraphs() {
	return this.listGraphs;
    }

    public void createClimatoSettingsPopUp() {
	JButton climatoValidateButton;
	JDialog climatoPopup;
	JSpinner spinnerClimato1;
	JSpinner spinnerClimato2;

	final int defaultLocalStd = 4;
	final int defaultLocalDMean = 0;

	// Open the Dialog
	climatoPopup = new JDialog(getScoop3Frame(), true);
	climatoPopup.getContentPane().setLayout(new BorderLayout(10, 10));

	/*
	 * NORTH : Title
	 */
	climatoPopup.setTitle(Messages.getMessage("gui.climato-settings"));

	/*
	 * CENTER : Panels
	 */
	final JPanel centerPanel = new JPanel(new FlowLayout());
	final JPanel centerPanel1 = new JPanel(new FlowLayout());
	centerPanel1.setPreferredSize(new Dimension(250, 40));
	final JPanel centerPanel2 = new JPanel(new FlowLayout());
	centerPanel2.setPreferredSize(new Dimension(250, 40));

	/*
	 * CENTER : Spinner standard deviation
	 */
	spinnerClimato1 = new JSpinner();
	final String defaultStd = FileConfig.getScoop3FileConfig().getString("climatology.n_std").trim();
	if (!defaultStd.equals("") && (climatoStd == -1)) {
	    spinnerClimato1.setValue(Integer.parseInt(defaultStd));
	    climatoStd = Integer.parseInt(defaultStd);
	} else if ((climatoStd != -1) && (climatoDMean != -1)) {
	    spinnerClimato1.setValue(climatoStd);
	} else {
	    spinnerClimato1.setValue(defaultLocalStd);
	}
	((JSpinner.DefaultEditor) spinnerClimato1.getEditor()).getTextField().setColumns(2);

	spinnerClimato1.addChangeListener((final ChangeEvent e) -> {
	    if ((int) spinnerClimato1.getValue() < 0) {
		spinnerClimato1.setValue(0);
	    } else if ((int) spinnerClimato1.getValue() > spinnerClimato1MaxValue) {
		spinnerClimato1.setValue(spinnerClimato1MaxValue);
	    }
	});

	/*
	 * CENTER : Spinner average
	 */
	spinnerClimato2 = new JSpinner();
	final String defaultDMean = FileConfig.getScoop3FileConfig().getString("climatology.n_dmean").trim();
	if (!defaultDMean.equals("") && (climatoDMean == -1)) {
	    spinnerClimato2.setValue(Integer.parseInt(defaultDMean));
	    climatoDMean = Integer.parseInt(defaultDMean);
	} else if ((climatoStd != -1) && (climatoDMean != -1)) {
	    spinnerClimato2.setValue(climatoDMean);
	} else {
	    spinnerClimato2.setValue(defaultLocalDMean);
	}
	((JSpinner.DefaultEditor) spinnerClimato2.getEditor()).getTextField().setColumns(2);

	spinnerClimato2.addChangeListener((final ChangeEvent e) -> {
	    if ((int) spinnerClimato2.getValue() < 0) {
		spinnerClimato2.setValue(0);
	    } else if ((int) spinnerClimato2.getValue() > spinnerClimato2MaxValue) {
		spinnerClimato2.setValue(spinnerClimato2MaxValue);
	    }
	});

	/*
	 * CENTER : Standard deviation
	 */
	final JPanel iconPanel1 = new JPanel(new GridLayout());
	final JLabel labelIcon1 = new JLabel();
	final ImageIcon icon1 = new ImageIcon(getClass().getClassLoader().getResource("icons/standard_deviation.png"));
	labelIcon1.setIcon(icon1);
	labelIcon1.setSize(icon1.getIconWidth(), icon1.getIconHeight());
	labelIcon1.setText("");
	iconPanel1.add(labelIcon1);
	iconPanel1.setSize(labelIcon1.getSize());
	centerPanel1.add(iconPanel1);

	centerPanel1.add(new JLabel(Messages.getMessage("gui.standard-deviation")));
	centerPanel1.add(spinnerClimato1);

	/*
	 * CENTER : Average
	 */
	final JPanel iconPanel2 = new JPanel(new GridLayout());
	final JLabel labelIcon2 = new JLabel();
	final ImageIcon icon2 = new ImageIcon(getClass().getClassLoader().getResource("icons/average.png"));
	labelIcon2.setIcon(icon2);
	labelIcon2.setSize(icon2.getIconWidth(), icon2.getIconHeight());
	labelIcon2.setText("");
	iconPanel2.add(labelIcon2);
	iconPanel2.setSize(labelIcon2.getSize());
	centerPanel2.add(iconPanel2);

	centerPanel2.add(new JLabel(Messages.getMessage("gui.derivative-of-the-mean")));
	centerPanel2.add(spinnerClimato2);

	centerPanel.add(centerPanel1, BorderLayout.NORTH);
	centerPanel.add(centerPanel2, BorderLayout.SOUTH);
	climatoPopup.getContentPane().add(centerPanel, BorderLayout.CENTER);

	/*
	 * SOUTH : Reset button
	 */
	final JButton climatoResetButton = new JButton();
	climatoResetButton.setPreferredSize(new Dimension(40, 30));
	climatoResetButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/undo_all.png")));
	climatoResetButton.addActionListener((final ActionEvent e) -> {
	    if (!defaultStd.equals("") && !defaultDMean.equals("")) {
		climatoStd = Integer.parseInt(defaultStd);
		climatoDMean = Integer.parseInt(defaultDMean);
	    } else {
		climatoStd = defaultLocalStd;
		climatoDMean = defaultLocalDMean;
	    }
	    spinnerClimato1.setValue(climatoStd);
	    spinnerClimato2.setValue(climatoDMean);
	    for (final ChartPanelWithComboBox c : getListGraphs()) {
		propertyChangeSupport
			.firePropertyChange(new SC3PropertyChangeEvent(c, EVENT_ENUM.EDIT_CLIMATO_ADDITIONAL_GRAPHS));
	    }
	    SC3Logger.LOGGER.info("Climatologie paramétrée par défaut avec un coefficient d'écart type à " + defaultStd
		    + " et un coefficient de dérivée verticale de la moyenne à " + defaultDMean);
	});

	/*
	 * SOUTH : Validate button
	 */
	climatoValidateButton = new JButton();
	climatoValidateButton.setPreferredSize(new Dimension(40, 30));
	climatoValidateButton
		.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/dialog_ok_apply.png")));
	climatoValidateButton.addActionListener((final ActionEvent e) -> {
	    climatoStd = (int) spinnerClimato1.getValue();
	    climatoDMean = (int) spinnerClimato2.getValue();
	    for (final ChartPanelWithComboBox c : getListGraphs()) {
		propertyChangeSupport
			.firePropertyChange(new SC3PropertyChangeEvent(c, EVENT_ENUM.EDIT_CLIMATO_ADDITIONAL_GRAPHS));
	    }
	    SC3Logger.LOGGER
		    .info("Climatologie paramétrée avec un coefficient d'écart type à " + spinnerClimato1.getValue()
			    + " et un coefficient de dérivée verticale de la moyenne à " + spinnerClimato2.getValue());
	});

	/*
	 * SOUTH Panel
	 */
	final JPanel closeButtonPanel = new JPanel();
	closeButtonPanel.add(climatoValidateButton);
	closeButtonPanel.add(new JPanel());
	closeButtonPanel.add(climatoResetButton);
	climatoPopup.getContentPane().add(closeButtonPanel, BorderLayout.SOUTH);

	/*
	 * General popup
	 */
	final Dimension dialogDim = new Dimension(300, 170);
	climatoPopup.setPreferredSize(dialogDim);
	climatoPopup.pack();
	climatoPopup.setResizable(false);
	climatoPopup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	climatoPopup.setLocationRelativeTo(getScoop3Frame());
	climatoPopup.setVisible(true);
    }

    public int getClimatoStd() {
	return this.climatoStd;
    }

    public int getClimatoDMean() {
	return this.climatoDMean;
    }

    /**
     * Create the 3d ribbon band
     */
    protected JRibbonBand getThreeDRibbonBand() {
	JCommandButton basicGraphsRibbonButton;
	JCommandButton delauneyRibbonButton;
	JCommandButton scatterRibbonButton;

	// initialisation, on lance le programme avec les graphs 2D
	type3d = Type3d.NONE;

	final JRibbonBand threeDRibbonBand = new JRibbonBand(Messages.getMessage("gui.ribbon-threed"), null);

	basicGraphsRibbonButton = addJCommandButtonToRibbonBand(threeDRibbonBand, "icons/basic_graphs.png",
		"bpc-gui.ribbon-basic-graphs", RibbonElementPriority.MEDIUM);

	delauneyRibbonButton = addJCommandButtonToRibbonBand(threeDRibbonBand, "icons/delauney.png",
		"bpc-gui.ribbon-delauney", RibbonElementPriority.MEDIUM);

	scatterRibbonButton = addJCommandButtonToRibbonBand(threeDRibbonBand, "icons/scatter.png",
		"bpc-gui.ribbon-scatter", RibbonElementPriority.MEDIUM);

	basicGraphsRibbonButton.addActionListener((final java.awt.event.ActionEvent evt) -> {
	    updateChartsPanel();// permet d'afficher les graphs 2D
	    changeColorComboBoxBackground(defaultColorBoxColor);
	});

	delauneyRibbonButton.addActionListener((final java.awt.event.ActionEvent evt) -> {
	    if (type3d == Type3d.NONE) {
		initGraphs3DComboBoxes();
		type3d = Type3d.BLANK;
	    } else {
		if ((xComboBox.getSelectedIndex() == yComboBox.getSelectedIndex())
			|| (xComboBox.getSelectedIndex() == zComboBox.getSelectedIndex())
			|| (yComboBox.getSelectedIndex() == zComboBox.getSelectedIndex())) {
		    JOptionPane.showMessageDialog(new JFrame(),
			    "Veillez à sélectionner des paramètres différents sur les axes X, Y et Z", "Attention",
			    JOptionPane.WARNING_MESSAGE);
		} else if ((xComboBox.getSelectedItem().toString().equals("NONE"))
			|| (yComboBox.getSelectedItem().toString().equals("NONE"))
			|| (zComboBox.getSelectedItem().toString().equals("NONE"))) {
		    JOptionPane.showMessageDialog(new JFrame(),
			    "Veuillez sélectionner d'autres paramètres que NONE dans les champs X, Y et Z", "Attention",
			    JOptionPane.WARNING_MESSAGE);
		} else {
		    if (((xComboBox.getSelectedItem().toString().equals(DataFrame3D.DATE_KEY))
			    && ((!yComboBox.getSelectedItem().toString().equals(DataFrame3D.LONGITUDE_KEY))
				    || (!yComboBox.getSelectedItem().toString().equals(DataFrame3D.LATITUDE_KEY))))
			    || ((yComboBox.getSelectedItem().toString().equals(DataFrame3D.DATE_KEY)) && ((!xComboBox
				    .getSelectedItem().toString().equals(DataFrame3D.LONGITUDE_KEY))
				    || (!xComboBox.getSelectedItem().toString().equals(DataFrame3D.LATITUDE_KEY))))) {
			JOptionPane.showMessageDialog(new JFrame(),
				"Pour observer un graph 3D Delauney avec ces paramètres, veuillez sélectionner DATE sur l'axe Z",
				"Erreur", JOptionPane.ERROR_MESSAGE);
		    } else {
			try {
			    updateDelauneyPanel();
			    changeColorComboBoxBackground(offColorBoxColor);
			} catch (final PlotGraph3DException e) {
			    JOptionPane.showMessageDialog(new JFrame(), "Le graph delauney ne peut pas être affiché",
				    "Erreur", JOptionPane.ERROR_MESSAGE);
			}
		    }
		}
	    }
	});

	scatterRibbonButton.addActionListener((final java.awt.event.ActionEvent evt) -> {
	    if (type3d == Type3d.NONE) {
		initGraphs3DComboBoxes();
		type3d = Type3d.BLANK;
	    } else {
		if ((xComboBox.getSelectedIndex() == yComboBox.getSelectedIndex())
			|| (xComboBox.getSelectedIndex() == zComboBox.getSelectedIndex())
			|| (yComboBox.getSelectedIndex() == zComboBox.getSelectedIndex())) {
		    JOptionPane.showMessageDialog(new JFrame(),
			    "Veillez à sélectionner des paramètres différents sur les axes X, Y et Z", "Attention",
			    JOptionPane.WARNING_MESSAGE);
		} else if ((xComboBox.getSelectedItem().toString().equals("NONE"))
			|| (yComboBox.getSelectedItem().toString().equals("NONE"))
			|| (zComboBox.getSelectedItem().toString().equals("NONE"))
			|| (colorMapComboBox.getSelectedItem().toString().equals("NONE"))) {
		    JOptionPane.showMessageDialog(new JFrame(),
			    "Veuillez sélectionner d'autres paramètres que NONE dans les champs X, Y, Z et color",
			    "Attention", JOptionPane.WARNING_MESSAGE);
		} else {
		    try {
			updateScatterPanel();
			changeColorComboBoxBackground(defaultColorBoxColor);
		    } catch (final PlotGraph3DException e) {
			JOptionPane.showMessageDialog(new JFrame(), "Le graph scatter ne peut pas être affiché",
				"Erreur", JOptionPane.ERROR_MESSAGE);
		    }
		}
	    }
	});

	final List<RibbonBandResizePolicy> resizePolicies = new ArrayList<RibbonBandResizePolicy>();
	resizePolicies.add(new CoreRibbonResizePolicies.Mirror(threeDRibbonBand.getControlPanel()));
	resizePolicies.add(new IconRibbonBandResizePolicy(threeDRibbonBand.getControlPanel()));
	threeDRibbonBand.setResizePolicies(resizePolicies);

	return threeDRibbonBand;
    }

    /**
     * Create the list of parameters for "x,y,z and color parameter" ribbon band for jzy3d
     *
     * @return getXYZJComboBox
     */
    public JComboBox<String> getXYZJComboBox(final SuperposedModeEnum superposedModeEnum) {
	String[] tabKeyset; // keyset of the graph3D

	List<String> keyset = new ArrayList<String>();
	sortedKeyset = new ArrayList<String>();

	for (int i = 0; i < 1; i++) {
	    keyset = new ArrayList<String>();
	    if (dataset.getDatasetType() == DatasetType.PROFILE) {
		keyset.add("NONE");
		addParameterToKeysetIfExists(keyset, "TEMP");
		addParameterToKeysetIfExists(keyset, "PSAL");
		addParameterToKeysetIfExists(keyset, DataFrame3D.DEPTH_KEY);
		addParameterToKeysetIfExists(keyset, DataFrame3D.PRES_KEY);
		sortedKeyset.add(DataFrame3D.LATITUDE_KEY);
		sortedKeyset.add(DataFrame3D.LONGITUDE_KEY);
		if ((superposedModeEnum == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
			|| (superposedModeEnum == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET)) {
		    sortedKeyset.add(DataFrame3D.CYCLE_NUMBER);
		}
		sortedKeyset.add(DataFrame3D.DATE_KEY);
	    }
	    if ((dataset.getDatasetType() == DatasetType.TIMESERIE)
		    || (dataset.getDatasetType() == DatasetType.TRAJECTORY)) {
		keyset.add("NONE");
		addParameterToKeysetIfExists(keyset, "TEMP");
		addParameterToKeysetIfExists(keyset, "PSAL");
		addParameterToKeysetIfExists(keyset, DataFrame3D.TIME_KEY);
		sortedKeyset.add(DataFrame3D.LATITUDE_KEY);
		sortedKeyset.add(DataFrame3D.LONGITUDE_KEY);
	    }
	}

	final Set<String> hs = new HashSet<>(dataset.getParametersNames());

	for (final String p : hs) {
	    if (!keyset.contains(p)) {
		sortedKeyset.add(p);
	    }
	}

	Collections.sort(sortedKeyset);
	keyset.addAll(sortedKeyset);
	tabKeyset = new String[keyset.size()];

	for (int j = 0; j < keyset.size(); j++) {
	    tabKeyset[j] = keyset.get(j);
	}

	final JComboBox<String> getXYZJComboBox = new JComboBox<String>(tabKeyset);
	getXYZJComboBox.setEnabled(true);
	return getXYZJComboBox;
    }

    private void addParameterToKeysetIfExists(final List<String> keyset, final String param) {
	boolean addParam = false;
	for (final Observation o : dataset.getObservations()) {
	    if ((o.getOceanicParameter(param) != null)
		    || o.getReferenceParameter().getCode().equalsIgnoreCase(param.toLowerCase())) {
		addParam = true;
		break;
	    }
	}
	if (addParam) {
	    keyset.add(param);
	}
    }

    private void updateDelauneyPanel() throws PlotGraph3DException {
	set3DGraphsParameters();
	final Dimension jzy3dSize = new Dimension(getEastPanel().getWidth(),
		eastPanel.getHeight() - (2 * comboBoxesGraphs3DPanelHeight)); // resize du graph 3D
	jzy3dManager = new Jzy3dManager(dataset, Type3d.DELAUNEY, xComboBox.getSelectedItem().toString(),
		yComboBox.getSelectedItem().toString(), zComboBox.getSelectedItem().toString(),
		colorMapComboBox.getSelectedItem().toString(), DataFrameType.NORMAL, null, jzy3dSize,
		getSuperposedModeJComboBox().getSuperposedMode(),
		((InfoInObservationSubPanel) infoInObservationSubPanel).getIndexTextField().getText());
	if (!jzy3dManager.getErrorInJzy3dManager()) {
	    jzy3dManager.getJzy3dPanel().setBackground(Color.WHITE);
	    graphs3DPanel.remove(1); // suppression du panel blanc
	    graphs3DPanel.add(jzy3dManager.getJzy3dPanel(), BorderLayout.SOUTH); // ajout du graph 3D à la place du
										 // blanc
	    getEastPanel().revalidate();
	    getEastPanel().repaint();
	    type3d = Type3d.DELAUNEY;
	}
    }

    private void updateScatterPanel() throws PlotGraph3DException {
	set3DGraphsParameters();
	final Dimension jzy3dSize = new Dimension(getEastPanel().getWidth(),
		eastPanel.getHeight() - (2 * comboBoxesGraphs3DPanelHeight)); // resize du graph 3D
	jzy3dManager = new Jzy3dManager(dataset, Type3d.SCATTER, xComboBox.getSelectedItem().toString(),
		yComboBox.getSelectedItem().toString(), zComboBox.getSelectedItem().toString(),
		colorMapComboBox.getSelectedItem().toString(), DataFrameType.NORMAL,
		(eastPanel.getSize().getHeight() - comboBoxesGraphs3DPanelHeight) * 0.96, jzy3dSize,
		getSuperposedModeJComboBox().getSuperposedMode(),
		((InfoInObservationSubPanel) infoInObservationSubPanel).getIndexTextField().getText());
	if (!jzy3dManager.getErrorInJzy3dManager()) {
	    jzy3dManager.getJzy3dPanel().setBackground(Color.WHITE);
	    graphs3DPanel.remove(1); // suppression du panel blanc
	    graphs3DPanel.add(jzy3dManager.getJzy3dPanel(), BorderLayout.SOUTH); // ajout du graph 3D à la place du
										 // blanc
	    getEastPanel().revalidate();
	    getEastPanel().repaint();
	    type3d = Type3d.SCATTER;
	}
    }

    public void updateChartsPanel() {
	if (type3d != Type3d.NONE) {// si le dernier graph est un graph 3D
	    getEastPanel().remove(panelList.get(0));// on supprime le dernier graph 3D qui était affiché
	    refillTempList();// reset la tempList qui a pu subir des modifications
	    getEastPanel().add(tempList.get(0));// on rajoute les graphs 2D qui étaient stockés dans la tempList
	    if ((jzy3dManager != null) && (jzy3dManager.getPlotGraphs3D() != null)) {
		jzy3dManager.getPlotGraphs3D().clearPreviousPlot();
	    }
	    getEastPanel().revalidate();
	    getEastPanel().repaint();
	    type3d = Type3d.NONE;
	}
    }

    public void initGraphs3DComboBoxes() {
	JPanel comboBoxesGraphs3DPanel;
	JPanel blankPanel;

	graphs3DPanel = new JPanel(new BorderLayout());

	// ajouter les comboboxes
	comboBoxesGraphs3DPanel = new JPanel();
	xComboBox = getXYZJComboBox(getSuperposedModeJComboBox().getSuperposedMode());
	yComboBox = getXYZJComboBox(getSuperposedModeJComboBox().getSuperposedMode());
	zComboBox = getXYZJComboBox(getSuperposedModeJComboBox().getSuperposedMode());
	colorMapComboBox = getXYZJComboBox(getSuperposedModeJComboBox().getSuperposedMode());

	comboBoxesGraphs3DPanel.add(new JLabel("X"));
	comboBoxesGraphs3DPanel.add(xComboBox);
	comboBoxesGraphs3DPanel.add(new JLabel("  "));
	comboBoxesGraphs3DPanel.add(new JLabel("Y"));
	comboBoxesGraphs3DPanel.add(yComboBox);
	comboBoxesGraphs3DPanel.add(new JLabel("  "));
	comboBoxesGraphs3DPanel.add(new JLabel("Z"));
	comboBoxesGraphs3DPanel.add(zComboBox);
	comboBoxesGraphs3DPanel.add(new JLabel("  "));
	comboBoxesGraphs3DPanel.add(new JLabel("Color"));
	comboBoxesGraphs3DPanel.add(colorMapComboBox);

	comboBoxesGraphs3DPanel
		.setPreferredSize(new Dimension(getEastPanel().getWidth(), comboBoxesGraphs3DPanelHeight));
	comboBoxesGraphs3DPanel.setBackground(Color.WHITE);

	// remplissage des comboBoxes
	if (xParameter != null) {
	    xComboBox.setSelectedItem(xParameter);
	}
	if (yParameter != null) {
	    yComboBox.setSelectedItem(yParameter);
	}
	if (zParameter != null) {
	    zComboBox.setSelectedItem(zParameter);
	}
	if (colorParameter != null) {
	    colorMapComboBox.setSelectedItem(colorParameter);
	}

	// ajouter un panel blanc
	blankPanel = new JPanel();
	blankPanel.setPreferredSize(new Dimension(getEastPanel().getWidth(),
		getEastPanel().getHeight() - comboBoxesGraphs3DPanel.getHeight()));
	blankPanel.setBackground(Color.WHITE);

	// ajouter les deux panels au graphs3DPanel
	graphs3DPanel.add(comboBoxesGraphs3DPanel, BorderLayout.NORTH);
	graphs3DPanel.add(blankPanel, BorderLayout.SOUTH);

	// affichage du panel
	if (panelList.isEmpty()) {
	    panelList.add(new JPanel());// la liste ne doit pas etre vide
	}
	panelList.add(graphs3DPanel);// ajout du panel dans la liste les gérant
	getEastPanel().add(panelList.get(1), BorderLayout.CENTER);// affichage du dernier panel stocké dans la liste
	getEastPanel().revalidate();
	getEastPanel().repaint();
	refillTempList();// re remplit la tempList qui peut subir des modifications
	getEastPanel().remove(getEastPanel().getComponent(1));// on supprime les graphs 2d de EastPanel pour éviter
	// qu'ils apparaissent au 1er plan lors d'un refresh
	getEastPanel().remove(panelList.get(0));// on supprime le dernier graph qui était affiché après le repaint,
						// pour éviter de le voir au second plan, si le repaint du graph est
						// long
	panelList.remove(0);// on supprime l'element de l'index 0 pour que la liste ne contienne que 1 panel
    }

    public void changeColorComboBoxBackground(final Color color) {
	if (colorMapComboBox != null) {
	    colorMapComboBox.setBackground(color);
	}
    }

    public void set3DGraphsParameters() {
	xParameter = xComboBox.getSelectedItem().toString();
	yParameter = yComboBox.getSelectedItem().toString();
	zParameter = zComboBox.getSelectedItem().toString();
	colorParameter = colorMapComboBox.getSelectedItem().toString();
    }

    public void refillTempList() {
	if (tempList.isEmpty()) {
	    tempList.add(getEastPanel().getComponent(1));
	} else if ((tempList.size() == 1) && (((JPanel) tempList.get(0)).getComponents().length == 0)) {
	    tempList.clear();
	    tempList.add(getEastPanel().getComponent(1));
	}
    }

    public void initClimatologySettings() {
	final String defaultStd = FileConfig.getScoop3FileConfig().getString("climatology.n_std").trim();
	if (!defaultStd.equals("") && (climatoStd == -1)) {
	    climatoStd = Integer.parseInt(defaultStd);
	}
	final String defaultDMean = FileConfig.getScoop3FileConfig().getString("climatology.n_dmean").trim();
	if (!defaultDMean.equals("") && (climatoDMean == -1)) {
	    climatoDMean = Integer.parseInt(defaultDMean);
	}
    }

    public boolean getDisplayCircle() {
	return displayCircle;
    }
}
