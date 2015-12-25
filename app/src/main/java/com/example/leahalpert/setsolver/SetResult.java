package com.example.leahalpert.setsolver;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leahalpert on 12/24/15.
 */
public class SetResult {
    private List<Mat> setImages = new ArrayList<>();
    private Mat allSetsImage;
    private List<Mat> failedImages = new ArrayList<>();

    public List<Mat> getSetImages() {
        return setImages;
    }

    public void setSetImages(List<Mat> setImages) {
        this.setImages = setImages;
    }

    public Mat getAllSetsImage() {
        return allSetsImage;
    }

    public void setAllSetsImage(Mat allSetsImage) {
        this.allSetsImage = allSetsImage;
    }

    public List<Mat> getFailedImages() {
        return failedImages;
    }

    public void setFailedImages(List<Mat> failedImages) {
        this.failedImages = failedImages;
    }

    public void addFailedImage(Mat image) {
        this.failedImages.add(image);
    }

    public void addSetImage(Mat image) {
        this.setImages.add(image);
    }

    public int numSets() {
        return setImages.size();
    }
}
