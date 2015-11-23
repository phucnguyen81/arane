package lou.arane.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * A story output model. A story has a name and chapters contained in a
 * directory.
 *
 * @author LOU
 */
public class Story {

    public Path dir;

    public String name;

    public final List<Chapter> chapters;

    public Story(Path dir) {
        this.dir = dir;
        name = dir.getFileName().toString();
        chapters = New.list();
        Util.walk(dir).forEach(path -> {
            if (Files.isDirectory(path)){
                boolean hasAtLeastOneFile = Util.list(path).anyMatch(f -> Files.isRegularFile(f));
                if (hasAtLeastOneFile) chapters.add(new Chapter(path));
            }
        });
    }

    /** Make all paths within this story relative to a base path */
    public void makeRelativeTo(Path base) {
        dir = base.relativize(dir);
        for (Chapter chapter : chapters) {
            chapter.makeRelativeTo(base);
        }
    }
}