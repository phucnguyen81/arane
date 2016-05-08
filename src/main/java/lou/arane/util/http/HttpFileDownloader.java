package lou.arane.util.http;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import lou.arane.util.Check;
import lou.arane.util.New;
import lou.arane.util.Uri;

/**
 * Download content from a source uri and write to a target path
 *
 * @author LOU
 */
public class HttpFileDownloader {

    /** Example usage: download a file from the net */
    public static void main(String[] args) throws Exception {
        Uri source = Uri.of("http://orgmode.org/worg/org-glossary.html");
        String target = source.getFileName();
        new HttpFileDownloader(source, Paths.get(target)).download();
    }

    private final Uri uri;
    private final Path path;

    private final HttpDownloader downloader = new HttpDownloader();

    private final LinkedList<IOException> exceptions = New.linkedList();

    public HttpFileDownloader(Uri uri, Path path) {
        this.uri = Check.notNull(uri);
        this.path = Check.notNull(path);
    }

    public Path getPath() {
        return path;
    }

    /** Whether the path to write to already exists */
    public boolean pathExists() {
        return Files.exists(path);
    }

    /** Return the exceptions caught when calling {@link #download} */
    public LinkedList<IOException> getDownloadExceptions() {
        return exceptions;
    }

    /** Download uri content and save to path. Keep track of exception. */
    public void download() throws IOException {
        IOException error = null;
        try {
            Path parentDir = path.toAbsolutePath().getParent();
            Files.createDirectories(parentDir);
            Files.write(path, downloadBytes());
        }
        catch (IOException e) {
            error = e;
        }
        finally {
            if (error != null) {
                exceptions.add(error);
                throw error;
            }
        }
    }

    /**
     * Download uri content as bytes. Try alternate uris if the original one
     * fails. Throw only the last error.
     */
    private byte[] downloadBytes() throws IOException {
        List<Uri> uris = New.list();
        uris.add(uri);
        uris.addAll(uri.getAlternatives());

        IOException error = null;
        for (Uri uri : uris) {
            try {
                return downloader.getBytes(uri);
            }
            catch (IOException e) {
                error = e;
            }
        }
        throw error;
    }

    @Override
    public String toString() {
    	return String.format("%s(%s->%s)",
    			getClass().getSimpleName(), uri, path);
    }

}
