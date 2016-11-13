package hxckdms.hxcconfig.handlers;

import hxckdms.hxcconfig.HxCConfig;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class SpecialHandlers {
    private static List<Class> classes = new ArrayList<>();

    public static void registerSpecialClass(Class clazz) {
        classes.add(clazz);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static class SpecialClassHandler implements IMultiLineHandler, IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            List<Field> fields = Arrays.asList(value.getClass().getDeclaredFields());
            LinkedList<String> lines = new LinkedList<>();

            lines.add("[");

            for (Field aField : fields) try {
                HxCConfig.setPublicStatic(aField);
                if (!Modifier.isPublic(aField.getModifiers())) continue;

                Object fValue = aField.get(value);
                String fName = aField.getName();

                Type type = aField.getGenericType();
                boolean isParameterized = (type instanceof ParameterizedType);
                Class<?> cType = isParameterized ? (Class<?>) ((ParameterizedType) type).getRawType() : (Class<?>) type;
                IConfigurationHandler cHandler = mainInstance.getConfigurationTypeHandler(cType);

                LinkedList<String> itValue = new LinkedList<>(cHandler.write(field, fValue, isParameterized ? (ParameterizedType) type : null, mainInstance).stream().map(str -> "\t" +str).collect(Collectors.toList()));
                String valueFirst = itValue.getFirst();
                itValue.removeFirst();

                lines.add('\t' + fName + "=" + valueFirst.trim());
                lines.addAll(itValue);

            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            lines.add("]");

            return lines;
        }

        @Override
        public Object read(String value, HxCConfig mainInstance, Map<String, Object> info) throws IOException {
            Class<?> type = (Class<?>) info.get("Type");
            Object instance;
            try {
                instance = type.newInstance();
            } catch (Exception ignored) {
                ignored.printStackTrace();
                return null;
            }

            String line;
            String fName = "";

            while ((line = mainInstance.getNextLine(true)) != null && !line.trim().equals("]")) try {
                if (fName.isEmpty()) fName = line.split("=")[0].trim();

                if (line.contains("=") && !fName.isEmpty()) {
                    Field field = HxCConfig.getField(type, fName);

                    Map<String, Object> innerInfo = new HashMap<>();
                    innerInfo.put("Type", field.getGenericType());

                    IConfigurationHandler cHandler = mainInstance.getConfigurationTypeHandler(field.getType());

                    field.set(instance, cHandler.read(line.split("=")[1].trim(), mainInstance, innerInfo));

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
