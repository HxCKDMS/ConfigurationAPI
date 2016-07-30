package HxCKDMS.HxCUtils;


import java.util.logging.Level;
import java.util.logging.LogManager;

@SuppressWarnings("unused")
public class LogHelper {
    private static LogManager logManager = LogManager.getLogManager();

    public static void log(Level logLevel, Object object, String app_name) {
        logManager.getLogger(app_name).log(logLevel, object.toString());
    }

    public static void all(Object object, String app_name) {
        log(Level.ALL, object, app_name);
    }

    public static void fine(Object object, String app_name) {
        log(Level.FINE, object, app_name);
    }

    public static void finer(Object object, String app_name) {
        log(Level.FINER, object, app_name);
    }

    public static void finest(Object object, String app_name) {
        log(Level.FINEST, object, app_name);
    }

    public static void severe(Object object, String app_name) {
        log(Level.SEVERE, object, app_name);
    }

    public static void info(Object object, String app_name) {
        log(Level.INFO, object, app_name);
    }

    public static void off(Object object, String app_name) {
        log(Level.OFF, object, app_name);
    }

    public static void warn(Object object, String app_name) {
        log(Level.WARNING, object, app_name);
    }
}
