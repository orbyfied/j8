package net.orbyfied.j8.util.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unchecked")
public class LogRecord {

    // the logger
    final Logger logger;

    // the level
    final LogLevel level;

    // the log string
    final LogText string;

    // the carried information
    final HashMap<String, Object> carry = new HashMap<>();

    // if it was cancelled
    final AtomicBoolean cancelled = new AtomicBoolean(false);
    private AtomicBoolean cancelled1;

    LogRecord(Logger logger, LogLevel level, LogText string) {
        this.logger = logger;
        this.level  = level;
        this.string = string;
    }

    public Logger getLogger() {
        return logger;
    }

    public LogLevel getLevel() {
        return level;
    }

    public LogText getText() {
        return string;
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public LogRecord setCancelled(boolean b) {
        cancelled.set(b);
        return this;
    }

    /*
     * Carrier system. Allows you to carry values throughout
     * the event chain that the log request goes through.
     */

    public <T> Map<String, T> carried() { return (Map<String, T>)carry; }

    public <T> void carry(final String key, final T value) {
        carry.put(key, value);
    }

    public <T> T carried(final String key) {
        return (T) carry.get(key);
    }

    public <T> T uncarry(final String key) {
        return (T) carry.remove(key);
    }

    public void uncarry(final Object o) {
        carry.forEach((key, value) -> {
            if (value == o) carry.remove(key);
        });
    }

}
