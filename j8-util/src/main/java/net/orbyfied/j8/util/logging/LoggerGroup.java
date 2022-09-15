package net.orbyfied.j8.util.logging;

import net.orbyfied.j8.util.logging.io.LogOutput;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoggerGroup {

    // the name of this group
    final String name;

    // the output worker
    LoggerWorker worker;
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

    // the loggers
    List<Logger>        loggers       = new ArrayList<>();
    Map<String, Logger> loggersByName = new HashMap<>();

    public LoggerGroup(String name) {
        this.name = name;
    }

    /* Loggers */

    public LoggerGroup add(Logger logger) {
        loggers.add(logger);
        loggersByName.put(logger.name, logger);
        return this;
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

                }
            }
        }

    }

}
