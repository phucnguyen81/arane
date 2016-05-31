package lou.arane.base;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import lou.arane.util.IO;
import lou.arane.util.Util;

/**
 * URLs as used by this project
 *
 * @author pnguyen58
 */
public class URLResource {

    /** Make instance or empty if url is malformed */
    public static Optional<URLResource> of(String url) {
    	try {
			return Optional.of(new URLResource(new URL(url)));
		} catch (MalformedURLException e) {
			return Optional.empty();
		}
    }

    /** Alternate urls meant to locate the same resource as this url.
     * Not clean code but works for now */
    private final Collection<URLResource> alternatives;

    private final URL url;

    public URLResource(URL url) {
        this(url, Collections.emptyList());
    }

    public URLResource(URLResource url, Collection<URLResource> alternatives) {
    	this(url.url, alternatives);
    }

    public URLResource(URL url, Collection<URLResource> alternatives) {
        this.url = url;
        this.alternatives = Collections.unmodifiableCollection(alternatives);
    }

    @Override
    public boolean equals(Object other) {
    	return url.equals(((URLResource) other).url);
    }

    @Override
    public String toString() {
    	return String.format("%s%n  alternatives=%s"
    			, url
    			, Util.join(alternatives, Util.NEWLINE));
    }

    public Collection<URLResource> alternatives() {
    	return alternatives;
    }

    public String string() {
    	return url.toExternalForm();
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
			return URLEncoder.encode(url, IO.defaultEncoding());
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(IO.defaultEncoding() + " should be supported!", e);
		}
	}
}
