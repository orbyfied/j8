package net.orbyfied.j8.util.logging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class LogText {

    public interface Stringable {

        String toString(boolean format);

    }

    public static Stringable formatted(String unformatted, String formatted) {
        return format -> (format ? formatted : unformatted);
    }

    /////////////////////////////////////////

    // the components
    final HashMap<String, Object> mapped = new LinkedHashMap<>();
    final ArrayList<Object> linear = new ArrayList<>();

    public HashMap<String, Object> getMapped() {
        return mapped;
    }

    public ArrayList<Object> getLinear() {
        return linear;
    }

    /**
     * Put a new component at the end.
     * @param val The value.
     * @return This.
     */
    public LogText put(Object val) {
        mapped.put(Integer.toHexString(System.identityHashCode(val)), val);
        linear.add(val);
        return this;
    }

    /**
     * Put a new component at the end.
     * @param key The key.
     * @param val The value.
     * @return This.
     */
    public LogText put(String key, Object val) {
        mapped.put(key, val);
        linear.add(val);
        return this;
    }

    /**
     * Put a new component at the specified index.
     * @param key The key.
     * @param idx The index.
     * @param val The value.
     * @return This.
     */
    public LogText put(String key, int idx, Object val) {
        mapped.put(key, val);
        linear.add(idx, val);
        return this;
    }

    /**
     * Create a new log text object, add
     * it as a component under the specified
     * key and return it.
     * @param key The key to put it under.
     * @return The text object.
     */
    public LogText sub(String key) {
        LogText n = new LogText();
        put(key, n);
        return n;
    }

    /**
     * Create a new log text object, add
     * it as a component under the specified
     * key and at the specified index and return it.
     * @param key The key to put it under.
     * @param idx The index.
     * @return The text object.
     */
    public LogText sub(String key, int idx) {
        LogText n = new LogText();
        put(key, idx, n);
        return n;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) mapped.get(key);
    }

    public <T> LogText use(String key, Consumer<T> consumer) {
        T t = get(key);
        if (consumer != null)
            consumer.accept(t);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(String key, Function<String, T> function) {
        if (!mapped.containsKey(key)) {
            T t = function.apply(key);
            mapped.put(key, t);
            linear.add(t);
        }

        return (T) mapped.get(key);
    }

    public <T> LogText useOrCreate(String key, Function<String, T> constructor, Consumer<T> consumer) {
        T t = getOrCreate(key, constructor);
        if (consumer != null)
            consumer.accept(t);
        return this;
    }

    public String toString(boolean format) {
        // create builder
        StringBuilder b = new StringBuilder();
        // append all components
        int l = linear.size();
        for (int i = 0; i < l; i++) {
            Object entry = linear.get(i);
            if (entry instanceof LogText ls)
                b.append(ls.toString(format));
            else if (entry instanceof Stringable s)
                b.append(s.toString(format));
            else
                b.append(entry);
        }

        // return string
        return b.toString();
    }

    @Override
    public String toString() {
        return "LogText{" + linear.toString() + "}";
    }

}
