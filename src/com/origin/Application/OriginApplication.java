package com.origin.Application;

import com.origin.Log.MLOG;

import android.app.Application;

public class OriginApplication extends Application{
	  static 
	  {
		  System.loadLibrary("Enet");
		  MLOG.i("≥…π¶º”‘ÿEnetø‚");
	  }
}
