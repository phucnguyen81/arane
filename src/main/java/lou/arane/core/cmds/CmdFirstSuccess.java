package lou.arane.core.cmds;

import java.util.ArrayList;
import java.util.List;

import lou.arane.core.Cmd;
import lou.arane.util.ToString;

/**
 * Run each command until the first one succeeds.
 * If all fail, throw the suppressed exceptions.
 *
 * @author Phuc
 */
public class CmdFirstSuccess implements Cmd {

    private final List<Cmd> cmds;

    public CmdFirstSuccess(Iterable<? extends Cmd> coll) {
        this.cmds = new ArrayList<>();
        coll.forEach(this.cmds::add);
    }

    @Override
    public String toString() {
        return ToString.of(CmdFirstSuccess.class).add(cmds).str();
    }

    @Override
    public final boolean canRun() {
        return cmds.stream().anyMatch(Cmd::canRun);
    }

    @Override
    public final void doRun() {
        RuntimeException err = new RuntimeException();
        for (Cmd c : cmds) {
            try {
                c.run();
                return;
            }
            catch (Exception e) {
                err.addSuppressed(e);
            }
        }
        if (err.getSuppressed().length != 0) {
            throw err;
        }
    }

}
