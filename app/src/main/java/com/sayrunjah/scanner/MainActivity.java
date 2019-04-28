package com.sayrunjah.scanner;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    private Button capture, switchCamera, flash;
    private Context myContext;
    private   LinearLayout cameraPreview;
    private boolean cameraFront = false;
    public static Bitmap bitmap;
    public static String code;

    private RelativeLayout frame;
    private View scan;
    private FireBaseScanner fireBaseScanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;

        fireBaseScanner = new FireBaseScanner(getApplicationContext());
        mCamera =  Camera.open();
        mCamera.setDisplayOrientation(90);
        cameraPreview = findViewById(R.id.cPreview);
        mPreview = new CameraPreview(myContext, mCamera, getPreviewCallback());
        cameraPreview.addView(mPreview);

        flash = findViewById(R.id.flash);



        frame = findViewById(R.id.frame);
        scan = findViewById(R.id.scan);
        manageBlinkEffect(scan);



        capture = findViewById(R.id.btnCam);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });


        switchCamera = (Button) findViewById(R.id.btnSwitch);
        switchCamera.setOnClickListener(v -> {
            //get the number of cameras
            int camerasNumber = Camera.getNumberOfCameras();
            if (camerasNumber > 1) {
                //release the old camera instance
                //switch camera, from the front and the back and vice versa

                releaseCamera();
                chooseCamera();
            } else {

            }
        });

        flash.setOnClickListener(v -> {
            if(mCamera != null){
                boolean isFlashOn = mPreview.isFlashOn;
                if(isFlashOn) {
                    mPreview.turnOffFlash();
                }else{
                    mPreview.turnOnFlash();
                }
            }
        });

        mCamera.startPreview();



    }

    private int findFrontFacingCamera() {

        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;

    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;

            }

        }
        return cameraId;
    }

    public void onResume() {

        super.onResume();
        /*if(mCamera == null) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewCallback(getPreviewCallback());
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
            Log.d("nu", "null");
        }else {
            Log.d("nu","no null");
        }*/

    }

    public void chooseCamera() {

        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        mCamera.setDisplayOrientation(90);
        mCamera.setPreviewCallback(getPreviewCallback());
        mPicture = getPictureCallback();
        mPreview.refreshCamera(mCamera);
        //if the camera preview is the front
       /* if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewCallback(getPreviewCallback());
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview
                mCamera = Camera.open(cameraId);
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewCallback(getPreviewCallback());
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = (data, camera) -> {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            try{
                //imageView.setImageBitmap(bitmap);
                // Convert to JPG
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
                byte[] jdata = baos.toByteArray();

// Convert to Bitmap
                Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);

            }catch (Exception ex){
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("SCANNER-error", ex.getMessage());
                Log.d("SCANNER-error", ex.toString());
            }

            //new FireBaseScanner(getApplicationContext()).scanCode(bitmap);
            Log.d("Scanner-E", String.valueOf(data));
            Log.d("Scanner-E", "----");
            //Intent intent = new Intent(MainActivity.this,PictureActivity.class);
            //startActivity(intent);

            /*Bitmap bitmap1 = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            bitmap1.copyPixelsFromBuffer(byteBuffer);
            try{
                imageView.setImageBitmap(bitmap1);
            }catch (Exception ex){
                Log.d("Scanner-Error", ex.getMessage());
            }*/
        };
        return picture;
    }

    private Camera.PreviewCallback getPreviewCallback() {
        Camera.PreviewCallback previewCallback = (data, camera) -> {
            try{
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                Bitmap bmp = decodeSampledBitmapFromResource(data, previewSize.width, previewSize.height);
                String barcode = fireBaseScanner.scanCode(bmp);

                if(barcode != null) {
                    //releaseCamera();
                    code = barcode;
                    Log.d("Barcodeeee", barcode);

                    Intent intent = new Intent(MainActivity.this,PictureActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

            }catch (Exception ex){
                ex.printStackTrace();
                //Log.d("SCANNER-error", ex.getMessage());
            }catch (OutOfMemoryError ex) {
                releaseCamera();
                chooseCamera();
            }


        };

        return previewCallback;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(byte[] data, int   reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //options.inPurgeable = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21, reqWidth, reqHeight, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, reqHeight, reqHeight), 80, baos);
        byte[] jdata = baos.toByteArray();
        return BitmapFactory.decodeByteArray(jdata, 0, jdata.length, options);
    }

    @SuppressLint("WrongConstant")
    private void manageBlinkEffect(View view) {
        ObjectAnimator anim = ObjectAnimator.ofInt(view, "backgroundColor", Color.WHITE, Color.RED,
                Color.WHITE);
        anim.setDuration(1500);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();
    }
}

