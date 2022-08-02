package net.orbyfied.j8.event;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.orbyfied.j8.event.pipeline.BusPipelineFactory;
import net.orbyfied.j8.event.pipeline.PipelineAccess;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The main event system class.
 */
public class EventBus {

    /**
     * Listeners mapped by class.
     */
    private final Object2ObjectOpenHashMap<Class<? extends net.orbyfied.j8.event.EventListener>, net.orbyfied.j8.event.RegisteredListener> listenersByClass = new Object2ObjectOpenHashMap<>();

    /**
     * Listeners stored linearly.
     */
    private final ArrayList<net.orbyfied.j8.event.RegisteredListener> listeners = new ArrayList<>();

    /**
     * Cache for event pipelines.
     */
    private final Object2ObjectOpenHashMap<Class<?>, PipelineAccess<?>> eventPipelineCache = new Object2ObjectOpenHashMap<>();

    /**
     * The default pipeline factory for events
     * that don't explicitly specify a pipeline.
     */
    private BusPipelineFactory defaultPipelineFactory;

    /**
     * Registers a listener instance by creating
     * a {@link net.orbyfied.j8.event.RegisteredListener} and adding it
     * to the registrations in this event bus.
     * @param listener The listener instance.
     * @return The new {@link net.orbyfied.j8.event.RegisteredListener} instance.
     */
    public net.orbyfied.j8.event.RegisteredListener register(net.orbyfied.j8.event.EventListener listener) {
        // create and add registered listener
        net.orbyfied.j8.event.RegisteredListener rl = new net.orbyfied.j8.event.RegisteredListener(this, listener);
        listeners.add(rl);
        listenersByClass.put(rl.klass, rl);

        // parse and register
        rl.parse().register();

        // return
        return rl;
    }

    /**
     * Checks if the listeners class
     * was registered already.
     * @param listener The listener object.
     * @return True/false.
     */
    public boolean isRegistered(net.orbyfied.j8.event.EventListener listener) {
        return listenersByClass.containsKey(listener.getClass());
    }

    /**
     * Checks if the supplied class
     * was registered already.
     * @param listener The listener object.
     * @return True/false.
     */
    public boolean isRegistered(Class<? extends net.orbyfied.j8.event.EventListener> listener) {
        return listenersByClass.containsKey(listener);
    }

    /**
     * Gets the latest registered listener
     * with the specified class.
     * @param klass The class.
     * @return The last registered listener.
     */
    public net.orbyfied.j8.event.RegisteredListener getRegistered(Class<? extends net.orbyfied.j8.event.EventListener> klass) {
        return listenersByClass.get(klass);
    }

    /**
     * Gets all registered listeners for
     * the specified class.
     * @param klass The class.
     * @return An unmodifiable list of listeners.
     */
    public List<net.orbyfied.j8.event.RegisteredListener> getAllRegistered(Class<? extends net.orbyfied.j8.event.EventListener> klass) {
        List<net.orbyfied.j8.event.RegisteredListener> list = new ArrayList<>();
        for (net.orbyfied.j8.event.RegisteredListener l : listeners)
            if (l.klass == klass) list.add(l);
        return Collections.unmodifiableList(list);
    }

    /**
     * Gets all registered listeners.
     * @return An unmodifiable list of listeners.
     */
    public List<net.orbyfied.j8.event.RegisteredListener> getAllRegistered() {
        return Collections.unmodifiableList(listeners);
    }

    /**
     * Unregisters a registered listener.
     * @param listener The registered listener.
     * @return This.
     */
    public EventBus unregister(net.orbyfied.j8.event.RegisteredListener listener) {
        if (listener == null)
            return this;

        // destroy listener
        listener.destroy();

        // remove from lists
        listeners.remove(listener);
        listenersByClass.remove(listener.klass, listener);

        // return
        return this;
    }

    /**
     * Unregisters the last listener of
     * the listeners type.
     * @param listener The listener.
     * @return This.
     */
    public EventBus unregisterLast(net.orbyfied.j8.event.EventListener listener) {
        unregister(listenersByClass.get(listener.getClass()));
        return this;
    }

    /**
     * Unregisters the last listener of
     * the specified type.
     * @param klass The listener class.
     * @return This.
     */
    public EventBus unregisterLast(Class<? extends net.orbyfied.j8.event.EventListener> klass) {
        unregister(listenersByClass.get(klass));
        return this;
    }

    /**
     * Unregisters all registered listeners
     * of the specified type.
     * @param klass The type.
     * @return This.
     */
    public EventBus unregisterAll(Class<? extends net.orbyfied.j8.event.EventListener> klass) {
        for (net.orbyfied.j8.event.RegisteredListener rl : listeners)
            if (rl.klass == klass) unregister(rl);
        return this;
    }

    /**
     * Bakes the event; prepares it.
     * Pre-caches the pipeline for an event.
     * This can significantly improve performance
     * on the first call.
     * @param event The event type.
     * @return This.
     */
    public EventBus bake(Class<?> event) {
        // cache pipeline
        getPipelineFor(event);

        // return
        return this;
    }

    /**
     * Posts an event to the event bus.
     * Uses the events class as the pipeline provider.
     * @param event The event.
     * @return This.
     */
    @SuppressWarnings("unchecked")
    public <E extends net.orbyfied.j8.event.BusEvent> EventBus post(E event) {
        return post((Class<E>) event.getClass(), event);
    }

    /**
     * Posts an event to the event bus
     * through the pipeline supplied by
     * the supplied class.
     * NOTE: Doesn't call any events, and
     * does not catch any errors.
     * @param fclass The pipeline provider class.
     * @param event The event.
     */
    @SuppressWarnings("unchecked")
    public <E> void postUnsafe(Class fclass, E event) {
        // get pipeline for event
        final PipelineAccess<E> acc = (PipelineAccess<E>) getPipelineFor(fclass);

        // post event
        acc.push(event);
    }

    /**
     * Posts an event to the event bus
     * through the pipeline supplied by
     * the supplied class. Calls all
     * functional event services before
     * posting.
     * @param fclass The pipeline provider class.
     * @param event The event.
     * @return This.
     */
    @SuppressWarnings("unchecked")
    public <E> EventBus post(Class<E> fclass, E event) {
        // get pipeline for event
        final PipelineAccess<E> acc = (PipelineAccess<E>) getPipelineFor(fclass);

        // post
        pushSafe(event, acc);

        // return
        return this;
    }

    /**
     * Safely pushes an event down the
     * given pipeline access.
     * @param event The event instance.
     * @param acc The pipeline access.
     * @param <E> The event type.
     */
    protected <E> void pushSafe(E event, PipelineAccess<E> acc) {
        try {
            // post event
            acc.push(event);
        } catch (Exception e) {
            // throw invocation exception
            throw new EventInvocationException(this, "error occurred in event handler", e);
        }
    }

    /**
     * Retrieves the pipeline of an event
     * for this event bus from either the cache, the
     * {@link net.orbyfied.j8.event.BusEvent#getPipeline(EventBus)} method or
     * the {@link EventBus#defaultPipelineFactory}.
     * @param event The event class.
     * @return The pipeline or null if the event type
     *         is invalid or an error occurred.
     */
    @SuppressWarnings("unchecked")
    public PipelineAccess<?> getPipelineFor(Class<?> event) {
        // try to get from cache
        PipelineAccess<?> pipeline = eventPipelineCache.get(event);
        if (pipeline != null)
            return pipeline;

        // retrieve and cache
        try {
            try {
                // try to use pipeline getter method
                Method getPipeline = event.getDeclaredMethod("getPipeline", EventBus.class);
                pipeline = (PipelineAccess<net.orbyfied.j8.event.BusEvent>) getPipeline.invoke(null, this);
            } catch (NoSuchMethodException e) {
                if (defaultPipelineFactory == null)
                    // throw exception
                    throw new InvalidEventException(this, event, "pipeline provider { PipelineAccess<E> getPipeline(EventBus); } not implemented");
                // create default pipeline
                pipeline = defaultPipelineFactory.createPipeline(this, event);
            }

            eventPipelineCache.put(event, pipeline); // cache
            return pipeline;
        } catch (InvalidEventException e) {
            throw e; // dont catch these
        } catch (Exception e) {
            // throw internal exception
            throw new InternalBusException(this, "internal exception while retrieving event pipeline from '" +
                    event.getName() + "'", e);
        }
    }

    public PipelineAccess<?> getPipelineOrNull(Class<?> event) {
        return eventPipelineCache.get(event);
    }

    public EventBus withDefaultPipelineFactory(BusPipelineFactory factory) {
        this.defaultPipelineFactory = factory;
        return this;
    }

}
