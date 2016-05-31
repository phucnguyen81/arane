package lou.arane.cmds;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import lou.arane.base.Cmd;

/**
 * Delegate to another command (Decorator Pattern).
 * Can replace the condition and/or the action of the origin command.
 *
 * @author Phuc
 */
public class CmdWrap implements Cmd {

	private final Optional<Cmd> origin;

	private final BooleanSupplier condition;

	private final Runnable action;

	/** Wrap another command to delegate to it */
	public CmdWrap(Cmd c) {
		this(c, c::canRun);
	}

	/** Wrap another command to change its condition */
	public CmdWrap(Cmd c, BooleanSupplier b) {
		this(c, b, c::doRun);
	}

	/** Wrap another command to change its action */
	public CmdWrap(Cmd c, Runnable r) {
		this(c, c::canRun, r);
	}

	/** Make command from condition and empty action */
	public CmdWrap(BooleanSupplier b) {
		this(Optional.empty(), b, () -> {});
	}

	/** Make command from action only, condition always holds true */
	public CmdWrap(Runnable r) {
		this(() -> true, r);
	}

	/** Make command from conditon and action */
	public CmdWrap(BooleanSupplier b, Runnable r) {
		this(Optional.empty(), b, r);
	}

	/** Wrap a command to change both condition and action */
	public CmdWrap(Cmd c, BooleanSupplier b, Runnable r) {
		this(Optional.of(c), b, r);
	}

	/** Internal constructor given an optional command, a condition and an action */
	private CmdWrap(Optional<Cmd> c, BooleanSupplier b, Runnable r) {
		this.origin = c;
		this.condition = b;
		this.action = r;
	}

	@Override
	public final boolean canRun() {
		return condition.getAsBoolean();
	}

	@Override
	public final void doRun() {
		action.run();
	}

	@Override
	public String toString() {
		String className = getClass().getSimpleName();
		return origin.map(c -> String.format("%s(%n%s%n)", className, c))
				.orElse(String.format("%s(%ncondition=%s%n, action=%s%n)", className, condition, action));
	}
}
