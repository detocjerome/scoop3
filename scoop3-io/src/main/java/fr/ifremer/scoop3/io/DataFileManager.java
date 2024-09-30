package fr.ifremer.scoop3.io;

import java.io.File;

/**
 * 
 * @author Altran
 * 
 */
public interface DataFileManager extends DataManager {

    /**
     * @return the current file
     */
    File getFile();
}
