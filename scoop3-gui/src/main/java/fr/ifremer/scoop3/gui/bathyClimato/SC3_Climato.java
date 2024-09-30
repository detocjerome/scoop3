package fr.ifremer.scoop3.gui.bathyClimato;

public abstract class SC3_Climato {

    public enum CLIMATO_ENUM {
	/** ARIVO (0.5X0.5) */
	ARIVO_05,
	/** BOBYCLIM */
	BOBY,
	/** ISAS 2013 (0.5X0.5) */
	ISAS13_05,
	/** LEVITUS 2001 (1X1) */
	L01,
	/** LEVITUS 2005 (1X1) */
	L05,
	/** Levitus 83 */
	L83,
	/** Levitus 94 */
	L94,
	/** Levitus 98 (ET 1X1) */
	L98_1,
	/** Levitus 98 (ET 5X5) */
	L98_5,
	/** MEDATLAS 2002 Black */
	M02B,
	/** MEDATLAS 2002 Medit. */
	M02M,
	/** MEDATLAS 97 */
	M97,
	/** MEDATLAS 97 (1X1) */
	M97_1,
	/** REYNAUD 1997 */
	REYN,
	/** no climatology */
	SANS,
	/** WOA 2009 (1X1) */
	WOA09_1,
	/** WOA 2009 (5X5) */
	WOA09_5,
	/** WOA 2013 (1X1) */
	WOA13_1,
	/** WOA 2013 (5X5) */
	WOA13_5,
	/** WOA 2018 (1X1) */
	WOA18_1,
	/** WOA 2018 (5X5) */
	WOA18_5,
	/** MIN_MAX */
	MIN_MAX;

	/**
	 * @return the climatology code
	 */
	public String getClimatologyCode() {
	    return String.valueOf(this);
	}

	/**
	 * @return the label for a given Climato code
	 */
	public String getLabel() {
	    String label = null;
	    switch (this) {
	    case ARIVO_05:
		label = "ARIVO (0.5X0.5)";
		break;
	    case BOBY:
		label = "BOBYCLIM";
		break;
	    case ISAS13_05:
		label = "ISAS 2013 (0.5X0.5)";
		break;
	    case L01:
		label = "LEVITUS 2001 (1X1)";
		break;
	    case L05:
		label = "LEVITUS 2005 (1X1)";
		break;
	    case L83:
		label = "Levitus 83";
		break;
	    case L94:
		label = "Levitus 94";
		break;
	    case L98_1:
		label = "Levitus 98 (ET 1X1)";
		break;
	    case L98_5:
		label = "Levitus 98 (ET 5X5)";
		break;
	    case M02B:
		label = "MEDATLAS 2002 Black";
		break;
	    case M02M:
		label = "MEDATLAS 2002 Medit.";
		break;
	    case M97:
		label = "MEDATLAS 97";
		break;
	    case M97_1:
		label = "MEDATLAS 97 (1X1)";
		break;
	    case REYN:
		label = "REYNAUD 1997";
		break;
	    case SANS:
		label = "no climatology";
		break;
	    case WOA09_1:
		label = "WOA 2009 (1X1)";
		break;
	    case WOA09_5:
		label = "WOA 2009 (5X5)";
		break;
	    case WOA13_1:
		label = "WOA 2013 (1X1)";
		break;
	    case WOA13_5:
		label = "WOA 2013 (5X5)";
		break;
	    case WOA18_1:
		label = "WOA 2018 (1X1)";
		break;
	    case WOA18_5:
		label = "WOA 2018 (5X5)";
		break;
	    case MIN_MAX:
		label = "MIN_MAX";
		break;
	    }
	    return label;
	}
    }
}
