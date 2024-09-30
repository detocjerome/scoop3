package fr.ifremer.scoop3.core.report.validation.backup;

public class BackupCacheException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 4112410963861689054L;

    public static final String PROGRAMMER_USAGE_OPEN = "HI Programmer ! You forgot to call ModelCache.open(), Don't forget to call ModelCache.close() too !";
    public static final String PROGRAMMER_USAGE_CLOSE = "HI Programmer ! You forgot to call ModelCache.open() and try to call ModelCache.close() !";
    public static final String NEW_CACHE_MANAGER = "Can't create cache manager : ";
    public static final String GET_LOCAL_CACHE = "Can't get local cache : ";
    public static final String GET_FROM_LOCAL_CACHE = "Can't get  from local cache : ";
    public static final String PUT_TO_LOCAL_CACHE = "Can't store  to local cache : ";
    public static final String ADD_NEW_TO_LOCAL_CACHE = "Can't add existing  to local cache : ";
    public static final String UPDATE_TO_LOCAL_CACHE = "Can't update  to local cache : ";
    public static final String CLEAR_LOCAL_CACHE = "Can't clear  from server cache : ";

    public BackupCacheException(final String message) {
	super(message);
    }
}
