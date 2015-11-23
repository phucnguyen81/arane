package lou.arane.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Chapter {
    public Path dir;
    public List<Path> images;

    public Chapter(Path dir) {
        this.dir = dir;
        images = Util.list(dir)
            .filter(p -> Files.isRegularFile(p))
            .collect(Collectors.toList());
    }

    public void makeRelativeTo(Path base) {
        dir = base.relativize(dir);
        images = images.stream()
            .map(img -> base.relativize(img))
            .collect(Collectors.toList());
    }

    public List<String> getImageUris() {
        return images.stream()
            .map(img -> Util.join(img, "/"))
            .collect(Collectors.toList());
    }
}