package lou.arane.sandbox.task;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Composite of {@link Task}
 *
 * @author Phuc
 */
public final class TaskCombo implements Task {

    private final List<Task> tasks;

    public TaskCombo(Iterable<Task> combo) {
        this.tasks = new ArrayList<>();
        combo.forEach(this.tasks::add);
    }

    /**
     * Simply try to run all tasks.
     *
     * @throws Exception of the first failed task
     */
    @Override
    public void run() throws Exception {
        for (Task t : tasks) {
            t.run();
        }
    }

    /**
     * Run the first task that satisfies pre-condition, i.e. does not throw
     * {@link ErrorPrecondition}. If all tasks throw pre-condition errors then
     * throw a pre-condition error itself.
     */
    public void runFirstValid() throws Exception {
        List<ErrorPrecondition> suppressed = new ArrayList<>();
        for (Task t : tasks) {
            try {
                t.run();
                return;
            }
            catch (ErrorPrecondition e) {
                suppressed.add(e);
            }
        }
        if (!suppressed.isEmpty()) {
            ErrorPrecondition p = new ErrorPrecondition("No tasks satisty pre-condition");
            suppressed.forEach(p::addSuppressed);
            throw p;
        }
    }

    /**
     * Run each task until the first one succeeds. Retry task if needed.
     *
     * @throws Exception if all tasks have failed
     */
    public void runFirstSuccess() throws Exception {
        List<Exception> suppressed = new ArrayList<>();
        Deque<Task> queue = new ArrayDeque<>(tasks);
        while (!queue.isEmpty()) {
            Task t = queue.removeFirst();
            try {
                t.run();
                return;
            }
            catch (ErrorCanRetry r) {
                // re-try right away
                queue.addFirst(r.retry());
            }
            catch (Exception e) {
                suppressed.add(e);
            }
        }
        if (!suppressed.isEmpty()) {
            Exception e = new Exception("All tasks have failed.");
            suppressed.forEach(e::addSuppressed);
            throw e;
        }
    }

    /**
     * Try to run each task at least one. Retry tasks if needed.
     *
     * @throws Exception if some task has failed
     */
    public void runAll() throws Exception {
        List<Exception> suppressed = new ArrayList<>();
        Deque<Task> queue = new ArrayDeque<>(tasks);
        while (!queue.isEmpty()) {
            Task t = queue.removeFirst();
            try {
                t.run();
            }
            catch (ErrorCanRetry r) {
                // re-try later
                queue.addLast(r.retry());
            }
            catch (Exception e) {
                // suppressed failed task
                suppressed.add(e);
            }
        }
        if (!suppressed.isEmpty()) {
            Exception e = new Exception("Some task has failed.");
            suppressed.forEach(e::addSuppressed);
            throw e;
        }
    }

}
