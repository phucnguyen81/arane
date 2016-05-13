package lou.arane.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * Uris as they are used by this project
 *
 * @author pnguyen58
 */
public class Uri implements Comparable<Uri> {

	/**
	 * When a url constains illegal uri characters (e.g. spaces),
	 * the url needs to be encoded to make a valid uri.
	 */
	public static Uri fromUrl(String url) {
		StringBuilder encoded = new StringBuilder();
		for (int codepoint : url.codePoints().toArray()) {
			String str = new String(Character.toChars(codepoint));
			if (codepoint != ':'
				&& codepoint != '/'
				&& codepoint != '?'
				&& codepoint != '#'
				&& codepoint != '&'
			) {
				str = encodeUrl(str);
			}
			encoded.append(str);
		}
		return of(encoded.toString());
	}

	public static Uri of(String uri) {
        return of(URI.create(uri));
    }

    public static Uri of(URI uri) {
        return new Uri(uri);
    }

	public static String encodeUrl(String url) {
		try {
			return URLEncoder.encode(url, IO.ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	public static String decodeUrl(String url) {
		try {
			return URLDecoder.decode(url, IO.ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

    /* alternate uris meant to locate the same resource as this uri */
    private final LinkedList<Uri> alternatives = New.linkedList();

    private final URI uri;

    private Uri(URI uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return uri.toString() +
            (alternatives.isEmpty() ? "" : " " + alternatives.toString());
    }

    public String toUriString() {
    	return uri.toString();
    }

    @Override
    public int compareTo(Uri other) {
        return uri.compareTo(other.uri);
    }

    @Override
    public boolean equals(Object other) {
        return compareTo(((Uri) other)) == 0;
    }

    public void addAlternatives(Uri uri) {
        alternatives.add(uri);
    }

    public LinkedList<Uri> getAlternatives() {
        return alternatives;
    }

    public Uri resolve(String str) {
        return new Uri(uri.resolve(str));
    }

    public String getQuery() {
        return uri.getQuery();
    }

    public String getFileExtension() {
    	String fileName = getFileName();
        if (fileName == null) {
            return null;
        }
        String ext = Util.getFileExtension(fileName);
        return ext == null ? "" : ext;
    }

    public String getFileName() {
        String path = getFilePath();
        if (path == null) return null;
        return Paths.get(path).getFileName().toString();
    }

    public String getFilePath() {
    	return uri.getPath();
    }

    public String getPath() {
        return uri.getPath();
    }
}
