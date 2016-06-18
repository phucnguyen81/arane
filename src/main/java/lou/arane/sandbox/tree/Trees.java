package lou.arane.sandbox.tree;

import java.util.Iterator;
import java.util.Stack;
import java.util.function.Function;

/**
 * Base algorithms for working with trees.
 *
 * @author Phuc
 */
public class Trees {

    /**
     * Perform a depth-first walk. Nodes visited along the way are reported to a
     * visitor.
     * <p>
     * NOTE walking using recursion is much simpler but can blow the stack for
     * deep trees.
     */
    public static <T> void walk(T t, Function<T, Iterable<T>> getChildren, TreeVisitor<T> v) {
        Stack<T> parents = new Stack<>();
        Stack<Iterator<T>> children = new Stack<>();
        visit(t, getChildren, v, children, parents);
        while (!children.isEmpty()) {
            Iterator<T> run = children.peek();
            if (run.hasNext()) {
                T n = run.next();
                visit(n, getChildren, v, children, parents);
            }
            else {
                children.pop();
                v.leave(parents.pop());
            }
        }
    }

    private static <T> void visit(T t, Function<T, Iterable<T>> getChildren, TreeVisitor<T> v,
            Stack<Iterator<T>> runs, Stack<T> parents) {
        v.enter(t);
        Iterator<T> children = getChildren.apply(t).iterator();
        if (children.hasNext()) {
            runs.push(children);
            parents.push(t);
        }
        else {
            v.leave(t);
        }
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
