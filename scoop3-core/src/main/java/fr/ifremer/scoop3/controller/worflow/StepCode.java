package fr.ifremer.scoop3.controller.worflow;

public enum StepCode {
    START, QC0, QC11, QC12, QC21, QC22, QC31;

    public static String toString(final StepCode stepCode) {
	return String.valueOf(stepCode);
    }

    /**
     * if stepCode != START, return the String without 'QC'. I.e. : 11 for QC11
     *
     * @param stepCode
     * @return
     */
    public static String toStringWithoutQ(final StepCode stepCode) {
	String toReturn;
	if (stepCode.equals(StepCode.START)) {
	    toReturn = toString(stepCode);
	} else {
	    toReturn = toString(stepCode).replace("QC", "");
	}
	return toReturn;
    }

    /**
     * Get a StepCode from its String
     *
     * @param codeStr
     * @return
     */
    public static StepCode getStepCode(String codeStr) {
	StepCode toReturn;

	// Force upper case
	codeStr = codeStr.toUpperCase();

	// Check if it is START Step
	if (codeStr.equalsIgnoreCase(StepCode.toString(START))) {
	    toReturn = START;
	} else {
	    // Check if the codeStr is only the number of the Step
	    if (!codeStr.startsWith("QC")) {
		codeStr = "QC" + codeStr;
	    }

	    // By default, return Q0
	    toReturn = QC0;
	    try {
		toReturn = StepCode.valueOf(codeStr);
		// code exists
	    } catch (final IllegalArgumentException ex) {
		// code does NOT exist
	    }
	}
	return toReturn;
    }
}
