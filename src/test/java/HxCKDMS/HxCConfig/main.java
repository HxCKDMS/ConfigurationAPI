package HxCKDMS.HxCConfig;

import java.io.File;

public class main {
    private static HxCConfig config;

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException {
        config = new HxCConfig(RandomConfig.class, "testConfig", new File("D:\\dev\\Java projects\\ConfigurationAPI\\test"), "cfg", "test");
        config.initConfiguration();

        System.out.println(RandomConfig.asdft);
    }
}