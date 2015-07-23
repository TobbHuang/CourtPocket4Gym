package com.pazdarke.courtpocket4gym.data;

import java.util.ArrayList;

public class Data {
	public static String URL = "http://120.25.123.42/CourtPocket/";

	public static ArrayList<String> GYMID;
	public static ArrayList<String> GYMNAME;
	public static String PASSCODE;
	public static String PHONE;

	// double小数点优化
	public static String doubleTrans(double num) {
		if (num % 1.0 == 0) {
			return String.valueOf((long) num);
		}
		return String.valueOf(num);
	}

}
