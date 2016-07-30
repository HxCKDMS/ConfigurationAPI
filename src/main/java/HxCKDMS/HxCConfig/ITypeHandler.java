package HxCKDMS.HxCConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;

public interface ITypeHandler {
    void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, String> DataWatcher, boolean InsideWriter) throws IllegalAccessException;

    void read(String variable, HashMap<String, String> DataWatcher, String currentLine, BufferedReader reader, Class<?> configClass, boolean InsideReader) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException;

    Class<?>[] getTypes();
}
