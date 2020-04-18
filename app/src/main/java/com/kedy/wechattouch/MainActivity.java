package com.kedy.wechattouch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";

    // Database management
    private DatabaseHelper mDatabaseHelper;

    // Used for store user information
    private SharedPreferences mUserPreferences;

    // VARS
    private Boolean mCalendarMenuState = false;
    private Integer mYear, mMonth, mDay;
    private ArrayList<Integer> mPlanIcons = new ArrayList<>();
    private ArrayList<String> mPlanDescriptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "on Create: started");
        mUserPreferences = getSharedPreferences("userSharedPreferences", MODE_PRIVATE);
        mDatabaseHelper = new DatabaseHelper(this);

        //如果用户还没有登录
        if (!mUserPreferences.getString("unionid", "").equals("")) {
            setContentView(R.layout.activity_main);
            setContentView(R.layout.login_main);
            //创建微信api并注册到微信
            Constants.wx_api = WXAPIFactory.createWXAPI(MainActivity.this, Constants.APP_ID, true);
            Constants.wx_api.registerApp(Constants.APP_ID);

            Button login_btn = findViewById(R.id.wechatLogin);//得到按钮
            login_btn.setOnClickListener(new View.OnClickListener() { //注册按钮点击事件
                @Override
                public void onClick(View v) {
                    if (!Constants.wx_api.isWXAppInstalled()){
                        Toast toast = Toast.makeText(getApplicationContext(), "请先安装微信客户端", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }

                    //发起登录请求
                    final SendAuth.Req req = new SendAuth.Req();
                    req.scope = "snsapi_userinfo";
                    req.state = "WeChatTouch_login";
                    Constants.wx_api.sendReq(req);
                }
            });

        // 用户已经登录，跳转到主界面
        }else {
            setContentView(R.layout.activity_main);
            init_calender();

            final CalendarView calendar = findViewById(R.id.calendar);
            final Button userMenu = findViewById(R.id.calendarUserMenu);
            final Button eventButton = findViewById(R.id.calendarAddEvent);
            final Button calenderMenu = findViewById(R.id.calendarMenuButton);

            //初始化时间
            Calendar sysCalendar = Calendar.getInstance();
            mYear = sysCalendar.get(Calendar.YEAR);
            mMonth = sysCalendar.get(Calendar.MONTH) + 1;
            mDay = sysCalendar.get(Calendar.DAY_OF_MONTH);
            //日历选择事件
            calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                    mYear = year;
                    mMonth = month + 1;
                    mDay = dayOfMonth;
                }
            });

            //根据状态显示或隐藏用户和添加事件按钮
            calenderMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCalendarMenuState) {
                        calenderMenu.setBackgroundResource(R.drawable.calender_menu_before);
                        userMenu.setVisibility(View.GONE);
                        eventButton.setVisibility(View.GONE);
                        mCalendarMenuState = false;
                    } else {
                        calenderMenu.setBackgroundResource(R.drawable.calender_menu_after);
                        userMenu.setVisibility(View.VISIBLE);
                        eventButton.setVisibility(View.VISIBLE);
                        mCalendarMenuState = true;
                    }
                }
            });

            //呼出用户界面
            userMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DrawerLayout mainDrawer = findViewById(R.id.mainDrawer);
                    mainDrawer.openDrawer(GravityCompat.END);
                }
            });

            //添加事件
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, AddPlanPopup.class);
                    intent.putExtra("YEAR", mYear);
                    intent.putExtra("MONTH", mMonth);
                    intent.putExtra("DAY", mDay);
                    startActivity(intent);
                }
            });
        }
    }

    private void init_calender(){
        RecyclerView recyclerView = findViewById(R.id.dateDetailRecycler);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mPlanDescriptions, mPlanIcons);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "on resume start");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "on pause start");

    }
}
