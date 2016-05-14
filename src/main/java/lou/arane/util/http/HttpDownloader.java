package lou.arane.util.http;

import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import lou.arane.core.Command;
import lou.arane.util.IO;
import lou.arane.util.New;
import lou.arane.util.Uri;
import lou.arane.util.Util;

/**
 * Perform a single download operation
 *
 * @author Phuc
 */
public class HttpDownloader implements Command {

	public final Uri source;
	public final Path target;

	public final Duration timeout;

	/** Create a downloader with default timeout */
	public HttpDownloader(Uri source, Path target) {
		this(source, target, Duration.of(2, ChronoUnit.MINUTES));
	}

	/** Create a downloader given source, target and timeout */
	public HttpDownloader(Uri uri, Path path, Duration timeout) {
		this.source = uri;
		this.target = path;
		this.timeout = timeout;
	}

	@Override
	public boolean canRun() {
		return Util.notExists(target);
	}

	/** Download from source to target.
	 * Try alternate sources if the original one fails.
	 * Throw only the error of the last source being tried. */
	@Override
	public void doRun() {
		List<Uri> uris = New.list();
		uris.add(source);
		uris.addAll(source.alternatives);
		while (!uris.isEmpty()) {
			Uri aUri = uris.remove(0);
			try {
				IO.download(aUri.toUriString(), target, timeout);
				return;
			}
			catch (RuntimeException e) {
				if (uris.isEmpty()) {
					throw e;
				}
			}
		}
		throw new AssertionError("This error should be unreachable!");
	}

    @Override
	public String toString() {
		return String.format("Download[%n  source:%s%n  target:%s%n]", source, target);
	}
}