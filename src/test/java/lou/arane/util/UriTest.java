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
    private static final Uri URI_EXAMPLE = new Uri(
        "http://ex2.unixmanga.net/onlinereading/Break Blade/?image=Break Blade/0001.jpg&server=nas");

    @Test
    public void getPathAndFileName() {
        assertEquals(Paths.get("onlinereading","Break Blade"), URI_EXAMPLE.getFilePath());
        assertEquals(Paths.get("Break Blade"), URI_EXAMPLE.getFileName());
    }

    @Test
    public void getQueryParameters() {
        assertEquals("Break Blade/0001.jpg", URI_EXAMPLE.getQueryParm("image"));
        assertEquals("nas", URI_EXAMPLE.getQueryParm("server"));
    }

    @Test
    public void removeQuery() {
        Uri noQuery = URI_EXAMPLE.removeQuery();
        assertNull(noQuery.getQuery());
    }

}
