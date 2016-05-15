package lou.arane.util.http;

import java.nio.file.Path;
import java.util.LinkedList;

import lou.arane.core.Command;
import lou.arane.core.RetryCommand;
import lou.arane.util.Check;
import lou.arane.util.Log;
import lou.arane.util.New;
import lou.arane.util.Url;
import lou.arane.util.Util;

/**
 * Download a batch of urls to files
 *
 * @author LOU
 */
public class HttpBatchDownloader implements Command {

	private final LinkedList<RetryCommand> downloaders = New.linkedList();

    private int maxDownloadAttempts = 1;

    public HttpBatchDownloader setMaxDownloadAttempts(int maxDownloadAttempts) {
        Check.require(maxDownloadAttempts > 0, "Download attempts must be positive");
        this.maxDownloadAttempts = maxDownloadAttempts;
        return this;
    }

    /** Add a pair of source-target to download */
    public void add(Url url, Path path) {
    	HttpDownloader d = new HttpDownloader(url, path);
        RetryCommand rd = new RetryCommand(d);
        rd.runLimit = maxDownloadAttempts;
		downloaders.add(rd);
    }

    @Override
	public boolean canRun() {
    	return downloaders.stream().anyMatch(d -> d.canRun());
    }

    /** Download the pairs of source-target that were added */
    @Override
	public void doRun() {
        while (!downloaders.isEmpty()) {
            RetryCommand item = downloaders.removeFirst();
            try {
        		Log.info("Start: " + item);
        		item.run();
        		Log.info("End: " + item);
        	}
        	catch (RuntimeException e) {
        		Log.error(e);
        		// re-try later
        		if (item.canRun()) {
        			downloaders.addLast(item);
        		}
        	}
        }
    }

	@Override
    public String toString() {
    	String className = getClass().getSimpleName();
		String joinedItems = Util.join(downloaders, Util.LINE_BREAK);
		return String.format("%s[%n%s%n]", className, joinedItems);
    }
}
