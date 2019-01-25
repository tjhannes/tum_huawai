package com.example.johannes.huawei;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
* Main Activity zum Starten der {@link Camera2Activity},
* des {@link NotificationService} Background Services
* und der {@link SettingsActivity}
* Als Backup ist zusätzlich {@link TfLiteActivity} verfügbar, dazu auf das Patron Logo klicken
*
* */

public class MainActivity extends AppCompatActivity {


    private TextView textPrediction;
    private ImageView imageLoad;

    private static final String LOG_TAG = "Main";


    public void startDetection(View view) {

        // launch hiai activity
        Intent intent = new Intent(this, Camera2Activity.class);
        Log.d(LOG_TAG, "Start HiAI Foundation");
        startActivity(intent);
    }

    public void startTfLite(View view) {

        // launch tensorflow activity
        Intent intent = new Intent(this, TfLiteActivity.class);
        Log.d(LOG_TAG, "Start Tensorflow");
        startActivity(intent);
    }

    public void changeSettings(View view) {

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textPrediction = (TextView) findViewById(R.id.textMain);
        textPrediction.setText("Authors: The HuawAI Team, by TUM");

        imageLoad = (ImageView) findViewById (R.id.imageLoad);
        imageLoad.setImageResource(R.drawable.icon_patron_red);

        Button buttonDetectBackground = findViewById(R.id.buttonDetectBackground);
        buttonDetectBackground.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "Background Service started. To stop please close the app in the app manager.",Toast.LENGTH_SHORT ).show();
                // notificationService starts DemoCamService every 2 seconds
                startService(new Intent(MainActivity.this, NotificationService.class));
                // to start classification manually
                //startService(new Intent(MainActivity.this, DemoCamService.class));
            }
        });

        // to get permissions e.g. when newly installed
        requestPermissions();

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
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
