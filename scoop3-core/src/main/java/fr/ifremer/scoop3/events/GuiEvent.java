package fr.ifremer.scoop3.events;

import org.bushe.swing.event.EventBus;

public abstract class GuiEvent extends EventBus {

    private final GuiEventEnum guiEventEnum;

    protected GuiEvent(final GuiEventEnum guiEventEnum) {
	this.guiEventEnum = guiEventEnum;
    }

    /**
     * @return the guiEventEnum
     */
    public GuiEventEnum getGuiEventEnum() {
	return guiEventEnum;
    }

    /**
     *
     */
    public enum GuiEventEnum {
	BACKUP_FILE_AND_REPORT, //
	BACKUP_IS_COMPLETE, //
	CHANGE_MAIN_PANEL_TO_STEP, //
	CONFIG_MANAGER, //
	DISPLAY_DIALOG, //
	MISC_EVENT, // Specific events
	REPLACE_CURRENT_DATASET, //
	RESET_DRIVERS, //
	RESTORE_BACKUP_FILE_AND_GO_HOME, //
	START_STEP, //
	STEP_COMPLETED, //
	TRANSCODE_PARAMETERS, //
	UPDATE_BUTTONS_ENABLED, //
	UPDATE_CONFIGURATION_COMBO, //
	UPDATE_FRAME_TITLE, //
	UPDATE_FILE_CHOOSE_NAME, //
	CREATE_HTML_REPORT, //
	CREATE_FINAL_PREVIEW
    }

}
