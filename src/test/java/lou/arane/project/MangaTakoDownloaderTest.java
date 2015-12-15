package lou.arane.project;

import java.nio.file.Path;
import java.util.regex.Pattern;

import lou.arane.util.Util;

import org.junit.Test;

/**
 * Download mangas from mangatako site
 *
 * @author LOU
 */
public class MangaTakoDownloaderTest {

    /** Small, completed manga suitable for regression test */
    public void YoureUnderArrest() {
        createDownloader("YoureUnderArrest", "You're Under Arrest!")
            .includeChapters("11")
            .run();
    }

    @Test
    public void Berserk() {
        createDownloader("Berserk", "Berserk")
            .includeChapters("336","337","338","339","340","341")
            .run();
    }

    public void TwentiethCenturyBoys() {
        download("20thCenturyBoys", "20th Century Boys");
    }

    public void Hive() {
        download("Hive", "Hive");
    }

    public void FiftyOneWaysToSaveMyGirlfriend() {
        download("51WaysToSaveMyGirlfriend", "51 Ways To Save My Girlfriend");
    }

    public void BattleRoyale() {
        download("BattleRoyale", "Battle Royale");
    }

    public void DragonQuestTheAdventureOfDai() {
        download("DragonQuestTheAdventureOfDai", "Dragon Quest The Adventure of Dai");
    }

    public void TheGamer() {
        Pattern fromChapter80 = Pattern.compile("([89]\\d+)|(\\d\\d\\d+)");
        createDownloader("TheGamer", "The Gamer")
            .setChapterFilter(fromChapter80.asPredicate())
            .run();
    }

    public void VinlandSaga() {
        createDownloader("VinlandSaga", "Vinland Saga")
            .run();
    }

    public void TerraForMars() {
        Pattern fromChapter123 = Pattern.compile("(12[3456789])|(13\\d)");
        createDownloader("TerraForMars", "Terra ForMars")
            .setChapterFilter(fromChapter123.asPredicate())
            .run();
    }

    public void ShingekiNoKyojin() {
        createDownloader("ShingekiNoKyojin", "Attack on Titan")
            .run();
    }

    /**
     * Download a manga series to a base directory
     *
     * @param seriesName = e.g. ShingekiNoKyojin
     * @param dirName = e.g. Attack on Titan
     */
    private static void download(String seriesName, String dirName) {
        createDownloader(seriesName, dirName).run();
    }

    /**
     * Create a downloader to download a manga series to a base directory
     *
     * @param seriesName = e.g. ShingekiNoKyojin
     * @param dirName = e.g. Attack on Titan
     */
    private static MangaTakoDownloader createDownloader(String seriesName, String dirName) {
        Path baseDir = Util.mangaDir("mangatako", dirName);
        return new MangaTakoDownloader(seriesName, baseDir);
    }
}
