package fr.ifremer.scoop3.gui.data;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Calendar;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import com.activetree.jswing.AtCalendar;
import com.activetree.jswing.common.Debug;
import com.activetree.jswing.common.StringCellComponent;

import fr.ifremer.scoop3.gui.utils.Dialogs;
import fr.ifremer.scoop3.infra.i18n.Messages;

public class AtCalendarExtended extends AtCalendar {

    private static final int STARTING_YEAR = 1900;
    private static final int ENDING_YEAR = 2100;
    protected JFormattedTextField yearsJTextField;
    protected static String[] years;
    private JPanel mainPanel;
    private int lastSelectedDay = -1;
    private boolean isLastDayOfMonth = false;
    private int currentYear;
    private Integer newYear = null;

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AtCalendarExtended(final Calendar calendar) {
	super(calendar);
	populateYearsArrayAndReplaceIt(calendar);
    }

    public AtCalendarExtended(final Calendar cal, final boolean b) {
	super(cal, b);
	populateYearsArrayAndReplaceIt(cal);
    }

    private void populateYearsArrayAndReplaceIt(final Calendar cal) {
	// fill the array of years
	years = new String[ENDING_YEAR - STARTING_YEAR];
	for (int i = 0; i < (ENDING_YEAR - STARTING_YEAR); i++) {
	    years[i] = String.valueOf(STARTING_YEAR + i);
	}

	// remove the old label
	mainPanel = (JPanel) getComponents()[0];
	final JPanel northPanel = (JPanel) mainPanel.getComponents()[0];
	northPanel.remove(northPanel.getComponent(4));

	// create the new combo
	yearsJTextField = new JFormattedTextField();
	currentYear = cal.get(1);
	yearsJTextField.setText(String.valueOf(currentYear));
	yearsJTextField.setColumns(4);
	yearsJTextField.addFocusListener(new FocusAdapter() {
	    /*
	     * (non-Javadoc)
	     *
	     * @see java.awt.event.FocusAdapter#focusLost(java.awt.event.FocusEvent)
	     */
	    @Override
	    public void focusLost(final FocusEvent e) {
		super.focusLost(e);
		newYear = null;
		try {
		    final int year = Integer.parseInt(yearsJTextField.getText());
		    // check if year is in years array
		    checkYearAsked(year);
		    newYear = year;
		} catch (final Exception e1) {
		    yearsJTextField.setText(String.valueOf(currentYear));
		    Dialogs.showErrorMessage(Messages.getMessage("Erreur"),
			    "Wrong year. It must be between " + STARTING_YEAR + " and " + ENDING_YEAR);
		}
		if (newYear != null) {
		    yearsJTextField.setText(String.valueOf(newYear));
		    final int yearRolling = newYear - currentYear;
		    yearSelectionChanged(yearRolling);
		    currentYear = newYear;
		}
	    }
	});
	// press Enter key raise the signal 'focusLost'
	yearsJTextField.addKeyListener(new KeyAdapter() {
	    /*
	     * (non-Javadoc)
	     *
	     * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
	     */
	    @Override
	    public void keyReleased(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		    // Force to loose focus on indexTextField
		    final Object src = e.getSource();
		    if (src != yearsJTextField) {
			return;
		    }
		    mainPanel.requestFocusInWindow();
		}
	    }
	});

	// add a block if the year is out of the range
	prevYearButton.addActionListener((final ActionEvent e) -> {
	    try {
		final int inputYear = Integer.parseInt(yearsJTextField.getText());
		// check if year is in years array
		checkYearAsked(inputYear);
		currentYear = inputYear;
	    } catch (final Exception e1) {
		yearsJTextField.setText(String.valueOf(currentYear));
		yearSelectionChanged(1);
		Dialogs.showErrorMessage(Messages.getMessage("Erreur"),
			"Wrong year. It must be between " + STARTING_YEAR + " and " + ENDING_YEAR);
	    }
	});

	// add a block if the year is out of the range
	nextYearButton.addActionListener((final ActionEvent e) -> {
	    try {
		final int inputYear = Integer.parseInt(yearsJTextField.getText());
		// check if year is in years array
		checkYearAsked(inputYear);
		currentYear = inputYear;
	    } catch (final Exception e1) {
		yearsJTextField.setText(String.valueOf(currentYear));
		yearSelectionChanged(-1);
		Dialogs.showErrorMessage(Messages.getMessage("Erreur"),
			"Wrong year. It must be between " + STARTING_YEAR + " and " + ENDING_YEAR);
	    }
	});

	// add the new combo at the old place
	northPanel.add(yearsJTextField,
		new GridBagConstraints(4, 0, 1, 1, 1.0D, 1.0D, 10, 1, new Insets(0, 0, 0, 0), 0, 0));

    }

    public void checkYearAsked(final int year) throws Exception {
	if ((year < STARTING_YEAR) || (year >= ENDING_YEAR)) {
	    throw new Exception();
	}
    }

    protected void yearSelectionChanged(final int rollingAmount) {
	Debug.debug("dateChoosr.yearSelectionChanged()...rollingAmt:" + rollingAmount);

	final Calendar cal = getCalendar();
	cal.roll(1, rollingAmount);

	Debug.debug("After resetting the cal: yr=" + cal.get(1) + ", mon=" + cal.get(2) + ", dt=" + cal.get(5));

	resetUIControlValues();
	setDaySelected(lastSelectedDay);
    }

    private Calendar getCalendar() {
	if (calendar == null) {
	    Debug.debug("CREATING a NEW CALENDAR!!!");
	    calendar = Calendar.getInstance();
	}
	return calendar;
    }

    private void resetUIControlValues() {
	final Calendar cal = getCalendar();
	mainPanel.setVisible(false);

	final int monthNo = cal.get(2);
	final int yearNo = cal.get(1);
	final int dayOfMonth = cal.get(5);
	final int noOfDaysInMonth = cal.getActualMaximum(5);

	Debug.debug("moNo: " + monthNo + ", yr: " + yearNo + ", dayOfMonth: " + dayOfMonth + ", noOfdayInMon="
		+ noOfDaysInMonth);

	if (lastSelectedDay == -1) {
	    lastSelectedDay = dayOfMonth;
	}
	if (lastSelectedDay > noOfDaysInMonth) {
	    lastSelectedDay = noOfDaysInMonth;
	}

	if (isLastDayOfMonth) {
	    lastSelectedDay = noOfDaysInMonth;
	}

	yearsJTextField.setText(String.valueOf(yearNo));
	combo.setSelectedItem(months[monthNo]);

	verifyDayNumber(lastSelectedDay);

	Debug.debug(
		"resetUIControlValues(): noOfDaysInMonth: " + noOfDaysInMonth + ", lastselectedDay=" + lastSelectedDay);

	for (int i = 0; i < days.length; i++) {
	    days[i].setText("");
	}

	cal.set(5, 1);
	final int startDay = cal.get(7);

	Debug.debug("startDay=" + startDay);

	int dayNumber = 1;
	final int start = startDay - 1;
	final int end = noOfDaysInMonth + start;
	for (int i = start; i < end; i++) {
	    days[i].setText(String.valueOf(dayNumber++));
	}
	cal.set(5, lastSelectedDay);

	setDaySelected(lastSelectedDay);
	mainPanel.setVisible(true);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
	final String cmd = e.getActionCommand();
	if (cmd.equals("DAY")) {
	    Debug.debug("Action cmd: " + cmd);

	    final StringCellComponent dayButton = (StringCellComponent) e.getSource();
	    final String text = dayButton.getText();
	    if (text.length() > 0) {
		setDayOfMonth(text);
		isLastDayOfMonth = isLastDayOfMonthSelected();
	    } else {
		dayButton.setBackground(defaultBgColor);
		dayButton.setForeground(defaultFgColor);
		dayButton.setSelected(false);
		dayButton.repaint();
	    }
	} else if (cmd.equals(OK_ACTION_COMMAND)) {
	    okActionPerformed(e);
	} else if (cmd.equals(CANCEL_ACTION_COMMAND)) {
	    cancelActionPerformed(e);
	} else {
	    isLastDayOfMonth = isLastDayOfMonthSelected();

	    Debug.debug("isLastDayOfMonth ? " + isLastDayOfMonth);

	    changeCalendar(e.getActionCommand());
	}
    }

    private void okActionPerformed(final ActionEvent e) {
	notifyListeners(e);
    }

    private void cancelActionPerformed(final ActionEvent e) {
	notifyListeners(e);
    }

    private void notifyListeners(final ActionEvent e) {
	for (int i = 0; i < listeners.size(); i++) {
	    final ActionListener l = (ActionListener) listeners.get(i);
	    l.actionPerformed(e);
	}
    }

    private boolean isLastDayOfMonthSelected() {
	final Calendar cal = getCalendar();
	final int noOfDaysInMonth = cal.getActualMaximum(5);
	final int selDay = getSelectedDayOfMonth();
	return selDay == noOfDaysInMonth;
    }

    private void setDaySelected(final int selDay) {
	Debug.debug("setDaySelected()");

	for (int i = 0; i < days.length; i++) {
	    final String dayValue = days[i].getText();
	    int dayValueNo = -1;
	    if (!dayValue.equals("")) {
		dayValueNo = Integer.parseInt(dayValue);
	    }
	    if (dayValueNo == selDay) {
		Debug.debug("\tselecting...day: " + dayValueNo);

		days[i].setSelectedBackground(selectionBg);
		days[i].setSelectedForeground(selectionFg);
		days[i].setSelected(true);
		days[i].repaint();
	    } else {
		days[i].setBackground(defaultBgColor);
		days[i].setForeground(defaultFgColor);
		days[i].setSelected(false);
		days[i].repaint();
	    }
	}
    }

    private void verifyDayNumber(final int selectedDayNo) {
	final Calendar cal = getCalendar();
	final int noOfDaysInMonth = cal.getActualMaximum(5);

	if (selectedDayNo > noOfDaysInMonth) {
	    throw new RuntimeException("Selected day: " + selectedDayNo + " more than: " + noOfDaysInMonth);
	}
    }

    private void changeCalendar(final String action) {
	final Calendar cal = getCalendar();

	final int monthNo = cal.get(2);
	if (action.equals("PrevMonth")) {
	    cal.roll(2, false);
	} else if (action.equals("NextMonth")) {
	    cal.roll(2, true);
	} else {
	    if (action.equals("PrevYear")) {
		cal.roll(1, false);
	    } else if (action.equals("NextYear")) {
		cal.roll(1, true);
	    }

	    final int newMonthNo = cal.get(2);
	    if (newMonthNo != monthNo) {
		cal.roll(2, monthNo - newMonthNo);
	    }
	}

	resetUIControlValues();
    }
}
