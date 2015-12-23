package com.example.leahalpert.setsolver;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
            return flattenCard(card, input);
        }

        return input;
    }

    private static List<MatOfPoint> extractCards(Mat img) {
        Mat clone = img.clone();
        Imgproc.cvtColor(clone, clone, Imgproc.COLOR_BGR2GRAY);
        Size kern = new Size(1, 1);
        Imgproc.GaussianBlur(clone, clone, kern, 1000);
        Imgproc.threshold(clone, clone, 120, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hier = new Mat();
        Imgproc.findContours(clone, contours, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint lhs, MatOfPoint rhs) {
                return -Double.compare(Imgproc.contourArea(lhs), Imgproc.contourArea(rhs));
            }
        });

        List<MatOfPoint> filteredContours = new ArrayList<>();
        for (MatOfPoint cnt : contours.subList(0, Math.min(MaxCardLikeObjects, contours.size()))) {
            double score = Imgproc.matchShapes(CardContour.contour, cnt, 1, 0.0);
            Log.i(Tag, "Score: " + score);
            Log.i(Tag, "Area:" + Imgproc.contourArea(cnt));
            if (score < CardFilterThreshold) {
                filteredContours.add(cnt);
            }
        }

        Log.i(Tag, "Found " + filteredContours.size() + " contours");

        Imgproc.drawContours(img, filteredContours, -1, new Scalar(100, 100, 0), 30);
        return filteredContours;
    }

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
