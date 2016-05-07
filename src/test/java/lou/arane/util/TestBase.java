package lou.arane.util;

import org.junit.Assert;

public class TestBase extends Assert {

    protected static void assertEqualsIgnoreNewlines(String expected, String actual) {
        expected = Util.normalizeNewlines(expected);
        actual = Util.normalizeNewlines(actual);
        assertEquals(expected, actual);
    }

}
