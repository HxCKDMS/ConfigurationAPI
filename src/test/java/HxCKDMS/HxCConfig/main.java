package HxCKDMS.HxCConfig;

import HxCKDMS.HxCConfig.Handlers.SpecialHandlers;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static HxCKDMS.HxCConfig.RandomConfig.asdf;

public class main {
    private static HxCConfig config;

    private static HashMap<String, Map<Float, String>> test = new HashMap<>();

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {

        SpecialHandlers.registerSpecialClass(Purple.class);

        long time = System.nanoTime();

        config = new HxCConfig(RandomConfig.class, "testConfig", new File("D:\\Development\\IdeaProjects\\ConfigurationAPI\\test"), "cfg", "test");
        config.initConfiguration();

        time = System.nanoTime() - time;
        System.out.println(time * 1E-9);

        Object tmp = asdf.get(1);
        Field f = tmp.getClass().getDeclaredField("x");

        f.setInt(tmp, 15);

        System.out.println(asdf.get(1).x);

        System.out.println(f.getInt(tmp));

    }
}