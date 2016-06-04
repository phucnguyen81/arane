package lou.arane;

import static java.util.stream.Collectors.toList;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map.Entry;

import lou.arane.core.cmds.CmdAllSuccess;
import lou.arane.core.cmds.CmdLimitedRetry;
import lou.arane.core.cmds.CmdWrap;
import lou.arane.util.Check;
import lou.arane.util.Log;
import lou.arane.util.URLResource;

/**
 * Download a batch of urls to files
 *
 * @author LOU
 */
public class Downloads extends CmdAllSuccess {

	public Downloads(Collection<Entry<URLResource, Path>> items) {
    	this(items, 1);
    }

    public Downloads(Collection<Entry<URLResource, Path>> items, int maxRetries) {
    	super(
        	items.stream()
				.map(i -> new Download(i))
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
    		, Downloads.class.getSimpleName()
    		, super.toString()
    	);
    }
}
