package com.lkl.ansuote.demo.googlemapdemo.base.util;

import android.text.TextUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * 网络请求工具类
 * 
 * @author Administrator
 * 
 */

public class HttpUtils {
	final static int TIME = 15000;// 网络超时时间

	// 判断请求的图片URL
	public static String checkPictureUrl(String url) {
		String flag = "http://www.qingfanqie.com";//正式
		if (TextUtils.isEmpty(url)) {
			return "";
		} else if (url.startsWith("http")) {
			return url;
		} else if (url.startsWith("/")) {
			return flag + url;
		} else {
			return flag + "/" + url;
		}
	}

	// get请求
	public static String doGet(String url) {
        String result = null;
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        ConnManagerParams.setTimeout(client.getParams(), TIME);
        HttpConnectionParams.setConnectionTimeout(client.getParams(), TIME);
        HttpConnectionParams.setSoTimeout(client.getParams(), TIME);
        try {
            // 执行请求，并返回HttpResponse对象
            HttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // 取得返回结果
                result = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
            }
        }catch(ConnectTimeoutException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            client.getConnectionManager().shutdown();
        }
        return result;
	}

	// post请求
	public static String doPost(String url, HashMap<String, String> params) {

		List<NameValuePair> nameValuePairs = new ArrayList<>();
		for (Entry<String, String> entry : params.entrySet()) {
			nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry
					.getValue()));
		}
		String result = null;
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
			HttpResponse httpResponse = client.execute(post);
			// HttpStatus.SC_OK用这个无效 ，直接用200就有效
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			System.gc();
			e.printStackTrace();
		} finally {
			client.getConnectionManager().shutdown();
		}
		return result;
	}


}
