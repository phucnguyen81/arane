package lou.arane.http;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import lou.arane.base.Command;
import lou.arane.commands.decor.ReplaceCommand;
import lou.arane.commands.decor.RetryCommand;
import lou.arane.util.Check;
import lou.arane.util.Log;
import lou.arane.util.Url;
import lou.arane.util.Util;

/**
 * Download a batch of urls to files
 * <p>
 * TODO without using Url and Path this has nothing specific about http or downloader.
 * Move Url and Path outside to make a more general component.
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
    public void add(Url url, Path path) {
		commands.add(new HttpDownloader(url, path));
    }

    @Override
	public boolean canRun() {
    	return commands.stream().anyMatch(d -> d.canRun());
    }

    /** Download everything that were added */
    @Override
	public void doRun() {
    	Deque<Command> cmds = createDecoratedCommands();
    	//NOTE: without limitting retries, this can loop forever
        while (!cmds.isEmpty()) {
            Command c = cmds.removeFirst();
            try {
        		c.run();
        	}
        	catch (RuntimeException e) {
        		Log.error(e);
        		// re-try later
        		if (c.canRun()) {
        			cmds.addLast(c);
        		}
        	}
        }
    }

    /** Attach retry and logging to the commands */
	private Deque<Command> createDecoratedCommands() {
		return new ArrayDeque<>(commands.stream()
			.map(c -> new ReplaceCommand(c, () -> {
				Log.info("Start: " + c);
				c.doRun();
				Log.info("End: " + c);
			}))
			.map(c -> new RetryCommand(c, maxDownloadAttempts))
			.collect(Collectors.toList()));
	}

	@Override
    public String toString() {
    	String className = getClass().getSimpleName();
		String joinedItems = Util.join(commands, Util.LINE_BREAK);
		return String.format("%s[%n%s%n]", className, joinedItems);
    }
}
