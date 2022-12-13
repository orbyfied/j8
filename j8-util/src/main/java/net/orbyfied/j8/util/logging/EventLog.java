package net.orbyfied.j8.util.logging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventLog {

    // the event log name
    private final String name;

    // the events that happened
    private final List<Event> events = new ArrayList<>();

    // the handler pipeline
    private final List<EventLogHandler> handlers = new ArrayList<>();
    private final Map<String, EventLogHandler> handlerMap = new HashMap<>();

    ThreadLocal<String> stage = new ThreadLocal<>();

    public EventLog(String name) {
        this.name = name;
    }

    /*
        Getters and setters
     */

    public String getName() {
        return name;
    }

    public List<Event> getEvents() {
        return events;
    }

    public EventLog withHandler(EventLogHandler handler) {
        handlers.add(handler);
        handlerMap.put(handler.getName(), handler);
        return this;
    }

    public EventLogHandler getHandler(String name) {
        return handlerMap.get(name);
    }

    /*
        Utility
     */

    public EventLog stage(String name) {
        stage.set(name);
        return this;
    }

    public Event log(String id, EventLevel level, String msgFormat, Object... msgValues) {
        return push(new Event(id)
                .withLevel(level)
                .withMessage(msgFormat, msgValues));
    }

    public Event info(String id, String msgFormat, Object... msgValues) {
        return push(new Event(id)
                .withMessage(msgFormat, msgValues)
                .withLevel(EventLevel.INFO));
    }

    public Event warn(String id, String msgFormat, Object... msgValues) {
        return push(new Event(id)
                .withMessage(msgFormat, msgValues)
                .withLevel(EventLevel.WARN));
    }

    public Event ok(String id, String msgFormat, Object... msgValues) {
        return push(new Event(id)
                .withMessage(msgFormat, msgValues)
                .withLevel(EventLevel.OK));
    }

    public Event err(String id, String msgFormat, Object... msgValues) {
        return push(new Event(id)
                .withMessage(msgFormat, msgValues)
                .withLevel(EventLevel.ERR));
    }

    public Event err(String id, Throwable t, String msgFormat, Object... msgValues) {
        return push(new Event(id)
                .withMessage(msgFormat, msgValues)
                .withLevel(EventLevel.ERR)
                .withError(t));
    }

    public Event newInfo(String id, String msgFormat, Object... msgValues) {
        return new Event(id)
                .withMessage(msgFormat, msgValues)
                .withLevel(EventLevel.INFO)
                .forLog(this);
    }

    public Event newWarn(String id, String msgFormat, Object... msgValues) {
        return new Event(id)
                .withMessage(msgFormat, msgValues)
                .withLevel(EventLevel.WARN)
                .forLog(this);
    }

    public Event newOk(String id, String msgFormat, Object... msgValues) {
        return new Event(id)
                .withMessage(msgFormat, msgValues)
                .withLevel(EventLevel.OK)
                .forLog(this);
    }

    public Event newErr(String id, String msgFormat, Object... msgValues) {
        return new Event(id)
                .withMessage(msgFormat, msgValues)
                .withLevel(EventLevel.ERR)
                .forLog(this);
    }

    public Event newErr(String id, Throwable t, String msgFormat, Object... msgValues) {
        return new Event(id)
                .withMessage(msgFormat, msgValues)
                .withLevel(EventLevel.ERR)
                .withError(t)
                .forLog(this);
    }

    /*
        Processing
     */

    public Event push(Event event) {
        try {
            // pass through handlers
            for (EventLogHandler handler : handlers)
                if (handler.isEnabled())
                    handler.getAction().handle(event);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // add to events
        events.add(event);

        // return
        return event;
    }

    public Event last() {
        return events.get(events.size() - 1);
    }

    /*
        Logging
     */

    protected static void setupLogging() {

    }

    public void logString(Event event, Logger logger) {
        // log message
        if (event.getMessage() != null) {
            logger.stage(stage.get());
            logger.log(event.getLevel().getLogLevel(), record -> {
                record.withMisc(event.errors);
            }, event.getMessage());
        }

        // TODO: do after logged
        for (Throwable t : event.errors)
            t.printStackTrace();
    }

}
