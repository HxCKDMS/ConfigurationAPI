package HxCKDMS.HxCConfig.Handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;

public interface ITypeHandler {
    void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, Object> dataWatcher) throws IllegalAccessException;
    void read(String variable, HashMap<String, Object> dataWatcher, String currentLine, BufferedReader reader, Class<?> configClass) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException;

    Class<?>[] getTypes();
}
