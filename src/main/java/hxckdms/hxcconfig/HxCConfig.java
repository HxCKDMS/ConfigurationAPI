package hxckdms.hxcconfig;

import hxckdms.hxcconfig.exceptions.InvalidConfigClassException;
import hxckdms.hxcconfig.handlers.CollectionsHandlers;
import hxckdms.hxcconfig.handlers.IConfigurationHandler;
import hxckdms.hxcconfig.handlers.PrimaryHandlers;
import hxckdms.hxcconfig.handlers.SpecialHandlers;
import hxckdms.hxcutils.LogHelper;
import hxckdms.hxcutils.StringHelper;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static hxckdms.hxcconfig.Flags.OVERWRITE;

@SuppressWarnings({"WeakerAccess", "unused"})
public class HxCConfig {
    private Class<?> configClass;
    private File configFile, configDirectory;
    private LinkedHashMap<String, LinkedHashMap<String, Object>> configWritingData = new LinkedHashMap<>();
    private LinkedHashSet<IConfigurationHandler> typeHandlers = new LinkedHashSet<>();
    private HashMap<String, String> categoryComments = new HashMap<>();
    private HashMap<String, HashMap<String, String>> valueComments = new HashMap<>();
    private String app_name;

    private LinkedList<String> lines;
    private int currentLine = -1;

    private void registerDefaultHandlers() {
        //Basic types
        registerHandler(new PrimaryHandlers.StringHandler());
        registerHandler(new PrimaryHandlers.IntegerHandler());
        registerHandler(new PrimaryHandlers.DoubleHandler());
        registerHandler(new PrimaryHandlers.CharacterHandler());
        registerHandler(new PrimaryHandlers.FloatHandler());
        registerHandler(new PrimaryHandlers.LongHandler());
        registerHandler(new PrimaryHandlers.ShortHandler());
        registerHandler(new PrimaryHandlers.ByteHandler());
        registerHandler(new PrimaryHandlers.BooleanHandler());

        //Lists
        registerHandler(new CollectionsHandlers.ListHandler());
        registerHandler(new CollectionsHandlers.ArrayListHandler());
        registerHandler(new CollectionsHandlers.LinkedListHandler());

        //Maps
        registerHandler(new CollectionsHandlers.MapHandler());
        registerHandler(new CollectionsHandlers.HashMapHandler());
        registerHandler(new CollectionsHandlers.LinkedHashMapHandler());

        //Special
        registerHandler(new SpecialHandlers.SpecialClassHandler());
    }

    public void registerHandler(IConfigurationHandler handler) {
        typeHandlers.add(handler);
    }

    public IConfigurationHandler getConfigurationTypeHandler(Class<?> type) {
        return typeHandlers.parallelStream().filter(typeHandler -> typeHandler.isTypeAccepted(type)).findFirst().orElseThrow(() -> new NullPointerException(String.format("No configuration handler for type: %s exists.", type.getCanonicalName())));
    }

    public void setCategoryComment(String category, String comment) {
        categoryComments.put(category, comment);
    }

    public HxCConfig(Class<?> clazz, String configName, File configDirectory, String extension, String app_name) {
        this.configClass = clazz;
        this.configFile = new File(configDirectory, configName + "." + extension);
        this.configDirectory = configDirectory;
        this.app_name = app_name;

        registerDefaultHandlers();
        setCategoryComment("Default", "This is the default category.");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public final void initConfiguration() {
        configWritingData.clear();
        currentLine = -1;

        try {
            configDirectory.mkdirs();
            if (!configFile.exists()) configFile.createNewFile();

            read();
            write();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This writes out without reading for situations I needed that
     *   ~ KeldonSlayer (DrZed)
     **/
    public final void saveConfiguration() {
        configWritingData.clear();
        currentLine = -1;

        try {
            write();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LinkedList<String> getLines() {
        return (LinkedList<String>) Collections.unmodifiableList(lines);
    }

    public String getPreviousLine(boolean decrement) {
        if ((currentLine - 1) < 0) return null;
        if (decrement) --currentLine;

        return lines.get(currentLine);
    }

    public String getCurrentLine() {
        return lines.get(currentLine);
    }

    public String getNextLine(boolean increment) {
        if ((currentLine + 1) > (lines.size() - 1)) return null;
        if (increment) ++currentLine;
        return lines.get(currentLine);
    }

    public int getCurrentLineNumber() {
        return currentLine + 1;
    }

    private void read() throws IOException {
        lines = new LinkedList<>(Files.readAllLines(Paths.get(configFile.toURI()), Charset.defaultCharset()));

        String line;
        //String category = "";
        while ((line = getNextLine(true)) != null) {
            if (line.trim().startsWith("#")) continue;
            boolean firstIteration = true;
            line = line.replaceFirst("    ", "\t");
            char[] characters = line.toCharArray();
            StringBuilder nameBuilder = new StringBuilder();

            /*if (line.endsWith("{")) {
                StringBuilder categoryBuilder = new StringBuilder();
                for (int i = 0; i < characters.length; i++) {
                    if (i == 0) continue;
                    if (characters[i] == '{' && characters[i - 1] == ' ') break;
                    categoryBuilder.append(characters[i - 1]);
                }

                if (categoryBuilder.length() != 0) category = categoryBuilder.toString();
            }*/

            for (char character : characters) {
                if (firstIteration && character == '\t') {
                    firstIteration = false;
                    continue;
                } else if (firstIteration) break;
                if (character == '\t' || character == '=' || character == ']') break;
                nameBuilder.append(character);
                firstIteration = false;
            }

            if (nameBuilder.length() == 0) continue;
            String variableName = nameBuilder.toString();

            try {
                Field field = HxCConfig.getField(configClass, variableName);

                boolean isParameterized = (field.getGenericType() instanceof ParameterizedType);
                HashMap<String, Object> info = new HashMap<>();
                info.put("field", field);

                if (isParameterized) info.put("Type", field.getGenericType());
                else info.put("Type", field.getType());

                Object value = getConfigurationTypeHandler(field.getType()).read(getCurrentLine().trim().replace(variableName + "=", ""), this, info);

                if (field.isAnnotationPresent(Config.flags.class) && (field.getAnnotation(Config.flags.class).value() & OVERWRITE) == OVERWRITE) {
                    if (field.get(configClass) == null || ((Map) field.get(null)).isEmpty()) field.set(configClass, value);
                } else field.set(configClass, value);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        lines.clear();
    }

    private void write() throws IOException {
        if (!configClass.isAnnotationPresent(Config.class)) throw new InvalidConfigClassException(configClass.getCanonicalName(), "Class doesn't have @Config annotation.");
        Arrays.stream(configClass.getDeclaredFields()).filter(field -> !field.isAnnotationPresent(Config.ignore.class)).forEachOrdered(this::handleFieldWriting);
        Arrays.stream(configClass.getDeclaredClasses()).filter(clazz -> !clazz.isAnnotationPresent(Config.ignore.class)).forEachOrdered(this::handleClass);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), "UTF-8"));

        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, LinkedHashMap<String, Object>> entry : configWritingData.entrySet()) {
            stringBuilder.append(StringHelper.repeat('#', 106)).append('\n');
            stringBuilder.append('#').append(' ').append(entry.getKey()).append('\n');
            stringBuilder.append(StringHelper.repeat('#', 106)).append('\n');

            if (categoryComments.containsKey(entry.getKey())) {
                stringBuilder.append('#').append(' ').append(categoryComments.get(entry.getKey())).append('\n');
                stringBuilder.append(StringHelper.repeat('#', 106)).append("\n");
            }
            stringBuilder.append('\n');

            stringBuilder.append(entry.getKey()).append(" {\n");

            boolean first = true;
            for (Map.Entry<String, Object> entry2 : entry.getValue().entrySet()) {
                if (!first && hasComment(entry.getKey(), entry2.getKey())) stringBuilder.append('\n');

                if (hasComment(entry.getKey(), entry2.getKey())) stringBuilder.append('\t').append('#').append(' ').append(getComment(entry.getKey(), entry2.getKey())).append('\n');
                stringBuilder.append('\t').append(entry2.getKey()).append('=').append(entry2.getValue()).append('\n');
                first = false;
            }
            stringBuilder.append("}\n\n");
        }

        writer.write(stringBuilder.toString().trim());
        writer.close();
    }

    private void handleClass(Class clazz) {

    }

    private void handleFieldWriting(Field field) {
        if (typeHandlers.parallelStream().anyMatch(typeHandler -> typeHandler.isTypeAccepted(field.getType()))) try {

            setPublicStatic(field);
            if (!Modifier.isPublic(field.getModifiers())) return;

            String categoryName = field.isAnnotationPresent(Config.category.class) ? field.getAnnotation(Config.category.class).value() : "General";

            boolean isParameterized = field.getGenericType() instanceof ParameterizedType;
            Class<?> type = (Class<?>) (isParameterized ? ((ParameterizedType) field.getGenericType()).getRawType() : field.getGenericType());
            List<String> value = getConfigurationTypeHandler(type).write(field, field.get(null), isParameterized ? (ParameterizedType) field.getGenericType() : null, this);

            LinkedHashMap<String, Object> categoryValues = configWritingData.getOrDefault(categoryName, new LinkedHashMap<>());
            categoryValues.putIfAbsent(field.getName(), value.stream().reduce((a, b) -> a + "\n\t" + b).orElse(""));
            configWritingData.put(categoryName, categoryValues);

            HashMap<String, String> comment = valueComments.getOrDefault(categoryName, new HashMap<>());
            if(field.isAnnotationPresent(Config.comment.class)) comment.put(field.getName(), field.getAnnotation(Config.comment.class).value());
            valueComments.put(categoryName, comment);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        else LogHelper.severe(String.format("Configuration type: %1$s is unsupported!", field.getType().getCanonicalName()), app_name);
    }

    //helper methods

    private boolean hasComment(String category, String variable) {
        return valueComments.containsKey(category) && valueComments.get(category).containsKey(variable);
    }

    private String getComment(String category, String variable) {
        return valueComments.get(category).get(variable);
    }

    public static Field getField(Class<?> clazz, String variable) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(variable);
        setPublicStatic(field);
        return field;
    }

    public static void setPublicStatic(Field field) {
        if (field.isAnnotationPresent(Config.force.class)) try {
            Field modField = Field.class.getDeclaredField("modifiers");
            modField.setAccessible(true);
            modField.setInt(field, field.getModifiers() & ~Modifier.PRIVATE);
            modField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            modField.setInt(field, field.getModifiers() | Modifier.PUBLIC);
            modField.setInt(field, field.getModifiers() | Modifier.STATIC);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
    }
}