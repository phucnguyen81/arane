package lou.arane.commands.decor;

import lou.arane.base.Command;

/**
 * Limit the number of times a command can run.
 *
 * @author Phuc
 */
public class RetryCommand implements Command {

	private final Command command;

	private int runLimit;

	public RetryCommand(Command command, int runLimit) {
		this.command = command;
		this.runLimit = runLimit;
	}

	/** Whether {@link #doRun()} should be called */
	@Override
	public boolean canRun() {
		return command.canRun() && runLimit > 0;
	}

	/** Perform the download */
	@Override
	public void doRun() {
		if (runLimit > 0) try {
			command.doRun();
		} catch (RuntimeException e) {
			runLimit -= 1;
			throw e;
		}
	}

    @Override
	public String toString() {
		return String.format("[%s%n  limit=%s]", command, runLimit);
	}
}