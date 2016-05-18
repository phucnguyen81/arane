package lou.arane.cmds;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lou.arane.base.Cmd;

/**
 * Keep running command(s) until no command raises exception
 * or no cmds can be run.
 *
 * @author Phuc
 */
public final class CmdRetryUntilSuccess implements Cmd {

	private final List<Cmd> cmds;
	private final Consumer<RuntimeException> errorHandler;

	public CmdRetryUntilSuccess(Iterable<Cmd> cmds) {
		this(cmds, e -> {});
	}

	public CmdRetryUntilSuccess(Iterable<Cmd> cmds, Consumer<RuntimeException> errorHandler) {
		this.cmds = StreamSupport
				.stream(cmds.spliterator(), false)
				.collect(Collectors.toList());
		this.errorHandler = errorHandler;
	}

	@Override
	public boolean canRun() {
		return cmds.stream().anyMatch(Cmd::canRun);
	}

	/** Run each command until all cmds run without raising exception.
	 * A command that raises exception is re-run later.
	 * This might run forever if cmds always want to run but keep failing. */
	@Override
	public void doRun() {
    	Deque<Cmd> queue = new ArrayDeque<>(cmds);
        while (!queue.isEmpty()) {
            Cmd c = queue.removeFirst();
            try {
        		c.run();
        	}
        	catch (RuntimeException e) {
        		errorHandler.accept(e);
        		// re-try later
        		if (c.canRun()) {
        			queue.addLast(c);
        		}
        	}
        }
	}
}
