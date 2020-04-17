package com.kedy.wechattouch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private ArrayList<Integer> mPlanIcons = new ArrayList<>();
    private ArrayList<String> mPlanDescriptions = new ArrayList<>();

    private SharedPreferences mUserPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "on Create: started");
        mUserPreferences = getSharedPreferences("userSharedPreferences", MODE_PRIVATE);

        if (mUserPreferences.getString("unionid", "").equals("")) {
            setContentView(R.layout.activity_main);
            setContentView(R.layout.login_main);
            //创建微信api并注册到微信
            Constants.wx_api = WXAPIFactory.createWXAPI(MainActivity.this, Constants.APP_ID, true);
            Constants.wx_api.registerApp(Constants.APP_ID);

            Button login_btn = findViewById(R.id.wechat_login);//得到按钮
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

            Button test_btn = findViewById(R.id.button_test);
            test_btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    test();
                }
            });


        }else {
            setContentView(R.layout.activity_main);
        }
    }

    private void test(){
//        init_calender();
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
    }

    private void init_calender(){
        RecyclerView recyclerView = findViewById(R.id.date_detail_recyc);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mPlanDescriptions, mPlanIcons);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
