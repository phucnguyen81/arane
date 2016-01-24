package lou.arane.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

/**
 * Facade over the standard library
 *
 * @author LOU
 */
public final class Util {

    public static void println(Object message) {
        System.out.println(message);
    }

    public static void printlnErr(Object message) {
        System.err.println(message);
    }

    public static String normalizeNewlines(String text) {
        if (text != null) {
            StringBuilder buffer = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
                String line = reader.readLine();
                for (; line != null; line = reader.readLine()) {
                    buffer.append(line).append(lineSeparator());
                }
            }
            catch (IOException e) {
                throw new IllegalStateException("Should never happen!", e);
            }
            text = buffer.toString();
        }
        return text;
    }

    /** Assume a string is a path, remove its file extension */
    public static String removeFileExtension(String str) {
        if (str != null) {
            int extensionIdx = str.lastIndexOf('.');
            if (extensionIdx >= 0) {
                str = str.substring(0, extensionIdx);
            }
        }
        return str;
    }

    /** Assume a string is a path, get its file extension (without the dot) */
    public static String getFileExtension(String str) {
        if (str == null) {
            return null;
        }
        int extensionIdx = str.lastIndexOf('.');
        if (extensionIdx >= 0) {
            return str.substring(0, extensionIdx + 1);
        }
        return null;
    }

    /** Common way to get a base directory for downloading a manga. */
    public static Path mangaDir(String first, String... more) {
        Path mangasDir = userHomeDir().resolve("mangas");
        Path baseDir = mangasDir.resolve(Paths.get(first, more));
        return baseDir;
    }

    /** Common user home directory; should work for many environments */
    public static Path userHomeDir() {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            throw new Unchecked("user.home system property is not available");
        }
        Path userHomeDir = Paths.get(userHome);
        if (!exists(userHomeDir)) {
            throw new Unchecked("User home directory does not exist at " + userHomeDir);
        }
        return userHomeDir;
    }

    public static Path createDirectories(Path dir, FileAttribute<?>... attrs) {
        try {
            return Files.createDirectories(dir, attrs);
        }
        catch (IOException e) {
            throw new Unchecked(e);
        }
    }

    public static void delete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new Unchecked(e);
        }
    }

    /**
     * Delete a file, a symbolic link or an empty directory if it exists;
     * return whether the file is deleted.
     */
    public static boolean deleteIfExists(Path path) {
        try {
            return Files.deleteIfExists(path);
        }
        catch (IOException e) {
            throw new Unchecked(e);
        }
    }

    /** Check if the absolute version of a path exists,
     * which means the underlying file/folder exists */
    public static boolean exists(Path path, LinkOption... options) {
        return Files.exists(path.toAbsolutePath(), options);
    }

    public static boolean isRegularFile(Path path, LinkOption... options) {
        return Files.isRegularFile(path, options);
    }

    public static boolean isDirectory(Path path, LinkOption... options) {
        return Files.isDirectory(path, options);
    }

    public static Path renameDirectory(Path dir, String newName) {
        return move(dir, dir.resolveSibling(newName));
    }

    /** @see Files#move(Path, Path, CopyOption...) */
    public static Path move(Path source, Path target, CopyOption... options) {
        try {
            return Files.move(source, target, options);
        }
        catch (IOException e) {
            throw new Unchecked(e);
        }
    }

    /** Get direct files and sub-dirs */
    public static Stream<Path> list(Path path) {
        try {
            return Files.list(path);
        }
        catch (IOException e) {
            throw new Unchecked(e);
        }
    }

    /** Get all files and dirs under a start path */
    public static Stream<Path> walk(Path start, FileVisitOption... options) {
        try {
            return Files.walk(start, options);
        }
        catch (IOException e) {
            throw new Unchecked(e);
        }
    }

    /** Copy a file to a target file.
     * @see Files#copy(Path, Path, CopyOption...) */
    public static void copy(Path source, Path target, CopyOption... options) {
        try {
            Files.copy(source, target, options);
        } catch (IOException e) {
            throw new Unchecked(e);
        }
    }

    /** Write/Overwrite a file with default settings */
    public static void write(Path path, String content) {
        write(path, content, defaultCharset());
    }

    /** Write/Overwrite a file with default settings */
    public static void write(Path path, String content, Charset charset) {
        write(path, content.getBytes(charset));
    }

    /** Write to a file with default behaviors: create all parent directories if
     * they do not exist, create the file if it does not exist, or initially
     * truncating an existing file to a size of 0. */
    public static void write(Path path, byte[] bytes) {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
        }
        catch (IOException e) {
            throw new Unchecked(e);
        }
    }

    /** Join string representation of given parts.
     * The parts are separated by the given separator. */
    public static String join(Iterable<?> parts, String separator) {
        return Joiner.on(separator).join(parts);
    }

    /** Given a string, pad numeric sequences shorter
     * than a minimum length found in that string. */
    public static String padNumericSequences(String str, int minLength) {
        // pattern to look for numeric sequences shorter than a minimum length
        Pattern notPadded = Pattern.compile("(\\A|\\D)(\\d{1," + (minLength - 1) + "})(\\z|\\D)");

        // replace numeric sequences with their padded versions
        Matcher notPaddedMatcher = notPadded.matcher(str);
        for (; notPaddedMatcher.find(); notPaddedMatcher = notPadded.matcher(str)) {
            String padded = padStart(notPaddedMatcher.group(2), minLength, '0');
            padded = notPaddedMatcher.group(1) + padded + notPaddedMatcher.group(3);
            padded = Matcher.quoteReplacement(padded);
            str = notPaddedMatcher.replaceFirst(padded);
        }
        return str;
    }

    /** Get a padded string of length at least minLength */
    public static String padStart(String string, int minLength, char padChar) {
        return Strings.padStart(string, minLength, padChar);
    }

    /** Get string representation of an exception's stack trace */
    public static String toString(Throwable error) {
        ByteArrayOutputStream stackTrace = new ByteArrayOutputStream();
        String encoding = defaultEncoding();
        boolean autoFlushFlag = false;
        try {
            PrintStream printer = new PrintStream(stackTrace, autoFlushFlag, encoding);
            error.printStackTrace(printer);
            return stackTrace.toString(encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Should never happen!", e);
        }
    }

    public static String defaultEncoding() {
        return defaultCharset().name();
    }

    public static Charset defaultCharset() {
        return StandardCharsets.UTF_8;
    }

    public static String lineSeparator() {
        return System.lineSeparator();
    }

    /** Generate a list of numbers as strings, e.g range(1,3) = ["1","2","3"] */
    public static LinkedList<String> rangeClosed(int startInclusive, int endInclusive) {
    	LinkedList<String> range = new LinkedList<String>();
    	for (int i = startInclusive; i <= endInclusive; i++) {
    		range.add(String.valueOf(i));
    	}
    	return range;
    }

    /** @see Util#parseHtml(Path, String) */
    public static Document parseHtml(Path path, Uri baseUri) {
    	return Util.parseHtml(path, baseUri.toString());
    }

    /**
     * Parse html file and return its html model. The base uri is set on the
     * model to resolve urls
     */
    public static Document parseHtml(Path path, String baseUri) {
    	Document html = Util.parseHtml(path);
    	html.setBaseUri(baseUri);
    	return html;
    }

    /** Parse html file and return its html model. */
    public static Document parseHtml(Path path) {
    	String defaultCharsetName = null;
    	try {
    		return Jsoup.parse(path.toFile(), defaultCharsetName);
    	} catch (IOException e) {
    		throw new Unchecked("Failed to parse html file " + path, e);
    	}
    }

    /** Find html files of a given directory */
    public static List<Path> findHtmlFiles(Path dir) {
    	return list(dir)
    	        .filter(Util::isRegularFile)
    			.filter(file -> file.toString().endsWith(".html"))
    			.collect(Collectors.toList());
    }

    /** Find all html files in the directory tree rooted at a given directory */
    public static List<Path> findAllHtmlFiles(Path dir) {
        return walk(dir)
                .filter(Util::isRegularFile)
                .filter(file -> file.toString().endsWith(".html"))
                .collect(Collectors.toList());
    }
}