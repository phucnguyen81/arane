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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.ws.Holder;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterators;

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
        return new Holder<T>();
    }

    public static ToStringHelper toStringHelper(Object o) {
        return Objects.toStringHelper(o);
    }

    public static BufferedReader reader(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream, Util.defaultCharset()));
    }

    public static BufferedReader reader(Path path) {
        try {
            return Files.newBufferedReader(path, Util.defaultCharset());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Enumeration<T> enumeration(final Iterator<T> iterator) {
        return Iterators.asEnumeration(iterator);
    }

    public static <T> ArrayList<T> list() {
        return new ArrayList<T>();
    }

    @SafeVarargs
    public static <T> List<T> list(T... elements) {
        return Arrays.asList(elements);
    }

    public static <T> ArrayList<T> list(Collection<T> toCopy) {
        return new ArrayList<T>(toCopy);
    }

    public static <T> LinkedList<T> linkedList() {
        return new LinkedList<T>();
    }

    public static <T> HashSet<T> set() {
        return new HashSet<T>();
    }

    @SafeVarargs
    public static <T> HashSet<T> set(T first, T... more) {
        HashSet<T> set = new HashSet<T>(1 + more.length);
        set.add(first);
        for (T m : more) {
            set.add(m);
        }
        return set;
    }

    /** Create a set that honors insertion order */
    public static <T> LinkedHashSet<T> linkedHashSet() {
        return new LinkedHashSet<T>();
    }

    public static <K, V> HashMap<K, V> map() {
        return new HashMap<K, V>();
    }

    public static <K, V> IdentityHashMap<K, V> identityMap() {
        return new IdentityHashMap<K, V>();
    }

    public static HashMap<String, LinkedList<String>> map(Map<String, String[]> map) {
        HashMap<String, LinkedList<String>> newMap = new HashMap<String, LinkedList<String>>();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            newMap.put(entry.getKey(), new LinkedList<String>(Arrays.asList(entry.getValue())));
        }
        return newMap;
    }

    public static <K, V> ArrayListMultimap<K, V> multiMap() {
        return ArrayListMultimap.create();
    }

    public static <K, V> ArrayListMultimap<K, V> multiMap(Map<K, V[]> map) {
        ArrayListMultimap<K, V> multimap = ArrayListMultimap.create();
        for (Map.Entry<K, V[]> entry : map.entrySet()) {
            multimap.putAll(entry.getKey(), Arrays.asList(entry.getValue()));
        }
        return multimap;
    }
}
