package com.origin.Abstract;

import java.util.Queue;

import com.origin.origin_wifi_controller.OriginEventListener;

public abstract class OriginSocketAbstract {
	public OriginEventListener vl;
	public Queue<String> SocketQueue;
	
	public abstract void sendMSG(String msg);
	public abstract void sendMSG();
	public abstract void ReceiveMsg();
	public abstract void close();
	
	public void setMatrixVibratorListener(OriginEventListener vl)
	{
		this.vl = vl;
	}

}
