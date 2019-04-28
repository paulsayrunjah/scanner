package com.sayrunjah.scanner;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.PreviewCallback previewCallback;
    public boolean isFlashOn = false;
    private boolean mAutoFocus = true;

    // this is the autofocus call back
    private Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback(){

        public void onAutoFocus(boolean autoFocusSuccess, Camera arg1) {
            //Wait.oneSec();
            mAutoFocus = true;
        }};

    public void setCameraFocus(Camera.AutoFocusCallback autoFocus) {
        if (mCamera.getParameters().getFocusMode().equals(mCamera.getParameters().FOCUS_MODE_AUTO) ||
                mCamera.getParameters().getFocusMode().equals(mCamera.getParameters().FOCUS_MODE_MACRO)) {
            mCamera.autoFocus(autoFocus);
        }
    }

    public CameraPreview(Context context, Camera camera, Camera.PreviewCallback previewCallback) {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        this.previewCallback = previewCallback;
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setCameraFocus(myAutoFocusCallback);
        setCamFocusMode();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // create the surface and start camera preview
            if (mCamera == null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }

        } catch (IOException e) {
            Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void refreshCamera(Camera camera) {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        setCamera(camera);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        refreshCamera(mCamera);
    }

    public void setCamera(Camera camera) {
        //method to set a camera instance
        mCamera = camera;
    }

    public void turnOnFlash() {
        Camera.Parameters p = mCamera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        isFlashOn = true;
        mCamera.setParameters(p);
    }

    public void turnOffFlash() {
        Camera.Parameters p = mCamera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        isFlashOn = false;
        mCamera.setParameters(p);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        mCamera.release();
    }

    private void setCamFocusMode(){
        if(null == mCamera) {
            return;
        }

        /* Set Auto focus */
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        mCamera.setParameters(parameters);
    }


}