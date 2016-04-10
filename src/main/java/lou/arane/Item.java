package lou.arane;

import java.nio.file.Path;

import lou.arane.util.Uri;

public class Item {

	public final Uri initialUri;
	public final Path baseDir;

	public Item(Uri source, Path target) {
		this.initialUri = source;
		this.baseDir = target;
	}

}
