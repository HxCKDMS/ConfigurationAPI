package hxckdms.hxcconfig.handlers;

import hxckdms.hxcconfig.HxCConfig;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class PrimaryHandlers {
    public static class StringHandler implements IConfigurationHandler {

        @Override
        public List<String> writeInCollection(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public String readFromCollection(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return value;
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[] {String.class};
        }
    }

    public static class IntegerHandler implements IConfigurationHandler {

        @Override
        public List<String> writeInCollection(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Integer readFromCollection(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Integer.parseInt(value);
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[] {Integer.class, Integer.TYPE};
        }
    }

    public static class DoubleHandler implements IConfigurationHandler {

        @Override
        public List<String> writeInCollection(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Double readFromCollection(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Double.parseDouble(value);
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[] {Double.class, Double.TYPE};
        }
    }

    public static class CharacterHandler implements IConfigurationHandler {

        @Override
        public List<String> writeInCollection(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Character readFromCollection(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return value.charAt(0);
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[] {Character.class, Character.TYPE};
        }
    }

    public static class BooleanHandler implements IConfigurationHandler {

        @Override
        public List<String> writeInCollection(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Boolean readFromCollection(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Boolean.valueOf(value);
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[] {Boolean.class, Boolean.TYPE};
        }
    }

    public static class FloatHandler implements IConfigurationHandler {

        @Override
        public List<String> writeInCollection(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Float readFromCollection(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Float.valueOf(value);
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[] {Float.class, Float.TYPE};
        }
    }

    public static class ShortHandler implements IConfigurationHandler {

        @Override
        public List<String> writeInCollection(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Short readFromCollection(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Short.valueOf(value);
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[] {Short.class, Short.TYPE};
        }
    }

    public static class LongHandler implements IConfigurationHandler {

        @Override
        public List<String> writeInCollection(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Long readFromCollection(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Long.valueOf(value);
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[] {Long.class, Long.TYPE};
        }
    }

    public static class ByteHandler implements IConfigurationHandler {

        @Override
        public List<String> writeInCollection(Field field, Object value, ParameterizedType parameterizedType, HxCConfig mainInstance) {
            return Collections.singletonList(String.valueOf(value));
        }

        @Override
        public Byte readFromCollection(String value, HxCConfig mainInstance, Map<String, Object> info) {
            return Byte.valueOf(mainInstance.getCurrentLine());
        }

        @Override
        public Class<?>[] getTypes() {
            return new Class<?>[] {Byte.class, Byte.TYPE};
        }
    }
}