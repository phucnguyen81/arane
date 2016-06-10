package lou.arane.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Create commonly used objects
 *
 * @author LOU
 */
public class New {

    public static <K, V> Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    public static <T> List<T> list() {
        return new ArrayList<T>();
    }

    public static <T> List<T> list(Iterable<T> items) {
        return stream(items).collect(Collectors.toList());
    }

    public static <T> List<T> list(Stream<T> items) {
        return items.collect(Collectors.toList());
    }

    public static <T> Stream<T> stream(Iterable<T> iter) {
        return StreamSupport.stream(iter.spliterator(), false);
    }

    public static <T> Stream<T> stream(Iterator<T> iter) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED),
                false);
    }

    public static BufferedReader reader(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream, IO.charset()));
    }

    public static BufferedReader reader(Path path) {
        try {
            return Files.newBufferedReader(path, IO.charset());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RuntimeException unchecked(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        else {
            return new RuntimeException(e);
        }
    }

}
