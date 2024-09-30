package fr.ifremer.scoop3.core.report.validation.backup;

import java.io.Serializable;

public class BackupKey implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3120681972729135367L;

    private final String uri;

    protected BackupKey(final String backupURI) {
	this.uri = backupURI;
    }

    protected String getURI() {
	return uri;
    }

    @Override
    public String toString() {
	return uri.toString();
    }
}
