package com.origin.origin_wifi_controller;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class OriginDataManager {
	public static void saveIPAddress(Context context ,String ip ,String port)
	{
		SharedPreferences preferences = context.getSharedPreferences("ip",Context.MODE_PRIVATE); 
		SharedPreferences.Editor editor = preferences.edit();

		editor.putString(ip, port);
		editor.commit();
	}
	
	public static void deleteIPAddress(Context context ,String ip)
	{
		SharedPreferences preferences = context.getSharedPreferences("ip",Context.MODE_PRIVATE); 
		SharedPreferences.Editor editor = preferences.edit();

		editor.remove(ip);
		editor.commit();
	}
	
	public static Map<String, ?> getIPList(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences("ip",Context.MODE_PRIVATE); 
		return preferences.getAll();
	}
}
