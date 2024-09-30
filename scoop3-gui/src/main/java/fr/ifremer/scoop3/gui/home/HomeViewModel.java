package fr.ifremer.scoop3.gui.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import fr.ifremer.scoop3.core.report.validation.Report;
import fr.ifremer.scoop3.model.valueAndQc.ValueAndQC;

public class HomeViewModel {

    /**
     * path of BCP file
     */
    protected AtomicReference<String> path = new AtomicReference<>();

    /**
     * path of Scoop3Explorer files
     */
    protected List<AtomicReference<String>> paths = new ArrayList<AtomicReference<String>>();

    /**
     * report
     */
    private final AtomicReference<Report> report = new AtomicReference<>();

    /**
     * reports of Scoop3Explorer files
     */
    private List<AtomicReference<Report>> reports = new ArrayList<AtomicReference<Report>>();

    private List<HashMap<String, ValueAndQC>> metadatas = new ArrayList<HashMap<String, ValueAndQC>>();

    public HomeViewModel() {
	// empty constructor
    }

    public Report getReport() {
	return report.get();
    }

    public ArrayList<Report> getReports() {
	final ArrayList<Report> list = new ArrayList<Report>();
	for (final AtomicReference<Report> r : reports) {
	    list.add(r.get());
	}
	return list;
    }

    public void setReport(final Report report) {
	this.report.set(report);
    }

    public void addReport(final Report report) {
	final AtomicReference<Report> newReport = new AtomicReference<Report>();
	newReport.set(report);
	reports.add(newReport);
    }

    public String getPath() {
	return path.get();
    }

    public ArrayList<String> getPaths() {
	final ArrayList<String> list = new ArrayList<String>();
	for (final AtomicReference<String> p : paths) {
	    list.add(p.get());
	}
	return list;
    }

    public List<HashMap<String, ValueAndQC>> getMetadatas() {
	return this.metadatas;
    }

    public void addMetadata(final int index, final String key, final ValueAndQC value) {
	if (metadatas.size() >= (index + 1)) {
	    metadatas.get(index).put(key, value);
	} else {
	    metadatas.add(new HashMap<String, ValueAndQC>());
	    metadatas.get(index).put(key, value);
	}
    }

    public void addBlankMetadata() {
	metadatas.add(new HashMap<String, ValueAndQC>());
    }

    public void resetPaths() {
	paths = new ArrayList<AtomicReference<String>>();
    }

    public void resetReports() {
	reports = new ArrayList<AtomicReference<Report>>();
    }

    public void resetMetadatas() {
	metadatas = new ArrayList<HashMap<String, ValueAndQC>>();
    }

    public void setPath(final String path) {
	this.path.set(path);
    }

    public void addPath(final String path) {
	final AtomicReference<String> newPath = new AtomicReference<String>();
	newPath.set(path);
	paths.add(newPath);
    }
}
