package com.origin.origin_wifi_controller;

import com.origin.origin_wifi_controller.R;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
 
import android.widget.TextView;
import android.app.Activity;

public class OriginRecvMessage {
	
	private static OriginRecvMessage matrixrecvMessage;
	private TextView recvText;//输出log
	private String recvMessage;
	
	OriginRecvMessage(Context context)
	{
		//初始化recvText
		recvText= (TextView)((Activity)context).findViewById(R.id.outputinfo);       
		recvText.setMovementMethod(ScrollingMovementMethod.getInstance());
	}
	
	public void clean()
	{
		recvText = null;
		matrixrecvMessage = null;
	}
	
	public static OriginRecvMessage getInstance(Context context)
	{
		if(null == matrixrecvMessage)
			matrixrecvMessage = new OriginRecvMessage(context);
		return matrixrecvMessage;
	}
	
	public void sendMessage(String message)
	{
		recvMessage = message;
		Message msg = new Message();
        msg.what = 0;
		mHandler.sendMessage(msg);
	}
	
	Handler mHandler = new Handler()
	{										
		  public void handleMessage(Message msg)										
		  {											
			  super.handleMessage(msg);			
			  if(msg.what == 0)
			  {
				  recvText.append("Matrix_Log:   "+recvMessage+"\n");	// 刷新
				  recvText.getScrollY();
				  if(recvText.getLineCount()>7)
				  {
					  int y = (int) ((recvText.getLineCount()-7)*recvText.getLineHeight());
					  int offset_y = recvText.getScrollY();
					  recvText.scrollBy(0, y - offset_y);
				  }
			  }
		  }									
	 };
}
