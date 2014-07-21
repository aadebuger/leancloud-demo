package com.avoscloud.beijing.push.demo.keepalive;

/**
 * Created by nsun on 5/6/14.
 */
public interface Constants {

  public static final String ACTION_BIND = "com.avoscloud.beijing.push.demo.keepalive.ACTION_BIND";

  public static final String START_SESSION = "START_SESSION";
  public static final int START_SESSION_CODE = 1;

  public static final String SEND_MESSAGE = "SEND_MESSAGE";
  public static final int SEND_MESSAGE_CODE = 2;

  public static final String CLOSE_SESSION = "CLOSE_SESSION";
  public static final int CLOSE_SESSION_CODE = 3;

  public static final String SEND_MESSAGE_MESSAGE = "SEND_MESSAGE_message";

  public static final String SEND_MESSAGE_RECEIVERS = "SEND_MESSAGE_receivers";

  public static final String SESSION_UPDATE = "SESSION_UPDATE";

  public static final String SESSION_PAUSED = "SESSION_PAUSED";

  public static final String SESSION_RESUMED = "SESSION_RESUMED";

  public static final String SESSION_UPDATE_CONTENT = "SESSION_UPDATE_content";

  public static final String SESSION_UPDATE_TYPE = "SESSION_UPDATE_type";

  public static final String SESSION_UPDATE_TYPE_NORMAL = "SESSION_UPDATE_type_normal";

  public static final String SESSION_UPDATE_TYPE_CONTROL = "SESSION_UPDATE_type_control";

  public static final String SESSION_SHOW_ONLINE = "SESSION_SHOW_ONLINE";
  public static final int SESSION_SHOW_ONLINE_CODE = 4;

  public static final String SESSION_TRIGGER_PUSH = "SESSION_TRIGGER_PUSH";
  public static final int SESSION_TRIGGER_PUSH_CODE = 5;
}
