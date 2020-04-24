package com.kedy.wechattouch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kedy.wechattouch.calendarlibrary.CalendarView;
import com.noober.menu.FloatMenu;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.Calendar;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";

    // Database management
    private DatabaseHelper mDatabaseHelper;

    // Used for store user information
    private SharedPreferences mUserPreferences;

    // Global Application
    private WeChatTouchApplication mApplication;

    // VARS
    private Boolean mCalendarMenuState = false;
    private Integer mYear, mMonth, mDay;
    private float mScale;
    private int mMaxRecyclerViewHeight;

    // FloatMenu 第三方菜单
    private Point mPoint = new Point();
    private int mClickedPosition;

    // Widgets
    private CalendarView mCalendar;
    private Button mUserMenu;
    private Button mEventButton;
    private Button mCalenderMenu;
    private RecyclerView mUncheckedRecyclerView;
    private RecyclerViewAdapter mUncheckedRecyclerViewAdapter;
    private TextView mCheckedRecyclerViewText;
    private RecyclerViewAdapter mCheckedRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserPreferences = getSharedPreferences("userSharedPreferences", MODE_PRIVATE);
        mDatabaseHelper = new DatabaseHelper(this);
        mApplication = (WeChatTouchApplication) this.getApplication();
        // 最大高度为4个icon
        mScale = getResources().getDisplayMetrics().density;
        mMaxRecyclerViewHeight = (int) (mScale * 180f + 0.5);

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

            //初始化时间
            Calendar sysCalendar = Calendar.getInstance();
            mYear = sysCalendar.get(Calendar.YEAR);
            mMonth = sysCalendar.get(Calendar.MONTH);
            mDay = sysCalendar.get(Calendar.DAY_OF_MONTH);

            init_calender();

            mCalendar = findViewById(R.id.calendar);
            mUserMenu = findViewById(R.id.calendarUserMenu);
            mEventButton = findViewById(R.id.calendarAddEvent);
            mCalenderMenu = findViewById(R.id.calendarMenuButton);

            //日历选择事件
            mCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onClickDay(int year, int month, int day) {
                    mYear = year;
                    mMonth = month;
                    mDay = day;
                    mUncheckedRecyclerViewAdapter.update(mDatabaseHelper, mYear + "-" + mMonth + "-" + mDay, "0");
                    mUncheckedRecyclerView.getLayoutParams().height = Math.min((int) (mScale * 40f * mUncheckedRecyclerViewAdapter.getItemCount() + 0.5), mMaxRecyclerViewHeight);
                    mCheckedRecyclerViewAdapter.update(mDatabaseHelper, mYear + "-" + mMonth + "-" + mDay, "1");
                    if (mCheckedRecyclerViewAdapter.getItemCount() > 0) {
                        mCheckedRecyclerViewText.setVisibility(View.VISIBLE);
                    }else {
                        mCheckedRecyclerViewText.setVisibility(View.INVISIBLE);
                    }
                }
            });

            //根据状态显示或隐藏用户和添加事件按钮
            mCalenderMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCalendarMenuState) {
                        mCalenderMenu.setBackgroundResource(R.drawable.calender_menu_before);
                        mUserMenu.setVisibility(View.GONE);
                        mEventButton.setVisibility(View.GONE);
                        mCalendarMenuState = false;
                    } else {
                        mCalenderMenu.setBackgroundResource(R.drawable.calender_menu_after);
                        mUserMenu.setVisibility(View.VISIBLE);
                        mEventButton.setVisibility(View.VISIBLE);
                        mCalendarMenuState = true;
                    }
                }
            });

            //呼出用户界面
            mUserMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DrawerLayout mainDrawer = findViewById(R.id.mainDrawer);
                    mainDrawer.openDrawer(GravityCompat.END);
                }
            });

            //添加事件
            mEventButton.setOnClickListener(new View.OnClickListener() {
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
        mUncheckedRecyclerView = findViewById(R.id.uncheckedDateDetailRecycler);
        mUncheckedRecyclerViewAdapter = new RecyclerViewAdapter();
        mUncheckedRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String[] info = mUncheckedRecyclerViewAdapter.getPlanTimeAndDescription(position);
                DateDetailDisplayPopup popup = new DateDetailDisplayPopup(MainActivity.this);
                popup.show(mPoint, mYear + "年" + (mMonth + 1) + "月" + (mDay + 1) + "日 " + info[0], info[1]);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                mClickedPosition = position;
                FloatMenu floatMenu = new FloatMenu(MainActivity.this);
                floatMenu.items("完成", "删除");
                floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
                    @Override
                    public void onClick(View v, int position) {
                        if (position == 1)
                            mUncheckedRecyclerViewAdapter.deletePlan(mDatabaseHelper, mClickedPosition);
                        else {
                            mUncheckedRecyclerViewAdapter.setStatus(mDatabaseHelper, mClickedPosition, 1);
                            mUncheckedRecyclerView.getLayoutParams().height = Math.min((int) (mScale * 40f * mUncheckedRecyclerViewAdapter.getItemCount() + 0.5), mMaxRecyclerViewHeight);
                            mCheckedRecyclerViewAdapter.update(mDatabaseHelper, mYear + "-" + mMonth + "-" + mDay, "1");
                            if (mCheckedRecyclerViewAdapter.getItemCount() > 0)
                                mCheckedRecyclerViewText.setVisibility(View.INVISIBLE);
                        }
                        mCalendar.invalidate();
                    }
                });
                floatMenu.show(mPoint);
            }
        });
        mUncheckedRecyclerView.setAdapter(mUncheckedRecyclerViewAdapter);
        mUncheckedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUncheckedRecyclerViewAdapter.update(mDatabaseHelper, mYear + "-" + mMonth + "-" + mDay, "0");
        mUncheckedRecyclerView.getLayoutParams().height = Math.min((int) (mScale * 40f * mUncheckedRecyclerViewAdapter.getItemCount() + 0.5), mMaxRecyclerViewHeight);

        mCheckedRecyclerViewText = findViewById(R.id.checkedDateDetailRecyclerText);
        RecyclerView mCheckedRecyclerView = findViewById(R.id.checkedDateDetailRecycler);
        mCheckedRecyclerViewAdapter = new RecyclerViewAdapter();
        mCheckedRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String[] info = mCheckedRecyclerViewAdapter.getPlanTimeAndDescription(position);
                DateDetailDisplayPopup popup = new DateDetailDisplayPopup(MainActivity.this);
                popup.show(mPoint, mYear + "年" + (mMonth + 1) + "月" + (mDay + 1) + "日 " + info[0], info[1]);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                mClickedPosition = position;
                FloatMenu floatMenu = new FloatMenu(MainActivity.this);
                floatMenu.items("未完成", "删除");
                floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
                    @Override
                    public void onClick(View v, int position) {
                        if (position == 1)
                            mCheckedRecyclerViewAdapter.deletePlan(mDatabaseHelper, mClickedPosition);
                        else {
                            mCheckedRecyclerViewAdapter.setStatus(mDatabaseHelper, mClickedPosition, position);
                            mUncheckedRecyclerViewAdapter.update(mDatabaseHelper, mYear + "-" + mMonth + "-" + mDay, "0");
                            mUncheckedRecyclerView.getLayoutParams().height = Math.min((int) (mScale * 40f * mUncheckedRecyclerViewAdapter.getItemCount() + 0.5), mMaxRecyclerViewHeight);
                            if (mCheckedRecyclerViewAdapter.getItemCount() == 0) {
                                mCheckedRecyclerViewText.setVisibility(View.INVISIBLE);
                            }
                        }
                        mCalendar.invalidate();
                    }
                });
                floatMenu.show(mPoint);
            }
        });
        mCheckedRecyclerView.setAdapter(mCheckedRecyclerViewAdapter);
        mCheckedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCheckedRecyclerViewAdapter.update(mDatabaseHelper, mYear + "-" + mMonth + "-" + mDay, "1");
        if (mCheckedRecyclerViewAdapter.getItemCount() > 0) {
            mCheckedRecyclerViewText.setVisibility(View.VISIBLE);
        }else {
            mCheckedRecyclerViewText.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mApplication.hasNewPlan()) {
            mCalendar.invalidate();
            String[] datetime = ((String) mApplication.getNewPlan().get(2)).split(" ");
            if (!datetime[0].equals(mYear + "-" + mMonth + "-" + mDay))
                return;
            mUncheckedRecyclerViewAdapter.update(mDatabaseHelper, datetime[0], "0");
            mUncheckedRecyclerView.getLayoutParams().height = Math.min((int) (mScale * 40f * mUncheckedRecyclerViewAdapter.getItemCount() + 0.5), mMaxRecyclerViewHeight);
            mCheckedRecyclerViewAdapter.update(mDatabaseHelper, datetime[0], "1");
            if (mCheckedRecyclerViewAdapter.getItemCount() > 0) {
                mCheckedRecyclerViewText.setVisibility(View.VISIBLE);
            }else {
                mCheckedRecyclerViewText.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            mPoint.x = (int) ev.getRawX();
            mPoint.y = (int) ev.getRawY();
        }
        return super.dispatchTouchEvent(ev);
    }
}
