package lou.arane.sandbox.tree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.TreeMap;

import lou.arane.util.Check;

/**
 * Build {@link Node} in a visual manner.
 *
 * @author Phuc
 */
public final class NodeBuilder {

	/** Represent indentation/depth */
    public static final Object __ = new Object();

    /**
     * Map from element depth to a list of nodes at that depth, in their
     * insertion order. This captures the state of this builder. There is a
     * single root node at level 0. Nodes at level 1 are nodes added first by
     * calling {@link #add(Object...)}.
     */
    private final TreeMap<Integer, LinkedList<Node>> nodes;

    /** Create an builder with empty root node */
    public NodeBuilder() {
        this(new Node());
    }

    public NodeBuilder(Node root) {
        nodes = new TreeMap<>();
        reset(root);
    }

    /** Reset the builder to build another tree given the new root */
    public void reset(Node root) {
        nodes.clear();
        registerNode(root, 0);
    }

    /** Reset the builder to build another tree with empty root */
    public void reset() {
        reset(new Node());
    }

    /** Get the first child of root, which represents the tree being built */
    public Optional<Node> getTree() {
        Integer treeKey = nodes.higherKey(0);
        if (nodes.containsKey(treeKey)) {
        	return Optional.of(nodes.get(treeKey).peekFirst());
        }
        return Optional.empty();
    }

    /** Add the nodes taking identation as node depth */
    public void add(Object... args) {
        int depth = getDepth(args);
        for (Node node : parseArguments(args)) {
            registerNode(node, depth);
        }
    }

    @Override
    public String toString() {
    	return String.format("%s:%n  %s", NodeBuilder.class.getSimpleName(), nodes);
    }

    /** Return the nodes created from processing the arguments */
    private Iterable<Node> parseArguments(Object[] args) {
    	if (args.length == 0) {
    		throw new IllegalArgumentException("No arguments");
    	}
        Deque<Node> nodes = new ArrayDeque<Node>();
        for (Object arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException(args + " contains null");
            }
            else if (arg == __) {
            	continue;
            }
            else if (arg instanceof Node) {
                nodes.addLast(((Node) arg));
            }
            else if (arg instanceof Iterable) {
            	for (Object ele : (Iterable<?>) arg) {
                    if (ele == null) {
                        throw new IllegalArgumentException(arg + " contains null");
                    }
                    else if (ele instanceof Node) {
            			nodes.addLast((Node) ele);
            		}
                    else {
                    	throw new IllegalArgumentException(arg + " contains non-Node " + ele);
                    }
            	}
            }
            else {
            	if (nodes.isEmpty()) {
            		nodes.addLast(new Node());
            	}
            	nodes.getLast().addAttr(arg);
            }
        }
        return nodes;
    }

    /** Link the node just added to to tree being built */
    private void registerNode(Node node, int depth) {
        // keep track of node's depth to find its parent
        if (!nodes.containsKey(depth)) {
            nodes.put(depth, new LinkedList<Node>());
        }
        nodes.get(depth).addLast(node);

        // link element to its parent
        findParent(depth).ifPresent(parent -> parent.addChild(node));
    }

    /**
     * Find parent node of the last inserted node. Knowning the node's depth is
     * enough to find its parent: the parent is the last inserted node that has
     * depth smaller than the child depth.
     */
    private Optional<Node> findParent(Integer childDepth) {
        Check.require(childDepth >= 0, "Expect non-negative tree depth");
        Integer parentDepth = nodes.lowerKey(childDepth);
        if (parentDepth != null) {
            LinkedList<Node> parents = nodes.get(parentDepth);
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
