package fr.ifremer.scoop3.chart.model;

import java.util.ArrayList;
import java.util.List;

public class ChartDataset {

    private List<ChartMetaVariable> metaVariables;
    private List<ChartPhysicalVariable> physicalVariables;
    /** pressure or depth or time */
    private String referenceLabel;

    /**
     * Constructor
     */
    public ChartDataset() {
	metaVariables = new ArrayList<ChartMetaVariable>();
	physicalVariables = new ArrayList<ChartPhysicalVariable>();
    }

    public void addMetaVariable(final ChartMetaVariable var) {
	metaVariables.add(var);
    }

    public void addPhysicalVariable(final ChartPhysicalVariable var) {
	if (var.isReferenceParameter()) {
	    referenceLabel = var.getLabel();
	}
	physicalVariables.add(var);
    }

    public boolean containsPhysicalVariable(final String code) {
	boolean contains = false;
	if (indexOfPhysicalVariable(code) != -1) {
	    contains = true;
	} else {
	    contains = false;
	}
	return contains;
    }

    public ChartPhysicalVariable getPhysicalVariable(final int index) {
	return physicalVariables.get(index);
    }

    public ChartPhysicalVariable getPhysicalVariable(final String code) {
	final int index = indexOfPhysicalVariable(code);
	return (index == -1) ? null : physicalVariables.get(index);
    }

    /**
     * @return the physicalVariables
     */
    public List<ChartPhysicalVariable> getPhysicalVariables() {
	return physicalVariables;
    }

    /**
     * @return the metaVariables
     */
    public List<ChartMetaVariable> getMetaVariables() {
	return metaVariables;
    }

    /**
     * @param referenceLabel
     */
    public void setReferenceLabel(final String referenceLabel) {
	this.referenceLabel = referenceLabel;
    }

    public String getReferenceLabel() {
	return referenceLabel;
    }

    public int indexOfPhysicalVariable(final String code) {
	int index = -1;
	int counter = -1;
	final List<ChartPhysicalVariable> tempPhysicalVariables = new ArrayList<ChartPhysicalVariable>(
		physicalVariables);
	for (final ChartPhysicalVariable physicalVariable : tempPhysicalVariables) {
	    counter++;
	    if (physicalVariable.getLabel().equalsIgnoreCase(code)) {
		index = counter;
	    }
	}
	return index;
    }

    /**
     * Unload data to save memory
     */
    public void prepareForDispose() {
	for (final ChartMetaVariable chartMetaVariable : metaVariables) {
	    chartMetaVariable.prepareForDispose();
	}
	metaVariables.clear();
	metaVariables = null;
	for (final ChartPhysicalVariable chartPhysicalVariable : physicalVariables) {
	    chartPhysicalVariable.prepareForDispose();
	}
	physicalVariables.clear();
	physicalVariables = null;
    }

    public void setPhysicalVariable(final int index, final ChartPhysicalVariable var) {
	physicalVariables.set(index, var);
    }
}
