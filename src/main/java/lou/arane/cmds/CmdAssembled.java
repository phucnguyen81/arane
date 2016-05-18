package lou.arane.cmds;

import java.util.function.BooleanSupplier;

import lou.arane.base.Cmd;

/**
 * Make command out of Runnable and BooleanSupplier.
 *
 * @author Phuc
 */
public final class CmdAssembled implements Cmd {

	private final BooleanSupplier b;
	private final Runnable r;

	public CmdAssembled(Runnable r) {
		this(() -> true, r);
	}

	public CmdAssembled(BooleanSupplier b, Runnable r) {
		this.b = b;
		this.r = r;
	}

	@Override
	public void run() {
		if (canRun()) {
			doRun();
		}
	}

	@Override
	public boolean canRun() {
		return b.getAsBoolean();
	}

	@Override
	public void doRun() {
		r.run();
	}

	@Override
	public String toString() {
		return String.format("CmdAssembled(canRun=%s%n  doRun=%s%n", b, r);
	}

}
