package fr.ifremer.scoop3.io.impl;

import java.io.File;

import fr.ifremer.scoop3.io.DataFileManager;

/**
 *
 * @author jmens
 *
 *
 */
public abstract class AbstractDataFileManager extends AbstractDataManager implements DataFileManager {

    /**
     * Current file
     */
    protected File file;

    /**
     *
     * @param path
     */
    protected AbstractDataFileManager(final String path) {
	super();
	this.file = new File(path);
    }

    /**
     * Return the current file
     */
    @Override
    public File getFile() {
	return this.file;
    }

    /**
     * To override if needed
     *
     * @param filePath
     *
     * @return
     */
    public String getDataFilePath(final String filePath) {
	if (getFile() == null) {
	    return filePath;
	}
	return getFile().getAbsolutePath();
    }
}
