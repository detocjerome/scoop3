package fr.ifremer.scoop3.io.impl;

import java.sql.Connection;

import fr.ifremer.scoop3.io.DataBaseManager;

/**
 *
 * @author Altran
 *
 */
public abstract class AbstractDataBaseManager extends AbstractDataManager implements DataBaseManager {

    /**
     * Connection for database
     */
    protected Connection connection;

    /**
     *
     * @param connection
     * @param dataset
     */
    protected AbstractDataBaseManager(final Connection connection) {
	super();
	this.connection = connection;
    }

    /**
     * @return database connection
     */
    @Override
    public Connection getConnection() {
	return this.connection;
    }
}
