package net.orbyfied.j8.util.logging;

public abstract class LogHandler {

    /**
     * Handler action
     */
    public interface Action {

        /**
         * Handles a record.
         * @param pipeline The pipeline.
         * @param record The record.
         */
        void handle(LogPipeline pipeline, LogRecord record);

    }

    /**
     * Priority enum.
     * The enum ordinal is used to determine priority.
     */
    public enum Priority {

        HIGHEST(0.0f),
        HIGH(0.25f),
        MEDIUM(0.4f),
        LOW(0.7f),
        LOWEST(0.9f),

        NONE(-1.0f);

        float posApprox;

        Priority(float posApprox) {
            this.posApprox = posApprox;
        }

        public float getPositionApproximation() {
            return posApprox;
        }

    }

    /*
        Static Construction Methods
     */

    public static LogHandler of(Action action) {
        return new LogHandler() {
            @Override
            protected void handle(LogPipeline pipeline, LogRecord record) {
                action.handle(pipeline, record);
            }
        };
    }

    public static LogHandler of(String name, Action action) {
        return of(action).named(name);
    }

    public static LogHandler of(String name, Priority priority, Action action) {
        return of(action).named(name).prioritized(priority);
    }

    ///////////////////////////////////////////

    // the priority of this handler
    protected Priority priority = Priority.NONE;

    // the name of this handler
    protected String name;

    // if this handler should be
    // scheduled to run asynchronously
    protected boolean async = false;

    public LogHandler() { }

    public LogHandler(String name) {
        this.name = name;
    }

    public LogHandler(String name, Priority priority) {
        this.name     = name;
        this.priority = priority;
    }

    public LogHandler(Priority priority) {
        this.priority = priority;
    }

    /* Getters */

    public boolean isAsync() {
        return async;
    }

    public String name() {
        return name;
    }

    public Priority priority() {
        return priority;
    }

    /* Setters */

    public LogHandler async(boolean b) {
        this.async = b;
        return this;
    }

    public LogHandler named(String name) {
        this.name = name;
        return this;
    }

    public LogHandler prioritized(Priority priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Called by a pipeline when an event (LogRecord)
     * needs to be handled.
     * @param pipeline The pipeline.
     * @param record The event.
     */
    protected abstract void handle(LogPipeline pipeline, LogRecord record);

}
