package lou.arane.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import lou.arane.util.Unchecked;
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
        this.url = Unchecked.tryGet(() -> new URL(url.toExternalForm()));
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
    public void httpDownload(Path file, Duration timeout) throws IOException {
    	try (
			OutputStream out = Files.newOutputStream(file);
			HttpResponse res = new HttpResponse(
				IO.httpGET(url, StandardCharsets.UTF_8, timeout));
	    ){
    		if (res.hasErrorStatus()) {
    			throw new RuntimeException(String.format(
    				"Downloading: %s gives error status: %s", this.url, res));
    		}
    		/* copy in 2 phases to reduce the chance of incomplete download */
    		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    		res.copyTo(buffer);
    		IO.copy(new ByteArrayInputStream(buffer.toByteArray()), out);
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
