package net.orbyfied.j8.util.logging;

import java.util.function.Consumer;

public class Event<V> {

    // the type identifier
    final String type;

    // the log level
    final LogLevel level;

    // the event value
    final V value;

    public Event(
            String type,
            LogLevel level,
            V value
    ) {
        this.type  = type;
        this.level = level;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public LogLevel getLevel() {
        return level;
    }

    public V getValue() {
        return value;
    }

    public Event<V> getValue(Consumer<V> consumer) {
        consumer.accept(value);
        return this;
    }

}
