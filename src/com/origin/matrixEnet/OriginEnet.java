package com.origin.matrixEnet;
  
import java.util.LinkedList; 
import java.util.Random;

import com.origin.Abstract.OriginSocketAbstract;
import com.origin.Log.MLOG;

public class OriginEnet extends OriginSocketAbstract{
	private static OriginEnet matrixenet;
	 
	
	private static boolean isConnectting;
	private static int channelLimit;
	
	public static OriginEnet getInstance()
	{
		if(null == matrixenet)
			matrixenet = new OriginEnet();
		return matrixenet;
	}
	
	public boolean start(final String ip ,final int port ,final int peercount ,final int channelLimit)
	{
		OriginEnet.channelLimit = channelLimit;
		if(null == SocketQueue)
			SocketQueue = new LinkedList<String>();
		else
		{
			SocketQueue.clear();
			SocketQueue = null;
			SocketQueue = new LinkedList<String>();
		}
		if(nativeEnetInitialize())
		{
			MLOG.i("初始化enet库成功");
			if(nativeEnetHostCreate(peercount ,channelLimit))
			{
				MLOG.i("创建本地host成功");
				if(nativeEnetConnectServer(ip ,port))
				{
					MLOG.i("连接服务器成功");
					isConnectting = true;
					sendMSG();
					ReceiveMsg();
					return true;
				}
				else
				{
					MLOG.w("连接服务器失败");
					return false;
				}
			}
			else
			{
				MLOG.w("创建本地host失败");
				return false;
			}
		}
		else
		{
			MLOG.w("初始化enet库失败");
			return false;
		}
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
				        	//MLOG.i(msg);  
				        	Random r = new Random();
	    					int i = r.nextInt(2);
							nativeEnetSend(msg,i);
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
				String msg="";
				while(isConnectting)
				{
					try {
						msg = nativeEnetRecive();
						if(msg.length()>0)
						{
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
						}
						msg = null;
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
					nativeEnetClose();  
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
	private static native boolean nativeEnetHostCreate(final int peercount, final int channelLimit);    //创建本地host
	private static native boolean nativeEnetConnectServer(final String ip, final int port);				//连接服务器
	private static native boolean nativeEnetSend(final String msg ,final int channel);         			//发送数据
	private static native boolean nativeEnetClose();        											//关闭连接
	private static native String  nativeEnetRecive();       											//收数据
}
