package lou.arane.commands.decor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lou.arane.base.Command;

/**
 * Keep running command(s) until no command raises exception
 * or no commands can be run.
 *
 * @author Phuc
 */
public final class RetryUntilSuccess implements Command {

	private final List<Command> commands;
	private final Consumer<RuntimeException> errorHandler;

	public RetryUntilSuccess(Iterable<Command> cmds) {
		this(cmds, e -> {});
	}

	public RetryUntilSuccess(Iterable<Command> cmds, Consumer<RuntimeException> errorHandler) {
		this.commands = StreamSupport
				.stream(cmds.spliterator(), false)
				.collect(Collectors.toList());
		this.errorHandler = errorHandler;
	}

	@Override
	public boolean canRun() {
		return commands.stream().anyMatch(Command::canRun);
	}

	/** Run each command until all commands run without raising exception.
	 * A command that raises exception is re-run later.
	 * This might run forever if commands always want to run but keep failing. */
	@Override
	public void doRun() {
    	Deque<Command> queue = new ArrayDeque<>(commands);
        while (!queue.isEmpty()) {
            Command c = queue.removeFirst();
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
