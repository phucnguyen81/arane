package lou.arane.commands.decor;

import lou.arane.base.Command;

/**
 * Wrap a command to do something else.
 *
 * @author Phuc
 */
public final class Decorator implements Command {

	private final Command origin;
	private final Runnable replacement;

	public Decorator(Command origin, Runnable replacement) {
		this.origin = origin;
		this.replacement = replacement;
	}

	@Override
	public boolean canRun() {
		return origin.canRun();
	}

	@Override
	public void doRun() {
		replacement.run();
	}

	@Override
	public String toString() {
		return String.format(
			"{origin: %s%n  replacement: %s%n}"
			, origin, replacement);
	}
}
