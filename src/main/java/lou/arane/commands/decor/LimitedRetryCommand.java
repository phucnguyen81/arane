package lou.arane.commands.decor;

import lou.arane.base.Command;

/**
 * Limit the number of times a command can run.
 *
 * @author Phuc
 */
public class LimitedRetryCommand implements Command {

	private final Command command;

	private int limit;

	public LimitedRetryCommand(Command command, int limit) {
		this.command = command;
		this.limit = limit;
	}

	/** Whether {@link #doRun()} should be called */
	@Override
	public boolean canRun() {
		return command.canRun() && limit > 0;
	}

	/** Perform the download */
	@Override
	public void doRun() {
		if (limit > 0) try {
			command.doRun();
		} catch (RuntimeException e) {
			limit -= 1;
			throw e;
		}
	}

    @Override
	public String toString() {
		return String.format("[%s%n  limit=%s]", command, limit);
	}
}