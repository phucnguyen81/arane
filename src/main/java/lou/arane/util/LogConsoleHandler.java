package lou.arane.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * Log to either System.err or System.out depending on log level.
 * <p>
 * NOTE: This handler does not read config from LogManager like ConsoleHandler.
 * The rest of the code is similar to ConsoleHandler though.
 *
 * @author Phuc
 */
public class LogConsoleHandler extends Handler {

    private final StreamHandler out;
    private final StreamHandler err;

    public LogConsoleHandler(Level level) {
        out = new StreamHandler(System.out, new SimpleFormatter());
        err = new StreamHandler(System.err, new SimpleFormatter());
        setLevel(level);
    }

    @Override
    public final void setLevel(Level level) {
        super.setLevel(level);
        out.setLevel(level);
        err.setLevel(level);
    }

    /**
     * Publish logs whose level >= WARNING to System.err and the rest to
     * System.out
     */
    @Override
    public final void publish(LogRecord record) {
        if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
            err.publish(record);
        }
        else {
            out.publish(record);
        }
        /* flush since for console we need to see the complete record right away */
        flush();
    }

    /**
     * Flush any published (buffered) records to console
     */
    @Override
    public final void flush() {
        out.flush();
        err.flush();
    }

    /**
     * Flush, not close, since we do not close System.out and System.err
     */
    @Override
    public final void close() throws SecurityException {
        flush();
    }

}
