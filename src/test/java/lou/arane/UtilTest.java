package lou.arane;

import org.junit.Test;

import lou.arane.util.Util;

public class UtilTest extends TestBase {

    @Test
    public void padNumericSequences() {
        assertEquals(Util.padNumericSequences("12", 3), "012");
        assertEquals(Util.padNumericSequences("a12", 3), "a012");
        assertEquals(Util.padNumericSequences("a12b456", 3), "a012b456");
        assertEquals(Util.padNumericSequences("a12b456c78d", 3), "a012b456c078d");
    }

    @Test
    public void removeEnding() {
        assertEquals(Util.removeEnding("abc", "c"), "ab");
        assertEquals(Util.removeEnding("abc", "bc"), "a");
        assertEquals(Util.removeEnding("abc", "b"), "abc");
        assertEquals(Util.removeEnding("abc", ""), "abc");
    }

}
