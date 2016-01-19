package com.example.leahalpert.setsolver;

import android.net.Uri;
import android.widget.TextView;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetFinderTest {

    @Test
    public void testSetAlgorithm() {

        Card cardOne = new Card(
                Card.Shape.SQUIGGLE,
                Card.Shading.OPEN,
                Card.Count.ONE,
                Card.Color.GREEN, 1);

        Card cardTwo = new Card(
                Card.Shape.OVAL,
                Card.Shading.OPEN,
                Card.Count.ONE,
                Card.Color.GREEN, 2);

        Card cardThree = new Card(
                Card.Shape.DIAMOND,
                Card.Shading.OPEN,
                Card.Count.ONE,
                Card.Color.GREEN, 3);

        testFindSetWhenPresent(cardOne,  cardTwo,  cardThree);
    }

    void testFindSetWhenPresent(Card cardOne, Card cardTwo, Card cardThree) {
        ArrayList<Card> cards = new ArrayList<Card>(
                Arrays.asList(cardOne, cardTwo, cardThree));
        List<Triple> results = SetFinder.findSets(cards);
        Assert.assertEquals(1, results.size());
    }

}
