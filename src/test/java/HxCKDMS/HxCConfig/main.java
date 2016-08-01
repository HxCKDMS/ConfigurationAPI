package HxCKDMS.HxCConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class main {
    private static HxCConfig config;

    private static HashMap<String, Map<Float, String>> test = new HashMap<>();

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        long time = System.nanoTime();

        config = new HxCConfig(RandomConfig.class, "testConfig", new File("D:\\Development\\IdeaProjects\\ConfigurationAPI\\test"), "cfg", "test");
        config.initConfiguration();

        time = System.nanoTime() - time;
        System.out.println(time * 1E-9);
    }
}