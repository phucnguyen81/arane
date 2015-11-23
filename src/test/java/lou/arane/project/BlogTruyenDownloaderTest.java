package lou.arane.project;

import java.nio.file.Path;

import lou.arane.util.Util;

import org.junit.Test;

/**
 * Download manga from blogtruyen
 *
 * @author LOU
 */
public class BlogTruyenDownloaderTest {

    @Test
    public void BayVienNgocRong() {
        download("dragon-ball-original", "7 Vien Ngoc Rong");
    }

    public void AStoryOfHeroes() {
        download("a-story-of-heroes", "A Story of Heroes");
    }

    public void Hakaijuu() {
        download("hakaijuu", "Hakaijuu");
    }

    public void TwentiethCenturyBoys() {
        download("20th-century-boys", "20th-century-boys");
    }

    public void ThienLongBatBo() {
        download("thien-long-bat-bo", "Thien Long Bat Bo");
    }

    public void PhongVan() {
        download("phong-van", "Phong Van");
    }

    public void TuyetTheVoSong() {
        download("tuyet-the-vo-song", "Tuyet The Vo Song");
    }

    public void ThapKy() {
        download("thap-ky", "Thap Ky");
    }

    public void Haikyuu() {
        download("haikyuu", "Haikyuu");
    }

    public void NguoiTrongGiangHo() {
        download("nguoi-trong-giang-ho", "Nguoi Trong Giang Ho");
    }

    public void ThietTuongTungHoanh() {
        download("thiet-tuong-tung-hoanh", "Thiet Tuong Tung Hoanh");
    }

    public void Hive() {
        download("hive", "Hive");
    }

    /** Download a manga given its base directory */
    private static void download(String story, String dirName) {
        Path baseDir = Util.mangaDir("blogtruyen", dirName);
        new BlogTruyenDownloader(story, baseDir).run();
    }

}
