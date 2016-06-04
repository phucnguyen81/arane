package lou.arane.sandbox.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

/**
 * Base algorithms for working with trees.
 *
 * @author Phuc
 */
public class Trees {

	/**
	 * Perform a depth-first walk.
	 * Nodes visited along the way are reported to a visitor.
	 * <p>
	 * NOTE walking using recursion is much simpler
	 * but can blow the stack for deep trees.
	 */
	public static <T> void walk(
		T t
		, Function<T, Iterable<T>> getChildren
		, TreeVisitor<T> v
	) {
		Stack<Iterator<T>> runs = new Stack<>();
		onEnter(t, getChildren, v, runs);
		while (!runs.isEmpty()) {
			Iterator<T> run = runs.peek();
			if (run.hasNext()) {
				T n = run.next();
				if (run.hasNext()) {
					onEnter(n, getChildren, v, runs);
				} else {
					v.leave(n);
				}
			} else {
				runs.pop();
			}
		}
	}

	private static <T> void onEnter(
		T t
		, Function<T, Iterable<T>> getChildren
		, TreeVisitor<T> v
		, Stack<Iterator<T>> runs
	) {
		v.enter(t);
		Iterator<T> children = getChildren.apply(t).iterator();
		if (children.hasNext()) {
			List<T> nexts = new ArrayList<>();
			children.forEachRemaining(nexts::add);
			nexts.add(t);
			runs.push(nexts.iterator());
		}
		else {
			v.leave(t);
		}
	}

	/**
	 * Show tree with indentation, looks like:
	 * <pre>
	 * NodeA
	 * 		NodeB1
	 * 		NodeB2
	 * 			NodeC
	 * </pre>
	 */
	public static <T> String render(
		T tree
		, Function<T, Iterable<T>> getChildren
		, Function<T, String> toString
	) {
		StringBuilder str = new StringBuilder();
		walk(tree, getChildren, new TreeVisitor<T>() {
			StringBuilder indents = new StringBuilder();
			String indent = "  ";
			@Override
			public void enter(T t) {
				//indent
				str.append(indents).append(toString.apply(t));
				str.append(System.lineSeparator());
				indents.append(indent);
			}
			@Override
			public void leave(T t) {
				//dedent
				indents.delete(0, indent.length());
			}
		});
		return str.toString();
	}

}
