package lou.arane.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jsoup.nodes.Document;

/**
 * Represent a file
 *
 * @author Phuc
 */
public class FileResource {

    private final DirResource dir;
    private final Path file;

    public FileResource(Path path) {
        this.dir = new DirResource(path.getParent());
        this.file = path;
    }

    @Override
    public String toString() {
        return ToString.of(FileResource.class).add(file).str();
    }

    public OutputStream outputStream() {
        try {
            return Files.newOutputStream(file);
        }
        catch (IOException e) {
            throw New.unchecked(e);
        }
    }

    public FileResource create() {
        try {
            Files.createFile(file);
        }
        catch (IOException e) {
            throw New.unchecked(e);
        }
        return this;
    }

    public FileResource createIfNotExists() {
        if (notExists()) {
            dir.create();
            create();
        }
        return this;
    }

    public void delete() {
        try {
            Files.delete(file);
        }
        catch (IOException e) {
            throw New.unchecked(e);
        }
    }

    public void deleteIfExists() {
        if (exists()) {
            delete();
        }
    }

    public boolean exists() {
        return Files.exists(file);
    }

    public boolean notExists() {
        return Files.notExists(file);
    }

    /**
     * TODO create Html for this
     */
    public Document parseHtml(String baseUri) {
        return Util.parseHtml(file, baseUri);
    }

}
