package lou.arane;

import org.junit.Assert;
import org.junit.Test;

import lou.arane.sandbox.tree.SimpleTree;
import lou.arane.sandbox.tree.SimpleTreeBuilder;
import lou.arane.util.Util;

public class SimpleTreeBuilderTest extends Assert {

    @Test
    public void thatBuildTreeWorks() {
        SimpleTreeBuilder b = new SimpleTreeBuilder() {
            @Override
            protected void build() {
                add("html");
                add(__, "header");
                add(__, "body");
                add(__, __, "div");
            }
        };

        SimpleTree html = b.getTree().get();
        Util.println(html);

        assertTrue(html.hasAttr("html"));
        for (SimpleTree c : html.children()) {
            if (c.hasAttr("header")) {
                continue;
            }
            else if (c.hasAttr("body")) {
                SimpleTree div = c.children().iterator().next();
                assertTrue(div.hasAttr("div"));
            }
            else {
                fail("Children don't match");
            }
        }
    }

}
