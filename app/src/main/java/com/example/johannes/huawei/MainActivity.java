package com.example.johannes.huawei;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textPrediction = (TextView) findViewById(R.id.textMain);
        textPrediction.setText("Authors: The HuawAI Team, by TUM");

        imageLoad = (ImageView) findViewById (R.id.imageLoad);
        imageLoad.setImageResource(R.drawable.huawai);

        Button buttonDetectBackground = findViewById(R.id.buttonDetectBackground);
        buttonDetectBackground.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.exit(0);
            }
        });

        // wird auch von DetectionActivity geprueft
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
    public void onDestroy() {
        super.onDestroy();
    }

}
