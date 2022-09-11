package j8_util.builder;

import net.orbyfied.j8.util.builder.Builder;
import net.orbyfied.j8.util.builder.BuilderTemplate;
import net.orbyfied.j8.util.builder.Constructor;
import net.orbyfied.j8.util.builder.Property;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

public class BuilderTests {

    static class MyLoggerBuilder extends Builder<MyLogger, MyLoggerBuilder> {
        public MyLoggerBuilder() {
            super(MyLogger.BUILDER_TEMPLATE);
        }

        public MyLoggerBuilder name(String name) {
            set("name", name);
            return this;
        }

        public MyLoggerBuilder important(boolean b) {
            set("important", b);
            return this;
        }
    }

    static class MyLogger {

        @SuppressWarnings("unchecked")
        static final BuilderTemplate<MyLogger, MyLoggerBuilder> BUILDER_TEMPLATE =
                new BuilderTemplate<MyLogger, MyLoggerBuilder>(MyLogger.class)
                        .parameter("name", Property.ofString().require(true))
                        .parameter("important", Property.ofBool().defaulted(false).require(true))
                        .constructors(Constructor.takeBuilder(MyLogger.class));

        ////////////////////////////

        // builder constructor
        MyLogger(Builder<String, Builder> builder) {
            this.name      = builder.get("name");
            this.important = builder.get("important");
        }

        final String  name;
        final boolean important;

        public void log(String text) {
            System.out.println((important ? "/!\\ " : "") + name + ": " + text);
        }

    }

    /* -------------- 1 ----------------- */

    @Test
    void test_BasicBuilder() {
        MyLogger builder = new MyLoggerBuilder()
                .name("Hank")
                .important(true)
                .build();

        builder.log("HELLO!");
    }

}
