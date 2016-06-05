package lou.arane.core;

/**
 * Represent self-contained tasks that run to completion.
 *
 * @author Phuc
 */
public interface Cmd extends Runnable {

	/** Whether {@link #doRun()} has any chance of success */
	default boolean canRun() {
		return true;
	}

	/** Do the actual work with or without checking {@link #canRun()} */
	default void doRun() {}

	/** Check {@link #canRun()} before running */
	@Override
	default void run() {
		if (canRun()) {
			doRun();
		}
	}

}
