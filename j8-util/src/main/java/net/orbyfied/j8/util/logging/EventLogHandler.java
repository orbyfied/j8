package net.orbyfied.j8.util.logging;

public class EventLogHandler {

    public interface Action {

        void handle(Event event);

    }

    ///////////////////////////

    private final String name;
    private final Action action;

    private boolean enabled = true;

    public EventLogHandler(String name, Action action) {
        this.name   = name;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public Action getAction() {
        return action;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public EventLogHandler setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

}
