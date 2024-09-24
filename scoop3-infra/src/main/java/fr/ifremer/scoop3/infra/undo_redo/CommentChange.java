package fr.ifremer.scoop3.infra.undo_redo;

public class CommentChange extends UndoRedoAction {

    private final String comment;
    private final String commentOldValue;
    private final String details;
    private final int observationIndex;
    private final String obsId;

    public CommentChange(final int observationIndex, final String obsId, final String details, final String comment,
	    final String commentOldValue) {
	super();
	this.observationIndex = observationIndex;
	this.obsId = obsId;
	this.details = details;
	this.comment = comment;
	this.commentOldValue = commentOldValue;
    }

    /**
     * @return the comment
     */
    public String getComment() {
	return comment;
    }

    /**
     * @return the commentOldValue
     */
    public String getCommentOldValue() {
	return commentOldValue;
    }

    /**
     * @return the details
     */
    public String getDetails() {
	return details;
    }

    /**
     * @return the observationIndex
     */
    public int getObservationIndex() {
	return observationIndex;
    }

    /**
     * @return the obsId
     */
    public String getObsId() {
	return obsId;
    }
}
