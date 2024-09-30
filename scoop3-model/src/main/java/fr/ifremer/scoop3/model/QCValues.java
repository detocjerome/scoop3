package fr.ifremer.scoop3.model;

import java.awt.Color;
import java.util.List;

import fr.ifremer.scoop3.model.valueAndQc.QCColor;
import fr.ifremer.scoop3.model.valueAndQc.ValueAndQC;

public enum QCValues {

    QC_0, QC_1, QC_2, QC_3, QC_4, QC_5, QC_6, QC_7, QC_8, QC_9, QC_Q, QC_A, QC_B, QC_FILL_VALUE;

    private static final int QC_FILL_VALUE_VALUE = -128;
    private static final char QC_FILL_VALUE_CHAR = '?';
    private static final String QC_FILL_VALUE_STRING = "?";
    private static final int QC_Q_VALUE = -127;
    private static final char QC_Q_CHAR = 'Q';
    private static final String QC_Q_STRING = "Q";
    private static final int QC_A_VALUE = -126;
    private static final char QC_A_CHAR = 'A';
    private static final String QC_A_STRING = "A";
    private static final int QC_B_VALUE = -125;
    private static final char QC_B_CHAR = 'B';
    private static final String QC_B_STRING = "B";

    /**
     *
     */
    public static char[] convertQCValuesListToCharArray(final List<QCValues> list) {

	final char[] chars = new char[list.size()];
	int number;
	for (int i = 0; i < list.size(); i++) {
	    if (list.get(i) != null) {
		number = list.get(i).getQCValue();
		if (number >= 0) {
		    chars[i] = (char) ('0' + number);
		} else {
		    switch (number) {
		    case -128:
			chars[i] = QC_FILL_VALUE_CHAR;
			break;
		    case -127:
			chars[i] = QC_Q_CHAR;
			break;
		    case -126:
			chars[i] = QC_A_CHAR;
			break;
		    case -125:
			chars[i] = QC_B_CHAR;
			break;
		    default:
			break;
		    }
		}
	    } else {
		chars[i] = ' ';
	    }
	}
	return chars;
    }

    /**
     *
     * @param list
     * @return
     */
    public static int[] convertQCValuesListToIntArray(final List<QCValues> list) {
	final int[] ints = new int[list.size()];
	for (int i = 0; i < list.size(); i++) {
	    ints[i] = list.get(i).getQCValue();
	}
	return ints;
    }

    /**
     * Get the color to use for a QCValues
     *
     * @param intQC
     * @return
     */
    public static Color getColor(final int intQC) {
	if (getQCValues(intQC) != null) {
	    return getQCValues(intQC).getColor();
	} else {
	    return null;
	}
    }

    /**
     * @return the FillValue
     */
    public static QCValues getFillValue() {
	return QC_FILL_VALUE;
    }

    public static QCValues getQCValues(final String stringQC) {
	final int intQC = Integer.parseInt(stringQC);
	return getQCValues(intQC);
    }

    /**
     * get the ENUM value from an int
     *
     * @param intQC
     * @return
     */
    public static QCValues getQCValues(final int intQC) {
	QCValues qc = null;
	switch (intQC) {
	case 0:
	    qc = QC_0;
	    break;
	case 1:
	    qc = QC_1;
	    break;
	case 2:
	    qc = QC_2;
	    break;
	case 3:
	    qc = QC_3;
	    break;
	case 4:
	    qc = QC_4;
	    break;
	case 5:
	    qc = QC_5;
	    break;
	case 6:
	    qc = QC_6;
	    break;
	case 7:
	    qc = QC_7;
	    break;
	case 8:
	    qc = QC_8;
	    break;
	case 9:
	    qc = QC_9;
	    break;
	case QC_Q_VALUE:
	    qc = QC_Q;
	    break;
	case QC_A_VALUE:
	    qc = QC_A;
	    break;
	case QC_B_VALUE:
	    qc = QC_B;
	    break;
	case QC_FILL_VALUE_VALUE:
	    qc = QC_FILL_VALUE;
	    break;
	default:
	    break;
	}

	return qc;
    }

    public static QCValues getQCValuesMantis32532(final int intQC) {
	QCValues qc = null;
	switch (intQC) {
	case 0:
	    qc = QC_0;
	    break;
	case 1:
	    qc = QC_1;
	    break;
	case 2:
	    qc = QC_2;
	    break;
	case 3:
	    qc = QC_3;
	    break;
	case 4:
	    qc = QC_4;
	    break;
	case 5:
	    qc = QC_5;
	    break;
	case 6:
	    qc = QC_6;
	    break;
	case 7:
	    qc = QC_7;
	    break;
	case 8:
	    qc = QC_8;
	    break;
	case 9:
	    qc = QC_9;
	    break;
	case QC_Q_VALUE:
	    qc = QC_Q;
	    break;
	case QC_A_VALUE:
	    qc = QC_A;
	    break;
	case QC_B_VALUE:
	    qc = QC_B;
	    break;
	case QC_FILL_VALUE_VALUE:
	    qc = QC_FILL_VALUE;
	    break;
	default:
	    break;
	}

	return qc;
    }

    /**
     * Return the best QC between qc1 and qc2 (that means, the lower value).
     *
     * @param qc1
     * @param qc2
     * @return
     */
    public static QCValues getBestQC(final QCValues qc1, final QCValues qc2) {
	if (qc1 == null) {
	    return qc2;
	}
	if (qc2 == null) {
	    return qc1;
	}

	// Case QC_FILL_VALUE
	if (qc1 == QC_FILL_VALUE) {
	    return qc2;
	} else if (qc2 == QC_FILL_VALUE) {
	    return qc1;
	}

	return QCValues.getQCValues(getBestQC(qc1.getQCValue(), qc2.getQCValue()));
    }

    /**
     * Return the best QC between qc1 and qc2 (that means, the lower value)
     *
     * @param qc1
     * @param qc2
     * @return
     */
    private static int getBestQC(final int qc1, final int qc2) {
	int bestQC;

	if ((qc1 == QC_5.getQCValue()) && ((qc2 == QC_3.getQCValue()) || (qc2 == QC_4.getQCValue()))) {
	    return qc1;
	}

	if ((qc2 == QC_5.getQCValue()) && ((qc1 == QC_3.getQCValue()) || (qc1 == QC_4.getQCValue()))) {
	    return qc2;
	}

	if ((qc1 == QC_Q.getQCValue())
		&& ((qc2 == QC_1.getQCValue()) || (qc2 == QC_2.getQCValue()) || (qc2 == QC_3.getQCValue())
			|| (qc2 == QC_4.getQCValue()) || (qc2 == QC_5.getQCValue()) || (qc2 == QC_6.getQCValue()))) {
	    return qc2;
	}

	if ((qc2 == QC_Q.getQCValue())
		&& ((qc1 == QC_1.getQCValue()) || (qc1 == QC_2.getQCValue()) || (qc1 == QC_3.getQCValue())
			|| (qc1 == QC_4.getQCValue()) || (qc1 == QC_5.getQCValue()) || (qc1 == QC_6.getQCValue()))) {
	    return qc1;
	}

	if ((qc1 == QC_A.getQCValue()) && ((qc2 == QC_1.getQCValue()) || (qc2 == QC_2.getQCValue())
		|| (qc2 == QC_3.getQCValue()) || (qc2 == QC_4.getQCValue()) || (qc2 == QC_5.getQCValue())
		|| (qc2 == QC_6.getQCValue()) || (qc2 == QC_Q.getQCValue()))) {
	    return qc2;
	}

	if ((qc2 == QC_A.getQCValue()) && ((qc1 == QC_1.getQCValue()) || (qc1 == QC_2.getQCValue())
		|| (qc1 == QC_3.getQCValue()) || (qc1 == QC_4.getQCValue()) || (qc1 == QC_5.getQCValue())
		|| (qc1 == QC_6.getQCValue()) || (qc1 == QC_Q.getQCValue()))) {
	    return qc1;
	}

	if ((qc1 == QC_B.getQCValue())
		&& ((qc2 == QC_1.getQCValue()) || (qc2 == QC_2.getQCValue()) || (qc2 == QC_3.getQCValue())
			|| (qc2 == QC_4.getQCValue()) || (qc2 == QC_5.getQCValue()) || (qc2 == QC_6.getQCValue())
			|| (qc2 == QC_Q.getQCValue()) || (qc2 == QC_A.getQCValue()) || (qc2 == QC_7.getQCValue()))) {
	    return qc2;
	}

	if ((qc2 == QC_B.getQCValue())
		&& ((qc1 == QC_1.getQCValue()) || (qc1 == QC_2.getQCValue()) || (qc1 == QC_3.getQCValue())
			|| (qc1 == QC_4.getQCValue()) || (qc1 == QC_5.getQCValue()) || (qc1 == QC_6.getQCValue())
			|| (qc1 == QC_Q.getQCValue()) || (qc2 == QC_A.getQCValue()) || (qc2 == QC_7.getQCValue()))) {
	    return qc1;
	}

	if (qc1 <= qc2) {
	    bestQC = qc1;
	} else {
	    bestQC = qc2;
	}

	return bestQC;
    }

    /**
     * Return the best QC between qc1 and qc2 (that means, the lower value) but not QC_0
     *
     * @param qc1
     * @param qc2
     * @return
     */
    public static QCValues getBestQCExceptQC0(final QCValues qc1, final QCValues qc2) {
	if (qc1 == QC_0) {
	    return qc2;
	}
	if (qc2 == QC_0) {
	    return qc1;
	}
	return getBestQC(qc1, qc2);
    }

    /**
     * Return the worst QC between qc1 and qc2 (that means the greater value)
     *
     * @param qc1
     * @param qc2
     * @return
     */
    public static int getWorstQC(final int qc1, final int qc2) {
	int worstQC;

	if ((qc1 == QC_5.getQCValue()) && ((qc2 == QC_3.getQCValue()) || (qc2 == QC_4.getQCValue()))) {
	    return qc2;
	}

	if ((qc2 == QC_5.getQCValue()) && ((qc1 == QC_3.getQCValue()) || (qc1 == QC_4.getQCValue()))) {
	    return qc1;
	}

	if ((qc1 == QC_Q.getQCValue()) && ((qc2 == QC_0.getQCValue()) || (qc2 == QC_1.getQCValue())
		|| (qc2 == QC_2.getQCValue()) || (qc2 == QC_3.getQCValue()) || (qc2 == QC_4.getQCValue())
		|| (qc2 == QC_5.getQCValue()) || (qc2 == QC_6.getQCValue()))) {
	    return qc1;
	}

	if ((qc2 == QC_Q.getQCValue()) && ((qc1 == QC_0.getQCValue()) || (qc1 == QC_1.getQCValue())
		|| (qc1 == QC_2.getQCValue()) || (qc1 == QC_3.getQCValue()) || (qc1 == QC_4.getQCValue())
		|| (qc1 == QC_5.getQCValue()) || (qc1 == QC_6.getQCValue()))) {
	    return qc2;
	}

	if ((qc1 == QC_A.getQCValue()) && ((qc2 == QC_0.getQCValue()) || (qc2 == QC_1.getQCValue())
		|| (qc2 == QC_2.getQCValue()) || (qc2 == QC_3.getQCValue()) || (qc2 == QC_4.getQCValue())
		|| (qc2 == QC_5.getQCValue()) || (qc2 == QC_6.getQCValue()) || (qc2 == QC_Q.getQCValue()))) {
	    return qc1;
	}

	if ((qc2 == QC_A.getQCValue()) && ((qc1 == QC_0.getQCValue()) || (qc1 == QC_1.getQCValue())
		|| (qc1 == QC_2.getQCValue()) || (qc1 == QC_3.getQCValue()) || (qc1 == QC_4.getQCValue())
		|| (qc1 == QC_5.getQCValue()) || (qc1 == QC_6.getQCValue()) || (qc1 == QC_Q.getQCValue()))) {
	    return qc2;
	}

	if ((qc1 == QC_B.getQCValue()) && ((qc2 == QC_0.getQCValue()) || (qc2 == QC_1.getQCValue())
		|| (qc2 == QC_2.getQCValue()) || (qc2 == QC_3.getQCValue()) || (qc2 == QC_4.getQCValue())
		|| (qc2 == QC_5.getQCValue()) || (qc2 == QC_6.getQCValue()) || (qc2 == QC_Q.getQCValue())
		|| (qc2 == QC_A.getQCValue()) || (qc2 == QC_7.getQCValue()))) {
	    return qc1;
	}

	if ((qc2 == QC_B.getQCValue()) && ((qc1 == QC_0.getQCValue()) || (qc1 == QC_1.getQCValue())
		|| (qc1 == QC_2.getQCValue()) || (qc1 == QC_3.getQCValue()) || (qc1 == QC_4.getQCValue())
		|| (qc1 == QC_5.getQCValue()) || (qc1 == QC_6.getQCValue()) || (qc1 == QC_Q.getQCValue())
		|| (qc2 == QC_A.getQCValue()) || (qc2 == QC_7.getQCValue()))) {
	    return qc2;
	}

	if (qc1 == QC_FILL_VALUE.getQCValue()) {
	    return qc1;
	}

	if (qc2 == QC_FILL_VALUE.getQCValue()) {
	    return qc2;
	}

	if (qc1 >= qc2) {
	    worstQC = qc1;
	} else {
	    worstQC = qc2;
	}

	return worstQC;
    }

    /**
     * Return the worst QC between qc1 and qc2 (/!\ qc1 and qc2 must be NOT NULL)
     *
     * @param qc1
     * @param qc2
     * @return
     */
    public static QCValues getWorstQC(final QCValues qc1, final QCValues qc2) {
	return QCValues.getQCValues(getWorstQC(qc1.getQCValue(), qc2.getQCValue()));
    }

    /**
     * Return the worst QC between qc1 and qc2 (that means the greater value (except 9))
     *
     * @param qc1
     * @param qc2
     * @return
     */
    public static int getWorstQCExcept9(final int qc1, final int qc2) {
	int worstQC;

	if (qc1 == 9) {
	    worstQC = qc2;
	} else if (qc2 == 9) {
	    worstQC = qc1;
	} else {
	    worstQC = getWorstQC(qc1, qc2);
	}
	return worstQC;
    }

    /**
     * Return the worst QC between qc1 and qc2 (/!\ qc1 and qc2 must be NOT NULL) (Except 9)
     *
     * @param qc1
     * @param qc2
     * @return
     */
    public static QCValues getWorstQCExcept9(final QCValues qc1, final QCValues qc2) {
	return QCValues.getQCValues(getWorstQCExcept9(qc1.getQCValue(), qc2.getQCValue()));
    }

    /**
     * Check if qc1 or valueAndQC2 is NULL before computing the Worst QC (Except 9)
     *
     * @param qc1
     * @param valueAndQC2
     * @return
     */
    public static QCValues getWorstQCExcept9(final QCValues qc1, final ValueAndQC valueAndQC2) {
	QCValues worstQC;
	if ((qc1 == null) && (valueAndQC2 == null)) {
	    worstQC = getFillValue();
	} else {
	    if (qc1 == null) {
		worstQC = valueAndQC2.getQc();
	    } else if (valueAndQC2 == null) {
		worstQC = qc1;
	    } else {
		worstQC = getWorstQCExcept9(qc1, valueAndQC2.getQc());
	    }
	}
	return worstQC;
    }

    /**
     * Check if valueAndQC1 or valueAndQC2 is NULL before computing the Worst QC (Except 9)
     *
     * @param valueAndQC1
     * @param valueAndQC2
     * @return
     */
    public static QCValues getWorstQCExcept9(final ValueAndQC valueAndQC1, final ValueAndQC valueAndQC2) {
	QCValues worstQC;
	if ((valueAndQC1 == null) && (valueAndQC2 == null)) {
	    worstQC = getFillValue();
	} else {
	    if (valueAndQC1 != null) {
		worstQC = getWorstQCExcept9(valueAndQC1.getQc(), valueAndQC2);
	    } else {
		worstQC = getWorstQCExcept9(valueAndQC2.getQc(), valueAndQC1);
	    }
	}
	return worstQC;
    }

    /**
     * Get the QCValue as an byte
     *
     * @return
     */
    public char getCharQCValue() {
	Character c = null;
	switch (this) {
	case QC_0:
	    c = '0';
	    break;
	case QC_1:
	    c = '1';
	    break;
	case QC_2:
	    c = '2';
	    break;
	case QC_3:
	    c = '3';
	    break;
	case QC_4:
	    c = '4';
	    break;
	case QC_5:
	    c = '5';
	    break;
	case QC_6:
	    c = '6';
	    break;
	case QC_7:
	    c = '7';
	    break;
	case QC_8:
	    c = '8';
	    break;
	case QC_9:
	    c = '9';
	    break;
	case QC_Q:
	    c = QC_Q_CHAR;
	    break;
	case QC_A:
	    c = QC_A_CHAR;
	    break;
	case QC_B:
	    c = QC_B_CHAR;
	    break;
	case QC_FILL_VALUE:
	    c = QC_FILL_VALUE_CHAR;
	    break;
	default:
	    break;
	}

	return c;
    }

    /**
     * Get the QCValue as an byte
     *
     * @return
     */
    public byte getByteQCValue() {
	Byte b = null;
	switch (this) {
	case QC_0:
	    b = 0;
	    break;
	case QC_1:
	    b = 1;
	    break;
	case QC_2:
	    b = 2;
	    break;
	case QC_3:
	    b = 3;
	    break;
	case QC_4:
	    b = 4;
	    break;
	case QC_5:
	    b = 5;
	    break;
	case QC_6:
	    b = 6;
	    break;
	case QC_7:
	    b = 7;
	    break;
	case QC_8:
	    b = 8;
	    break;
	case QC_9:
	    b = 9;
	    break;
	case QC_Q:
	    b = QC_Q_VALUE;
	    break;
	case QC_A:
	    b = QC_A_VALUE;
	    break;
	case QC_B:
	    b = QC_B_VALUE;
	    break;
	case QC_FILL_VALUE:
	    b = QC_FILL_VALUE_VALUE;
	    break;
	default:
	    break;
	}

	return b;
    }

    /**
     * Get the color to use for this QCValues
     *
     * @return
     */
    public Color getColor() {
	return QCColor.QC_COLOR_MAP.get(getQCValue());
    }

    /**
     * Return the hexa code for color of QC value
     *
     * @return
     */
    public String getHexaColorCode() {
	return "#" + Integer.toHexString(QCColor.QC_COLOR_MAP.get(getQCValue()).getRGB()).substring(2);
    }

    /**
     * Get the color to use for this QCValues
     *
     * @return
     */
    public Color getForegroundColor() {
	return QCColor.QC_FOREGROUND_COLOR_MAP.get(getQCValue());
    }

    /**
     * Get the QCValue as an int
     *
     * @return
     */
    public int getQCValue() {
	Integer i = null;
	switch (this) {
	case QC_0:
	    i = 0;
	    break;
	case QC_1:
	    i = 1;
	    break;
	case QC_2:
	    i = 2;
	    break;
	case QC_3:
	    i = 3;
	    break;
	case QC_4:
	    i = 4;
	    break;
	case QC_5:
	    i = 5;
	    break;
	case QC_6:
	    i = 6;
	    break;
	case QC_7:
	    i = 7;
	    break;
	case QC_8:
	    i = 8;
	    break;
	case QC_9:
	    i = 9;
	    break;
	case QC_Q:
	    i = QC_Q_VALUE;
	    break;
	case QC_A:
	    i = QC_A_VALUE;
	    break;
	case QC_B:
	    i = QC_B_VALUE;
	    break;
	case QC_FILL_VALUE:
	    i = QC_FILL_VALUE_VALUE;
	    break;
	default:
	    break;
	}

	return i;
    }

    /**
     * Get the QCValue as an string
     *
     * @return
     */
    public String getStringQCValue() {
	String s = null;
	switch (this) {
	case QC_0:
	    s = "0";
	    break;
	case QC_1:
	    s = "1";
	    break;
	case QC_2:
	    s = "2";
	    break;
	case QC_3:
	    s = "3";
	    break;
	case QC_4:
	    s = "4";
	    break;
	case QC_5:
	    s = "5";
	    break;
	case QC_6:
	    s = "6";
	    break;
	case QC_7:
	    s = "7";
	    break;
	case QC_8:
	    s = "8";
	    break;
	case QC_9:
	    s = "9";
	    break;
	case QC_Q:
	    s = QC_Q_STRING;
	    break;
	case QC_A:
	    s = QC_A_STRING;
	    break;
	case QC_B:
	    s = QC_B_STRING;
	    break;
	case QC_FILL_VALUE:
	    s = QC_FILL_VALUE_STRING;
	    break;
	default:
	    break;
	}

	return s;
    }

    /**
     * get the ENUM value from an string
     *
     * @param stringQC
     * @return
     */
    public static QCValues getQCValuesFromString(final String stringQC) {
	QCValues qc = null;
	switch (stringQC) {
	case "0":
	    qc = QC_0;
	    break;
	case "1":
	    qc = QC_1;
	    break;
	case "2":
	    qc = QC_2;
	    break;
	case "3":
	    qc = QC_3;
	    break;
	case "4":
	    qc = QC_4;
	    break;
	case "5":
	    qc = QC_5;
	    break;
	case "6":
	    qc = QC_6;
	    break;
	case "7":
	    qc = QC_7;
	    break;
	case "8":
	    qc = QC_8;
	    break;
	case "9":
	    qc = QC_9;
	    break;
	case QC_Q_STRING:
	    qc = QC_Q;
	    break;
	case QC_A_STRING:
	    qc = QC_A;
	    break;
	case QC_B_STRING:
	    qc = QC_B;
	    break;
	case QC_FILL_VALUE_STRING:
	    qc = QC_FILL_VALUE;
	    break;
	default:
	    break;
	}

	return qc;
    }

}
