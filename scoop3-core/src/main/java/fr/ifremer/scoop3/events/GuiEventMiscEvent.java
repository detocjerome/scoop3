package fr.ifremer.scoop3.events;

public class GuiEventMiscEvent extends GuiEvent {

    private final Object miscObject;

    public GuiEventMiscEvent(final Object miscObject) {
	super(GuiEventEnum.MISC_EVENT);
	this.miscObject = miscObject;
    }

    /**
     * @return the miscObject
     */
    public Object getMiscObject() {
	return miscObject;
    }

}
