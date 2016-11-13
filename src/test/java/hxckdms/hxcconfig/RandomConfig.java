package hxckdms.hxcconfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

@Config
public class RandomConfig {
    public static LinkedHashMap<Integer, Blue> blues = new LinkedHashMap<Integer, Blue>(){{
        put(5, new Blue(532, 543, 52, 235));
        put(543, new Blue(543, 1234, 4123, 532462));
        put(645, new Blue(432, 5423, 876, 45));
        put(34, new Blue(543, 77, 687, 87));
        put(345, new Blue(6543, 87, 786, 645));
    }};

    public static String test = "asdfkl";
    public static double PI = Math.PI;

    public static LinkedHashMap<HashMap<String, Integer>, LinkedList<Boolean>> rawr = new LinkedHashMap<HashMap<String, Integer>, LinkedList<Boolean>>() {{
        put(new HashMap<String, Integer>() {{
            put("test", 5);
            put("asdf", 1);
            put("rawr", -1);
        }}, new LinkedList<>(Arrays.asList(true, false, false)));

        put(new HashMap<String, Integer>() {{
            put("abv", 543);
            put("tre", 654);
            put("hdf", -65);
        }}, new LinkedList<>(Arrays.asList(false, true, false)));

        put(new HashMap<String, Integer>() {{
            put("sdh", 3345);
            put("gfsd", 6354);
            put("erwt", -765);
        }}, new LinkedList<>(Arrays.asList(false, true, true)));
    }};
}