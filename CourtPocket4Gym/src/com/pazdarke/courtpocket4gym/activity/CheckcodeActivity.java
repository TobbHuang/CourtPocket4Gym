package com.pazdarke.courtpocket4gym.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.pazdarke.courtpocket4gym.R;
import com.pazdarke.courtpocket4gym.data.Data;
import com.pazdarke.courtpocket4gym.httpConnection.HttpPostConnection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class CheckcodeActivity extends Activity {
	
	ProgressDialog progressDialog;
	
	ImageView iv_clear;
	EditText et_code;
	TextView tv_code,tv_result,tv_date,tv_time,tv_courtname;
	
	CheckcodeHandler checkcodeHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_checkcode);
		
		progressDialog=new ProgressDialog(this);
		progressDialog.setMessage("正在努力验证中...");
		progressDialog.setCancelable(false);
		
		ImageView iv_leftarrow=(ImageView)findViewById(R.id.iv_checkcode_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);
		
		iv_clear=(ImageView)findViewById(R.id.iv_checkcode_clear);
		iv_clear.setOnClickListener(onClickListener);
		
		Button btn_check=(Button)findViewById(R.id.btn_checkcode_check);
		btn_check.setOnClickListener(onClickListener);
		
		et_code=(EditText)findViewById(R.id.et_checkcode_code);
		tv_code=(TextView)findViewById(R.id.tv_checkcode_code);
		tv_result=(TextView)findViewById(R.id.tv_checkcode_result);
		tv_date=(TextView)findViewById(R.id.tv_checkcode_date);
		tv_time=(TextView)findViewById(R.id.tv_checkcode_time);
		tv_courtname=(TextView)findViewById(R.id.tv_checkcode_courtname);
		
		checkcodeHandler=new CheckcodeHandler();
		
	}
	
	OnClickListener onClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.iv_checkcode_leftarrow:
				finish();
				break;
			case R.id.iv_checkcode_clear:
				et_code.setText("");
				break;
			case R.id.btn_checkcode_check:
				String code=et_code.getText().toString();
				if(!code.equals("")){
					tv_code.setText(code);
					tv_result.setText("");
					tv_date.setText("");
					tv_time.setText("");
					tv_courtname.setText("");
					progressDialog.show();
					new Thread(r_Checkcode).start();
				}
				break;
			}
		}
	};
	
	Runnable r_Checkcode=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "CheckCode"));
			params.add(new BasicNameValuePair("Code", et_code.getText()
					.toString()));
			params.add(new BasicNameValuePair("Phone", Data.PHONE));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));

			String result = new HttpPostConnection("BusinessServer", params)
					.httpConnection();
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result", result);
			msg.setData(b);
			checkcodeHandler.sendMessage(msg);
			
		}
	};
	
	@SuppressLint("HandlerLeak")
	class CheckcodeHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			Bundle b=msg.getData();
			
			progressDialog.dismiss();
			
			String result=b.getString("result");
			System.out.println(result);
			if(result.equals("timeout")){
				tv_result.setText("验证失败，网络不畅通");
			} else{
				try {
					JSONObject json=new JSONObject(result);
					
					tv_result.setText(json.getString("Result"));
					tv_date.setText(json.getString("Date"));
					tv_time.setText(MainActivity.minuteToClock(json
							.getInt("Time") * 30)
							+ "-"
							+ MainActivity.minuteToClock((json.getInt("Time") + json
									.getInt("Weight")) * 30));
					tv_courtname.setText(json.getString("CourtName"));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
}
