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
    @SuppressWarnings("OptionalGetWithoutIsPresent")
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

    @SuppressWarnings("unused")
    private static <T> List mainListCollectionReader(HashMap<String, Object> subDataWatcher, String currentLine, BufferedReader reader, List<T> tempList) throws IOException {
        Class<T> listType = (Class<T>) subDataWatcher.get("Type");
        ICollectionsHandler cHandler = HxCConfig.getCollectionsHandler(listType);

        HashMap<String, Object> subDataWatcherInner = (HashMap<String, Object>) subDataWatcher.getOrDefault("SubDataWatcher", null);

        String line;
        while ((line = reader.readLine()) != null && !line.trim().startsWith("]")) try {
            if (cHandler instanceof ICollectionsTypeHandler && ((ICollectionsTypeHandler) cHandler).beginChar() == line.trim().charAt(0)) {
                tempList.add((T) cHandler.readFromCollection(subDataWatcherInner, line.trim(), reader));
                continue;
            }

            tempList.add((T) cHandler.readFromCollection(subDataWatcherInner, line.trim(), reader));

            reader.mark(1000000);

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

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void mainMapWriter(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, Object> dataWatcher) throws IllegalAccessException {
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

        HashMap<String, Object> subKeyDataWatcher = new HashMap<>();
        HashMap<String, Object> subValueDataWatcher = new HashMap<>();

        mapTextBuilder.append('[');
        boolean firstIteration = true;
        for (Map.Entry<Object, Object> entry: tempMap.entrySet()) {
            mapTextBuilder.append('\n').append("\t\t").append(cKeyHandler.writeInCollection(field, entry.getKey(), firstIteration ? subKeyDataWatcher : null, isKeyParameterized ? (ParameterizedType) types[0] : null).stream().reduce((a, b) -> a + "\n\t\t" + b).get()).append('=').append(cValueHandler.writeInCollection(field, entry.getValue(), firstIteration ? subValueDataWatcher : null,  isValueParameterized ? (ParameterizedType) types[1] : null).stream().reduce((a, b) -> a + "\n\t\t" + b).get());
            firstIteration = false;
        }
        mapTextBuilder.append('\n').append('\t').append(']');

        LinkedHashMap<String, Object> categoryValues = config.getOrDefault(categoryName, new LinkedHashMap<>());
        categoryValues.putIfAbsent(field.getName(), mapTextBuilder.toString());
        config.put(categoryName, categoryValues);

        dataWatcher.put("SubKeyDataWatcher", subKeyDataWatcher);
        dataWatcher.put("SubValueDataWatcher", subValueDataWatcher);
        dataWatcher.put("MapKeyType", keyType);
        dataWatcher.put("MapValueType", valueType);
    }

    private static List<String> mainMapCollectionWriter(Field field, Map<Object, Object> value, HashMap<String, Object> subDataWatcher, ParameterizedType parameterizedType) {
        Type[] types = parameterizedType.getActualTypeArguments();
        boolean isKeyParameterized = (types[0] instanceof ParameterizedType);
        boolean isValueParameterized = (types[1] instanceof ParameterizedType);
        Class<?> keyType = isKeyParameterized ? (Class<?>) ((ParameterizedType) types[0]).getRawType() : (Class<?>) types[0];
        Class<?> valueType = isValueParameterized ? (Class<?>) ((ParameterizedType) types[1]).getRawType() : (Class<?>) types[1];

        ICollectionsHandler cKeyHandler = HxCConfig.getCollectionsHandler(keyType);
        ICollectionsHandler cValueHandler = HxCConfig.getCollectionsHandler(valueType);

        LinkedList<String> lines = new LinkedList<>();

        HashMap<String, Object> subKeyDataWatcher = new HashMap<>();
        HashMap<String, Object> subValueDataWatcher = new HashMap<>();

        lines.add("[");
        boolean firstIteration = true;
        for (Map.Entry<Object, Object> entry : value.entrySet()) {
            LinkedList<String> itKey = new LinkedList<>(cKeyHandler.writeInCollection(field, entry.getKey(), firstIteration ? subKeyDataWatcher : null, isKeyParameterized ? (ParameterizedType) types[0] : null).stream().map(str -> "\t" + str).collect(Collectors.toList()));
            LinkedList<String> itValue = new LinkedList<>(cValueHandler.writeInCollection(field, entry.getValue(), firstIteration ? subValueDataWatcher : null, isValueParameterized ? (ParameterizedType) types[1] : null).stream().map(str -> "\t" + str).collect(Collectors.toList()));
            String keyLast = itKey.getLast();
            String valueFirst = itValue.getFirst();
            itKey.removeLast();
            itValue.removeFirst();

            lines.addAll(itKey);
            lines.add(keyLast + "=" + valueFirst.trim());
            lines.addAll(itValue);

            firstIteration = false;
        }
        lines.add("]");

        if (subDataWatcher != null) {
            subDataWatcher.put("SubKeyDataWatcher", subKeyDataWatcher);
            subDataWatcher.put("SubValueDataWatcher", subValueDataWatcher);
            subDataWatcher.put("MapKeyType", keyType);
            subDataWatcher.put("MapValueType", valueType);
        }

        return lines;
    }

    private static <K,V> void mainMapReader(String variable, HashMap<String, Object> dataWatcher, BufferedReader reader, Class<?> configClass, Map<K,V> tempMap) throws NoSuchFieldException, ClassNotFoundException, IOException, IllegalAccessException {
        Field field = configClass.getField(variable);
        Class<K> mapKeyType = (Class<K>) dataWatcher.get("MapKeyType");
        Class<V> mapValueType = (Class<V>) dataWatcher.get("MapValueType");
        HashMap<String, Object> subKeyDataWatcher = (HashMap<String, Object>) dataWatcher.getOrDefault("SubKeyDataWatcher", null);
        HashMap<String, Object> subValueDataWatcher = (HashMap<String, Object>) dataWatcher.getOrDefault("SubValueDataWatcher", null);

        ICollectionsHandler cKeyHandler = HxCConfig.getCollectionsHandler(mapKeyType);
        ICollectionsHandler cValueHandler = HxCConfig.getCollectionsHandler(mapValueType);

        if (field.isAnnotationPresent(Config.flags.class) && (field.getAnnotation(Config.flags.class).value() & RETAIN_ORIGINAL_VALUES) == RETAIN_ORIGINAL_VALUES) tempMap = (Map<K, V>) field.get(null);

        String line;
        K key = null;
        while ((line = reader.readLine()) != null && !line.trim().equals("]")) try {
            if (key == null) {
                key = (K) cKeyHandler.readFromCollection(subKeyDataWatcher, line.split("=")[0].trim(), reader);

                try {
                    reader.reset();
                    line = reader.readLine();
                    if (line == null) break;
                } catch (IOException ignored) {}
            }

            if (line.contains("=")) {
                tempMap.put(key, (V) cValueHandler.readFromCollection(subValueDataWatcher, line.split("=")[1].trim(), reader));
                key = null;
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        if (field.isAnnotationPresent(Config.flags.class) && (field.getAnnotation(Config.flags.class).value() & OVERWRITE) == OVERWRITE) {
            if (field.get(null) == null || ((Map) field.get(null)).isEmpty()) field.set(configClass, tempMap);
        } else field.set(configClass, tempMap);
    }

    @SuppressWarnings("unused")
    private static <K, V> Map mainMapCollectionReader(HashMap<String, Object> subDataWatcher, String currentLine, BufferedReader reader, Map<K, V> tempMap) throws IOException {
        Class<K> mapKeyType = (Class<K>) subDataWatcher.get("MapKeyType");
        Class<V> mapValueType = (Class<V>) subDataWatcher.get("MapValueType");
        ICollectionsHandler cKeyHandler = HxCConfig.getCollectionsHandler(mapKeyType);
        ICollectionsHandler cValueHandler = HxCConfig.getCollectionsHandler(mapValueType);

        HashMap<String, Object> subKeyDataWatcherInner = (HashMap<String, Object>) subDataWatcher.getOrDefault("SubKeyDataWatcher", null);
        HashMap<String, Object> subValueDataWatcherInner = (HashMap<String, Object>) subDataWatcher.getOrDefault("SubValueDataWatcher", null);

        String line;
        K key = null;
        while ((line = reader.readLine()) != null && !line.trim().startsWith("]")) try {
            if (key == null) key = (K) cKeyHandler.readFromCollection(subKeyDataWatcherInner, line.split("=")[0].trim(), reader);

            if (line.contains("=")) {
                tempMap.put(key, (V) cValueHandler.readFromCollection(subValueDataWatcherInner, line.split("=")[1].trim(), reader));
                key = null;
            }

            reader.mark(1000000);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return tempMap;
    }

    public static class MapHandler implements ICollectionsTypeHandler, ICollectionsHandler {

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
        public List<String> writeInCollection(Field field, Object value, HashMap<String, Object> subDataWatcher, ParameterizedType parameterizedType) {
            return mainMapCollectionWriter(field, (Map) value, subDataWatcher, parameterizedType);
        }

        @Override
        public Object readFromCollection(HashMap<String, Object> subDataWatcher, String currentLine, BufferedReader reader) throws IOException {
            return mainMapCollectionReader(subDataWatcher, currentLine, reader, new HashMap<>());
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

    public static class HashMapHandler implements ICollectionsTypeHandler, ICollectionsHandler {

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
        public List<String> writeInCollection(Field field, Object value, HashMap<String, Object> subDataWatcher, ParameterizedType parameterizedType) {
            return mainMapCollectionWriter(field, (Map) value, subDataWatcher, parameterizedType);
        }

        @Override
        public Object readFromCollection(HashMap<String, Object> subDataWatcher, String currentLine, BufferedReader reader) throws IOException {
            return mainMapCollectionReader(subDataWatcher, currentLine, reader, new HashMap<>());
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

    public static class LinkedHashMapHandler implements ICollectionsTypeHandler, ICollectionsHandler {

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
        public List<String> writeInCollection(Field field, Object value, HashMap<String, Object> subDataWatcher, ParameterizedType parameterizedType) {
            return mainMapCollectionWriter(field, (Map) value, subDataWatcher, parameterizedType);
        }

        @Override
        public Object readFromCollection(HashMap<String, Object> subDataWatcher, String currentLine, BufferedReader reader) throws IOException {
            return mainMapCollectionReader(subDataWatcher, currentLine, reader, new LinkedHashMap<>());
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