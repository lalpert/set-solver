package com.example.leahalpert.setsolver;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Triple implements Comparable<Triple>, Iterable<Integer> {
    List ints;

    public Triple(int i, int j, int k) {
        this.ints = Arrays.asList(i, j, k);
    }

    public List getInts() {
        return ints;
    }

    @Override
    public Iterator iterator() {
        return ints.iterator();
    }

    @Override
    public int compareTo(Triple another) {
        return getInts().toString().compareTo(another.getInts().toString());
    }

    @Override
    public String toString() {
        return ints.toString();
    }
}
