package com.example.johannes.huawei.tensorflow;

/*
 * Copyright 2016 Keval Patel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraService;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraFocus;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;

import java.io.File;
import java.io.IOException;

/**
 * Created by Keval on 11-Nov-16.
 *
 * @author {@link 'https://github.com/kevalpatel2106'}
 *
 * Starts a Background Service which classifies an image from the rear facing camera
 * and returns the result as a toast.
 */


public class DemoCamService extends HiddenCameraService {

    Bitmap bitmap;
    String classificationResult;
    String classificationDetails;

    private static final String TAG = "TfLiteCameraService";
    private static final String HANDLE_THREAD_NAME = "CameraBackground";

    private final Object lock = new Object();
    private boolean runClassifier = false;
    private ImageClassifier classifier;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
                CameraConfig cameraConfig = new CameraConfig()
                        .getBuilder(this)
                        .setCameraFacing(CameraFacing.REAR_FACING_CAMERA)
                        .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                        .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                        .setCameraFocus(CameraFocus.AUTO)
                        .build();

                startCamera(cameraConfig);

                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(DemoCamService.this, "Taking picture", Toast.LENGTH_SHORT).show();

                        takePicture();
                    }
                }, 20L);
            } else {

                //Open settings to grant permission for "Draw other apps".
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
            }
        } else {

            // Ask your parent activity for providing runtime permission
            Toast.makeText(this, "Camera permission not available", Toast.LENGTH_SHORT).show();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onImageCapture(@NonNull File imageFile) {
        //Toast.makeText(this, "Captured image size is : " + imageFile.length(), Toast.LENGTH_SHORT).show();

        // Do something with the image...
        startClassification(imageFile);

    }

    @Override
    public void onCameraError(@CameraError.CameraErrorCodes int errorCode) {
        switch (errorCode) {
            case CameraError.ERROR_CAMERA_OPEN_FAILED:
                //Camera open failed. Probably because another application
                //is using the camera
                Toast.makeText(this, "error_cannot_open", Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_IMAGE_WRITE_FAILED:
                //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                Toast.makeText(this, "error_cannot_write", Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                //camera permission is not available
                //Ask for the camera permission before initializing it.
                Toast.makeText(this, "error_cannot_get_permission", Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                //Display information dialog to the user with steps to grant "Draw over other app"
                //permission for the app.
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                Toast.makeText(this, "error_not_having_camera", Toast.LENGTH_LONG).show();
                break;
        }

        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        //stopBackgroundThread();
        //stoptimertask();
        super.onDestroy();


    }


    private void startClassification(File imageFile) {

        /** Load the model and labels. */
        try {
            classifier = new ImageClassifier(this);
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize an image classifier.");
        }

        String filePath = imageFile.getPath();
        bitmap = BitmapFactory.decodeFile(filePath);
        //classifyFrame();
        startBackgroundThread();
    }


    /** Starts a background thread and its {@link Handler}. */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == 0){
                    // show result on UI
                    Toast.makeText(DemoCamService.this, "Patron: " + classificationResult, Toast.LENGTH_SHORT).show();
                    // recycle classifiactionResult, background thread and classifier
                    classificationResult = null;
                    classifier.close();
                    //stopBackgroundThread();
                    stopSelf();
                }else{
                    // showErrorDialog
                }
            }
        };
        synchronized (lock) {
            runClassifier = true;
        }
        backgroundHandler.post(periodicClassify);
    }

    /** Stops the background thread and its {@link Handler}. */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
            synchronized (lock) {
                runClassifier = false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** Takes photos and classify them periodically. */
    private Runnable periodicClassify =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier) {
                            classifyFrame();
                        }
                    }
                    //backgroundHandler.post(periodicClassify);
                }
            };

    /** Classifies a frame */
    private void classifyFrame() {
        if (classifier == null || bitmap == null) {
            Toast.makeText(this, "Uninitialized Classifier or invalid context.", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = getResizedBitmap(this.bitmap, ImageClassifier.DIM_IMG_SIZE_X, ImageClassifier.DIM_IMG_SIZE_Y);
        //textureView.getBitmap(ImageClassifier.DIM_IMG_SIZE_X, ImageClassifier.DIM_IMG_SIZE_Y);
        classificationDetails = classifier.classifyFrame(bitmap);
        Log.d(TAG, String.valueOf(classifier.bestLabel));
        Log.d(TAG, String.valueOf(classifier.bestWeight));
        if (classifier.bestWeight > (float) 0.10) {
            if (classifier.bestLabel.equals("danger")) {
                classificationResult = "Danger";
            } else {
                classificationResult = "No Danger";
            }
        } else {
            classificationResult = "not significant";
        }
        bitmap.recycle();
        backgroundHandler.sendEmptyMessage(0);
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

}