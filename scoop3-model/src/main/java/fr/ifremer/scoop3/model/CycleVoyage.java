package fr.ifremer.scoop3.model;

import java.io.Serializable;

/**
 * Class defining a cycle voyage
 *
 * @author criou
 *
 */
public class CycleVoyage implements Serializable {

    private static final long serialVersionUID = -2559526083006245014L;
    /**
     *
     */
    
    private String platformCode;
    private Integer cvNumber;
    private Long startDate;
    private Long endDate;
    private String dataState;
    private Integer contextId;

    /**
     * Default constructor
     */
    public CycleVoyage() {
	platformCode = null;
	cvNumber = null;
	startDate = null;
	endDate = null;
	dataState = null;
	contextId = null;
    }

    public CycleVoyage(String platformCode, Integer cvNumber, Long startDate, Long endDate, String dataState,
	    Integer contextId) {
	super();
	this.platformCode = platformCode;
	this.cvNumber = cvNumber;
	this.startDate = startDate;
	this.endDate = endDate;
	this.dataState = dataState;
	this.contextId = contextId;
    }

    public String getPlatformCode() {
        return platformCode;
    }

    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode;
    }

    public Integer getCvNumber() {
        return cvNumber;
    }

    public void setCvNumber(Integer cvNumber) {
        this.cvNumber = cvNumber;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public String getDataState() {
        return dataState;
    }

    public void setDataState(String dataState) {
        this.dataState = dataState;
    }

    public Integer getContextId() {
        return contextId;
    }

    public void setContextId(Integer contextId) {
        this.contextId = contextId;
    }
}
