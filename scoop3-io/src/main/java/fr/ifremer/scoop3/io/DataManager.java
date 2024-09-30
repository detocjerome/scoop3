package fr.ifremer.scoop3.io;

import fr.ifremer.scoop3.model.Dataset;

/**
 * 
 * @author Altran
 * 
 */
public interface DataManager {

    /**
     * Read something and return something
     * 
     * @throws Exception
     */
    Dataset read() throws Exception;

    /**
     * Write something TODO Maybe the write method should return something too
     */
    void write();

}
