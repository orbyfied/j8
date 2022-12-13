package net.orbyfied.j8.util.logging;

public enum EventLevel {

    OK(LogLevel.OK),
    INFO(LogLevel.INFO),
    WARN(LogLevel.WARN),
    ERR(LogLevel.ERROR);

    private final LogLevel logLevel;

    EventLevel(LogLevel level) {
        this.logLevel = level;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

}
