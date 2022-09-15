package net.orbyfied.j8.util.logging;

import net.orbyfied.j8.util.logging.io.LogOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Logger {

    // the name
    protected final String name;
    // the group this logger belongs to
    protected final LoggerGroup group;

    // the logger tag
    protected String tag;
    // the stage
    protected String stage;

    /**
     * The pre-pipeline, this pipeline is called
     * synchronously before the record is posted
     * to the logger worker. This should not do
     * any heavy tasks.
     */
    protected LogPipeline prePipeline = new LogPipeline();

    /**
     * The worker pipeline, this pipeline is called
     * asynchronously by the logger worker. This is
     * where you should do formatting and other
     * heavy tasks.
     */
    protected LogPipeline pipeline = new LogPipeline();

    // the outputs to write to
    protected List<LogOutput> outputs = new ArrayList<>();

    Logger(LoggerGroup group, String name) {
        // set group to group or global
        LoggerGroup group1;
        group1 = group;
        if (group == null)
            group1 = LoggerGroup.GLOBAL;
        this.group = group1;

        // set name and default tag
        this.name = name;
        this.tag  = name;

        // apply config
        this.group.applyConfig(this);
    }

    /* Getters */

    public String getName() {
        return name;
    }

    public String getStage() {
        return stage;
    }

    public String getTag() {
        return tag;
    }

    /* Setters */

    public Logger setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public Logger stage(String stage) {
        this.stage = stage;
        return this;
    }

    /**
     * Get the worker pipeline, this pipeline is called
     * asynchronously by the logger worker. This is
     * where you should do formatting and other
     * heavy tasks.
     * @return The worker pipeline.
     */
    public LogPipeline pipeline() {
        return pipeline;
    }

    /**
     * The pre-pipeline, this pipeline is called
     * synchronously before the record is posted
     * to the logger worker. This should not do
     * any heavy tasks.
     * @return The pre-pipeline.
     */
    public LogPipeline prePipeline() { return prePipeline; }

    /* Log */

    public Logger log(LogLevel level, Consumer<LogRecord> consumer, Object... message) {
        return this;
    }

    public Logger log(LogLevel level, Object... message) {
        // create log record
        LogRecord record = new LogRecord(this, level, stage, new LogText(), message);

        // call pre pipeline
        record.setCancelled(false);
        prePipeline.push(record);
        if (record.isCancelled())
            return this; // cancelled

        // push record
        group.queue(record);

        // return
        return this;
    }

}
