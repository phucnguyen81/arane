package lou.arane.util;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;

/**
 * Data related to a download operation
 *
 * @author Phuc
 */
public class DownloadItem {

	public final Uri uri;
	public final Path path;

	public Duration timeout = Duration.of(2, ChronoUnit.MINUTES);

	public final LinkedList<IOException> exceptions = New.linkedList();

	public DownloadItem(Uri uri, Path path) {
		this.uri = uri;
		this.path = path;
	}

	@Override
	public String toString() {
		return String.format("[%s -> %s]", uri, path);
	}

}