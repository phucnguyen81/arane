package lou.arane.cmds;

import lou.arane.base.Cmd;

/**
 * Wrap a command to do something else.
 *
 * @author Phuc
 */
public final class CmdDecorator implements Cmd {

	private final Cmd origin;
	private final Runnable doRun;

	public CmdDecorator(Cmd origin, Runnable doRun) {
		this.origin = origin;
		this.doRun = doRun;
	}

	@Override
	public boolean canRun() {
		return origin.canRun();
	}

	@Override
	public void doRun() {
		doRun.run();
	}

	@Override
	public String toString() {
		return String.format(
			"{origin: %s%n  doRun: %s%n}"
			, origin, doRun);
	}
}
