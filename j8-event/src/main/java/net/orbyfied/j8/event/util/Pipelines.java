package net.orbyfied.j8.event.util;

import net.orbyfied.j8.event.BusEvent;
import net.orbyfied.j8.event.EventBus;
import net.orbyfied.j8.event.pipeline.Pipeline;
import net.orbyfied.j8.event.pipeline.PipelineAccess;
import net.orbyfied.j8.event.pipeline.PipelineConverter;
import net.orbyfied.j8.event.pipeline.PipelineHandlerAction;
import net.orbyfied.j8.event.pipeline.impl.BasicPipeline;

public class Pipelines {

    private Pipelines() { }

    @SuppressWarnings("unchecked")
    public static PipelineAccess parental(EventBus bus,
                                          Class<?> klass) {
        final Class<?>[] parents = klass.getClasses();
        return new PipelineAccess() {
            Pipeline pipeline = new BasicPipeline<>();

            @Override
            public PipelineAccess push(Object event) {
                pipeline.push(event);
                for (Class<?> parent : parents) {
                    PipelineAccess p = bus.getPipelineOrNull(parent);
                    if (p == null)
                        continue;
                    p.push(event);
                }
                return this;
            }

            @Override
            public Pipeline base() {
                return pipeline;
            }

            @Override
            public Pipeline base(PipelineConverter converter) {
                pipeline = converter.convert(pipeline);
                return null;
            }
        };
    }

    public static PipelineAccess<BusEvent> mono(EventBus bus) {
        return new PipelineAccess<>() {
            Pipeline pipeline = new BasicPipeline<>();

            @Override
            public PipelineAccess<BusEvent> push(BusEvent event) {
                pipeline.push(event);
                return this;
            }

            @Override
            public Pipeline<BusEvent, ? extends PipelineHandlerAction> base() {
                return pipeline;
            }

            @Override
            public Pipeline base(PipelineConverter converter) {
                pipeline = converter.convert(pipeline);
                return null;
            }
        };
    }
}
