package com.pazdarke.courtpocket4gym.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.pazdarke.courtpocket4gym.R;
import com.pazdarke.courtpocket4gym.data.Data;
import com.pazdarke.courtpocket4gym.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket4gym.tools.MD5.Encrypt;
import com.tencent.android.tpush.XGPushManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	ProgressDialog pd;

	EditText et_phone, et_password;
	Button btn_login;

	LoginHandler loginHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);

		pd = new ProgressDialog(this);
		pd.setMessage("正在登录...");
		pd.setCancelable(false);

		initView();

		loginHandler = new LoginHandler();

	}

	private void initView() {
		// TODO Auto-generated method stub
		et_phone = (EditText) findViewById(R.id.et_login_phone);
		et_password = (EditText) findViewById(R.id.et_login_password);
		btn_login = (Button) findViewById(R.id.btn_login_login);
		btn_login.setOnClickListener(onClickListener);
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_login_login:
				String phone = et_phone.getText().toString();
				String password = et_password.getText().toString();
				if (phone.equals("") || password.equals("")) {
					Toast.makeText(LoginActivity.this, "手机号和密码不可为空",
							Toast.LENGTH_SHORT).show();
				} else {
					pd.show();
					new Thread(r_Login).start();

					/*
					 * Data.GYMID=new ArrayList<String>(); Data.GYMNAME=new
					 * ArrayList<String>();
					 * 
					 * Data.GYMID.add("1"); Data.GYMNAME.add("ABC");
					 * 
					 * Data.GYMID.add("2"); Data.GYMNAME.add("DEF");
					 * 
					 * Intent intent=new
					 * Intent(LoginActivity.this,MainActivity.class);
					 * startActivity(intent); finish();
					 */
				}
				break;
			}
		}
	};

	Runnable r_Login = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request", "Login"));
				params.add(new BasicNameValuePair("Phone", et_phone.getText()
						.toString()));
				params.add(new BasicNameValuePair("Password", Encrypt
						.MD5(et_phone.getText().toString()
								+ et_password.getText().toString())));

				String result = new HttpPostConnection("BusinessServer", params)
						.httpConnection();

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("result", result);
				msg.setData(b);
				loginHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@SuppressLint("HandlerLeak")
	class LoginHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			try {

				pd.dismiss();

				String result = msg.getData().getString("result");

				if (result.equals("timeout")) {
					Toast.makeText(LoginActivity.this, "登录超时，请检查您的网络设置",
							Toast.LENGTH_SHORT).show();
					return;
				}

				System.out.println(result);

				JSONObject json = new JSONObject(result);

				if (!json.getString("Login").equals("用户名或密码错误")) {

					Data.PASSCODE = json.getString("Login");
					Data.PHONE = et_phone.getText().toString();

					Data.GYMID = new ArrayList<String>();
					Data.GYMNAME = new ArrayList<String>();

					if (json.length() == 1) {
						Toast.makeText(LoginActivity.this, "无效的帐户",
								Toast.LENGTH_SHORT).show();
						return;
					}

					for (int i = 0; i < (json.length() - 1) / 2; i++) {
						Data.GYMID.add(json.getInt("GymID" + i) + "");
						Data.GYMNAME.add(json.getString("GymName" + i));
					}

					// 记录登录状态
					SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(LoginActivity.this);
					Editor pEdit = sp.edit();
					pEdit.putBoolean("isLogin", true);
					pEdit.putString("Passcode", Data.PASSCODE);
					pEdit.putString("Phone", Data.PHONE);
					for (int i = 0; i < (json.length() - 1) / 2; i++) {
						pEdit.putString("GymID" + i, json.getInt("GymID" + i)
								+ "");
						pEdit.putString("GymName" + i,
								json.getString("GymName" + i));
					}
					pEdit.putInt("GymNum", (json.length() - 1) / 2);
					pEdit.commit();

					Intent intent = new Intent(LoginActivity.this,
							MainActivity.class);
					startActivity(intent);
					finish();
				} else {
					Toast.makeText(LoginActivity.this, "登录失败",
							Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT)
						.show();
			}

		}
	}

}
