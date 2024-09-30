package fr.ifremer.scoop3.io.driver;

import java.util.List;

import fr.ifremer.scoop3.events.GuiEventResetDrivers;

public interface DriverManager {

    void registerNewDriver(IDriver driver);

    List<IDriver> findDriverForFile(String file) throws DriverException;

    List<IDriver> getDrivers();

    void resetDrivers(final GuiEventResetDrivers event);

}
