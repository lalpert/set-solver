package com.example.leahalpert.setsolver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TakePhoto extends AppCompatActivity {

    private static final String  TAG = "TakePhoto";
    final int PICK_IMAGE_REQUEST = 123;

    /** Connect to Android OpenCVManager */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private Uri fileUri;

    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(){
        File extFiles = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mediaStorageDir = new File(extFiles, "SetSolverApp");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("SetSolverApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");

        return Uri.fromFile(file);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        setContentView(R.layout.activity_take_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_take_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * When you click the "take photos" button, this function opens the camera app
     */
    public void takePhotoMessage(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
        startActivityForResult(intent, 1);
    }

    public void loadGalleryPhoto(View view) {

        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * This function runs after a photo is taken from the camera app and saved to the phone
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Intent data = intent;
        switch (requestCode) {
            case PICK_IMAGE_REQUEST:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {

                    Uri uri = data.getData();


                    ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);

                    // For debugging
                    // Bitmap bitmap = displayGrayScaleImage(fileUri);

                    Bitmap bitmap = computeAndCircleSets(uri);
                    imageView.setImageBitmap(bitmap);
                    return;

                }
        }
        if (resultCode == RESULT_OK) {
            ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);

            // For debugging
            // Bitmap bitmap = displayGrayScaleImage(fileUri);

            Bitmap bitmap = computeAndCircleSets(fileUri);
            imageView.setImageBitmap(bitmap);

        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "CANCELLED", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "FAILED", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Takes an image URI, finds sets in the image, and outlines the cards that are part of the set.
     * @return the image with outlined cards
     */
    // TODO: Fill in this function! Replace with real code...
    // TODO: To find the sets from the Cards, use List<List<Integer>> results = SetFinder.findSets(cards);
    private Bitmap computeAndCircleSets(Uri uri) {

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            Mat imgToProcess = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC3);
            Utils.bitmapToMat(bitmap, imgToProcess);
            Mat result = SetCVLib.computeAndCircleSets(imgToProcess);

            Bitmap bmpOut = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);


            Utils.matToBitmap(result, bmpOut);
            return bmpOut;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void testSetAlgorithm() {
        Card[] cards = idCards(fileUri);
        List<List<Integer>> results = SetFinder.findSets(cards);
        TextView textView = (TextView) findViewById(R.id.textDisplay);
        textView.setText(results.toString());
    }

    public Card[] idCards(Uri imageUri) {
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
        ArrayList<Card> cards = new ArrayList<Card>(
                Arrays.asList(cardOne, cardTwo, cardThree));


        Card[] cardArr = cards.toArray(new Card[cards.size()]);
        return cardArr;

        // example usage: List<List<Integer>> results = SetFinder.findSets(cards);

    }
}
