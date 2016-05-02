package lou.arane.util.html;

import org.jsoup.nodes.Element;

/** Keep a matching result of an {@link Element} */
public class HtmlMatcher {

    public Element ele;

    public String text() {
        return ele ==  null ? null : ele.text();
    }

    /** @see {@link Element#val} */
    public String val() {
        return ele == null ? null : ele.val();
    }

    @Override
    public String toString() {
    	return String.format("HtmlMatcher(%s)", ele);
    }

}