package lou.arane.cmds;

import lou.arane.base.Cmd;

/**
 * Limit the number of times a cmd can run.
 *
 * @author Phuc
 */
public final class CmdLimitedRetry implements Cmd {

	private final Cmd cmd;

	private int limit;

	public CmdLimitedRetry(Cmd cmd, int limit) {
		this.cmd = cmd;
		this.limit = limit;
	}

	/** Whether {@link #doRun()} should be called */
	@Override
	public boolean canRun() {
		return cmd.canRun() && limit > 0;
	}

	/** Perform the download */
	@Override
	public void doRun() {
		if (limit > 0) try {
			cmd.doRun();
		} catch (RuntimeException e) {
			limit -= 1;
			throw e;
		}
	}

    @Override
	public String toString() {
		return String.format("[%s%n  limit=%s]", cmd, limit);
	}
}