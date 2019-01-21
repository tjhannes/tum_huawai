package com.example.johannes.huawei;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class TfLiteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neural_net);
        if (null == savedInstanceState) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, TfLiteFragment.newInstance())
                    .commit();
        }
    }

}
