package lou.arane;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.junit.Test;

import lou.arane.html.HtmlBuilder;
import lou.arane.html.HtmlMatcher;
import lou.arane.html.HtmlPattern;

public class HtmlPatternTest extends TestBase {

    @Test
    public void matchSimplePatterns() {
        Element html = new HtmlBuilder() {
            @Override
            protected void build() {
                add("html", attr("lang", "en-US"));
                add(__, "head", attr("class", "header"));
            }
        }.extract();

        HtmlMatcher head = new HtmlMatcher();
        assertTrue(new HtmlPattern() {
            @Override
            protected void build() {
                add("html");
                add(__, "head", attr("class", "header"), head);
            }
        }.match(html));
        assertTrue(head.ele.tagName().equals("head"));

        assertFalse(new HtmlPattern() {
            @Override
            protected void build() {
                add("html", attr("lang", "en-US"));
                add(__, "head", attr("class", "commonHeader"));
            }
        }.match(html));
    }

    @Test
    public void matchWithAction() {
        Element html = new HtmlBuilder() {
            @Override
            protected void build() {
                add("html");
                add(__, "form", attr("id", "pages"));
                add(__, __, "input", attr("name", "series"));
                add(__, __, "input", attr("name", "chapter"));
                add(__, __, "input", attr("name", "index"));
                add(__, __, "select", attr("name", "page"));
                add(__, __, __, "option");
                add(__, __, __, "option");
            }
        }.extract();

        List<Element> options = new ArrayList<>();
        HtmlMatcher option = new HtmlMatcher();
        Runnable addOption = () -> options.add(option.ele);
        new HtmlPattern() {
            @Override
            protected void build() {
                add("form", "id=pages");
                add(__, "input", "name=series");
                add(__, "input", "name=chapter");
                add(__, "input", "name=index");
                add(__, "select", "name=page");
                add(__, __, "option", option, addOption);
            }
        }.matchAll(html);
        assertTrue(options.size() == 2);
    }

}
