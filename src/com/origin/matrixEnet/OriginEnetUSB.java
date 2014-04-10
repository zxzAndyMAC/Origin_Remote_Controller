package com.origin.matrixEnet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList; 
  

import com.origin.Abstract.OriginSocketAbstract;
import com.origin.Log.MLOG;

public class OriginEnetUSB extends OriginSocketAbstract{

	private static OriginEnetUSB matrixenetUSB;
	 
	private ServerSocket serverSocket = null;
	
	private static boolean isConnectting;
	
	BufferedOutputStream out;
	BufferedInputStream in;
	
	public static OriginEnetUSB getInstance()
	{
		if(null == matrixenetUSB)
			matrixenetUSB = new OriginEnetUSB();
		return matrixenetUSB;
	}
	
	public boolean start()
	{
		if(null == SocketQueue)
			SocketQueue = new LinkedList<String>();
		else
		{
			SocketQueue.clear();
			SocketQueue = null;
			SocketQueue = new LinkedList<String>();
		}
		try {
			serverSocket = new ServerSocket(10086,1);
			new Thread(){
				@Override
				public void run()
				{
					Socket client = null;
					try {
						MLOG.i("开始监听连接");
						client = serverSocket.accept();
						
						out = new BufferedOutputStream(client.getOutputStream());
						in = new BufferedInputStream(client.getInputStream());
						
						MLOG.i("新客户端连接");
						isConnectting = true;
						if(vl !=null)
							vl.EventListener(2);
						
						sendMSG();
						ReceiveMsg();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						MLOG.i(e.toString());
					}					
				}
			}.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			MLOG.i(e.toString());
			return false;
		}
		return true;
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
					try{
					String msg;  
				        while((msg=SocketQueue.poll())!=null){  
				        	//MLOG.d("send message");
				        	out.write(msg.getBytes());
							out.flush();
				        }
					}catch(Exception e)
					{
						MLOG.w("sendMSG  "+e.toString());
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
				int MAX_BUFFER_BYTES = 2048;
				String msg = "";
				while(true)
				{
					try {
						msg = "";
						byte[] tempbuffer = new byte[MAX_BUFFER_BYTES];
						try {
							int numReadedBytes = in.read(tempbuffer, 0, tempbuffer.length);
							msg = new String(tempbuffer, 0, numReadedBytes, "utf-8");
							tempbuffer = null;
						} catch (Exception e) {
							e.printStackTrace();
						}
						MLOG.i("来自服务端的消息："+msg);
						if("exit".equals(msg))
						{
							close();
						}
						else
						{
							if(vl !=null && msg.length()>0)
								vl.EventListener(Integer.valueOf(msg));
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						MLOG.w("ReceiveMsg  "+e.toString());
					}
				}
			}
		}.start();
	}
	
	@Override
	public void close() {
		// TODO Auto-generated method stub
		new Thread(){
			@Override
			public void run()
			{
				try
				{
					if(null != serverSocket)
						serverSocket.close();
					isConnectting = false;
					SocketQueue.clear();
					SocketQueue = null;
				}catch(Exception e)
				{
					MLOG.w(e.toString());
				}
			}
		}.start();
	}
	
	private static native boolean nativeEnetInitialize();   											//初始化enet库
	private static native boolean nativeEnetSend(final String msg ,final int channel);         			//发送数据
	private static native boolean nativeEnetClose();        											//关闭连接
	private static native String  nativeEnetRecive();       											//收数据
}
