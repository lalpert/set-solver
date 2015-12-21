package com.example.leahalpert.setsolver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TakePhoto extends AppCompatActivity {
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
        setContentView(R.layout.activity_take_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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

    public void takePhotoMessage(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
       fileUri = getOutputMediaFileUri(); // create a file to save the image
        Log.d("SetSolverApp", "URI: " + fileUri + fileUri.toString());

       intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
        startActivityForResult(intent, 1);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            // Image captured and saved to fileUri specified in the Intent
           // Uri uri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
           // Log.d("SetSolverApp", "GOT URI: " + uri + uri.toString());

           Uri data = intent.getData();
            Log.d("SetSolverApp", "DATA: " + data);



            Toast.makeText(this, "Image saved to:\n" +
                    fileUri, Toast.LENGTH_LONG).show();

            ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);
            imageView.setImageURI(fileUri);
            Card[] cards = idCards(fileUri);
            List<List<Integer>> results = SetFinder.findSets(cards);
            TextView textView = (TextView) findViewById(R.id.textDisplay);
            textView.setText(results.toString());
            



        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "CANCELLED", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "FAILED", Toast.LENGTH_LONG).show();
        }
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

    }
}
