package HxCKDMS.HxCConfig;

import java.util.LinkedList;
import java.util.List;

@Config
public class RandomConfig {
    public static List<Purple> asdf = new LinkedList<>();

    static {
        Purple p1 = new Purple();
        Purple p2 = new Purple();
        Purple p3 = new Purple();

        p1.x = 1;
        p1.y = 2;
        p1.z = 3;

        p2.x = 4;
        p2.y = 5;
        p2.z = 6;

        p3.x = 7;
        p3.y = 8;
        p3.z = 9;


        asdf.add(p1);
        asdf.add(p2);
        asdf.add(p3);
    }
}