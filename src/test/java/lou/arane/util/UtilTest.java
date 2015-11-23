package lou.arane.util;

import org.junit.Test;

public class UtilTest extends TestBase {

    @Test
    public void padNumericSequences() {
        assertEquals(Util.padNumericSequences("12", 3), "012");
        assertEquals(Util.padNumericSequences("a12", 3), "a012");
        assertEquals(Util.padNumericSequences("a12b456", 3), "a012b456");
        assertEquals(Util.padNumericSequences("a12b456c78d", 3), "a012b456c078d");
    }

}
