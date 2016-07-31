package HxCKDMS.HxCConfig;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
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

        time = System.nanoTime();

        Type[] types = ((ParameterizedType) main.class.getDeclaredField("test").getGenericType()).getActualTypeArguments();

        if (types[1] instanceof ParameterizedType) {
            boolean check = ((ParameterizedType) types[1]).getRawType() == Map.class || Arrays.stream(((Class<?>) ((ParameterizedType) types[1]).getRawType()).getInterfaces()).filter(clazz -> clazz == Map.class).findAny().isPresent();

            if (check) {
                System.out.println("yes");
            }
            System.out.println(((ParameterizedType) types[1]).getActualTypeArguments()[0]);
            System.out.println(((ParameterizedType) types[1]).getActualTypeArguments()[1]);
        } else {
            System.out.println(((Class<?>) types[0]).getCanonicalName());
            System.out.println(((Class<?>) types[1]).getCanonicalName());
        }


        time = System.nanoTime() - time;
        System.out.println(time * 1E-9);
    }
}