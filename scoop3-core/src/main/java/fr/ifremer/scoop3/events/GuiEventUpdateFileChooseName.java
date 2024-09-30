package fr.ifremer.scoop3.events;

public class GuiEventUpdateFileChooseName extends GuiEvent {

    private final String newFileName;

    public GuiEventUpdateFileChooseName(final String newFileName) {
	super(GuiEventEnum.UPDATE_FILE_CHOOSE_NAME);
	this.newFileName = newFileName;
    }

    /**
     * @return the newFileName
     */
    public String getNewFileName() {
	return newFileName;
    }

}
