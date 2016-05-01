package lou.arane.core;

/** Contract for handling a task */
public interface Handler {
	boolean canRun();
	void doRun();
}
