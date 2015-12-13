package lou.arane.util.script;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lou.arane.util.Util;

/**
 * Do some simple file processing tasks: rename, remove, ect.  
 *
 * @author LOU
 */
public class ProcessFiles {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");
    
    public static void main(String[] args) {
        Path dir = Paths.get("C:/Ban Long");
        Util.list(dir)
            .filter(p -> Util.isDirectory(p))
            .forEach(p -> renameDirectory(p));
    }

    /** rename dir to keep only its numeric part */
    static void renameDirectory(Path dir) {
        String match = null;
        Matcher matcher = NUMERIC_PATTERN.matcher(dir.getFileName().toString());
        while (matcher.find()) match = matcher.group();
        if (match != null) {
            match = Util.padNumericSequences(match, 3);
            Util.renameDirectory(dir, match);
        }
    }

    /** whether a file padded with 0 already exists;
     * e.g. '1.jpg' is a duplicate of '01.jpg' */
    static boolean isDuplicatePath(Path p) {
        String fileName = p.getFileName().toString();
        return Util.exists(p.resolveSibling("0" + fileName))
            || Util.exists(p.resolveSibling("00" + fileName));
    }

}
