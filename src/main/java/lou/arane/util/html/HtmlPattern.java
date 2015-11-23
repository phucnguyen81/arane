package lou.arane.util.html;

import java.util.Stack;

import lou.arane.util.TreeBuilder;

import org.jsoup.nodes.Element;

/**
 * Search for html elements in an html document model parsed by Jsoup. This is
 * done in two steps: create a pattern to look for then use the pattern to look
 * for matches. Callbacks can be registered to process the matches.
 *
 * @author pnguyen58
 */
public class HtmlPattern extends TreeBuilder<Ele> {

    /**
     * Peform matching operation for all element under a given element
     * (including the given element and all its descendants)
     */
    public void matchAll(Element ele) {
        for (Element e : ele.getAllElements()) {
            match(e);
        }
    }

    /**
     * Match the pattern tree to a given element
     */
    public boolean match(Element element) {
        Ele pattern = getTree();
        return pattern != null && pattern.match(element);
    }

    public static Attr attr(String key) {
        return new Attr(key, null);
    }

    public static Attr attr(String key, String value) {
        return new Attr(key, value);
    }

    @Override
    protected void addChild(Ele parent, Ele child) {
        parent.appendChild(child);
    }

    /** Return the elements created from processing the arguments */
    @Override
    protected Iterable<Ele> parseArguments(Object[] args) {
        Stack<Ele> elements = new Stack<Ele>();
        for (Object arg : args) {
            if (arg == null) {
                elements.push(new Ele());
            }
            else if (arg instanceof Ele) {
                handleArg((Ele) arg, elements);
            }
            else if (arg instanceof Iterable) {
                handleArg((Iterable<?>) arg, elements);
            }
            else if (arg instanceof String) {
                handleArg((String) arg, elements);
            }
            else if (arg instanceof Attr) {
                handleArg((Attr) arg, elements);
            }
            else if (arg instanceof HtmlMatcher) {
                handleArg((HtmlMatcher) arg, elements);
            }
            else if (arg instanceof Runnable) {
                handleArg((Runnable) arg, elements);
            }
        }
        return elements;
    }

    private static void handleArg(Attr arg, Stack<Ele> elements) {
        if (elements.empty()) elements.push(new Ele());
        elements.peek().add(arg);
    }

    private static void handleArg(Ele arg, Stack<Ele> elements) {
        elements.push(arg);
    }

    private static void handleArg(Iterable<?> arg, Stack<Ele> elements) {
        for (Object ele : (Iterable<?>) arg) {
            if (ele != null && ele instanceof Ele) {
                elements.push((Ele) ele);
            }
        }
    }

    private void handleArg(String arg, Stack<Ele> elements) {
        int idx = arg.indexOf('=');
        if (idx < 0) {
            elements.add(new Ele(arg));
        }
        else {
            String attrKey = arg.substring(0, idx);
            String attrValue = arg.substring(idx + 1);
            if (elements.empty()) elements.push(new Ele());
            elements.peek().add(new Attr(attrKey, attrValue));
        }
    }

    private static void handleArg(HtmlMatcher arg, Stack<Ele> elements) {
        if (elements.empty()) elements.push(new Ele());
        elements.peek().eleHolder = (arg);
    }

    private static void handleArg(Runnable arg, Stack<Ele> elements) {
        if (elements.empty()) elements.push(new Ele());
        elements.peek().callbacks.add(arg);
    }
}