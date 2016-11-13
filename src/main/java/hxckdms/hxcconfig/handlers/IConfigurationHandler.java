package hxckdms.hxcconfig.handlers;

import hxckdms.hxcconfig.HxCConfig;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

public interface IConfigurationHandler {
    List<String> writeInCollection(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance);
    Object readFromCollection(String value, HxCConfig mainInstance, Map<String, Object> info) throws IOException;

    Class<?>[] getTypes();
}
