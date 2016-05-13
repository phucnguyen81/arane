package lou.arane.core;

/** Contract for handling a task */
public interface Handler {

	/** Whether {@link #doRun()} has any chance of success */
	boolean canRun();

	/** Do the work */
	void doRun();

}
