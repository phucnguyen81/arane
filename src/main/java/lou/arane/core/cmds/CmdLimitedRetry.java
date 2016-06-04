package lou.arane.core.cmds;

import lou.arane.core.Cmd;

/**
 * Limit the number of times a cmd can run.
 *
 * @author Phuc
 */
public final class CmdLimitedRetry implements Cmd {

	private final Cmd origin;

	private int limit;

	public CmdLimitedRetry(Cmd cmd, int limit) {
		this.origin = cmd;
		this.limit = limit;
	}

	/** Whether {@link #doRun()} should be called */
	@Override
	public boolean canRun() {
		return origin.canRun() && limit > 0;
	}

	/** Perform the download */
	@Override
	public void doRun() {
		if (limit > 0) try {
			origin.doRun();
		} catch (RuntimeException e) {
			limit -= 1;
			throw e;
		}
	}

    @Override
	public String toString() {
		return String.format("%s:%n  %s%n  limit=%s"
			, CmdLimitedRetry.class.getSimpleName(), origin, limit
		);
	}
}