package net.orbyfied.j8.util.logging;

import net.orbyfied.j8.util.StringUtil;
import net.orbyfied.j8.util.data.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Event {

    // the internal name
    protected final String id;

    // the message
    protected Object message;
    // the event level
    protected EventLevel level = EventLevel.INFO;

    // the errors
    protected final List<Throwable> errors = new ArrayList<>();
    // extra values
    protected Values extra;

    protected EventLog log;

    public Event(String id) {
        this.id = id;
    }

    public Event forLog(EventLog log) {
        this.log = log;
        return this;
    }

    public Event push() {
        log.push(this);
        return this;
    }

    public String identifier() {
        return id;
    }

    public Values extra() {
        if (extra == null)
            extra = new Values();
        return extra;
    }

    public Event extra(Consumer<Values> consumer) {
        consumer.accept(extra());
        return this;
    }

    public Event withMessage(Object message) {
        this.message = message;
        return this;
    }

    public Event withMessage(String format, Object... values) {
        this.message = StringUtil.format(format, values);
        return this;
    }

    public Event withError(Throwable t) {
        errors.add(t);
        return this;
    }

    public Event withLevel(EventLevel level) {
        this.level = level;
        return this;
    }

    public EventLevel getLevel() {
        return level;
    }

    public List<Throwable> getErrors() {
        return errors;
    }

    public Object getMessage() {
        return message;
    }

}
