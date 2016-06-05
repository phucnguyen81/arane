package lou.arane.util;

import java.util.ArrayList;
import java.util.List;
import static java.util.stream.Collectors.joining;

/**
 * Help generate output for {@link #toString}
 *
 * @author Phuc
 */
public class ToString {

    private final Class<?> c;
    private final List<Object> parts;

    public ToString(Class<?> c) {
        parts = new ArrayList<>();
        this.c = c;
    }

    public String render() {
        StringBuilder b = new StringBuilder(c.getSimpleName());
        b.append(parts.stream().map(String::valueOf).collect(joining(",", "(", ")")));
        return b.toString();
    }

    public ToString line(Object part) {
        return join(part).join(System.lineSeparator());
    }

    public ToString line(Object name, Object part) {
        return join(name, part).join(System.lineSeparator());
    }

    public ToString join(Object part) {
        return join("", part);
    }

    public ToString join(Object name, Object part) {
        parts.add(name);
        parts.add(": ");
        parts.add(part);
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", ToString.class.getSimpleName(), parts);
    }

}
