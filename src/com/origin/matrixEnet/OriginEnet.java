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
			MLOG.i("��ʼ��enet��ɹ�");
			if(nativeEnetHostCreate(peercount ,channelLimit))
			{
				MLOG.i("��������host�ɹ�");
				if(nativeEnetConnectServer(ip ,port))
				{
					MLOG.i("���ӷ������ɹ�");
					isConnectting = true;
					sendMSG();
					ReceiveMsg();
					return true;
				}
				else
				{
					MLOG.w("���ӷ�����ʧ��");
					return false;
				}
			}
			else
			{
				MLOG.w("��������hostʧ��");
				return false;
			}
		}
		else
		{
			MLOG.w("��ʼ��enet��ʧ��");
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
							MLOG.i("���Է���˵���Ϣ��"+msg);
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
	
	private static native boolean nativeEnetInitialize();   											//��ʼ��enet��
	private static native boolean nativeEnetHostCreate(final int peercount, final int channelLimit);    //��������host
	private static native boolean nativeEnetConnectServer(final String ip, final int port);				//���ӷ�����
	private static native boolean nativeEnetSend(final String msg ,final int channel);         			//��������
	private static native boolean nativeEnetClose();        											//�ر�����
	private static native String  nativeEnetRecive();       											//������
}
