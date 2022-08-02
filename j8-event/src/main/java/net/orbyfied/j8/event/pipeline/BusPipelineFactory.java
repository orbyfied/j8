package net.orbyfied.j8.event.pipeline;

import net.orbyfied.j8.event.EventBus;

public interface BusPipelineFactory {

    PipelineAccess createPipeline(
            EventBus bus,
            Class eventClass
    );

}
