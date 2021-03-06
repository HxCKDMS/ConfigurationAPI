package hxckdms.hxcconfig.handlers;

import hxckdms.hxcconfig.HxCConfig;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

public interface IConfigurationHandler {
    List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance);
    Object read(String value, HxCConfig mainInstance, Map<String, Object> info) throws IOException;

    boolean isTypeAccepted(Class<?> type);
}
