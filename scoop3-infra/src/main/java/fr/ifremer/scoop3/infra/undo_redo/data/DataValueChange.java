package fr.ifremer.scoop3.infra.undo_redo.data;

public class DataValueChange extends QCValueChange {

    private String comment = null;
    private String commentOldValue = null;
    private String errorMessage = null;
    private Boolean isErrorChecked = null;
    private Boolean isErrorCheckedOldValue = null;

    /**
     * @param observationIndex
     * @param observationLevel
     * @param oldQC
     * @param newQC
     * @param obsId
     * @param parameterName
     * @param refValueStr
     */
    public DataValueChange(final int observationIndex, final int observationLevel, final int oldQC, final int newQC,
	    final String obsId, final String parameterName, final Number parameterValue, final String parameterValueStr,
	    final String refValueStr) {
	super(observationIndex, observationLevel, oldQC, newQC, obsId, parameterName, parameterValue, parameterValueStr,
		refValueStr, "");
    }

    /**
     *
     * @param observationIndex
     * @param observationLevel
     * @param obsId
     * @param parameterName
     * @param refValueStr
     * @param errorMessage
     */
    public DataValueChange(final int observationIndex, final int observationLevel, final String obsId,
	    final String parameterName, final Number parameterValue, final String parameterValueStr,
	    final String refValueStr, final String errorMessage) {
	this(observationIndex, observationLevel, QCValueChange.NO_NEW_QC, QCValueChange.NO_NEW_QC, obsId, parameterName,
		parameterValue, parameterValueStr, refValueStr);
	this.errorMessage = errorMessage;
    }

    /**
     * @param observationIndex
     * @param observationLevel
     * @param oldQC
     * @param newQC
     * @param obsId
     */
    protected DataValueChange(final int observationIndex, final int observationLevel, final int oldQC, final int newQC,
	    final String obsId) {
	this(observationIndex, observationLevel, oldQC, newQC, obsId, null, null, null, null);
    }

    /**
     * @param observationIndex
     * @param observationLevel
     * @param oldQC
     * @param newQC
     * @param obsId
     */
    protected DataValueChange(final int observationIndex, final int observationLevel, final int oldQC, final int newQC,
	    final String obsId, final String refValueStr) {
	this(observationIndex, observationLevel, oldQC, newQC, obsId, null, null, null, refValueStr);
    }

    /**
     *
     * @param observationIndex
     * @param observationLevel
     * @param obsId
     * @param errorMessage
     */
    protected DataValueChange(final int observationIndex, final int observationLevel, final String obsId,
	    final String errorMessage) {
	this(observationIndex, observationLevel, QCValueChange.NO_NEW_QC, QCValueChange.NO_NEW_QC, obsId, null, null,
		null, null);
	this.errorMessage = errorMessage;
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
     * @return the errorMessage
     */
    public String getErrorMessage() {
	return errorMessage;
    }

    /**
     * @return the isErrorChecked
     */
    public Boolean isErrorChecked() {
	return isErrorChecked;
    }

    /**
     * @return the isErrorCheckedOldValue
     */
    public Boolean isErrorCheckedOldValue() {
	return isErrorCheckedOldValue;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(final String comment) {
	this.comment = comment;
    }

    /**
     * @param commentOldValue
     *            the commentOldValue to set
     */
    public void setCommentOldValue(final String commentOldValue) {
	this.commentOldValue = commentOldValue;
	if (this.commentOldValue == null) {
	    this.commentOldValue = "";
	}
    }

    /**
     * @param errorMessage
     *            the errorMessage to set
     */
    public void setErrorMessage(final String errorMessage) {
	this.errorMessage = errorMessage;
    }

    /**
     * @param isErrorChecked
     *            the isErrorChecked to set
     */
    public void setIsErrorChecked(final Boolean isErrorChecked) {
	this.isErrorChecked = isErrorChecked;
    }

    /**
     * @param isErrorCheckedOldValue
     *            the isErrorCheckedOldValue to set
     */
    public void setIsErrorCheckedOldValue(final Boolean isErrorCheckedOldValue) {
	this.isErrorCheckedOldValue = isErrorCheckedOldValue;
    }

}
