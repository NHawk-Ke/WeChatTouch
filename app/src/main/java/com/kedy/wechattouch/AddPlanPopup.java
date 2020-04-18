package com.kedy.wechattouch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.IntentService;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPlanPopup extends Activity {
    private static final String TAG = "AddPlanPopup";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_plan_popup);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .8), (int) (height * .6));

        //创建计划
        Button button = findViewById(R.id.createPlan);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: check errors and put values into database. Update global variable
                EditText planTitle = findViewById(R.id.planName);
                planTitle.setError(Html.fromHtml("计划名称不能为空"));
                finish();
            }
        });

        //计划时间
        EditText planTime = findViewById(R.id.planTime);
        Intent intent = getIntent();
        int year = intent.getIntExtra("YEAR", -1);
        int month = intent.getIntExtra("MONTH", -1);
        int day = intent.getIntExtra("DAY", -1);
        planTime.setText(year + "-" + month + "-" + day + " 00:00");
        planTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogPick((EditText) v);
            }
        });

        //计划图标
        Spinner iconSpinner = findViewById(R.id.planIconSelector);
        final SimpleAdapter s_adapter = new SimpleAdapter(
                this, getIcons(), R.layout.icon_item,
                new String[]{"icon"}, new int[]{R.id.icon});
        iconSpinner.setAdapter(s_adapter);
    }

    //将两个选择时间的dialog放在该函数中
    private void showDialogPick(final EditText timeText) {
        final StringBuffer time = new StringBuffer();
        //获取Calendar对象，用于获取当前时间
        String oldValue = timeText.getText().toString();
        String[] timeInfo = oldValue.split("[ \\-:]");
        int year = Integer.parseInt(timeInfo[0]);
        int month = Integer.parseInt(timeInfo[1]);
        int day = Integer.parseInt(timeInfo[2]);
        int hour = Integer.parseInt(timeInfo[3]);
        int minute = Integer.parseInt(timeInfo[4]);
        //实例化TimePickerDialog对象
        final TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            //选择完时间后会调用该回调函数
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (hourOfDay < 10)
                    time.append(" 0").append(hourOfDay);
                else
                    time.append(" ").append(hourOfDay);
                if (minute < 10)
                    time.append(":0").append(minute);
                else
                    time.append(":").append(minute);
                //设置TextView显示最终选择的时间
                timeText.setText(time);
            }
        }, hour, minute, true);
        //实例化DatePickerDialog对象
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            //选择完日期后会调用该回调函数
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //因为monthOfYear会比实际月份少一月所以这边要加1
                time.append(year).append("-").append(monthOfYear + 1).append("-").append(dayOfMonth);
                //选择完日期后弹出选择时间对话框
                timePickerDialog.show();
            }
        }, year, month-1, day);
        //弹出选择日期对话框
        datePickerDialog.show();
    }

    //图标数据源
    private List<Map<String, Object>> getIcons() {
        List<Map<String, Object>> icons = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("icon", R.mipmap.ic_launcher);
        icons.add(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("icon", R.mipmap.ic_launcher_round);
        icons.add(map1);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("icon", R.drawable.calender_menu_after);
        icons.add(map2);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("icon", R.mipmap.ic_launcher_round);
        icons.add(map3);
        return icons;
    }
}
