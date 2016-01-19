package com.example.leahalpert.setsolver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

    private static final String TAG = "TakePhoto";
    final int PICK_IMAGE_REQUEST = 123;
    final int TAKE_IMAGE_REQUEST = 456;
    private Uri fileUri;
    boolean debugMode = false;

    /**
     * Connect to Android OpenCVManager
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    /**
     * Create a file Uri for saving an image
     */
    private Uri getOutputMediaFileUri() {
        File extFiles = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mediaStorageDir = new File(extFiles, "SetSolverApp");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("SetSolverApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        return Uri.fromFile(file);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        setContentView(R.layout.activity_take_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
        switch (item.getItemId()) {

            case R.id.action_settings:
                // TODO: do something
                return true;

            case R.id.stream:
                Intent intent = new Intent();
                intent.setClass(this, CameraPreviewActivity.class);
                startActivity(intent);
                return true;

            case R.id.takephoto:
                Intent intent2 = new Intent();
                intent2.setClass(this, TakePhoto.class);
                startActivity(intent2);
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
        startActivityForResult(intent, TAKE_IMAGE_REQUEST);
    }

    public void loadGalleryPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // Show only images, no videos or anything else
        intent.setType("image/*");
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * This function runs after a photo is taken from the camera app and saved to the phone
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Intent data = intent;

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
        }

        if (resultCode == RESULT_OK) {
            renderResults();
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "CANCELLED", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "FAILED", Toast.LENGTH_LONG).show();
        }
    }


    public void renderResults() {
        ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);

        Bitmap originalBitmap = null;
        Mat matToProcess = null;
        try {
            originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
            matToProcess = bitmapToMat(originalBitmap);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        SetResult result = SetCVLib.computeAndCircleSets(matToProcess);

        if (result.getFailedImages().size() > 0 && debugMode) {
            Bitmap bitmap = matToBitmap(result.getFailedImages().get(0));
            imageView.setImageBitmap(bitmap);
        } else if (result.numSets() > 0) {
            Bitmap bitmap = matToBitmap(result.getSetImages().get(0));
            imageView.setImageBitmap(bitmap);
        } else {
            Bitmap bitmap = matToBitmap(result.getAllSetsImage());
            imageView.setImageBitmap(bitmap);
            Toast.makeText(this, "No sets found", Toast.LENGTH_LONG).show();

        }

    }

    private Bitmap matToBitmap(Mat mat) {
        Bitmap bmpOut = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmpOut);
        return bmpOut;
    }


    @NonNull
    private Mat bitmapToMat(Bitmap bitmap) throws IOException {
        Mat imgToProcess = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmap, imgToProcess);
        return imgToProcess;
    }

}
