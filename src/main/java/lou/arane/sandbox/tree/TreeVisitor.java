package lou.arane.sandbox.tree;

/**
 * Visit trees like structures.
 * Each node is visited twice upon {@link #enter} and {@link #leave}.
 * A node is entered before its children and left after its children.
 * <p>
 * Note that a visitor can reconstruct the entire tree.
 * In other words, a visitor can do anything with the tree.
 *
 * @author Phuc
 */
public interface TreeVisitor<T> {

	/**
	 * Visit a node the first time.
	 * The visitor must enter a node before entering its children.
	 */
	void enter(T t);

	/**
	 * Visit a node the second (also last) time.
	 * The visitor must leave a node after leaving its children.
	 */
	void leave (T t);

}