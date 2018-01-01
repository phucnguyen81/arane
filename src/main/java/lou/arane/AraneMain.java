package lou.arane;

/**
 * Do all the testing here for now
 *
 * @author Phuc
 */
public class AraneMain {

	public static void main(String[] args) {
		boolean on = true, off = false;
        if (off) testMangakakalot();
        if (on) testManganelo();
        if (off) testEgScans();
        if (off) testMangaLife();
		if (off) testIzTruyenTranh();
		if (off) testMangaGo();
		if (off) testKissManga();
        if (off) testBlogTruyen();
		if (off) testMangaSee();
	}

    static void testManganelo() {
        Arane.main(new String[] {
            "unOrdinary", "http://manganelo.com/manga/unordinary"
        });
    }

    static void testMangakakalot() {
        Arane.main(new String[] {
            "Samurai Deeper Kyo"
            , "http://mangakakalot.com/manga/samurai_deeper_kyo"
        });
    }

    static void testMangaLife() {
		Arane.main(new String[] {
			"TerraForMars"
			, "http://manga.life/read-online/TerraForMars"
		});
	}

	static void testBlogTruyen() {
		Arane.main(new String[] {
            "Psyren"
            , "http://blogtruyen.com/186/psyren"
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
            "Heroes_of_the_Spring_and_Autumn"
            , "http://read.egscans.com/Heroes_of_the_Spring_and_Autumn"
		});
	}

	static void testIzTruyenTranh() {
		Arane.main(new String[] {
			"trinity_wonder"
			, "http://iztruyentranh.com/truyen-tranh/trinity_wonder"
		});
	}
}
