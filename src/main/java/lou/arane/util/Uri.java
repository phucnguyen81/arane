package lou.arane.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

/**
 * Capture uris normally accepted by browser.
 * <p>
 * TODO the alternate uris make this not immutable. Find a clean way to separate
 * handling of alternate uris.
 *
 * @author pnguyen58
 */
public class Uri implements Comparable<Uri> {

    public static Uri of(String uri) {
        return new Uri(uri);
    }

    private static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    /* alternate uris meant to locate the same resource as this uri */
    private final LinkedList<Uri> alternatives = New.linkedList();

    private final UriComponents uri;

    public Uri(URI uri) {
        this(UriComponentsBuilder.fromUri(uri));
    }

    public Uri(String uri) {
        this(UriComponentsBuilder.fromUriString(uri));
    }

    @Override
    public String toString() {
        return uri.toString() +
            (alternatives.isEmpty() ? "" : " " + alternatives.toString());
    }

    @Override
    public int compareTo(Uri other) {
        return toUri().compareTo(other.toUri());
    }

    @Override
    public boolean equals(Object other) {
        return compareTo(((Uri) other)) == 0;
    }

    /**
     * Create uri from a uri string. If the uri scheme is not present, it
     * defaults to http.
     */
    public static Uri http(String uriString) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uriString);
        if (builder.build().getScheme() == null) {
            builder.scheme("http");
        }
        return new Uri(builder);
    }

    /** Base method to create uri */
    private Uri(UriComponentsBuilder builder) {
        uri = encode(builder.build());
    }

    public void addAlternatives(Uri uri) {
        alternatives.add(uri);
    }

    public LinkedList<Uri> getAlternatives() {
        return alternatives;
    }

    public Uri resolve(String str) {
        str = encode(str);
        return new Uri(toUri().resolve(str));
    }

    public Uri queryParam(String name, Object... values) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(toUri());
        builder.queryParam(name, values);
        return new Uri(builder);
    }

    /** Make a uri from this uri without the query part */
    public Uri removeQuery() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(toUri());
        builder.replaceQuery(null);
        return new Uri(builder);
    }

    public URI toUri() {
        return uri.toUri();
    }

    public String getFileExtension() {
        if (getFileName() == null) return null;
        String filename = getFileName().toString();
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex < 0) return "";
        return filename.substring(extensionIndex + 1);
    }

    public Path getFileName() {
        Path path = getFilePath();
        return path == null ? null : path.getFileName();
    }

    public Path getFilePath() {
        Path path = null;
        String uriPath = uri.getPath();
        if (uriPath != null) for (Path p : Paths.get(decode(uriPath))) {
            if (path == null) path = p;
            else path = path.resolve(p);
        }
        return path;
    }

    public String getQuery() {
        return decode(uri.getQuery());
    }

    public String getQueryParm(String key) {
        MultiValueMap<String, String> params = uri.getQueryParams();
        String val = params.getFirst(key);
        return decode(val);
    }

    public static String decode(String source) {
        try {
            return source == null ? null : UriUtils.decode(source, DEFAULT_ENCODING);
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Should not happen", e);
        }
    }

    public static String encode(String uriStr) {
        UriComponents uri = UriComponentsBuilder.fromUriString(uriStr).build();
        return encode(uri).toUriString();
    }

    private static UriComponents encode(UriComponents uri) {
        try {
            return uri.encode(DEFAULT_ENCODING);
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Should not happen!", e);
        }
    }
}
