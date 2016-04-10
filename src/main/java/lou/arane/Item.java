package lou.arane;

import java.nio.file.Path;

import lou.arane.util.Uri;

public class Item {

	public final Uri source;
	public final Path target;

	public Item(Uri source, Path target) {
		this.source = source;
		this.target = target;
	}

}
