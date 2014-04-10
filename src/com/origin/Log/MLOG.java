package com.origin.Log;

import android.util.Log;

public class MLOG {
	private static final String TAG = "Matrix";
	private static final String _TAG = "===============";
	
	public static void d(String s)
	{
		Log.d(TAG,_TAG+s);
	}
	
	public static void i(String s)
	{
		Log.i(TAG, _TAG+s);
	}
	
	public static void e(String s)
	{
		Log.e(TAG, _TAG+s);
	}
	
	public static void w(String s)
	{
		Log.w(TAG, _TAG+s);
	}
	
	public static void v(String s)
	{
		Log.v(TAG, _TAG+s);
	}
}
