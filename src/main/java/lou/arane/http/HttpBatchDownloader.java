package lou.arane.http;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lou.arane.base.Command;
import lou.arane.base.URLResource;
import lou.arane.commands.decor.ReplaceCommand;
import lou.arane.commands.decor.LimitedRetryCommand;
import lou.arane.commands.decor.RetryUntilSuccessCommand;
import lou.arane.util.Check;
import lou.arane.util.Log;
import lou.arane.util.Util;

/**
 * Download a batch of urls to files
 * <p>
 * TODO without using URLResource and Path this has nothing specific about http or downloader.
 * Move URLResource and Path outside to make a more general component.
 * Also, would want this one immutable.
 *
 * @author LOU
 */
public class HttpBatchDownloader implements Command {

	private final List<Command> commands = new ArrayList<>();

    private int maxDownloadAttempts = 1;

    public HttpBatchDownloader setMaxDownloadAttempts(int maxDownloadAttempts) {
        Check.require(maxDownloadAttempts > 0, "Download attempts must be positive");
        this.maxDownloadAttempts = maxDownloadAttempts;
        return this;
    }

    /** Add a pair of source-target to download */
    public void add(URLResource url, Path path) {
		commands.add(new HttpDownloader(url, path));
    }

    @Override
	public boolean canRun() {
    	return commands.stream().anyMatch(d -> d.canRun());
    }

    /** Download everything that were added */
    @Override
	public void doRun() {
    	new RetryUntilSuccessCommand(
    		withLoggingAndRetries()
    		, e -> Log.error(e)
    	).doRun();
    }

    /** Enhance commands with retry and logging */
	private List<Command> withLoggingAndRetries() {
		return commands.stream()
			.map(c -> new LimitedRetryCommand(c, maxDownloadAttempts))
			.map(c -> new ReplaceCommand(c, () -> {
				Log.info("Start: " + c);
				c.doRun();
				Log.info("End: " + c);
			}))
			.collect(Collectors.toList());
	}

	@Override
    public String toString() {
    	String className = getClass().getSimpleName();
		String joinedItems = Util.join(commands, Util.LINE_BREAK);
		return String.format("%s[%n%s%n]", className, joinedItems);
    }
}
