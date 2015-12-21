package com.example.leahalpert.setsolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetFinder {


    public static List<List<Integer>> findSets(Card[] args) {
        List<List<Integer>> results = new ArrayList<List<Integer>>();

        int numCards = args.length;
        for (int i = 0; i < numCards - 2; i++) {
            for (int j = i + 1; j < numCards - 1; j++) {
                for (int k = j; k < numCards; k++) {
                    if (makesSet(args[i], args[j], args[k])) {
                        results.add(Arrays.asList(i, j, k));
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

    /*
    public void main() {
        Card cardOne = new Card(
                Shape.SQUIGGLE,
                Shading.OPEN,
                Count.ONE,
                Color.GREEN, 1);

        Card cardTwo = new Card(
                Shape.OVAL,
                Shading.OPEN,
                Count.ONE,
                Color.GREEN, 1);

        Card cardThree = new Card(
                Shape.DIAMOND,
                Shading.OPEN,
                Count.ONE,
                Color.GREEN, 1);


        testFindSetWhenPresent(cardOne, cardTwo, cardThree);
    }

    void testFindSetWhenPresent(Card cardOne, Card cardTwo, Card cardThree) {
        ArrayList<Card> cards = new ArrayList<Card>(
                Arrays.asList(cardOne, cardTwo, cardThree));
        Card[] cardArr = cards.toArray(new Card[cards.size()]);
        SetFinder finder = new SetFinder(cardArr);
        System.out.println(finder.getResults());
    }
        */