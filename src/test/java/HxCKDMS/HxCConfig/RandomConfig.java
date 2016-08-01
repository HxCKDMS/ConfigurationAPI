package HxCKDMS.HxCConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Config
public class RandomConfig {
    //public static List<List<List<List<String>>>> test = new ArrayList<>();

    public static List<List<List<String>>> test5 = new ArrayList<>();

    //public static List<List<String>> test1 = new ArrayList<>();

    //public static List<String> test4 = new ArrayList<>();

    static {
        List<List<String>> test1 = new ArrayList<>();
        List<List<String>> test2 = new ArrayList<>();
        List<List<String>> test3 = new ArrayList<>();
        //List<List<List<String>>> test5 = new ArrayList<>();

        test1.add(Arrays.asList("test", "karle", "mnadrkea"));
        test1.add(Arrays.asList("no", "yes", "maybe"));
        test1.add(Arrays.asList("true", "false", "neither"));
        test5.add(test1);

        test2.add(Arrays.asList("hfd", "54t", "htr"));
        test2.add(Arrays.asList("nbc", "gasd", "ewrg"));
        test2.add(Arrays.asList("gfsd", "rwe", "ytr"));
        test5.add(test2);

        test3.add(Arrays.asList("bgd", "dsfag", "sfg"));
        test3.add(Arrays.asList("adsf", "xcas", "bcv"));
        test3.add(Arrays.asList("try", "bgfd", "tht"));
        test5.add(test3);

        //test.add(test5);

        //test4 = Arrays.asList("asdf", "fdsa", "qwerty");
    }
}