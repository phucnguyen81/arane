package lou.arane;

import org.junit.Assert;

import lou.arane.util.Util;

public class TestBase extends Assert {

    protected static void assertEqualsIgnoreNewlines(String expected, String actual) {
        expected = Util.normalizeNewlines(expected);
        actual = Util.normalizeNewlines(actual);
        assertEquals(expected, actual);
    }

}
