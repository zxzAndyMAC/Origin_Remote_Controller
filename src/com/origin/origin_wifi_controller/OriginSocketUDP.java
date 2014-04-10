package com.origin.origin_wifi_controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.LinkedList; 

import com.origin.Abstract.OriginSocketAbstract;
import com.origin.Log.MLOG;

public class OriginSocketUDP extends OriginSocketAbstract{
	private static OriginSocketUDP martixSocketUDP;
	
	private static final int MAX_DATA_PACKET_LENGTH = 1024;  
    private byte[] buffer = new byte[MAX_DATA_PACKET_LENGTH];

	private DatagramSocket udpSocket;
	private DatagramPacket rev_package ,send_package;
	
	private static boolean isConnectting; 	
	
	public static OriginSocketUDP getInstance()
	{
		if(null == martixSocketUDP)
			martixSocketUDP = new OriginSocketUDP();
		return martixSocketUDP;
	}
	
	public void start(String ip ,int port) throws Exception
	{
		if(null == SocketQueue)
			SocketQueue = new LinkedList<String>();
		else
		{
			SocketQueue.clear();
			SocketQueue = null;
			SocketQueue = new LinkedList<String>();
		}
		//InetAddress serverAddr = InetAddress.getByName(ip);
		udpSocket = new DatagramSocket(port); 
		InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
		udpSocket.connect(socketAddress);
		
		send_package = new DatagramPacket(buffer, MAX_DATA_PACKET_LENGTH);
		rev_package = new DatagramPacket(buffer, MAX_DATA_PACKET_LENGTH);
	
		//send_package.setAddress(serverAddr);
		
		isConnectting = true;
		sendMSG();
		ReceiveMsg();
	}
	
	@Override
	public void sendMSG(String msg)
	{
		SocketQueue.offer(msg);
	}
	
	@Override
	public void sendMSG()
	{
		new Thread(){
			@Override
			public void run()
			{
				while(isConnectting)
				{
					String msg;  
			        while((msg=SocketQueue.poll())!=null){  
			        	MLOG.i(msg);
			        	byte out[] = msg.getBytes();  
			        	send_package.setData(out);  
			        	send_package.setLength(out.length);  
			        	try {
							udpSocket.send(send_package);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							MLOG.w("send error :"+e.toString());
						}
			        }
				}
			}
		}.start();
	}
	
	@Override
	public void ReceiveMsg()
	{   
		new Thread(){
			@Override
			public void run()
			{
				byte data[] = new byte[1024];
				String msg="";
				while(isConnectting)
				{
					try {
						//简单震动 1000
						rev_package.setData(data);
						rev_package.setLength(data.length);
						udpSocket.receive(rev_package);
						msg = new String(rev_package.getData() , rev_package.getOffset() , rev_package.getLength());
						MLOG.i("来自服务端的消息："+msg);
						if(vl !=null && msg.length()>0)
							vl.EventListener(Integer.valueOf(msg));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						MLOG.w(e.toString());
					}
				}
			}
		}.start();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		try
		{
			udpSocket.close();
			SocketQueue.clear();
			SocketQueue = null;
			rev_package = null;
			send_package = null;
			martixSocketUDP = null;
			isConnectting = false;
		}catch(Exception e)
		{
			MLOG.w(e.toString());
		}
	}
}
