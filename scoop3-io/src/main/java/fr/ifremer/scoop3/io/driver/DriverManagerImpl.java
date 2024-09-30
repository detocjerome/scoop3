package fr.ifremer.scoop3.io.driver;

import java.util.ArrayList;
import java.util.List;

import fr.ifremer.scoop3.events.GuiEventResetDrivers;

public class DriverManagerImpl implements DriverManager {

    protected ArrayList<IDriver> drivers;

    public DriverManagerImpl() {
	this.drivers = new ArrayList<>();
    }

    /**/
    @Override
    public void registerNewDriver(final IDriver driver) {
	if (driver == null) {
	    throw new IllegalArgumentException();
	}
	this.drivers.add(driver);
    }

    /**/

    @Override
    public List<IDriver> findDriverForFile(final String file) throws DriverException {

	final List<IDriver> availableDrivers = new ArrayList<IDriver>();

	for (final IDriver driver : drivers) {
	    if (driver.canOpen(file)) {
		availableDrivers.add(driver);
	    }
	}
	return availableDrivers;
    }
    /**/

    @Override
    public List<IDriver> getDrivers() {
	return drivers;
    }

    @Override
    public void resetDrivers(final GuiEventResetDrivers event) {
	for (final IDriver driver : drivers) {
	    driver.resetFileManager();
	}
    }

}
