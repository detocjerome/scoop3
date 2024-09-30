package fr.ifremer.scoop3.model.valueAndQc;

// package fr.ifremer.scoop3.model;
//
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import fr.ifremer.scoop3.model.QCValues;

public class QCColor {

    public static final Map<Integer, Color> QC_COLOR_MAP = new HashMap<Integer, Color>();
    public static final HashMap<Integer, Color> QC_COLOR_OLD_MAP = new HashMap<Integer, Color>();
    public static final HashMap<Integer, Color> QC_FOREGROUND_COLOR_MAP = new HashMap<Integer, Color>();

    public static void populateQcColorMap(final boolean isBPC) {
	/**
	 * Couleurs a utiliser en fonction d'une valeur entiere (QC)
	 */
	if (QC_COLOR_MAP.isEmpty()) {
	    if (isBPC) {
		QC_COLOR_MAP.put(QCValues.QC_0.getQCValue(), Color.WHITE);
		QC_COLOR_MAP.put(QCValues.QC_1.getQCValue(), new Color(0, 200, 0));
		QC_COLOR_MAP.put(QCValues.QC_2.getQCValue(), new Color(230, 230, 0));
		QC_COLOR_MAP.put(QCValues.QC_3.getQCValue(), new Color(255, 128, 0));
		QC_COLOR_MAP.put(QCValues.QC_4.getQCValue(), Color.RED);
		QC_COLOR_MAP.put(QCValues.QC_5.getQCValue(), new Color(200, 0, 200));
		QC_COLOR_MAP.put(QCValues.QC_6.getQCValue(), new Color(170, 170, 170));
		QC_COLOR_MAP.put(QCValues.QC_7.getQCValue(), new Color(128, 0, 128));
		QC_COLOR_MAP.put(QCValues.QC_8.getQCValue(), new Color(0, 128, 255));
		QC_COLOR_MAP.put(QCValues.QC_9.getQCValue(), Color.BLACK);
		QC_COLOR_MAP.put(QCValues.QC_Q.getQCValue(), Color.GRAY);
		QC_COLOR_MAP.put(QCValues.QC_A.getQCValue(), new Color(92, 54, 36)); // dark brown
		QC_COLOR_MAP.put(QCValues.QC_B.getQCValue(), new Color(0, 255, 255));
		QC_COLOR_MAP.put(QCValues.QC_FILL_VALUE.getQCValue(), new Color(160, 82, 45)); // light brown
	    } else {
		QC_COLOR_MAP.put(QCValues.QC_0.getQCValue(), Color.WHITE);
		QC_COLOR_MAP.put(QCValues.QC_1.getQCValue(), new Color(0, 200, 0));
		QC_COLOR_MAP.put(QCValues.QC_2.getQCValue(), new Color(230, 230, 0));
		QC_COLOR_MAP.put(QCValues.QC_3.getQCValue(), new Color(255, 128, 0));
		QC_COLOR_MAP.put(QCValues.QC_4.getQCValue(), Color.RED);
		QC_COLOR_MAP.put(QCValues.QC_5.getQCValue(), new Color(200, 0, 200));
		QC_COLOR_MAP.put(QCValues.QC_6.getQCValue(), new Color(170, 170, 170));
		QC_COLOR_MAP.put(QCValues.QC_7.getQCValue(), Color.GRAY);
		QC_COLOR_MAP.put(QCValues.QC_8.getQCValue(), new Color(0, 128, 255));
		QC_COLOR_MAP.put(QCValues.QC_9.getQCValue(), Color.BLACK);
		QC_COLOR_MAP.put(QCValues.QC_Q.getQCValue(), Color.GRAY);
		QC_COLOR_MAP.put(QCValues.QC_A.getQCValue(), new Color(92, 54, 36)); // dark brown
		QC_COLOR_MAP.put(QCValues.QC_B.getQCValue(), new Color(0, 255, 255));
		QC_COLOR_MAP.put(QCValues.QC_FILL_VALUE.getQCValue(), new Color(160, 82, 45)); // light brown
	    }
	}
	/**
	 * Couleurs a utiliser pour l'�criture en fonction d'une valeur entiere (QC)
	 */
	if (QC_FOREGROUND_COLOR_MAP.isEmpty()) {
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_0.getQCValue(), Color.BLACK);
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_1.getQCValue(), Color.BLACK);
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_2.getQCValue(), Color.BLACK);
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_3.getQCValue(), Color.BLACK);
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_4.getQCValue(), Color.BLACK);
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_5.getQCValue(), Color.BLACK);
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_6.getQCValue(), Color.BLACK);
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_7.getQCValue(), Color.WHITE);
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_8.getQCValue(), Color.WHITE);
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_9.getQCValue(), Color.WHITE);
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_Q.getQCValue(), Color.WHITE);
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_A.getQCValue(), Color.WHITE);
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_B.getQCValue(), Color.BLACK);
	    QC_FOREGROUND_COLOR_MAP.put(QCValues.QC_FILL_VALUE.getQCValue(), Color.WHITE);
	}
    }

    static {
	QC_COLOR_OLD_MAP.put(QCValues.QC_0.getQCValue(), Color.WHITE);
	QC_COLOR_OLD_MAP.put(QCValues.QC_1.getQCValue(), Color.GREEN);
	QC_COLOR_OLD_MAP.put(QCValues.QC_2.getQCValue(), Color.YELLOW);
	QC_COLOR_OLD_MAP.put(QCValues.QC_3.getQCValue(), new Color(237, 127, 16));
	QC_COLOR_OLD_MAP.put(QCValues.QC_4.getQCValue(), Color.RED);
	QC_COLOR_OLD_MAP.put(QCValues.QC_5.getQCValue(), Color.MAGENTA);
	QC_COLOR_OLD_MAP.put(QCValues.QC_6.getQCValue(), Color.LIGHT_GRAY);
	QC_COLOR_OLD_MAP.put(QCValues.QC_7.getQCValue(), new Color(0, 255, 255));
	QC_COLOR_OLD_MAP.put(QCValues.QC_8.getQCValue(), Color.BLUE);
	QC_COLOR_OLD_MAP.put(QCValues.QC_9.getQCValue(), Color.BLACK);
	QC_COLOR_OLD_MAP.put(QCValues.QC_Q.getQCValue(), Color.GRAY);
	QC_COLOR_OLD_MAP.put(QCValues.QC_A.getQCValue(), new Color(92, 54, 36)); // dark brown
	QC_COLOR_OLD_MAP.put(QCValues.QC_B.getQCValue(), new Color(0, 255, 255));
	QC_COLOR_OLD_MAP.put(QCValues.QC_FILL_VALUE.getQCValue(), new Color(160, 82, 45)); // light brown
    }

    // # La couleur des codes qualit�s en RGB.
    // # Les valeurs doivent �tre comprisent entre [0, 255].
    // no=255;255;255
    // qc0=255;255;255
    // qc1=0;200;0
    // qc2=230;230;0
    // qc3=255;128;0
    // qc4=255;0;0
    // qc5=200;0;200
    // qc8=0;128;255
    // qc9=0;0;0
}
