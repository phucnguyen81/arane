package lou.arane.url;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import lou.arane.base.cmds.CmdFirstSuccess;
import lou.arane.base.cmds.CmdWrap;
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
	public URLDownload(Entry<URLResource, Path> item) {
		this(item, Duration.of(1, MINUTES));
	}

	/** Instantiate given download item and timeout */
	public URLDownload(Entry<URLResource, Path> item, Duration timeout) {
		this(item.getKey(), item.getValue(), timeout);
	}

	/** Instantiate given source, target and timeout */
	private URLDownload(URLResource source, Path target, Duration timeout) {
		super(
			allSources(source)
			.map(url -> new CmdWrap(
				() -> Util.notExists(target)
				, () -> url.httpDownload(target, timeout)))
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