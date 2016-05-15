package lou.arane.base;

/**
 * Represent self-contained tasks that run to completion.
 *
 * @author Phuc
 */
public interface Command extends Runnable {

	/** Check {@link #canRun()} before running */
	@Override
	default void run() {
		if (canRun()) {
			doRun();
		}
	}

	/** Whether {@link #doRun()} has any chance of success */
	default boolean canRun() {
		return true;
	}

	/** Do the work with or without checking {@link #canRun()} */
	default void doRun() {}

}
