package fr.ifremer.scoop3.gui.data;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.activetree.jswing.AtCalendar;

import fr.ifremer.scoop3.chart.model.ChartPhysicalVariable;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneAbstract;
import fr.ifremer.scoop3.chart.view.scrollpane.JScoop3ChartScrollPaneForTimeserie;
import fr.ifremer.scoop3.gui.data.slider.RangeSlider;
import fr.ifremer.scoop3.gui.utils.Dialogs;
import fr.ifremer.scoop3.infra.i18n.Messages;
import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.mail.UnhandledException;
import fr.ifremer.scoop3.infra.tools.Conversions;

public class ChangeAxisMinMaxDialog extends JDialog {

    /**
     * Float precision is managed with the COEF. To use x.y => COEF == 10. To use x.yy => COEF == 100
     */
    private static final int COEF = 10;
    private static final int DATE_END = 1;
    private static final int DATE_START = 0;
    private static final String JTEXTFIED_STRING_FORMAT = "%.1f";
    private static final long serialVersionUID = -7764523233695672830L;

    private MinOrMaxJTextField abscissaMaxJTextField;
    private MinOrMaxJTextField abscissaMinJTextField;
    private RangeSlider abscissaRangeSlider;
    private transient ChangeListener abscissaRangeSliderChangeListener;
    private final JButton cancelButton;
    private transient ActionListener cancelButtonActionListener;
    private Date endDate;
    private JButton endDateButton;
    private transient ActionListener endDateButtonActionListener;
    private JLabel endTimeLabel;
    private Date maxDate;
    private Date minDate;
    private MinOrMaxJTextField ordinateMaxJTextField;
    private MinOrMaxJTextField ordinateMinJTextField;
    private RangeSlider ordinateRangeSlider;
    private transient ChangeListener ordinateRangeSliderChangeListener;
    private final ArrayList<JWindow> popupCollection = new ArrayList<JWindow>();
    private int returnValue;
    private Date startDate;
    private JButton startDateButton;
    private transient ActionListener startDateButtonActionListener;
    private JLabel startTimeLabel;
    private final JButton validateButton;
    private transient ActionListener validateButtonActionListener;
    private boolean propagateValueFromRangeSlider = true;

    /**
     * @param scoop3Frame
     * @param jScoop3ChartPanel
     */
    public ChangeAxisMinMaxDialog(final Frame scoop3Frame, final JScoop3ChartScrollPaneAbstract jScoop3ChartPanel) {
	super(scoop3Frame, true);

	setLayout(new BorderLayout(5, 5));

	add(new JLabel(Messages.getMessage("bpc-gui.change-axis-min-max")), BorderLayout.NORTH);

	final JPanel centerPanel = new JPanel();
	centerPanel.setLayout(new GridLayout(0, 1, 5, 5));

	addAxis(centerPanel, jScoop3ChartPanel, true);
	addAxis(centerPanel, jScoop3ChartPanel, false);
	add(centerPanel, BorderLayout.CENTER);

	final JPanel southPanel = new JPanel();
	validateButton = new JButton(Messages.getMessage("bpc-gui.button-validate"));
	cancelButton = new JButton(Messages.getMessage("bpc-gui.button-cancel"));
	southPanel.add((new JPanel()).add(validateButton));
	southPanel.add((new JPanel()).add(cancelButton));
	add(southPanel, BorderLayout.SOUTH);

	addListeners();

	returnValue = JOptionPane.NO_OPTION;

	final Dimension dialogDim = new Dimension(600, 300);
	setPreferredSize(dialogDim);

	pack();
	repaint();
	setLocationRelativeTo(null);
	setResizable(false);
    }

    public JWindow createCalendarPopup(final Component invoker, final int fieldType) {
	final Calendar cal = Conversions.getUTCCalendar();

	if (fieldType == DATE_START) {
	    cal.setTime(startDate);
	} else if (fieldType == DATE_END) {
	    cal.setTime(endDate);
	}

	// FIXME Locale seams to not work
	AtCalendarExtended.setDefaultLocale(Messages.getLocale());
	final AtCalendarExtended calendar = new AtCalendarExtended(cal, false);
	calendar.setLocale(Messages.getLocale());
	calendar.updateUiControls();

	final JWindow popupWindow = new JWindow(this);
	final Container c = popupWindow.getContentPane();
	c.setLayout(new BorderLayout());
	final JPanel p = new JPanel() {
	    private static final long serialVersionUID = 1L;
	    Insets insets = new Insets(5, 5, 5, 5);

	    @Override
	    public Insets getInsets() {
		return insets;
	    }
	};
	p.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

	p.setLayout(new BorderLayout());
	p.add(calendar, BorderLayout.CENTER);
	c.add(p, BorderLayout.CENTER);
	popupWindow.setSize(new Dimension(220, 250));

	calendar.addActionListener(new ActionHandler(popupWindow, calendar, fieldType));

	final Point scrLoc = invoker.getLocationOnScreen();
	final int x = scrLoc.x + (invoker.getSize().width / 2);
	final int y = scrLoc.y + (invoker.getSize().height / 2);
	popupWindow.setLocation(x, y);
	return popupWindow;
    }

    /**
     * @return the Abscissa max value
     */
    public Float getAbscissaMaxValue() {
	if (abscissaMaxJTextField == null) {
	    if (endDate != null) {
		// The abscissa is a Date
		return (float) endDate.getTime();
	    }
	    return null;
	}
	return Float.parseFloat(abscissaMaxJTextField.getText().replace(",", "."));
    }

    /**
     * @return the Abscissa min value
     */
    public Float getAbscissaMinValue() {
	if (abscissaMinJTextField == null) {
	    if (startDate != null) {
		// The abscissa is a Date
		return (float) startDate.getTime();
	    }
	    return null;
	}
	return Float.parseFloat(abscissaMinJTextField.getText().replace(",", "."));
    }

    /**
     * @return the Ordinate max value
     */
    public Float getOrdinateMaxValue() {
	if (ordinateMaxJTextField == null) {
	    if (endDate != null) {
		// The ordinate is a Date
		return (float) endDate.getTime();
	    }
	    return null;
	} else {
	    // Normal case
	    return Float.parseFloat(ordinateMaxJTextField.getText().replace(",", "."));
	}
    }

    /**
     * @return the Ordinate min value
     */
    public Float getOrdinateMinValue() {
	if (ordinateMinJTextField == null) {
	    if (startDate != null) {
		// The ordinate is a Date
		return (float) startDate.getTime();
	    }
	    return null;
	} else {
	    // Normal case
	    return Float.parseFloat(ordinateMinJTextField.getText().replace(",", "."));
	}
    }

    /**
     * @return JOptionPane.YES_OPTION or JOptionPane.NO_OPTION
     */
    public int updateAxis() {
	setVisible(true);
	return returnValue;
    }

    /**
     * Create a new panel with a RangeSlider and its Min/Max JTextField
     *
     * @param centerPanel
     * @param jScoop3ChartPanel
     * @param isAbscissa
     */
    private void addAxis(final JPanel centerPanel, final JScoop3ChartScrollPaneAbstract jScoop3ChartPanel,
	    final boolean isAbscissa) {

	final ChartPhysicalVariable physicalVar = (isAbscissa) ? jScoop3ChartPanel.getAbscissaPhysicalVar()
		: jScoop3ChartPanel.getOrdinatePhysicalVar();
	final int currentStation = jScoop3ChartPanel.getCurrentStation();

	if (!physicalVar.isADate()) {
	    final JPanel axisPanel = new JPanel();
	    axisPanel.setLayout(new BorderLayout());

	    axisPanel.add(new JLabel(physicalVar.getLabel()), BorderLayout.NORTH);

	    final RangeSlider rangeSlider = createSlider(jScoop3ChartPanel, isAbscissa, physicalVar, currentStation);

	    axisPanel.add(rangeSlider, BorderLayout.CENTER);

	    final MinOrMaxJTextField minJTextField = new MinOrMaxJTextField(rangeSlider, true);
	    final MinOrMaxJTextField maxJTextField = new MinOrMaxJTextField(rangeSlider, false);

	    minJTextField.setText(rangeSlider.getValue());
	    maxJTextField.setText(rangeSlider.getUpperValue());

	    final JPanel jTextFieldPanel = new JPanel();
	    jTextFieldPanel.add(new JLabel("Min"));
	    jTextFieldPanel.add(minJTextField);
	    jTextFieldPanel.add(new JLabel("Max"));
	    jTextFieldPanel.add(maxJTextField);

	    axisPanel.add(jTextFieldPanel, BorderLayout.SOUTH);

	    centerPanel.add(axisPanel);

	    if (isAbscissa) {
		abscissaRangeSlider = rangeSlider;
		abscissaMinJTextField = minJTextField;
		abscissaMaxJTextField = maxJTextField;
	    } else {
		ordinateRangeSlider = rangeSlider;
		ordinateMinJTextField = minJTextField;
		ordinateMaxJTextField = maxJTextField;
	    }
	} else {
	    long minValueLong;
	    long maxValueLong;
	    if (isAbscissa) {
		minValueLong = (long) jScoop3ChartPanel.getMinAbscissaPhysVal();
		maxValueLong = (long) jScoop3ChartPanel.getMaxAbscissaPhysVal();
	    } else {
		minValueLong = (long) jScoop3ChartPanel.getMinOrdinatePhysVal();
		maxValueLong = (long) jScoop3ChartPanel.getMaxOrdinatePhysVal();
	    }
	    minDate = new Date(minValueLong);
	    maxDate = new Date(maxValueLong);

	    // Get the position of the scrollbar
	    int scrollBarStart;
	    int scrollBarEnd;
	    int scrollBarMax;
	    if (isAbscissa) {
		scrollBarStart = jScoop3ChartPanel.getHorizontalScrollBar().getValue();
		scrollBarEnd = scrollBarStart + jScoop3ChartPanel.getHorizontalScrollBar().getModel().getExtent();
		scrollBarMax = jScoop3ChartPanel.getHorizontalScrollBar().getModel().getMaximum();
	    } else {
		scrollBarStart = jScoop3ChartPanel.getVerticalScrollBar().getValue();
		scrollBarEnd = scrollBarStart + jScoop3ChartPanel.getVerticalScrollBar().getModel().getExtent();
		scrollBarMax = jScoop3ChartPanel.getVerticalScrollBar().getModel().getMaximum();
	    }

	    // Compute the Min and Max displayed
	    final float actualMinFloat = (((float) (maxValueLong - minValueLong) * scrollBarStart) / (scrollBarMax))
		    + minValueLong;
	    float actualMaxFloat = (((float) (maxValueLong - minValueLong) * scrollBarEnd) / (scrollBarMax))
		    + minValueLong;

	    actualMaxFloat = (((actualMaxFloat <= minValueLong) || (actualMaxFloat >= maxValueLong)) ? maxValueLong
		    : actualMaxFloat);

	    final JPanel axisPanel = new JPanel();
	    axisPanel.setLayout(new BorderLayout());

	    axisPanel.add(new JLabel(physicalVar.getLabel()), BorderLayout.NORTH);

	    final JPanel datesPanel = new JPanel();
	    datesPanel.setLayout(new GridLayout(0, 3, 5, 5));

	    datesPanel.add(new JLabel(Messages.getMessage("gui.change-axis-dialog.start-date")));
	    startDate = new Date((long) actualMinFloat);
	    startTimeLabel = new JLabel(Conversions.formatDateAndHourMinSec(startDate));
	    datesPanel.add(startTimeLabel);
	    final URL resourceBack = getClass().getClassLoader().getResource("icons/1day.png");
	    startDateButton = new JButton(new ImageIcon(resourceBack));
	    startDateButton.setPreferredSize(new Dimension(20, 20));

	    startDateButtonActionListener = (final ActionEvent e) -> {
		cleanPopups();
		final JWindow popupWindow = createCalendarPopup((Component) e.getSource(), DATE_START);
		popupCollection.add(popupWindow);
		popupWindow.setVisible(true);
	    };

	    startDateButton.addActionListener(startDateButtonActionListener);
	    JPanel buttonPanel = new JPanel();
	    buttonPanel.add(startDateButton);
	    datesPanel.add(buttonPanel);

	    datesPanel.add(new JLabel(Messages.getMessage("gui.change-axis-dialog.end-date")));
	    endDate = new Date((long) actualMaxFloat);
	    endTimeLabel = new JLabel(Conversions.formatDateAndHourMinSec(endDate));
	    datesPanel.add(endTimeLabel);
	    endDateButton = new JButton(new ImageIcon(resourceBack));
	    endDateButton.setPreferredSize(new Dimension(20, 20));

	    endDateButtonActionListener = (final ActionEvent e) -> {
		cleanPopups();
		final JWindow popupWindow = createCalendarPopup((Component) e.getSource(), DATE_END);
		popupCollection.add(popupWindow);
		popupWindow.setVisible(true);
	    };

	    endDateButton.addActionListener(endDateButtonActionListener);
	    buttonPanel = new JPanel();
	    buttonPanel.add(endDateButton);
	    datesPanel.add(buttonPanel);

	    axisPanel.add(datesPanel, BorderLayout.CENTER);

	    final JPanel axisPanel2 = new JPanel();
	    axisPanel2.add(axisPanel);
	    centerPanel.add(axisPanel2);
	}
    }

    /**
     * Add all listeners
     */
    private void addListeners() {
	if (abscissaRangeSlider != null) {
	    abscissaRangeSliderChangeListener = (final ChangeEvent e) -> {
		cleanPopups();

		if (propagateValueFromRangeSlider) {
		    final int minValue = ((RangeSlider) e.getSource()).getValue();
		    final int maxValue = ((RangeSlider) e.getSource()).getUpperValue();

		    abscissaMinJTextField.setRangeSliderToUpdate(null);
		    abscissaMinJTextField.setText(minValue);
		    abscissaMinJTextField.setRangeSliderToUpdate(abscissaRangeSlider);

		    abscissaMaxJTextField.setRangeSliderToUpdate(null);
		    abscissaMaxJTextField.setText(maxValue);
		    abscissaMaxJTextField.setRangeSliderToUpdate(abscissaRangeSlider);
		}
	    };
	    abscissaRangeSlider.addChangeListener(abscissaRangeSliderChangeListener);

	    abscissaMinJTextField.addFocusListener(abscissaMinJTextField);
	    abscissaMaxJTextField.addFocusListener(abscissaMaxJTextField);
	}

	if (ordinateRangeSlider != null) {
	    ordinateRangeSliderChangeListener = (final ChangeEvent e) -> {
		cleanPopups();
		if (propagateValueFromRangeSlider) {
		    final int minValue = ((RangeSlider) e.getSource()).getValue();
		    final int maxValue = ((RangeSlider) e.getSource()).getUpperValue();

		    ordinateMinJTextField.setRangeSliderToUpdate(null);
		    ordinateMinJTextField.setText(minValue);
		    ordinateMinJTextField.setRangeSliderToUpdate(ordinateRangeSlider);

		    ordinateMaxJTextField.setRangeSliderToUpdate(null);
		    ordinateMaxJTextField.setText(maxValue);
		    ordinateMaxJTextField.setRangeSliderToUpdate(ordinateRangeSlider);
		}
	    };
	    ordinateRangeSlider.addChangeListener(ordinateRangeSliderChangeListener);

	    ordinateMinJTextField.addFocusListener(ordinateMinJTextField);
	    ordinateMaxJTextField.addFocusListener(ordinateMaxJTextField);
	}

	validateButtonActionListener = (final ActionEvent e) -> {
	    returnValue = JOptionPane.YES_OPTION;
	    removeListeners();
	    setVisible(false);
	};
	validateButton.addActionListener(validateButtonActionListener);

	cancelButtonActionListener = (final ActionEvent e) -> {
	    returnValue = JOptionPane.NO_OPTION;
	    removeListeners();
	    setVisible(false);
	};
	cancelButton.addActionListener(cancelButtonActionListener);
    }

    /**
     * Remove existing popups (if needed)
     */
    private void cleanPopups() {
	for (int i = 0; i < popupCollection.size(); i++) {
	    final JWindow window = popupCollection.get(i);
	    if (window != null) {
		window.dispose();
	    }
	}
    }

    /**
     * Create a new RangeSlider with "float" management.
     *
     * @param jScoop3ChartPanel
     * @param isAbscissa
     * @param physicalVar
     * @param currentStation
     * @return
     */
    private RangeSlider createSlider(final JScoop3ChartScrollPaneAbstract jScoop3ChartPanel, final boolean isAbscissa,
	    final ChartPhysicalVariable physicalVar, final int currentStation) {

	// Get Min / Max values
	double minValueFloat;
	double maxValueFloat;
	if (isAbscissa) {
	    minValueFloat = jScoop3ChartPanel.getMinAbscissaPhysVal();
	    maxValueFloat = jScoop3ChartPanel.getMaxAbscissaPhysVal();
	} else {
	    minValueFloat = jScoop3ChartPanel.getMinOrdinatePhysVal();
	    maxValueFloat = jScoop3ChartPanel.getMaxOrdinatePhysVal();
	}

	for (final double value : physicalVar.getPhysicalValuesByStation().get(currentStation)) {
	    if (!Double.isInfinite(value)) {
		minValueFloat = Math.min(minValueFloat, value);
		maxValueFloat = Math.max(maxValueFloat, value);
	    }
	}
	// Use COEF to simulate float management
	minValueFloat *= COEF;
	maxValueFloat *= COEF;

	RangeSlider rangeSlider = null;
	try {
	    rangeSlider = new RangeSlider((int) Math.round(minValueFloat), (int) Math.round(maxValueFloat) + 1);
	} catch (final IllegalArgumentException e) {
	    final UnhandledException exception = new UnhandledException("Création de rangeSlider, minValueFloat : "
		    + minValueFloat + " / maxValueFloat : " + maxValueFloat + " / Round minValueFloat : "
		    + Math.round(minValueFloat) + " / Round maxValueFloat : " + Math.round(maxValueFloat), e);
	}

	// Compute major ticks
	int majorTick = (int) ((maxValueFloat - minValueFloat) / 10);
	if ((majorTick < 10) && ((majorTick > (maxValueFloat - minValueFloat)) || (majorTick == 0))) {
	    majorTick = 1;
	}

	// Compute the labels
	final Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
	for (int index = 0; index < 20; index++) {
	    final double tickValueFloat = minValueFloat + (index * majorTick);
	    final int tickValue = (int) Math.round(tickValueFloat);
	    String tickStr;
	    if ((maxValueFloat - minValueFloat) > (20 * COEF)) {
		tickStr = String.format("%.0f", ((float) tickValue) / COEF);
	    } else {
		tickStr = String.format("%.1f", ((float) tickValue) / COEF);
	    }
	    labelTable.put(tickValue, new JLabel(tickStr));
	}
	if (rangeSlider != null) {
	    rangeSlider.setLabelTable(labelTable);
	    rangeSlider.setMajorTickSpacing(majorTick);
	    rangeSlider.setPaintTicks(true);
	    rangeSlider.setPaintLabels(true);
	}

	// Get the position of the scrollbar
	int scrollBarStart;
	int scrollBarEnd;
	int scrollBarMax;
	if (isAbscissa) {
	    scrollBarStart = jScoop3ChartPanel.getHorizontalScrollBar().getValue();
	    scrollBarEnd = scrollBarStart + jScoop3ChartPanel.getHorizontalScrollBar().getModel().getExtent();
	    scrollBarMax = jScoop3ChartPanel.getHorizontalScrollBar().getModel().getMaximum();
	} else {
	    scrollBarStart = jScoop3ChartPanel.getVerticalScrollBar().getValue();
	    scrollBarEnd = scrollBarStart + jScoop3ChartPanel.getVerticalScrollBar().getModel().getExtent();
	    scrollBarMax = jScoop3ChartPanel.getVerticalScrollBar().getModel().getMaximum();
	}

	// Compute the Min and Max displayed
	double actualMinFloat = (((maxValueFloat - minValueFloat) * scrollBarStart) / (scrollBarMax)) + minValueFloat;
	double actualMaxFloat = (((maxValueFloat - minValueFloat) * scrollBarEnd) / (scrollBarMax)) + minValueFloat;
	if (jScoop3ChartPanel instanceof JScoop3ChartScrollPaneForTimeserie) {
	    actualMinFloat = maxValueFloat - ((maxValueFloat - minValueFloat) * ((float) scrollBarEnd / scrollBarMax));
	    actualMaxFloat = maxValueFloat
		    - ((maxValueFloat - minValueFloat) * ((float) scrollBarStart / scrollBarMax));
	}

	final int actualMin = (int) Math.round(actualMinFloat);
	int actualMax = (int) (Math.round(actualMaxFloat) + 1);
	actualMax = (int) Math
		.round(((actualMax <= minValueFloat) || (actualMax >= maxValueFloat)) ? maxValueFloat : actualMax);

	// Set the default values
	if (rangeSlider != null) {
	    rangeSlider.setUpperValue(actualMax);
	    rangeSlider.setValue(actualMin);
	    rangeSlider.setUpperValue(actualMax);
	}

	return rangeSlider;
    }

    private void removeListeners() {
	if (abscissaRangeSlider != null) {
	    abscissaRangeSlider.removeChangeListener(abscissaRangeSliderChangeListener);

	    abscissaMinJTextField.removeFocusListener(abscissaMinJTextField);
	    abscissaMaxJTextField.removeFocusListener(abscissaMaxJTextField);
	}

	if (ordinateRangeSlider != null) {
	    ordinateRangeSlider.removeChangeListener(ordinateRangeSliderChangeListener);

	    ordinateMinJTextField.removeFocusListener(ordinateMinJTextField);
	    ordinateMaxJTextField.removeFocusListener(ordinateMaxJTextField);
	}

	if (startDateButton != null) {
	    startDateButton.removeActionListener(startDateButtonActionListener);
	}

	if (endDateButton != null) {
	    endDateButton.removeActionListener(endDateButtonActionListener);
	}

	validateButton.removeActionListener(validateButtonActionListener);

	cancelButton.removeActionListener(cancelButtonActionListener);
    }

    private class ActionHandler implements ActionListener {
	AtCalendar calendar;
	int fieldType;
	JWindow popup;

	public ActionHandler(final JWindow popup, final AtCalendar calendar, final int fieldType) {
	    this.popup = popup;
	    this.calendar = calendar;
	    this.fieldType = fieldType;
	}

	@Override
	public void actionPerformed(final ActionEvent evt) {
	    final String actionCommand = evt.getActionCommand();
	    final AtCalendar localCalendar = this.calendar;
	    if (actionCommand.equals(AtCalendar.OK_ACTION_COMMAND)) {
		final Date newDate = localCalendar.getTime();

		boolean valueIsGood;
		if (fieldType == DATE_START) {
		    valueIsGood = !newDate.after(endDate);
		    if (!valueIsGood) {
			final String title = Messages.getMessage("gui.change-axis-dialog.start-date-after-end-date");
			final String message = MessageFormat.format(
				Messages.getMessage("gui.change-axis-dialog.end-date-value"),
				Conversions.formatDateAndHourMinSec(endDate))
				+ "\n"
				+ MessageFormat.format(Messages.getMessage("gui.change-axis-dialog.valid-range"),
					Conversions.formatDateAndHourMinSec(minDate),
					Conversions.formatDateAndHourMinSec(maxDate));
			SC3Logger.LOGGER.debug(title);
			Dialogs.showErrorMessage(title, message);
		    }
		} else {
		    valueIsGood = !newDate.before(startDate);
		    if (!valueIsGood) {
			final String title = Messages.getMessage("gui.change-axis-dialog.end-date-before-start-date");
			final String message = MessageFormat.format(
				Messages.getMessage("gui.change-axis-dialog.start-date-value"),
				Conversions.formatDateAndHourMinSec(startDate))
				+ "\n"
				+ MessageFormat.format(Messages.getMessage("gui.change-axis-dialog.valid-range"),
					Conversions.formatDateAndHourMinSec(minDate),
					Conversions.formatDateAndHourMinSec(maxDate));

			SC3Logger.LOGGER.debug(title);
			Dialogs.showErrorMessage(title, message);
		    }
		}

		if (valueIsGood) {
		    if ((newDate.before(minDate) || newDate.after(maxDate))) {
			final String title;
			if (fieldType == DATE_START) {
			    title = Messages.getMessage("gui.change-axis-dialog.start-date-not-in-valid-range");
			} else {
			    title = Messages.getMessage("gui.change-axis-dialog.end-date-not-in-valid-range");
			}

			final String message = MessageFormat.format(
				Messages.getMessage("gui.change-axis-dialog.valid-range"),
				Conversions.formatDateAndHourMinSec(minDate),
				Conversions.formatDateAndHourMinSec(maxDate));
			SC3Logger.LOGGER.debug(title);
			Dialogs.showErrorMessage(title, message);
		    } else {
			// The date is valid
			if (fieldType == DATE_START) {
			    startDate = localCalendar.getTime();
			    startTimeLabel.setText(Conversions.formatDateAndHourMinSec(startDate));
			} else if (fieldType == DATE_END) {
			    endDate = localCalendar.getTime();
			    endTimeLabel.setText(Conversions.formatDateAndHourMinSec(endDate));
			}
			popup.dispose();
		    }
		}
	    } else if (actionCommand.equals(AtCalendar.CANCEL_ACTION_COMMAND)) {
		popup.dispose();
	    }
	}
    }

    private class MinOrMaxJTextField extends JTextField implements FocusListener {

	private static final long serialVersionUID = 3335752110414292237L;

	private boolean minValue;
	private RangeSlider rangeSliderToUpdate;
	private int valueToSet;

	public MinOrMaxJTextField(final RangeSlider rangeSliderToUpdate, final boolean minValue) {
	    super();

	    this.rangeSliderToUpdate = rangeSliderToUpdate;
	    this.minValue = minValue;

	    setInputVerifier(new InputVerifier() {
		@Override
		public boolean verify(final JComponent input) {
		    try {
			Float.parseFloat(((JTextField) input).getText().replace(",", "."));
			return true;
		    } catch (final NumberFormatException nfe) {
			// The number is not correct
		    }
		    return false;
		}
	    });

	    setPreferredSize(new Dimension(50, 20));
	}

	@Override
	public void focusGained(final FocusEvent e) {
	    cleanPopups();
	}

	@Override
	public void focusLost(final FocusEvent e) {
	    try {
		// Check value
		final String valueStr = getText().replace(",", ".");
		valueToSet = Math.round(Float.parseFloat(valueStr) * COEF);

		if (minValue) {
		    // final int minValueInt = rangeSliderToUpdate.getMinimum();
		    final int actualMaxValueInt = rangeSliderToUpdate.getUpperValue();
		    // FAE 29386 - allows values lower than min value
		    // if (valueToSet < minValueInt) {
		    // valueToSet = minValueInt;
		    // setText(valueToSet);
		    // }
		    if (valueToSet > actualMaxValueInt) {
			valueToSet = actualMaxValueInt;
			setText(valueToSet);
		    }
		} else {
		    final int actualMinValueInt = rangeSliderToUpdate.getValue();
		    // final int maxValueInt = rangeSliderToUpdate.getMaximum();

		    if (valueToSet < actualMinValueInt) {
			valueToSet = actualMinValueInt;
			setText(valueToSet);
		    }
		    // FAE 29386 - allows values greater than max value
		    // if (valueToSet > maxValueInt) {
		    // valueToSet = maxValueInt;
		    // setText(valueToSet);
		    // }
		}

		if (rangeSliderToUpdate != null) {
		    SwingUtilities.invokeLater(() -> {
			propagateValueFromRangeSlider = false;
			if (minValue) {
			    rangeSliderToUpdate.setValue(valueToSet);
			} else {
			    rangeSliderToUpdate.setUpperValue(valueToSet);
			}
			propagateValueFromRangeSlider = true;
		    });
		}
	    } catch (final NumberFormatException nfe) {
		// The number is not correct
	    }
	}

	/**
	 * @param rangeSliderToUpdate
	 *            the rangeSliderToUpdate to set
	 */
	public void setRangeSliderToUpdate(final RangeSlider rangeSliderToUpdate) {
	    this.rangeSliderToUpdate = rangeSliderToUpdate;
	}

	/**
	 * @param value
	 *            will be divided by COEF
	 */
	public void setText(final int value) {
	    final String toDisplay = String.format(JTEXTFIED_STRING_FORMAT, ((float) value) / COEF);
	    if (!getText().equals(toDisplay)) {
		super.setText(toDisplay);
	    }
	}
    }
}
