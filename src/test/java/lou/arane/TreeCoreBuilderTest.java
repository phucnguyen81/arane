package lou.arane;

import org.junit.Assert;
import org.junit.Test;

import lou.arane.sandbox.tree.TreeCore;
import lou.arane.sandbox.tree.TreeCoreBuilder;
import lou.arane.util.Util;

public class TreeCoreBuilderTest extends Assert {

    @Test
    public void thatBuildTreeWorks() {
        TreeCoreBuilder b = new TreeCoreBuilder() {
            @Override
            protected void build() {
                add("html");
                add(__, "header");
                add(__, "body");
                add(__, __, "div");
            }
        };

        TreeCore html = b.getTree().get();
        Util.println(html);

        assertTrue(html.hasAttr("html"));
        for (TreeCore c : html.children()) {
            if (c.hasAttr("header")) {
                continue;
            }
            else if (c.hasAttr("body")) {
                TreeCore div = c.children().iterator().next();
                assertTrue(div.hasAttr("div"));
            }
            else {
                fail("Children don't match");
            }
        }
    }

}
