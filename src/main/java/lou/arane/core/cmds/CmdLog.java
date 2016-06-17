package lou.arane.core.cmds;

import lou.arane.core.Cmd;
import lou.arane.util.Log;
import lou.arane.util.ToString;

/**
 * Log before and after the run.
 *
 * @author Phuc
 */
public class CmdLog extends CmdWrap<Cmd> {

    public CmdLog(Cmd origin) {
        super(origin);
    }

    @Override
    protected final void beforeDoRun(Cmd origin) {
        Log.info("Start " + origin);
    }

    @Override
    protected final void afterDoRun(Cmd origin) {
        Log.info("End " + origin);
    }

    @Override
    public String toString() {
        return ToString.of(CmdLog.class).add(super.toString()).str();
    }

}
