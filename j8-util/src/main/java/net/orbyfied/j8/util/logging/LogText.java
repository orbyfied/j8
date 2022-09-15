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

    public LogText put(String key, Object val) {
        mapped.put(key, val);
        linear.add(val);
        return this;
    }

    public LogText put(String key, int idx, Object val) {
        mapped.put(key, val);
        linear.add(idx, val);
        return this;
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
        for (Object entry : linear) {
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

}
