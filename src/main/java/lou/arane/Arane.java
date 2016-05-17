package lou.arane;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lou.arane.base.Command;
import lou.arane.base.Context;
import lou.arane.base.URLResource;
import lou.arane.commands.BlogTruyen;
import lou.arane.commands.EgScans;
import lou.arane.commands.IzTruyenTranh;
import lou.arane.commands.KissManga;
import lou.arane.commands.MangaGo;
import lou.arane.commands.MangaLife;
import lou.arane.commands.MangaSee;
import lou.arane.commands.decor.Assembled;
import lou.arane.commands.decor.Decorator;
import lou.arane.util.Log;
import lou.arane.util.Util;

/**
 * Entry point of the system.
 *
 * @author Phuc
 */
public class Arane implements Command {

	public static void main(String[] args) {
		if (args.length < 2) {
			printHelp(args);
		}
		else try {
			new Arane(args[0], args[1]).run();
		}
		catch (Throwable t) {
			printHelp(args);
			throw t;
		}
	}

	private static void printHelp(String[] args) {
		System.err.println("------------------------------------------");
		System.err.println("Failed to handle arguments: " + Arrays.asList(args));
		System.err.println("------------------------------------------");
		System.err.println("Usage: arane name url");
		System.err.println("name = name to identify the content");
		System.err.println("url = the url you want to download from");
		System.err.println("e.g. arane WorldTrigger http://manga.life/read-online/WorldTrigger");
		System.err.println("------------------------------------------");
		System.err.println("Current supported sites are:");
		System.err.println("blogtruyen, egscans, iztruyentranh, kissmanga, mangago, manga.life, mangasee");
		System.err.println("------------------------------------------");
	}

	private final String name;
	private final String url;

	public Arane(String name, String url) {
		this.name = name;
		this.url = url;
	}

	/** Run the first command that can handle the given name and url */
	@Override
	public void doRun() {
		createCommands()
		.stream()
		.filter(Command::canRun)
		.findFirst()
		.orElse(new Assembled(() ->
			Log.info("No supports for: " + name + ", " + url)))
		.doRun();
	}

	/** Create all available handlers */
	private List<Command> createCommands() {
		List<Command> commands = new ArrayList<>();
		Context ctx;

		ctx = new Context(name, new URLResource(url), mangaDir("blogtruyen", name));
		commands.add(attachLog(new BlogTruyen(ctx), ctx));

		ctx = new Context(name, new URLResource(url), mangaDir("egscans", name));
		commands.add(attachLog(new EgScans(ctx), ctx));

		ctx = new Context(name, new URLResource(url), mangaDir("izmanga", name));
		commands.add(attachLog(new IzTruyenTranh(ctx), ctx));

		ctx = new Context(name, new URLResource(url), mangaDir("kissmanga", name));
		commands.add(attachLog(new KissManga(ctx), ctx));

		ctx = new Context(name, new URLResource(url), mangaDir("mangago", name));
		commands.add(attachLog(new MangaGo(ctx), ctx));

		ctx = new Context(name, new URLResource(url), mangaDir("manga.life", name));
		commands.add(attachLog(new MangaLife(ctx), ctx));

		ctx = new Context(name, new URLResource(url), mangaDir("mangasee", name));
		commands.add(attachLog(new MangaSee(ctx), ctx));

		return commands;
	}

	/** Attach logging before and after a run */
	private static Command attachLog(Command cmd, Context ctx) {
		return new Decorator(cmd, () -> {
			Log.info(String.format("Start downloading %s into %s", ctx.sourceName, ctx.target));
			cmd.doRun();
			Log.info(String.format("Finished downloading %s into %s", ctx.sourceName, ctx.target));
		});
	}

	/** Common way to get a base directory for downloading a manga. */
	private static Path mangaDir(String first, String... more) {
	    Path mangasDir = Util.userHomeDir().resolve("mangas");
	    Path baseDir = mangasDir.resolve(Paths.get(first, more));
	    return baseDir;
	}

}
