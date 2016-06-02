package lou.arane.io;

import static java.util.stream.Collectors.toList;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map.Entry;

import lou.arane.base.cmds.CmdAllSuccess;
import lou.arane.base.cmds.CmdLimitedRetry;
import lou.arane.base.cmds.CmdWrap;
import lou.arane.util.Check;
import lou.arane.util.Log;

/**
 * Download a batch of urls to files
 *
 * @author LOU
 */
public class URLDownloads extends CmdAllSuccess {

	public URLDownloads(Collection<Entry<URLResource, Path>> items) {
    	this(items, 1);
    }

    public URLDownloads(Collection<Entry<URLResource, Path>> items, int maxRetries) {
    	super(
        	items.stream()
				.map(i -> new URLDownload(i))
				.map(c -> new CmdLimitedRetry(c, maxRetries))
				.map(c -> new CmdWrap(c, () -> {
					Log.info("Start: " + c);
					c.doRun();
					Log.info("End: " + c);
				}))
				.collect(toList())
			, e -> Log.error(e)
		);
    	Check.require(maxRetries > 0, "Retries must be positive");
    }

    @Override
    public String toString() {
    	return String.format("%s:%n  %s"
    		, URLDownloads.class.getSimpleName()
    		, super.toString()
    	);
    }
}
