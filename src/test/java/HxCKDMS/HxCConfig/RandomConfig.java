package HxCKDMS.HxCConfig;

import java.util.ArrayList;
import java.util.List;

@Config
public class RandomConfig {
    public static List<Purple> asdf = new ArrayList<Purple>(){{
        add(new Purple(1, 2, 3));
        add(new Purple(4, 5, 6));
        add(new Purple(7, 8, 9));
    }};

    public static Purple purple = new Purple(15, 16, 17);
}