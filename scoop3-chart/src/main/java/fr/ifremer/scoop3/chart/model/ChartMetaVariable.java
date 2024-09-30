package fr.ifremer.scoop3.chart.model;

import java.util.ArrayList;
import java.util.List;

public class ChartMetaVariable implements ChartDatasetVariable {

	public String label = null;
	public List<String> valuesByStation = null;

	public ChartMetaVariable(String label) {
		this.label = label;
		valuesByStation = new ArrayList<String>();
	}

	public void addValues(Object values) {

		if (values instanceof float[]) {
			valuesByStation.add(String.valueOf(((float[]) values)[0]));
		} else if (values instanceof int[]) {
			valuesByStation.add(String.valueOf(((int[]) values)[0]));
		} else if (values instanceof double[]) {
			valuesByStation.add(String.valueOf(((double[]) values)[0]));
		} else {
			valuesByStation.add(new String((char[]) values));
		}
	}

	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * Unload data to save memory
	 */
	public void prepareForDispose() {
		valuesByStation.clear();
		valuesByStation = null;
	}
}
