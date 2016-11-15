package hxckdms.hxcconfig.handlers;

import hxckdms.hxcconfig.HxCConfig;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class PrimaryHandlers {
    public static class StringHandler implements IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public String read(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return value;
        }

        @Override
        public boolean isTypeAccepted(Class<?> type) {
            return type == String.class;
        }
    }

    public static class IntegerHandler implements IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Integer read(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Integer.parseInt(value);
        }

        @Override
        public boolean isTypeAccepted(Class<?> type) {
            return type == Integer.class || type == Integer.TYPE;
        }
    }

    public static class DoubleHandler implements IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Double read(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Double.parseDouble(value);
        }

        @Override
        public boolean isTypeAccepted(Class<?> type) {
            return type == Double.class || type == Double.TYPE;
        }
    }

    public static class CharacterHandler implements IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Character read(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return value.charAt(0);
        }

        @Override
        public boolean isTypeAccepted(Class<?> type) {
            return type == Character.class || type == Character.TYPE;
        }
    }

    public static class BooleanHandler implements IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Boolean read(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Boolean.valueOf(value);
        }

        @Override
        public boolean isTypeAccepted(Class<?> type) {
            return type == Boolean.class || type == Boolean.TYPE;
        }
    }

    public static class FloatHandler implements IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Float read(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Float.valueOf(value);
        }

        @Override
        public boolean isTypeAccepted(Class<?> type) {
            return type == Float.class || type == Float.TYPE;
        }
    }

    public static class ShortHandler implements IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Short read(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Short.valueOf(value);
        }

        @Override
        public boolean isTypeAccepted(Class<?> type) {
            return type == Short.class || type == Short.TYPE;
        }
    }

    public static class LongHandler implements IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Long read(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Long.valueOf(value);
        }

        @Override
        public boolean isTypeAccepted(Class<?> type) {
            return type == Long.class || type == Long.TYPE;
        }
    }

    public static class ByteHandler implements IConfigurationHandler {

        @Override
        public List<String> write(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Byte read(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Byte.valueOf(value);
        }

        @Override
        public boolean isTypeAccepted(Class<?> type) {
            return type == Byte.class || type == Byte.TYPE;
        }
    }
}