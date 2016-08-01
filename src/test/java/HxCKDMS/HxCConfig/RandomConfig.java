package HxCKDMS.HxCConfig;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@Config
public class RandomConfig {
    public static LinkedHashMap<List<List<String>>, List<List<String>>> test = new LinkedHashMap<>();

    static {
        List<String> test1 = Arrays.asList("test", "gklvasd", "nvirr");
        List<String> test2 = Arrays.asList("bgf", "rewvfs", "bsd");
        List<String> test3 = Arrays.asList("rvweds", "breswe", "asdf");

        List<String> test4 = Arrays.asList("fasdf", "fdsav", "bgf");
        List<String> test5 = Arrays.asList("bgfdtr", "asdf", "brt");
        List<String> test6 = Arrays.asList("Keldon", "Sietse", "Anthony");

        test.put(Arrays.asList(test1, test2, test3), Arrays.asList(test4, test5, test6));
        test.put(Arrays.asList(test2, test3, test1), Arrays.asList(test5, test6, test4));
        test.put(Arrays.asList(test3, test1, test2), Arrays.asList(test6, test4, test5));
    }
}