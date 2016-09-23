package HxCKDMS.HxCConfig;

import java.util.HashMap;

@Config
public class RandomConfig {
    public static HashMap<Float, Purple> asdf = new HashMap<Float, Purple>(){{
        put(1.4356f, new Purple(1, 2, 3));
        put(0.543f, new Purple(4, 5, 6));
        put(-0.5342f, new Purple(7, 8, 9));
    }};

    public static Purple purple = new Purple(15, 16, 17);
}