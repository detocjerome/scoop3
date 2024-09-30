package fr.ifremer.scoop3.gui.common.model;

import fr.ifremer.scoop3.infra.i18n.Messages;

public enum DisplayedQCEnum {
    DATE_QC, //
    POSITION_QC, //
    VALUES, //
    ;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
	return getLabel();
    }

    public String getLabel() {
	return Messages.getMessage("coriolis-gui.DisplayedQCEnum.label." + this.name());
    }

    public String getTooltip() {
	return Messages.getMessage("coriolis-gui.DisplayedQCEnum.tooltip." + this.name());
    }
}
