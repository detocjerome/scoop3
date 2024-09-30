package fr.ifremer.scoop3.gui.home;

import java.sql.SQLException;

public class HomeControllerException extends Exception {

    public HomeControllerException(SQLException e) {
	super(e);
    }

    public HomeControllerException(String message) {
	super(message);
    }

}
