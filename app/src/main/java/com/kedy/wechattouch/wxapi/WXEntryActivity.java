package com.kedy.wechattouch.wxapi;

import android.app.Activity;

import android.os.Bundle;

import android.util.Log;

import com.kedy.wechattouch.Constants;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;


public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Constants.wx_api.handleIntent(getIntent(), this);
	}

	//微信请求相应
	@Override
	public void onReq(BaseReq baseReq) {

	}

	//发送到微信请求的响应结果
	@Override
	public void onResp(BaseResp resp) {
		switch (resp.errCode) {
			case BaseResp.ErrCode.ERR_OK:
				Log.i("WXTest","onResp OK");

				if(resp instanceof SendAuth.Resp){
					SendAuth.Resp newResp = (SendAuth.Resp) resp;
					//获取微信传回的code
					String code = newResp.code;
					Log.i("WXTest","onResp code = "+code);
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
