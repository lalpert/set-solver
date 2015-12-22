package com.example.leahalpert.setsolver;

import android.graphics.Bitmap;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by russell on 12/21/15.
 */
public class SetCVLib {
    public static void computeAndCircleSets(Mat input) {
        extractCards(input);
        //return input;
    }

    private static void extractCards(Mat img) {
        Mat clone = img.clone();
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        Size kern = new Size(1,1);
        Imgproc.GaussianBlur(img, img, kern, 1000);
        Imgproc.threshold(img, img, 120, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hier = new Mat();
        Imgproc.findContours(img, contours, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        // TODO: filter contours
        Imgproc.drawContours(clone, contours, -1, new Scalar(0,255,0), 2);
    }
    /*

    im2, contours, hierarchy = cv2.findContours(thresh,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)
    contours = sorted(contours, key=cv2.contourArea,reverse=True)[:20]

    CNT_THRESH = 0.07
    print [cv2.matchShapes(CARD_CONTOUR, cnt, 1, 0.0) for cnt in contours]
    filtered = [cnt for cnt in contours if cv2.matchShapes(CARD_CONTOUR, cnt, 1, 0.0) < CNT_THRESH]
    cv2.drawContours(img, filtered, -1, (0, 255, 0), 2)
    show(img)
    return filtered
     */


}
