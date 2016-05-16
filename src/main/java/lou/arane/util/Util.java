package lou.arane.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.StringJoiner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import lou.arane.base.URLResource;

/**
 * Facade over the standard library
 *
 * @author LOU
 */
public final class Util {

    /** system-dependent line separator */
    public static final String LINE_BREAK = System.lineSeparator();

    public static void println(Object message) {
        System.out.println(message);
    }

    public static void printlnErr(Object message) {
        System.err.println(message);
    }

    /** Null or empty? */
	public static boolean isBlank(CharSequence s) {
		return s == null || s.length() == 0;
	}

	/** Convert all newlines to platform-specific newlines.
	 * Not good on performance, just need to be simple enough. */
    public static String normalizeNewlines(String text) {
    	if (text == null) return null;
        StringBuilder buffer = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
        	reader.lines().forEach(line -> buffer.append(line).append(LINE_BREAK));
        }
        catch (IOException e) {
            throw new AssertionError("Should never happen!", e);
        }
        return buffer.toString();
    }

	/** Remove a part of a string if it ends with that part */
	public static String removeEnding(String str, String ending) {
		if (str.endsWith(ending)) {
			return str.substring(0, str.length() - ending.length());
		} else {
			return str;
		}
	}

    /** Assume a string is a target, remove its file extension if there is one */
    public static String removeFileExtension(String str) {
    	if (str == null) return null;
        int extensionIdx = str.lastIndexOf('.');
        if (extensionIdx >= 0) {
            return str.substring(0, extensionIdx);
        }
        return str;
    }

    /** Assume a string is a target, get its file extension (without the dot) */
    public static String getFileExtension(String str) {
        if (str == null) return null;
        int extensionIdx = str.lastIndexOf('.');
        if (extensionIdx >= 0) {
            return str.substring(extensionIdx + 1);
        }
        return null;
    }

    /** Common user home directory;
     * should work on both Windows/Unix (not tested on Unix though) */
    public static Path userHomeDir() {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            throw new AssertionError("user.home system property is not available");
        }
        Path userHomeDir = Paths.get(userHome);
        if (notExists(userHomeDir)) {
            throw new AssertionError("User home directory does not exist at " + userHomeDir);
        }
        return userHomeDir;
    }

	public static void createFileIfNotExists(Path file) {
		if (notExists(file)) {
			createDirectories(file.getParent());
			createFile(file);
		}
	}

    public static void createFile(Path path, FileAttribute<?>... attrs) {
		try {
			Files.createFile(path, attrs);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Path createDirectories(Path dir, FileAttribute<?>... attrs) {
        try {
            return Files.createDirectories(dir, attrs);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static void delete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new AssertionError(e);
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
            throw new AssertionError(e);
        }
    }

    /** Check if the absolute version of a target exists,
     * which means the underlying file/folder exists */
    public static boolean exists(Path path, LinkOption... options) {
        return Files.exists(path.toAbsolutePath(), options);
    }

    /** Negation of {@link #exists(Path, LinkOption...)} */
    public static boolean notExists(Path path, LinkOption... options) {
    	return !exists(path, options);
	}

    public static boolean isNotEmpty(Path p) {
        return list(p).findAny().isPresent();
    }

    public static boolean isEmpty(Path p) {
        return !isNotEmpty(p);
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
            throw new AssertionError(e);
        }
    }

    /** Get direct files and sub-dirs */
    public static Stream<Path> list(Path path) {
        if (Files.isDirectory(path)) {
            try {
                return Files.list(path);
            }
            catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        else {
        	return Stream.empty();
        }
    }

    /** Get all files and dirs under a start target */
    public static Stream<Path> walk(Path start, FileVisitOption... options) {
        try {
            return Files.walk(start, options);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /** Copy a file/dir to a target file/dir.
     * Intermediate directories are created if they do not exist.
     * @see Files#copy(Path, Path, CopyOption...) */
    public static void copy(Path source, Path target, CopyOption... options) {
        createDirectories(target.getParent());

        try {
            Files.copy(source, target, options);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        //recursively copy sub-files/dirs
        list(source).forEach(src -> {
            Path rel = source.relativize(src);
            Path dst = target.resolve(rel);
            copy(src, dst);
        });
    }

    /** Join string representation of given parts.
     * The parts are separated by the given separator. */
    public static String join(Iterable<?> parts, CharSequence separator) {
    	StringJoiner joiner = new StringJoiner(separator);
    	for (Object part : parts) {
    		joiner.add(part == null ? "" : part.toString());
    	}
        return joiner.toString();
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
    public static String padStart(String s, int minLength, char padChar) {
    	StringBuilder padded = new StringBuilder();
    	for (int i = s.length(); i < minLength; i++) {
    		padded.append(padChar);
    	}
    	padded.append(s);
        return padded.toString();
    }

    /** Get string representation of an error's stack trace */
    public static String toString(Throwable error) {
        ByteArrayOutputStream stackTrace = new ByteArrayOutputStream();
        boolean autoFlushFlag = false;
        try {
            PrintStream printer = new PrintStream(stackTrace, autoFlushFlag, IO.ENCODING);
            error.printStackTrace(printer);
            return stackTrace.toString(IO.ENCODING);
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Should never happen!", e);
        }
    }

    /** Generate a list of numbers as strings, e.g range(1,3) = ["1","2","3"] */
    public static LinkedList<String> rangeClosed(int startInclusive, int endInclusive) {
    	LinkedList<String> range = new LinkedList<>();
    	for (int i = startInclusive; i <= endInclusive; i++) {
    		range.add(String.valueOf(i));
    	}
    	return range;
    }

    /** @see Util#parseHtml(Path, String) */
    public static Document parseHtml(Path path, URLResource baseUri) {
    	return parseHtml(path, baseUri.toString());
    }

    /**
     * Parse html file and return its html model. The base source is set on the
     * model to resolve urls
     */
    public static Document parseHtml(Path path, String baseUri) {
    	Document html = parseHtml(path);
    	html.setBaseUri(baseUri);
    	return html;
    }

    /** Parse html file and return its html model. */
    public static Document parseHtml(Path path) {
    	String defaultCharsetName = null;
    	try {
    		return Jsoup.parse(path.toFile(), defaultCharsetName);
    	} catch (IOException e) {
    		throw new AssertionError("Failed to parse html file " + path, e);
    	}
    }

    /** Find html files of a given directory */
    public static List<Path> findHtmlFiles(Path dir) {
    	return list(dir)
    	        .filter(Files::isRegularFile)
    			.filter(file -> file.toString().endsWith(".html"))
    			.collect(Collectors.toList());
    }

    /** Find all html files in the directory tree rooted at a given directory */
    public static List<Path> findAllHtmlFiles(Path dir) {
        return walk(dir)
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".html"))
                .collect(Collectors.toList());
    }

    /** Use a pattern to collect and index matches found in input */
	public static Map<Integer, MatchResult> findIndexed(CharSequence input, Pattern pattern) {
		Map<Integer, MatchResult> matches = new LinkedHashMap<>();
		Matcher matcher = pattern.matcher(input);
		Integer index = 0;
		while (matcher.find()) {
			MatchResult match = matcher.toMatchResult();
			matches.put(index, match);
			index += 1;
		}
		return matches;
	}

	public static Iterable<Entry<Integer, MatchResult>> find(CharSequence input, Pattern pattern) {
		return new Iterable<Entry<Integer, MatchResult>>() {
			@Override
			public Iterator<Entry<Integer, MatchResult>> iterator() {
				return findIter(input, pattern);
			}
		};
	}

	public static Iterator<Entry<Integer, MatchResult>> findIter(CharSequence input, Pattern pattern) {
		return new Iterator<Entry<Integer, MatchResult>>() {
			Matcher matcher = pattern.matcher(input);
			boolean hasNext = matcher.find();
			int index = 0;
			@Override
			public boolean hasNext() {
				return hasNext;
			}
			@Override
			public Entry<Integer, MatchResult> next() {
				if (hasNext) {
					MatchResult result = matcher.toMatchResult();
					hasNext = matcher.find();
					return new AbstractMap.SimpleEntry<>(index++, result);
				}
				else {
					throw new NoSuchElementException("No more pattern " + pattern + " found in " + input);
				}
			}
		};
	}
}