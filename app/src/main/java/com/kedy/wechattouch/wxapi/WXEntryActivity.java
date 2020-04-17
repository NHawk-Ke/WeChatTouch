package com.kedy.wechattouch.wxapi;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.kedy.wechattouch.Constants;

import com.kedy.wechattouch.MainActivity;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final int RETURN_MSG_TYPE_LOGIN = 1;
    private static final int RETURN_MSG_TYPE_SHARE = 2;

    private static final String TAG = "WXEntryActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Constants.wx_api.handleIntent(getIntent(), this);
	}

	//微信请求相应
	@Override
	public void onReq(BaseReq baseReq) {

	}

	//获取access token
    private void getAccessToken(String code) {
        String path = "https://api.weixin.qq.com/sns/oauth2/access_token?"
                + "appid=" + Constants.APP_ID
                + "&secret=" + Constants.APP_SECRET
                + "&code=" + code
                + "&grant_type=authorization_code";

		final Request request = new Request.Builder()
				.url(path)
				.build();
		Call call = new OkHttpClient().newCall(request);
		call.enqueue(new Callback() {
			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				finish();
			}

			@RequiresApi(api = Build.VERSION_CODES.KITKAT)
			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
				try {
					JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
					String access_token = jsonObject.getString("access_token");
					String openId = jsonObject.getString("openid");
					getUserInfo(access_token, openId);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
    }

    private void getUserInfo(String access_token, String openId) {
		String url = "https://api.weixin.qq.com/sns/userinfo";
		RequestBody body = new FormBody.Builder()
				.add("access_token", access_token)
				.add("openid", openId)
				.build();
		final Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();
		Call call = new OkHttpClient().newCall(request);
		call.enqueue(new Callback() {
			@Override
			public void onFailure(@NotNull Call call, @NotNull IOException e) {
				finish();
			}

			@RequiresApi(api = Build.VERSION_CODES.KITKAT)
			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
				try {
					JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
					String nickName = jsonObject.getString("nickname");
					String headimgurl = jsonObject.getString("headimgurl");
					String unionid = jsonObject.getString("unionid");
					SharedPreferences mPref = getSharedPreferences("userSharedPreferences", MODE_PRIVATE);
					SharedPreferences.Editor mEditor = mPref.edit();
					mEditor.putString("nickname", nickName);
					mEditor.putString("headimgurl", headimgurl);
					mEditor.putString("unionid", unionid);
					mEditor.apply();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	//发送到微信请求的响应结果
	@Override
	public void onResp(BaseResp resp) {
		switch (resp.errCode) {
			case BaseResp.ErrCode.ERR_OK:
				Log.i("WXTest","onResp OK");
				switch (resp.getType()) {
                    case RETURN_MSG_TYPE_LOGIN:
                        Log.i("WXTest", "微信登录成功");
                        SendAuth.Resp authResp = (SendAuth.Resp)resp;
                        final String code = authResp.code;
						getAccessToken(code);
                        finish();
                        break;
                    case RETURN_MSG_TYPE_SHARE:
                        Log.i("WXTest", "微信分享成功");
                        finish();
                        break;
                }

				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				Log.i("WXTest","onResp ERR_USER_CANCEL ");
				//发送取消
				break;
			case BaseResp.ErrCode.ERR_AUTH_DENIED:
				Log.i("WXTest","onResp ERR_AUTH_DENIED");
				//发送被拒绝
				break;
			default:
				Log.i("WXTest","onResp default errCode " + resp.errCode);
				//发送返回
				break;
		}
		finish();
	}
}
