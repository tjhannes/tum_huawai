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

/*
* Main Activity zum Starten der Detection Activity
*
* */

public class MainActivity extends AppCompatActivity {


    private TextView textPrediction;
    private ImageView imageLoad;

    private static final String LOG_TAG = "Main";


    public void startDetection(View view) {

        // launch neural net activity
        Intent intent = new Intent(this, Camera2Activity.class);
        Log.d(LOG_TAG, "Start HiAI Foundation");
        startActivity(intent);
    }

    public void changeSettings(View view) {

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }

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
                // geht hier irgendwie nicht
                // startService(new Intent(this, NotificationService.class));
                System.exit(0);
            }
        });

        changeBorderColor(getResources().getColor(R.color.colorPrimary));

        // wird auch von DetectionActivity geprueft
        requestPermissions();

    }

    private void changeBorderColor(int color) {

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.container);
        ShapeDrawable rectShapeDrawable = new ShapeDrawable();
        // get paint and set border color, stroke and stroke width
        Paint paint = rectShapeDrawable.getPaint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(50);
        layout.setBackground(rectShapeDrawable);
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
        startService(new Intent(this, NotificationService.class));
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
