package fr.ifremer.scoop3.events;


public class GuiEventDisplayDialog extends GuiEvent {

    private final String dialogMessage;
    private final String dialogTitle;
    private final boolean displayJDialog;
    private final long timeOfEvent = System.currentTimeMillis();

    /**
     * Constructor to call the dispose of the Dialog
     */
    public GuiEventDisplayDialog() {
	this(false, null, null);
    }

    /**
     * Constructor to call the display of the Dialog
     */
    public GuiEventDisplayDialog(final String dialogTitle, final String dialogMessage) {
	this(true, dialogTitle, dialogMessage);
    }

    /**
     * Default constructor
     * 
     * @param displayDialog
     * @param dialogTitle
     * @param dialogMessage
     */
    private GuiEventDisplayDialog(final boolean displayDialog, final String dialogTitle, final String dialogMessage) {
	super(GuiEventEnum.DISPLAY_DIALOG);
	this.displayJDialog = displayDialog;
	this.dialogTitle = dialogTitle;
	this.dialogMessage = dialogMessage;
    }

    /**
     * @return the displayJDialog
     */
    public Boolean displayDialog() {
	return displayJDialog;
    }

    /**
     * @return the dialogMessage
     */
    public String getDialogMessage() {
	return dialogMessage;
    }

    /**
     * @return the dialogTitle
     */
    public String getDialogTitle() {
	return dialogTitle;
    }

    /**
     * @return the timeOfEvent
     */
    public long getTimeOfEvent() {
	return timeOfEvent;
    }
}
