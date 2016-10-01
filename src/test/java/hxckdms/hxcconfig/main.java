package hxckdms.hxcconfig;

import hxckdms.hxcconfig.handlers.SpecialHandlers;

import java.io.File;
import java.lang.reflect.Field;

public class main {
    //THIS IS THE LAST TEST

    private static HxCConfig config;

    private static blargh test = blargh.asdf2;

    public static void main(String[] args) {

        SpecialHandlers.registerSpecialClass(Purple.class);
        SpecialHandlers.registerSpecialClass(Blue.class);

        long time = System.nanoTime();

        config = new HxCConfig(RandomConfig.class, "testConfig", new File(".\\test"), "cfg", "test");
        config.initConfiguration();

        time = System.nanoTime() - time;
        System.out.println(time * 1E-9);

        try {
            Field field = main.class.getDeclaredField("test");

            System.out.println(field.getType().isEnum());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}

