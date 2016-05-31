package lou.arane.url;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import lou.arane.cmds.CmdFirstSuccess;
import lou.arane.cmds.CmdWrap;
import lou.arane.util.IO;
import lou.arane.util.Util;

/**
 * Download source url to target file.
 *
 * @author Phuc
 */
public class URLDownload extends CmdWrap {

	private final URLResource source;
	private final Path target;

	/** Instantiate with default timeout */
	public URLDownload(URLResource source, Path target) {
		this(source, target, Duration.of(1, MINUTES));
	}

	/** Instantiate given source, target and timeout */
	public URLDownload(URLResource source, Path target, Duration timeout) {
		super(
			allSources(source)
			.map(url -> url.string())
			.map(url -> new CmdWrap(
				() -> Util.notExists(target)
				, () -> IO.download(url, target, timeout)))
			.collect(collectingAndThen(
				toList()
				, cmds -> new CmdFirstSuccess(cmds)))
		);
		this.source = source;
		this.target = target;
	}

	private static Stream<URLResource> allSources(URLResource source) {
		List<URLResource> urls = new ArrayList<>();
		urls.add(source);
		urls.addAll(source.alternatives());
		return urls.stream();
	}

	@Override
	public String toString() {
		return String.format("Download[%n source:%s%n target:%s%n]", source, target);
	}
}