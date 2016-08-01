package HxCKDMS.HxCConfig.Handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;

public interface ICollectionsHandler {
    List<String> writeInCollection(Field field, Object value, HashMap<String, Object> subDataWatcher, ParameterizedType parameterizedType);
    Object readFromCollection(HashMap<String, Object> subDataWatcher, String currentLine, BufferedReader reader) throws IOException;

    Class<?>[] getTypes();
}
