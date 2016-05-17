package lou.arane.commands.decor;

import java.util.function.BooleanSupplier;

import lou.arane.base.Command;

/**
 * Make command out of Runnable and BooleanSupplier.
 *
 * @author Phuc
 */
public final class Assembled implements Command {

	private final BooleanSupplier b;
	private final Runnable r;

	public Assembled(Runnable r) {
		this(() -> true, r);
	}

	public Assembled(BooleanSupplier b, Runnable r) {
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
		return String.format("Yoke(canRun=%s%n  doRun=%s%n", b, r);
	}

}
