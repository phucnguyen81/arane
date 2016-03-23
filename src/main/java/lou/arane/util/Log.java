package lou.arane.util;

import org.pmw.tinylog.Level;
import org.pmw.tinylog.LogEntryForwarder;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.FileWriter;

/**
 * Just enough to hide the logging library, which in this case is tinylog.
 *
 * @author LOU
 */
public class Log {

    /** Add a path for writing logs */
    public static void addOutFile(String filepath) {
        Logger
        .getConfiguration()
        .addWriter(new FileWriter(filepath))
        .activate();
    }

    public static void info(String msg) {
        LogEntryForwarder.forward(1, Level.INFO, msg);
    }

    public static void info(Throwable err, String msg) {
        LogEntryForwarder.forward(1, Level.INFO, err, msg);
    }

    public static void error(Throwable err) {
        LogEntryForwarder.forward(1, Level.ERROR, err);
    }

}