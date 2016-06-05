package lou.arane;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lou.arane.app.Context;
import lou.arane.app.usecases.BlogTruyen;
import lou.arane.app.usecases.EgScans;
import lou.arane.app.usecases.IzTruyenTranh;
import lou.arane.app.usecases.KissManga;
import lou.arane.app.usecases.MangaGo;
import lou.arane.app.usecases.MangaLife;
import lou.arane.app.usecases.MangaSee;
import lou.arane.core.Cmd;
import lou.arane.core.cmds.CmdLog;
import lou.arane.util.Log;
import lou.arane.util.URLResource;
import lou.arane.util.Util;

/**
 * Entry point of the system.
 *
 * @author Phuc
 */
public class Arane {

	public static void main(String[] args) {
		if (args.length < 2) {
			printHelp(args);
		}
		else try {
			String name = args[0];
			URLResource url = URLResource.of(args[1])
				.orElseThrow(() -> new IllegalArgumentException(
					"Illegal url: " + args[1]));
			new Arane(name, url).run();
		}
		catch (Throwable t) {
			printHelp(args);
			t.printStackTrace();
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
	private final URLResource url;

	public Arane(String name, URLResource url) {
		this.name = name;
		this.url = url;
	}

	public void run() {
		createCommands()
		.stream()
		.filter(Cmd::canRun)
		.findFirst()
		.<Cmd>map(CmdLog::new)
		.orElse(reportNoSupport())
		.doRun();
	}

	/** Create all available commands to handle the args */
	private List<Cmd> createCommands() {
		List<Cmd> cmds = new ArrayList<>();
		cmds.add(new BlogTruyen(new Context(name, url, mangaDir("blogtruyen", name))));
		cmds.add(new EgScans(new Context(name, url, mangaDir("egscans", name))));
		cmds.add(new IzTruyenTranh(new Context(name, url, mangaDir("izmanga", name))));
		cmds.add(new KissManga(new Context(name, url, mangaDir("kissmanga", name))));
		cmds.add(new MangaGo(new Context(name, url, mangaDir("mangago", name))));
		cmds.add(new MangaLife(new Context(name, url, mangaDir("manga.life", name))));
		cmds.add(new MangaSee(new Context(name, url, mangaDir("mangasee", name))));
		return cmds;
	}

	private Cmd reportNoSupport() {
		return new Cmd() {
			@Override
			public void doRun() {
				Log.info("Found no supports for downloading: " + name + ", " + url);
			}
		};
	}

	/** Common way to get a base directory for downloading a manga. */
	private static Path mangaDir(String first, String... more) {
	    Path mangasDir = Util.userHomeDir().resolve("mangas");
	    Path baseDir = mangasDir.resolve(Paths.get(first, more));
	    return baseDir;
	}

}
