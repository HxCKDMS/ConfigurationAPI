package HxCKDMS.HxCConfig;

import java.util.LinkedHashMap;

@Config
public class RandomConfig {
    public static LinkedHashMap<String, Integer> test = new LinkedHashMap<String, Integer>(){{
        put("test", 12);
        put("asdf", -15);
    }};
}