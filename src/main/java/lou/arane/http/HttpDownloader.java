package lou.arane.http;

import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;

import lou.arane.base.Command;
import lou.arane.base.URLResource;
import lou.arane.util.IO;
import lou.arane.util.Util;

/**
 * Download source url to target file.
 *
 * @author Phuc
 */
public class HttpDownloader implements Command {

	private final URLResource source;
	private final Path target;

	private final Duration timeout;

	/** Create a downloader with default timeout */
	public HttpDownloader(URLResource source, Path target) {
		this(source, target, Duration.of(2, ChronoUnit.MINUTES));
	}

	/** Create a downloader given source, target and timeout */
	public HttpDownloader(URLResource url, Path path, Duration timeout) {
		this.source = url;
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
		Deque<URLResource> urls = new ArrayDeque<>(source.alternatives());
		urls.addFirst(source);
		while (!urls.isEmpty()) {
			URLResource url = urls.removeFirst();
			try {
				IO.download(url.string(), target, timeout);
				return;
			}
			catch (RuntimeException e) {
				if (urls.isEmpty()) {
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