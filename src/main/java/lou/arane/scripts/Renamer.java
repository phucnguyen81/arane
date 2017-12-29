package lou.arane.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import lou.arane.util.Util;

public class Renamer {

    public static void main(String[] args) {
        Path root = Paths.get("/home/phuc/mangas/blogtruyen/Psyren/images");
        Util.list(root)
            .filter(Files::isDirectory)
            .forEach(dir -> {
                String dirName = dir.getFileName().toString();
                dirName = dirName.replace("psyren-chap-", "");
                dirName = Util.padNumericSequences(dirName, 3);
                Path target = dir.resolveSibling(dirName);
                System.out.println(dir + " -> " + target);
                rename(dir, target);
            });
    }

    private static void rename(Path source, Path target) {
        if (!Objects.equals(source.getParent(), target.getParent()))
            throw new IllegalArgumentException(
                target + " is not in the same directory as " + source);
        try {
            Files.move(source, target);
        } catch (IOException e) {
            String msg = "Failed to rename %s to %s";
            msg = String.format(msg, source, target);
            throw new RuntimeException(msg, e);
        }
    }
}
