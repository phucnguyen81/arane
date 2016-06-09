package lou.arane.core.cmds;

import lou.arane.core.Cmd;
import lou.arane.util.ToString;

/**
 * Wrap another command (Decorator Pattern) to attach additional logic.
 *
 * @author Phuc
 */
public class CmdWrap<C extends Cmd> implements Cmd {

    private final C origin;

    public CmdWrap(C origin) {
        this.origin = origin;
    }

    @Override
    public final boolean canRun() {
        return canRun(origin);
    }

    @Override
    public final void doRun() {
        beforeDoRun(origin);
        origin.doRun();
        afterDoRun(origin);
    }

    /**
     * Template Method for eval {@link #canRun()} status
     */
    protected boolean canRun(C origin) {
        return origin.canRun();
    }

    /**
     * Template Method for what to do before running
     */
    protected void beforeDoRun(C origin) {
    }

    /**
     * Template Method for what to do after running
     */
    protected void afterDoRun(C origin) {
    }

    @Override
    public String toString() {
        return ToString.of(CmdWrap.class).join(origin).render();
    }
}
