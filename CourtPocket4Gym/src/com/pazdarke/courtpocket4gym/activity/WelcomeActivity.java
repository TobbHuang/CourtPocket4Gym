package com.pazdarke.courtpocket4gym.activity;

import java.util.ArrayList;

import com.pazdarke.courtpocket4gym.R;
import com.pazdarke.courtpocket4gym.data.Data;
import com.tencent.android.tpush.XGPushManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Window;

public class WelcomeActivity extends Activity {

	MyHandler myHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_welcome);

		// ÐÅ¸ë³õÊ¼»¯
		Context context = getApplicationContext();
		XGPushManager.registerPush(context);

		myHandler = new MyHandler();

		new Thread(r).start();

	}

	Runnable r = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(3000);
				Bundle b = new Bundle();
				Message msg = new Message();
				msg.setData(b);
				myHandler.sendMessage(msg);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(WelcomeActivity.this);
			Boolean isLogin = prefs.getBoolean("isLogin", false);
			// ÓÐµÇÂ¼×´Ì¬
			if (isLogin) {

				Data.PASSCODE = prefs.getString("Passcode", "");
				Data.PHONE = prefs.getString("Phone", "");
				Data.GYMID = new ArrayList<String>();
				Data.GYMNAME = new ArrayList<String>();
				for (int i = 0; i < prefs.getInt("GymNum", 0); i++) {
					Data.GYMID.add(prefs.getString("GymID" + i, ""));
					Data.GYMNAME.add(prefs.getString("GymName" + i, ""));
				}
				startActivity(new Intent(WelcomeActivity.this,
						MainActivity.class));
				finish();
			} else {
				// ÎÞµÇÂ¼×´Ì¬
				startActivity(new Intent(WelcomeActivity.this,
						LoginActivity.class));
				finish();
			}

		}
	}

}
