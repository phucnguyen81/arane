package lou.arane.core.cmds;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import lou.arane.core.Cmd;
import lou.arane.util.New;

/**
 * Keep running command(s) until no command raises exception
 * or no command can be run.
 *
 * @author Phuc
 */
public class CmdAllSuccess<C extends Cmd> implements Cmd {

	private final List<C> cmds;

	public CmdAllSuccess(Iterable<C> iter) {
	    this.cmds = New.list(iter);
	}

	/**
	 * Can run if at least one can run
	 */
	@Override
	public final boolean canRun() {
	    return cmds.stream().anyMatch(Cmd::canRun);
	}

	/**
	 * Run each command until all cmds run without raising exception.
	 * A command that raises exception is re-run later.
	 * This might run forever if commands always want to run but keep failing.
	 */
	@Override
	public final void doRun() {
	    List<C> filtered = New.list(onFilter(cmds));
    	Deque<C> queue = new ArrayDeque<>(filtered);
        while (!queue.isEmpty()) {
            C c = queue.removeFirst();
            try {
        		c.run();
        	}
        	catch (Exception e) {
        		onException(c, e);
        		if (c.canRun()) {
        			// re-try later
        			queue.addLast(c);
        		}
        	}
        }
	}

	/**
	 * Template Method for selecting commands that will get run
	 */
	protected Iterable<C> onFilter(Iterable<C> cmds) {
	    return cmds;
	}

	/**
	 * Template Method for what to do if a command throws an exception
	 */
	protected void onException(C c, Exception e) {}

}
