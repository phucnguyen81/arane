package lou.arane.util;

import org.junit.Test;

/**
 * See how to handle urls normally found on the net.
 *
 * @author pnguyen58
 */
public class UriTest extends TestBase {

    /* source example that contains illegal spaces in its target */
    private final Uri uri = Uri.fromUrl(
    	"http://ex2.unixmanga.net/onlinereading/Break Blade/?image=Break Blade/0001.jpg&server=nas");

    @Test
    public void getPathAndFileName() {
        assertEquals("/onlinereading/Break+Blade/", uri.getFilePath());
        assertEquals("Break+Blade", uri.getFileName());
    }
}
