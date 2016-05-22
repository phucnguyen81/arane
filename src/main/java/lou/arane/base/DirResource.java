package lou.arane.base;

import java.nio.file.Files;
import java.nio.file.Path;

import lou.arane.util.Try;

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

	public void create() {
		Try.toDo(() -> Files.createDirectories(dir));
	}
}
