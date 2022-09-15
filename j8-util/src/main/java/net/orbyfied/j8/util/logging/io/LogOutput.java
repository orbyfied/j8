package net.orbyfied.j8.util.logging.io;

import net.orbyfied.j8.util.builder.BuilderTemplate;
import net.orbyfied.j8.util.builder.Constructor;
import net.orbyfied.j8.util.builder.Property;
import net.orbyfied.j8.util.logging.LogPipeline;
import net.orbyfied.j8.util.logging.LogRecord;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;

public class LogOutput {

    // builder template
    private static final BuilderTemplate<LogOutput, Builder> TEMPLATE =
            new BuilderTemplate<LogOutput, Builder>(LogOutput.class)
                    .parameter("name", Property.ofString().require(true))
                    .parameter("stream", Property.of(PrintStream.class).require(true))
                    .parameter("formatted", Property.ofBool().require(false).defaulted(false))
                    .constructors(Constructor.takeBuilder(LogOutput.class));

    public static final LogOutput STDOUT = builder("stdout")
            .stream(System.out)
            .formatted(true)
            .build()
            .setActive(true);

    public static final LogOutput VOIDING = builder("voiding")
            .stream(null)
            .formatted(false)
            .build()
            .setActive(true);

    ///////////////////////////////////////////////

    // the name
    final String name;

    // the output worker
    OutputWorker worker;
    // if it is active
    AtomicBoolean active = new AtomicBoolean(false);
    // if the thread is waiting on content
    AtomicBoolean waiting = new AtomicBoolean(false);
    // the lock
    Object lock = new Object();

    // the deque of messages
    Deque<LogRecord> queue;
    // the processing pipeline
    LogPipeline pipeline;

    // the output stream
    PrintStream stream;
    // if it supports formatting
    boolean formatted = false;

    LogOutput(String name) {
        this.name     = name;
        this.worker   = new OutputWorker();
        this.pipeline = new LogPipeline();
    }

    LogOutput(Builder builder) {
        this.name      = builder.get("name");
        this.formatted = builder.get("formatted");
        this.stream    = builder.get("stream");
        this.queue     = new ArrayDeque<>();
        this.worker    = new OutputWorker();
        this.pipeline  = new LogPipeline();
    }

    /**
     * Forks this output.
     * @param name The new name.
     * @return The builder.
     */
    public Builder fork(String name) {
        Builder b = new Builder(name);
        b.set("formatted", formatted);
        b.set("stream", stream);
        return b;
    }

    /* Getters */

    public String getName() {
        return name;
    }

    public boolean isFormatted() {
        return formatted;
    }

    public PrintStream getStream() {
        return stream;
    }

    public boolean isActive() {
        return active.get();
    }

    public LogPipeline pipeline() {
        return pipeline;
    }

    /* Process */

    /**
     * Closes this output, deactivating
     * the worker thread and closing the
     * output stream.
     */
    public void close() {
        setActive(false);
        stream.close();
    }

    /**
     * Queues a new log record and notifies
     * the worker thread if it is waiting..
     * @param record The record.
     */
    public void queue(LogRecord record) {
        // push to queues
        synchronized (queue) {
            queue.add(record);
        }

        // notify if waiting
        if (waiting.get()) {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    // activate worker
    private void activate() {
        worker.start();
    }

    // deactivate worker
    private void deactivate() {
        try {
            worker.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the worker status.
     * @param b Active or inactive.
     * @return This.
     */
    public LogOutput setActive(boolean b) {
        active.set(b);
        if (b)
            activate();
        else
            deactivate();
        return this;
    }

    /* ---------- Worker ----------- */

    class OutputWorker extends Thread {

        @Override
        public void run() {
            // main loop
            while (active.get()) {
                // wait on lock
                try {
                    synchronized (lock) {
                        waiting.set(true);
                        lock.wait();
                    }
                } catch (Exception e) {
                    waiting.set(false);
                    e.printStackTrace();
                }

                waiting.set(false);

                // continue if no elements
                if (queue.size() == 0)
                    continue;

                // process elements
                while (queue.size() != 0) {
                    // poll new record
                    LogRecord record = queue.poll();

                    // pass through pipeline
                    record.setCancelled(false);
                    pipeline.push(record);
                    if (record.isCancelled())
                        continue; // skip if cancelled

                    // stringify text
                    String str = record.getText().toString(formatted);

                    // write to output stream
                    if (stream != null)
                        stream.println(str);
                }
            }
        }

    }

    /* ---------- Builder ----------- */

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder extends net.orbyfied.j8.util.builder.Builder<LogOutput, Builder> {
        Builder(String name) {
            super(TEMPLATE);
            set("name", name);
        }

        public Builder stream(PrintStream stream) {
            return set("stream", stream);
        }

        public Builder formatted(boolean b) {
            return set("formatted", b);
        }
    }

}
