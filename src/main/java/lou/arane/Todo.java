package lou.arane;

import lou.arane.project.MangaTakoDownloader;
import lou.arane.util.http.HttpFileBatchDownloader;

/**
 * Task list
 *
 * @author LOU
 */
public class Todo {

    /**
     * Except for {@link MangaTakoDownloader}, other downloaders pad numeric
     * sequences for the entire image file names. Although this is a simple way
     * to order the images lexicographically, it changes manga names that have
     * numneric sequences in it. For example, 3x3_Eyes is padded to
     * 003x003_Eyes, which is not desired.
     * <p>
     * TODO avoid padding the manga names when padding image names
     */
    public static void todo_1() {
        see(MangaTakoDownloader.class);
    }

    /**
     * Sometimes we want to clear the target files before downloading new ones.
     * <p>
     * TODO handle existing target paths
     */
    public static void todo_2() {
        see(HttpFileBatchDownloader.class);
    }

    private static void see(Object... what) {}
}
