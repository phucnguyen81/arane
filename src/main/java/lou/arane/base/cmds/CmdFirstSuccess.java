package lou.arane.base.cmds;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import lou.arane.base.Cmd;

/**
 * Run each command until the first one succeeds.
 * If all fail, throw the exception of the last command.
 *
 * @author Phuc
 */
public final class CmdFirstSuccess implements Cmd {

	private final Collection<Cmd> coll;

	public CmdFirstSuccess(Collection<? extends Cmd> coll) {
		this.coll = Collections.unmodifiableCollection(coll);
	}

	@Override
	public boolean canRun() {
		return coll.stream().anyMatch(c -> c.canRun());
	}

	@Override
	public void doRun() {
		Optional<RuntimeException> lastError = Optional.empty();
		for (Cmd c : coll) {
			if (c.canRun()) try {
				c.doRun();
				return;
			} catch (RuntimeException e) {
				lastError = Optional.of(e);
			}
		}
		lastError.ifPresent(e -> {throw e;});
	}

}
