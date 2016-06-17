package lou.arane.core.cmds;

import lou.arane.core.Cmd;
import lou.arane.util.ToString;

/**
 * Limit the number of times a cmd can run.
 *
 * @author Phuc
 */
public class CmdLimitedRetry implements Cmd {

    private final Cmd origin;

    private int limit;

    public CmdLimitedRetry(Cmd cmd, int limit) {
        this.origin = cmd;
        this.limit = limit;
    }

    /** Whether {@link #doRun()} should be called */
    @Override
    public final boolean canRun() {
        return origin.canRun() && limit > 0;
    }

    /** Perform the download */
    @Override
    public final void doRun() {
        if (limit <= 0) {
            throw new RuntimeException("Retry exceeded for running " + this);
        }
        else {
            limit -= 1;
            origin.run();
        }
    }

    @Override
    public String toString() {
        return ToString.of(CmdLimitedRetry.class).add("limit=", limit).nln()
                .add(origin).str();
    }
}