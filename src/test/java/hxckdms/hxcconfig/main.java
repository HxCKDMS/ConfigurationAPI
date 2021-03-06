package hxckdms.hxcconfig;

import hxckdms.hxcconfig.handlers.SpecialHandlers;

import java.io.File;

public class main {
    private static HxCConfig config;

    public static void main(String[] args) {

        SpecialHandlers.registerSpecialClass(Purple.class);
        SpecialHandlers.registerSpecialClass(Blue.class);

        long time = System.nanoTime();

        config = new HxCConfig(RandomConfig.class, "testConfig", new File(".\\test"), "cfg", "test");
        config.initConfiguration();

        time = System.nanoTime() - time;
        System.out.println(time * 1E-9);
    }
}

