package lou.arane.sandbox.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic tree with attributes and children. Attributes are external
 * information about the tree. Children are internal parts of the tree.
 * <p>
 * This tree is meant to be a temporary structure. It can be built with
 * {@link TreeCoreBuilder} then converted to other tree types.
 *
 * @author Phuc
 */
public class TreeCore {

    private final List<Object> attrs = new ArrayList<>();

    private final List<TreeCore> children = new ArrayList<>();

    public final boolean hasAttr(Object attr) {
        return attrs.contains(attr);
    }

    public final void addAttr(Object attr) {
        attrs.add(attr);
    }

    public final void addChild(TreeCore child) {
        children.add(child);
    }

    public final Iterable<TreeCore> children() {
        return children;
    }

    @Override
    public String toString() {
        return Tree.render(this, t -> t.children, t -> t.attrs.toString());
    }
}