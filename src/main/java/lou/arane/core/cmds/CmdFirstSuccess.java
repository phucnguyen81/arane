package lou.arane.core.cmds;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import lou.arane.core.Cmd;
import lou.arane.util.New;

/**
 * Run each command until the first one succeeds.
 * If all fail, throw the exception of the last command.
 *
 * @author Phuc
 */
public class CmdFirstSuccess implements Cmd {

	private final Collection<Cmd> coll;

	public CmdFirstSuccess(Collection<? extends Cmd> coll) {
		this.coll = Collections.unmodifiableCollection(coll);
	}

	@Override
	public final boolean canRun() {
		return coll.stream().anyMatch(c -> c.canRun());
	}

	@Override
	public final void doRun() {
		Optional<Exception> lastError = Optional.empty();
		for (Cmd c : coll) {
			if (c.canRun()) try {
				c.doRun();
				return;
			} catch (Exception e) {
				lastError = Optional.of(e);
			}
		}
		lastError.ifPresent(e -> {throw New.unchecked(e);});
	}

}
