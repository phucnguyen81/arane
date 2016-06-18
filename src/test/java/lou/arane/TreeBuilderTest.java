package lou.arane;

import static lou.arane.sandbox.tree.SimpleTreeBuilder.__;

import org.junit.Test;

import lou.arane.sandbox.tree.SimpleTreeBuilder;
import lou.arane.util.Util;

public class TreeBuilderTest {

    /** TODO proper test */
	@Test
	public void build() {
		SimpleTreeBuilder b = new SimpleTreeBuilder();
		b.add("html");
		b.add(__, "header");
		b.add(__, "body");
		b.add(__, __, "div");
		b.getTree().ifPresent(n -> Util.println(n));
	}
}
