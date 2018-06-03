package pl.edu.pw.elka.nlp;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;

public class MyDocument implements Comparable<MyDocument> {
    public String document;
    public HashMap<String, Integer> wordsInDocument;
    public int wordCounter;
    public double rating;
    public boolean isInCategory = false;

    @Override
    public String toString() {
        return "wordsInDocument = " + wordsInDocument + " \n\t with rating = " + rating;
    }

    @Override
    public int compareTo(@NotNull MyDocument o) {
        return Double.compare(this.rating, o.rating);
    }
}

