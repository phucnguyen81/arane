package lou.arane.commands.decor;

import lou.arane.base.Command;

/**
 * Do something before and/or after a command run.
 *
 * @author Phuc
 */
public class PrePostCommand implements Command {

	private static final Runnable DO_NOTHING = new Runnable() {
		public @Override void run() {}
	};

	private final Runnable pre;
	private final Command cmd;
	private final Runnable post;

	public PrePostCommand(Runnable pre, Command cmd) {
		this(pre, cmd, DO_NOTHING);
	}

	public PrePostCommand(Command cmd, Runnable post) {
		this(DO_NOTHING, cmd, post);
	}

	public PrePostCommand(Runnable pre, Command cmd, Runnable post) {
		this.pre = pre;
		this.cmd = cmd;
		this.post = post;
	}

	/** Delegate to the internal command */
	@Override
	public boolean canRun() {
		return cmd.canRun();
	}

	/** Run command together with pre and post actions */
	@Override
	public void doRun() {
		pre.run();
		cmd.doRun();
		post.run();
	}

}
