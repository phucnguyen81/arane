package lou.arane.core.cmds;

import lou.arane.core.Cmd;

/**
 * Limit the number of times a cmd can run.
 *
 * @author Phuc
 */
public class CmdLimitedRetry implements Cmd {

	private final Cmd origin;

	private int limit;

	public CmdLimitedRetry(Cmd cmd, int limit) {
		this.origin = cmd;
		this.limit = limit;
	}

	/** Whether {@link #doRun()} should be called */
	@Override
	public final boolean canRun() {
		return origin.canRun() && limit > 0;
	}

	/** Perform the download */
	@Override
	public final void doRun() {
		if (limit > 0) try {
			origin.doRun();
		} catch (Exception e) {
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