package fr.ifremer.scoop3.io.datasetCache;

import java.nio.file.NoSuchFileException;
import java.util.List;

import fr.ifremer.scoop3.infra.logger.SC3Logger;
import fr.ifremer.scoop3.infra.resources.ResourceLoader;
import fr.ifremer.scoop3.model.Dataset;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;

public class DatasetCache {

    private static final String CACHE_CONFIG = "ehcache.xml";
    private static final String LOCAL_CACHE = "datasets";

    private CacheManager cacheManager = null;
    private Cache localCache = null;

    /**
     * Model cache constructor, call open() before using and close() after.
     *
     */
    public DatasetCache() {
	// empty constructor
    }

    public void open() throws DatasetCacheException {
	if (cacheManager == null) {
	    try {
		cacheManager = CacheManager.newInstance(new ResourceLoader(CACHE_CONFIG).getResource());

	    } catch (CacheException | NoSuchFileException ce) {
		throw new DatasetCacheException(DatasetCacheException.NEW_CACHE_MANAGER + ce.getMessage());
	    }
	}
    }

    public void close() throws DatasetCacheException {
	if (cacheManager == null) {
	    throw new DatasetCacheException(DatasetCacheException.PROGRAMMER_USAGE_CLOSE);
	}
	cacheManager.shutdown();
    }

    private CacheManager getCacheManager() throws DatasetCacheException {
	if (cacheManager == null) {
	    throw new DatasetCacheException(DatasetCacheException.PROGRAMMER_USAGE_OPEN);
	}
	return cacheManager;
    }

    private Cache getCache() throws DatasetCacheException {
	if (localCache == null) {
	    try {
		localCache = getCacheManager().getCache(LOCAL_CACHE);
	    } catch (CacheException | IllegalStateException | ClassCastException e) {
		throw new DatasetCacheException(DatasetCacheException.GET_LOCAL_CACHE + e.getMessage());
	    }
	    if (localCache == null) {
		throw new DatasetCacheException(DatasetCacheException.GET_LOCAL_CACHE + DatasetCache.LOCAL_CACHE);
	    }
	}
	return (localCache);
    }

    public Dataset getFromCache(final DatasetKey datasetKey) throws DatasetCacheException {
	try {
	    final DatasetElement element = (DatasetElement) getCache().get(datasetKey);
	    if (element != null) {
		return ((Dataset) (element.getObjectValue()));
	    }
	    throw new DatasetCacheException(DatasetCacheException.GET_FROM_LOCAL_CACHE);
	} catch (IllegalStateException | CacheException e) {
	    throw new DatasetCacheException(DatasetCacheException.GET_FROM_LOCAL_CACHE + e.getMessage());
	}
    }

    public void updateInCache(final Dataset dataset, final DatasetKey.UPDATE... updates) throws DatasetCacheException {
	try {
	    final DatasetKey campaignKey = getDatasetKey(dataset.getURI());
	    if (campaignKey == null) {
		throw new DatasetCacheException(DatasetCacheException.UPDATE_TO_LOCAL_CACHE);
	    }
	    campaignKey.addUpdates(updates);
	    getCache().put(new DatasetElement(campaignKey, dataset));

	} catch (IllegalArgumentException | IllegalStateException | CacheException e) {
	    throw new DatasetCacheException(DatasetCacheException.UPDATE_TO_LOCAL_CACHE + e.getMessage());
	}
    }

    public DatasetKey addNewToCache(final Dataset dataset) throws DatasetCacheException {
	try {
	    if (getDatasetKey(dataset.getURI()) != null) {
		throw new DatasetCacheException(DatasetCacheException.ADD_NEW_TO_LOCAL_CACHE);
	    }
	    final DatasetKey datasetKey = new DatasetKey(dataset.getURI(), DatasetKey.VERSION.NEW);
	    getCache().put(new DatasetElement(datasetKey, dataset));
	    return (datasetKey);
	} catch (IllegalArgumentException | IllegalStateException | CacheException e) {
	    throw new DatasetCacheException(DatasetCacheException.PUT_TO_LOCAL_CACHE + e.getMessage());
	}
    }

    // public void rebuildCacheFromDatabase() throws SQLException, DatasetCacheException {
    // long t1 = System.currentTimeMillis();
    // CampaignDAOFactory factory = CampaignDAOFactory.getInstance();
    // List<Dataset> datasets = new CampaignDAO(factory).getCampaignList();
    // try {
    // getCache().removeAll();
    // } catch (IllegalStateException | CacheException e) {
    // throw new DatasetCacheException(DatasetCacheException.CLEAR_LOCAL_CACHE + e.getMessage());
    // }
    // boolean first = true;
    // for (Dataset dataset : datasets) {
    // if (first) {
    // // to remove
    // addNewToCache(dataset);
    // first = false;
    // } else {
    // // to keep
    // getCache().put(
    // new DatasetElement(new DatasetKey(dataset.getURI(), DatasetKey.VERSION.DATABASE), dataset));
    // }
    // }
    // long t2 = System.currentTimeMillis();
    // SC3Logger.LOGGER.debug("void rebuildCacheFromDatabase() : " + (t2 - t1) + "ms");
    //
    // }

    public DatasetKey getDatasetKey(final String URI) throws DatasetCacheException {
	final long t1 = System.currentTimeMillis();
	for (final DatasetKey datasetKey : getDatasetKeys()) {
	    if (datasetKey.getURI().equals(URI)) {
		final long t2 = System.currentTimeMillis();
		SC3Logger.LOGGER.debug("CampaignKey getCampaignKey(Integer campaignCode) : " + (t2 - t1) + "ms");
		return (datasetKey);
	    }
	}
	return (null);
    }

    @SuppressWarnings("unchecked")
    public List<DatasetKey> getDatasetKeys() throws DatasetCacheException {
	final long t1 = System.currentTimeMillis();
	@SuppressWarnings("rawtypes")
	final List list = getCache().getKeys();
	final long t2 = System.currentTimeMillis();
	SC3Logger.LOGGER.debug("List<DatasetKey> getCampaignKeys() : " + (t2 - t1) + "ms");
	return (list);
    }

}
