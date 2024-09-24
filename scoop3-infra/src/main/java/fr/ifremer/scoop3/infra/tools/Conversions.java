package fr.ifremer.scoop3.infra.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

/**
 * Diverse function
 *
 */
public class Conversions {

    public static final String DATE_FORMAT_DDMMYYYY = "dd/MM/yyyy";
    public static final String DATE_FORMAT_DDMMYYYY_HHMM = "dd/MM/yyyy HH:mm";
    public static final String DATE_FORMAT_DDMMYYYY_HHMMSS = "dd/MM/yyyy HH:mm:ss";
    public static final String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Convert an angle in Degree to its value in Radian
     *
     * @param angleInDegree
     * @return
     */
    public static double convertAngleFromDegreeToRadian(final double angleInDegree) {
	return angleInDegree * ((2 * Math.PI) / 360.0d);
    }

    /**
     * Convert Decimal degrees to medatlas string
     *
     * Output latitude format : "NXX XX.XX" or "SXX XX.XX"
     *
     * Output longitude format : "EXXX XX.XX" or "WXXX XX.XX"
     *
     * @param decimalDegrees
     *            positive or negative
     * @param isLatitude
     * @return the latitude or longitude in medatlas format
     */
    public static String convertDecimalToString(final double decimalDegrees, final boolean isLatitude) {

	String sign;
	if (isLatitude) {
	    if (decimalDegrees >= 0) {
		sign = "N";
	    } else {
		sign = "S";
	    }
	} else {
	    if (decimalDegrees >= 0) {
		sign = "E";
	    } else {
		sign = "W";
	    }
	}
	final int degrees = (int) Math.abs(decimalDegrees);
	final double minutesFloat = (Math.abs(decimalDegrees) - degrees) * 60;
	final int minutes = (int) Math.abs(minutesFloat);
	final double hunderdMinutesFloat = (minutesFloat - minutes) * 100;
	final long hundredMinutes = Math.abs(Math.round(hunderdMinutesFloat));

	final int nbDigits = (isLatitude) ? 2 : 3;
	return String.format("%1s%0" + nbDigits + "d %02d.%02d", sign, degrees, minutes, hundredMinutes);
    }

    /**
     *
     */
    public static float[] convertDoubleListToFloatArray(final LinkedList<Double> list) {
	final float[] floats = new float[list.size()];
	float number;
	for (int i = 0; i < list.size(); i++) {
	    number = list.get(i).floatValue();
	    floats[i] = number;
	}
	return floats;
    }

    /**
     * Convert medatlas string to Decimal degrees
     *
     * Input latitude format : "NXX XX.XX" or "SXX XX.XX"
     *
     * Input longitude format : "EXXX XX.XX" or "WXXX XX.XX"
     *
     * @param latOrLonString
     * @param isLatitude
     * @return the latitude or longitude as Decimal degrees
     */
    public static double convertLatOrLonStringToDecimalDegrees(final String latOrLonString, final boolean isLatitude) {
	double decimalDegrees;
	double coef;

	double degrees;
	double minutes;
	double hundredMinutes;

	// N38 30.00 or S50 00.00 or E010 00.00 or W030 00.00
	if (isLatitude) {
	    if (latOrLonString.startsWith("N")) {
		coef = 1.0d;
	    } else {
		coef = -1.0d;
	    }
	    final String degreesStr = latOrLonString.substring(1, 3);
	    final String minutesStr = latOrLonString.substring(4, 6);
	    final String hundredMinutesStr = latOrLonString.substring(7, latOrLonString.length());

	    degrees = Double.valueOf(degreesStr);
	    minutes = Double.valueOf(minutesStr);
	    hundredMinutes = Double.valueOf(hundredMinutesStr);
	} else {
	    if (latOrLonString.startsWith("E")) {
		coef = 1.0d;
	    } else {
		coef = -1.0d;
	    }

	    final String degreesStr = latOrLonString.substring(1, 4);
	    final String minutesStr = latOrLonString.substring(5, 7);
	    final String hundredMinutesStr = latOrLonString.substring(8, latOrLonString.length());

	    degrees = Double.valueOf(degreesStr);
	    minutes = Double.valueOf(minutesStr);
	    hundredMinutes = Double.valueOf(hundredMinutesStr);
	}

	decimalDegrees = coef * (degrees + (minutes / 60d) + (hundredMinutes / 100d / 60d));
	// SC3Logger.LOGGER.debug(latOrLonString + " VS " + convertDecimalToString(decimalDegrees, isLatitude));

	return decimalDegrees;
    }

    /**
     *
     */
    public static Double[] convertNumberListToDoubleArray(final List<? extends Number> list) {
	final Double[] doubleArray = new Double[list.size()];
	Double number;
	for (int i = 0; i < list.size(); i++) {
	    if (list.get(i) != null) {
		number = list.get(i).doubleValue();
	    } else {
		number = null;
	    }
	    doubleArray[i] = number;
	}
	return doubleArray;
    }

    /**
     * format a date with the pattern dd/MM/yyyy.
     *
     * @param date
     *            a Date
     * @return the formatted string
     */
    public static String formatDate(final Date date) {
	return formatDate(date.getTime());
    }

    /**
     * format a date with the pattern dd/MM/yyyy.
     *
     * @param date
     *            a Date
     * @return the formatted string
     */
    public static String formatDate(final long date) {
	return getSimpleDateFormat(DATE_FORMAT_DDMMYYYY).format(date);
    }

    /**
     * format a date with the pattern yyyy-MM-dd'T'HH:mm:ss.SSSZ.
     *
     * @param date
     *            a Date
     * @return the formatted string
     */
    public static String formatDateAndHourIso(final Date date) {
	return formatDateAndHourIso(date.getTime());
    }

    /**
     * format a date with the pattern yyyy-MM-dd'T'HH:mm:ss.SSSZ.
     *
     * @param date
     *            a Date
     * @return the formatted string
     */
    public static String formatDateAndHourIso(final long date) {
	return getSimpleDateFormat(DATE_FORMAT_ISO).format(date);
    }

    /**
     * format a date with the pattern dd/MM/yyyy hh:mm.
     *
     * @param date
     *            a Date
     * @return the formatted string
     */
    public static String formatDateAndHourMin(final Date date) {
	return formatDateAndHourMin(date.getTime());
    }

    /**
     * format a date with the pattern dd/MM/yyyy hh:mm.
     *
     * @param date
     *            a Date
     * @return the formatted string
     */
    public static String formatDateAndHourMin(final long date) {
	return getSimpleDateFormat(DATE_FORMAT_DDMMYYYY_HHMM).format(date);
    }

    /**
     * format a date with the pattern dd/MM/yyyy HH:mm:ss.
     *
     * @param date
     *            a Date
     * @return the formatted string
     */
    public static String formatDateAndHourMinSec(final Date date) {
	return formatDateAndHourMinSec(date.getTime());
    }

    /**
     * format a date with the pattern dd/MM/yyyy HH:mm:ss.
     *
     * @param date
     *            a Date
     * @return the formatted string
     */
    public static String formatDateAndHourMinSec(final long date) {
	return getSimpleDateFormat(DATE_FORMAT_DDMMYYYY_HHMMSS).format(date);
    }

    /**
     * A simple date format. -> use this method to change all call in one time if needed.
     *
     * @param format
     * @return
     */
    public static SimpleDateFormat getSimpleDateFormat(final String format) {
	final SimpleDateFormat sdf = new SimpleDateFormat(format);
	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	sdf.setLenient(false);
	return sdf;
    }

    /**
     * parse a string with the pattern dd/MM/yyyy.
     *
     * @param date
     *            a String
     * @return the date parsed
     * @throws ParseException
     */
    public static Date parseDate(final String date) throws ParseException {
	return getSimpleDateFormat(DATE_FORMAT_DDMMYYYY).parse(date);
    }

    /**
     * parse a string with the pattern yyyy-MM-dd'T'HH:mm:ss.SSSZ.
     *
     * @param date
     *            a String
     * @return the date parsed
     * @throws ParseException
     */
    public static Date parseDateAndHourIso(final String date) throws ParseException {
	return getSimpleDateFormat(DATE_FORMAT_ISO).parse(date);
    }

    /**
     * parse a string with the pattern dd/MM/yyyy hh:mm.
     *
     * @param date
     *            a String
     * @return the date parsed
     * @throws ParseException
     */
    public static Date parseDateAndHourMin(final String date) throws ParseException {
	return getSimpleDateFormat(DATE_FORMAT_DDMMYYYY_HHMM).parse(date);
    }

    private Conversions() {

    }

    public static Calendar getGMTCalendar() {
	return Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    }

    public static Calendar getUTCCalendar() {
	return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }
}
