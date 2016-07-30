package HxCKDMS.HxCConfig;

import java.io.File;

public class main {
    private static HxCConfig config;

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException {
        config = new HxCConfig(RandomConfig.class, "testConfig", new File("D:\\Development\\IdeaProjects\\ConfigurationAPI\\test"), "cfg", "test");
        config.initConfiguration();


    }
}