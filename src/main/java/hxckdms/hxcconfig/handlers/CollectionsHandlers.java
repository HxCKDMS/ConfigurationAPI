package hxckdms.hxcconfig.handlers;

import hxckdms.hxcconfig.Config;
import hxckdms.hxcconfig.HxCConfig;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static hxckdms.hxcconfig.Flags.RETAIN_ORIGINAL_VALUES;

@SuppressWarnings("unchecked")
public class CollectionsHandlers {

    //LIST STUFF
    private static List<String> mainListCollectionWriter(Field field, List<Object> value, ParameterizedType parameterizedType, HxCConfig HxCConfigClass) {
        Type[] types = parameterizedType.getActualTypeArguments();
        boolean isParameterized = (types[0] instanceof ParameterizedType);
        Class<?> type = isParameterized ? (Class<?>) ((ParameterizedType) types[0]).getRawType() : (Class<?>) types[0];

        IConfigurationHandler cHandler = HxCConfigClass.getConfigurationTypeHandler(type);

        LinkedList<String> lines = new LinkedList<>();

        lines.add("[");

        for (Object obj : value) {
            lines.addAll(cHandler.write(field, obj, isParameterized ? (ParameterizedType) types[0] : null, HxCConfigClass).stream().map(str -> "\t" + str).collect(Collectors.toList()));
        }
        lines.add("]");

        return lines;
    }

    private static <T> List mainListCollectionReader(Map<String, Object> info, List<T> tempList, HxCConfig mainInstance) throws IOException {
        Type[] types = ((ParameterizedType) info.get("Type")).getActualTypeArguments();
        boolean isParameterized = (types[0] instanceof ParameterizedType);
        Class<T> listType = isParameterized ? (Class<T>) ((ParameterizedType) types[0]).getRawType() : (Class<T>) types[0];

        IConfigurationHandler cHandler = mainInstance.getConfigurationTypeHandler(listType);

        Map<String, Object> innerInfo = new HashMap<>();
        innerInfo.clear();
        innerInfo.put("Type", types[0]);

        String line;
        while ((line = mainInstance.getNextLine(true)) != null && !line.trim().startsWith("]")) try {
            if (cHandler instanceof IMultiLineHandler && ((IMultiLineHandler) cHandler).beginChar() == line.trim().charAt(0)) {
                tempList.add((T) cHandler.read(line.trim(), mainInstance, innerInfo));
                continue;
            }

            tempList.add((T) cHandler.read(line.trim(), mainInstance, innerInfo));

        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return tempList;
    }

    public static class ListHandler implements IMultiLineHandler, IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return mainListCollectionWriter(field, (List) value, parameterizedType, mainInstance);
        }

        @Override
        public List read(String value, HxCConfig mainInstance, Map<String, Object> info) throws IOException {
            return mainListCollectionReader(info, new ArrayList<>(), mainInstance);
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

    public static class LinkedListHandler implements IMultiLineHandler, IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return mainListCollectionWriter(field, (List) value, parameterizedType, mainInstance);
        }

        @Override
        public LinkedList read(String value, HxCConfig mainInstance, Map<String, Object> info) throws IOException {
            return (LinkedList) mainListCollectionReader(info, new LinkedList<>(), mainInstance);
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

    public static class ArrayListHandler implements IMultiLineHandler, IConfigurationHandler {


        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return mainListCollectionWriter(field, (List) value, parameterizedType, mainInstance);
        }

        @Override
        public ArrayList read(String value, HxCConfig mainInstance, Map<String, Object> info) throws IOException {
            return (ArrayList) mainListCollectionReader(info, new ArrayList<>(), mainInstance);
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

    private static List<String> mainMapWriter(Field field, Map<Object, Object> value, ParameterizedType parameterizedType, HxCConfig HxCConfigClass) {
        Type[] types = parameterizedType.getActualTypeArguments();
        boolean isKeyParameterized = (types[0] instanceof ParameterizedType);
        boolean isValueParameterized = (types[1] instanceof ParameterizedType);
        Class<?> keyType = isKeyParameterized ? (Class<?>) ((ParameterizedType) types[0]).getRawType() : (Class<?>) types[0];
        Class<?> valueType = isValueParameterized ? (Class<?>) ((ParameterizedType) types[1]).getRawType() : (Class<?>) types[1];

        IConfigurationHandler cKeyHandler = HxCConfigClass.getConfigurationTypeHandler(keyType);
        IConfigurationHandler cValueHandler = HxCConfigClass.getConfigurationTypeHandler(valueType);

        LinkedList<String> lines = new LinkedList<>();

        lines.add("[");
        for (Map.Entry<Object, Object> entry : value.entrySet()) {
            LinkedList<String> itKey = new LinkedList<>(cKeyHandler.write(field, entry.getKey(), isKeyParameterized ? (ParameterizedType) types[0] : null, HxCConfigClass).stream().map(str -> "\t" + str).collect(Collectors.toList()));
            LinkedList<String> itValue = new LinkedList<>(cValueHandler.write(field, entry.getValue(), isValueParameterized ? (ParameterizedType) types[1] : null, HxCConfigClass).stream().map(str -> "\t" + str).collect(Collectors.toList()));
            String keyLast = itKey.removeLast();
            String valueFirst = itValue.removeFirst();

            lines.addAll(itKey);
            lines.add(keyLast + "=" + valueFirst.trim());
            lines.addAll(itValue);
        }
        lines.add("]");

        return lines;
    }

    private static <K, V> Map mainMapReader(Map<String, Object> info, Map<K, V> tempMap, HxCConfig mainInstance) throws IOException {
        if (info.containsKey("field") && ((Field) info.get("field")).isAnnotationPresent(Config.flags.class) && (((Field) info.get("field")).getAnnotation(Config.flags.class).value() & RETAIN_ORIGINAL_VALUES) == RETAIN_ORIGINAL_VALUES) try {
            tempMap = (Map<K, V>) ((Field) info.get("field")).get(null);
        } catch (IllegalAccessException ignored) {}

        Type[] types = ((ParameterizedType) info.get("Type")).getActualTypeArguments();

        boolean isKeyParameterized = types[0] instanceof ParameterizedType;
        boolean isValueParameterized = types[1] instanceof ParameterizedType;

        Class<K> mapKeyType = isKeyParameterized ? (Class<K>) ((ParameterizedType) types[0]).getRawType() : (Class<K>) types[0];
        Class<V> mapValueType = isValueParameterized ? (Class<V>) ((ParameterizedType) types[1]).getRawType() : (Class<V>) types[1];

        IConfigurationHandler cKeyHandler = mainInstance.getConfigurationTypeHandler(mapKeyType);
        IConfigurationHandler cValueHandler = mainInstance.getConfigurationTypeHandler(mapValueType);

        Map<String, Object> keyInnerInfo = new HashMap<>();
        keyInnerInfo.put("Type", types[0]);
        Map<String, Object> valueInnerInfo = new HashMap<>();
        valueInnerInfo.put("Type", types[1]);

        String line;
        K key = null;
        while ((line = mainInstance.getNextLine(true)) != null && !line.trim().startsWith("]")) try {
            if (key == null) key = (K) cKeyHandler.read(line.split("=")[0].trim(), mainInstance, keyInnerInfo);

            if (mainInstance.getPreviousLine(false).contains("=")) {
                tempMap.put(key, (V) cValueHandler.read(mainInstance.getPreviousLine(false).split("=")[1].trim(), mainInstance, valueInnerInfo));
                key = null;
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return tempMap;
    }

    public static class MapHandler implements IMultiLineHandler, IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return mainMapWriter(field, (Map) value, parameterizedType, mainInstance);
        }

        @Override
        public Object read(String value, HxCConfig mainInstance, Map<String, Object> info) throws IOException {
            return mainMapReader(info, new HashMap<>(), mainInstance);
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

    public static class HashMapHandler implements IMultiLineHandler, IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return mainMapWriter(field, (Map) value, parameterizedType, mainInstance);
        }

        @Override
        public Object read(String value, HxCConfig mainInstance, Map<String, Object> info) throws IOException {
            return mainMapReader(info, new HashMap<>(), mainInstance);
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

    public static class LinkedHashMapHandler implements IMultiLineHandler, IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return mainMapWriter(field, (Map) value, parameterizedType, mainInstance);
        }

        @Override
        public Object read(String value, HxCConfig mainInstance, Map<String, Object> info) throws IOException {
            return mainMapReader(info, new LinkedHashMap<>(), mainInstance);
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