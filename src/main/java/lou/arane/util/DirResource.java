package lou.arane.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represent a directory
 *
 * @author Phuc
 */
public class DirResource {

	private final Path dir;

	public DirResource(Path path) {
		this.dir = path;
	}

	public DirResource create() {
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			throw New.unchecked(e);
		}
		return this;
	}
}
