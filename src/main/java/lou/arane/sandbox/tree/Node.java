package lou.arane.sandbox.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic node with attributes and children.
 * Attributes are external information about the node.
 * Children are internal parts of the node.
 *
 * @author Phuc
 */
public class Node {

	private final List<Object> attrs = new ArrayList<>();

	private final List<Node> children = new ArrayList<>();

	public void addAttr(Object attr) {
		attrs.add(attr);
	}

	public void addChild(Node child) {
		children.add(child);
	}

	@Override
	public String toString() {
		return String.format("Node:%n%s%n%s", attrs, children);
	}
}