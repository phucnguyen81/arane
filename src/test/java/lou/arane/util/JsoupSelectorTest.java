package lou.arane.util;

import lou.arane.util.html.HtmlBuilder;

import org.jsoup.nodes.Element;
import org.junit.Test;

/**
 * Show how to use Jsoup selector to search for elements in an html document.
 *
 * @author LOU
 */
public class JsoupSelectorTest extends TestBase {

    private final Element htmlSample = new HtmlBuilder() {
        @Override
        protected void build() {
            add("html");
            add(__, "head");
            add(__, __, "meta", "http-equiv=Content-Type", "content=text/html");
            add(__, "body");
            add(__, __, "div");
            add(__, __, "img", "width=500");
            add(__, __, "img", "height=100");
        }
    }.extract();

    @Test
    public void simpleQueries() {
        String tagName = "div";
        assertEquals("<div></div>", htmlSample.select(tagName).toString());

        String attributeIgnoreTagName = "*[width=500]";
        assertEquals("<img width=\"500\" />", htmlSample.select(attributeIgnoreTagName).toString());

        String parentChild = "body img[height]";
        assertEquals("<img height=\"100\" />", htmlSample.select(parentChild).toString());

        String multipleAttributes = "meta[http-equiv=Content-Type][content=text/html]";
        assertEquals("<meta http-equiv=\"Content-Type\" content=\"text/html\" />", htmlSample
            .select(multipleAttributes).toString());
    }
}
