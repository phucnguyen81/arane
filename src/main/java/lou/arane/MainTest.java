package lou.arane;

/**
 * Do all the testing here for now
 *
 * @author Phuc
 */
public class MainTest {

	public static void main(String[] args) {
		testMangaGo();
//		testBlogTruyen();
//		testMangaSee();
	}

	static void testBlogTruyen() {
		Main.main(new String[] {
			"paladin"
			, "http://blogtruyen.com/truyen/paladin"
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

	static void testMangaLife() {
		Main.main(new String[] {
			"Gleipnir"
			, "http://manga.life/read-online/Gleipnir"
		});
	}

	static void testIzManga() {
		Main.main(new String[] {
			"doraemon_doremon_bong_chay"
			, "http://izmanga.com/truyen-tranh/doraemon_doremon_bong_chay"
		});
	}
}
