package HxCKDMS.HxCConfig;

import java.util.LinkedHashMap;

@Config
public class RandomConfig {
    public static LinkedHashMap<LinkedHashMap<String, Integer>, String> test = new LinkedHashMap<LinkedHashMap<String, Integer>, String>(){{
        put(new LinkedHashMap<String, Integer>(){{
            put("asdf1", 1);
            put("asdf2", 2);
            put("asdf3", 3);
        }}, "test1");

        put(new LinkedHashMap<String, Integer>(){{
            put("asdf4", 4);
            put("asdf5", 5);
            put("asdf6", 6);
        }}, "test2");

        put(new LinkedHashMap<String, Integer>(){{
            put("asdf7", 7);
            put("asdf8", 8);
            put("asdf9", 9);
        }}, "test3");
    }};
}