package net.orbyfied.j8.event.pipeline.impl;

import net.orbyfied.j8.event.handler.priority.HandlerPriority;
import net.orbyfied.j8.event.pipeline.PipelineHandlerAction;

public interface OrderedPipelineHandlerAction<S extends PipelineHandlerAction, E> extends PipelineHandlerAction<S, E> {

    OrderedPipelineHandlerAction<S, E> prioritized(HandlerPriority priority);

}
