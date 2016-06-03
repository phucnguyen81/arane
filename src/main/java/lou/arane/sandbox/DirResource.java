package lou.arane.sandbox;

import java.nio.file.Files;
import java.nio.file.Path;

import lou.arane.util.Unchecked;

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
		Unchecked.tryGet(() -> Files.createDirectories(dir));
		return this;
	}
}
