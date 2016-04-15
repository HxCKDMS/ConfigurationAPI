package HxCKDMS.HxCUtils;


import java.util.logging.Level;
import java.util.logging.LogManager;

@SuppressWarnings("unused")
public class LogHelper {
    private static LogManager logManager = LogManager.getLogManager();

    public static void log(Level logLevel, Object object, String modName){
        logManager.getLogger(modName).log(logLevel, object.toString());
    }

    public static void all(Object object, String modName){
        log(Level.ALL, object, modName);
    }

    public static void fine(Object object, String modName){
        log(Level.FINE, object, modName);
    }

    public static void finer(Object object, String modName){
        log(Level.FINER, object, modName);
    }

    public static void finest(Object object, String modName){
        log(Level.FINEST, object, modName);
    }

    public static void severe(Object object, String modName){
        log(Level.SEVERE, object, modName);
    }

    public static void info(Object object, String modName){
        log(Level.INFO, object, modName);
    }

    public static void off(Object object, String modName){
        log(Level.OFF, object, modName);
    }

    public static void warn(Object object, String modName){
        log(Level.WARNING, object, modName);
    }
}
