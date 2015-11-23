package lou.arane.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Alternative to {@link Stream}.
 *
 * @author LOU
 */
public class Pipe<T> {

    private final Stream<T> stream;

    public Pipe(T single) {
        this(Arrays.asList(single).stream());
    }

    public Pipe(Stream<T> stream) {
        this.stream = stream;
    }

    public <R> Pipe<R> map(Function<? super T, ? extends R> mapper) {
        return new Pipe<R>(stream.map(mapper));
    }

    public List<T> toList() {
        return stream.collect(Collectors.toList());
    }

    public Optional<T> first() {
        return stream.findFirst();
    }

}
