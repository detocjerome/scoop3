package fr.ifremer.scoop3.gui.home;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;

import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;

import fr.ifremer.scoop3.gui.carousel.Carousel;
import fr.ifremer.scoop3.gui.carousel.DriverPanel;
import fr.ifremer.scoop3.gui.core.View;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.properties.FileConfig;
import fr.ifremer.scoop3.io.driver.IDriver;

/**
 * Home view implementation
 *
 * @author Altran
 *
 */
public class HomeViewImpl implements View {

    protected JButton chooseFileButton = new JButton(Messages.getMessage("bpc-gui.choose-file-button"));
    protected JButton configurationButton;
    protected JComboBox<String> configurationJComboBox;
    protected JButton dataButton = new JButton(Messages.getMessage("bpc-gui.data-button"));
    protected JPanel centerPanel;
    protected JLabel dataToControlLabel;
    protected JLabel controlLabel;
    /**
     * Display Component Panel - This is the parent container
     */
    protected JPanel displayComponent;
    /**
     * File chooser
     */
    protected final JFileChooser fileChooser;
    protected JTextField fileChooseTextField = new JTextField(Messages.getMessage("bpc-gui.choose-file-label"));
    protected JButton formatButton = new JButton(Messages.getMessage("bpc-gui.format-button"));
    protected JButton metadataButton = new JButton(Messages.getMessage("bpc-gui.metadata-button"));
    /**
     * Progress Bar
     */
    protected ProgressMonitor progressMonitor = new ProgressMonitor(null,
	    Messages.getMessage("bpc-gui.progress-monitor-title"), Messages.getMessage("bpc-gui.progress-monitor-init"),
	    0, 100);
    protected JButton toolButton = new JButton(Messages.getMessage("bpc-gui.tools-button"));

    /**
     * Carousel
     */
    protected Carousel carousel = new Carousel(3);

    /**
     * @param availableDrivers
     *
     */
    public HomeViewImpl() {

	fileChooser = new JFileChooser(getPreferredDefaultDirectory());

	// Initialize the display component
	displayComponent = new JPanel();
	// Set a border layout to the display component
	displayComponent.setLayout(new BoxLayout(displayComponent, BoxLayout.PAGE_AXIS));
	// Build the display component
	buildDisplayComponent();
	// CustomInit
	customInit();
    }

    /**
     * @return the file chooser button
     */
    public JButton getChooseFileButton() {
	return chooseFileButton;
    }

    /**
     * @return the configurationJComboBox
     */
    public JComboBox<String> getConfigurationJComboBox() {
	return configurationJComboBox;
    }

    /**
     * @return the data button
     */
    public JButton getDataButton() {
	return dataButton;
    }

    /**
     *
     * Return the display component
     */
    @Override
    public Component getDisplayComponent() {
	return displayComponent;
    }

    /**
     *
     * @return the file chooser
     */
    public JFileChooser getFileChooser() {
	return fileChooser;
    }

    /**
     *
     * @return the file chooser text field
     */
    public JTextField getFileChooseTextField() {
	return fileChooseTextField;
    }

    /**
     *
     * @return the format button
     */
    public JButton getFormatButton() {
	return formatButton;
    }

    /**
     *
     * @return the meta data button
     */
    public JButton getMetadataButton() {
	return metadataButton;
    }

    /**
     * @return the preferred default directory
     */
    public File getPreferredDefaultDirectory() {
	final String preferredDefaultDirectory = FileConfig.getScoop3FileConfig()
		.getString("gui.preferred-default-path");
	File preferredDefaultDirectoryFile = (preferredDefaultDirectory == null) ? null
		: new File(preferredDefaultDirectory);
	if ((preferredDefaultDirectory != null) && !preferredDefaultDirectoryFile.exists()) {
	    preferredDefaultDirectoryFile = null;
	}
	return preferredDefaultDirectoryFile;
    }

    /**
     *
     * @return the tool button
     */
    public JButton getToolButton() {
	return toolButton;
    }

    /**
     * Update values available in the Configuration JCombobox
     */
    public void updateConfigurationJComboBox() {

	final List<String> availableFiles = new ArrayList<>();
	availableFiles.add(Messages.getMessage("gui.select-configuration-file"));
	availableFiles.add(FileConfig.CONTROLS_PROPERTIES);

	final File customPropsDir = new File(FileConfig.CUSTOM_CONTROLS_PROPERTIES_DIRECTORY);
	if (customPropsDir.exists()) {
	    final String[] customFiles = customPropsDir.list((final File dir, final String name) -> {
		return name.endsWith("." + FileConfig.CONTROLS_PROPERTIES);
	    });
	    for (final String customFile : customFiles) {
		availableFiles.add(customFile);
	    }
	}

	final Object selectedItem = configurationJComboBox.getSelectedItem();
	configurationJComboBox.setModel(new DefaultComboBoxModel<>(availableFiles.toArray(new String[0])));

	if (selectedItem != null) {
	    configurationJComboBox.setSelectedItem(selectedItem);
	}
    }

    /**
     * The method can be override to add easily content in the center panel
     */
    protected void addCustomElementsInCenterPanel(final JPanel superCenterPanel) {
	// Nothing to do here
    }

    /**
     * Build the display component The display component is the parent which will put in the Main Scoop Frame and and
     * will contain all sub component like jpanel, jbutton, etc..
     */
    public void buildDisplayComponent() {
	// Set a white background
	displayComponent.setBackground(Color.WHITE);

	// Disable the meta data button (It will be enable later)
	metadataButton.setEnabled(false);
	// Disable the data button (It will be enable later)
	dataButton.setEnabled(false);

	// ------------------------------------------------------------------------------------------------------
	// Create the center panel and set the background
	centerPanel = new JPanel(new GridBagLayout());
	centerPanel.setBorder(
		BorderFactory.createTitledBorder(Messages.getMessage("gui.homeview.jpanel-title.data-control")));
	centerPanel.setBackground(Color.WHITE);
	// Create the GridBagConstraints to manage the layout
	final GridBagConstraints constraints = new GridBagConstraints();

	// ------------------------------------------------------------------------------------------------------

	// Create the first field "Data to control"
	dataToControlLabel = new JLabel(Messages.getMessage("bpc-gui.data-control"));
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	centerPanel.add(dataToControlLabel, constraints);
	// ------------------------------------------------------------------------------------------------------

	// Create the file chooser panel
	final JPanel fileChosserPanel = new JPanel();
	fileChosserPanel.setBackground(Color.WHITE);

	// Create the file chooser textfield and configure it
	fileChooseTextField.setColumns(20);
	fileChooseTextField.setEditable(false);

	// add components to the panel
	fileChosserPanel.add(fileChooseTextField);
	fileChosserPanel.add(chooseFileButton);
	fileChosserPanel.setPreferredSize(new Dimension(400, 40));

	// Define constraints
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	centerPanel.add(fileChosserPanel, constraints);
	// ------------------------------------------------------------------------------------------------------

	// Create the configuration label and textfield
	final JLabel configurationLabel = new JLabel(Messages.getMessage("bpc-gui.configuration"));
	// Define constraints
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	centerPanel.add(configurationLabel, constraints);

	final JPanel configurationPanel = new JPanel();
	configurationPanel.setBackground(Color.WHITE);
	configurationJComboBox = new JComboBox<>();
	updateConfigurationJComboBox();

	final int configurationButtonImgWidth = 22;
	final URL resource = getClass().getClassLoader().getResource("icons/gear.png");
	configurationButton = new JButton(ImageWrapperResizableIcon.getIcon(resource,
		new Dimension(configurationButtonImgWidth, configurationButtonImgWidth)));
	configurationButton
		.setPreferredSize(new Dimension(configurationButtonImgWidth + 2, configurationButtonImgWidth + 2));
	configurationPanel.add(configurationJComboBox);
	configurationPanel.add(configurationButton);

	// Define constraints
	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	centerPanel.add(configurationPanel, constraints);

	// ------------------------------------------------------------------------------------------------------
	// Create the control panel
	final JPanel controlPanel = getControlPanel();

	// Create the control label
	controlLabel = new JLabel(Messages.getMessage("bpc-gui.controls"));
	constraints.gridx = 0;
	constraints.gridy = 5;
	constraints.gridwidth = GridBagConstraints.REMAINDER;

	// Add the control label to the center panel
	centerPanel.add(controlLabel, constraints);

	// Define constraints
	constraints.gridx = 0;
	constraints.gridy = 6;
	constraints.gridwidth = GridBagConstraints.REMAINDER;

	// add the control panel to the center panel
	centerPanel.add(controlPanel, constraints);

	final JPanel superCenterPanel = new JPanel();
	superCenterPanel.setLayout(new BoxLayout(superCenterPanel, BoxLayout.PAGE_AXIS));
	// Add empty space at the top of the center panel
	superCenterPanel.add(Box.createRigidArea(new Dimension(5, 100)));

	/* Stage Matthieu Maros Juin 2016 Replace scoop3.png by Carousel */

	// Create the Scoop 3 Image Panel
	// final JLabel labelName = new JLabel(new ImageIcon(getClass().getResource("/scoop3.png")));
	// labelName.setAlignmentX(Component.CENTER_ALIGNMENT);
	// superCenterPanel.add(labelName);

	superCenterPanel.setBackground(Color.WHITE);

	/* Stage Matthieu Maros Juin 2016 Replace scoop3.png by Carousel */

	// Create the Scoop 3 Image Panel
	// final JLabel labelName = new JLabel(new ImageIcon(getClass().getResource("/scoop3.png")));
	// labelName.setAlignmentX(Component.CENTER_ALIGNMENT);
	// superCenterPanel.add(labelName);

	// superCenterPanel.add(carousel, BorderLayout.NORTH);

	/* Fin stage Matthieu */

	// Add empty space at the top of the center panel
	superCenterPanel.add(Box.createRigidArea(new Dimension(5, 20)));
	superCenterPanel.add(centerPanel);
	addCustomElementsInCenterPanel(superCenterPanel);

	// to avoid that "superCenterPanel" takes all the space available
	final JPanel gigaCenterPanel = new JPanel();
	gigaCenterPanel.setBackground(Color.WHITE);
	gigaCenterPanel.add(superCenterPanel);

	// Add the center panel to the global display component
	displayComponent.add(gigaCenterPanel, BorderLayout.CENTER);

    }

    /**
     * The method can be override to modify easily the HomeViewImpl
     */
    protected void customInit() {
	// Nothing to do here
    }

    /**
     * @return the configurationButton
     */
    protected JButton getConfigurationButton() {
	return configurationButton;
    }

    protected JPanel getControlPanel() {
	final JPanel controlPanel = new JPanel();
	controlPanel.setBackground(Color.WHITE);

	// Add controls buttons to the control panel
	formatButton.setEnabled(false);
	controlPanel.add(formatButton);
	controlPanel.add(metadataButton);
	controlPanel.add(dataButton);

	return controlPanel;
    }

    /**
     * Met ajour le Carousel avec les drivers Instancier autant de DriverPanel que de drivers disponible
     * (availableDrivers) et les ajouter au Carousel
     *
     */
    public void updateCarouselWithDrivers(final List<IDriver> availableDrivers) {
	try {
	    final List<DriverPanel> entree = new ArrayList<DriverPanel>();

	    for (final IDriver driver : availableDrivers) {
		DriverPanel p;
		p = new DriverPanel(null, driver.getBackgroundImage());
		entree.add(p);

	    }

	    carousel.setComposants(entree);
	    carousel.setPreferredSize(new Dimension(1000, 200));

	    /* TimerPicker X 2 */

	} catch (final IOException e) {
	    SC3Logger.LOGGER.warn("Cannot add carousel to home frame");
	}
    }

    public JPanel getCenterPanel() {
	return this.centerPanel;
    }

    public JLabel getDataToControlLabel() {
	return this.dataToControlLabel;
    }

    public JLabel getControlLabel() {
	return this.controlLabel;
    }

}
