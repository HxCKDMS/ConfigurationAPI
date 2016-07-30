package HxCKDMS.HxCConfig;

import java.util.HashMap;

@Config
public class RandomConfig {
    public static HashMap<String, Integer> test = new HashMap<String, Integer>(){{
        put("test", 153342);
        put("asdf", 534);
        put("failure", 404);
    }};
}