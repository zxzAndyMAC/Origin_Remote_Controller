package com.origin.origin_wifi_controller;
 
 
import com.origin.origin_wifi_controller.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
 
import android.view.KeyEvent;
import android.view.MotionEvent;

public class SelectDialog extends AlertDialog{
	
	public boolean finish = false;
	
	public SelectDialog(Context context, int theme) {
	    super(context, theme);
	}

	public SelectDialog(Context context) {
	    super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.dialog);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		switch (ev.getAction())
		{
		case MotionEvent.ACTION_UP:
			this.dismiss();
		}
		return true;	
	}

	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if(keyCode== KeyEvent.KEYCODE_BACK)
		{
			finish = true;
			this.dismiss();
			return true;
		}
		return false; 
	}
}
