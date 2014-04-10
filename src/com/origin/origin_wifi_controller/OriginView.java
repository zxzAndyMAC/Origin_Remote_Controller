package com.origin.origin_wifi_controller;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class OriginView extends View{
	
	private Paint paint = new Paint();
	private Paint txtPaint = new Paint();
	
	private int radius = 70;
	private float x[] ,y[];
	private int id[];
	private int pointerNumber=0;
	private int ac_up = -1;
	
	private final int[] color = {Color.GREEN ,Color.BLUE ,Color.CYAN ,Color.BLACK ,Color.RED
								,Color.MAGENTA ,Color.YELLOW ,Color.parseColor("#DA7F3C")
								,Color.parseColor("#52504F") ,Color.parseColor("#1A9D98")
								,Color.parseColor("#9D201A")};
	
	public OriginView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		radius = this.dipToPixels(radius);
	}
	
	public void setPointer(int pointerNumber ,float[] x ,float[] y, int[] id ,int action_up)
	{
		this.pointerNumber = pointerNumber;
		this.x = x;
		this.y = y;
		this.id = id;
		this.ac_up = action_up;
		initPaint();
	}
	
	private void initPaint()
	{
		paint.setAntiAlias(true);                       //设置画笔为无锯齿        
        paint.setStrokeWidth((float) 6.0);              //线宽  
        paint.setStyle(Style.STROKE);                   //空心效果
        
        txtPaint.setAntiAlias(true);
        txtPaint.setStyle(Style.FILL);
        txtPaint.setTextAlign(Align.CENTER);
        txtPaint.setTextSize(this.dipToPixels(20));
	}
	
    @Override
    protected void onDraw(Canvas canvas)
    {
    	super.onDraw(canvas);
    	if(pointerNumber<=0)
    		return;
        
        for(int i = 0; i < pointerNumber; i++)
        {
        	if(ac_up != id[i])
        	{
        		paint.setColor(color[i]);                    //设置画笔颜色  
        		txtPaint.setColor(color[i]);
        		canvas.drawCircle(x[i], y[i], radius, paint);
        		canvas.drawText(String.valueOf(i+1), x[i], y[i]-radius-10, txtPaint);
        	}
        }
    }
    
	private int dipToPixels(int dip) {
		Resources r = getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
		return (int) px;
	}
}
