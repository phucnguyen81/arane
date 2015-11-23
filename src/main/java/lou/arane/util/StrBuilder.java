package lou.arane.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.stringtemplate.v4.ST;

/**
 * Alternative to {@link StringBuilder} that provides a more convenient way to
 * add content and formatting support with {@link ST}.
 *
 * @author LOU
 */
public class StrBuilder {

    private static final String NEWLINE = System.lineSeparator();

    // store all parts added with one of the 'add' methods
    private final LinkedList<Object> parts;

    // the separator between the added parts
    private String separator;

    // attributes '{key:value}' where 'value' will replace placeholder $key$
    private final Map<String, List<Object>> attrs = new HashMap<String, List<Object>>();

    public StrBuilder(Object initialContent) {
        this();
        parts.add(initialContent);
    }

    public StrBuilder() {
        parts = new LinkedList<Object>();
    }

    public StrBuilder separator(String separator) {
        this.separator = separator;
        return this;
    }

    public StrBuilder join(Object x) {
        parts.add(x);
        return this;
    }

    public StrBuilder line(Object x) {
        parts.add(x);
        parts.add(NEWLINE);
        return this;
    }

    public StrBuilder newline() {
        parts.add(NEWLINE);
        return this;
    }

    public StrBuilder attr(String name, Object value) {
        if (!attrs.containsKey(name)) {
            attrs.put(name, new ArrayList<Object>());
        }
        attrs.get(name).add(value);
        return this;
    }

    /**
     * Return the string built so far. Attributes enclosed in $attribute$ are
     * rendered if at least one attribute is specified. If no attributes are
     * specified, the result is the same as {@link #toRawString()}.
     */
    @Override
    public String toString() {
        String str = toRawString();
        if (!attrs.isEmpty()) {
            ST st = new ST(toRawString(), '$', '$');
            for (Map.Entry<String, List<Object>> attr : attrs.entrySet()) {
                st.add(attr.getKey(), attr.getValue());
            }
            str = st.render();
        }
        return str;
    }

    /** Return the string built without rendering attributes */
    public String toRawString() {
        StringBuilder rawResult = new StringBuilder();
        Iterator<Object> partsIter = parts.iterator();
        while (partsIter.hasNext()) {
            Object part = partsIter.next();
            if (part != null) {
                rawResult.append(part.toString());
                if (separator != null && partsIter.hasNext()) {
                    rawResult.append(separator);
                }
            }
        }
        return rawResult.toString();
    }

}