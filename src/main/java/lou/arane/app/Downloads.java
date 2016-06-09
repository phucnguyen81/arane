package lou.arane.app;

import static java.util.stream.Collectors.toList;

import java.util.Map.Entry;

import lou.arane.core.Cmd;
import lou.arane.core.cmds.CmdAllSuccess;
import lou.arane.core.cmds.CmdLimitedRetry;
import lou.arane.core.cmds.CmdLog;
import lou.arane.util.Check;
import lou.arane.util.FileResource;
import lou.arane.util.Log;
import lou.arane.util.New;
import lou.arane.util.ToString;
import lou.arane.util.URLResource;

/**
 * Download a batch of urls to files
 *
 * @author LOU
 */
public class Downloads extends CmdAllSuccess<Cmd> {

	public Downloads(Iterable<Entry<URLResource, FileResource>> items) {
    	this(items, 1);
    }

    public Downloads(Iterable<Entry<URLResource, FileResource>> items, int maxRetries) {
    	super(
			New.stream(items)
			    .map(i -> new Download(i))
				.map(c -> new CmdLimitedRetry(c, maxRetries))
				.map(CmdLog::new)
				.collect(toList())
		);
    	Check.require(maxRetries > 0, "Retries must be positive");
    }

    @Override
	protected Iterable<Cmd> onFilter(Iterable<Cmd> cmds) {
        return New.stream(cmds).filter(c -> {
            if (c.canRun()) {
                return true;
            }
            else {
                Log.info("Skip over " + c);
                return false;
            }
        }).collect(toList());
    }

    @Override
	protected void onException(Cmd c, Exception e) {
    	Log.error(e);
    }

    @Override
    public String toString() {
        return new ToString(Downloads.class).line(super.toString()).render();
    }
}
