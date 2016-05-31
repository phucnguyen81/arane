package lou.arane.base;

import java.nio.file.Files;
import java.nio.file.Path;

import lou.arane.util.Try;

/**
 * Represent a file
 *
 * @author Phuc
 */
public class FileResource {

	private final DirResource dir;
	private final Path file;

	public FileResource(Path path)	{
		this.dir = new DirResource(path.getParent());
		this.file = path;
	}

	public FileResource create() {
		Try.toDo(() -> Files.createFile(file));
		return this;
	}

	public boolean notExists() {
		return Files.notExists(file);
	}

	public FileResource createIfNotExists() {
		if (notExists()) {
			dir.create();
			create();
		}
		return this;
	}

}
