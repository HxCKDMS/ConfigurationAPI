package HxCKDMS.HxCConfig;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@Config
public class RandomConfig {
    public static LinkedHashMap<String, List<Integer>> test = new LinkedHashMap<String, List<Integer>>(){{
        put("test", Arrays.asList(1, 2 ,3));
        put("asdf", Arrays.asList(4, 5, 6));
        put("bfd", Arrays.asList(7, 8, 9));
    }};
}