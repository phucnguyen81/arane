package lou.arane.core;

/**
 * Limit the number of times a command can run.
 *
 * @author Phuc
 */
public class RetryCommand implements Command {

	private final Command command;

	public int runLimit = 1;

	public RetryCommand(Command command) {
		this.command = command;
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
		else {
			throw new RuntimeException("Run limit exceeded for " + this);
		}
	}

    @Override
	public String toString() {
		return String.format("[%s%n  limit=%s]", command, runLimit);
	}
}