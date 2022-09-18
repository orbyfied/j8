package net.orbyfied.j8.util.logging;

import net.orbyfied.j8.util.StringUtil;
import net.orbyfied.j8.util.logging.io.LogOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Logger {

    private static PrintStream createOutStream(final Logger logger,
                                               final LogLevel level) {
        return new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // write newlines raw
                for (LogOutput output : logger.outputs)
                    output.getStream().write(b);
            }
        }, true) {
            @Override
            public void write(byte[] buf, int off, int len) {
                // log raw string, disable newline because println() will append
                // a newline in the string, removing the need for a newline
                logger.log(level, record -> record.getText().newLine(false), new String(buf));
            }
        };
    }

    //////////////////////////////////////////

    // the name
    protected final String name;
    // the group this logger belongs to
    protected final LoggerGroup group;

    // the logger tag
    protected String tag;
    // the stage
    // this is thread local for thread safety
    protected ThreadLocal<String> stage;

    // output streams
    @Deprecated // broken
    public final PrintStream out = createOutStream(this, LogLevel.INFO);
    @Deprecated // broken
    public final PrintStream err = createOutStream(this, LogLevel.ERROR);

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
        return stage.get();
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
        this.stage.set(stage);
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

    public Logger logf(LogLevel level, String format, Object... values) {
        return log(level, StringUtil.pattern(format).format(values));
    }

    public Logger log(LogLevel level, Object... message) {
        return log(level, null, message);
    }

    public Logger log(LogLevel level, Consumer<LogRecord> consumer, Object... message) {
        // create log record
        LogRecord record = new LogRecord(this, level, stage.get(), new LogText(), message);
        record.getText().newLine(true);

        // process record
        if (consumer != null)
            consumer.accept(record);

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
    
    public Logger info(String format, Object... values) {
        return logf(LogLevel.INFO, format, values);
    }

    public Logger info(Object... values) {
        return log(LogLevel.INFO, values);
    }

    public Logger ok(String format, Object... values) {
        return logf(LogLevel.OK, format, values);
    }

    public Logger ok(Object... values) {
        return log(LogLevel.OK, values);
    }

    public Logger warn(String format, Object... values) {
        return logf(LogLevel.WARN, format, values);
    }

    public Logger warn(Object... values) {
        return log(LogLevel.WARN, values);
    }

    public Logger err(String format, Object... values) {
        return logf(LogLevel.ERROR, format, values);
    }

    public Logger err(Object... values) {
        return log(LogLevel.ERROR, values);
    }

    public Logger errt(String format, Throwable t, Object... values) {
        return log(LogLevel.ERROR, record -> record.withMisc(t),
                StringUtil.format(format, values));
    }

}
