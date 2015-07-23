package com.pazdarke.courtpocket4gym.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.pazdarke.courtpocket4gym.R;
import com.pazdarke.courtpocket4gym.data.Data;
import com.pazdarke.courtpocket4gym.httpConnection.HttpPostConnection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class BillActivity extends Activity {

	ProgressDialog progressDialog;

	JSONObject jsn_billinfo;

	int i;
	boolean isDateShow;
	DatePickerDialog datePickerDialog;

	Spinner mSpinner;

	ArrayList<String> list;
	BilllistAdapter billlistAdapter;

	String GymName;
	String GymID;

	TextView tv_startdate, tv_enddate;

	BillHandler billHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_bill);

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("加载中...加载时间随选择时间范围的增大而变长");
		progressDialog.setCancelable(true);

		GymName = getIntent().getStringExtra("GymName");
		GymID = getIntent().getStringExtra("GymID");

		initView();

		billHandler = new BillHandler();

	}

	@SuppressLint("SimpleDateFormat")
	private void initView() {
		// TODO Auto-generated method stub
		RelativeLayout rl_leftarrow = (RelativeLayout) findViewById(R.id.rl_bill_leftarrow);
		rl_leftarrow.setOnClickListener(onClickListener);

		TextView tv_title = (TextView) findViewById(R.id.tv_bill_gymname);
		tv_title.setText(GymName);

		tv_startdate = (TextView) findViewById(R.id.tv_bill_startdate);
		tv_enddate = (TextView) findViewById(R.id.tv_bill_enddate);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		tv_startdate.setText(dateFormat.format(date));
		tv_enddate.setText(dateFormat.format(date));
		tv_startdate.setOnClickListener(onClickListener);
		tv_enddate.setOnClickListener(onClickListener);

		Button btn_query = (Button) findViewById(R.id.btn_bill_query);
		btn_query.setOnClickListener(onClickListener);

		init_spinner();
		initList();

	}

	// type=0为startdate
	void initTimeDialog(final int type) {

		String[] date;
		if (type == 0) {
			date = tv_startdate.getText().toString().split("-");
		} else {
			date = tv_enddate.getText().toString().split("-");
		}

		i = 0;// 解决OnDateSet可能执行两次的bug
		isDateShow = true;
		datePickerDialog = new DatePickerDialog(
				BillActivity.this,
				new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker arg0, int arg1, int arg2,
							int arg3) {
						// TODO Auto-generated method stub

						if (i == 0) {
							i++;
							Time t = new Time("GMT+8");
							t.setToNow();
							// 检测日期是否选择有误，即不能大于当前日期
							if (arg1 > t.year
									|| (arg1 == t.year && (arg2 + 1) > (t.month + 1))
									|| (arg1 == t.year
											&& (arg2 + 1) == (t.month + 1) && arg3 > t.monthDay)) {
								Toast.makeText(BillActivity.this,
										"选择日期不可大于当前日期", Toast.LENGTH_SHORT)
										.show();
							} else {
								String str;
								// 格式化日期格式，主要是加 0
								str = (arg1) + "-";
								if (arg2 <= 8) {
									str += "0";
								}
								str += (arg2 + 1);
								str += "-";
								if (arg3 < 10) {
									str += "0";
								}
								str += arg3;

								if (type == 0) {
									tv_startdate.setText(str);
								} else {
									tv_enddate.setText(str);
								}

								isDateShow = false;

							}
						}
					}

				}, Integer.parseInt(date[0]), Integer.parseInt(date[1]) - 1,
				Integer.parseInt(date[2]));
		datePickerDialog.show();
	}

	private void init_spinner() {
		// TODO Auto-generated method stub

		ArrayList<String> list = new ArrayList<String>();
		list.add("全部");
		list.add("订场订单");
		list.add("匹配订单");
		list.add("约战订单");
		list.add("卡券订单");

		mSpinner = (Spinner) findViewById(R.id.sp_bill_type);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				BillActivity.this, R.layout.layout_bill_spinner,
				R.id.tv_billspinnerlayout, list) {
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
				refreshList();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		mSpinner.setSelection(0);

	}

	private void initList() {
		list = new ArrayList<String>();
		billlistAdapter = new BilllistAdapter(this, list);

		ListView lv_bill = (ListView) findViewById(R.id.lv_bill);
		lv_bill.setAdapter(billlistAdapter);

	}

	private void refreshList() {
		try {
			if (jsn_billinfo != null) {
				list.clear();
				JSONObject json;
				switch (mSpinner.getSelectedItemPosition()) {
				case 0:
					for (int i = 0; i < jsn_billinfo.getInt("BillNum"); i++) {
						json = jsn_billinfo.getJSONObject("Bill" + i);
						if (json.getInt("Status") != 0) {
							json.put("Type", 0);
							if (json.getInt("SmallBillNum") == 1) {
								json.put("CourtName",
										json.getString("CourtName0"));
								json.put("Time", json.getInt("Time0"));
								list.add(json.toString());
							} else {
								for (int j = json.getInt("SmallBillNum"); j > 0; j--) {
									json.put(
											"CourtName",
											json.getString("CourtName"
													+ (j - 1)));
									json.put("Time",
											json.getInt("Time" + (j - 1)));
									list.add(json.toString());
								}
							}
						}
					}
					
					for (int i = 0; i < jsn_billinfo.getInt("MatchBillNum"); i++) {
						json = jsn_billinfo.getJSONObject("MatchBill" + i);
						if (json.getInt("Status") == -1
								|| json.getInt("Status") == 4
								|| json.getInt("Status") == 5
								|| json.getInt("Status") == 6) {
							json.put("Type", 1);
							list.add(json.toString());
						}
					}

					for (int i = 0; i < jsn_billinfo.getInt("FightBillNum"); i++) {
						json = jsn_billinfo.getJSONObject("FightBill" + i);
						if (json.getInt("Status") == -1
								|| json.getInt("Status") == 4
								|| json.getInt("Status") == 5
								|| json.getInt("Status") == 6) {
							json.put("Type", 2);
							list.add(json.toString());
						}
					}

					for (int i = 0; i < jsn_billinfo.getInt("CardBillNum"); i++) {
						json = jsn_billinfo.getJSONObject("CardBill" + i);
						if (json.getInt("Status") == 2
								|| json.getInt("Status") == 3
								|| json.getInt("Status") == 4) {
							json.put("Type", 3);
							list.add(json.toString());
						}
					}
					break;
				case 1:
					for (int i = 0; i < jsn_billinfo.getInt("BillNum"); i++) {
						json = jsn_billinfo.getJSONObject("Bill" + i);
						if (json.getInt("Status") != 0) {
							json.put("Type", 0);
							if (json.getInt("SmallBillNum") == 1) {
								json.put("CourtName",
										json.getString("CourtName0"));
								json.put("Time", json.getInt("Time0"));
								list.add(json.toString());
							} else {
								for (int j = json.getInt("SmallBillNum"); j > 0; j--) {
									json.put(
											"CourtName",
											json.getString("CourtName"
													+ (j - 1)));
									json.put("Time",
											json.getInt("Time" + (j - 1)));
									list.add(json.toString());
								}
							}
						}
					}
					break;
				case 2:
					for (int i = 0; i < jsn_billinfo.getInt("MatchBillNum"); i++) {
						json = jsn_billinfo.getJSONObject("MatchBill" + i);
						if (json.getInt("Status") == -1
								|| json.getInt("Status") == 4
								|| json.getInt("Status") == 5
								|| json.getInt("Status") == 6) {
							json.put("Type", 1);
							list.add(json.toString());
						}
					}
					break;
				case 3:
					for (int i = 0; i < jsn_billinfo.getInt("FightBillNum"); i++) {
						json = jsn_billinfo.getJSONObject("FightBill" + i);
						if (json.getInt("Status") == -1
								|| json.getInt("Status") == 4
								|| json.getInt("Status") == 5
								|| json.getInt("Status") == 6) {
							json.put("Type", 2);
							list.add(json.toString());
						}
					}
					break;
				case 4:
					for (int i = 0; i < jsn_billinfo.getInt("CardBillNum"); i++) {
						json = jsn_billinfo.getJSONObject("CardBill" + i);
						if (json.getInt("Status") == 2
								|| json.getInt("Status") == 3
								|| json.getInt("Status") == 4) {
							json.put("Type", 3);
							list.add(json.toString());
						}
					}
					break;
				}
			}
			
			billlistAdapter.notifyDataSetChanged();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.rl_bill_leftarrow:
				finish();
				break;
			case R.id.tv_bill_startdate:
				initTimeDialog(0);
				break;
			case R.id.tv_bill_enddate:
				initTimeDialog(1);
				break;
			case R.id.btn_bill_query:
				progressDialog.show();
				list.clear();
				new Thread(r_Billinfo).start();
				break;
			}
		}
	};

	Runnable r_Billinfo = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "GetGymBill"));
			params.add(new BasicNameValuePair("Phone", Data.PHONE));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("GymID", GymID));
			params.add(new BasicNameValuePair("DateType", "GenerateTime"));
			params.add(new BasicNameValuePair("StartDate", tv_startdate
					.getText().toString()));
			params.add(new BasicNameValuePair("EndDate", tv_enddate.getText()
					.toString()));

			String result = new HttpPostConnection("BusinessServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			msg.setData(b);
			billHandler.sendMessage(msg);
		}
	};

	class BillHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			try {

				String result = msg.getData().getString("result");

				if (result.equals("timeout")) {
					progressDialog.dismiss();
					Toast.makeText(BillActivity.this, "网络连接不畅通",
							Toast.LENGTH_SHORT).show();
					return;
				}

				jsn_billinfo = new JSONObject(msg.getData().getString("result"));
				JSONObject json;

				for (int i = 0; i < jsn_billinfo.getInt("BillNum"); i++) {
					json = jsn_billinfo.getJSONObject("Bill" + i);
					if (json.getInt("Status") != 0) {
						json.put("Type", 0);
						if (json.getInt("SmallBillNum") == 1) {
							json.put("CourtName", json.getString("CourtName0"));
							json.put("Time", json.getInt("Time0"));
							list.add(json.toString());
						} else {
							for (int j = json.getInt("SmallBillNum"); j > 0; j--) {
								json.put("CourtName",
										json.getString("CourtName" + (j - 1)));
								json.put("Time", json.getInt("Time" + (j - 1)));
								list.add(json.toString());
							}
						}
					}
				}

				for (int i = 0; i < jsn_billinfo.getInt("MatchBillNum"); i++) {
					json = jsn_billinfo.getJSONObject("MatchBill" + i);
					if (json.getInt("Status") == -1
							|| json.getInt("Status") == 4
							|| json.getInt("Status") == 5
							|| json.getInt("Status") == 6) {
						json.put("Type", 1);
						list.add(json.toString());
					}
				}

				for (int i = 0; i < jsn_billinfo.getInt("FightBillNum"); i++) {
					json = jsn_billinfo.getJSONObject("FightBill" + i);
					if (json.getInt("Status") == -1
							|| json.getInt("Status") == 4
							|| json.getInt("Status") == 5
							|| json.getInt("Status") == 6) {
						json.put("Type", 2);
						list.add(json.toString());
					}
				}

				for (int i = 0; i < jsn_billinfo.getInt("CardBillNum"); i++) {
					json = jsn_billinfo.getJSONObject("CardBill" + i);
					if (json.getInt("Status") == 2
							|| json.getInt("Status") == 3
							|| json.getInt("Status") == 4) {
						json.put("Type", 3);
						list.add(json.toString());
					}
				}

				billlistAdapter.notifyDataSetChanged();
				progressDialog.dismiss();

			} catch (Exception e) {
				e.printStackTrace();
				progressDialog.dismiss();
				Toast.makeText(BillActivity.this, "身份验证失败，请重新登录",
						Toast.LENGTH_SHORT).show();
			}

		}
	}

	class BilllistAdapter extends BaseAdapter {

		Context context;
		ArrayList<String> list;

		public BilllistAdapter(Context context, ArrayList<String> list) {
			this.context = context;
			this.list = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub

			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.item_bill, null);
			}

			try {

				TextView tv_order = (TextView) convertView
						.findViewById(R.id.tv_itembill_order);
				TextView tv_generatetime = (TextView) convertView
						.findViewById(R.id.tv_itembill_generatetime);
				TextView tv_courtname = (TextView) convertView
						.findViewById(R.id.tv_itembill_courtname);
				TextView tv_time = (TextView) convertView
						.findViewById(R.id.tv_itembill_time);
				TextView tv_price = (TextView) convertView
						.findViewById(R.id.tv_itembill_price);
				TextView tv_status = (TextView) convertView
						.findViewById(R.id.tv_itembill_status);
				TextView tv_type = (TextView) convertView
						.findViewById(R.id.tv_itembill_type);

				tv_order.setText((position + 1) + "");

				JSONObject json = new JSONObject(list.get(position));

				switch (json.getInt("Type")) {
				case 0:
					tv_generatetime.setText(json.getString("GenerateTime")
							.substring(0, 19));
					tv_courtname.setText(json.getString("CourtName"));
					tv_time.setText(json.getString("Date")
							+ " "
							+ MainActivity.minuteToClock(json.getInt("Time") * 30));
					tv_price.setText(Data.doubleTrans(json.getDouble("Price")));
					switch (json.getInt("Status")) {
					case -1:
						tv_status.setText("未验证");
						break;
					case 0:
						tv_status.setText("关闭");
						break;
					case 1:
						tv_status.setText("未付款");
						break;
					case 2:
						tv_status.setText("未验证");
						break;
					case 3:
						tv_status.setText("已验证");
						break;
					case 4:
						tv_status.setText("已验证");
						break;
					}
					tv_type.setText("订场");
					break;
				case 1:
					tv_generatetime.setText(json.getString("GenerateTime")
							.substring(0, 19));
					tv_courtname.setText(json.getString("CourtName"));
					tv_time.setText(json.getString("Date")
							+ " "
							+ MainActivity.minuteToClock(json.getInt("Time") * 30));
					tv_price.setText(Data.doubleTrans(json.getDouble("Price")));
					switch (json.getInt("Status")) {
					case -1:
						tv_status.setText("未验证");
						break;
					case 0:
						tv_status.setText("关闭");
						break;
					case 1:
						tv_status.setText("正在寻找对手");
						break;
					case 2:
						tv_status.setText("未付款");
						break;
					case 3:
						tv_status.setText("未付款");
						break;
					case 4:
						tv_status.setText("未验证");
						break;
					case 5:
						tv_status.setText("已验证");
						break;
					case 6:
						tv_status.setText("已验证");
						break;
					case 11:
						tv_status.setText("重新寻找对手");
						break;
					}
					tv_type.setText("匹配");
					break;
				case 2:
					tv_generatetime.setText(json.getString("GenerateTime")
							.substring(0, 19));
					tv_courtname.setText(json.getString("CourtName"));
					tv_time.setText(json.getString("Date")
							+ " "
							+ MainActivity.minuteToClock(json.getInt("Time") * 30));
					tv_price.setText(Data.doubleTrans(json.getDouble("Price")));
					switch (json.getInt("Status")) {
					case -1:
						tv_status.setText("未验证");
						break;
					case 0:
						tv_status.setText("关闭");
						break;
					case 1:
						tv_status.setText("待付款");
						break;
					case 2:
						tv_status.setText("待付款");
						break;
					case 3:
						tv_status.setText("待付款");
						break;
					case 4:
						tv_status.setText("未验证");
						break;
					case 5:
						tv_status.setText("已验证");
						break;
					case 6:
						tv_status.setText("已验证");
						break;
					}
					tv_type.setText("约战");
					break;
				case 3:
					tv_generatetime.setText(json.getString("GenerateTime")
							.substring(0, 19));
					tv_courtname.setText(json.getString("CardName"));
					tv_price.setText(Data.doubleTrans(json.getDouble("Price")));
					switch (json.getInt("Status")) {
					case 0:
						tv_status.setText("关闭");
						break;
					case 1:
						tv_status.setText("待付款");
						break;
					case 2:
						tv_status.setText("有效");
						break;
					case 3:
						tv_status.setText("无效");
						break;
					case 4:
						tv_status.setText("无效");
						break;
					}
					tv_type.setText("卡券");
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return convertView;
		}

	}

}
