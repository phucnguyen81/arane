package lou.arane.base;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lou.arane.util.IO;
import lou.arane.util.Util;

/**
 * URLs as used by this project
 *
 * @author pnguyen58
 */
public class URLResource {

    /** Alternate urls meant to locate the same resource as this url.
     * Not clean code but works for now */
    private final List<URLResource> alternatives;

    private final URL url;

    /** Make instance or empty if url is malformed */
    public static Optional<URLResource> of(String url) {
    	try {
			return Optional.of(new URLResource(new URL(url)));
		} catch (MalformedURLException e) {
			return Optional.empty();
		}
    }
    
    public URLResource(URL url) {
        this(url, Collections.emptyList());
    }

    public URLResource(URLResource url, Iterable<URLResource> alternatives) {
    	this(url.url, alternatives);
    }

    public URLResource(URL url, Iterable<URLResource> alternatives) {
        this.url = url;
        this.alternatives = StreamSupport
        		.stream(alternatives.spliterator(), false)
        		.collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object other) {
    	return url.equals(((URLResource) other).url);
    }

    @Override
    public String toString() {
    	return String.format("%s%n  alternatives=%s"
    			, url
    			, Util.join(alternatives, Util.LINE_BREAK));
    }

    public List<URLResource> alternatives() {
    	return alternatives;
    }

    public String urlString() {
    	return url.toString();
    }

    /** Return the query part of empty string */
    public String query() {
        String q = url.getQuery();
        return q == null ? "" : q;
    }

    public String fileExtension() {
    	String fileName = fileName();
        return Util.getFileExtension(fileName);
    }

    public String fileName() {
        String path = filePath();
        return Paths.get(path).getFileName().toString();
    }

    public String filePath() {
    	String p = url.getPath();
    	return p == null ? "" : p;
    }

	/** A url needs to be encoded if it constains
	 * illegal source characters (e.g. spaces) */
	public static String encode(String url) {
		StringBuilder encoded = new StringBuilder();
		for (int codepoint : url.codePoints().toArray()) {
			String chars = new String(Character.toChars(codepoint));
			if (   codepoint == ':'
				|| codepoint == '/'
				|| codepoint == '?'
				|| codepoint == '#'
				|| codepoint == '&'
			) {
				encoded.append(chars);
			} else {
				String charsEncoded = encodeWithEncoder(chars);
				encoded.append(charsEncoded);
			}
		}
		return encoded.toString();
	}

	/** Encode url using default encoding and unchecked exception */
	private static String encodeWithEncoder(String url) {
		try {
			return URLEncoder.encode(url, IO.ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(IO.ENCODING + " should be supported!", e);
		}
	}
}
