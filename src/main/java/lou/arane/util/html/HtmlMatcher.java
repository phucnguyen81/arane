package lou.arane.util.html;

import lou.arane.util.New;

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
        return New.toStringHelper(this).addValue(ele).toString();
    }

}