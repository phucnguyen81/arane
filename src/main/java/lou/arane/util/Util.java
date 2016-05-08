package lou.arane.util;

import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
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

/**
 * Facade over the standard library
 *
 * @author LOU
 */
public final class Util {

    /** default charset for I/O operations */
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    /** default encoding for I/O operation */
    public static final String ENCODING = CHARSET.name();

    /** system-dependent line separator */
    public static final String LINE_BREAK = System.lineSeparator();

    /** buffer size used for I/O operations */
    public static final int BUFFER_SIZE = 1024 * 8;

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

	public static String encodeUrl(String url) {
		try {
			return URLEncoder.encode(url, ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new Unchecked(e);
		}
	}

	public static String decodeUrl(String url) {
		try {
			return URLDecoder.decode(url, ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new Unchecked(e);
		}
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
            throw new Unchecked("Should never happen!", e);
        }
        return buffer.toString();
    }

    /** Assume a string is a path, remove its file extension if there is one */
    public static String removeFileExtension(String str) {
    	if (str == null) return null;
        int extensionIdx = str.lastIndexOf('.');
        if (extensionIdx >= 0) {
            return str.substring(0, extensionIdx);
        }
        return str;
    }

    /** Assume a string is a path, get its file extension (without the dot) */
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
            throw new Unchecked("user.home system property is not available");
        }
        Path userHomeDir = Paths.get(userHome);
        if (notExists(userHomeDir)) {
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

    /** Negation of {@link #exists(Path, LinkOption...)} */
    public static boolean notExists(Path path, LinkOption... options) {
    	return !exists(path, options);
	}

    public static boolean isRegularFile(Path path, LinkOption... options) {
        return Files.isRegularFile(path, options);
    }

    public static boolean isDirectory(Path path, LinkOption... options) {
        return Files.isDirectory(path, options);
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
            throw new Unchecked(e);
        }
    }

    /** Get direct files and sub-dirs */
    public static Stream<Path> list(Path path) {
        if (isDirectory(path)) {
            try {
                return Files.list(path);
            }
            catch (IOException e) {
                throw new Unchecked(e);
            }
        }
        else {
            return New.emptyStream();
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

    /** Copy a file/dir to a target file/dir.
     * Intermediate directories are created if they do not exist.
     * @see Files#copy(Path, Path, CopyOption...) */
    public static void copy(Path source, Path target, CopyOption... options) {
        createDirectories(target.getParent());

        try {
            Files.copy(source, target, options);
        } catch (IOException e) {
            throw new Unchecked(e);
        }

        //recursively copy sub-files/dirs
        list(source).forEach(src -> {
            Path rel = source.relativize(src);
            Path dst = target.resolve(rel);
            copy(src, dst);
        });
    }

    /** Write/Overwrite a file with default settings */
    public static void write(Path path, String content) {
        write(path, content, CHARSET);
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

    /** Get string representation of an exception's stack trace */
    public static String toString(Throwable error) {
        ByteArrayOutputStream stackTrace = new ByteArrayOutputStream();
        boolean autoFlushFlag = false;
        try {
            PrintStream printer = new PrintStream(stackTrace, autoFlushFlag, ENCODING);
            error.printStackTrace(printer);
            return stackTrace.toString(ENCODING);
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
				return Util.findIter(input, pattern);
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

	public static Response getUrl(String urlStr, Duration timeout) {
		Response r = new Response();
		try {
			URL url = new URL(urlStr);
			return get(url, timeout);
		} catch (IOException e) {
			r.exception = e;
		}
		return r;
	}

	public static Response get(URL url, Duration timeout) {
		Response r = new Response();
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			try (Closeable disconnect = () -> conn.disconnect()) {
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept-Charset", Util.ENCODING);
				conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1");
				conn.setConnectTimeout((int) timeout.toMillis());
				conn.setReadTimeout((int) timeout.toMillis());

				r.content = Util.read(conn.getInputStream());
				r.code = conn.getResponseCode();
				if (r.code < 200 || r.code > 299) {
					r.exception = new IOException(String.format(
						"Error code is %s for getting %s", r.code, url));
				}
			}
		} catch (IOException e) {
			r.exception = e;
		}
		return r;
	}

	public static class Response {
		public IOException exception;
		public Integer code;
		public byte[] content;
	}

	/** Read then close a byte stream */
	public static final byte[] read(InputStream in) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			copy(in, out);
			return out.toByteArray();
		}
	}

	/**
	 * Reads all bytes from an input stream and writes them to an output stream.
	 * NOTE: this is taken from {@link Files#copy(InputStream, OutputStream)}
	 *
	 * @return the number of bytes copied
	 */
    public static long copy(InputStream source, OutputStream sink)
        throws IOException
    {
        long nread = 0L;
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }

}