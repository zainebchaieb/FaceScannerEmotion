package com.example.hp.facescanneremotion;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Canvas;


import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;


import java.io.File;
import java.io.FileNotFoundException;


public class DetectActivity extends AppCompatActivity {


    private static final String LOG_TAG="FACE API";
    private static final int PHOTO_REQUEST= 10;
    private TextView scanerResults;
    private ImageView imageView;
    private Uri imageUri;
    private FaceDetector detector;

    private static final int  REQUEST_WRITE_PERMISSION= 20;
    private static final String SAVED_INSTANCE_URI="uri";
    private static final String SAVED_INSTANCE_BITMAP="bitmap";
    private static final String SAVED_INSTANCE_RESULT="result";

    Bitmap editeBitmap ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);


        Button button=(Button)findViewById(R.id.button);
        scanerResults =(TextView)findViewById(R.id.results);
        imageView=(ImageView)findViewById(R.id.ScannerdResults);

        if (savedInstanceState != null)
        {
            editeBitmap=savedInstanceState.getParcelable(SAVED_INSTANCE_BITMAP);
            if (savedInstanceState.getString(SAVED_INSTANCE_URI)!= null)
            {
                imageUri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI));
            }
            imageView.setImageBitmap(editeBitmap);
            scanerResults.setText(savedInstanceState.getString(SAVED_INSTANCE_RESULT));
        }

        detector = new FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(DetectActivity.this, new String[]
                        {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION );

            }

        });
    }

    @Override
    public  void onRequestPermissionsResult (int requestCode , @NonNull String [] permissions ,@NonNull int [ ] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions ,grantResults );
        switch (requestCode)
        {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    takePicture();
                }
                else
                {
                    Toast.makeText(DetectActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }
    @Override
    protected void  onActivityResult (int requestCode , int resultCode , Intent data)
    {
        if (( requestCode == PHOTO_REQUEST) && (resultCode ==RESULT_OK))
        {
            lanchMediaScanIntent ();
            try
            {
                ScanFaces();
            }
            catch (Exception e)
            {
                Toast.makeText(this,"Failed to load image ",Toast.LENGTH_SHORT).show();
                Log.e(LOG_TAG,e.toString());
            }
        }
    }
    private void ScanFaces () throws Exception
    {
        Bitmap bitmap = decodeBitmapUri (this , imageUri);
        if (detector.isOperational()&&bitmap!=null) {
            editeBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            float scale = getResources().getDisplayMetrics().density;
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.rgb(255, 61, 61 ));
            paint.setTextSize((int) (14 * scale));
            paint.setStrokeWidth(3f);
            paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
            Canvas canvas = new Canvas(editeBitmap);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            Frame frame = new Frame.Builder().setBitmap(editeBitmap).build();
            SparseArray<Face> faces = detector.detect(frame);
            scanerResults.setText(null);
            for (int index = 0; index < faces.size(); ++index) {

                Face face = faces.valueAt(index);
                canvas.drawRect(face.getPosition().x,
                        face.getPosition().y,
                        face.getPosition().x + face.getWidth(),
                        face.getPosition().y + face.getHeight(), paint);
                scanerResults.setText(scanerResults.getText() + "Face" + (index + 1) + "\n");
                scanerResults.setText(scanerResults.getText() + "Smile Probability : " + "\n");
                scanerResults.setText(scanerResults.getText() + String.valueOf(face.getIsSmilingProbability() + "\n"));
                scanerResults.setText(scanerResults.getText() + "left Eye Open Probability: " + "\n");
                scanerResults.setText(scanerResults.getText() + String.valueOf(face.getIsSmilingProbability() + "\n"));
                scanerResults.setText(scanerResults.getText() + "Right Eye Open Probability: " + "\n");
                scanerResults.setText(scanerResults.getText() + ".................: " + "\n");
                for (Landmark landmark : face.getLandmarks()) {
                    int cx = (int) (landmark.getPosition().x);
                    int cy = (int) (landmark.getPosition().y);
                    canvas.drawCircle(cx, cy, 5, paint);
                }

            }
            if (faces.size() == 0) {
                scanerResults.setText("Scan Failed found nothing to scan ");

            } else {
                imageView.setImageBitmap(editeBitmap);
                scanerResults.setText(scanerResults.getText() + "no of faces detected :" + "\n");
                scanerResults.setText(scanerResults.getText() + String.valueOf(faces.size()) + "\n");
                scanerResults.setText(scanerResults.getText() + "..............." + "\n");
            }
        } else
        {
            scanerResults.setText("could not set up the detctor ! ");
        }

    }
    private void takePicture ()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(),"picture.jpg");
        imageUri = FileProvider.getUriForFile(DetectActivity.this,BuildConfig.APPLICATION_ID+".provider",photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent, Integer.parseInt(String.valueOf(PHOTO_REQUEST)));

    }
    @Override
    protected  void  onSaveInstanceState (Bundle outState)
    {
        if ( imageUri!= null)
        {
            outState.putParcelable(SAVED_INSTANCE_BITMAP,editeBitmap);
            outState.putString(SAVED_INSTANCE_URI,imageUri+toString());
            outState.putString(SAVED_INSTANCE_RESULT,scanerResults.getText().toString());
            super.onSaveInstanceState(outState);
        }
    }
    @Override
    protected void onDestroy () {

        super.onDestroy();
        detector.release();
    }
    private  void lanchMediaScanIntent ()
    {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
    }
    private  Bitmap decodeBitmapUri (Context ctx , Uri uri )throws FileNotFoundException
    {
        int targetN = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds=true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri),null,bmOptions);
        int phptoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(phptoW/targetN,photoH/targetH);
        bmOptions.inJustDecodeBounds=false;
        bmOptions.inSampleSize=scaleFactor;
        return BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri),null,bmOptions);
    }


}
