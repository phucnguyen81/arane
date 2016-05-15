package lou.arane.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.List;

/**
 * URLs as used by this project
 *
 * @author pnguyen58
 */
public class Url {

    /** Alternate urls meant to locate the same resource as this url.
     * Not clean code but works for now */
    public final List<Url> alternatives = New.list();

    private final URL url;

    public Url(String url) {
    	this(url(url));
    }

    public Url(URL url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object other) {
    	if (other instanceof Url) {
    		return url.equals(((Url) other).url);
    	}
    	else {
    		return false;
    	}
    }

    @Override
    public String toString() {
    	return url.toString() +
    			(alternatives.isEmpty() ? "" : " " + alternatives.toString());
    }

    public String string() {
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
    	return url.getPath();
    }

	public static URL url(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			return nullURL();
		}
	}

	public static URL nullURL() {
		String url = "http:";
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new AssertionError(url + " should be accepted as url", e);
		}
	}

    public static boolean isWellFormed(String url) {
		try {
			new URL(url);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
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
