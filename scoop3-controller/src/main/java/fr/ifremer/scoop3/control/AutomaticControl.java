package fr.ifremer.scoop3.control;

import java.util.ArrayList;

import fr.ifremer.scoop3.core.report.validation.model.messages.AutomaticControlStatusMessageItem;
import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;

public abstract class AutomaticControl {

    private final ArrayList<CAErrorMessageItem> errorMessageItems = new ArrayList<>();

    protected void addErrorMessageItem(final CAErrorMessageItem messageItem) {
	errorMessageItems.add(messageItem);
    }

    /**
     * @return the errorMessageItems
     */
    public ArrayList<CAErrorMessageItem> getErrorMessageItemsAndClear() {
	@SuppressWarnings("unchecked")
	final ArrayList<CAErrorMessageItem> errorMessageItem = (ArrayList<CAErrorMessageItem>) this.errorMessageItems
		.clone();
	this.errorMessageItems.clear();
	return errorMessageItem;
    }

    /**
     * @return the Status of the Automatic Control
     */
    public abstract AutomaticControlStatusMessageItem getAutomaticControlStatus();

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return getClass().getSimpleName();
    }
}
