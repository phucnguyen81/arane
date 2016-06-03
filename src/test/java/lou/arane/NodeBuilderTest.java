package lou.arane;

import static lou.arane.sandbox.tree.NodeBuilder.__;

import java.util.Optional;

import org.junit.Test;

import lou.arane.sandbox.tree.Node;
import lou.arane.sandbox.tree.NodeBuilder;

public class NodeBuilderTest {

	@Test
	public void buildASimpleNode() {
		NodeBuilder b = new NodeBuilder();
		b.add("html");
		b.add(__, "header");
		b.getTree().ifPresent(n -> System.out.println(n));
	}
}
