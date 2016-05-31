package lou.arane.http;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lou.arane.base.Cmd;
import lou.arane.base.URLResource;
import lou.arane.cmds.CmdAllSuccess;
import lou.arane.cmds.CmdLimitedRetry;
import lou.arane.cmds.CmdWrap;
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
public class HttpBatchDownloader implements Cmd {

	private final List<Cmd> cmds = new ArrayList<>();

    private int maxDownloadAttempts = 1;

    public HttpBatchDownloader setMaxDownloadAttempts(int maxDownloadAttempts) {
        Check.require(maxDownloadAttempts > 0, "Download attempts must be positive");
        this.maxDownloadAttempts = maxDownloadAttempts;
        return this;
    }

    /** Add a pair of source-target to download */
    public void add(URLResource url, Path path) {
		cmds.add(new HttpDownloader(url, path));
    }

    @Override
	public boolean canRun() {
    	return cmds.stream().anyMatch(d -> d.canRun());
    }

    /** Download everything that were added */
    @Override
	public void doRun() {
    	new CmdAllSuccess(
    		withLoggingAndRetries()
    		, e -> Log.error(e)
    	).doRun();
    }

    /** Add retry and logging */
	private List<Cmd> withLoggingAndRetries() {
		return cmds.stream()
			.map(c -> new CmdLimitedRetry(c, maxDownloadAttempts))
			.map(c -> new CmdWrap(c, () -> {
				Log.info("Start: " + c);
				c.doRun();
				Log.info("End: " + c);
			}))
			.collect(Collectors.toList());
	}

	@Override
    public String toString() {
    	String className = getClass().getSimpleName();
		String joinedItems = Util.join(cmds, Util.NEWLINE);
		return String.format("%s[%n%s%n]", className, joinedItems);
    }
}
