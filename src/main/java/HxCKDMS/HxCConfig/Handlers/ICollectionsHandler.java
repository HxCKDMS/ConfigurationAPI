package HxCKDMS.HxCConfig.Handlers;

import java.io.BufferedReader;
import java.lang.reflect.Field;
import java.util.HashMap;

public interface ICollectionsHandler {
    String writeInCollection(Field field, Object value, HashMap<String, String> subDataWatcher);
    Object readFromCollection(HashMap<String, String> subDataWatcher, String currentLine, BufferedReader reader);

    Class<?>[] getTypes();
}
