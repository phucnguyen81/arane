package lou.arane.scripts;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import lou.arane.util.Util;

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
        chapters = new ArrayList<>();
        Util.walk(dir).forEach(path -> {
            if (Files.isDirectory(path)){
                boolean hasAtLeastOneFile = Util.list(path).anyMatch(f -> Files.isRegularFile(f));
                if (hasAtLeastOneFile) chapters.add(new Chapter(path));
            }
        });
    }

    /** Make all paths within this story relative to a base target */
    public void makeRelativeTo(Path base) {
        dir = base.relativize(dir);
        for (Chapter chapter : chapters) {
            chapter.makeRelativeTo(base);
        }
    }
}