package com.example.johannes.huawei;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

/*
*
* */

public class FoundationActivity extends AppCompatActivity {

    private static final String TAG = "FoundationActivity";
    public static final Integer AI_OK = 0;

    public static final int GALLERY_REQUEST_CODE = 0;
    public static final int IMAGE_CAPTURE_REQUEST_CODE = 1;
    public static final int RESIZED_WIDTH = 227;
    public static final int RESIZED_HEIGHT = 227;

    public static final double meanValueOfBlue = 103.939;
    public static final double meanValueOfGreen = 116.779;
    public static final double meanValueOfRed = 123.68;

    private List<ClassifyItemModel> items;

    private RecyclerView rv;

    private AssetManager mgr;

    private Bitmap show;

    private ClassifyAdapter adapter;

    private Button btnCamera;

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
                        Toast.makeText(FoundationActivity.this, "load model success. taskId is:" + taskId, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FoundationActivity.this, "load model fail. taskId is:" + taskId, Toast.LENGTH_SHORT).show();
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
                        Toast toast = Toast.makeText(FoundationActivity.this, "run model success. taskId is:" + taskId, Toast.LENGTH_SHORT);
                        CustomToast.showToast(toast, 500);
                    } else {
                        Toast toast = Toast.makeText(FoundationActivity.this, "run model fail. taskId is:" + taskId, Toast.LENGTH_SHORT);
                        CustomToast.showToast(toast, 500);
                    }


                    items.add(new ClassifyItemModel(output[0], output[1], output[2], show));

                    adapter.notifyDataSetChanged();
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
                        Toast.makeText(FoundationActivity.this, "unload model success. taskId is:" + taskId, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FoundationActivity.this, "unload model fail. taskId is:" + taskId, Toast.LENGTH_SHORT).show();
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

    /*
    * Initiiert ModelManager libhiai.so
    *
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_foundation);

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

        /*
         * initiiert den RecyclerView, LayoutManager (liefert views) und ClassifyAdapter (managt views)
         * außerdem checkCameraPermission
         * */
        initView();
    }

    private void setHeaderView(RecyclerView view) {
        View header = LayoutInflater.from(this).inflate(R.layout.recyclerview_hewader, view, false);


        btnCamera = header.findViewById(R.id.btn_camera);

        adapter.setHeaderView(header);
    }

    private void initView() {
        rv = (RecyclerView) findViewById(R.id.rv);

        // The RecyclerView fills itself with views provided by the linear layout manager
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rv.setLayoutManager(manager);

        // The view holder objects are managed by the ClassifyAdapter. The adapter creates view holders as needed.
        adapter = new ClassifyAdapter(items);
        rv.setAdapter(adapter);

        setHeaderView(rv);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission();
            }
        });
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

    // startet takePicture, wird aufgerufen durch Button.onClickListener
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    IMAGE_CAPTURE_REQUEST_CODE);
        } else {
            takePictureAndClassify();
        }
    }

    // startet takePicture
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == IMAGE_CAPTURE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePictureAndClassify();
            } else {
                Toast.makeText(FoundationActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void takePictureAndClassify() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) switch (requestCode) {
            case GALLERY_REQUEST_CODE:
                try {
                    Bitmap bitmap;
                    ContentResolver resolver = getContentResolver();
                    Uri originalUri = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                    cursor.moveToFirst();
                    Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    final Bitmap initClassifiedImg = Bitmap.createScaledBitmap(rgba, RESIZED_WIDTH, RESIZED_HEIGHT, false);

                    final float[] pixels = getPixel(initClassifiedImg, RESIZED_WIDTH, RESIZED_HEIGHT);

                    show = initClassifiedImg;
                    ModelManager.runModelAsync("hiai", pixels);

                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }

                break;
            case IMAGE_CAPTURE_REQUEST_CODE:
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Bitmap rgba = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);

                Bitmap initClassifiedImg = Bitmap.createScaledBitmap(rgba, RESIZED_WIDTH, RESIZED_HEIGHT, false);

                final float[] pixels = getPixel(initClassifiedImg, RESIZED_WIDTH, RESIZED_HEIGHT);

                // this is where the magic happens
                ModelManager.runModelAsync("hiai", pixels);

                show = initClassifiedImg;
                break;

            default:
                break;
        }
        else {
            Toast.makeText(FoundationActivity.this,
                    "Return without selecting pictures|Gallery has no pictures|Return without taking pictures", Toast.LENGTH_SHORT).show();
        }

    }

    private float[] getPixel(Bitmap bitmap, int resizedWidth, int resizedHeight) {
        int channel = 3;
        float[] buff = new float[channel * resizedWidth * resizedHeight];

        int rIndex, gIndex, bIndex;
        for (int i = 0; i < resizedHeight; i++) {
            for (int j = 0; j < resizedWidth; j++) {
                bIndex = i * resizedWidth + j;
                gIndex = bIndex + resizedWidth * resizedHeight;
                rIndex = gIndex + resizedWidth * resizedHeight;

                int color = bitmap.getPixel(j, i);

                buff[bIndex] = (float) (blue(color) - meanValueOfBlue);
                buff[gIndex] = (float) (green(color) - meanValueOfGreen);
                buff[rIndex] = (float) (red(color) - meanValueOfRed);
            }
        }

        return buff;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ModelManager.unloadModelAsync();
    }

}
