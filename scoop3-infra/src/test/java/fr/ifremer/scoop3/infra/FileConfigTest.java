package fr.ifremer.scoop3.infra;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.ifremer.scoop3.infra.properties.FileConfig;

public class FileConfigTest {

    @Test
    public void test() throws Exception {
	FileConfig fileConfig = FileConfig.getScoop3FileConfig();
	assertEquals(fileConfig.getString("application.title"), fileConfig.getString("application.title"));
    }
}
