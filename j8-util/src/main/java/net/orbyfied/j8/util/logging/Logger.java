package net.orbyfied.j8.util.logging;

public class Logger {

    // the name
    final String name;

    // the logger tag
    String tag;
    // the stage
    String stage;

    // the pipeline
    LogPipeline pipeline;

    Logger(String name) {
        this.name = name;
    }

    /* Getters */

    public String getName() {
        return name;
    }

    public String getStage() {
        return stage;
    }

    public String getTag() {
        return tag;
    }

    public LogPipeline pipeline() {
        return pipeline;
    }



}
