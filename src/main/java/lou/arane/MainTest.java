package lou.arane;

/**
 * Do all the testing here for now
 *
 * @author Phuc
 */
public class MainTest {

	public static void main(String[] args) {
		boolean on = true, off = false;
		if (off) testMangaLife();
		if (off) testIzTruyenTranh();
		if (off) testMangaGo();
		if (off) testKissManga();
		if (on) testBlogTruyen();
		if (off) testMangaSee();
	}

	static void testMangaLife() {
		Main.main(new String[] {
			"Guarding"
			, "http://manga.life/read-online/Guarding"
		});
	}

	static void testBlogTruyen() {
		Main.main(new String[] {
			"chu-thoong-11820"
			, "http://blogtruyen.com/truyen/chu-thoong-11820"
		});
	}

	static void testMangaGo() {
		Main.main(new String[] {
			"2001_nights"
			, "http://www.mangago.me/read-manga/2001_nights/"
		});
	}

	static void testMangaSee() {
		Main.main(new String[] {
			"DrSlump"
			, "http://mangasee.co/manga/?series=DrSlump"
		});
	}

	static void testKissManga() {
		Main.main(new String[] {
			"Lighter"
			, "http://kissmanga.com/Manga/Lighter"
		});
	}

	static void testEgScans() {
		Main.main(new String[] {
			"Paladin"
			, "http://read.egscans.com/Paladin"
		});
	}

	static void testIzTruyenTranh() {
		Main.main(new String[] {
			"trinity_wonder"
			, "http://iztruyentranh.com/truyen-tranh/trinity_wonder"
		});
	}
}
