package com.example.johannes.huawei;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hiai.vision.common.ConnectionCallback;
import com.huawei.hiai.vision.common.VisionBase;
import com.huawei.hiai.vision.image.detector.SceneDetector;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.visionkit.image.detector.Scene;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private TextView textPrediction;
    private Button buttonPredictScene;
    private Handler mMyHandler = null;
    private MyHandlerThread mMyHandlerThread = null;
    private Bitmap bitmap;

    SceneDetector sceneDetector;
    String result;

    public static final int MSG_SCENE = 1;

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
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
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

        textPrediction = (TextView) findViewById(R.id.textPrediction);
        buttonPredictScene = (Button) findViewById(R.id.buttonPredictScene);

        mMyHandlerThread = new MyHandlerThread();
        mMyHandlerThread.start();
        mMyHandler = new Handler(mMyHandlerThread.getLooper(), mMyHandlerThread);

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

        buttonPredictScene.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                textPrediction.setText("");

                mMyHandler.sendEmptyMessage(MSG_SCENE);

                /** Define class detector，the context of this project is the input parameter： */
                SceneDetector sceneDetector = new SceneDetector(MainActivity.this);
            }
        });

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
            String path = "C:\\Users\\Johannes\\Desktop\\Korsika 2018\\20180918_125913.jpg";
            Frame frame = new Frame();
            Bitmap bitmap = BitmapFactory.decodeFile(path);
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
                    result = "result : " + scene.getType(); // TODO: returns result as number in sceneAStringArr[], should be string
                    long end = System.currentTimeMillis();
                    Log.e("Scene demo", "scene need time:" + (end - startTime));
                    mHandler.sendEmptyMessage(11);
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
                case 1:
                    Log.d("Scene demo", "bind ok ");
                    Toast.makeText(MainActivity.this, "bind success", Toast.LENGTH_SHORT).show();
                    sceneDetector = new SceneDetector(MainActivity.this);
                    break;
                case 2:
                    Toast.makeText(MainActivity.this, "disconnect", Toast.LENGTH_SHORT).show();
                    break;
                case 11:
                    textPrediction.setText(result);
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        /** Source release */
        VisionBase.destroy();
        sceneDetector.release();
        mMyHandlerThread.quit();
        super.onDestroy();
    }

}
