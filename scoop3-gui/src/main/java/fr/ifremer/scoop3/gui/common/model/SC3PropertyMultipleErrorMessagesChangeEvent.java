package fr.ifremer.scoop3.gui.common.model;

import java.util.List;

import fr.ifremer.scoop3.core.report.validation.model.messages.CAErrorMessageItem;
import fr.ifremer.scoop3.model.QCValues;

public class SC3PropertyMultipleErrorMessagesChangeEvent extends SC3PropertyChangeEvent {

    private static final long serialVersionUID = 2837709245506470428L;

    private final String commentToSet;
    private final List<? extends CAErrorMessageItem> errorMessagesToUpdate;
    private final Boolean isChecked;
    private final QCValues qcToSet;

    public SC3PropertyMultipleErrorMessagesChangeEvent(final Object source,
	    final List<? extends CAErrorMessageItem> errorMessagesToUpdate, final QCValues qcToSet,
	    final String commentToSet, final Boolean isChecked) {
	super(source, EVENT_ENUM.CHANGE_MULTIPLE);

	this.errorMessagesToUpdate = errorMessagesToUpdate;
	this.qcToSet = qcToSet;
	this.commentToSet = commentToSet;
	this.isChecked = isChecked;
    }

    /**
     * @return the commentToSet
     */
    public String getCommentToSet() {
	return commentToSet;
    }

    /**
     * @return the errorMessagesToUpdate
     */
    public List<? extends CAErrorMessageItem> getErrorMessagesToUpdate() {
	return errorMessagesToUpdate;
    }

    /**
     * @return the qcToSet
     */
    public QCValues getQcToSet() {
	return qcToSet;
    }

    /**
     * @return the isChecked
     */
    public Boolean isChecked() {
	return isChecked;
    }

}
