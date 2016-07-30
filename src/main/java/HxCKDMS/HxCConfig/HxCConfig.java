package HxCKDMS.HxCConfig;

import HxCKDMS.HxCConfig.Exceptions.InvalidConfigClassException;
import HxCKDMS.HxCConfig.Handlers.AdvancedHandlers;
import HxCKDMS.HxCConfig.Handlers.BasicHandlers;
import HxCKDMS.HxCConfig.Handlers.ICollectionsHandler;
import HxCKDMS.HxCConfig.Handlers.ITypeHandler;
import HxCKDMS.HxCUtils.LogHelper;
import HxCKDMS.HxCUtils.StringHelper;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static HxCKDMS.HxCConfig.Flags.collectionHandler;
import static HxCKDMS.HxCConfig.Flags.typeHandler;

@SuppressWarnings({"WeakerAccess", "unused"})
public class HxCConfig {
    private Class<?> configClass;
    private HashMap<String, HashMap<String, HashMap<String, String>>> configDataWatcherTest = new HashMap<>();
    private File configFile, dataWatcherFile, configDirectory, dataWatcherDirectory;
    private LinkedHashMap<String, LinkedHashMap<String, Object>> configWritingData = new LinkedHashMap<>();
    private static HashMap<Class<?>, ITypeHandler> TypeHandlers = new HashMap<>();
    private static HashMap<Class<?>, ICollectionsHandler> CollectionsHandlers = new HashMap<>();
    private HashMap<String, String> CategoryComments = new HashMap<>();
    private HashMap<String, HashMap<String, String>> valueComments = new HashMap<>();
    private String app_name;

    static {
        //Basic types
        registerHandler(new BasicHandlers.StringHandler(), typeHandler | collectionHandler);
        registerHandler(new BasicHandlers.IntegerHandler(), typeHandler | collectionHandler);
        registerHandler(new BasicHandlers.DoubleHandler(), typeHandler | collectionHandler);
        registerHandler(new BasicHandlers.CharacterHandler(), typeHandler | collectionHandler);
        registerHandler(new BasicHandlers.FloatHandler(), typeHandler | collectionHandler);
        registerHandler(new BasicHandlers.LongHandler(), typeHandler | collectionHandler);
        registerHandler(new BasicHandlers.ShortHandler(), typeHandler | collectionHandler);
        registerHandler(new BasicHandlers.ByteHandler(), typeHandler | collectionHandler);
        registerHandler(new BasicHandlers.BooleanHandler(), typeHandler | collectionHandler);

        //Lists
        registerHandler(new AdvancedHandlers.ListHandler(), typeHandler);
        registerHandler(new AdvancedHandlers.ArrayListHandler(), typeHandler);
        registerHandler(new AdvancedHandlers.LinkedListHandler(), typeHandler);

        //Maps
        registerHandler(new AdvancedHandlers.MapHandler(), typeHandler);
        registerHandler(new AdvancedHandlers.HashMapHandler(), typeHandler);
        registerHandler(new AdvancedHandlers.LinkedHashMapHandler(), typeHandler);
    }

    @Deprecated
    public static void registerTypeHandler(ITypeHandler handler) {
        registerHandler(handler, typeHandler);
    }

    public static void registerHandler(Object handler, int flag) {
        if ((flag & Flags.typeHandler) == Flags.typeHandler) Arrays.stream(((ITypeHandler)handler).getTypes()).forEach(clazz -> TypeHandlers.putIfAbsent(clazz, (ITypeHandler) handler));
        if ((flag & Flags.collectionHandler) == Flags.collectionHandler) Arrays.stream(((ICollectionsHandler)handler).getTypes()).forEach(clazz -> CollectionsHandlers.putIfAbsent(clazz, (ICollectionsHandler) handler));
    }

    public static ICollectionsHandler getCollectionsHandler(Class<?> type) {
        return CollectionsHandlers.get(type);
    }

    public void setCategoryComment(String category, String comment) {
        CategoryComments.put(category, comment);
    }

    public HxCConfig(Class<?> clazz, String configName, File configDirectory, String extension, String app_name) {
        this.configClass = clazz;
        this.configFile = new File(configDirectory, configName + "." + extension);
        this.configDirectory = configDirectory;
        this.dataWatcherDirectory = new File(configDirectory + "/.datawatcher/");
        this.dataWatcherFile = new File(dataWatcherDirectory, configName + ".dat");
        this.app_name = app_name;

        setCategoryComment("Default", "This is the default category.");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public final void initConfiguration() {
        configWritingData.clear();

        try {
            configDirectory.mkdirs();
            if (!configFile.exists()) configFile.createNewFile();
            dataWatcherDirectory.mkdirs();
            if (!dataWatcherFile.exists()) dataWatcherFile.createNewFile();

            Path path = dataWatcherDirectory.toPath();
            Files.setAttribute(path, "dos:hidden", true);
            deSerialize();

            read();
            write();

            serialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void deSerialize() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(dataWatcherFile));
            configDataWatcherTest = (HashMap<String, HashMap<String, HashMap<String, String>>>) inputStream.readObject();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void read() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), "UTF-8"));
        String line;
        String category = "";
        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("#")) continue;
            boolean firstIteration = true;
            line = line.replaceFirst("    ", "\t");
            char[] characters = line.toCharArray();
            StringBuilder nameBuilder = new StringBuilder();

            if (line.endsWith("{")) {
                StringBuilder categoryBuilder = new StringBuilder();
                for (int i = 0; i < characters.length; i++) {
                    if (i == 0) continue;
                    if (characters[i] == '{' && characters[i - 1] == ' ') break;
                    categoryBuilder.append(characters[i - 1]);
                }

                if (categoryBuilder.length() != 0) category = categoryBuilder.toString();
            }

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
                Class<?> type = Class.forName(configDataWatcherTest.get(category).get(variableName).get("Type"));

                TypeHandlers.get(type).read(variableName, configDataWatcherTest.get(category).get(variableName), line, reader, configClass, false);
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        reader.close();
    }

    private void serialize() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(dataWatcherFile));
            outputStream.writeObject(configDataWatcherTest);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

            if (CategoryComments.containsKey(entry.getKey())) {
                stringBuilder.append('#').append(' ').append(CategoryComments.get(entry.getKey())).append('\n');
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
        if (TypeHandlers.containsKey(field.getType())) try {
            HashMap<String, String> data = new HashMap<>();
            String categoryName = field.isAnnotationPresent(Config.category.class) ? field.getAnnotation(Config.category.class).value() : "General";
            HashMap<String, HashMap<String, String>> category = configDataWatcherTest.getOrDefault(categoryName, new HashMap<>());

            TypeHandlers.get(field.getType()).write(field, configWritingData, data, false);

            category.put(field.getName(), data);
            configDataWatcherTest.put(categoryName, category);

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
}