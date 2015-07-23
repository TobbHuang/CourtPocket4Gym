package com.pazdarke.courtpocket4gym.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.pazdarke.courtpocket4gym.R;
import com.pazdarke.courtpocket4gym.data.Data;
import com.pazdarke.courtpocket4gym.httpConnection.HttpPostConnection;
import com.pazdarke.courtpocket4gym.view.BaseTableAdapter;
import com.pazdarke.courtpocket4gym.view.TableFixHeaders;
import com.tencent.android.tpush.XGPushManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;

public class MainActivity extends Activity {

	int screenWidth, screenHeight;

	ProgressDialog progressDialog;

	Spinner mSpinner;

	LinearLayout ll_date1, ll_date2, ll_date3, ll_date4, ll_date5, ll_date6,
			ll_date7, ll_date8, ll_date9, ll_date10;
	TextView tv_week1, tv_week2, tv_week3, tv_week4, tv_week5, tv_week6,
			tv_week7, tv_week8, tv_week9, tv_week10;
	TextView tv_date1, tv_date2, tv_date3, tv_date4, tv_date5, tv_date6,
			tv_date7, tv_date8, tv_date9, tv_date10;
	LinearLayout[] ll_date;
	TextView[] tv_week;
	TextView[] tv_date;
	String currentSelectDate;

	SimpleDateFormat dateFormat;
	SimpleDateFormat weekFormat;
	SimpleDateFormat fullDateFormat;
	List<Date> days;

	TableFixHeaders ll_table;

	boolean[][] courtIsSelected;
	int countIsSelected = 0;
	int countOprate = 0;
	LinearLayout ll_instruction, ll_selection;

	int courtNum;
	String[] courtID;
	String[] courtName;
	String[][] courtPrice;
	int[][] isBooked;
	int startTime, endTime, weight;

	Button btn_reservation, btn_price;
	PopupWindow pop_operation;

	String gymID;

	CourtHandler courtHandler;
	ChangeReservationHandler changeReservationHandler;
	ChangePriceHandler changePriceHandler;
	TimeoutHandler timeoutHandler;

	int loadTime = 0;// 多线程加载数据，用于判断是否加载完毕
	ExecutorService pool;
	private Lock lock = new ReentrantLock();// 锁对象

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_main);

			// 注册信鸽
			XGPushManager.registerPush(MainActivity.this, "gym" + Data.PHONE);

			pool = Executors.newFixedThreadPool(15);

			DisplayMetrics metric = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metric);
			screenHeight = metric.heightPixels;
			screenWidth = metric.widthPixels;

			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载中...");
			progressDialog.setCancelable(true);

			initView();

			courtHandler = new CourtHandler();
			changeReservationHandler = new ChangeReservationHandler();
			changePriceHandler = new ChangePriceHandler();
			timeoutHandler = new TimeoutHandler();

			progressDialog.show();
			new Thread(r_GetCourtInfo).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void initView() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.layout_popupwindow, null);
		pop_operation = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		pop_operation.setBackgroundDrawable(new BitmapDrawable());
		pop_operation.setOutsideTouchable(true);
		pop_operation.setFocusable(true);
		TextView tv_checkcode = (TextView) view
				.findViewById(R.id.tv_pop_checkcode);
		tv_checkcode.setOnClickListener(onClickListener);

		TextView tv_notice = (TextView) view.findViewById(R.id.tv_pop_notice);
		tv_notice.setOnClickListener(onClickListener);

		TextView tv_bill = (TextView) view.findViewById(R.id.tv_pop_bill);
		tv_bill.setOnClickListener(onClickListener);

		TextView tv_quitlogin = (TextView) view
				.findViewById(R.id.tv_pop_quitlogin);
		tv_quitlogin.setOnClickListener(onClickListener);

		LinearLayout ll_operation = (LinearLayout) findViewById(R.id.ll_mainpage_operation);
		ll_operation.setOnClickListener(onClickListener);

		init_date();

		ll_table = (TableFixHeaders) findViewById(R.id.ll_book_table);
		ll_table.getLayoutParams().height = (int) (screenHeight * 0.5);

		ll_instruction = (LinearLayout) findViewById(R.id.ll_mainpage_instruction);
		ll_selection = (LinearLayout) findViewById(R.id.ll_mainpage_selection);

		TextView tv_refresh = (TextView) findViewById(R.id.tv_mainpage_refresh);
		tv_refresh.setOnClickListener(onClickListener);

		btn_reservation = (Button) findViewById(R.id.btn_mainpage_reservation);
		btn_reservation.setOnClickListener(onClickListener);

		btn_price = (Button) findViewById(R.id.btn_mainpage_price);
		// btn_price.setOnClickListener(onClickListener);

		init_spinner();
	}

	void initDialog() {
		final AlertDialog alertDialog = new AlertDialog.Builder(
				MainActivity.this).create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
		Window window = alertDialog.getWindow();
		window.setContentView(R.layout.dialog_updateinfo);

		WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
		lp.width = (int) (screenWidth * 0.9);// 定义宽度
		lp.height = (int) (screenWidth * 0.6);// 定义高度

		alertDialog.getWindow().setAttributes(lp);

		final EditText et_dialog = (EditText) window
				.findViewById(R.id.et_updatedialog);
		et_dialog.setFocusable(true);
		window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		ImageView iv_dialog_clear = (ImageView) window
				.findViewById(R.id.iv_updatedialog_clear);
		Button btn_dialog_negative = (Button) window
				.findViewById(R.id.btn_updatedialog_negative);
		Button btn_dialog_position = (Button) window
				.findViewById(R.id.btn_updatedialog_position);
		iv_dialog_clear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				et_dialog.setText("");
			}
		});

		btn_dialog_negative.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
			}
		});

		btn_dialog_position.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!et_dialog.getText().toString().equals("")) {
					alertDialog.dismiss();

					progressDialog.show();
					for (int i = 0; i < courtNum; i++) {
						for (int j = 0; j < 24; j++) {
							if (courtIsSelected[i][j]) {
								new ChangePriceThread(j, courtID[i],
										courtName[i], Integer
												.parseInt(et_dialog.getText()
														.toString())).start();
							}
						}
					}
				}
			}
		});

	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_mainpage_reservation:
				if (countIsSelected != 0) {
					progressDialog.show();

					for (int i = 0; i < courtNum; i++) {
						for (int j = 0; j < 48; j++) {
							if (courtIsSelected[i][j]) {
								if (isBooked[i][j] == 2)
									new ChangeReservationThread(j, courtID[i],
											courtName[i], 1).start();
								else if (isBooked[i][j] == 1)
									new ChangeReservationThread(j, courtID[i],
											courtName[i], 0).start();
							}
						}
					}

				}
				break;
			case R.id.btn_mainpage_price:
				if (countIsSelected != 0) {
					initDialog();
				}
				break;
			case R.id.tv_mainpage_refresh:
				progressDialog.show();
				ll_selection.removeAllViews();
				ll_selection.setVisibility(View.GONE);
				ll_instruction.setVisibility(View.VISIBLE);
				countIsSelected = 0;
				new Thread(r_GetCourtInfo).start();
				break;
			case R.id.ll_mainpage_operation:
				if (pop_operation.isShowing()) {
					pop_operation.dismiss();
				} else {
					pop_operation.showAsDropDown(v);
				}
				break;
			case R.id.tv_pop_checkcode:
				pop_operation.dismiss();
				startActivity(new Intent(MainActivity.this,
						CheckcodeActivity.class));
				break;
			case R.id.tv_pop_notice:
				pop_operation.dismiss();
				startActivity(new Intent(MainActivity.this,
						NoticeActivity.class));
				break;
			case R.id.tv_pop_bill:
				pop_operation.dismiss();
				Intent intent = new Intent(MainActivity.this,
						BillActivity.class);
				intent.putExtra("GymID", gymID);
				intent.putExtra("GymName", (String) mSpinner.getSelectedItem());
				startActivity(intent);
				break;
			case R.id.tv_pop_quitlogin:
				pop_operation.dismiss();

				// 反注册信鸽
				XGPushManager.registerPush(MainActivity.this, "*");

				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(MainActivity.this);
				Editor pEdit = sp.edit();
				pEdit.putBoolean("isLogin", false);
				pEdit.commit();

				startActivity(new Intent(MainActivity.this, LoginActivity.class));
				finish();
				break;
			}
		}
	};

	private void init_spinner() {
		// TODO Auto-generated method stub

		ArrayList<String> list = new ArrayList<String>();
		list = Data.GYMNAME;

		mSpinner = (Spinner) findViewById(R.id.sp_mainpage_gymname);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				MainActivity.this, R.layout.layout_gymname_spinner,
				R.id.tv_gymnamespinnerlayout, list) {
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
				gymID = Data.GYMID.get(position);
				progressDialog.show();
				ll_selection.removeAllViews();
				ll_selection.setVisibility(View.GONE);
				ll_instruction.setVisibility(View.VISIBLE);
				countIsSelected = 0;
				loadTime = 0;
				new Thread(r_GetCourtInfo).start();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		mSpinner.setSelection(0);
		gymID = Data.GYMID.get(0);

	}

	void init_date() {

		// 获取未来10天的日期
		dateFormat = new SimpleDateFormat("MM月dd日");
		weekFormat = new SimpleDateFormat("EEE");
		fullDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new Date();
		days = dateToWeek(currentDate);

		ll_date1 = (LinearLayout) findViewById(R.id.ll_mainpage_date1);
		ll_date1.setOnClickListener(dateOnClickListener);
		ll_date2 = (LinearLayout) findViewById(R.id.ll_mainpage_date2);
		ll_date2.setOnClickListener(dateOnClickListener);
		ll_date3 = (LinearLayout) findViewById(R.id.ll_mainpage_date3);
		ll_date3.setOnClickListener(dateOnClickListener);
		ll_date4 = (LinearLayout) findViewById(R.id.ll_mainpage_date4);
		ll_date4.setOnClickListener(dateOnClickListener);
		ll_date5 = (LinearLayout) findViewById(R.id.ll_mainpage_date5);
		ll_date5.setOnClickListener(dateOnClickListener);
		ll_date6 = (LinearLayout) findViewById(R.id.ll_mainpage_date6);
		ll_date6.setOnClickListener(dateOnClickListener);
		ll_date7 = (LinearLayout) findViewById(R.id.ll_mainpage_date7);
		ll_date7.setOnClickListener(dateOnClickListener);
		ll_date8 = (LinearLayout) findViewById(R.id.ll_mainpage_date8);
		ll_date8.setOnClickListener(dateOnClickListener);
		ll_date9 = (LinearLayout) findViewById(R.id.ll_mainpage_date9);
		ll_date9.setOnClickListener(dateOnClickListener);
		ll_date10 = (LinearLayout) findViewById(R.id.ll_mainpage_date10);
		ll_date10.setOnClickListener(dateOnClickListener);

		tv_week1 = (TextView) findViewById(R.id.tv_mainpage_week1);
		tv_week2 = (TextView) findViewById(R.id.tv_mainpage_week2);
		tv_week2.setText(weekFormat.format(days.get(1)) + "");
		tv_week3 = (TextView) findViewById(R.id.tv_mainpage_week3);
		tv_week3.setText(weekFormat.format(days.get(2)) + "");
		tv_week4 = (TextView) findViewById(R.id.tv_mainpage_week4);
		tv_week4.setText(weekFormat.format(days.get(3)) + "");
		tv_week5 = (TextView) findViewById(R.id.tv_mainpage_week5);
		tv_week5.setText(weekFormat.format(days.get(4)) + "");
		tv_week6 = (TextView) findViewById(R.id.tv_mainpage_week6);
		tv_week6.setText(weekFormat.format(days.get(5)) + "");
		tv_week7 = (TextView) findViewById(R.id.tv_mainpage_week7);
		tv_week7.setText(weekFormat.format(days.get(6)) + "");
		tv_week8 = (TextView) findViewById(R.id.tv_mainpage_week8);
		tv_week8.setText(weekFormat.format(days.get(7)) + "");
		tv_week9 = (TextView) findViewById(R.id.tv_mainpage_week9);
		tv_week9.setText(weekFormat.format(days.get(8)) + "");
		tv_week10 = (TextView) findViewById(R.id.tv_mainpage_week10);
		tv_week10.setText(weekFormat.format(days.get(9)) + "");

		tv_date1 = (TextView) findViewById(R.id.tv_mainpage_date1);
		tv_date1.setText(dateFormat.format(days.get(0)) + "");
		tv_date2 = (TextView) findViewById(R.id.tv_mainpage_date2);
		tv_date2.setText(dateFormat.format(days.get(1)) + "");
		tv_date3 = (TextView) findViewById(R.id.tv_mainpage_date3);
		tv_date3.setText(dateFormat.format(days.get(2)) + "");
		tv_date4 = (TextView) findViewById(R.id.tv_mainpage_date4);
		tv_date4.setText(dateFormat.format(days.get(3)) + "");
		tv_date5 = (TextView) findViewById(R.id.tv_mainpage_date5);
		tv_date5.setText(dateFormat.format(days.get(4)) + "");
		tv_date6 = (TextView) findViewById(R.id.tv_mainpage_date6);
		tv_date6.setText(dateFormat.format(days.get(5)) + "");
		tv_date7 = (TextView) findViewById(R.id.tv_mainpage_date7);
		tv_date7.setText(dateFormat.format(days.get(6)) + "");
		tv_date8 = (TextView) findViewById(R.id.tv_mainpage_date8);
		tv_date8.setText(dateFormat.format(days.get(7)) + "");
		tv_date9 = (TextView) findViewById(R.id.tv_mainpage_date9);
		tv_date9.setText(dateFormat.format(days.get(8)) + "");
		tv_date10 = (TextView) findViewById(R.id.tv_mainpage_date10);
		tv_date10.setText(dateFormat.format(days.get(9)) + "");

		ll_date = new LinearLayout[10];
		ll_date[0] = ll_date1;
		ll_date[1] = ll_date2;
		ll_date[2] = ll_date3;
		ll_date[3] = ll_date4;
		ll_date[4] = ll_date5;
		ll_date[5] = ll_date6;
		ll_date[6] = ll_date7;
		ll_date[7] = ll_date8;
		ll_date[8] = ll_date9;
		ll_date[9] = ll_date10;

		tv_week = new TextView[10];
		tv_week[0] = tv_week1;
		tv_week[1] = tv_week2;
		tv_week[2] = tv_week3;
		tv_week[3] = tv_week4;
		tv_week[4] = tv_week5;
		tv_week[5] = tv_week6;
		tv_week[6] = tv_week7;
		tv_week[7] = tv_week8;
		tv_week[8] = tv_week9;
		tv_week[9] = tv_week10;

		tv_date = new TextView[10];
		tv_date[0] = tv_date1;
		tv_date[1] = tv_date2;
		tv_date[2] = tv_date3;
		tv_date[3] = tv_date4;
		tv_date[4] = tv_date5;
		tv_date[5] = tv_date6;
		tv_date[6] = tv_date7;
		tv_date[7] = tv_date8;
		tv_date[8] = tv_date9;
		tv_date[9] = tv_date10;

		currentSelectDate = fullDateFormat.format(days.get(0));

	}

	OnClickListener dateOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			for (int i = 0; i < 10; i++) {
				ll_date[i].setBackgroundResource(R.drawable.shape_date);
				tv_week[i].setTextColor(getResources().getColor(R.color.black));
				tv_date[i].setTextColor(getResources().getColor(
						R.color.darkGrey));
			}
			v.setBackgroundResource(R.drawable.shape_date_selected);
			switch (v.getId()) {
			case R.id.ll_mainpage_date1:
				tv_week[0].setTextColor(getResources().getColor(R.color.white));
				tv_date[0].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(0));
				break;
			case R.id.ll_mainpage_date2:
				tv_week[1].setTextColor(getResources().getColor(R.color.white));
				tv_date[1].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(1));
				break;
			case R.id.ll_mainpage_date3:
				tv_week[2].setTextColor(getResources().getColor(R.color.white));
				tv_date[2].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(2));
				break;
			case R.id.ll_mainpage_date4:
				tv_week[3].setTextColor(getResources().getColor(R.color.white));
				tv_date[3].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(3));
				break;
			case R.id.ll_mainpage_date5:
				tv_week[4].setTextColor(getResources().getColor(R.color.white));
				tv_date[4].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(4));
				break;
			case R.id.ll_mainpage_date6:
				tv_week[5].setTextColor(getResources().getColor(R.color.white));
				tv_date[5].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(5));
				break;
			case R.id.ll_mainpage_date7:
				tv_week[6].setTextColor(getResources().getColor(R.color.white));
				tv_date[6].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(6));
				break;

			case R.id.ll_mainpage_date8:
				tv_week[7].setTextColor(getResources().getColor(R.color.white));
				tv_date[7].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(7));
				break;

			case R.id.ll_mainpage_date9:
				tv_week[8].setTextColor(getResources().getColor(R.color.white));
				tv_date[8].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(8));
				break;

			case R.id.ll_mainpage_date10:
				tv_week[9].setTextColor(getResources().getColor(R.color.white));
				tv_date[9].setTextColor(getResources().getColor(R.color.white));
				currentSelectDate = fullDateFormat.format(days.get(9));
				break;
			}
			progressDialog.show();
			ll_selection.removeAllViews();
			ll_selection.setVisibility(View.GONE);
			ll_instruction.setVisibility(View.VISIBLE);
			countIsSelected = 0;
			loadTime = 0;
			new Thread(r_GetCourtInfo).start();
		}
	};

	/**
	 * 根据日期获得所在10天内的日期
	 * 
	 * @param mdate
	 * @return
	 */
	public static List<Date> dateToWeek(Date mdate) {
		Date fdate;
		List<Date> list = new ArrayList<Date>();
		Long fTime = mdate.getTime();
		for (int a = 0; a <= 9; a++) {
			fdate = new Date();
			fdate.setTime(fTime + (a * 24 * 3600000));
			list.add(a, fdate);
		}
		return list;
	}

	@SuppressWarnings("deprecation")
	private void makeItems() {

		ll_table.setAdapter(new TableAdapter());

	}

	class MyOnClickListener implements OnClickListener {

		int i, j;

		MyOnClickListener(int i, int j) {
			this.i = i;
			this.j = j;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (courtIsSelected[i][j]) {
				if (isBooked[i][j] == 2)
					v.setBackgroundResource(R.color.blue);
				else if (isBooked[i][j] == 1)
					v.setBackgroundResource(R.color.darkGrey);
				courtIsSelected[i][j] = false;
				countIsSelected--;

				/*
				 * if (countIsSelected == 0) {
				 * ll_instruction.setVisibility(View.VISIBLE);
				 * ll_selection.setVisibility(View.GONE); }
				 */
				// bookitem反选
				for (int k = 0; k < ll_selection.getChildCount(); k++) {
					View view = ll_selection.getChildAt(k);
					TextView tv_describe = (TextView) view
							.findViewById(R.id.tv_bookitem_describe);
					String[] temp = tv_describe.getText().toString().split("-");
					if (temp[0].equals(i + "") && temp[1].equals(j + "")) {
						ll_selection.removeView(view);
						break;
					}
				}
			} else {
				v.setBackgroundResource(R.color.yellow);
				courtIsSelected[i][j] = true;
				countIsSelected++;
				// ll_instruction.setVisibility(View.GONE);
				// ll_selection.setVisibility(View.VISIBLE);

				LayoutInflater mLi = LayoutInflater.from(MainActivity.this);
				View view = mLi.inflate(R.layout.layout_bookitem, null);
				TextView tv_time = (TextView) view
						.findViewById(R.id.tv_bookitem_time);
				TextView tv_courtname = (TextView) view
						.findViewById(R.id.tv_bookitem_courtname);
				TextView tv_describe = (TextView) view
						.findViewById(R.id.tv_bookitem_describe);

				tv_time.setText(minuteToClock(j * 30) + "-"
						+ minuteToClock((j + weight) * 30));
				tv_courtname.setText(courtName[i]);
				// 用于标识，方便反选
				tv_describe.setText(i + "-" + j);

				LinearLayout.LayoutParams p = new LayoutParams(
						(int) (screenWidth * 0.23), LayoutParams.MATCH_PARENT);
				p.setMargins((int) (screenWidth * 0.016), 0, 0, 0);

				ll_selection.addView(view, p);
			}

		}

	}

	Runnable r_GetCourtInfo = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				// 查询场地的数量、名称、ID，并初始化各个状态参数
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Request",
						"RequestListByGymID"));
				params.add(new BasicNameValuePair("GymID", gymID));

				String result = new HttpPostConnection("GymInfoServer", params)
						.httpConnection();

				if (result.equals("timeout")) {
					Message msg = new Message();
					Bundle b = new Bundle();
					msg.setData(b);
					timeoutHandler.handleMessage(msg);
					return;
				}

				System.out.println(result);

				JSONObject json_courtinfo = new JSONObject(result);
				courtNum = (json_courtinfo.length() - 3) / 2;
				courtIsSelected = new boolean[courtNum][48];
				courtID = new String[courtNum];
				courtName = new String[courtNum];
				courtPrice = new String[courtNum][48];
				isBooked = new int[courtNum][48];

				startTime = json_courtinfo.getInt("StartTime");
				endTime = json_courtinfo.getInt("EndTime");
				weight = json_courtinfo.getInt("Weight");

				for (int i = 0; i < courtNum; i++) {
					for (int j = 0; j < 48; j++) {
						isBooked[i][j] = 2;
						courtIsSelected[i][j] = false;
					}
				}

				for (int i = 0; i < courtNum; i++) {
					courtID[i] = json_courtinfo.getInt("Court" + i) + "";
					courtName[i] = json_courtinfo.getString("Name" + i) + "";
				}

				// 启动线程获取场地占用情况、价格
				for (int i = 0; i < courtNum; i++) {
					pool.execute(new TimeThread(i));
					pool.execute(new PriceThread(i));
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	class TimeThread extends Thread {

		int i;

		TimeThread(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// 查询场地的占用情况
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "RequestTimeByCourtID"));
			params.add(new BasicNameValuePair("CourtID", courtID[i]));
			params.add(new BasicNameValuePair("Date", currentSelectDate));

			String result = new HttpPostConnection("GymInfoServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				Message msg = new Message();
				Bundle b = new Bundle();
				msg.setData(b);
				timeoutHandler.handleMessage(msg);
				return;
			}

			// 时间过了场地自动变为不可预订
			Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			String day = c.get(Calendar.DATE) + "";
			if (Integer.parseInt(day) < 10)
				day = "0" + day;
			String[] str = currentSelectDate.split("-");
			if (str[2].equals(day)) {

				for (int j = 0; j < 48; j++) {
					if ((hour * 2 + 1) > j)
						isBooked[i][j] = 0;
				}
			}

			// 根据服务器传回的数据设定是否可预定
			try {
				JSONObject json = new JSONObject(result);

				for (int j = 0; j < 48; j++) {

					if (json.has("Time" + j)) {
						isBooked[i][j] = json.getInt("Time" + j);
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			lock.lock();
			loadTime++;
			if (loadTime >= courtNum * 2) {
				Message msg = new Message();
				Bundle b = new Bundle();
				msg.setData(b);
				courtHandler.sendMessage(msg);
			}
			lock.unlock();

		}
	}

	class PriceThread extends Thread {

		int i;

		PriceThread(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// 查询court的价格
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request",
					"RequestPriceByCourtID"));
			params.add(new BasicNameValuePair("CourtID", courtID[i]));
			params.add(new BasicNameValuePair("Date", currentSelectDate));

			String result = new HttpPostConnection("GymInfoServer", params)
					.httpConnection();

			if (result.equals("timeout")) {
				Message msg = new Message();
				Bundle b = new Bundle();
				msg.setData(b);
				timeoutHandler.handleMessage(msg);
				return;
			}

			try {
				JSONObject json = new JSONObject(result);

				for (int j = 0; j < 48; j++) {
					if (json.has(j + ""))
						courtPrice[i][j] = json.getInt(j + "") + "";
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			lock.lock();
			loadTime++;
			if (loadTime >= courtNum * 2) {
				Message msg = new Message();
				Bundle b = new Bundle();
				msg.setData(b);
				courtHandler.sendMessage(msg);
			}
			lock.unlock();

		}
	}

	class ChangeReservationThread extends Thread {

		int Time;
		String CourtID;
		String CourtName;
		int ChangeCode;

		ChangeReservationThread(int Time, String CourtID, String CourtName,
				int ChangeCode) {
			this.Time = Time;
			this.CourtID = CourtID;
			this.CourtName = CourtName;
			this.ChangeCode = ChangeCode;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "ChangeReservation"));
			params.add(new BasicNameValuePair("CourtID", CourtID));
			params.add(new BasicNameValuePair("Time", Time + ""));
			params.add(new BasicNameValuePair("Date", currentSelectDate));
			params.add(new BasicNameValuePair("Phone", Data.PHONE));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));
			params.add(new BasicNameValuePair("ChangeCode", ChangeCode + ""));

			String result = new HttpPostConnection("BusinessServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			b.putInt("Time", Time);
			b.putString("CourtName", CourtName);
			msg.setData(b);
			changeReservationHandler.sendMessage(msg);
		}

	}

	class ChangePriceThread extends Thread {

		int Time;
		String CourtID;
		String CourtName;
		int Price;

		ChangePriceThread(int Time, String CourtID, String CourtName, int Price) {
			this.Time = Time;
			this.CourtID = CourtID;
			this.CourtName = CourtName;
			this.Price = Price;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Request", "ChangePrice"));
			params.add(new BasicNameValuePair("CourtID", CourtID));
			params.add(new BasicNameValuePair("Time", Time + ""));
			params.add(new BasicNameValuePair("Price", Price + ""));
			params.add(new BasicNameValuePair("Phone", Data.PHONE));
			params.add(new BasicNameValuePair("Passcode", Data.PASSCODE));

			String result = new HttpPostConnection("BusinessServer", params)
					.httpConnection();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putString("result", result);
			b.putInt("Time", Time);
			b.putString("CourtName", CourtName);
			msg.setData(b);
			changePriceHandler.sendMessage(msg);
		}

	}

	class CourtHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			makeItems();

			progressDialog.dismiss();

		}
	}

	@SuppressLint("HandlerLeak")
	class ChangeReservationHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			try {

				countOprate++;

				Bundle b = msg.getData();

				String result = b.getString("result");

				System.out.println(result);

				JSONObject json = new JSONObject(result);
				if (json.has("Verification")) {

					SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(MainActivity.this);
					Editor pEdit = sp.edit();
					pEdit.putBoolean("isLogin", false);
					pEdit.commit();

					Toast.makeText(MainActivity.this, "身份过期，请重新登录",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(MainActivity.this,
							LoginActivity.class);
					startActivity(intent);
					finish();
					return;
				}

				if (!json.getString("ChangeReservation").equals("AddSuccess")
						&& !json.getString("ChangeReservation").equals(
								"DeleteSuccess")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							MainActivity.this);
					builder.setMessage(b.getString("CourtName") + " "
							+ minuteToClock(b.getInt("Time") * 30) + "-"
							+ minuteToClock((b.getInt("Time") + weight) * 30)
							+ " 修改失败 失败原因："
							+ json.getString("ChangeReservation"));
					builder.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
								}
							});
					builder.show();
				}

				if (countOprate >= countIsSelected) {

					progressDialog.dismiss();

					Toast.makeText(MainActivity.this, "操作完成",
							Toast.LENGTH_SHORT).show();

					progressDialog.show();

					ll_selection.removeAllViews();
					ll_selection.setVisibility(View.GONE);
					ll_instruction.setVisibility(View.VISIBLE);
					countIsSelected = 0;
					new Thread(r_GetCourtInfo).start();

					countOprate = 0;
				}

			} catch (Exception e) {
				e.printStackTrace();
				progressDialog.dismiss();

				Toast.makeText(MainActivity.this, "操作未全部完成", Toast.LENGTH_SHORT)
						.show();

				progressDialog.show();

				ll_selection.removeAllViews();
				ll_selection.setVisibility(View.GONE);
				ll_instruction.setVisibility(View.VISIBLE);
				countIsSelected = 0;
				new Thread(r_GetCourtInfo).start();

				countOprate = 0;
			}

		}
	}

	@SuppressLint("HandlerLeak")
	class ChangePriceHandler extends Handler {
		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			try {

				countOprate++;

				Bundle b = msg.getData();

				String result = b.getString("result");

				System.out.println(result);

				JSONObject json = new JSONObject(result);
				if (json.has("Verification")) {

					SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(MainActivity.this);
					Editor pEdit = sp.edit();
					pEdit.putBoolean("isLogin", false);
					pEdit.commit();

					Toast.makeText(MainActivity.this, "身份过期，请重新登录",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(MainActivity.this,
							LoginActivity.class);
					startActivity(intent);
					finish();
				}

				if (!json.getString("ChangePrice").equals("成功,刷新查询")) {
					final AlertDialog alertDialog = new AlertDialog.Builder(
							MainActivity.this).create();
					alertDialog.setMessage(b.getString("CourtName") + " "
							+ b.getInt("Time") + ":00-"
							+ (b.getInt("Time") + 1) + ":00 修改失败 失败原因："
							+ json.getString("ChangePrice"));
					alertDialog.setButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									alertDialog.dismiss();
								}
							});
					alertDialog.show();
				}

				if (countOprate >= countIsSelected) {

					progressDialog.dismiss();

					Toast.makeText(MainActivity.this, "操作完成",
							Toast.LENGTH_SHORT).show();

					progressDialog.show();

					ll_selection.removeAllViews();
					ll_selection.setVisibility(View.GONE);
					ll_instruction.setVisibility(View.VISIBLE);
					countIsSelected = 0;
					new Thread(r_GetCourtInfo).start();

					countOprate = 0;
				}

			} catch (Exception e) {
				e.printStackTrace();
				progressDialog.dismiss();

				Toast.makeText(MainActivity.this, "操作未全部完成", Toast.LENGTH_SHORT)
						.show();

				progressDialog.show();

				ll_selection.removeAllViews();
				ll_selection.setVisibility(View.GONE);
				ll_instruction.setVisibility(View.VISIBLE);
				countIsSelected = 0;
				new Thread(r_GetCourtInfo).start();

				countOprate = 0;
			}

		}
	}

	@SuppressLint("HandlerLeak")
	class TimeoutHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			Toast.makeText(MainActivity.this, "网络不畅通，请检查网络设置",
					Toast.LENGTH_SHORT).show();

			progressDialog.dismiss();
		}
	}

	class TableAdapter extends BaseTableAdapter {

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return (endTime - startTime) / weight;
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return courtName.length;
		}

		@Override
		public View getView(int row, int column, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			try {
				// if (convertView == null) {
				convertView = new LinearLayout(MainActivity.this);
				((LinearLayout) convertView)
						.setOrientation(LinearLayout.VERTICAL);
				// }

				if (column == -1) {
					if (row != -1) {
						TextView tv = new TextView(MainActivity.this);
						tv.setText(minuteToClock((startTime + row * weight) * 30)
								+ "ˉ");
						tv.setTextColor(MainActivity.this.getResources()
								.getColor(R.color.black));
						tv.setBackgroundResource(R.color.white);
						tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
						// tv.setGravity(Gravity.END);

						LinearLayout.LayoutParams p = new LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.MATCH_PARENT);
						p.gravity = Gravity.END;
						// p.setMargins(0, -(int) (Data.SCREENHEIGHT * 0.005),
						// 0, 0);
						((LinearLayout) convertView).addView(tv, p);
					}
				} else if (row == -1) {
					if (column != -1) {

						TextView tv = new TextView(MainActivity.this);

						tv.setText(courtName[column]);
						tv.setTextColor(MainActivity.this.getResources()
								.getColor(R.color.black));
						tv.setBackgroundResource(R.color.white);
						tv.setGravity(Gravity.CENTER);
						tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

						LinearLayout.LayoutParams p = new LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT);
						((LinearLayout) convertView).addView(tv, p);
					}
				} else {
					TextView tv = new TextView(MainActivity.this);

					if (isBooked[column][startTime + row * weight] == 2) {

						tv.setText("￥"
								+ courtPrice[column][startTime + row * weight]);
						tv.setTextColor(MainActivity.this.getResources()
								.getColor(R.color.white));
						if (courtIsSelected[column][startTime + row * weight]) {
							tv.setBackgroundResource(R.color.yellow);
						} else {
							tv.setBackgroundResource(R.color.blue);
						}
						tv.setGravity(Gravity.CENTER);
						tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

						tv.setOnClickListener(new MyOnClickListener(column,
								startTime + row * weight));

					} else if (isBooked[column][startTime + row * weight] == 0) {
						tv.setBackgroundResource(R.color.grey);
					} else {
						tv.setBackgroundResource(R.color.darkGrey);

						tv.setText("￥"
								+ courtPrice[column][startTime + row * weight]);
						tv.setTextColor(MainActivity.this.getResources()
								.getColor(R.color.white));
						if (courtIsSelected[column][startTime + row * weight]) {
							tv.setBackgroundResource(R.color.yellow);
						} else {
							tv.setBackgroundResource(R.color.darkGrey);
						}
						tv.setGravity(Gravity.CENTER);
						tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

						tv.setOnClickListener(new MyOnClickListener(column,
								startTime + row * weight));
					}

					LinearLayout.LayoutParams p = new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT);
					p.setMargins((int) (screenHeight * 0.003),
							(int) (screenHeight * 0.006),
							(int) (screenHeight * 0.003), 0);
					((LinearLayout) convertView).addView(tv, p);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return convertView;
		}

		@Override
		public int getWidth(int column) {
			// TODO Auto-generated method stub
			if (column == -1) {
				return (int) (screenWidth * 0.12);
			} else {
				return (int) (screenWidth * 0.2);
			}
		}

		@Override
		public int getHeight(int row) {
			// TODO Auto-generated method stub
			if (row == -1) {
				return (int) (screenHeight * 0.05);
			} else {
				return (int) (screenHeight * 0.0625);
			}
		}

		@Override
		public int getItemViewType(int row, int column) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return 1;
		}

	}

	public static String minuteToClock(int sumMinute) {
		int hour = sumMinute / 60;
		int minute = sumMinute - hour * 60;
		if (minute == 0) {
			return hour + ":00";
		} else {
			return hour + ":30";
		}
	}

}
