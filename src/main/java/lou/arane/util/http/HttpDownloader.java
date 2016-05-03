package lou.arane.util.http;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lou.arane.util.Check;
import lou.arane.util.Uri;

import com.ning.http.client.AsyncCompletionHandlerBase;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Response;

/**
 * Downloads content from url via http protocol. Checked-exceptions are
 * re-thrown as {@link HttpIOException}.
 */
public class HttpDownloader {

    private final long timeout;
    private final TimeUnit timeoutUnit;

    /** Create a downloader with default timeout */
    public HttpDownloader() {
        this(2, MINUTES);
    }

    /** Create a downloader with a given timeout */
    public HttpDownloader(long timeout, TimeUnit timeoutUnit) {
        Check.require(timeout > 0, "Expect positive timeout, actual value: " + timeout);
        Check.notNull(timeoutUnit, "Null time unit");
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
    }

    public byte[] getBytes(Uri uri) throws HttpIOException {
        return getBytes(uri.uri);
    }

    public byte[] getBytes(URI uri) throws HttpIOException {
        try {
            return getBytes(uri.toURL());
        }
        catch (MalformedURLException urlError) {
            throw new HttpIOException(urlError);
        }
    }

    public byte[] getBytes(URL url) throws HttpIOException {
        return getBytes(url.toString());
    }

    public byte[] getBytes(String url) throws HttpIOException {
        try {
            return getBytesInternal(url);
        }
        catch (IOException e) {
            throw new HttpIOException(e);
        }
    }

    private byte[] getBytesInternal(String url) throws HttpIOException, IOException {
        Check.notNull(url, "Null url");
        Response response = getResponse(url);
        int status = response.getStatusCode();
        if (status < 200 || status > 299) {
            String message = "Failed to download from %s %n %s";
            message = String.format(message, url, response.getResponseBody());
            throw new HttpIOException(message);
        }
        else {
            return response.getResponseBodyAsBytes();
        }
    }

    /**
     * Try to get response before timeout. Will close connection by itself.
     */
    private Response getResponse(String url) throws HttpIOException {
        try (AsyncHttpClient client = new AsyncHttpClient()) {
            BoundRequestBuilder request = client.prepareGet(url);
            Future<Response> receiver = request.execute(new AsyncCompletionHandlerBase());
            return receiver.get(timeout, timeoutUnit);
        }
        catch (IOException | InterruptedException | ExecutionException | TimeoutException cause) {
            throw new HttpIOException(cause);
        }
    }

}
