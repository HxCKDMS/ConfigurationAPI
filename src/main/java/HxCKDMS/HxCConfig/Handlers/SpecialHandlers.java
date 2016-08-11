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

public class SpecialHandlers {
    private static List<Class> classes = new ArrayList<>();

    public static void registerSpecialClass(Class clazz) {
        classes.add(clazz);
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
    public static class specialClassHandler implements ITypeHandler, IMultiLineHandler, ICollectionsHandler {

        @Override
        public void write(Field field, LinkedHashMap<String, LinkedHashMap<String, Object>> config, HashMap<String, Object> dataWatcher) throws IllegalAccessException {
            List<Field> fields = Arrays.asList(field.get(null).getClass().getDeclaredFields());
            String categoryName = field.isAnnotationPresent(Config.category.class) ? field.getAnnotation(Config.category.class).value() : "General";
            StringBuilder classTextBuilder = new StringBuilder();

            classTextBuilder.append('[');
            for (Field aField : fields) {
                HashMap<String, Object> subDataWatcher = new HashMap<>();
                Object value = aField.get(field.get(null));
                String fName = aField.getName();

                Type type = aField.getGenericType();
                boolean isParameterized = (type instanceof ParameterizedType);
                Class<?> cType = isParameterized ? (Class<?>) ((ParameterizedType) type).getRawType() : (Class<?>) type;
                ICollectionsHandler cHandler = HxCConfig.getCollectionsHandler(cType);

                classTextBuilder.append("\n\t\t").append(fName).append('=').append(cHandler.writeInCollection(aField, value, subDataWatcher, isParameterized ? (ParameterizedType) type : null).stream().map(str -> "\t\t" + str).reduce((a, b) -> a + '\n' + b).get().trim());

                subDataWatcher.put("Type", cType);
                dataWatcher.put("SubDataWatcher_" + fName, subDataWatcher);
            }
            classTextBuilder.append("\n\t]");
            LinkedHashMap<String, Object> categoryValues = config.getOrDefault(categoryName, new LinkedHashMap<>());
            categoryValues.putIfAbsent(field.getName(), classTextBuilder.toString());
            config.put(categoryName, categoryValues);

            dataWatcher.put("Type", field.get(null).getClass());
        }

        @Override
        public void read(String variable, HashMap<String, Object> dataWatcher, String currentLine, BufferedReader reader, Class<?> configClass) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IOException {
            Field field = HxCConfig.getField(configClass, variable);
            if (field.isAnnotationPresent(Config.flags.class) && (field.getAnnotation(Config.flags.class).value() & OVERWRITE) == OVERWRITE && field.get(null) != null) return;

            String line;
            String fName = "";
            while ((line = reader.readLine()) != null && !line.trim().startsWith("]")) try {
                if (fName.isEmpty()) fName = line.split("=")[0].trim();

                if (line.contains("=") && !fName.isEmpty()) {

                    Field aField = HxCConfig.getField(field.get(null).getClass(), fName);
                    HashMap<String, Object> subDataWatcher = (HashMap<String, Object>) dataWatcher.getOrDefault("SubDataWatcher_" + fName, null);
                    ICollectionsHandler cHandler = HxCConfig.getCollectionsHandler((Class<?>) subDataWatcher.get("Type"));
                    aField.set(field.get(null), cHandler.readFromCollection(subDataWatcher, line.split("=")[1].trim(), reader));

                    fName = "";
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }

        @Override
        public List<String> writeInCollection(Field field, Object value, HashMap<String, Object> subDataWatcher, ParameterizedType parameterizedType) {
            List<Field> fields = Arrays.asList(value.getClass().getDeclaredFields());
            LinkedList<String> lines = new LinkedList<>();

            lines.add("[");

            for (Field aField : fields) try {
                HashMap<String, Object> subDataWatcherInner = new HashMap<>();
                Object fValue = aField.get(value);
                String fName = aField.getName();

                Type type = aField.getGenericType();
                boolean isParameterized = (type instanceof ParameterizedType);
                Class<?> cType = isParameterized ? (Class<?>) ((ParameterizedType) type).getRawType() : (Class<?>) type;
                ICollectionsHandler cHandler = HxCConfig.getCollectionsHandler(cType);

                LinkedList<String> itValue = new LinkedList<>(cHandler.writeInCollection(field, fValue, subDataWatcherInner, isParameterized ? (ParameterizedType) type : null).stream().map(str -> "\t" +str).collect(Collectors.toList()));
                String valueFirst = itValue.getFirst();
                itValue.removeFirst();

                lines.add('\t' + fName + "=" + valueFirst.trim());
                lines.addAll(itValue);


                subDataWatcherInner.put("Type", cType);

                if (subDataWatcher != null) subDataWatcher.put("SubDataWatcher_" + fName, subDataWatcherInner);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            lines.add("]");

            if (subDataWatcher != null) subDataWatcher.put("Type", value.getClass());

            return lines;
        }

        @Override
        public Object readFromCollection(HashMap<String, Object> subDataWatcher, String currentLine, BufferedReader reader) throws IOException {
            Class<?> type = (Class<?>) subDataWatcher.get("Type");
            Object instance;
            try {
                instance = type.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }

            String line;
            String fName = "";

            while ((line = reader.readLine()) != null && !line.trim().equals("]")) try {
                if (fName.isEmpty()) fName = line.split("=")[0].trim();

                if (line.contains("=") && !fName.isEmpty()) {
                    Field field = HxCConfig.getField(type, fName);

                    HashMap<String, Object> subDataWatcherInner = (HashMap<String, Object>) subDataWatcher.getOrDefault("SubDataWatcher_" + fName, null);
                    ICollectionsHandler cHandler = HxCConfig.getCollectionsHandler((Class<?>) subDataWatcherInner.get("Type"));

                    field.set(instance, cHandler.readFromCollection(subDataWatcherInner, line.split("=")[1].trim(), reader));

                    fName = "";
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }

            return instance;
        }

        @Override
        public Class<?>[] getTypes() {
            Class<?>[] tmp = new Class<?>[classes.size()];
            return classes.toArray(tmp);
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
