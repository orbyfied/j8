package net.orbyfied.j8.util.logging;

import java.util.ArrayList;
import java.util.List;

public class EventLog {

    // the events that have happened
    final List<Event<?>> events = new ArrayList<>();

    // the stack index
    int stackIdx = -1;

    public List<Event<?>> getEvents() {
        return events;
    }

    @SuppressWarnings("unchecked")
    public <V> Event<V> peek() {
        return (Event<V>) events.get(stackIdx);
    }

    @SuppressWarnings("unchecked")
    public <V> Event<V> poll() {
        if (stackIdx == -1)
            return null;
        return (Event<V>) events.get(stackIdx--);
    }

    public <V> Event<V> poll(String type) {
        return null;
    }

}
