package fr.ifremer.scoop3.io;

import java.sql.Connection;

/**
 * 
 * @author Altran
 * 
 */
public interface DataBaseManager extends DataManager {

    /**
     * @return database connection
     */
    Connection getConnection();
}
