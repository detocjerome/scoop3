package fr.ifremer.scoop3.events;

public class GuiEventUpdateFrameTitle extends GuiEvent {

    private final String newTitle;

    public GuiEventUpdateFrameTitle(final String newTitle) {
	super(GuiEventEnum.UPDATE_FRAME_TITLE);
	this.newTitle = newTitle;
    }

    /**
     * @return the newTitle
     */
    public String getNewTitle() {
	return newTitle;
    }

}
