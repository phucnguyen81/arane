package lou.arane.util;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Optional;

/**
 * Help generate output for implementing toString method. NOTE: call
 * {@link #str()} to generate output, not {@link #toString()}.
 *
 * @author Phuc
 */
public class ToString {

    /**
     * Create instance given the class that needs to implement toString.
     */
    public static ToString of(Class<?> c) {
        return new ToString(Optional.of(c.getSimpleName()));
    }

    public static ToString of() {
        return new ToString(Optional.empty());
    }

    private final Optional<String> clazz;

    private final List<String> args;

    private ToString(Optional<String> c) {
        this.clazz = c;
        args = New.list();
    }

    /**
     * Show string representation. NOTE: use {@link #str()} to get the
     * output.
     */
    @Override
    public String toString() {
        String thisClass = ToString.class.getSimpleName();
        if (clazz.isPresent()) {
            return String.format("%s(%s, %s)", thisClass, clazz.get(), args);
        }
        else {
            return String.format("%s(%s)", thisClass, args);
        }
    }

    /**
     * Append the arguments to the ends of this
     */
    public ToString add(Object first, Object... rest) {
        args.add(first.toString());
        for (Object r : rest) {
            args.add(r.toString());
        }
        return this;
    }

    /**
     * Append a system line-separator to the end of this
     */
    public ToString nln() {
        args.add(System.lineSeparator());
        return this;
    }

    /**
     * Get the output of the arguments added so far.
     */
    public String str() {
        if (clazz.isPresent()) {
            return args.stream().collect(joining("", clazz.get() + "(", ")"));
        }
        else {
            return args.stream().collect(joining());
        }
    }

}
