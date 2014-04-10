package com.origin.origin_wifi_controller;
 
import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import com.origin.origin_wifi_controller.R;
import com.origin.Log.MLOG;
import com.origin.matrixEnet.OriginEnet;
import com.origin.matrixEnet.OriginEnetUSB;
 

import android.app.Activity;
import android.content.Context;
import android.content.Intent; 
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;

public class OriginMainActivity extends Activity implements SensorEventListener ,OriginEventListener{
	
	private final static float Sensor_offset = 0.5f;
	
	private final String ACTION_POINTER_DOWN = "ACTION_POINTER_DOWN";
	private final String ACTION_DOWN = "ACTION_DOWN";
	private final String ACTION_MOVE = "ACTION_MOVE";
	private final String ACTION_POINTER_UP = "ACTION_POINTER_UP";
	private final String ACTION_UP = "ACTION_UP";
	private final String ACTION_CANCEL = "ACTION_CANCEL";
	private final String ACTION_SENSOR = "Sensor";
	
	private static boolean sendCoordinate;
	private static boolean sendGyroscope;
	
	private DecimalFormat fnum = new DecimalFormat("##0.00");  
	
	private static int SCREEN_WIDTH ,SCREEN_HEIGHT;
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private int mNaturalOrientation;
	
	private float sensor_x=0 ,sensor_y=0 ,sensor_z = 0;
	
	private Vibrator vibrator;
	
	private OriginView matrixview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.main2);
		
		int ore = setAttribute();
		
		SCREEN_HEIGHT = this.getResources().getDisplayMetrics().heightPixels;
		SCREEN_WIDTH = this.getResources().getDisplayMetrics().widthPixels;
		
		createMsg("Pixels" ,SCREEN_WIDTH ,SCREEN_HEIGHT ,ore);
		
		if(sendGyroscope)
		{
			this.mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
			this.mAccelerometer = this.mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

			final Display display = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			this.mNaturalOrientation = display.getOrientation();
			//enable();
			setInterval(60.0f);
		}
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE); 
		if("TCP".equals(OriginSocket.TYPE))
		{
			OriginSocketTCP.getInstance().setMatrixVibratorListener(this);
		}
		else if("UDP".equals(OriginSocket.TYPE))
		{
			OriginSocketUDP.getInstance().setMatrixVibratorListener(this);			
		}
		else if("ENET".equals(OriginSocket.TYPE))
		{
			OriginEnet.getInstance().setMatrixVibratorListener(this);
		}
		else if("USB".equals(OriginSocket.TYPE))
		{
			OriginEnetUSB.getInstance().setMatrixVibratorListener(this);
		}
		
		matrixview = (OriginView)findViewById(R.id.view);
		
		MLOG.i("MainActivity onCreate");
	}
	
	@Override
	protected void onDestroy()
	{
		try {
			if("TCP".equals(OriginSocket.TYPE))
			{
				OriginSocketTCP.getInstance().close();
			}
			else if("UDP".equals(OriginSocket.TYPE))
			{
				OriginSocketUDP.getInstance().close();
			}
			else if("ENET".equals(OriginSocket.TYPE))
			{
				OriginEnet.getInstance().close();
			}
			else if("USB".equals(OriginSocket.TYPE))
			{
				OriginEnetUSB.getInstance().close();
			}
			MLOG.i("MainActivity onDestroy");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(sendGyroscope)
			disable();
		super.onDestroy();
	}
	
	public void enable() 
	{
		this.mSensorManager.registerListener(this, this.mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
	}

    public void setInterval(float interval) 
    {
	        // Honeycomb version is 11
	    if(android.os.Build.VERSION.SDK_INT < 11) {
		    this.mSensorManager.registerListener(this, this.mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		} else {
		    //convert seconds to microseconds
		    this.mSensorManager.registerListener(this, this.mAccelerometer, (int)(interval*100000));
		}
	}
      
	public void disable() 
	{
		this.mSensorManager.unregisterListener(this);
	}
	
	private int setAttribute()
	{
		try{
			Intent intent = this.getIntent();
			Bundle bundle = intent.getBundleExtra("attribute");
			sendCoordinate = bundle.getBoolean("points");
			sendGyroscope = bundle.getBoolean("gyroscope");
			int orientation = bundle.getInt("orientation", -1);
			if(orientation != -1)
				this.setRequestedOrientation(orientation);
			return orientation;
		}catch(Exception e)
		{
			MLOG.w(e.toString());
		}
		return 0;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent pMotionEvent) 
	{
		// these data are used in ACTION_MOVE and ACTION_CANCEL
		
		final int pointerNumber = pMotionEvent.getPointerCount();
		final int[] ids = new int[pointerNumber];
		final float[] xs = new float[pointerNumber];
		final float[] ys = new float[pointerNumber];
		int action_up = -1;
		for (int i = 0; i < pointerNumber; i++) {
			ids[i] = pMotionEvent.getPointerId(i);
			xs[i] = pMotionEvent.getX(i);
			ys[i] = pMotionEvent.getY(i);
		}

		switch (pMotionEvent.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_POINTER_DOWN:
				final int indexPointerDown = pMotionEvent.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
				final int idPointerDown = pMotionEvent.getPointerId(indexPointerDown);
				final float xPointerDown = pMotionEvent.getX(indexPointerDown);
				final float yPointerDown = pMotionEvent.getY(indexPointerDown);
				//MLOG.i("action: "+ ACTION_POINTER_DOWN +"  id: "+idPointerDown+"  x: "+xPointerDown+"  y: "+yPointerDown);
				createMsg(ACTION_POINTER_DOWN ,idPointerDown ,xPointerDown ,yPointerDown);
				break;

			case MotionEvent.ACTION_DOWN:
				// there are only one finger on the screen
				final int idDown = pMotionEvent.getPointerId(0);
				final float xDown = xs[0];
				final float yDown = ys[0];
				//MLOG.i("action: "+ ACTION_DOWN +"  id: "+idDown+"  x: "+xDown+"  y: "+yDown);
				createMsg(ACTION_DOWN ,idDown ,xDown ,yDown); 
				break;

			case MotionEvent.ACTION_MOVE:
				createMsg(ACTION_MOVE ,ids, xs, ys);
				break;

			case MotionEvent.ACTION_POINTER_UP:
				final int indexPointUp = pMotionEvent.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
				final int idPointerUp = pMotionEvent.getPointerId(indexPointUp);
				final float xPointerUp = pMotionEvent.getX(indexPointUp);
				final float yPointerUp = pMotionEvent.getY(indexPointUp);
				action_up = idPointerUp;
				//MLOG.i("action: "+ ACTION_POINTER_UP +"  id: "+idPointerUp+"  x: "+xPointerUp+"  y: "+yPointerUp);
				createMsg(ACTION_POINTER_UP ,idPointerUp ,xPointerUp ,yPointerUp); 
				break;

			case MotionEvent.ACTION_UP:
				// there are only one finger on the screen
				final int idUp = pMotionEvent.getPointerId(0);
				final float xUp = xs[0];
				final float yUp = ys[0];
				action_up = idUp;
				//MLOG.i("action: "+ ACTION_UP +"  id: "+idUp+"  x: "+xUp+"  y: "+yUp);
				createMsg(ACTION_UP ,idUp ,xUp ,yUp); 
				break;

			case MotionEvent.ACTION_CANCEL:
				createMsg(ACTION_CANCEL ,ids, xs, ys);
				break;
		}
		matrixview.setPointer(pointerNumber, xs, ys ,ids ,action_up);
		matrixview.invalidate();
		return true;	
	}
	
	private void createMsg(String action, int[] ids, float[] xs,
			float[] ys) {
		try{
			// TODO Auto-generated method stub
			JSONObject total = new JSONObject(); 
			JSONArray array = new JSONArray();
			JSONObject a;
			total.put("action", action);
			for(int i=0 ; i<ids.length ; i++)
			{
				a = new JSONObject();
				a.put("id", ids[i]);
				a.put("x", Float.valueOf(fnum.format(xs[i])));
				a.put("y", Float.valueOf(fnum.format(ys[i])));
				array.put(a);
			}
			total.put("pm", array);
			sendMSG(total.toString());
			//MLOG.i(total.toString());
		}catch(Exception e)
		{
			MLOG.w(e.toString());
		}
	}

	private void createMsg(String action ,int id ,float x ,float y)
	{
		try{
			JSONObject total = new JSONObject(); 
			total.put("action", action);
			total.put("id", id);
			total.put("x", Float.valueOf(fnum.format(x)));
			total.put("y", Float.valueOf(fnum.format(y)));
			sendMSG(total.toString());
			//MLOG.i(total.toString());
		}catch(Exception e)
		{
			MLOG.w(e.toString());
		}
	}
	
	
	private void createMsg(String action, int sCREEN_WIDTH2, int sCREEN_HEIGHT2 ,int ore) {
		// TODO Auto-generated method stub
		try{
			JSONObject total = new JSONObject(); 
			total.put("action", action);
			total.put("SCREEN_WIDTH", sCREEN_WIDTH2);
			total.put("SCREEN_HEIGHT", sCREEN_HEIGHT2);
			total.put("ore", ore);
			sendMSG(total.toString());
			//MLOG.i(total.toString());
		}catch(Exception e)
		{
			MLOG.w(e.toString());
		}
	}
	
	private void sendMSG(String msg) throws Exception
	{
		if(sendCoordinate)
		{
			if("TCP".equals(OriginSocket.TYPE))
			{
				OriginSocketTCP.getInstance().sendMSG(msg);
			}
			else if("UDP".equals(OriginSocket.TYPE))
			{
				OriginSocketUDP.getInstance().sendMSG(msg);
			}
			else if("ENET".equals(OriginSocket.TYPE))
			{
				OriginEnet.getInstance().sendMSG(msg);
			}
			else if("USB".equals(OriginSocket.TYPE))
			{
				OriginEnetUSB.getInstance().sendMSG(msg);
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		MLOG.i("精度发生变换");
	}

	@Override
	public void onSensorChanged(SensorEvent pSensorEvent) {
		// TODO Auto-generated method stub
		if (pSensorEvent.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
			return;
		}

		float x = pSensorEvent.values[0];
		float y = pSensorEvent.values[1];
		final float z = pSensorEvent.values[2];
		
		float off_x = Math.abs(Math.abs(x) - Math.abs(sensor_x));
		float off_y = Math.abs(Math.abs(y) - Math.abs(sensor_y));
		float off_z = Math.abs(Math.abs(z) - Math.abs(sensor_z));
		
		if(off_x<Sensor_offset && off_y<Sensor_offset && off_z<Sensor_offset)
			return;
		sensor_x = x;
		sensor_y = y;
		sensor_z = z;
		/*
		 * Because the axes are not swapped when the device's screen orientation
		 * changes. So we should swap it here. In tablets such as Motorola Xoom,
		 * the default orientation is landscape, so should consider this.
		 */
		final int orientation = this.getResources().getConfiguration().orientation;

		if ((orientation == Configuration.ORIENTATION_LANDSCAPE) && (this.mNaturalOrientation != Surface.ROTATION_0)) {
			final float tmp = x;
			x = -y;
			y = tmp;
		} else if ((orientation == Configuration.ORIENTATION_PORTRAIT) && (this.mNaturalOrientation != Surface.ROTATION_0)) {
			final float tmp = x;
			x = y;
			y = -tmp;
		}
		
		createMsg(x,y,z,pSensorEvent.timestamp);
	}

	private void createMsg(float x, float y, float z, long timestamp) {
		// TODO Auto-generated method stub
		try{
			JSONObject total = new JSONObject(); 
			total.put("action", ACTION_SENSOR);
			total.put("x", Float.valueOf(fnum.format(x)));
			total.put("y", Float.valueOf(fnum.format(y)));
			total.put("z", Float.valueOf(fnum.format(z)));
			total.put("t", timestamp);
			sendMSG(total.toString());
			//MLOG.i(total.toString());
		}catch(Exception e)
		{
			MLOG.w(e.toString());
		}
	}
	
	@Override
	public void EventListener(int time)
	{
		vibrator.vibrate(time);
	}
}
