package com.pazdarke.courtpocket4gym.receiver;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.pazdarke.courtpocket4gym.R;
import com.pazdarke.courtpocket4gym.activity.MainActivity;
import com.pazdarke.courtpocket4gym.activity.WelcomeActivity;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

public class PushReceiver extends XGPushBaseReceiver {

	static int id = 0;

	@Override
	public void onDeleteTagResult(Context arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		System.out.println("onDeleteTagResult");
	}

	@Override
	public void onNotifactionClickedResult(Context arg0,
			XGPushClickedResult arg1) {
		// TODO Auto-generated method stub
		System.out.println("onNotifactionClickedResult");
	}

	@Override
	public void onNotifactionShowedResult(Context arg0, XGPushShowedResult arg1) {
		// TODO Auto-generated method stub
		System.out.println("onNotifactionShowedResult");
	}

	@Override
	public void onRegisterResult(Context arg0, int arg1,
			XGPushRegisterResult arg2) {
		// TODO Auto-generated method stub
		System.out.println("onRegisterResult");
	}

	@Override
	public void onSetTagResult(Context arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		System.out.println("onSetTagResult");
	}

	@Override
	public void onTextMessage(Context context, XGPushTextMessage msg) {
		// TODO Auto-generated method stub
		try {
			System.out.println("onTextMessage");

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			Boolean isNotificationOpen = prefs.getBoolean("isNotificationOpen",
					true);

			if (isNotificationOpen) {
				NotificationManager manager = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationCompat.Builder mBuilder;
				Intent intent;
				PendingIntent pendingIntent;
				Notification notification;

				JSONObject json = new JSONObject(msg.getContent());

				int code = json.getInt("Code");

				switch (code) {
				case 0:
					mBuilder = new NotificationCompat.Builder(context)
							.setSmallIcon(R.drawable.icon)
							.setContentText(json.getString("Message"))
							.setContentTitle("场兜").setTicker("您有一条通知消息")
							.setWhen(System.currentTimeMillis())
							.setAutoCancel(true);

					intent = new Intent(context, WelcomeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					pendingIntent = PendingIntent.getActivity(context, 0,
							intent, PendingIntent.FLAG_UPDATE_CURRENT);
					mBuilder.setContentIntent(pendingIntent);

					notification = mBuilder.build();
					notification.defaults = Notification.DEFAULT_SOUND;
					manager.notify(id, notification);
					id++;
					break;
				case 1:
					mBuilder = new NotificationCompat.Builder(context)
							.setSmallIcon(R.drawable.icon)
							.setContentText(
									json.getString("CourtName")
											+ " "
											+ json.getString("Date")
											+ " "
											+ MainActivity.minuteToClock(json
													.getInt("Time") * 30)
											+ " 未付款")
							.setContentTitle(json.getString("GymName"))
							.setTicker("您有一条占用场地消息")
							.setWhen(System.currentTimeMillis())
							.setAutoCancel(true);

					intent = new Intent(context, WelcomeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					pendingIntent = PendingIntent.getActivity(context, 0,
							intent, PendingIntent.FLAG_UPDATE_CURRENT);
					mBuilder.setContentIntent(pendingIntent);

					notification = mBuilder.build();
					notification.defaults = Notification.DEFAULT_SOUND;
					manager.notify(id, notification);
					id++;
					break;
				case 2:
					mBuilder = new NotificationCompat.Builder(context)
							.setSmallIcon(R.drawable.icon)
							.setContentText(
									"已付款 "
											+ json.getString("CourtName")
											+ " "
											+ json.getString("Date")
											+ " "
											+ MainActivity.minuteToClock(json
													.getInt("Time") * 30))
							.setContentTitle(json.getString("GymName"))
							.setTicker("您有一条付款成功消息")
							.setWhen(System.currentTimeMillis())
							.setAutoCancel(true);

					intent = new Intent(context, WelcomeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					pendingIntent = PendingIntent.getActivity(context, 0,
							intent, PendingIntent.FLAG_UPDATE_CURRENT);
					mBuilder.setContentIntent(pendingIntent);

					notification = mBuilder.build();
					notification.defaults = Notification.DEFAULT_SOUND;
					manager.notify(id, notification);
					id++;
					break;
				case 3:
					mBuilder = new NotificationCompat.Builder(context)
							.setSmallIcon(R.drawable.icon)
							.setContentText(
									"付款超时，占用解除 "
											+ json.getString("CourtName")
											+ " "
											+ json.getString("Date")
											+ " "
											+ MainActivity.minuteToClock(json
													.getInt("Time") * 30))
							.setContentTitle(json.getString("GymName"))
							.setTicker("您有一条付款超时消息")
							.setWhen(System.currentTimeMillis())
							.setAutoCancel(true);

					intent = new Intent(context, WelcomeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					pendingIntent = PendingIntent.getActivity(context, 0,
							intent, PendingIntent.FLAG_UPDATE_CURRENT);
					mBuilder.setContentIntent(pendingIntent);

					notification = mBuilder.build();
					notification.defaults = Notification.DEFAULT_SOUND;
					manager.notify(id, notification);
					id++;
					break;
				case 4:
					mBuilder = new NotificationCompat.Builder(context)
							.setSmallIcon(R.drawable.icon)
							.setContentText(
									"售出一张 " + json.getString("CardName"))
							.setContentTitle(json.getString("GymName"))
							.setTicker("您有一条购卡消息")
							.setWhen(System.currentTimeMillis())
							.setAutoCancel(true);

					intent = new Intent(context, WelcomeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					pendingIntent = PendingIntent.getActivity(context, 0,
							intent, PendingIntent.FLAG_UPDATE_CURRENT);
					mBuilder.setContentIntent(pendingIntent);

					notification = mBuilder.build();
					notification.defaults = Notification.DEFAULT_SOUND;
					manager.notify(id, notification);
					id++;
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onUnregisterResult(Context arg0, int arg1) {
		// TODO Auto-generated method stub
		System.out.println("onUnregisterResult");
	}

}
