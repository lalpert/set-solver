package com.example.leahalpert.setsolver;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetFinder {

    public static List<Triple> findSets(List<Card> cards) {
        List<Triple> results = new ArrayList<>();

        int numCards = cards.size();
        for (int i = 0; i < numCards - 2; i++) {
            for (int j = i + 1; j < numCards - 1; j++) {
                for (int k = j + 1; k < numCards; k++) {
                    if (makesSet(cards.get(i), cards.get(j), cards.get(k))) {
                        results.add(new Triple(i, j, k));
                        Log.d("SET.FOUND", "set found: " + cards.get(i) + ", " + cards.get(j) +  ", " + cards.get(k));
                    }
                }
            }
        }
        return results;
    }

    private static boolean attrMakesSet(Enum firstAttribute, Enum secondAttribute,
                                        Enum thirdAttribute) {
        return ((firstAttribute == secondAttribute && secondAttribute == thirdAttribute) ||
                (firstAttribute != secondAttribute && secondAttribute != thirdAttribute
                        && firstAttribute != thirdAttribute));
    }

    private static boolean makesSet(Card firstCard, Card secondCard, Card thirdCard) {
        boolean colorsMatch = attrMakesSet(firstCard.color, secondCard.color, thirdCard.color);
        boolean numbersMatch = attrMakesSet(firstCard.count, secondCard.count, thirdCard.count);
        boolean fillsMatch = attrMakesSet(firstCard.shading, secondCard.shading, thirdCard.shading);
        boolean shapesMatch = attrMakesSet(firstCard.shape, secondCard.shape, thirdCard.shape);
        return colorsMatch && numbersMatch && fillsMatch && shapesMatch;
    }

}
