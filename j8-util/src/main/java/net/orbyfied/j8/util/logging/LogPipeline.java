package net.orbyfied.j8.util.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LogPipeline {

    // the handlers on the pipeline
    ArrayList<LogHandler> handlers = new ArrayList<>();
    // handlers by name
    HashMap<String, LogHandler> named = new HashMap<>();

    // the async executor
    Executor asyncExecutor;

    public LogPipeline() {
        // create default executor
        this.asyncExecutor = Executors.newSingleThreadExecutor();
    }

    // deploys a handler to the async executor
    // to be called with a record later,
    // asynchronously of course
    private void deployAsync(final LogHandler h,
                             final LogRecord record) {
        if (asyncExecutor != null) {
            // schedule async
            asyncExecutor.execute(() -> h.handle(this, record));
        }
    }

    /**
     * Pushes a logging record into the
     * pipeline, passing it down through
     * all handlers.
     * @param record The record.
     */
    public void push(LogRecord record) {
        // cancel if empty
        if (count() == 0)
            return;

        // loop
        int l = named.size();
        for (int i = 0; i < l; i++) {
            LogHandler h = handlers.get(i);
            // execute
            if (!h.async) {
                h.handle(this, record);
            } else {
                // schedule handler
                deployAsync(h, record);
            }
        }
    }

    /**
     * Get the amount of handlers.
     * @return The count.
     */
    public int count() {
        return named.size();
    }

    /**
     * Get a list of all handlers in order.
     * @return The list.
     */
    public List<LogHandler> all() {
        return Collections.unmodifiableList(handlers);
    }

    /**
     * Get a handler by index if inbound,
     * or null if out of bounds.
     * @param i The index.
     * @return The handler or null.
     */
    public LogHandler get(int i) {
        if (i < 0 || i >= named.size())
            return null;
        return handlers.get(i);
    }

    /**
     * Get a handler by name if present,
     * or null if absent.
     * @param name The name.
     * @return The handler or null.
     */
    public LogHandler get(String name) {
        return named.get(name);
    }

    /**
     * Removes a handler by name if present,
     * or doesn't do anything if absent.
     * @param name The name.
     * @return This.
     */
    public LogPipeline remove(String name) {
        return remove(named.get(name));
    }

    /**
     * Remove a handler from the pipeline.
     * @param handler The handler.
     * @return This.
     */
    public LogPipeline remove(LogHandler handler) {
        // check null
        if (handler == null)
            return this;

        // remove if named
        if (handler.name != null)
            named.remove(handler.name);

        // remove from list
        handlers.remove(handler);

        // return
        return this;
    }

    /**
     * Adds a handler to the pipeline, putting it
     * in the named map if it has a name.
     *
     * It will search for an index if a priority is
     * set, otherwise it will just add it to the tail
     * of the list. It uses the approximated index as
     * a starting point to the search for a priority.
     * It will search for the end of the block of
     * priority (hence {@code addLast}).
     * @param handler The handler.
     * @return This.
     */
    public LogPipeline addLast(LogHandler handler) {
        // put if named
        if (handler.name != null)
            named.put(handler.name, handler);

        // try to put in correct position
        LogHandler.Priority priority = handler.priority();
        if (priority != null && priority != LogHandler.Priority.NONE) {
            int ord = priority.ordinal();
            int i = (int)(handler.priority().getPositionApproximation() * handlers.size());
            boolean in = false;
            while (i > 0 && i < handlers.size()) {
                LogHandler h = handlers.get(i);
                if (h.priority == null || h.priority == LogHandler.Priority.NONE) {
                    i++;
                    continue;
                }

                int ho = h.priority.ordinal();

                // check
                if (!in) {
                    if (ho < ord) {
                        i++;
                    } else if (ho > ord) {
                        i--;
                    } else {
                        in = true;
                    }
                } else {
                    if (ho != ord) {
                        break;
                    }

                    i++;
                }
            }

            // add handler
            handlers.add(i, handler);
        } else {
            // just add handler lol
            handlers.add(handler);
        }

        // return
        return this;
    }

    /**
     * Adds a handler to the pipeline, putting it
     * in the named map if it has a name.
     *
     * It will search for an index if a priority is
     * set, otherwise it will just add it to the start
     * of the list. It uses the approximated index as
     * a starting point to the search for a priority.
     * It will search for the start of the block of
     * priority (hence {@code addFirst}).
     * @param handler The handler.
     * @return This.
     */
    public LogPipeline addFirst(LogHandler handler) {
        // put if named
        if (handler.name != null)
            named.put(handler.name, handler);

        // try to put in correct position
        LogHandler.Priority priority = handler.priority();
        if (priority != null && priority != LogHandler.Priority.NONE) {
            int ord = priority.ordinal();
            int i = (int)(handler.priority().getPositionApproximation() * handlers.size());
            boolean in = false;
            while (i > 0 && i < handlers.size()) {
                LogHandler h = handlers.get(i);
                if (h.priority == null || h.priority == LogHandler.Priority.NONE) {
                    i--;
                    continue;
                }

                int ho = h.priority.ordinal();

                // check
                if (!in) {
                    if (ho < ord) {
                        i++;
                    } else if (ho > ord) {
                        i--;
                    } else {
                        in = true;
                    }
                } else {
                    if (ho != ord) {
                        break;
                    }

                    i--;
                }
            }

            // add handler
            handlers.add(i, handler);
        } else {
            // just add handler lol
            handlers.add(0, handler);
        }

        // return
        return this;
    }

}
