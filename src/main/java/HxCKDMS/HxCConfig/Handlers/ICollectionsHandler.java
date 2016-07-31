package HxCKDMS.HxCConfig.Handlers;

import java.io.BufferedReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public interface ICollectionsHandler {
    List<String> writeInCollection(Field field, Object value, HashMap<String, Object> subDataWatcher);
    Object readFromCollection(HashMap<String, Object> subDataWatcher, String currentLine, BufferedReader reader);

    Class<?>[] getTypes();
}
