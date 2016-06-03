package lou.arane.util;

import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Base class for building trees in a visual manner.
 *
 * @param <N> = type of tree node
 *
 * @author LOU
 */
public abstract class TreeBuilder<N> {

    /** Represent indentation/depth */
    public static final Object __ = new Object();

    /**
     * Map from element depth to a list of nodes at that depth, in their
     * insertion order. This captures the state of this builder. There is a
     * single root node at level 0. Nodes at level 1 are nodes added first by
     * calling {@link #add(Object...)}.
     */
    private final TreeMap<Integer, LinkedList<N>> nodes;

    /** Create an builder with null root node */
    public TreeBuilder() {
        this(null);
    }

    public TreeBuilder(N root) {
        nodes = new TreeMap<>();
        reset(root);
        build();
    }

    /** Reset the builder to build another tree given its root */
    public void reset(N root) {
        nodes.clear();
        registerNode(root, 0);
    }

    /** Reset the builder to build another tree */
    public void reset() {
        reset(null);
    }

    /**
     * Template Method called from constructor for initializing code.
     */
    protected void build() {}

    /** Get the first node added to root, which represents the tree being built */
    protected N getTree() {
        N tree = null;
        Integer treeKey = nodes.higherKey(0);
        if (nodes.containsKey(treeKey)) {
            tree = nodes.get(treeKey).peekFirst();
        }
        return tree;
    }

    /**
     * Register a node to be built.
     *
     * @see #parseArguments(Object[]) for how arguments are handled
     */
    public void add(Object... args) {
        int depth = getDepth(args);
        for (N node : parseArguments(args)) {
            registerNode(node, depth);
        }
    }

    /** Return the nodes created from processing the arguments */
    protected abstract Iterable<N> parseArguments(Object[] args);

    /** Link the node just added to to tree being built */
    private void registerNode(N node, int depth) {
        // keep track of node's depth to find its parent
        if (!nodes.containsKey(depth)) {
            nodes.put(depth, new LinkedList<N>());
        }
        nodes.get(depth).addLast(node);

        // link element to its parent
        N parent = findParent(depth);
        if (parent != null) {
            addChild(parent, node);
        }
    }

    /**
     * Find parent node of the last inserted node. Knowning the node's depth is
     * enough to find its parent: the parent is the last inserted node that has
     * depth smaller than the child depth.
     */
    private N findParent(Integer childDepth) {
        Check.require(childDepth >= 0, "Expect non-negative tree depth");
        N parent = null;
        Integer parentDepth = nodes.lowerKey(childDepth);
        if (parentDepth != null) {
            LinkedList<N> parents = nodes.get(parentDepth);
            parent = parents.peekLast();
        }
        return parent;
    }

    /** Attempt to add child to parent */
    protected abstract void addChild(N parent, N child);

    /**
     * Depth = number of indentations
     */
    private static int getDepth(Object[] args) {
        // root depth is 0, others start from 1
        int depth = 1;
        for (Object arg : args) {
            if (arg == __) ++depth;
        }
        return depth;
    }

    @Override
    public String toString() {
    	return String.format("%s:%n  %s", TreeBuilder.class.getSimpleName(), nodes);
    }

}
