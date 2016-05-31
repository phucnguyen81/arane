package lou.arane.url;

import static java.util.Collections.unmodifiableCollection;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import lou.arane.cmds.CmdAllSuccess;
import lou.arane.cmds.CmdLimitedRetry;
import lou.arane.cmds.CmdWrap;
import lou.arane.util.Check;
import lou.arane.util.Log;
import lou.arane.util.Util;

/**
 * Download a batch of urls to files
 *
 * @author LOU
 */
public class URLDownloads extends CmdWrap {

    private final Collection<Entry<URLResource, Path>> items;

	public URLDownloads(Collection<Entry<URLResource, Path>> items) {
    	this(items, 1);
    }

    public URLDownloads(Collection<Entry<URLResource, Path>> items, int maxRetries) {
    	super(
    		new CmdAllSuccess(
        		items.stream()
				.map(e1 -> new URLDownload(e1.getKey(), e1.getValue()))
				.map(c -> new CmdLimitedRetry(c, maxRetries))
				.map(c -> new CmdWrap(c, () -> {
					Log.info("Start: " + c);
					c.doRun();
					Log.info("End: " + c);}))
				.collect(Collectors.toList())
        		, e -> Log.error(e)));
    	Check.require(maxRetries > 0, "Retries must be positive");
    	this.items = unmodifiableCollection(items);
    }

    @Override
    public String toString() {
    	String className = getClass().getSimpleName();
		String joinedItems = Util.join(items, Util.NEWLINE);
		return String.format("%s(%n%s%n)", className, joinedItems);
    }
}
