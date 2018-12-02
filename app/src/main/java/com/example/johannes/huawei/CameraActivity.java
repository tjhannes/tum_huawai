package com.example.johannes.huawei;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    private TextView mTextMessage;
    private List<ClassifyItemModel> items;
    private Bitmap show;
    private AssetManager mgr;

    /*
     * implementiert die Grundfunktionen des ModelManagerListeners:
     * lädt das Model, startet und stoppt es.
     * Bei Modelstart Aufrufe an Items.add(new ClassifyModel)->show und Adapter.notifyChange
     * */
    ModelManagerListener listener = new ModelManagerListener() {

        @Override
        public void onStartDone(final int taskId) {
            Log.e(TAG, " java layer onStartDone: " + taskId);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (taskId > 0) {
                        Toast.makeText(CameraActivity.this, "load model success. taskId is:" + taskId, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CameraActivity.this, "load model fail. taskId is:" + taskId, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onRunDone(final int taskId, final String[] output) {

            for (int i = 0; i < output.length; i++) {
                Log.e(TAG, "java layer onRunDone: output[" + i + "]:" + output[i]);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (taskId > 0) {
                        Toast toast = Toast.makeText(CameraActivity.this, "run model success. taskId is:" + taskId, Toast.LENGTH_SHORT);
                        CustomToast.showToast(toast, 500);
                    } else {
                        Toast toast = Toast.makeText(CameraActivity.this, "run model fail. taskId is:" + taskId, Toast.LENGTH_SHORT);
                        CustomToast.showToast(toast, 500);
                    }


                    // show bitmap
                    items.add(new ClassifyItemModel(output[0], output[1], output[2], show));
                    String result = output[0];
                    mTextMessage.setText(result);

                    //adapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onStopDone(final int taskId) {
            Log.e(TAG, "java layer onStopDone: " + taskId);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (taskId > 0) {
                        Toast.makeText(CameraActivity.this, "unload model success. taskId is:" + taskId, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CameraActivity.this, "unload model fail. taskId is:" + taskId, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onTimeout(final int taskId) {
            Log.e(TAG, "java layer onTimeout: " + taskId);
        }

        @Override
        public void onError(final int taskId, final int errCode) {
            Log.e(TAG, "onError:" + taskId + " errCode:" + errCode);
        }

        @Override
        public void onServiceDied() {
            Log.e(TAG, "onServiceDied: ");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mTextMessage = (TextView) findViewById(R.id.mTextMessage);
        mTextMessage.setText("Init");

        /** load libhiai.so */
        boolean isSoLoadSuccess = ModelManager.init();
        if (isSoLoadSuccess) {
            Toast.makeText(this, "load libhiai.so success.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "load libhiai.so fail.", Toast.LENGTH_SHORT).show();
        }

        /** init classify labels */
        initLabels();

        /*
         * AssetManager -> Huawei-Cambricon Model
         * */
        mgr = getResources().getAssets();

        int ret = ModelManager.registerListenerJNI(listener);

        Log.e(TAG, "onCreate: " + ret);

        // lädt das Model
        ModelManager.loadModelAsync("hiai", mgr);

        items = new ArrayList<>();

        // doppelt?
        mgr = getResources().getAssets();

        if (null == savedInstanceState) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, Camera.newInstance())
                    .commit();
        }
    }


    /**
     * initiert die Labels des Models aus labels.txt und gibt weiter an ModelManager.initLabels
     */
    private void initLabels() {
        byte[] labels;
        try {
            InputStream assetsInputStream = getAssets().open("labels.txt");
            int available = assetsInputStream.available();
            labels = new byte[available];
            assetsInputStream.read(labels);
            assetsInputStream.close();
            ModelManager.initLabels(labels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ModelManager.unloadModelAsync();
    }

    }
