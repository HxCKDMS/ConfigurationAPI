package HxCKDMS.HxCConfig.Handlers;

import HxCKDMS.HxCConfig.Config;
import HxCKDMS.HxCConfig.HxCConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static HxCKDMS.HxCConfig.Flags.OVERWRITE;
import static HxCKDMS.HxCConfig.Flags.RETAIN_ORIGINAL_VALUES;

@SuppressWarnings("unchecked")
public class AdvancedHandlers {

    //LIST STUFF
    private static void mainListWriter(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, Object> dataWatcher) throws IllegalAccessException {
        Type[] types = ((ParameterizedType)field.getGenericType()).getActualTypeArguments();

        boolean isParameterized = (types[0] instanceof ParameterizedType);

        Class<?> type = isParameterized ? (Class<?>) ((ParameterizedType) types[0]).getRawType() : (Class<?>) types[0];
        ICollectionsHandler cHandler = HxCConfig.getCollectionsHandler(type);

        List<Object> tempList = (List<Object>) field.get(null);

        String categoryName = field.isAnnotationPresent(Config.category.class) ? field.getAnnotation(Config.category.class).value() : "General";
        StringBuilder listTextBuilder = new StringBuilder();

        HashMap<String, Object> subDataWatcher = new HashMap<>();

        listTextBuilder.append('[');
        boolean firstIteration = true;
        for (Object value : tempList) {
            listTextBuilder.append('\n').append(cHandler.writeInCollection(field, value, firstIteration ? subDataWatcher : null, isParameterized ? (ParameterizedType) types[0] : null).stream().map(str -> "\t\t" + str).reduce((a, b) -> a + "\n" + b).get());
            firstIteration = false;
        }
        listTextBuilder.append('\n').append('\t').append(']');

        LinkedHashMap<String, Object> categoryValues = config.getOrDefault(categoryName, new LinkedHashMap<>());
        categoryValues.putIfAbsent(field.getName(), listTextBuilder.toString());
        config.put(categoryName, categoryValues);

        dataWatcher.put("SubDataWatcher", subDataWatcher);
        dataWatcher.put("ListType", type);
    }

    private static List<String> mainListCollectionWriter(Field field, List<Object> value, HashMap<String, Object> subDataWatcher, ParameterizedType parameterizedType) {
        Type[] types = parameterizedType.getActualTypeArguments();
        boolean isParameterized = (types[0] instanceof ParameterizedType);
        Class<?> type = isParameterized ? (Class<?>) ((ParameterizedType) types[0]).getRawType() : (Class<?>) types[0];

        ICollectionsHandler cHandler = HxCConfig.getCollectionsHandler(type);

        LinkedList<String> lines = new LinkedList<>();
        HashMap<String, Object> subDataWatcherInner = new HashMap<>();

        lines.add("[");

        boolean firstIteration = true;
        for (Object obj : value) {
            lines.addAll(cHandler.writeInCollection(field, obj, firstIteration ? subDataWatcherInner : null, isParameterized ? (ParameterizedType) types[0] : null).stream().map(str -> "\t" + str).collect(Collectors.toList()));
            firstIteration = false;
        }
        lines.add("]");

        if (subDataWatcher != null) {
            subDataWatcher.put("SubDataWatcher", subDataWatcherInner);
            subDataWatcher.put("Type", type);
        }

        return lines;
    }

    private static <T> void mainListReader(String variable, HashMap<String, Object> dataWatcher, BufferedReader reader, Class<?> configClass, List<T> tempList) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
        Field field = configClass.getField(variable);
        Class<T> listType = (Class<T>) dataWatcher.get("ListType");
        ICollectionsHandler cHandler = HxCConfig.getCollectionsHandler(listType);
        HashMap<String, Object> subDataWatcher = (HashMap<String, Object>) dataWatcher.getOrDefault("SubDataWatcher", null);

        if (field.isAnnotationPresent(Config.flags.class) && (field.getAnnotation(Config.flags.class).value() & RETAIN_ORIGINAL_VALUES) == RETAIN_ORIGINAL_VALUES) tempList = (List<T>) field.get(null);

        String line;
        while ((line = reader.readLine()) != null && !line.trim().equals("]")) try { tempList.add((T) cHandler.readFromCollection(subDataWatcher, line.trim(), reader)); } catch (Exception ignored) {}

        if (field.isAnnotationPresent(Config.flags.class) && (field.getAnnotation(Config.flags.class).value() & OVERWRITE) == OVERWRITE) {
            if (field.get(null) == null || ((List) field.get(null)).isEmpty()) field.set(configClass, tempList);
        } else field.set(configClass, tempList);
    }

    private static <T> List mainListCollectionReader(HashMap<String, Object> subDataWatcher, String currentLine, BufferedReader reader, List<T> tempList) throws IOException {
        Class<T> listType = (Class<T>) subDataWatcher.get("Type");
        ICollectionsHandler cHandler = HxCConfig.getCollectionsHandler(listType);

        HashMap<String, Object> subDataWatcherInner = (HashMap<String, Object>) subDataWatcher.getOrDefault("SubDataWatcher", null);

        String line;
        while ((line = reader.readLine()) != null && !line.trim().equals("]")) try {
            if (cHandler instanceof ICollectionsTypeHandler && ((ICollectionsTypeHandler) cHandler).beginChar() == line.trim().charAt(0)) {
                tempList.add((T) cHandler.readFromCollection(subDataWatcherInner, line.trim(), reader));
                continue;
            }

            Object test = cHandler.readFromCollection(subDataWatcherInner, line.trim(), reader);
            tempList.add((T) test);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }


        return tempList;
    }

    public static class ListHandler implements ICollectionsTypeHandler, ICollectionsHandler {

        @Override
        public void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, Object> dataWatcher) throws IllegalAccessException {
            mainListWriter(field, config, dataWatcher);
            dataWatcher.put("Type", List.class);
        }

        @Override
        public void read(String variable, HashMap<String, Object> dataWatcher, String currentLine, BufferedReader reader, Class<?> configClass) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
            mainListReader(variable, dataWatcher, reader, configClass, new LinkedList<>());
        }

        @Override
        public List<String> writeInCollection(Field field, Object value, HashMap<String, Object> subDataWatcher, ParameterizedType parameterizedType) {
            return mainListCollectionWriter(field, (List) value, subDataWatcher, parameterizedType);
        }

        @Override
        public List readFromCollection(HashMap<String, Object> subDataWatcher, String currentLine, BufferedReader reader) throws IOException {
            return mainListCollectionReader(subDataWatcher, currentLine, reader, new ArrayList<>());
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[] {List.class};
        }

        @Override
        public char beginChar() {
            return '[';
        }

        @Override
        public char endChar() {
            return ']';
        }
    }

    public static class LinkedListHandler implements ICollectionsTypeHandler, ICollectionsHandler {

        @Override
        public void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, Object> dataWatcher) throws IllegalAccessException {
            mainListWriter(field, config, dataWatcher);
            dataWatcher.put("Type", LinkedList.class);
        }

        @Override
        public void read(String variable, HashMap<String, Object> dataWatcher, String currentLine, BufferedReader reader, Class<?> configClass) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
            mainListReader(variable, dataWatcher, reader, configClass, new LinkedList<>());
        }

        @Override
        public List<String> writeInCollection(Field field, Object value, HashMap<String, Object> subDataWatcher, ParameterizedType parameterizedType) {
            return mainListCollectionWriter(field, (List) value, subDataWatcher, parameterizedType);
        }

        @Override
        public LinkedList readFromCollection(HashMap<String, Object> subDataWatcher, String currentLine, BufferedReader reader) throws IOException {
            return (LinkedList) mainListCollectionReader(subDataWatcher, currentLine, reader, new LinkedList<>());
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[]{LinkedList.class};
        }

        @Override
        public char beginChar() {
            return '[';
        }

        @Override
        public char endChar() {
            return ']';
        }
    }

    public static class ArrayListHandler implements ICollectionsTypeHandler, ICollectionsHandler {

        @Override
        public void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, Object> dataWatcher) throws IllegalAccessException {
            mainListWriter(field, config, dataWatcher);
            dataWatcher.put("Type", ArrayList.class);
        }

        @Override
        public void read(String variable, HashMap<String, Object> dataWatcher, String currentLine, BufferedReader reader, Class<?> configClass) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
            mainListReader(variable, dataWatcher, reader, configClass, new ArrayList<>());
        }


        @Override
        public List<String> writeInCollection(Field field, Object value, HashMap<String, Object> subDataWatcher, ParameterizedType parameterizedType) {
            return mainListCollectionWriter(field, (List) value, subDataWatcher, parameterizedType);
        }

        @Override
        public ArrayList readFromCollection(HashMap<String, Object> subDataWatcher, String currentLine, BufferedReader reader) throws IOException {
            return (ArrayList) mainListCollectionReader(subDataWatcher, currentLine, reader, new ArrayList<>());
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[]{ArrayList.class};
        }

        @Override
        public char beginChar() {
            return '[';
        }

        @Override
        public char endChar() {
            return ']';
        }
    }

    //MAP STUFF

    private static void mainMapWriter(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, Object> DataWatcher) throws IllegalAccessException {
        Map<Object, Object> tempMap = (Map<Object, Object>) field.get(null);

        Type[] types = ((ParameterizedType)field.getGenericType()).getActualTypeArguments();

        boolean isKeyParameterized = types[0] instanceof ParameterizedType;
        boolean isValueParameterized = types[1] instanceof ParameterizedType;

        Class<?> keyType = isKeyParameterized ? (Class<?>) ((ParameterizedType) types[0]).getRawType() : (Class<?>) types[0];
        Class<?> valueType = isValueParameterized ? (Class<?>) ((ParameterizedType) types[1]).getRawType() : (Class<?>) types[1];

        ICollectionsHandler cKeyHandler = HxCConfig.getCollectionsHandler(keyType);
        ICollectionsHandler cValueHandler = HxCConfig.getCollectionsHandler(valueType);

        String categoryName = field.isAnnotationPresent(Config.category.class) ? field.getAnnotation(Config.category.class).value() : "General";
        StringBuilder mapTextBuilder = new StringBuilder();

        mapTextBuilder.append('[');
        for (Map.Entry<Object, Object> entry: tempMap.entrySet()) {
            mapTextBuilder.append('\n').append("\t\t").append(cKeyHandler.writeInCollection(field, entry.getKey(), null, isKeyParameterized ? (ParameterizedType) types[0] : null).stream().reduce((a, b) -> a + "\n\t\t" + b).get()).append('=').append(cValueHandler.writeInCollection(field, entry.getValue(), null,  isKeyParameterized ? (ParameterizedType) types[1] : null).stream().reduce((a, b) -> a + "\n\t\t" + b).get());
        }
        mapTextBuilder.append('\n').append('\t').append(']');

        LinkedHashMap<String, Object> categoryValues = config.getOrDefault(categoryName, new LinkedHashMap<>());
        categoryValues.putIfAbsent(field.getName(), mapTextBuilder.toString());
        config.put(categoryName, categoryValues);


        DataWatcher.put("MapKeyType", keyType);
        DataWatcher.put("MapValueType", valueType);
    }

    private static <K,V> void mainMapReader(String variable, HashMap<String, Object> DataWatcher, BufferedReader reader, Class<?> configClass, Map<K,V> tempMap) throws NoSuchFieldException, ClassNotFoundException, IOException, IllegalAccessException {
        Field field = configClass.getField(variable);
        Class<K> mapKeyType = (Class<K>) DataWatcher.get("MapKeyType");
        Class<V> mapValueType = (Class<V>) DataWatcher.get("MapValueType");

        ICollectionsHandler cKeyHandler = HxCConfig.getCollectionsHandler(mapKeyType);
        ICollectionsHandler cValueHandler = HxCConfig.getCollectionsHandler(mapValueType);

        if (field.isAnnotationPresent(Config.flags.class) && (field.getAnnotation(Config.flags.class).value() & RETAIN_ORIGINAL_VALUES) == RETAIN_ORIGINAL_VALUES) tempMap = (Map<K, V>) field.get(null);

        String line;
        while ((line = reader.readLine()) != null && !line.trim().equals("]")) try { tempMap.put((K) cKeyHandler.readFromCollection(null, line.split("=")[0].trim(), reader), (V) cValueHandler.readFromCollection(null, line.split("=")[1].trim(), reader)); } catch (Exception ignored) {}

        if (field.isAnnotationPresent(Config.flags.class) && (field.getAnnotation(Config.flags.class).value() & OVERWRITE) == OVERWRITE) {
            if (field.get(null) == null || ((Map) field.get(null)).isEmpty()) field.set(configClass, tempMap);
        } else field.set(configClass, tempMap);
    }

    public static class MapHandler implements ICollectionsTypeHandler {

        @Override
        public void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, Object> dataWatcher) throws IllegalAccessException {
            mainMapWriter(field, config, dataWatcher);
            dataWatcher.put("Type", Map.class);
        }

        @Override
        public void read(String variable, HashMap<String, Object> dataWatcher, String currentLine, BufferedReader reader, Class<?> configClass) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
            mainMapReader(variable, dataWatcher, reader, configClass, new HashMap<>());
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[]{Map.class};
        }

        @Override
        public char beginChar() {
            return '[';
        }

        @Override
        public char endChar() {
            return ']';
        }
    }

    public static class HashMapHandler implements ICollectionsTypeHandler {

        @Override
        public void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, Object> dataWatcher) throws IllegalAccessException {
            mainMapWriter(field, config, dataWatcher);
            dataWatcher.put("Type", HashMap.class);
        }

        @Override
        public void read(String variable, HashMap<String, Object> dataWatcher, String currentLine, BufferedReader reader, Class<?> configClass) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
            mainMapReader(variable, dataWatcher, reader, configClass, new HashMap<>());
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[]{HashMap.class};
        }

        @Override
        public char beginChar() {
            return '[';
        }

        @Override
        public char endChar() {
            return ']';
        }
    }

    public static class LinkedHashMapHandler implements ICollectionsTypeHandler {

        @Override
        public void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, Object> dataWatcher) throws IllegalAccessException {
            mainMapWriter(field, config, dataWatcher);
            dataWatcher.put("Type", LinkedHashMap.class);
        }

        @Override
        public void read(String variable, HashMap<String, Object> dataWatcher, String currentLine, BufferedReader reader, Class<?> configClass) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
            mainMapReader(variable, dataWatcher, reader, configClass, new LinkedHashMap<>());
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[]{LinkedHashMap.class};
        }

        @Override
        public char beginChar() {
            return '[';
        }

        @Override
        public char endChar() {
            return ']';
        }
    }
}