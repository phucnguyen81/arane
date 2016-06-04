package lou.arane.base;

/**
 * Represent self-contained tasks that run to completion.
 *
 * @author Phuc
 */
public interface Cmd extends Runnable {

	/** Whether {@link #doRun()} has any chance of success */
	boolean canRun();

	/** Do the actual work with or without checking {@link #canRun()} */
	void doRun();

	/** Check {@link #canRun()} before running */
	@Override
	default void run() {
		if (canRun()) {
			doRun();
		}
	}

}
