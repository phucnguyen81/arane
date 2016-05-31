package lou.arane.url;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import lou.arane.util.HttpResponse;
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

    public Collection<URLResource> alternatives() {
    	return alternatives;
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

    /** Download to a file assuming the protocol is http.
	 * If there is no exception, the right content should be downloaded to the file. */
    public void httpDownload(Path file, Duration timeout) {
		Util.createFileIfNotExists(file);
		try ( HttpResponse response = httpGET(IO.charset(), timeout)
	    	; OutputStream output = new BufferedOutputStream(
	    		Files.newOutputStream(file))
	    ){
			if (response.hasErrorCode()) {
				throw new RuntimeException(String.format(
					"Error code is %s for getting %s", response.code, url));
	    	} else {
	    		response.copyTo(output);
	    	}
	    }
		catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    /** Make a GET request assuming the protocol is http */
    public HttpResponse httpGET(Charset charset, Duration timeout) {
		try {
			HttpURLConnection conn;
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept-Charset", charset.name());
			conn.setConnectTimeout((int) timeout.toMillis());
			conn.setReadTimeout((int) timeout.toMillis());
			// pretend to be Mozilla since some server might check it
			conn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1");
			return new HttpResponse(conn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    @Override
    public boolean equals(Object other) {
    	return url.equals(((URLResource) other).url);
    }

    @Override
    public String toString() {
    	return String.format("%s%n  alternatives=%s"
    			, url
    			, Util.join(alternatives, Util.newline()));
    }
}
