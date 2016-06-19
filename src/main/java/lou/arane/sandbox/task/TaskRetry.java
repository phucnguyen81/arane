package lou.arane.sandbox.task;

/**
 * Wrap a task to throw retry for it.
 *
 * @author Phuc
 */
public class TaskRetry implements Task {

    private final Task task;
    private final int retries;
    private final Exception cause;

    public TaskRetry(Task task, int maxRetries, Exception cause) {
        if (maxRetries <= 0) {
            throw new IllegalArgumentException("Retries must be positive, not " + maxRetries);
        }
        this.task = task;
        this.retries = maxRetries;
        this.cause = cause;
    }

    @Override
    public void run() throws Exception {
        try {
            task.run();
        }
        catch (Exception e) {
            e.addSuppressed(cause);
            int nextRetries = retries - 1;
            if (nextRetries <= 0) {
                throw new Exception("No more retries", e);
            }
            else {
                TaskRetry retry = new TaskRetry(task, nextRetries, e);
                throw new ErrorCanRetry(retry);
            }
        }
    }

}
