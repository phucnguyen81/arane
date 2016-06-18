package lou.arane.sandbox.tree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.TreeMap;

import lou.arane.util.Check;

/**
 * Build {@link SimpleTree} in a visual manner.
 *
 * @author Phuc
 */
public abstract class SimpleTreeBuilder {

	/** Represent indentation/depth */
    public static final Object __ = new Object();

    /**
     * Map from element depth to a list of nodes at that depth, in their
     * insertion order. This captures the state of this builder. There is a
     * single root node at level 0. Nodes at level 1 are nodes added first by
     * calling {@link #add(Object...)}.
     */
    private final TreeMap<Integer, LinkedList<SimpleTree>> trees;

    /** Create an builder with empty root node */
    public SimpleTreeBuilder() {
        this(new SimpleTree());
    }

    public SimpleTreeBuilder(SimpleTree root) {
        trees = new TreeMap<>();
        reset(root);
        build();
    }

    /**
     * Called from constructor {@link #SimpleTreeBuilder(SimpleTree)} to build the tree.
     */
    protected abstract void build();

    /** Reset the builder to build another tree given the new root */
    public final void reset(SimpleTree root) {
        trees.clear();
        registerNode(root, 0);
    }

    /** Reset the builder to build another tree with empty root */
    public final void reset() {
        reset(new SimpleTree());
    }

    /** Get the first child of root, which represents the tree being built.
     * Return empty if no children have been built or the builder was reset. */
    public final Optional<SimpleTree> getTree() {
        Integer treeKey = trees.higherKey(0);
        if (trees.containsKey(treeKey)) {
        	return Optional.of(trees.get(treeKey).peekFirst());
        }
        return Optional.empty();
    }

    /** Add the nodes taking identation as node depth */
    public final void add(Object... args) {
        int depth = getDepth(args);
        for (SimpleTree node : parseArguments(args)) {
            registerNode(node, depth);
        }
    }

    @Override
    public String toString() {
    	return String.format("%s:%n  %s", SimpleTreeBuilder.class.getSimpleName(), trees);
    }

    /** Return the nodes created from processing the arguments */
    private Iterable<SimpleTree> parseArguments(Object[] args) {
    	if (args.length == 0) {
    		throw new IllegalArgumentException("No arguments");
    	}
        Deque<SimpleTree> trees = new ArrayDeque<SimpleTree>();
        for (Object arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException(args + " contains null");
            }
            else if (arg == __) {
            	continue;
            }
            else if (arg instanceof SimpleTree) {
                trees.addLast(((SimpleTree) arg));
            }
            else if (arg instanceof Iterable) {
            	for (Object ele : (Iterable<?>) arg) {
                    if (ele == null) {
                        throw new IllegalArgumentException(arg + " contains null");
                    }
                    else if (ele instanceof SimpleTree) {
            			trees.addLast((SimpleTree) ele);
            		}
                    else {
                    	throw new IllegalArgumentException(arg + " contains non-Tree " + ele);
                    }
            	}
            }
            else {
            	if (trees.isEmpty()) {
            		trees.addLast(new SimpleTree());
            	}
            	trees.getLast().addAttr(arg);
            }
        }
        return trees;
    }

    /** Link the node just added to the tree being built */
    private void registerNode(SimpleTree node, int depth) {
        // keep track of node's depth to find its parent
        if (!trees.containsKey(depth)) {
            trees.put(depth, new LinkedList<SimpleTree>());
        }
        trees.get(depth).addLast(node);

        // link element to its parent
        findParent(depth).ifPresent(parent -> parent.addChild(node));
    }

    /**
     * Find parent node of the last inserted node. Knowning the node's depth is
     * enough to find its parent: the parent is the last inserted node that has
     * depth smaller than the child depth.
     */
    private Optional<SimpleTree> findParent(Integer childDepth) {
        Check.require(childDepth >= 0, "Expect non-negative tree depth");
        Integer parentDepth = trees.lowerKey(childDepth);
        if (parentDepth != null) {
            LinkedList<SimpleTree> parents = trees.get(parentDepth);
            return Optional.of(parents.peekLast());
        }
        return Optional.empty();
    }

    /**
     * Depth = number of indentations
     */
    private static int getDepth(Object[] args) {
        // root depth is 0, others start from 1
        int depth = 1;
        for (Object arg : args) {
            if (arg == __) depth += 1;
        }
        return depth;
    }

}
