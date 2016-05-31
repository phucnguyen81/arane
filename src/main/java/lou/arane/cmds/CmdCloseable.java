package lou.arane.cmds;

import java.io.Closeable;

import lou.arane.base.Cmd;

/**
 * Wrap a command to indicate it needs cleanup.
 *
 * @author Phuc
 */
public class CmdCloseable implements Cmd, Closeable {

	private final Cmd origin;
	private final Runnable cleanup;

	public CmdCloseable(Cmd origin, Runnable cleanup) {
		this.origin = origin;
		this.cleanup = cleanup;
	}

	@Override
	public boolean canRun() {
		return origin.canRun();
	}

	@Override
	public void doRun() {
		origin.doRun();
	}

	@Override
	public void close() {
		cleanup.run();
	}

	@Override
	public String toString() {
		return String.format("%s(%n origin:%s%n cleanup:%s%n)",
			getClass().getSimpleName(), origin, cleanup);
	}

}
