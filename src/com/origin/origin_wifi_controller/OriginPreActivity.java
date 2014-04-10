package com.origin.origin_wifi_controller;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.origin.origin_wifi_controller.R;
import com.origin.Log.MLOG;
import com.origin.matrixEnet.OriginEnet;
import com.origin.matrixEnet.OriginEnetUSB;
 

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
 
import android.provider.Settings;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.ActivityInfo;
import android.graphics.Color; 
  
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText; 
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class OriginPreActivity extends Activity implements OriginEventListener{
	
	private SelectDialog waringdialog;
	private static boolean opendialog = false;
	
	
	private EditText ipEditText;//ip编辑框
	private EditText portEditText;//端口编辑框
	private CheckedTextView pointCheck;//坐标值传输确认选择框
	private CheckedTextView gyroscopeCheck;//陀螺仪数据传输确认选择框
	private CheckedTextView landscapeCheck;//固定横屏
	private CheckedTextView portraitCheck;//固定竖屏
	private CheckedTextView usb;
	private Button connectBut; //连接按钮
	
	private ImageButton bt1_ip;
	private PopupWindow IPpopview;
	private MatrixAdapter IpDownAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.main);
		
		if(!isWifiConnected() && !opendialog)
			SetDialog();
		
		initResources();
		checkOption();
		MLOG.i("PreActivity onCreate");
	}
	
	@Override
	protected void onResume()
	{
		connectBut.setText("完成并连接");
		connectBut.setTextColor(Color.parseColor("#804747"));
		connectBut.setClickable(true);
		super.onResume();
	}
	
	private void checkOption()
	{
		SharedPreferences preferences = this.getSharedPreferences("user",Context.MODE_PRIVATE); 
		if(preferences.getBoolean("pos", false))
		{
			if(!pointCheck.isChecked())
				pointCheck.toggle();
		}
		if(preferences.getBoolean("gyroscope", false))
		{
			if(!gyroscopeCheck.isChecked())
				gyroscopeCheck.toggle();
		}
		if(preferences.getBoolean("landscape", false))
		{
			if(!landscapeCheck.isChecked())
				landscapeCheck.toggle();
			OriginPreActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		else if(preferences.getBoolean("portrait", false))
		{
			if(!portraitCheck.isChecked())
				portraitCheck.toggle();
			OriginPreActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		if(preferences.getBoolean("usb", false))
		{
			if(!usb.isChecked())
				usb.toggle();
			ipEditText.setEnabled(false);
    		portEditText.setEnabled(false);
    		OriginSocket.setTYPE("USB");
		}
	}
	
	@Override
	protected void onDestroy()
	{
		try {
			SharedPreferences preferences = this.getSharedPreferences("user",Context.MODE_PRIVATE); 
			SharedPreferences.Editor editor = preferences.edit();
			editor.putBoolean("pos", pointCheck.isChecked());
			editor.putBoolean("gyroscope", gyroscopeCheck.isChecked());
			editor.putBoolean("landscape", landscapeCheck.isChecked());
			editor.putBoolean("portrait", portraitCheck.isChecked());
			editor.putBoolean("usb", usb.isChecked());
			editor.commit();
			
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
			OriginRecvMessage.getInstance(this).clean();
			MLOG.i("PreActivity onDestroy");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDestroy();
		System.exit(0);
	}
	
	private void initResources() {
		// TODO Auto-generated method stub
		bt1_ip = (ImageButton)findViewById(R.id.dropdown_button);
		bt1_ip.setOnClickListener(new View.OnClickListener() {                           
            @Override
            public void onClick(View v) 
            {
            	if(null == IPpopview)
            	{
            		initIPPopView();
            		if(null != IPpopview)
            		{
	            		if (!IPpopview.isShowing()) {  
	            			IPpopview.showAsDropDown(ipEditText);  
	            			MLOG.d("显示ip下拉框");
	                    } else {  
	                    	IPpopview.dismiss();  
	                    }
            		}
            	}
            	else
            	{
            		 if (!IPpopview.isShowing()) {  
            			 IPpopview.showAsDropDown(ipEditText);  
                     } else {  
                    	 IPpopview.dismiss();  
                     }
            	}
            }
		}
        );
        //初始化ip编辑框
        ipEditText = (EditText)findViewById(R.id.IPText);
        
        //初始化端口编辑框
        portEditText = (EditText)findViewById(R.id.PortText);

        //初始化按钮
        connectBut = (Button)findViewById(R.id.connect);
        
        connectBut.setOnClickListener(new View.OnClickListener() {                           
            @Override
            public void onClick(View v) 
            {
                     // TODO Auto-generated method stub
            	final String ip = OriginPreActivity.this.ipEditText.getText().toString();
            	final String port = OriginPreActivity.this.portEditText.getText().toString();
            	if(!"USB".equals(OriginSocket.TYPE))
            	{	            	
	            	if(!CheckIP(ip) || ip.length() == 0)
	            	{
		            	String recvMessage = "哥啊,ip格式不对啊,坑爹啊！";
		            	MLOG.w(recvMessage);
		            	OriginRecvMessage.getInstance(OriginPreActivity.this).sendMessage(recvMessage);
						return;
	            	}
	            	if(!isNumeric(port) || port.length() == 0)
	            	{
	            		String recvMessage = "日,端口格式不对！";
	            		MLOG.w(recvMessage);
	            		OriginRecvMessage.getInstance(OriginPreActivity.this).sendMessage(recvMessage);
						return;
	            	}
	            	String recvMessage = "ip:  "+ ip + ":"+ port;
	            	OriginRecvMessage.getInstance(OriginPreActivity.this).sendMessage(recvMessage);
					
	            	OriginDataManager.saveIPAddress(OriginPreActivity.this, ip ,port);
	            	if(null != IpDownAdapter)
	            		IpDownAdapter.updateData(getList());
            	}
            	
				connectBut.setText("连接中别碰我....");
				connectBut.setTextColor(Color.GRAY);
				connectBut.setClickable(false);
				new Thread(){
					@Override
					public void run()
					{
						boolean success = false;
						try {
							if("TCP".equals(OriginSocket.TYPE))
							{
								MLOG.v("Start Socket with TCP");
								OriginSocketTCP.getInstance().setIPAddress(ip);
								OriginSocketTCP.getInstance().setPort(port);
								OriginSocketTCP.getInstance().connect();
								success = true;
							}
							else if("UDP".equals(OriginSocket.TYPE))
							{
								MLOG.v("Start Socket with UDP");
								OriginSocketUDP.getInstance().start(ip, Integer.valueOf(port));
								success = true;
							}
							else if("ENET".equals(OriginSocket.TYPE))
							{
								MLOG.v("Start Socket with ENET");
								success = OriginEnet.getInstance().start(ip, Integer.valueOf(port), 1, 2);
							}
							else if("USB".equals(OriginSocket.TYPE))
							{
								OriginEnetUSB.getInstance().setMatrixVibratorListener(OriginPreActivity.this);
								OriginEnetUSB.getInstance().start();
								success = false;
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							OriginRecvMessage.getInstance(OriginPreActivity.this).sendMessage(e.toString());
							OriginPreActivity.this.runOnUiThread(new Runnable()
							{

								@Override
								public void run() {
									// TODO Auto-generated method stub
									connectBut.setText("连接失败");
									connectBut.setTextColor(Color.parseColor("#804747"));
									connectBut.setClickable(true);
								}
								
							});
							success = false;
						}
						if(success)
						{
							OriginRecvMessage.getInstance(OriginPreActivity.this).sendMessage("连接成功");
							Message msg = new Message();
					        msg.what = 0;
							mHandler.sendMessage(msg);
						}
						else
						{
							if(!"USB".equals(OriginSocket.TYPE))
							{
								OriginPreActivity.this.runOnUiThread(new Runnable()
								{
	
									@Override
									public void run() {
										// TODO Auto-generated method stub
										connectBut.setText("连接失败");
										connectBut.setTextColor(Color.parseColor("#804747"));
										connectBut.setClickable(true);
									}
									
								});
							}
						}
					}
				}.start();
            }
        });
        
        //初始化选择框
        pointCheck = (CheckedTextView)findViewById(R.id.checkeSendPoint);
        pointCheck.setOnClickListener(new View.OnClickListener() {                           
            @Override
            public void onClick(View v) 
            {
                     // TODO Auto-generated method stub
            	pointCheck.toggle();
            	String recvMessage; 
            	if(pointCheck.isChecked())
            		recvMessage= "设置发送触摸坐标数据";
            	else
            		recvMessage= "禁止发送触摸坐标数据";
            	OriginRecvMessage.getInstance(OriginPreActivity.this).sendMessage(recvMessage);
            }
        });
        
        gyroscopeCheck = (CheckedTextView)findViewById(R.id.checkeSendGyroscope);
        gyroscopeCheck.setOnClickListener(new View.OnClickListener() {                           
            @Override
            public void onClick(View v) 
            {
                     // TODO Auto-generated method stub
            	gyroscopeCheck.toggle();
            	String recvMessage; 
            	if(gyroscopeCheck.isChecked())
            		recvMessage= "设置发送陀螺仪数据";
            	else
            		recvMessage= "禁止发送陀螺仪数据";
            	OriginRecvMessage.getInstance(OriginPreActivity.this).sendMessage(recvMessage);
            }
        });

        landscapeCheck = (CheckedTextView)findViewById(R.id.checkelandscape);
        landscapeCheck.setOnClickListener(new View.OnClickListener() {                           
            @Override
            public void onClick(View v) 
            {
                     // TODO Auto-generated method stub
            	landscapeCheck.toggle();
            	if(landscapeCheck.isChecked())
            	{
            		if(portraitCheck.isChecked())
            			portraitCheck.toggle();
            		OriginPreActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            		String recvMessage = "强制横屏";
            		OriginRecvMessage.getInstance(OriginPreActivity.this).sendMessage(recvMessage);
            	}
            }
        });
        
        portraitCheck = (CheckedTextView)findViewById(R.id.checkeportrait);
        portraitCheck.setOnClickListener(new View.OnClickListener() {                           
            @Override
            public void onClick(View v) 
            {
                     // TODO Auto-generated method stub
            	portraitCheck.toggle();
            	if(portraitCheck.isChecked())
            	{
            		if(landscapeCheck.isChecked())
            			landscapeCheck.toggle();
            		OriginPreActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            		String recvMessage = "强制竖屏";
            		OriginRecvMessage.getInstance(OriginPreActivity.this).sendMessage(recvMessage);
            	}
            }
        });
        
        usb = (CheckedTextView)findViewById(R.id.usbconnect);
        usb.setOnClickListener(new View.OnClickListener() {                           
            @Override
            public void onClick(View v) 
            {
                     // TODO Auto-generated method stub
            	usb.toggle();
            	if(usb.isChecked())
            	{
            		ipEditText.setEnabled(false);
            		portEditText.setEnabled(false);
            		OriginSocket.setTYPE("USB");
            		String recvMessage = "启用USB调试连接";
            		OriginRecvMessage.getInstance(OriginPreActivity.this).sendMessage(recvMessage);
            	}
            	else 
            	{
            		ipEditText.setEnabled(true);
            		portEditText.setEnabled(true);
            		OriginSocket.setTYPE(OriginSocket.DEFAULT);
            		String recvMessage = "取消USB调试连接";
            		OriginRecvMessage.getInstance(OriginPreActivity.this).sendMessage(recvMessage);
            	}
            }
        });
	}
	
	Handler mHandler = new Handler()
	{										
		  public void handleMessage(Message msg)										
		  {											
			  super.handleMessage(msg);			
			  if(msg.what == 0)
			  {
				  connectBut.setText("连接成功");
				  connectBut.setTextColor(Color.BLUE);
				  Intent intent = new Intent(OriginPreActivity.this ,OriginMainActivity.class);
				  Bundle bundle = new Bundle();
				  bundle.putBoolean("points", pointCheck.isChecked());
				  bundle.putBoolean("gyroscope", gyroscopeCheck.isChecked());
				  if(landscapeCheck.isChecked())
					  bundle.putInt("orientation", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				  else if(portraitCheck.isChecked())
					  bundle.putInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				  intent.putExtra("attribute", bundle);
				  OriginPreActivity.this.startActivity(intent);
			  }
		  }									
	 };
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		// TODO Auto-generated method stub
		MLOG.i("requestCode  :"+requestCode);
		MLOG.i("resultCode  :"+resultCode);
		if(!isWifiConnected())
			SetDialog();
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		MLOG.i("监听到用户按键");
		if(keyCode== KeyEvent.KEYCODE_BACK)
		{
			MLOG.i("监听到用户按下返回键");
			this.finish();
			return true;
		}
		return false; 
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//是否连接WIFI
    public boolean isWifiConnected()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected())
        {
            return true ;
        }
     
        return false ;
    }

    
	private void openNetOption()
	{
		try{
			Intent mIntent = new Intent("/");
	        ComponentName comp = new ComponentName(
	                "com.android.settings",
	                "com.android.settings.WirelessSettings");
	        mIntent.setComponent(comp);
	        mIntent.setAction("android.intent.action.VIEW");
	        startActivityForResult(mIntent,0);
		}catch(Exception e)
		{
			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
		}
	}
	
	private void SetDialog()
	{
		if(opendialog) return;
		MLOG.i("SetDialog");
		opendialog = true;
		waringdialog = new SelectDialog(this, R.style.selectorDialog);   
		waringdialog.show(); 
		WindowManager.LayoutParams lp=waringdialog.getWindow().getAttributes();
		// 模糊度
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);          
		
		Display display = getWindowManager().getDefaultDisplay();
		lp.width = (int) (display.getWidth()*0.7);
		lp.height = (int) (display.getHeight()*0.4);
		lp.alpha=0.8f;//透明度，黑暗度为lp.dimAmount=1.0f;
		lp.dimAmount=0.2f;
		waringdialog.getWindow().setAttributes(lp);
		waringdialog.setOnDismissListener(new OnDismissListener(){

			@Override
			public void onDismiss(DialogInterface arg0) {
				// TODO Auto-generated method stub
				if(!waringdialog.finish)
				{
					MLOG.i("打开wifi网络");
					OriginPreActivity.opendialog = false;
					openNetOption();
				}
			}
			
		});	
	}
	 
	 //判定ip格式
	 private boolean CheckIP(String ipValue)
	 {
		 Pattern p = Pattern.compile("(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})" +
				 "\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})" +
				 "\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})" +
				 "\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})");
		 Matcher m = p.matcher(ipValue);
		 if(m.matches())
		 {
			 return true;
		 }else
		 {
			 return false;
		 }
	 }
	 
	 //判定端口格式
	 public static boolean isNumeric(String str)
	 { 
		 Pattern pattern = Pattern.compile("[0-9]*");   
		 return pattern.matcher(str).matches();      
	 }
	 
	 
	 
	 //下拉列表
	 
	 private List<HashMap<String, Object>> getList()
	 {
		 List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();  
		 
		 Map<String, ?> map = OriginDataManager.getIPList(this);
		 if(map.size()<=0)
		 {
			 Toast.makeText(this, "无ip记录", Toast.LENGTH_LONG).show();
			 return null;
		 }
		 Iterator<String> keys = map.keySet().iterator();
		 
 		 Object key = null;
 		 Object value =null;
 		 while(keys.hasNext())
 		 {
 			HashMap<String, Object> ipmap = new HashMap<String, Object>();
 			key = keys.next();//key
 			value = map.get(key);//上面key对应的value
 			//MLOG.i("ip: "+(String)value);
 			if(key == null && value == null)
 				break;
 			ipmap.put("name", (String)key);  
 			ipmap.put("port", (String)value);  
	        list.add(ipmap);
 		 }
 		 
 		 return list;
	 }
	 
	 private void initIPPopView() 
	 { 
		 List<HashMap<String, Object>> list = getList();
		 if(null == list)
			 return;
 		  
	     IpDownAdapter = new MatrixAdapter(this, list, R.layout.dropdown_item,  
	             new String[] { "name", "port" }, new int[] { R.id.textview,  
	                     R.id.delete } ,"ip");  
	     ListView listView = new ListView(this);  
	     listView.setAdapter(IpDownAdapter);  
	     IPpopview = new PopupWindow(listView, ipEditText.getWidth(),  
	                ViewGroup.LayoutParams.WRAP_CONTENT, true);  
	     IPpopview.setFocusable(true);  
	     IPpopview.setOutsideTouchable(true);  
	     IPpopview.setBackgroundDrawable(getResources().getDrawable(R.drawable.popview));
	    } 
	 
	 class MatrixAdapter extends SimpleAdapter{
			private List<HashMap<String, Object>> data;  
			private String type;
		    public MatrixAdapter(Context context, List<HashMap<String, Object>> data,int resource, String[] from, int[] to ,String type) 
		    {  
		        super(context, data, resource, from, to);  
		        this.data = data;  
		        this.type = type;
		    }  
		    
		    public void updateData(List<HashMap<String, Object>> data)
		    {
		    	if(null != data)
			    {
			    	this.data = data;
			    	this.notifyDataSetChanged();
		    	}
		    }
		    
		    @Override  
		    public int getCount() {  
		        return data.size();  
		    }  

		    @Override  
		    public Object getItem(int position) {  
		        return position;  
		    }  

		    @Override  
		    public long getItemId(int position) {  
		        return position;  
		    }  

		    @Override  
		    public View getView(final int position, View convertView, ViewGroup parent) 
		    {   
		        final ViewHolder holder;  
		        if (convertView == null) {  
		            holder = new ViewHolder();  
		            convertView = LayoutInflater.from(OriginPreActivity.this).inflate(  
		                    R.layout.dropdown_item, null);  
		            holder.btn = (ImageButton) convertView  
		                    .findViewById(R.id.delete);  
		            holder.tv = (TextView) convertView.findViewById(R.id.textview);  
		            convertView.setTag(holder);  
		        } else {  
		            holder = (ViewHolder) convertView.getTag();  
		        }  
		        holder.tv.setText(data.get(position).get("name").toString());  
		        holder.tv.setOnClickListener(new View.OnClickListener() {  

		            @Override  
		            public void onClick(View v) {  
		                if(type.equals("ip"))
		                {
		                	ipEditText.setText(holder.tv.getText());
		                	portEditText.setText(data.get(position).get("port").toString());
		                	IPpopview.dismiss();  
		                }	                
		            }  
		        });  
		        holder.btn.setOnClickListener(new View.OnClickListener() {  

		            @Override  
		            public void onClick(View v) { 
		            	if(type.equals("ip"))
		            	{
		            		OriginDataManager.deleteIPAddress(OriginPreActivity.this, holder.tv.getText().toString()); 
		            		if (OriginDataManager.getIPList(OriginPreActivity.this).size() > 0) {  
		            			IPpopview.dismiss();
			                	initIPPopView();  
			                	IPpopview.showAsDropDown(ipEditText);  
			                } else {  
			                	IPpopview.dismiss();
			                	IPpopview = null;  
			                } 
		            	}
		                 
		            }  
		        });  
		        return convertView;  
		    }  
		}
	 
	  class ViewHolder 
	  {  
	       private TextView tv;  
	       private ImageButton btn;  
	  }

	@Override
	public void EventListener(final int time) {
		// TODO Auto-generated method stub
		this.runOnUiThread(new Runnable()
		{

			@Override
			public void run() {
				if(2 == time)
				{
					OriginRecvMessage.getInstance(OriginPreActivity.this).sendMessage("连接成功");
					Message msg = new Message();
			        msg.what = 0;
					mHandler.sendMessage(msg);
				}
			}
		});

	}  
}
