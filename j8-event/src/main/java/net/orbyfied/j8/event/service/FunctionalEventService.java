package net.orbyfied.j8.event.service;

import net.orbyfied.j8.event.RegisteredListener;
import net.orbyfied.j8.event.pipeline.PipelineAccess;

public interface FunctionalEventService extends EventService {

    default void prePublish(Object event, PipelineAccess<?> pipeline) { }

    default void bake(Class<?> eventClass) { }

    default void registered(RegisteredListener listener) { }

    default void unregistered(RegisteredListener listener) { }

}
