package com.origin.origin_wifi_controller;

public class OriginSocket {
	public static String TYPE = "ENET";//TCP ,UDP ,ENET ,USB
	
	public final static String DEFAULT = "ENET";
	
	public static void setTYPE(String type)
	{
		OriginSocket.TYPE = type;
	}
}
