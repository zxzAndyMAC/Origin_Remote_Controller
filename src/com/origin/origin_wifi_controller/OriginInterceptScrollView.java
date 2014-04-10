package com.origin.origin_wifi_controller;

import android.content.Context;
import android.util.AttributeSet;
 
import android.view.MotionEvent;
import android.widget.ScrollView;

public class OriginInterceptScrollView extends ScrollView
{

	public OriginInterceptScrollView(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}

	public OriginInterceptScrollView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public OriginInterceptScrollView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		return super.onInterceptTouchEvent(ev);
	}
}
