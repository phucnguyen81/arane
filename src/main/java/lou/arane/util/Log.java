package lou.arane.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Just enough code to hide the logging implementation.
 * In this case, Java standard logging is more than enough.
 *
 * @author LOU
 */
public class Log {

    private static final Logger BASE = configureBaseLogger();

    private static Logger configureBaseLogger() {
        //base namespace for this app
        Logger base = Logger.getLogger("lou.arane");
        // don't delegate to parent logger
        base.setUseParentHandlers(false);
        // INFO level should be enough
        base.setLevel(Level.INFO);
        for (Handler h : base.getHandlers()) {
            // remove all default handlers if any
            base.removeHandler(h);
        }
        // add customized handler
        Handler handler = new LogConsoleHandler(base.getLevel());
        base.addHandler(handler);
        return base;
    }

    /** Log with base logger.
     * The args are separated by a space */
    public static void info(Object first, Object... rest) {
        StringBuilder s = new StringBuilder(first.toString());
        for (Object r : rest) {
            s.append(" ").append(r);
        }
        BASE.info(s.toString());
    }

    /** Log with base logger */
    public static void warning(Throwable err) {
        BASE.log(Level.WARNING, "", err);
    }

    /** Log with base logger */
    public static void warning(Object msg) {
        BASE.log(Level.WARNING, msg.toString());
    }

}
