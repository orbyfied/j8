package net.orbyfied.j8.util.logging;

import net.orbyfied.j8.util.logging.io.LogOutput;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoggerGroup {

    public interface Configure {

        void configure(LoggerGroup group,
                       Logger logger);

    }

    public static final LoggerGroup GLOBAL = new LoggerGroup("global");

    ///////////////////////////////////////////

    // the name of this group
    final String name;

    // the output worker
    LoggerWorker worker = new LoggerWorker();
    // if it is active
    AtomicBoolean active = new AtomicBoolean(false);
    // if the thread is waiting on content
    AtomicBoolean waiting = new AtomicBoolean(false);
    // the lock
    Object lock = new Object();

    // the deque of messages
    Deque<LogRecord> queue = new ArrayDeque<>();
    // the processing pipeline
    LogPipeline pipeline = new LogPipeline();

    // the configures
    List<Configure> configures = new ArrayList<>();

    // the loggers
    List<Logger>        loggers       = new ArrayList<>();
    Map<String, Logger> loggersByName = new HashMap<>();

    public LoggerGroup(String name) {
        this.name = name;
        setActive(true);
    }

    /* Functions */

    public LoggerGroup addConfigure(Configure configure) {
        this.configures.add(configure);
        return this;
    }

    public LoggerGroup removeConfigure(Configure configure) {
        this.configures.remove(configure);
        return this;
    }

    /* Loggers */

    public Logger getByName(String name) {
        return loggersByName.get(name);
    }

    public Logger create(String name) {
        Logger logger = new Logger(this, name);
        add(logger);
        return logger;
    }

    public LoggerGroup add(Logger logger) {
        loggers.add(logger);
        loggersByName.put(logger.name, logger);
        return this;
    }

    public LoggerGroup remove(Logger logger) {
        if (logger == null)
            return this;
        loggers.remove(logger);
        loggersByName.remove(logger.name);
        return this;
    }

    public LoggerGroup remove(String name) {
        return remove(getByName(name));
    }

    // called by the logger when assigned a group
    // to set default values, can be overridden
    protected void applyConfig(Logger logger) {
        // add std out
        logger.outputs.add(LogOutput.STDOUT);

        // apply configures
        for (Configure configure : configures) {
            configure.configure(this, logger);
        }
    }

    /* Process */

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
    public LoggerGroup setActive(boolean b) {
        active.set(b);
        if (b)
            activate();
        else
            deactivate();
        return this;
    }

    /* ----------- Worker ------------ */

    class LoggerWorker extends Thread {

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
                    Logger    logger = record.getLogger();

                    // format
                    {
                        // get base text
                        LogText text  = record.getText();
                        LogLevel level = record.getLevel();
                        List<Object> message = record.getMessage();

                        // create logger tag
                        String stage = record.stage;
                        LogText tLogger = text.sub("logger");
                        tLogger.put("[");
                        tLogger.put("logger-tag", logger.tag);
                        if (stage != null) {
                            tLogger.put("/");
                            tLogger.put("logger-stage", stage);
                        }
                        tLogger.put("]");

                        // tag level
                        LogText tLevel = text.sub("level");
                        tLevel.put("[");
                        level.getTagger().accept(record, tLevel);
                        tLevel.put("]");

                        // create message text
                        LogText tMessage = text.sub("message");
                        tMessage.put(" ");
                        int l = message.size();
                        for (int i = 0; i < l; i++) {
                            tMessage.put(Integer.toString(i), Objects.toString(message.get(i)));
                        }

                    }

                    // pass through logger pipeline
                    record.setCancelled(false);
                    logger.pipeline.push(record);
                    if (record.isCancelled())
                        continue; // skip if cancelled

                    // pass through group pipeline
                    record.setCancelled(false);
                    pipeline.push(record);
                    if (record.isCancelled())
                        continue; // skip if cancelled

                    // write to outputs
                    for (LogOutput output : logger.outputs)
                        output.queue(record);
                }
            }
        }

    }

}
