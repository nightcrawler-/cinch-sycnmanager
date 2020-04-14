package com.bharatpetroleum.syncmanager;

import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bharatpetroleum.syncmanager.BuildConfig;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class SyncManager {

	private static final String PREFS_NAME = "http";

	private static SyncManager instance = null;

	protected Context mContext;

	private AsyncHttpClient client = null;

	public String token = null;

	public boolean bLoggedin = false;

	private String sBaseUrl;

	public static SyncManager getInstance(Context mAct) {
		if (instance == null)
			instance = new SyncManager();
		instance.setAct(mAct);
		return instance;
	}

	public void setBaseUrl(String url) {
		this.sBaseUrl = url;
	}

	public void setAct(Context mAct) {
		this.mContext = mAct;
	}

	protected SyncManager() {
	}

	private String addAuthCodeHeader(String url) {

		token = getLoginStatus();
		log("token =" + token);

		if (token != null) {
			getClient().addHeader("token", token);
			if (!url.contains("?"))
				url = url + "?token=" + token;
			else
				url = url + "&token=" + token;
		}
		else {
			getClient().addHeader("token", token);
		}
		return url;
	}

	public AsyncHttpClient getClient() {
		if (client == null) {
			client = new AsyncHttpClient();
		}
		return client;
	}

	public String getUrl(String url) {
		return getUrl(url, true);
	}

	public String getUrl(String url, boolean addAuthCode) {

		if (url.contains("http://") || url.contains("https://")) {

		}
		else if (url.startsWith("/")) {
			url = sBaseUrl + url.substring(1);
		}
		else {
			url = sBaseUrl + url;
		}

		if (addAuthCode) {
			url = addAuthCodeHeader(url);
		}

		log("url  = " + url);
		return url;
	}

	/*
	 * public boolean check(SyncEventListner syncHandler) { String controller = "api/user/check"; String url =
	 * getUrl(controller); if (isNetworkAvailable()) { getClient().get(url, new MyHanlder(syncHandler)); } return true;
	 * }
	 */

	class MyHanlder extends JsonHttpResponseHandler {

		private SyncEventListner myHandler;

		public MyHanlder(SyncEventListner syncEventListner) {
			this.myHandler = syncEventListner;
		}

		@Override
		public void onFailure(Throwable e, JSONArray errorResponse) {
			myHandler.onSyncFailure(SyncEventListner.ERROR_CODE_INTERNET_CONNECTION);
		}

		@Override
		public void onFailure(Throwable e, JSONObject errorResponse) {
			myHandler.onSyncFailure(SyncEventListner.ERROR_CODE_INTERNET_CONNECTION);
		}

		@Override
		public void onFailure(Throwable arg0, String arg1) {

			if (arg0 instanceof HttpResponseException) {
				HttpResponseException httpError = (HttpResponseException) arg0;
				if (httpError.getStatusCode() == 403)
					myHandler.onSyncForbidden(httpError.getStatusCode(), "Login");
				else
					myHandler.onSyncFailure(httpError.getStatusCode());
			}
			myHandler.onSyncFailure(SyncEventListner.ERROR_HTTP_SERVER_ERROR);
		}

		@Override
		public void onFailure(Throwable error) {
			myHandler.onSyncFailure(SyncEventListner.ERROR_CODE_INTERNET_CONNECTION);
		}

		@Override
		public void onStart() {
			myHandler.onSyncStart();
			super.onStart();
		}

		@Override
		public void onFinish() {
			myHandler.onSyncFinish();
			super.onFinish();
		}

		@Override
		public void onSuccess(String arg0) {

			super.onSuccess(arg0);
			if (BuildConfig.DEBUG) {
				Toast.makeText(mContext, arg0.toString(), Toast.LENGTH_LONG).show();
			}

			log(arg0);
		}

		@Override
		public void onSuccess(JSONArray response) {

			super.onSuccess(response);
			if (BuildConfig.DEBUG) {
				Toast.makeText(mContext, response.toString(), Toast.LENGTH_LONG).show();
			}
			log(response.toString());
		}

		@Override
		public void onSuccess(JSONObject response) {

			log(response.toString());

			try {
				myHandler.onSyncSuccess(response.getString("code"), response.getBoolean("success"), response);
			}
			catch (JSONException e1) {

				e1.printStackTrace();
				myHandler.onSyncFailure(SyncEventListner.ERROR_HTTP_SERVER_ERROR);
			}
		}

		@Override
		public void onProgress(int position, int length) {
			myHandler.onSyncProgress(position, length);
		}

	}

	public boolean sendToServer(String url, RequestParams params, SyncEventListner syncHandler) {

		String completeUrl = getUrl(url);

		if (isNetworkAvailable()) {

			log("sendToServer = " + completeUrl + " params =" + params);
			getClient().post(completeUrl, params, new MyHanlder(syncHandler));
			return true;
		}
		return false;
	}

	public boolean getFromServer(String url, RequestParams params, SyncEventListner syncHandler) {

		String completeUrl = getUrl(url);

		if (isNetworkAvailable()) {

			log("sendToServer = " + completeUrl + " params =" + params);
			getClient().get(completeUrl, params, new MyHanlder(syncHandler));
			return true;
		}
		return false;
	}

	public String getLoginStatus() {
		// Restore preferences
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
		String loginValid = settings.getString("token", null);
		log("PrefStore" + "loginValid " + loginValid);
		return loginValid;
	}

	public void setLoginStatus(String loginValid) {
		SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("token", loginValid);
		// Commit the edits!
		editor.commit();

		bLoggedin = loginValid != null ? true : false;
		log("PrefStore" + "loginValid " + loginValid);
	}

	public boolean isNetworkAvailable() {

		ConnectivityManager connectivity = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null && info.getState() == NetworkInfo.State.CONNECTED) {
				return true;
			}
		}
		return false;
	}



	private void log(String string) {
		Log.e("http", string);

	}
}
