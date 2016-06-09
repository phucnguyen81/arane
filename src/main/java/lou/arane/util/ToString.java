package lou.arane.util;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * Help generate output for implementing toString method.
 * NOTE: call {@link #render()} to generate output, not {@link #toString()}.
 *
 * @author Phuc
 */
public class ToString {

    /**
     * Create instance given the class that needs to implement toString.
     */
    public static ToString of(Class<?> c) {
        return new ToString(c);
    }

    private final Class<?> clazz;

    private final Map<Object, Object> parts;

    private ToString(Class<?> c) {
        this.clazz = c;
        parts = new LinkedHashMap<>();
    }

    /**
     * Show string representation. NOTE: use {@link #render()} to get the output.
     */
    @Override
    public String toString() {
        return String.format("%s(%s, %s)", ToString.class.getSimpleName(), clazz.getSimpleName(), parts);
    }

    /**
     * Get the output of the parts added so far.
     */
    public String render() {
        return clazz.getSimpleName() + parts.entrySet().stream().map(e -> {
            if (e.getKey().equals("")) {
                return String.valueOf(e.getValue());
            }
            else {
                return e.getKey() + " = " + e.getValue();
            }
        }).collect(joining(", ", "(", ")"));
    }

    public ToString line(Object part) {
        return line("", part);
    }

    public ToString line(Object name, Object part) {
        return join(name, System.lineSeparator() + part);
    }

    public ToString join(Object part) {
        return join("", part);
    }

    public ToString join(Object name, Object part) {
        parts.put(name, part);
        return this;
    }

}
