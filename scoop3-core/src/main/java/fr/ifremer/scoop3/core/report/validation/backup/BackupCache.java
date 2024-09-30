package fr.ifremer.scoop3.core.report.validation.backup;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.io.Files;

import fr.ifremer.scoop3.core.report.utils.ReportUtils;
import fr.ifremer.scoop3.infra.resources.ResourceLoader;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.DiskStoreConfiguration;

public class BackupCache {

    private static final String CACHE_CONFIG = "backupehcache.xml";
    public static final String LOCAL_CACHE = "backup";

    private CacheManager cacheManager = null;
    private Cache localCache = null;
    private String uri = null;

    /**
     * Model cache constructor, call open() before using and close() after.
     *
     */
    public BackupCache(final String path) {
	// Crate the directory for cache if not exist
	ReportUtils.createContextDirectory(Paths.get(path));
	setURI(path);

    }

    public void open() throws BackupCacheException {
	if (cacheManager == null) {
	    try {
		// Create Configuration object from backupEhCache.xml
		final Configuration configuration = ConfigurationFactory
			.parseConfiguration(new ResourceLoader(CACHE_CONFIG).getResource());

		// Define the path of backup cache
		final DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
		diskStoreConfiguration.setPath(translatePath(ReportUtils.getContextDir()));
		configuration.diskStore(diskStoreConfiguration);

		// Crate the cache manager
		cacheManager = CacheManager.newInstance(configuration);

	    } catch (CacheException | NoSuchFileException ce) {
		throw new BackupCacheException(BackupCacheException.NEW_CACHE_MANAGER + ce.getMessage());
	    }
	}
    }

    public void close() throws BackupCacheException {
	if (cacheManager == null) {
	    throw new BackupCacheException(BackupCacheException.PROGRAMMER_USAGE_CLOSE);
	}
	cacheManager.shutdown();
    }

    private CacheManager getCacheManager() throws BackupCacheException {
	if (cacheManager == null) {
	    throw new BackupCacheException(BackupCacheException.PROGRAMMER_USAGE_OPEN);
	}
	return cacheManager;
    }

    public Cache getCache() throws BackupCacheException {
	if (localCache == null) {
	    try {
		localCache = getCacheManager().getCache(LOCAL_CACHE);
	    } catch (CacheException | IllegalStateException | ClassCastException e) {
		throw new BackupCacheException(BackupCacheException.GET_LOCAL_CACHE + e.getMessage());
	    }
	    if (localCache == null) {
		throw new BackupCacheException(BackupCacheException.GET_LOCAL_CACHE + BackupCache.LOCAL_CACHE);
	    }
	}
	return (localCache);
    }

    public Backup getFromCache(final BackupKey backupKey) throws BackupCacheException {
	try {
	    final BackupElement element = (BackupElement) getCache().get(backupKey);
	    if (element != null) {
		return ((Backup) (element.getObjectValue()));
	    }
	    throw new BackupCacheException(BackupCacheException.GET_FROM_LOCAL_CACHE);
	} catch (IllegalStateException | CacheException e) {
	    throw new BackupCacheException(BackupCacheException.GET_FROM_LOCAL_CACHE + e.getMessage());
	}
    }

    public BackupKey addNewToCache(final Backup backup) throws BackupCacheException {
	try {
	    final String path = backup.getURI();

	    // 57274 : old backup uri was path, new backup uri is fileName
	    final String fileName = Files.getNameWithoutExtension(path) + "." + Files.getFileExtension(path);
	    final BackupKey backupKey = new BackupKey(fileName);
	    backup.setURI(fileName);

	    // remove all to avoid problems with doublons
	    getCache().removeAll();

	    // flush for generate index file
	    getCache().flush();

	    getCache().put(new BackupElement(backupKey, backup));
	    return (backupKey);
	} catch (IllegalArgumentException | IllegalStateException | CacheException e) {
	    throw new BackupCacheException(BackupCacheException.PUT_TO_LOCAL_CACHE + e.getMessage());
	}
    }

    public BackupKey getBackupKey(final String URI) throws BackupCacheException {
	for (final BackupKey backupKey : getBackupKeys()) {
	    if (backupKey == null) {
		return backupKey;
	    }
	    // 57274 : old backup uri was path, new backup uri is fileName
	    final String fileName = Files.getNameWithoutExtension(URI) + "." + Files.getFileExtension(URI);
	    final String backUpKeyFileName = Files.getNameWithoutExtension(backupKey.getURI()) + "."
		    + Files.getFileExtension(backupKey.getURI());
	    if ((backupKey.getURI() != null) && backUpKeyFileName.equalsIgnoreCase(fileName)) {
		return (backupKey);
	    }
	}
	return null;
    }

    @SuppressWarnings("unchecked")
    public List<BackupKey> getBackupKeys() throws BackupCacheException {
	@SuppressWarnings("rawtypes")
	final List list = getCache().getKeys();
	return (list);
    }

    /**
     * Avoid problem with translate of DiskStoreConfiguration translatePath methode
     *
     * @param path
     * @return
     */
    private static String translatePath(final String path) {
	return path.replace(File.separator, File.separator + File.separator);
    }

    public String getURI() {
	return this.uri;
    }

    public void setURI(final String uri) {
	this.uri = uri;
    }

}
