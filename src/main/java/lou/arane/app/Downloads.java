package lou.arane.app;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lou.arane.core.Cmd;
import lou.arane.core.cmds.CmdAllSuccess;
import lou.arane.core.cmds.CmdLimitedRetry;
import lou.arane.core.cmds.CmdLog;
import lou.arane.util.Check;
import lou.arane.util.Log;
import lou.arane.util.New;
import lou.arane.util.ToString;

/**
 * Download a batch of urls to files
 *
 * @author LOU
 */
public class Downloads extends CmdAllSuccess<Cmd> {

    public Downloads() {
        this(Collections.emptyList(), 1);
    }

    public Downloads(Iterable<Download> dls, int maxRetries) {
        super(withRetryAndLog(dls, maxRetries));
    }

    private static List<Cmd> withRetryAndLog(Iterable<Download> downloads, int retries) {
        Check.require(retries > 0, "Retries must be positive");
        return New.stream(downloads)
                .map(d -> new CmdLimitedRetry(d, retries))
                .map(d -> new CmdLog(d))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return ToString.of(Downloads.class).add(super.toString()).str();
    }

    /**
     * Filter out downloads that cannot run in the first place.
     * Log these downloads as being skipped.
     */
    @Override
    protected final Iterable<Cmd> onFilter(Iterable<Cmd> cmds) {
        List<Cmd> filtered = New.list();
        for (Cmd c : cmds) {
            if (c.canRun()) {
                filtered.add(c);
            }
            else {
                Log.info("Skip over " + c);
            }
        }
        return filtered;
    }

    @Override
    protected final void onException(Cmd c, Exception e) {
        Log.warning(e);
    }

}
