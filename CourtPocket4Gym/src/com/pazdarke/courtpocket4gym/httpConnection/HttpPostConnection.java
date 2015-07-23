package com.pazdarke.courtpocket4gym.httpConnection;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import com.pazdarke.courtpocket4gym.data.Data;

public class HttpPostConnection {
	String url;
	List<NameValuePair> params;

	public HttpPostConnection(String url, List<NameValuePair> params) {
		this.url = url;
		this.params = params;
	}

	public String httpConnection() {

		String result = null;
		try {
			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setIntParameter(
					CoreConnectionPNames.SO_TIMEOUT, 15000); // ��ʱ����
			httpClient.getParams().setIntParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);// ���ӳ�ʱ

			HttpPost httpPost = new HttpPost(Data.URL + url);

			httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

			HttpResponse httpResponse = httpClient.execute(httpPost);
			result = EntityUtils.toString(httpResponse.getEntity());

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "timeout";
		} catch (Exception e) {
			System.out.println("Exception!!!");
			e.printStackTrace();
		}

		return result;

	}

}
