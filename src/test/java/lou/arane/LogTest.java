package lou.arane;

import org.junit.Test;

import lou.arane.util.Log;

public class LogTest {

    @Test
    public void info() {
        Log.info("This is info");
    }

    @Test
    public void warn() {
        Log.warning("This is warning");
    }


}
