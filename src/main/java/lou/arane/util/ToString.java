package lou.arane.util;

import static java.util.stream.Collectors.joining;

import java.util.List;

import lou.arane.core.Cmd;

/**
 * Help generate output for implementing toString method. NOTE: call
 * {@link #render()} to generate output, not {@link #toString()}.
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

    private final List<Object> parts;

    private ToString(Class<?> c) {
        this.clazz = c;
        parts = New.list();
    }

    /**
     * Show string representation. NOTE: use {@link #render()} to get the
     * output.
     */
    @Override
    public String toString() {
        return String.format("%s(%s, %s)",
                ToString.class.getSimpleName(),
                clazz.getSimpleName(),
                parts);
    }

    /**
     * Get the output of the parts added so far.
     */
    public String render() {
        String attrs = parts.stream().map(Object::toString).collect(joining(", ", "(", ")"));
        return clazz.getSimpleName() + attrs;
    }

    public ToString lines(Iterable<Cmd> lines) {
        lines.forEach(this::line);
        return this;
    }

    public ToString line(Object part) {
        return line("", part);
    }

    public ToString line(Object name, Object part) {
        return join(System.lineSeparator() + name, part);
    }

    public ToString join(Object part) {
        return join("", part);
    }

    public ToString join(Object name, Object part) {
        if (name.equals("")) {
            parts.add(part);
        }
        else {
            parts.add(name + " = " + part);
        }
        return this;
    }

}
