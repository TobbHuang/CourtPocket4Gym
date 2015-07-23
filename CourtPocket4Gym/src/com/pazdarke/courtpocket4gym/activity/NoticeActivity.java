package com.pazdarke.courtpocket4gym.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.pazdarke.courtpocket4gym.R;
import com.pazdarke.courtpocket4gym.R.id;
import com.pazdarke.courtpocket4gym.R.layout;
import com.pazdarke.courtpocket4gym.R.menu;
import com.pazdarke.courtpocket4gym.data.Data;
import com.pazdarke.courtpocket4gym.httpConnection.HttpPostConnection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class NoticeActivity extends Activity {

	ProgressDialog progressDialog;

	Spinner mSpinner;
	EditText et_notice;
	TextView tv_textnum;
	
	NoticeHandler noticeHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_notice);

			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCancelable(true);

			initView();
			
			noticeHandler=new NoticeHandler();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initView() {
		// TODO Auto-generated method stub
		ImageView iv_leftarrow = (ImageView) findViewById(R.id.iv_notice_leftarrow);
		iv_leftarrow.setOnClickListener(onClickListener);

		ImageView iv_clear = (ImageView) findViewById(R.id.iv_notice_clear);
		iv_clear.setOnClickListener(onClickListener);

		et_notice = (EditText) findViewById(R.id.et_notice_notice);
		et_notice.addTextChangedListener(textWatcher);

		tv_textnum = (TextView) findViewById(R.id.tv_notice_textnum);
		
		Button btn_submit=(Button)findViewById(R.id.btn_notice_submit);
		btn_submit.setOnClickListener(onClickListener);

		initSpinner();

	}

	private void initSpinner() {
		mSpinner = (Spinner) findViewById(R.id.sp_notice_gym);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				NoticeActivity.this, R.layout.layout_notice_spinner,
				R.id.tv_noticespinnerlayout, Data.GYMNAME) {
			@Override
			public View getDropDownView(int position, View convertView,
					ViewGroup parent) {
				// TODO Auto-generated method stub

				if (convertView == null) {
					convertView = getLayoutInflater().inflate(
							R.layout.item_gymname_spinner, parent, false);
				}
				TextView label = (TextView) convertView
						.findViewById(R.id.tv_gymnameitem);
				label.setText(getItem(position));

				return convertView;
			}
		};
		mSpinner.setAdapter(adapter);

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		mSpinner.setSelection(0);
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.iv_notice_leftarrow:
				finish();
				break;
			case R.id.iv_notice_clear:
				et_notice.setText("");
				break;
			case R.id.btn_notice_submit:
				if(!et_notice.getText().toString().equals("")){
					progressDialog.show();
					new Thread(r_Notice).start();
				}
				break;
			}
		}
	};

	TextWatcher textWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			tv_textnum.setText((60 - et_notice.getText().toString().length())
					+ "");
			if ((60 - et_notice.getText().toString().length()) == 0) {
				tv_textnum.setTextColor(getResources().getColor(R.color.red));
			} else {
				tv_textnum.setTextColor(getResources().getColor(R.color.blue));
			}
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			tv_textnum.setText((60 - et_notice.getText().toString().length())
					+ "");
			if ((60 - et_notice.getText().toString().length()) == 0) {
				tv_textnum.setTextColor(getResources().getColor(R.color.red));
			} else {
				tv_textnum.setTextColor(getResources().getColor(R.color.blue));
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			tv_textnum.setText((60 - et_notice.getText().toString().length())
					+ "");
			if ((60 - et_notice.getText().toString().length()) == 0) {
				tv_textnum.setTextColor(getResources().getColor(R.color.red));
			} else {
				tv_textnum.setTextColor(getResources().getColor(R.color.blue));
			}
		}

	};
	
	Runnable r_Notice=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "UpdateNotice"));
			params.add(new BasicNameValuePair("Phone", Data.PHONE));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("GymID", Data.GYMID.get(mSpinner
					.getSelectedItemPosition())));
			params.add(new BasicNameValuePair("Notice", et_notice.getText().toString()));

			String result = new HttpPostConnection("BusinessServer", params)
					.httpConnection();
			
			Message msg=new Message();
			Bundle b=new Bundle();
			b.putString("result", result);
			msg.setData(b);
			noticeHandler.sendMessage(msg);
		}
	};
	
	@SuppressLint("HandlerLeak")
	class NoticeHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			progressDialog.dismiss();

			String result = msg.getData().getString("result");

			if (result.equals("timeout")) {
				Toast.makeText(NoticeActivity.this, "网络不畅通...",
						Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				JSONObject json = new JSONObject(result);

				if (json.getString("Result").equals("身份验证失败")) {
					Toast.makeText(NoticeActivity.this, "登录过期，请重新登录",
							Toast.LENGTH_SHORT).show();
				} else if (json.getString("Result").equals("资料更新成功")) {
					Toast.makeText(NoticeActivity.this, "发布公告成功",
							Toast.LENGTH_SHORT).show();
					finish();
				} else {
					Toast.makeText(NoticeActivity.this,
							json.getString("Result"), Toast.LENGTH_SHORT)
							.show();
				}

			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(NoticeActivity.this, result, Toast.LENGTH_SHORT)
						.show();
			}
			
			System.out.println(result);
			
		}
	}

}
