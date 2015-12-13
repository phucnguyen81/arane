package lou.arane.util.script;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lou.arane.util.Check;
import lou.arane.util.Util;

/**
 * Organize files (images) into directories (volumes, chapters).
 * For example, we want to copy the image "c001_p001.jpg" to "c001/c001_p001.jpg".
 *
 * @author LOU
 */
public class CopyFiles {

    /** Test script */
    public static void main(String[] args) {
        Path imageDir = Paths.get("project/mangachrome/DrSlump/images");
        Pattern chapterPattern = Pattern.compile("c\\d+");
        new CopyFiles(imageDir, imageDir)
            .setDirPattern(chapterPattern)
            .padNumericSequences(3)
            .run();
    }

    /* directory containing the files to be copied */
    private final Path sourceDir;

    /* directory to copy the selected files to */
    private final Path targetDir;

    /* resolve directory name from file name */
    private Function<String, String> dirResolver;

    /* padded-length of numeric sequences found in filenames */
    private Integer padToLength;

    /**
     * Specify a source directory and a target directory to copy files to.
     */
    public CopyFiles(Path sourceDir, Path targetDir) {
        this.sourceDir = Check.notNull(sourceDir, "Null directory");
        this.targetDir = Check.notNull(targetDir, "Null target directory");
        setDirPattern(Pattern.compile("\\d+"));
    }

    /** @see #dirPattern */
    public CopyFiles setDirPattern(final Pattern dirPattern) {
        Check.notNull(dirPattern, "Null pattern");
        setDirResolver(fileName -> {
            Matcher matcher = dirPattern.matcher(fileName);
            return matcher.find() ? matcher.group() : null;
        });
        return this;
    }

    /** @see #dirResolver */
    public CopyFiles setDirResolver(Function<String, String> dirResolver) {
        this.dirResolver = Check.notNull(dirResolver, "Null function");
        return this;
    }

    /** @see #padToLength */
    public CopyFiles padNumericSequences(int padToLength) {
        Check.require(padToLength > 0, "Padding must be positive");
        this.padToLength = padToLength;
        return this;
    }

    /** Run the process of matching, grouping and copying files to target
     * directory */
    public void run() {
        Util.list(sourceDir)
            .filter(Util::isRegularFile)
            .forEach(sourcePath -> {
                String targetName = targetFileName(sourcePath);
                Path targetPath = targetPath(targetName);
                copyFile(sourcePath, targetPath);
            });
    }

    /**
     * @param sourcePath = e.g. c1_p01.jpg
     * @return e.g. c001_p001.jpg
     */
    private String targetFileName(Path sourcePath) {
        String targetName = sourcePath.getFileName().toString();
        if (padToLength != null) {
            targetName = Util.padNumericSequences(targetName, padToLength);
        }
        return targetName;
    }

    /**
     * @param targetFileName = e.g. c001_p001.jpg
     * @return e.g. c001/c001_p001.jpg
     */
    private Path targetPath(String targetFileName) {
        Path targetPath = Paths.get(targetFileName);
        String dirName = dirResolver.apply(targetFileName);
        if (dirName != null) {
            targetPath = Paths.get(dirName, targetFileName);
        }
        return targetDir.resolve(targetPath);
    }

    /** Copy a file to a directory; the target file-name is preserved */
    private static void copyFile(Path source, Path target) {
        if (Util.exists(target)) return;
        Util.createDirectories(target.getParent());
        Util.copy(source, target);
    }

}
