package lou.arane.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;

/** Used to model element */
class Ele {
    final String tag;
    final Map<String, Attr> attrs = new HashMap<>();
    final LinkedList<Ele> children = new LinkedList<>();
    HtmlMatcher eleHolder;
    final List<Runnable> callbacks = new ArrayList<>();

    Ele() {
        this(null);
    }

    Ele(String tag) {
        this.tag = tag;
    }

    void add(Attr attr) {
        attrs.put(attr.key, attr);
    }

    void appendChild(Ele child) {
        children.add(child);
    }

    /** TODO this is a complex method that needs refactoring */
    boolean match(Element ele) {
        if (tag != null && !tag.equalsIgnoreCase(ele.tagName())) {
            /* not a match if tag does not match */
            return false;
        }

        for (Attr attr : attrs.values()) {
            /* not a match if some attribute does not match */
            if (!ele.hasAttr(attr.key)) {
                return false;
            }
            else if (attr.value != null && !attr.value.equalsIgnoreCase(ele.attr(attr.key))) {
                return false;
            }
        }

        /* from here on, both tag and attributes match; we execute the
         * callback then proceed to compare children */

        if (eleHolder != null) {
            eleHolder.ele = ele;
        }
        for (Runnable callback : callbacks) {
            callback.run();
        }

        List<Element> elements = new ArrayList<>(ele.children());
        for (Ele pattern : children) {
            int firstMatch = -1;
            for (int i = 0; i < elements.size(); i++) {
                if (pattern.match(elements.get(i))) {
                    if (firstMatch < 0) firstMatch = i;
                }
            }
            if (firstMatch < 0) return false;
            else elements = elements.subList(firstMatch + 1, elements.size());
        }

        /* both this and descendants match */
        return true;
    }
}