package fr.ifremer.scoop3.gui.data;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane;
import fr.ifremer.scoop3.chart.view.scrollpane.AbstractChartScrollPane.MouseMode;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.data.SuperposedModeEnum;
import fr.ifremer.scoop3.gui.common.DataOrReferenceViewController;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent;
import fr.ifremer.scoop3.gui.common.model.SC3PropertyChangeEvent.EVENT_ENUM;
import fr.ifremer.scoop3.gui.data.popup.Scoop3ChartPanelChangeQCJMenuItem;
import fr.ifremer.scoop3.gui.data.popup.Scoop3ChartPanelJMenu;
import fr.ifremer.scoop3.gui.data.popup.Scoop3ChartPanelJMenuItem;
import fr.ifremer.scoop3.gui.data.popup.Scoop3ChartPanelShiftJMenuItem;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.model.DatasetType;
import fr.ifremer.scoop3.model.QCValues;
import fr.ifremer.scoop3.model.parameter.Parameter.LINK_PARAM_TYPE;

public class Scoop3ChartPanelPopupMenu extends JPopupMenu {

    /**
     *
     */
    private static final long serialVersionUID = 8296246351149411745L;

    /**
     * Reference on the ChartPanelWithComboBox
     */
    private final ChartPanelWithComboBox chartPanelWithComboBox;

    /**
     * Reference on the DataOrReferenceViewController
     */
    protected final transient DataOrReferenceViewController dataOrReferenceViewController;

    /**
     * @param chartPanelWithComboBox
     * @param dataOrReferenceViewController
     *
     */
    public Scoop3ChartPanelPopupMenu(final ChartPanelWithComboBox chartPanelWithComboBox,
	    final DataOrReferenceViewController dataOrReferenceViewController) {
	super();
	this.chartPanelWithComboBox = chartPanelWithComboBox;
	this.dataOrReferenceViewController = dataOrReferenceViewController;

	initJMenuItems();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
     */
    @Override
    public void show(final Component invoker, final int x, final int y) {
	for (final MenuElement menuElt : getSubElements()) {
	    if (menuElt instanceof Scoop3ChartPanelJMenu) {
		((Scoop3ChartPanelJMenu) menuElt).updateEnabled();
	    } else if (menuElt instanceof Scoop3ChartPanelJMenuItem) {
		((Scoop3ChartPanelJMenuItem) menuElt).updateEnabled();
	    }
	}
	super.show(invoker, x, y);
    }

    /**
     *
     */
    private void initJMenuItems() {

	final boolean isProfile = dataOrReferenceViewController.getCommonViewModel().getDataset()
		.getDatasetType() == DatasetType.PROFILE;
	final boolean isTimeserie = dataOrReferenceViewController.getCommonViewModel().getDataset()
		.getDatasetType() == DatasetType.TIMESERIE;

	JMenuItem item = new Scoop3ChartPanelJMenuItem("bpc-gui.ribbon-dataset_mode", "icons/superposed_mode.png",
		dataOrReferenceViewController) {
	    private static final long serialVersionUID = 1446113703460329261L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		dataOrReferenceViewController.getPropertyChangeSupport()
			.firePropertyChange(new SC3PropertyChangeEvent(this, EVENT_ENUM.ALL_PROFILES));
		if (dataOrReferenceViewController.isSuperposedMode()) {
		    this.setText(Messages.getMessage("bpc-gui.ribbon-current_station_mode"));
		    this.setIcon(getImageIconForPopupMenu("icons/current_station_mode.png"));
		    dataOrReferenceViewController.updateValidationCounts(true);
		} else {
		    this.setText(Messages.getMessage("bpc-gui.ribbon-dataset_mode"));
		    this.setIcon(getImageIconForPopupMenu("icons/superposed_mode.png"));
		    dataOrReferenceViewController.updateValidationCounts(false);
		}
	    }

	    @Override
	    public void updateEnabled() {
		// This submenu is always enabled (or disabled)
		setEnabled((dataOrReferenceViewController.getCommonViewModel().getObservations().size() > 1)
			&& ((((DataViewImpl) dataOrReferenceViewController.getCommonViewImpl())
				.getSuperposedModeJComboBox() == null)
				|| ((((DataViewImpl) dataOrReferenceViewController.getCommonViewImpl())
					.getSuperposedModeJComboBox() != null)
					&& ((DataViewImpl) dataOrReferenceViewController.getCommonViewImpl())
						.getSuperposedModeJComboBox().isEnabled())));
	    }
	};
	if (dataOrReferenceViewController.isSuperposedMode()) {
	    item.setText(Messages.getMessage("bpc-gui.ribbon-current_station_mode"));
	    item.setIcon(Scoop3ChartPanelJMenuItem.getImageIconForPopupMenu("icons/current_station_mode.png"));
	} else {
	    item.setText(Messages.getMessage("bpc-gui.ribbon-dataset_mode"));
	    item.setIcon(Scoop3ChartPanelJMenuItem.getImageIconForPopupMenu("icons/superposed_mode.png"));
	}
	// This submenu is enabled only if the dataset contains Profile (special case for BPCReferenceViewController
	// which cannot be cast to DataViewController)
	if (dataOrReferenceViewController.getCommonViewImpl().getScoop3Frame().getTitle().contains("IDMDB")
		|| (((DataViewController) dataOrReferenceViewController).getDataViewImpl()
			.getSuperposedModeJComboBox() != null)) {
	    item.setEnabled(isProfile);
	    add(item);
	} else {
	    item.setEnabled(false);
	}

	JMenu menu = new Scoop3ChartPanelJMenu("bpc-gui.ribbon-shift_profiles") {
	    private static final long serialVersionUID = 1446113703460329261L;

	    @Override
	    public void updateEnabled() {
		// This submenu is enabled only if ALL_PROFILE is selected
		boolean enabled = false;
		if ((dataOrReferenceViewController
			.getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
			|| (dataOrReferenceViewController
				.getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET)) {
		    enabled = true;
		}
		setEnabled(enabled);
		for (final MenuElement subMenuElt : getSubElements()) {
		    if (subMenuElt instanceof JMenuItem) {
			((JMenuItem) subMenuElt).setEnabled(enabled);
		    }
		}
	    }
	};
	add(menu);

	for (int index = 1; index <= 4; index++) {
	    // SubItem
	    final JMenuItem subItem = new Scoop3ChartPanelShiftJMenuItem("bpc-gui.ribbon-shift_profiles." + index,
		    "icons/shift_profiles." + index + ".png", index, dataOrReferenceViewController);
	    menu.add(subItem);
	}

	item = new Scoop3ChartPanelJMenuItem("bpc-gui.ribbon-display_points", "icons/display_point.png",
		dataOrReferenceViewController) {
	    private static final long serialVersionUID = 1446113703460329261L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		dataOrReferenceViewController.getPropertyChangeSupport()
			.firePropertyChange(new SC3PropertyChangeEvent(this, EVENT_ENUM.DISPLAY_POINTS_ON_GRAPH));
	    }

	    @Override
	    public void updateEnabled() {
		// This submenu is always enabled
	    }
	};
	add(item);

	if (dataOrReferenceViewController instanceof DataViewController) {
	    item = new Scoop3ChartPanelJMenuItem("gui.dataview-popup.display-info-point-dialog",
		    "icons/circle-full-16.png", dataOrReferenceViewController) {

		private static final long serialVersionUID = 6409678392041919307L;

		@Override
		public void actionPerformed(final ActionEvent e) {
		    // Display cirlce on point if needed
		    if (!((DataViewController) dataOrReferenceViewController).getDataViewImpl().getDisplayCircle()) {
			dataOrReferenceViewController.getPropertyChangeSupport().firePropertyChange(
				new SC3PropertyChangeEvent(this, EVENT_ENUM.DISPLAY_CIRCLE_ON_GRAPH));
		    }

		    ((DataViewController) dataOrReferenceViewController).displayInfoPointDialog();

		    // TODO event
		}

		@Override
		public void updateEnabled() {
		    // This submenu is always enabled
		}
	    };
	    add(item);
	}

	addSeparator();

	item = new Scoop3ChartPanelJMenuItem("bpc-gui.ribbon-change_axis_min_max", "icons/transform_move.png",
		dataOrReferenceViewController) {
	    /**
	    	 *
	    	 */
	    private static final long serialVersionUID = 1446113703460329261L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		dataOrReferenceViewController.getPropertyChangeSupport()
			.firePropertyChange(new SC3PropertyChangeEvent(getSource(), EVENT_ENUM.CHANGE_AXIS_MIN_MAX));
	    }

	    @Override
	    public void updateEnabled() {
		// This submenu is always enabled
	    }
	};
	add(item);

	addSeparator();

	item = new Scoop3ChartPanelJMenuItem("bpc-gui.ribbon-zoom_in", "icons/zoom_in.png",
		dataOrReferenceViewController) {
	    /**
	    	 *
	    	 */
	    private static final long serialVersionUID = 1446113703460329261L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		dataOrReferenceViewController.getPropertyChangeSupport()
			.firePropertyChange(new SC3PropertyChangeEvent(getSource(), EVENT_ENUM.ZOOM_IN));
	    }

	    @Override
	    public void updateEnabled() {
		// This submenu is always enabled
	    }
	};
	add(item);

	item = new Scoop3ChartPanelJMenuItem("bpc-gui.ribbon-zoom_out", "icons/zoom_out.png",
		dataOrReferenceViewController) {
	    /**
	    	 *
	    	 */
	    private static final long serialVersionUID = 1446113703460329261L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		dataOrReferenceViewController.getPropertyChangeSupport()
			.firePropertyChange(new SC3PropertyChangeEvent(getSource(), EVENT_ENUM.ZOOM_OUT));
	    }

	    @Override
	    public void updateEnabled() {
		// This submenu is always enabled
	    }
	};
	add(item);

	item = new Scoop3ChartPanelJMenuItem("bpc-gui.ribbon-zoom_initial", "icons/zoom_original.png",
		dataOrReferenceViewController) {
	    /**
	    	 *
	    	 */
	    private static final long serialVersionUID = 1446113703460329261L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		dataOrReferenceViewController.getPropertyChangeSupport()
			.firePropertyChange(new SC3PropertyChangeEvent(getSource(), EVENT_ENUM.ZOOM_INITIAL));
	    }

	    @Override
	    public void updateEnabled() {
		// This submenu is always enabled
	    }
	};
	add(item);

	addSeparator();

	/*
	 * Change Selection Mode
	 */
	item = new Scoop3ChartPanelJMenuItem("bpc-gui.ribbon-mouse_mode_selection", "icons/cursor_select.png",
		dataOrReferenceViewController) {
	    private static final long serialVersionUID = 1446113703460329261L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		if (AbstractChartScrollPane.getMouseMode() != MouseMode.SELECTION) {
		    AbstractChartScrollPane.setMouseMode(MouseMode.SELECTION);
		    dataOrReferenceViewController.getPropertyChangeSupport()
			    .firePropertyChange(new SC3PropertyChangeEvent(getSource(), EVENT_ENUM.MOUSE_MODE_CHANGED));
		}
	    }

	    @Override
	    public void updateEnabled() {
		setEnabled(AbstractChartScrollPane.getMouseMode() != MouseMode.SELECTION);
	    }
	};
	add(item);
	item = new Scoop3ChartPanelJMenuItem("bpc-gui.ribbon-mouse_mode_zoom", "icons/zoom_fit_best.png",
		dataOrReferenceViewController) {
	    private static final long serialVersionUID = 1446113703460329261L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		if (AbstractChartScrollPane.getMouseMode() != MouseMode.ZOOM) {
		    AbstractChartScrollPane.setMouseMode(MouseMode.ZOOM);
		    dataOrReferenceViewController.getPropertyChangeSupport()
			    .firePropertyChange(new SC3PropertyChangeEvent(getSource(), EVENT_ENUM.MOUSE_MODE_CHANGED));
		}

	    }

	    @Override
	    public void updateEnabled() {
		setEnabled(AbstractChartScrollPane.getMouseMode() != MouseMode.ZOOM);
	    }
	};
	add(item);

	addSeparator();

	/*
	 * Change QC current obs
	 */
	menu = new Scoop3ChartPanelJMenu("bpc-gui.ribbon-change_qc_cur_obs") {

	    /**
	     *
	     */
	    private static final long serialVersionUID = -8578228582049046805L;

	    @Override
	    public void updateEnabled() {
		// This submenu is enabled only if there is a selection box
		final boolean enabled = changeQCMenuIsEnabled();
		setEnabled(enabled);
		for (final MenuElement subMenuElt : getSubElements()) {
		    if (subMenuElt instanceof JMenuItem) {
			((JMenuItem) subMenuElt).setEnabled(enabled);
		    }
		}
	    }
	};
	add(menu);

	for (final QCValues qcValue : dataOrReferenceViewController.getQCValuesSettable()) {
	    if (qcValue != QCValues.QC_FILL_VALUE) { // Cant change to fill value qc
		// SubItem
		final JMenuItem subItem = new Scoop3ChartPanelChangeQCJMenuItem(
			(dataOrReferenceViewController.isBPCVersion() ? "bpc-gui" : "coriolis-gui")
				+ ".ribbon-change_qc_cur_obs_QC_",
			true, qcValue, chartPanelWithComboBox, dataOrReferenceViewController);
		menu.add(subItem);
	    }
	}

	addSeparator();

	/*
	 * Change QC all obs
	 */
	menu = new Scoop3ChartPanelJMenu("bpc-gui.ribbon-change_qc_all_obs") {

	    /**
	     *
	     */
	    private static final long serialVersionUID = -8578228582049046805L;

	    @Override
	    public void updateEnabled() {
		// This submenu is enabled only if ALL_PROFILE is selected
		final boolean enabled = ((dataOrReferenceViewController
			.getSuperposedModeEnum() == SuperposedModeEnum.PROFILES_FOR_PLATFORM_FROM_DATASET)
			|| (dataOrReferenceViewController
				.getSuperposedModeEnum() == SuperposedModeEnum.ALL_OBSERVATIONS_FROM_DATASET))
			&& changeQCMenuIsEnabled();
		setEnabled(enabled);
		for (final MenuElement subMenuElt : getSubElements()) {
		    if (subMenuElt instanceof JMenuItem) {
			((JMenuItem) subMenuElt).setEnabled(enabled);
		    }
		}
	    }
	};
	add(menu);

	for (final QCValues qcValue : dataOrReferenceViewController.getQCValuesSettable()) {
	    if (qcValue != QCValues.QC_FILL_VALUE) { // Cant change to fill value qc
		// SubItem
		final JMenuItem subItem = new Scoop3ChartPanelChangeQCJMenuItem(
			(dataOrReferenceViewController.isBPCVersion() ? "bpc-gui" : "coriolis-gui")
				+ ".ribbon-change_qc_all_obs_QC_",
			false, qcValue, chartPanelWithComboBox, dataOrReferenceViewController);
		menu.add(subItem);
	    }
	}

	if (isTimeserie) {
	    addSeparator();

	    item = new Scoop3ChartPanelJMenuItem("bpc-gui.ribbon-display_statistics", "icons/chart01_64.png",
		    dataOrReferenceViewController) {
		/**
		 *
		 */
		private static final long serialVersionUID = 1446113703460329261L;

		@Override
		public void actionPerformed(final ActionEvent e) {
		    dataOrReferenceViewController.getPropertyChangeSupport().firePropertyChange(
			    new SC3PropertyChangeEvent(chartPanelWithComboBox, EVENT_ENUM.DISPLAY_STATISTICS));
		}

		@Override
		public void updateEnabled() {
		    // This submenu is always enabled (or disabled)
		    setEnabled(isTimeserie);
		}
	    };
	    // This submenu is
	    add(item);

	    //
	    item = new Scoop3ChartPanelJMenuItem("bpc-gui.ribbon-divide-timeserie", "icons/transform_move.png",
		    dataOrReferenceViewController) {

		/**
		 *
		 */
		private static final long serialVersionUID = 2270994800170023181L;

		@Override
		public void actionPerformed(final ActionEvent e) {
		    dataOrReferenceViewController.getPropertyChangeSupport()
			    .firePropertyChange(new SC3PropertyChangeEvent(getSource(), EVENT_ENUM.DIVIDE_TS));
		}

		@Override
		public void updateEnabled() {
		    // This submenu is always enabled (or disabled)
		    setEnabled(isTimeserie);
		}
	    };
	    // This submenu is
	    add(item);

	    //
	}

	addSeparator();
	addSeparator();

	addSaveImagePart();
    }

    public void addSaveImagePart() {
	final URL saveIconURL = getClass().getClassLoader().getResource("icons/save_16x16.png");
	final ImageIcon saveIcon = new ImageIcon(saveIconURL);
	final JMenuItem item = new JMenuItem(Messages.getMessage("gui.dataview-popup.save-image"), saveIcon);
	item.addActionListener(
		(final ActionEvent e) -> dataOrReferenceViewController.saveImage(chartPanelWithComboBox));
	add(item);
    }

    /**
     * @return TRUE if the Menu "Change QC" is enabled.
     */
    protected boolean changeQCMenuIsEnabled() {
	return chartPanelWithComboBox.isSelectionBoxActive() && (JScoop3ChartScrollPaneAbstract.getCoefX() == 0)
	// If the Parameter is a computed paramater (with type CONTROL), it is not possible to change the QCValue
		&& (chartPanelWithComboBox.getLinkParamType() != LINK_PARAM_TYPE.COMPUTED_CONTROL);
    }

    /**
     * @return the Source to send in the SC3PropertyChangeEvent
     */
    protected Object getSource() {
	return chartPanelWithComboBox;
    }
}
