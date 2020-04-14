package com.bharatpetroleum.syncmanager;

import org.json.JSONObject;

public interface SyncEventListner {

	public static int ERROR_CODE_INTERNET_CONNECTION = 1300;
	public static int ERROR_CODE_CONNECTION_FAILED = 1301;
	public static int ERROR_HTTP_NOT_FOUND = 404;
	public static int ERROR_HTTP_BAD_REQUEST = 400;
	public static int ERROR_HTTP_FORBIDDEN = 403;
	public static int ERROR_HTTP_SERVER_ERROR = 500;

	public void onSyncStart();

	public void onSyncFinish();

	public void onSyncFailure(int code);

	public void onSyncForbidden(int code, String string);

	public void onSyncSuccess(String code,  boolean success,
                              JSONObject jsonObject);

	public void onSyncProgress(int progress, int length);

}
