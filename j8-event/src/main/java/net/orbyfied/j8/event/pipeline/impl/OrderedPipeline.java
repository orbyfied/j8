package net.orbyfied.j8.event.pipeline.impl;

import net.orbyfied.j8.event.pipeline.Pipeline;

public interface OrderedPipeline<E, A extends OrderedPipelineHandlerAction> extends Pipeline<E, A> {

}
