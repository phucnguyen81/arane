package lou.arane.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.ws.Holder;

/**
 * Create commonly used objects
 *
 * @author LOU
 */
public class New {

    public static <T> Stream<T> stream(T single) {
        return Arrays.asList(single).stream();
    }

    public static <T> Holder<T> holder() {
        return new Holder<>();
    }

    public static BufferedReader reader(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream, IO.CHARSET));
    }

    public static BufferedReader reader(Path path) {
        try {
            return Files.newBufferedReader(path, IO.CHARSET);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> ArrayList<T> list() {
        return new ArrayList<>();
    }

    public static <T> ArrayList<T> list(Collection<T> toCopy) {
        return new ArrayList<>(toCopy);
    }

    public static <T> LinkedList<T> linkedList() {
        return new LinkedList<>();
    }

    public static <T> Collector<T, ?, List<T>> listCollector() {
        return Collectors.toList();
    }

    public static <T> HashSet<T> set() {
        return new HashSet<>();
    }

    /** Create a set that honors insertion order */
    public static <T> LinkedHashSet<T> linkedHashSet() {
        return new LinkedHashSet<>();
    }

    public static <K, V> HashMap<K, V> map() {
        return new HashMap<>();
    }

    public static <T> Stream<T> emptyStream() {
        return Collections.<T>emptyList().stream();
    }
}
