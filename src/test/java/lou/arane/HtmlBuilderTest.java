package lou.arane;

import org.jsoup.nodes.Element;
import org.junit.Test;

import lou.arane.html.HtmlBuilder;

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
