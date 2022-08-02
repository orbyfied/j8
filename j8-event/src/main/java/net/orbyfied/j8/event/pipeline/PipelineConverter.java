package net.orbyfied.j8.event.pipeline;

// TODO: better name lmao
public interface PipelineConverter<T extends Pipeline> {

    T convert(Pipeline in);

}
