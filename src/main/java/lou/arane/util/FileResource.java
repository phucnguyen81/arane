package lou.arane.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
		try {
			Files.createFile(file);
		} catch (IOException e) {
			throw New.unchecked(e);
		}
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
