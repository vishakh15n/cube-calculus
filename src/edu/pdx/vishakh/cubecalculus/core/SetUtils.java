package edu.pdx.vishakh.cubecalculus.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SetUtils {
    /** Returns the intersection of two sets. */
    public static <T> Set<T> findSetIntersection(Set<T> c1, Set<T> c2) {
        Set<T> overlap = new HashSet<>(c1);
        overlap.retainAll(c2);
        return overlap;
    }

    /** Returns true if set A contains set B, false if setB contains set A, null if neither. */
    public static <T> Boolean setAContainsB(Set<T> a, Set<T> b) {
        Set<T> c = findSetIntersection(a, b);
        if (c.equals(b)) return true;
        if (c.equals(a)) return false;
        return null;
    }

    /** Adds an object of type E into a map with type T against a set of type Set<E>. */
    public static <T,E> void addToSetInMap(T key, E c, Map<T, Set<E>> map) {
        Set<E> set;
        if (map.containsKey(key)) {
            set = map.get(key);
        } else {
            set = new HashSet<>();
            map.put(key, set);
        }
        set.add(c);
    }

    /** Remove an element of type E from a set that is the value against a key of type T. */
    public static <T, E> void removeFromSetInMap(T key, E c, Map<T, Set<E>> map) {
        Set<E> set;
        if (map.containsKey(key)) {
            set = map.get(key);
            set.remove(c);
            if (set.isEmpty()) {
                map.remove(key);
            }
        }
    }
}
