package com.avoscloud.beijing.push.demo.keepalive;

import android.app.Application;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;

/**
 * Created by nsun on 4/28/14.
 */
public class HTBApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // 必需：初始化你的appid和appkey，保存installationid
    AVOSCloud.initialize(this, "19y77w6qkz7k5h1wifou7lwnrxf9i3g4qdpxb4k1yeuvjgp7", "gyxj747shi4j6ryedriq68k2jlqoftqfjpqxrzmqo8zmkjf6");
    AVOSCloud.showInternalDebugLog();
    AVInstallation.getCurrentInstallation().saveInBackground();

  }
}
