package lou.arane;

/**
 * Do all the testing here for now
 *
 * @author Phuc
 */
public class AraneTest {

	public static void main(String[] args) {
		boolean on = true, off = false;
		if (on) testMangaLife();
		if (off) testIzTruyenTranh();
		if (off) testMangaGo();
		if (off) testKissManga();
		if (off) testBlogTruyen();
		if (off) testMangaSee();
	}

	static void testMangaLife() {
		Arane.main(new String[] {
			"Guarding"
			, "http://manga.life/read-online/Guarding"
		});
	}

	static void testBlogTruyen() {
		Arane.main(new String[] {
			"saiki-kusuo-no-sainan"
			, "http://blogtruyen.com/truyen/saiki-kusuo-no-sainan"
		});
	}

	static void testMangaGo() {
		Arane.main(new String[] {
			"saber_tiger"
			, "http://www.mangago.me/read-manga/saber_tiger"
		});
	}

	static void testMangaSee() {
		Arane.main(new String[] {
			"DrSlump"
			, "http://mangasee.co/manga/?series=DrSlump"
		});
	}

	static void testKissManga() {
		Arane.main(new String[] {
			"Lighter"
			, "http://kissmanga.com/Manga/Lighter"
		});
	}

	static void testEgScans() {
		Arane.main(new String[] {
			"Paladin"
			, "http://read.egscans.com/Paladin"
		});
	}

	static void testIzTruyenTranh() {
		Arane.main(new String[] {
			"trinity_wonder"
			, "http://iztruyentranh.com/truyen-tranh/trinity_wonder"
		});
	}
}
