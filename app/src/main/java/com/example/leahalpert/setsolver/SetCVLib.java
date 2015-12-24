package com.example.leahalpert.setsolver;

import android.util.Log;
import android.util.Pair;

import com.example.leahalpert.setsolver.contours.CardContour;
import com.example.leahalpert.setsolver.contours.Diamond;
import com.example.leahalpert.setsolver.contours.Oval;
import com.example.leahalpert.setsolver.contours.Squiggle;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by russell on 12/21/15.
 * <p/>
 * CV Library to find and identify set cards!
 */
public class SetCVLib {
    final static double CardFilterThreshold = 0.07;
    final static int MaxCardLikeObjects = 18;
    final static String Tag = "SetCVLib";

    public static Mat computeAndCircleSets(Mat input) {
        // TODO: carefully clone the input
        List<MatOfPoint> cardContours = extractCards(input);
        for (MatOfPoint card : cardContours) {
            Mat flattened = flattenCard(card, input);
            Card recognized = recognizeCard(flattened);
            if (recognized != null) {
                Log.i(Tag, recognized.toString());
            } else {
                Log.i(Tag, "Could not find");
                return flattened;
            }
        }

        return input;
    }

    private static Mat prepImage(Mat img, int thresh) {

        // TODO: try adaptive
        Mat ret = img.clone();
        Imgproc.cvtColor(ret, ret, Imgproc.COLOR_BGR2GRAY);
        Size kern = new Size(1, 1);
        Imgproc.GaussianBlur(ret, ret, kern, 1000);
        Imgproc.threshold(ret, ret, thresh, 255, Imgproc.THRESH_BINARY);
        return ret;
    }

    private static List<MatOfPoint> areaSortedContours(Mat inp, int method) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hier = new Mat();
        Imgproc.findContours(inp, contours, hier, method, Imgproc.CHAIN_APPROX_SIMPLE);
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint lhs, MatOfPoint rhs) {
                return -Double.compare(Imgproc.contourArea(lhs), Imgproc.contourArea(rhs));
            }
        });
        return contours;
    }

    private static List<MatOfPoint> extractCards(Mat img) {
        Mat clone = prepImage(img, 120);

        List<MatOfPoint> contours = areaSortedContours(clone, Imgproc.RETR_TREE);

        List<MatOfPoint> filteredContours = new ArrayList<>();
        for (MatOfPoint cnt : contours.subList(0, Math.min(MaxCardLikeObjects, contours.size()))) {
            double score = Imgproc.matchShapes(CardContour.contour, cnt, 1, 0.0);
            Log.i(Tag, "Card Score: " + score);
            Log.i(Tag, "Area:" + Imgproc.contourArea(cnt));
            if (score < CardFilterThreshold) {
                filteredContours.add(cnt);
            }
        }

        Log.i(Tag, "Found " + filteredContours.size() + " contours");

        Imgproc.drawContours(img, filteredContours, -1, new Scalar(100, 100, 0), 30);
        return filteredContours;
    }

    /**
     * Rotate a card into the canonical orientation
     */
    private static Mat flattenCard(MatOfPoint contour, Mat image) {
        final int Width = 180;
        final int Height = 116;
        MatOfPoint2f m2f = new MatOfPoint2f(contour.toArray());
        double perimeter = Imgproc.arcLength(m2f, true);
        MatOfPoint2f cornersMat = new MatOfPoint2f();

        // We now have a 4 point approximation for our card.
        // We need to determine which point corresponds to which corner.
        // Since it is a set card, and we won't handle extreme perspective, it is sufficient
        // to assume the longer edge is in fact the long edge of the card.
        Imgproc.approxPolyDP(m2f, cornersMat, 0.02 * perimeter, true);

        List<Point> corners = new ArrayList<>();
        Converters.Mat_to_vector_Point2f(cornersMat, corners);
        if (corners.size() != 4) {
            throw new RuntimeException("Not a quadrilateral");
        }

        // Create the target points
        // Long edge -> short edge -> long edge -> short edge
        List<Point> destPoints = new ArrayList<>(Arrays.asList(
                new Point(0, 0),
                new Point(Width, 0),
                new Point(Width, Height),
                new Point(0, Height)));

        Mat destMat = Converters.vector_Point2f_to_Mat(destPoints);

        // Create the source points
        List<Point> orderedCorners = sortedCorners(corners);
        Mat srcMat = Converters.vector_Point2f_to_Mat(orderedCorners);
        Mat transform = Imgproc.getPerspectiveTransform(srcMat, destMat);
        Size sz = new Size(Width, Height);
        Mat dest = new Mat(sz, image.type());
        Imgproc.warpPerspective(image, dest, transform, sz);
        return dest;
    }

    private static Card recognizeCard(Mat card) {
        Mat prepped = prepImage(card, 160);
        List<MatOfPoint> allContours = areaSortedContours(prepped, Imgproc.RETR_CCOMP);
        // The 0th contour is the card, the remaining 3 could be shapes
        List<MatOfPoint> possibleContours = allContours.subList(1, Math.min(4, allContours.size()));
        List<MatOfPoint> shapeContours = removeInteriorContours(possibleContours);
        List<Pair<MatOfPoint, Card.Shape>> identifiedShapes = new ArrayList<>();
        for (MatOfPoint contour : shapeContours) {
            Card.Shape ident = identifyShape(contour);
            if (ident != null) {
                identifiedShapes.add(new Pair<>(contour, ident));
            }
        }
        Imgproc.drawContours(card, shapeContours, -1, new Scalar(255, 0, 0), 3);

        if (identifiedShapes.size() > 0) {
            Card.Shape head = identifiedShapes.get(0).second;
            MatOfPoint contour = identifiedShapes.get(1).first;
            return new Card(head, Card.Shading.OPEN, Card.intToCount(identifiedShapes.size()), Card.Color.GREEN, 0);
        }
        return null;
    }

    private static Card.Shading identifyShading(Mat thresholdedCard, MatOfPoint contour) {
        Rect boundingBox = Imgproc.boundingRect(contour);
        
        /*
            def identify_fill(card_thresh, cnt):
        bb = cv2.boundingRect(cnt)
        average = np.average(center_section(card_thresh, bb))
        print "average: ", average
        if average < 100:
            return "solid"
        if 100 <= average < 200:
            return "striped"
        else:
            return "empty"
         */
    }

    private static List<MatOfPoint> removeInteriorContours(List<MatOfPoint> contours) {
        Set<Integer> badContours = new HashSet<>();
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            for (int j = i; j < contours.size(); j++) {
                MatOfPoint internalContour = contours.get(j);
                Rect externalBb = Imgproc.boundingRect(contour);
                Rect internalBb = Imgproc.boundingRect(internalContour);
                if (bbContains(externalBb, internalBb)) {
                    badContours.add(j);
                }
            }
        }

        List<MatOfPoint> ret = new ArrayList<>();
        for (int i = 0; i < contours.size(); i++) {
            if (!badContours.contains(i)) {
                ret.add(contours.get(i));
            }
        }
        return ret;
    }

    private static boolean bbContains(Rect outer, Rect inner) {
        return outer.x < inner.x && outer.y < inner.y && outer.x + outer.width > inner.x + inner.width &&
                outer.y + outer.height > inner.y + inner.height;
    }

    private static Card.Shape identifyShape(MatOfPoint contour) {
        double ShapeThreshold = 0.1;
        HashMap<Card.Shape, MatOfPoint> shapeMapping = new HashMap<Card.Shape, MatOfPoint>(){{
            put(Card.Shape.OVAL, Oval.contour);
            put(Card.Shape.DIAMOND, Diamond.contour);
            put(Card.Shape.SQUIGGLE, Squiggle.contour);
        }};
        double bestScore = ShapeThreshold;
        Card.Shape bestShape = null;
        for (Card.Shape shape : shapeMapping.keySet()) {
            double score = Imgproc.matchShapes(contour, shapeMapping.get(shape), 1, 0.0);
            Log.i(Tag, "Score: " + score + "shape: " + shape.toString());
            if (score <= bestScore) {
                bestScore = score;
                bestShape = shape;
            }
        }

        return bestShape;
/*def identify_shape(cnt):
    THRESH = 0.07
    SHAPES = {
            "oval": load_contour("oval"),
            "diamond": load_contour("diamond"),
            "squiggle": load_contour("squiggle")
    }
    res = [(cv2.matchShapes(shape, cnt, 1, 0.0), name) for (name, shape) in SHAPES.iteritems()]
    score, shape = min(res)
    if score < THRESH:
        return (shape, score)
    else:
        print shape,score, "MISS"
        return None*/
    }

    private static List<Point> sortedCorners(List<Point> corners) {
        // Goal: Long -> short
        double d1 = dist(corners.get(0), corners.get(1));
        double d2 = dist(corners.get(1), corners.get(2));
        if (d1 < d2) {
            // Need to shift by one so we are long -> short -> long -> short
            List<Point> ret = new ArrayList<>();
            ret.addAll(corners.subList(1, 4));
            ret.add(corners.get(0));
            assert ret.size() == 4;
            return ret;
        } else {
            return corners;
        }
    }

    private static double dist(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

}
