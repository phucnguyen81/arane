package lou.arane.project.mangalife;

import java.nio.file.Path;

import lou.arane.util.Log;
import lou.arane.util.Util;

/**
 * Download mangas from manga-life site
 *
 * @author LOU
 */
public class MangaLifeMain {

    public static void main(String[] args) {
        ouroboros();
    }

    static void ouroboros() {
        download("Ouroboros", "Ouroboros");
    }

    static void theBreakerNewWaves() {
        download("TheBreakerNewWaves", "The Breaker - New Waves");
    }

    static void historyStrongestDiscipleKenichi() {
        download("HistoryStrongestDiscipleKenichi", "Strongest Disciple Kenichi");
    }

    static void vinlandSaga() {
        download("VinlandSaga", "Vinland Saga");
    }

    /** A gate suddenly opens connecting Japan to a fantasy world */
    static void gate() {
        download("GateJietaiKareNoChiNiteKakuTatakeri", "Gate");
    }

    /** In a world where super-power is commonplace,
     * a boy without power yearns to become a super-hero */
    static void heroAcademia() {
        download("BokuNoHeroAcademia", "Boku no Hero Academia");
    }

    private static void download(String mangaName, String dir) {
        Path mangaDir = Util.mangaDir("mangalife", dir);
        Log.addOutFile(mangaDir.resolve("log.txt").toString());
        new MangaLifeDownloader(mangaName, mangaDir).run();
    }
}
