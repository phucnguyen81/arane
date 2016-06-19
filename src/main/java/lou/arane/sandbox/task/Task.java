package lou.arane.sandbox.task;

/**
 * A self-sufficient process carried out under a context. Results are returned
 * to caller via throwing exceptions.
 * <p>
 * TODO consider adding context (logging, html matching, ect.)
 *
 * @author Phuc
 */
public interface Task {

    void run() throws Exception;

    /**
     * Thrown when pre-condition for running is not satisfied. In this case, the
     * task has not been run at all. In general this means the task should not
     * be retried.
     */
    @SuppressWarnings("serial")
    class ErrorPrecondition extends Exception {
        ErrorPrecondition(String msg) {
            super(msg);
        }

        public ErrorPrecondition(String string, Exception cause) {
            super(string, cause);
        }
    }

    /**
     * Thrown when a task has failed but can be retried. e.g. disconnected to
     * the cloud. This can be used as the retry-task itself.
     */
    @SuppressWarnings("serial")
    class ErrorCanRetry extends Exception {
        private final TaskRetry retry;

        ErrorCanRetry(Task task, int maxRetries, Exception cause) {
            this(new TaskRetry(task, maxRetries, cause));
        }

        ErrorCanRetry(TaskRetry retry) {
            this.retry = retry;
        }

        TaskRetry retry() {
            return retry;
        }
    }

}
