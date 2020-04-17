package com.kedy.wechattouch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

@SuppressLint("Registered")
public class CalendarActivity extends Activity {
    private static final String TAG = "CalendarActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
