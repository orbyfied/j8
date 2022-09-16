package j8_util;

import net.orbyfied.j8.util.StringUtil;
import net.orbyfied.j8.util.builder.Property;
import org.junit.jupiter.api.Test;

public class StringUtilTest {

    @Test
    void test_StringFormat() {
        // compile pattern
        StringUtil.FormatPattern pattern =
                StringUtil.pattern("Hello, {0}!");

        // format strings
        System.out.println(pattern.format("World"));
        System.out.println(pattern.format("Orbyfied"));
        System.out.println(pattern.format(69696969));
    }

}
