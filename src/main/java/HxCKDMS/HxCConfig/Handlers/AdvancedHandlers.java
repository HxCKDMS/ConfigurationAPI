package HxCKDMS.HxCConfig.Handlers;

import HxCKDMS.HxCConfig.Config;
import HxCKDMS.HxCConfig.HxCConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static HxCKDMS.HxCConfig.Flags.overwrite;
import static HxCKDMS.HxCConfig.Flags.retainOriginalValues;

@SuppressWarnings("unchecked")
public class AdvancedHandlers {

    //LIST STUFF
    private static void mainListWriter(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, String> DataWatcher) throws IllegalAccessException {
        Class<?> type = (Class<?>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
        ICollectionsHandler cHandler = HxCConfig.getCollectionsHandler(type);

        List<Object> tempList = (List<Object>) field.get(null);

        String categoryName = field.isAnnotationPresent(Config.category.class) ? field.getAnnotation(Config.category.class).value() : "General";
        StringBuilder listTextBuilder = new StringBuilder();

        listTextBuilder.append('[');
        tempList.forEach(item -> listTextBuilder.append('\n').append("\t\t").append(cHandler.writeInCollection(field, item, null)));
        listTextBuilder.append('\n').append('\t').append(']');

        LinkedHashMap<String, Object> categoryValues = config.getOrDefault(categoryName, new LinkedHashMap<>());
        categoryValues.putIfAbsent(field.getName(), listTextBuilder.toString());
        config.put(categoryName, categoryValues);

        DataWatcher.put("ListType", type.getCanonicalName());
    }

    private static <T> void mainListReader(String variable, HashMap<String, String> DataWatcher, BufferedReader reader, Class<?> configClass, List<T> tempList) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
        Field field = configClass.getField(variable);
        Class<T> listType = (Class<T>) Class.forName(DataWatcher.get("ListType"));
        ICollectionsHandler cHandler = HxCConfig.getCollectionsHandler(listType);

        if (field.isAnnotationPresent(Config.flags.class) && (field.getAnnotation(Config.flags.class).value() & retainOriginalValues) == 0b1) tempList = (List<T>) field.get(null);

        String line;
        while ((line = reader.readLine()) != null && !line.trim().equals("]")) try { tempList.add((T) cHandler.readFromCollection(null, line.trim(), reader)); } catch (Exception ignored) {}

        if (field.isAnnotationPresent(Config.flags.class) && (field.getAnnotation(Config.flags.class).value() & overwrite) == 0b10) {
            if (field.get(null) == null || ((List) field.get(null)).isEmpty()) field.set(configClass, tempList);
        } else field.set(configClass, tempList);
    }

    public static class ListHandler implements ITypeHandler {

        @Override
        public void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, String> DataWatcher, boolean InsideReader) throws IllegalAccessException {
            mainListWriter(field, config, DataWatcher);
            DataWatcher.put("Type", List.class.getCanonicalName());
        }

        @Override
        public void read(String variable, HashMap<String, String> DataWatcher, String currentLine, BufferedReader reader, Class<?> configClass, boolean InsideReader) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
            mainListReader(variable, DataWatcher, reader, configClass, new LinkedList<>());
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[] {List.class};
        }
    }

    public static class LinkedListHandler implements ITypeHandler {

        @Override
        public void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, String> DataWatcher, boolean InsideReader) throws IllegalAccessException {
            mainListWriter(field, config, DataWatcher);
            DataWatcher.put("Type", LinkedList.class.getCanonicalName());
        }

        @Override
        public void read(String variable, HashMap<String, String> DataWatcher, String currentLine, BufferedReader reader, Class<?> configClass, boolean InsideReader) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
            mainListReader(variable, DataWatcher, reader, configClass, new LinkedList<>());
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[]{LinkedList.class};
        }
    }

    public static class ArrayListHandler implements ITypeHandler {

        @Override
        public void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, String> DataWatcher, boolean InsideReader) throws IllegalAccessException {
            mainListWriter(field, config, DataWatcher);
            DataWatcher.put("Type", ArrayList.class.getCanonicalName());
        }

        @Override
        public void read(String variable, HashMap<String, String> DataWatcher, String currentLine, BufferedReader reader, Class<?> configClass, boolean InsideReader) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
            mainListReader(variable, DataWatcher, reader, configClass, new ArrayList<>());
        }


        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[]{ArrayList.class};
        }
    }

    //MAP STUFF

    private static void mainMapWriter(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, String> DataWatcher) throws IllegalAccessException {
        Map<Object, Object> tempMap = (Map<Object, Object>) field.get(null);

        Type[] types = ((ParameterizedType)field.getGenericType()).getActualTypeArguments();
        Class<?> keyType = (Class<?>) types[0];
        Class<?> valueType = (Class<?>) types[1];

        ICollectionsHandler cKeyHandler = HxCConfig.getCollectionsHandler(keyType);
        ICollectionsHandler cValueHandler = HxCConfig.getCollectionsHandler(valueType);

        String categoryName = field.isAnnotationPresent(Config.category.class) ? field.getAnnotation(Config.category.class).value() : "General";
        StringBuilder mapTextBuilder = new StringBuilder();

        mapTextBuilder.append('[');
        tempMap.forEach((key, value) -> mapTextBuilder.append('\n').append("\t\t").append(cKeyHandler.writeInCollection(field, key, null)).append('=').append(cValueHandler.writeInCollection(field, value, null)));
        mapTextBuilder.append('\n').append('\t').append(']');

        LinkedHashMap<String, Object> categoryValues = config.getOrDefault(categoryName, new LinkedHashMap<>());
        categoryValues.putIfAbsent(field.getName(), mapTextBuilder.toString());
        config.put(categoryName, categoryValues);


        DataWatcher.put("MapKeyType", keyType.getCanonicalName());
        DataWatcher.put("MapValueType", valueType.getCanonicalName());
    }

    private static <K,V> void mainMapReader(String variable, HashMap<String, String> DataWatcher, BufferedReader reader, Class<?> configClass, Map<K,V> tempMap) throws NoSuchFieldException, ClassNotFoundException, IOException, IllegalAccessException {
        Field field = configClass.getField(variable);
        Class<K> mapKeyType = (Class<K>) Class.forName(DataWatcher.get("MapKeyType"));
        Class<V> mapValueType = (Class<V>) Class.forName(DataWatcher.get("MapValueType"));

        ICollectionsHandler cKeyHandler = HxCConfig.getCollectionsHandler(mapKeyType);
        ICollectionsHandler cValueHandler = HxCConfig.getCollectionsHandler(mapValueType);

        if (field.isAnnotationPresent(Config.flags.class) && (field.getAnnotation(Config.flags.class).value() & retainOriginalValues) == 0b1) tempMap = (Map<K, V>) field.get(null);

        String line;
        while ((line = reader.readLine()) != null && !line.trim().equals("]")) try { tempMap.put((K) cKeyHandler.readFromCollection(null, line.split("=")[0].trim(), reader), (V) cValueHandler.readFromCollection(null, line.split("=")[1].trim(), reader)); } catch (Exception ignored) {}

        if (field.isAnnotationPresent(Config.flags.class) && (field.getAnnotation(Config.flags.class).value() & overwrite) == 0b10) {
            if (field.get(null) == null || ((Map) field.get(null)).isEmpty()) field.set(configClass, tempMap);
        } else field.set(configClass, tempMap);
    }

    public static class MapHandler implements ITypeHandler {

        @Override
        public void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, String> DataWatcher, boolean InsideReader) throws IllegalAccessException {
            mainMapWriter(field, config, DataWatcher);
            DataWatcher.put("Type", Map.class.getCanonicalName());
        }

        @Override
        public void read(String variable, HashMap<String, String> DataWatcher, String currentLine, BufferedReader reader, Class<?> configClass, boolean InsideReader) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
            mainMapReader(variable, DataWatcher, reader, configClass, new HashMap<>());
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[]{Map.class};
        }
    }

    public static class HashMapHandler implements ITypeHandler {

        @Override
        public void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, String> DataWatcher, boolean InsideReader) throws IllegalAccessException {
            mainMapWriter(field, config, DataWatcher);
            DataWatcher.put("Type", HashMap.class.getCanonicalName());
        }

        @Override
        public void read(String variable, HashMap<String, String> DataWatcher, String currentLine, BufferedReader reader, Class<?> configClass, boolean InsideReader) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
            mainMapReader(variable, DataWatcher, reader, configClass, new HashMap<>());
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[]{HashMap.class};
        }
    }

    public static class LinkedHashMapHandler implements ITypeHandler {

        @Override
        public void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, String> DataWatcher, boolean InsideReader) throws IllegalAccessException {
            mainMapWriter(field, config, DataWatcher);
            DataWatcher.put("Type", LinkedHashMap.class.getCanonicalName());
        }

        @Override
        public void read(String variable, HashMap<String, String> DataWatcher, String currentLine, BufferedReader reader, Class<?> configClass, boolean InsideReader) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
            mainMapReader(variable, DataWatcher, reader, configClass, new LinkedHashMap<>());
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[]{LinkedHashMap.class};
        }
    }
}