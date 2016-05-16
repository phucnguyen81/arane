package lou.arane;

import java.net.URL;

import org.junit.Test;

import lou.arane.base.URLResource;

/**
 * See how to handle urls normally found on the net.
 *
 * @author pnguyen58
 */
public class UrlTest extends TestBase {

    /* source example that contains illegal spaces in its target */
    private final URLResource url = new URLResource(URLResource.encode((
    	"http://ex2.unixmanga.net/onlinereading/Break Blade/?image=Break Blade/0001.jpg&server=nas")));

    @Test
    public void getPathAndFileName() {
        assertEquals("/onlinereading/Break+Blade/", url.filePath());
        assertEquals("Break+Blade", url.fileName());
    }

    /** Can create URL from any string as long as protocol is specified */
    @Test
    public void createNullURL() throws Exception {
    	new URL("http:");
    }
}
