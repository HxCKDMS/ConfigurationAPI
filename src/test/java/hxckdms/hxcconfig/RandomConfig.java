package hxckdms.hxcconfig;

import java.util.LinkedHashMap;

@Config
public class RandomConfig {

    public static LinkedHashMap<Integer, String> test = new LinkedHashMap<Integer, String>(){{
        put(5, "fdsa");
        put(43, "fads");
        put(54, "asdvnilas");
    }};

    public static LinkedHashMap<Integer, Blue> blues = new LinkedHashMap<Integer, Blue>(){{
        put(5, new Blue(532, 543, 52, 235));
        put(543, new Blue(543, 1234, 4123, 532462));
        put(645, new Blue(432, 5423, 876, 45));
        put(34, new Blue(543, 77, 687, 87));
        put(345, new Blue(6543, 87, 786, 645));
    }};
}