package lou.arane.cmds;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import lou.arane.base.Cmd;

/**
 * Keep running command(s) until no command raises exception
 * or no command can be run.
 *
 * @author Phuc
 */
public final class CmdRetryUntilSuccess implements Cmd {

	private final List<Cmd> cmds;
	private final Consumer<RuntimeException> errorHandler;

	public CmdRetryUntilSuccess(Collection<Cmd> cmds) {
		this(cmds, e -> {});
	}

	public CmdRetryUntilSuccess(Collection<Cmd> cmds, Consumer<RuntimeException> errorHandler) {
		this.cmds = new ArrayList<>(cmds);
		this.errorHandler = errorHandler;
	}

	@Override
	public boolean canRun() {
		return cmds.stream().anyMatch(Cmd::canRun);
	}

	/** Run each command until all cmds run without raising exception.
	 * A command that raises exception is re-run later.
	 * This might run forever if commands always want to run but keep failing. */
	@Override
	public void doRun() {
    	Deque<Cmd> queue = new ArrayDeque<>(cmds);
        while (!queue.isEmpty()) {
            Cmd c = queue.removeFirst();
            if (c.canRun()) try {
        		c.doRun();
        	}
        	catch (RuntimeException e) {
        		errorHandler.accept(e);
        		if (c.canRun()) {
        			// re-try later
        			queue.addLast(c);
        		}
        	}
        }
	}
}
