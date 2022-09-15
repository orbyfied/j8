package net.orbyfied.j8.util.logging;

import net.orbyfied.j8.util.logging.formatting.TextFormat;

import java.awt.*;
import java.util.function.BiConsumer;

public class LogLevel {

    // the identifier
    String id;

    // the tagger
    BiConsumer<LogRecord, LogText> tagger;

    public LogLevel(String id, BiConsumer<LogRecord, LogText> tagger) {
        this.id     = id;
        this.tagger = tagger;
    }

    public String getIdentifier() {
        return id;
    }

    public BiConsumer<LogRecord, LogText> getTagger() {
        return tagger;
    }

    public void tag(LogRecord record, LogText string) {
        tagger.accept(record, string);
    }

    ////////////////////////////////////////////////

    private static LogLevel ofString(final String id, final String unformatted, final Color color) {
        final TextFormat tf = TextFormat.of(color);
        return new LogLevel(id, (record, string) -> {
            string.put("level-" + id, (LogText.Stringable) format -> {
                if (format) {
                    return tf + unformatted + TextFormat.RESET;
                } else {
                    return unformatted;
                }
            });
        });
    }

    public static final LogLevel INFO  = ofString("info",  "INFO",  new Color(0x4CC4E3));
    public static final LogLevel OK    = ofString("ok",    "OK",    new Color(0x7CDD59));
    public static final LogLevel WARN  = ofString("warn",  "WARN",  new Color(0xDEB754));
    public static final LogLevel ERROR = ofString("error", "ERROR", new Color(0xEA5C5C));

}
