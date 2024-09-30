package fr.ifremer.scoop3.io.datasetCache;

import java.io.Serializable;

import net.sf.ehcache.Element;
import fr.ifremer.scoop3.model.Dataset;

class DatasetElement extends Element implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -8299498594233906470L;

    public DatasetElement(final DatasetKey key, final Dataset value) {
	super(key, value);
    }

    public DatasetKey getDatasetKey() {
	return ((DatasetKey) getObjectKey());
    }

}
