package lou.arane.sandbox.tree;

import java.util.Iterator;
import java.util.Stack;
import java.util.function.Function;

/**
 * Generic tree algorithms.
 *
 * @author Phuc
 */
public class Tree<T> {

    private final T root;
    private final Function<T, Iterable<T>> getChildren;

    /** A tree, the the core, is anything that can yield children */
    public Tree(T root, Function<T, Iterable<T>> getChildren) {
        this.root = root;
        this.getChildren = getChildren;
    }

    /** Call {@link #walk(Object, Function, TreeVisitor)} */
    public final void walk(TreeVisitor<T> v) {
        walk(root, getChildren, v);
    }

    /** Call {@link #render(Object, Function, Function)} */
    public final String render(Function<T, String> toString) {
        return render(root, getChildren, toString);
    }

    /**
     * Perform a depth-first walk. Nodes visited along the way are reported to a
     * visitor.
     * <p>
     * NOTE walking using recursion is much simpler but can blow the stack for
     * deep trees. Here the stack is simulated with explicit use of {@link Stack}.
     */
    public static <T> void walk(final T base, Function<T, Iterable<T>> getChildren, TreeVisitor<T> v) {
        v.enter(base);
        Stack<T> toLeave = new Stack<>();
        toLeave.push(base);
        Stack<Iterator<T>> toEnter = new Stack<>();
        toEnter.push(getChildren.apply(base).iterator());
        do {
            Iterator<T> siblings = toEnter.peek();
            if (siblings.hasNext()) {
                T t = siblings.next();
                v.enter(t);
                Iterator<T> children = getChildren.apply(t).iterator();
                if (children.hasNext()) {
                    toEnter.push(children);
                    toLeave.push(t);
                }
                else {
                    v.leave(t);
                }
            }
            else {
                toEnter.pop();
                v.leave(toLeave.pop());
            }
        }
        while (!toEnter.isEmpty());
    }

    /**
     * Show tree with indentation, looks like:
     *
     * <pre>
     * NodeA
     * 		NodeB1
     * 		NodeB2
     * 			NodeC
     * </pre>
     */
    public static <T> String render(T tree, Function<T, Iterable<T>> getChildren,
            Function<T, String> toString) {
        StringBuilder str = new StringBuilder();
        walk(tree, getChildren, new TreeVisitor<T>() {
            StringBuilder indents = new StringBuilder();
            String indent = "  ";

            @Override
            public void enter(T t) {
                // indent
                str.append(indents).append(toString.apply(t));
                str.append(System.lineSeparator());
                indents.append(indent);
            }

            @Override
            public void leave(T t) {
                // dedent
                indents.delete(0, indent.length());
            }
        });
        return str.toString();
    }

}
