package j8_util;

import net.orbyfied.j8.util.StringReader;
import net.orbyfied.j8.util.StringUtil;
import net.orbyfied.j8.util.builder.Property;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilTest {

    @Test
    void test_StringReader() {
        // create reader
        StringReader readerA = new StringReader("69 ae3 -67 69.420");

        // read int
        Assertions.assertEquals(69, readerA.collectInt(10));
        readerA.next();
        Assertions.assertEquals(0xae3, readerA.collectInt(16));
        readerA.next();
        Assertions.assertEquals(-67, readerA.collectInt(10));
        readerA.next();
        Assertions.assertEquals(69.420f, readerA.collectFloat());
    }

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
