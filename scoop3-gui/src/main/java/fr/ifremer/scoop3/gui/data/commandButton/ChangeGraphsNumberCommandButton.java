package fr.ifremer.scoop3.gui.data.commandButton;

import org.pushingpixels.flamingo.api.common.JCommandButton;

public class ChangeGraphsNumberCommandButton {

    private final JCommandButton changeGraphsNumber;
    private final int columnNb;
    private final int rowNb;

    public ChangeGraphsNumberCommandButton(final JCommandButton changeGraphsNumber, final int rowNb, final int columnNb) {
	this.changeGraphsNumber = changeGraphsNumber;
	this.columnNb = columnNb;
	this.rowNb = rowNb;
    }

    /**
     * @return the changeGraphsNumber
     */
    public JCommandButton getChangeGraphsNumber() {
	return changeGraphsNumber;
    }

    /**
     * @return the columnNb
     */
    public int getColumnNb() {
	return columnNb;
    }

    /**
     * @return the rowNb
     */
    public int getRowNb() {
	return rowNb;
    }

}
