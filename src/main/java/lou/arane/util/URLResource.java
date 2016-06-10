package lou.arane.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.MINUTES;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

	/** A url needs to be encoded if it constains
	 * illegal source characters (e.g. spaces) */
	public static String encode(String url) {
		return url.codePoints()
			.mapToObj(Character::toChars)
			.map(String::new)
			.map(c ->
				(  c.equals(":")
				|| c.equals("/")
				|| c.equals("?")
				|| c.equals("#")
				|| c.equals("&")
				)
				? c
				: encodeWithEncoder(c)
			)
			.collect(Collectors.joining());
	}

	/** Encode url using default encoding and unchecked exception */
	private static String encodeWithEncoder(String url) {
		try {
			return URLEncoder.encode(url, IO.encoding());
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(IO.encoding() + " should be supported!", e);
		}
	}

    /** Alternate urls meant to locate the same resource as this url*/
    private final Collection<URLResource> alternatives;

    private final URL url;

    public URLResource(URL url) {
        this(url, Collections.emptyList());
    }

    public URLResource(URLResource url, Collection<URLResource> alternatives) {
    	this(url.url, alternatives);
    }

    public URLResource(URL url, Collection<URLResource> alternatives) {
        try {
			this.url = new URL(url.toExternalForm());
		} catch (MalformedURLException e) {
			throw New.unchecked(e);
		}
        this.alternatives = Collections.unmodifiableCollection(alternatives);
    }

    public Collection<URLResource> alternatives() {
    	return alternatives;
    }

    public List<URLResource> plusAlternatives() {
        List<URLResource> urls = New.list();
        urls.add(this);
        urls.addAll(alternatives());
        return urls;
    }

    public String externalForm() {
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

    /**
     * Make GET request assuming the protocol is http.
     */
    public HttpResponse httpGET() {
    	try {
			HttpURLConnection conn;
			conn = IO.httpGET(url, UTF_8, Duration.of(1, MINUTES));
            return new HttpResponse(conn);
		} catch (Exception e) {
			throw New.unchecked(e);
		}
    }

	@Override
    public boolean equals(Object other) {
    	return url.equals(((URLResource) other).url);
    }

    @Override
    public String toString() {
    	return String.format("%s%n  alternatives:%s"
    		, url, Util.joinLines(alternatives)
    	);
    }

}
