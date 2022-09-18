package j8_util.logging;

import net.orbyfied.j8.tests.Benchmarks;
import net.orbyfied.j8.util.logging.*;
import net.orbyfied.j8.util.reflect.Reflector;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BasicLoggerTest {

    @Test
    void test_BasicLogger() {
        // create group
        LoggerGroup group = new LoggerGroup("server");

        /*
         * Time appending.
         */

        final DateFormat format = new SimpleDateFormat("hh:mm:ss.SSSS");

        group.addConfigure((group1, logger1) -> {
            logger1.prePipeline()
                    .addLast(LogHandler.of((pipeline, record) -> {
                        record.carry("time", new Date());
                    }).named("set-time"));

            logger1.pipeline()
                    .addLast(LogHandler.of((pipeline, record) -> {
                        Date date = record.carried("time");
                        LogText text  = record.getText();
                        LogText tTime = text.sub("time", 0);
                        tTime.put("(");
                        tTime.put("time-value", format.format(date));
                        tTime.put(")");
                    }).named("format-time"));
        });

        /*
         * Logging.
         */

        // create logger
        Logger logger = group.create("Server");

        logger.log(LogLevel.INFO, "Hello!");
        logger.stage("Startup");
        logger.log(LogLevel.INFO, "Hello!");

        // test output streams
        logger.out.println("Hello.");
        logger.out.println(69);
        logger.err.println("Error.");
        logger.err.println(69);
    }

}
