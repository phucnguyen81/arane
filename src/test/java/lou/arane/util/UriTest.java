package lou.arane.util;

import java.nio.file.Paths;

import org.junit.Test;

/**
 * See how to handle urls normally found on the net.
 *
 * @author pnguyen58
 */
public class UriTest extends TestBase {

    /* uri example that contains illegal spaces in its path */
    private final Uri uri = Uri.fromUrl(
    	"http://ex2.unixmanga.net/onlinereading/Break Blade/?image=Break Blade/0001.jpg&server=nas");

    @Test
    public void getPathAndFileName() {
        assertEquals(Paths.get("onlinereading","Break+Blade"), uri.getFilePath());
        assertEquals(Paths.get("Break+Blade"), uri.getFileName());
    }
}
