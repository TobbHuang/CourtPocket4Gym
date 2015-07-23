package com.pazdarke.courtpocket4gym.httpConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.pazdarke.courtpocket4gym.data.Data;

public class HttpGetPicConnection {

	public Bitmap httpConnection(String url) {
		Bitmap bitmap = null;
		try {
			// ��鱾���Ƿ���ͼƬ�Ĵ洢
			File dir = new File(Environment.getExternalStorageDirectory()
					+ "/CourtPocket");

			if (!dir.exists()) {
				dir.mkdir();
			}

			String imgFilePath = Environment.getExternalStorageDirectory()
					+ "/CourtPocket/" + url.replace("/", "-");
			File file = new File(imgFilePath);
			if (file.exists()) {
				Bitmap bm = BitmapFactory.decodeFile(imgFilePath);

				return bm;
			}

			HttpClient httpClient = new DefaultHttpClient();
			httpClient.getParams().setIntParameter(
					CoreConnectionPNames.SO_TIMEOUT, 10000); // ��ʱ����
			httpClient.getParams().setIntParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);// ���ӳ�ʱ

			HttpGet httpGet = new HttpGet(Data.URL + url);

			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// ȡ�������Ϣ ȡ��HttpEntiy
				HttpEntity httpEntity = httpResponse.getEntity();
				// ���һ��������
				InputStream is = httpEntity.getContent();
				bitmap = BitmapFactory.decodeStream(is);
				is.close();

				if (bitmap != null) {
					FileOutputStream out = new FileOutputStream(file);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.flush();
					out.close();
				}

			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;

	}

}
