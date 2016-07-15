package HxCKDMS.HxCConfig;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Config
public class RandomConfig {
    public static int test = 15;
    public static long asdf = 9L;

    @Config.comment("This is a random HashMap")
    public static HashMap<String, Long> lol = new HashMap<String, Long>() {{
        put("lol", 5L);
        put("test", 134084525642L);
        put("asdf", -325346554235423L);
    }};

    public static Map<String, String> asdft = new LinkedHashMap<>();
}
