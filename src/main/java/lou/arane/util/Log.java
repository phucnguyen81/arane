package lou.arane.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Just enough code to hide the logging implementation.
 *
 * @author LOU
 */
public class Log {

    /** Log with root logger */
    public static void info(Object msg) {
        rootLogger().info(msg.toString());
    }

    /** Log with root logger */
    public static void error(Throwable err) {
        rootLogger().log(Level.WARNING, "", err);
    }

    /** Log with root logger */
    public static void error(Object msg) {
        rootLogger().log(Level.WARNING, "");
    }

    private static Logger rootLogger() {
        return Logger.getLogger("");
    }

}
