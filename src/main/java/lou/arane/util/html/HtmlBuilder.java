package lou.arane.util.html;

import java.util.ArrayDeque;
import java.util.Deque;

import lou.arane.util.TreeBuilder;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

/**
 * Build Jsoup html structure in a convenient, visual way
 *
 * <pre>
 * Example of creating an html element:
 *
 *  html = new HtmlBuilder() {
 *      @Override
 *      protected void build() {
 *          add("html", attr("lang", "en-US"));
 *          add(__, "head");
 *          add(__, "body", attr("class", "commonBody"));
 *      }
 *  }.extract();
 * </pre>
 *
 * @author pnguyen58
 */
public class HtmlBuilder extends TreeBuilder<Element> {

    private static final Attribute NULL_ATTR = new Attribute("NULL", "");

    /** Create an html builder with no root */
    public HtmlBuilder() {}

    /** Create an html builder from a root element */
    public HtmlBuilder(Element root) {
        super(root);
    }

    @Override
    protected void addChild(Element parent, Element child) {
        if (child.baseUri().isEmpty()) {
            // inherit base source
            child.setBaseUri(parent.baseUri());
        }
        parent.appendChild(child);
    }

    /**
     * Get a node representing the tree from the internal nodes built so far.
     * This method also resets this builder.
     */
    public Element extract() {
        Element tree = getTree();
        if (tree != null) {
            removeNullElements(tree);
        }
        reset();
        return tree;
    }

    /**
     * Remove all nodes that are created by this class to mean a null node
     */
    private static void removeNullElements(Element tree) {
        for (Element ele : tree.getAllElements()) {
            for (Attribute attr : ele.attributes()) {
                if (attr == NULL_ATTR) ele.remove();
            }
        }
    }

    /** Return the elements created from processing the arguments */
    @Override
    protected Iterable<Element> parseArguments(Object[] args) {
        Deque<Element> elements = new ArrayDeque<Element>();
        for (Object arg : args) {
            if (arg == null) {
                elements.add(nullElement());
            }
            else if (arg instanceof Element) {
                elements.add(((Element) arg));
            }
            else if (arg instanceof String) {
                String str = (String) arg;
                int idx = str.indexOf('=');
                if (idx < 0) {
                    Tag tag = Tag.valueOf((String) arg);
                    Element ele = new Element(tag, "");
                    elements.add(ele);
                }
                else if (!elements.isEmpty()) {
                    String attrKey = str.substring(0, idx);
                    String attrValue = str.substring(idx + 1);
                    elements.getLast().attr(attrKey, attrValue);
                }
            }
            else if (arg instanceof Attribute && !elements.isEmpty()) {
                Attribute attr = (Attribute) arg;
                elements.getLast().attr(attr.getKey(), attr.getValue());
            }
            else if (arg instanceof Iterable) {
                for (Object ele : (Iterable<?>) arg) {
                    if (ele != null && ele instanceof Element) {
                        elements.add((Element) ele);
                    }
                }
            }
        }
        return elements;
    }

    /** Make a node that will be removed when the tree is extracted */
    private static Element nullElement() {
        Attributes nullAttrs = new Attributes();
        nullAttrs.put(NULL_ATTR);
        Element nullEle = new Element(Tag.valueOf("NULL"), "", nullAttrs);
        return nullEle;
    }

    /** Shorthand for attribute */
    public static Attribute attr(String key, String value) {
        return new Attribute(key, value);
    }

}
