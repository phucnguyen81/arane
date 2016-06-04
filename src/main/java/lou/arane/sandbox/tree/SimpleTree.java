package lou.arane.sandbox.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic tree with attributes and children.
 * Attributes are external information about the tree.
 * Children are internal parts of the tree.
 * <p>
 * This tree is meant to be a temporary structure.
 * It can be built with {@link SimpleTreeBuilder}
 * then converted to other tree types.
 *
 * @author Phuc
 */
public class SimpleTree {

	private final List<Object> attrs = new ArrayList<>();

	private final List<SimpleTree> children = new ArrayList<>();

	public void addAttr(Object attr) {
		attrs.add(attr);
	}

	public void addChild(SimpleTree child) {
		children.add(child);
	}

	@Override
	public String toString() {
		return Trees.render(this, t -> t.children, t -> t.attrs.toString());
	}
}