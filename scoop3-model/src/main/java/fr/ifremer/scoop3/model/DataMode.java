package fr.ifremer.scoop3.model;

/**
 * Source : http://www.oceansites.org/docs/oceansites_user_manual_version1.2.pdf
 *
 * § 4.5 Reference table 5: data mode
 *
 * The values for the variables PARAM_DM, the global attribute data_mode, and variable attributes PARAM:DM_indicator are
 * defined as follows:
 */
public enum DataMode {
    /**
     * Real-time data. Data coming from the (typically remote) platform through a communication channel without physical
     * access to the instruments, disassembly or recovery of the platform. Example: for a mooring with a radio
     * communication, this would be data obtained through the radio.
     */
    R,
    /**
     * Provisional data. Data obtained after the instruments or the platform have been recovered or serviced. Example:
     * for instruments on a mooring, this would be data downloaded directly from the instruments after the mooring has
     * been recovered on a ship
     */
    P,
    /**
     * Delayed-mode data. Data published after all calibrations and quality control procedures have been applied on the
     * internally recorded or best available original data. This is the best possible version of processed data.
     */
    D,
    /**
     * Mixed. This value is only allowed in the global attribute data_mode or in attributes to variables in the form
     * PARAM:DM_indicator. It indicates that the file contains data in more than one of the above states. In this case,
     * the variable(s) PARAM_DM specify which data is in which data mode.
     */
    M,
    /**
     * The fill value of the DataMode
     */
    FILL_VALUE;

    @Override
    public String toString() {
	switch (this) {
	case R:
	    return "R";
	case P:
	    return "P";
	case D:
	    return "D";
	case M:
	    return "M";
	case FILL_VALUE:
	    return " ";
	default:
	    throw new IllegalArgumentException();
	}
    }

    /**
     * @return the FillValue
     */
    public static DataMode getFillValue() {
	return FILL_VALUE;
    }

    public static DataMode getDataMode(final char charQc) {
	final DataMode dm;
	switch (charQc) {
	case 'R':
	    dm = R;
	    break;
	case 'P':
	    dm = P;
	    break;
	case 'D':
	    dm = D;
	    break;
	case 'M':
	    dm = M;
	    break;
	default:
	    dm = getFillValue();
	    break;
	}
	return dm;
    }

}
