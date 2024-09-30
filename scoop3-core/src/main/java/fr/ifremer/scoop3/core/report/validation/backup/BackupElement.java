package fr.ifremer.scoop3.core.report.validation.backup;

import java.io.Serializable;

import net.sf.ehcache.Element;

class BackupElement extends Element implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -8299498594233906470L;

    public BackupElement(final BackupKey key, final Backup value) {
	super(key, value);
    }

    public BackupKey getBackupKey() {
	return ((BackupKey) getObjectKey());
    }

}
