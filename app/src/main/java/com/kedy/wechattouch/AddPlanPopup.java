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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPlanPopup extends Activity {
    private static final String TAG = "AddPlanPopup";

    // Database management
    private DatabaseHelper mDatabaseHelper;

    // Global Application
    private WeChatTouchApplication mApplication;

    // Widgets
    private Button mCreatePlanBtn;
    private EditText mPlanTitle;
    private EditText mPlanTime;
    private Spinner mIconSpinner;
    private EditText mPlanDescription;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseHelper = new DatabaseHelper(this);
        mApplication = (WeChatTouchApplication) this.getApplication();

        setContentView(R.layout.add_plan_popup);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .8), (int) (height * .6));

        mCreatePlanBtn = findViewById(R.id.createPlan);
        mPlanTitle = findViewById(R.id.planName);
        mPlanTime = findViewById(R.id.planTime);
        mIconSpinner = findViewById(R.id.planIconSelector);
        mPlanDescription = findViewById(R.id.planDescription);

        //创建计划
        mCreatePlanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mPlanTitle.getText().toString();
                if (title.equals("")) {
                    mPlanTitle.setError(Html.fromHtml("计划名称不能为空"));
                    return;
                }

                Map<String, Object> selectedIcon = (Map<String, Object>) mIconSpinner.getSelectedItem();
                int iconID = (int) selectedIcon.get("icon");
                String datetime = mPlanTime.getText().toString();
                String description = mPlanDescription.getText().toString();
                mApplication.setPlan(title, iconID, datetime, description);
                mDatabaseHelper.insert(title, iconID, datetime, description);
                mDatabaseHelper.checkTable();

                finish();
            }
        });

        //计划时间
        Intent intent = getIntent();
        int year = intent.getIntExtra("YEAR", -1);
        int month = intent.getIntExtra("MONTH", -1);
        int day = intent.getIntExtra("DAY", -1);
        mPlanTime.setText(year + "-" + (month + 1) + "-" + (day + 1) + " 00:00");
        mPlanTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogPick((EditText) v);
            }
        });

        //计划图标
        final SimpleAdapter s_adapter = new SimpleAdapter(
                this, getIcons(), R.layout.icon_item,
                new String[]{"icon", "description"}, new int[]{R.id.icon, R.id.icon_description});
        mIconSpinner.setAdapter(s_adapter);
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
        if (mApplication.getIcons() != null) return mApplication.getIcons();

        List<Map<String, Object>> icons = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("icon", R.drawable.ic_empty);
        map.put("description", "");
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_alarm);
        map.put("description", getString(R.string.ic_alarm));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_badminton);
        map.put("description", getString(R.string.ic_badminton));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_bank);
        map.put("description", getString(R.string.ic_bank));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_barber);
        map.put("description", getString(R.string.ic_barber));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_basketball);
        map.put("description", getString(R.string.ic_basketball));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_beer);
        map.put("description", getString(R.string.ic_beer));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_bicycle);
        map.put("description", getString(R.string.ic_bicycle));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_birthday);
        map.put("description", getString(R.string.ic_birthday));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_bowling);
        map.put("description", getString(R.string.ic_bowling));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_brainstorm);
        map.put("description", getString(R.string.ic_brainstorm));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_car);
        map.put("description", getString(R.string.ic_car));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_cd);
        map.put("description", getString(R.string.ic_cd));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_cloth);
        map.put("description", getString(R.string.ic_cloth));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_game);
        map.put("description", getString(R.string.ic_game));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_gift);
        map.put("description", getString(R.string.ic_gift));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_gym);
        map.put("description", getString(R.string.ic_gym));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_hospital);
        map.put("description", getString(R.string.ic_hospital));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_hotpot);
        map.put("description", getString(R.string.ic_hotpot));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_id);
        map.put("description", getString(R.string.ic_id));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_key);
        map.put("description", getString(R.string.ic_key));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_keyboard);
        map.put("description", getString(R.string.ic_keyboard));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_knife);
        map.put("description", getString(R.string.ic_knife));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_ktv);
        map.put("description", getString(R.string.ic_ktv));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_library);
        map.put("description", getString(R.string.ic_library));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_meeting);
        map.put("description", getString(R.string.ic_meeting));
        icons.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_money);
        map.put("description", getString(R.string.ic_money));
        icons.add(map);
        mApplication.setIcons(icons);
        return icons;
    }
}
