package com.origin.origin_wifi_controller;
  
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList; 

import com.origin.Abstract.OriginSocketAbstract;
import com.origin.Log.MLOG;

public class OriginSocketTCP extends OriginSocketAbstract{
	private static OriginSocketTCP socket;
	
	private String ip;
	private int port;
	private Socket clientSocket;
	private PrintWriter mPrintWriterClient; 
	private BufferedReader mBufferedReader;
	  
	
	private static boolean isConnectting;
	
	public static OriginSocketTCP getInstance()
	{
		if(null == socket)
			socket = new OriginSocketTCP();
		return socket;
	}
	
	public void setIPAddress(String ip)
	{
		this.ip = ip;
	}
	
	public void setPort(String port)
	{
		this.port = Integer.valueOf(port);
	}
	
	public void connect() throws Exception
	{
		if(null == SocketQueue)
			SocketQueue = new LinkedList<String>();
		else
		{
			SocketQueue.clear();
			SocketQueue = null;
			SocketQueue = new LinkedList<String>();
		}
		
		InetAddress serverAddr = InetAddress.getByName(ip);
		int p = Integer.valueOf(port);
		clientSocket = new Socket(serverAddr ,p);
		clientSocket.setSoTimeout(30*1000);
		mBufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		mPrintWriterClient = new PrintWriter(clientSocket.getOutputStream(), true);
		
		isConnectting = true;
		sendMSG();
		ReceiveMsg();
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
					try{
						String msg;  
				        while((msg=SocketQueue.poll())!=null){  
				        	mPrintWriterClient.print(msg);//发送给服务器
				        	mPrintWriterClient.flush();
				        }
					}catch(Exception e)
					{
						MLOG.w(e.toString());
					}
				}
			}
		}.start();
	}
	
	@Override
	public void sendMSG(String msg)
	{
		SocketQueue.offer(msg);
	}
	
	@Override
	public void ReceiveMsg()
	{   
		new Thread(){
			@Override
			public void run()
			{
				char[] buffer = new char[1024];
				int count = 0;
				while(isConnectting)
				{
					try {
						if((count = mBufferedReader.read(buffer))>0)
						{
							//简单震动 1000
							String msg = getInfoBuff(buffer, count);
							MLOG.i("来自服务端的消息："+msg);
							if(vl !=null)
								vl.EventListener(Integer.valueOf(msg));
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						MLOG.w(e.toString());
					}
				}
			}
		}.start();
	}
	
	private String getInfoBuff(char[] buff, int count)
	{
		char[] temp = new char[count];
		for(int i=0; i<count; i++)
		{
			temp[i] = buff[i];
		}
		return new String(temp);
	}
	
	@Override
	public void close()
	{
		if(null != mPrintWriterClient)
		{
			mPrintWriterClient.close();
			mPrintWriterClient = null;
		}
		try{
			if(null != clientSocket)
			{
				clientSocket.close();
				clientSocket = null;
			}
			if(null != mBufferedReader)
			{
				mBufferedReader.close();
				mBufferedReader = null;
			}
		}catch(Exception e)
		{
			MLOG.w(e.toString());
		}
		SocketQueue.clear();
		SocketQueue = null;
		socket = null;
		isConnectting = false;
	}
}
