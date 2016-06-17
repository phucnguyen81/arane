package lou.arane.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/**
 * Results from making http connection
 */
public class HttpResponse implements Closeable {

    private final HttpURLConnection conn;

    private final byte[] content;

    private final int status;

    public HttpResponse(HttpURLConnection conn) {
        this.conn = conn;
        try {
            this.status = conn.getResponseCode();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (InputStream in = conn.getInputStream()) {
                IO.copy(conn.getInputStream(), buffer);
            }
            this.content = buffer.toByteArray();
        }
        catch (IOException e) {
            throw New.unchecked(e);
        }
    }

    /** Whether response code indicates error */
    public boolean hasErrorStatus() {
        return status < 200 || status > 299;
    }

    /**
     * Write content to output file. The file is created if not exists.
     */
    public void copyTo(FileResource file) {
        file.createIfNotExists();
        try (InputStream input = new ByteArrayInputStream(content);
                OutputStream fileOut = file.outputStream()) {
            IO.copy(input, fileOut);
        }
        catch (IOException e) {
            throw New.unchecked(e);
        }
    }

    @Override
    public void close() {
        conn.disconnect();
    }

    @Override
    public String toString() {
        return ToString.of(HttpResponse.class).add("conn=", conn).add(", ").add("status=", status)
                .str();
    }

}