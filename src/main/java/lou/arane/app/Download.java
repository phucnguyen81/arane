package lou.arane.app;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lou.arane.core.Cmd;
import lou.arane.core.cmds.CmdFirstSuccess;
import lou.arane.core.cmds.CmdWrap;
import lou.arane.util.FileResource;
import lou.arane.util.URLResource;

/**
 * Download source url to target file.
 *
 * @author Phuc
 */
public class Download extends CmdWrap<Cmd> {

	private final URLResource source;
	private final FileResource target;

	/** Instantiate given download item */
	public Download(Entry<URLResource, FileResource> item) {
		this(item.getKey(), item.getValue());
	}

	/** Instantiate given source and target download */
	private Download(URLResource source, FileResource target) {
		super(
			new CmdFirstSuccess(
				sourceAndAlternatives(source)
				.stream()
				.map(url -> new DownloadUnit(url, target))
				.collect(toList())
			)
		);
		this.source = source;
		this.target = target;
	}

	private static List<URLResource> sourceAndAlternatives(URLResource source) {
		List<URLResource> urls = new ArrayList<>();
		urls.add(source);
		urls.addAll(source.alternatives());
		return urls;
	}

	@Override
	public String toString() {
		return String.format("%s:%n  source:%s%n  target:%s"
			, Download.class.getSimpleName(), source, target
		);
	}
}