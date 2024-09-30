package fr.ifremer.scoop3.core.report.validation.backup;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ifremer.scoop3.core.report.validation.model.StepItem;
import fr.ifremer.scoop3.core.report.validation.model.StepItem.STEP_TYPE;
import fr.ifremer.scoop3.core.report.validation.model.messages.ComputedParameterMessageItem;

public class Backup implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5613716078499265205L;

    private List<ComputedParameterMessageItem> computedParameters;
    private String originalFilepath;
    public Map<STEP_TYPE, StepItem> steps;

    /**
     * Identifiant unique du dataset utile - A sa mise en cache local
     */
    private String uri;

    /**
     * Constructor. Initialize attributes and create the first {@link StepItem}.
     *
     * @param path
     */
    public Backup(final String path) {
	this.originalFilepath = path;
	this.steps = new HashMap<>();
	this.uri = path;
    }

    public String getURI() {
	return uri;
    }

    public void setURI(final String datasetURI) {
	uri = datasetURI;

    }

    public List<ComputedParameterMessageItem> getComputedParameters() {
	return computedParameters;
    }

    public void setComputedParameters(final List<ComputedParameterMessageItem> computedParameters) {
	this.computedParameters = computedParameters;
    }

    public String getOriginalFilepath() {
	return originalFilepath;
    }

    public void setOriginalFilepath(final String originalFilepath) {
	this.originalFilepath = originalFilepath;
    }

    public Map<STEP_TYPE, StepItem> getSteps() {
	return steps;
    }

    public void setSteps(final Map<STEP_TYPE, StepItem> steps) {
	this.steps = steps;
    }

}
