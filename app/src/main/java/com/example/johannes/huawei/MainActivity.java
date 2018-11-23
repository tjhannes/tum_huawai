package com.example.johannes.huawei;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hiai.vision.common.ConnectionCallback;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.image.detector.SceneDetector;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.image.detector.Scene;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/*
*
* Lädt per Intent ein aufgenommes Bild in das Bitmap.
* Schickt das Bitmap als Frame an HiAI API. Return ein JsonObject mit einem von 14 möglichen Scenes.
* Nutzt dabei Threads um die Anfrage parallel zu Bearbeiten
*
* */

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private TextView textPrediction;
    private Button buttonPredictScene;
    private Button buttonCamera;
    private ImageView imageLoad;
    private Handler mMyHandler = null;
    private MyHandlerThread mMyHandlerThread = null;
    private Uri fileUri;
    private Bitmap bitmap;
    private String sceneStringArr[] = {"Unknown", "UnSupport", "Beach", "BlueSky", "Sunset", "Food", "Flower", "GreenPlant", "Snow", "Night",
            "Text", "Stage",
            "Cat", "Dog", "Firework", "Overcast", "Fallen", "Panda", "Car", "OldBuildings", "Bicycle", "Waterfall"};

    SceneDetector sceneDetector;
    String result;

    private static final String LOG_TAG = "Scene_demo";
    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_SELECT = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MSG_SERIVCE_CONNECTED = 1;
    public static final int MSG_SERIVCE_DISCONNECTED = 2;
    public static final int MSG_SCENE = 1;
    public static final int MSG_SHOW_RESULT_SCENE = 11;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    Intent intentCameraActivity = new Intent(MainActivity.this, CameraActivity.class);
                    startActivity(intentCameraActivity);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    Intent intentFoundationActivity = new Intent(MainActivity.this, FoundationActivity.class);
                    startActivity(intentFoundationActivity);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mMyHandlerThread = new MyHandlerThread();
        mMyHandlerThread.start();
        mMyHandler = new Handler(mMyHandlerThread.getLooper(), mMyHandlerThread);

        textPrediction = (TextView) findViewById(R.id.textPrediction);
        textPrediction.setText("Possible scenes: "+ Arrays.toString(sceneStringArr));
        imageLoad = (ImageView) findViewById (R.id.imageLoad);
        imageLoad.setImageResource(R.drawable.strand_hd);
        buttonCamera = (Button) findViewById(R.id.buttonCamera);
        buttonCamera.setText("Take Picture");
        buttonCamera.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //Log.d(LOG_TAG, "get uri");
                // Create a File for saving an image or video
                File outputMediaFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                fileUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", outputMediaFile);
                Log.d(LOG_TAG, "end get uri = " + fileUri);
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
            }
        });
        buttonPredictScene = (Button) findViewById(R.id.buttonPredictScene);
        buttonPredictScene.setText("Predict");
        buttonPredictScene.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                textPrediction.setText("");


                /** Define class detector，the context of this project is the input parameter： */
                sceneDetector = new SceneDetector(MainActivity.this);
                mMyHandler.sendEmptyMessage(MSG_SCENE);


            }
        });
        //bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.strand_hd);

        /** Initialize with the VisionBase static class and asynchronously get the connection of the service */
        VisionBase.init(this, new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                /** This callback method is invoked when the service connection is successful; you can do the initialization of the detector class, mark the service connection status, and so on */
            }

            @Override
            public void onServiceDisconnect() {
                /** When the service is disconnected, this callback method is called; you can choose to reconnect the service here, or to handle the exception. */
            }
        });

        requestPermissions();



    }



    private class MyHandlerThread extends HandlerThread implements Handler.Callback {
        public MyHandlerThread() {
            super("MyHandler");
            // TODO Auto-generated constructor stub
        }

        public MyHandlerThread(String name) {
            super(name);
            // TODO Auto-generated constructor stub
        }

        @Override
        public boolean handleMessage(Message arg0) {

            Frame frame = new Frame();
            frame.setBitmap(bitmap);
            switch (arg0.what) {
                case MSG_SCENE: //scene detect
                    long startTime = System.currentTimeMillis();
                    JSONObject obj = sceneDetector.detect(frame, null);
                    if (obj == null) {
                        result = "error";
                    }
//                    result = obj.toString();
                    Scene scene = sceneDetector.convertResult(obj);
                    if (scene == null) {
                        break;
                    }
                    // getType returns result as number in sceneAStringArr[]
                    result = "result : " + sceneStringArr[scene.getType()];
                    long end = System.currentTimeMillis();
                    Log.e("Scene demo", "scene need time:" + (end - startTime));
                    mHandler.sendEmptyMessage(MSG_SHOW_RESULT_SCENE);
                    break;


                default:
                    break;
            }
            return false;
        }

    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SERIVCE_CONNECTED:
                    Log.d("Scene demo", "bind ok ");
                    Toast.makeText(MainActivity.this, "bind success", Toast.LENGTH_SHORT).show();
                    sceneDetector = new SceneDetector(MainActivity.this);
                    break;
                case MSG_SERIVCE_DISCONNECTED:
                    Toast.makeText(MainActivity.this, "disconnect", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_SHOW_RESULT_SCENE:
                    imageLoad.setImageBitmap(bitmap);
                    textPrediction.setText(result);
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // only image_capture implemented
        if ((requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_IMAGE_SELECT) && resultCode == RESULT_OK) {
            String imgPath;

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imgPath = Environment.getExternalStorageDirectory() + fileUri.getPath();
            } else {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = MainActivity.this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();
            }
            Log.d(LOG_TAG, "imgPath = " + imgPath);
            bitmap = BitmapFactory.decodeFile(imgPath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                , "Scene-Demo");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(LOG_TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }
        Log.d(LOG_TAG, "mediaFile " + mediaFile);
        return mediaFile;
    }

    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission1 = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                int permission2 = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA);
                if (permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager
                        .PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onDestroy() {
        /** Source release */
        VisionBase.destroy();
        sceneDetector.release();
        mMyHandlerThread.quit();
        super.onDestroy();
    }

}
