package fr.ifremer.scoop3.io.datasetCache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatasetKey implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7657295103459196775L;

    public enum VERSION {
	DATABASE, NEW, UPDATED
    }

    public enum UPDATE {
	CAMPAIGN, CHIEF, DIVE, OPERATION
    }

    private final ArrayList<UPDATE> updateList = new ArrayList<>();
    private VERSION version;
    private final String uri;

    protected DatasetKey(final String datasetURI, final VERSION version, final UPDATE... updates) {
	this.uri = datasetURI;
	this.version = version;
	addUpdates(updates);
    }

    public VERSION getVersion() {
	return version;
    }

    protected String getURI() {
	return uri;
    }

    protected final void addUpdates(final UPDATE... updates) {
	if ((version == VERSION.DATABASE) && (updates.length > 0)) {
	    version = VERSION.UPDATED;
	}
	for (final UPDATE update : updates) {
	    if (!updateList.contains(update)) {
		updateList.add(update);
	    }
	}
    }

    public List<UPDATE> getUpdates() {
	return Collections.unmodifiableList(updateList);
    }

    @Override
    public String toString() {
	return uri;
    }
}
