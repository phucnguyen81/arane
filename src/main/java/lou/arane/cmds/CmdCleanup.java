package lou.arane.cmds;

import lou.arane.base.Cmd;

/**
 * Make sure a cleanup action is done for a command.
 *
 * @author Phuc
 */
public class CmdCleanup implements Cmd {

	private final Cmd origin;
	private final Runnable cleanup;

	public CmdCleanup(Cmd origin, Runnable cleanup) {
		this.origin = origin;
		this.cleanup = cleanup;
	}

	@Override
	public boolean canRun() {
		return origin.canRun();
	}

	@Override
	public void doRun() {
		try {
			origin.doRun();
		}
		finally {
			cleanup.run();
		}
	}

	@Override
	public String toString() {
		return String.format("%s(%n origin:%s%n cleanup:%s%n)",
			getClass().getSimpleName(), origin, cleanup);
	}

}
