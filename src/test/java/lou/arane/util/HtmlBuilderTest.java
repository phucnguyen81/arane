package lou.arane.util;

import lou.arane.util.html.HtmlBuilder;

import org.jsoup.nodes.Element;
import org.junit.Test;

public class HtmlBuilderTest extends TestBase {

    @Test
    public void buildSimpleHtml() {
        Element html = new HtmlBuilder() {
            @Override
            protected void build() {
                add("html", attr("lang", "en-US"));
                add(__, "head", "class=header");
            }
        }.extract();

        assertTrue(html.hasAttr("lang"));
        assertTrue(html.getElementsByTag("head").hasAttr("class"));
    }

}
