package com.avoscloud.beijing.push.demo.keepalive;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVMessageReceiver;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.Session;
import com.avos.avospush.notification.NotificationCompat;

public class ChatDemoMessageReceiver extends AVMessageReceiver {

  private final Queue<String> failedMessage = new LinkedList<String>();

  @Override
  public void onSessionOpen(Context context, Session session) {

    this.sendIntent(context, HeartBeatActivity.AVOSCLOUD_IM_SDK_SESSION_OPEN, null);

    // 此处仅仅是业务层决定的逻辑，完全可以不写
    JSONObject obj = new JSONObject();
    obj.put("dn", PreferenceManager.getDefaultSharedPreferences(context)
        .getString("username", null));
    obj.put("st", "on");
    session.sendMessage(obj.toJSONString(), session.getAllPeers());
  }

  @Override
  public void onSessionPaused(Context context, Session session) {
    LogUtil.avlog.d("这里掉线了");
  }

  @Override
  public void onSessionResumed(Context context, Session session) {
    LogUtil.avlog.d("重新连接上了");
    // 在遇到session重连之后，将之前失败的消息重发一次。。当然你可以将重复过程放在App里面，让用户自己去选择是否需要重发
    while (!failedMessage.isEmpty()) {
      String msg = failedMessage.poll();

      session.sendMessage(msg, session.getAllPeers(), false);
    }
  }

  @Override
  public void onMessage(Context context, Session session, String msg, String fromPeerId) {
    JSONObject j = JSONObject.parseObject(msg);
    ChatMessage message = new ChatMessage();
    /*
     * 这里是demo中自定义的数据格式，在你自己的实现中，可以完全自由的通过json来定义属于你自己的消息格式
     * 
     * 用户发送的消息 {"msg":"这是一个消息","dn":"这是消息来源者的名字"}
     * 
     * 用户的状态消息 {"st":"用户触发的状态信息","dn":"这是消息来源者的名字"}
     */

    if (j.containsKey("msg")) {

      message.setMessage(j.getString("msg"));
      message.setType(1);
      message.setUsername(j.getString("dn"));
      // 如果Activity在屏幕上不是active的时候就选择发送 通知
      if (!isChatUIActive(context)) {
        LogUtil.avlog.d("Activity inactive, about to send notification.");
        NotificationManager nm =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String ctnt = j.getString("dn") + "：" + j.getString("msg");
        Intent resultIntent = new Intent(context, HeartBeatActivity.class);
        resultIntent.setAction(HeartBeatActivity.AVOSCLOUD_IM_SDK_ACTION);
        resultIntent.putExtra(Session.AV_SESSION_INTENT_DATA_KEY, JSON.toJSONString(message));
        resultIntent.putExtra(Session.AV_SESSION_INTENT_OPERATION_KEY,
            HeartBeatActivity.AVOSCLOUD_IM_SDK_SESSION_ONMESSAGE);
        resultIntent
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        PendingIntent pi =
            PendingIntent.getActivity(context, -1, resultIntent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification =
            new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.notif_title))
                .setContentText(ctnt)
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(
                    BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                .setAutoCancel(true).build();
        nm.notify(233, notification);
        LogUtil.avlog.d("notification sent");
      } else {
        sendIntent(context, HeartBeatActivity.AVOSCLOUD_IM_SDK_SESSION_ONMESSAGE,
            JSON.toJSONString(message));
      }
    } else if (j.containsKey("st")) {
      // 是状态消息
      message.setType(0);
      String plainStatus;
      if (j.getString("st").equals("on")) {
        message.setMessage(String.format(context.getResources().getString(R.string.joined_notif),
            j.getString("dn")));
        // 如果聊天室有用户加入了，watch他，这样之后的消息会被发送到这个用户
        if (!session.isWatching(fromPeerId)) {
          List<String> watchPeers = new ArrayList<String>();
          watchPeers.add(fromPeerId);
          session.watchPeers(watchPeers);
        }
      } else {
        message.setMessage(String.format(context.getResources().getString(R.string.left_notif),
            j.getString("dn")));
        // 取消关注
        /*
         * if(session.isWatching(fromPeerId)) { List<String> watchPeers = new ArrayList<>();
         * watchPeers.add(fromPeerId); session.unwatchPeers(watchPeers); }
         */
      }

      /*
       * 将这些来自服务器的消息发送到具体的UI组件去，显示在UI上
       * 
       * Intent仅仅是最简单的方法
       */

      sendIntent(context, HeartBeatActivity.AVOSCLOUD_IM_SDK_SESSION_ONMESSAGE,
          JSON.toJSONString(message));
    }
  }

  @Override
  public void onMessageSent(Context context, Session session, String msg, List<String> receivers) {
    LogUtil.avlog.d("message sent :" + msg);
  }

  @Override
  public void onMessageFailure(Context context, Session session, String msg, List<String> receivers) {
    LogUtil.avlog.d("message failed :" + msg);
    this.failedMessage.offer(msg);
  }

  @Override
  public void onStatusOnline(Context context, Session session, List<String> peerIds) {
    LogUtil.avlog.d("status online :" + peerIds.toString());
  }

  @Override
  public void onStatusOffline(Context context, Session session, List<String> peerIds) {
    LogUtil.avlog.d("status offline :" + peerIds.toString());
  }

  @Override
  public void onError(Context context, Session session, Throwable e) {
    LogUtil.log.e("session error", (Exception) e);
  }


  private void sendIntent(Context context, String operation, String data) {
    Intent intent = new Intent(context, HeartBeatActivity.class);
    intent.setAction(HeartBeatActivity.AVOSCLOUD_IM_SDK_ACTION);
    intent.putExtra(Session.AV_SESSION_INTENT_OPERATION_KEY, operation);
    intent.putExtra(Session.AV_SESSION_INTENT_DATA_KEY, data);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(intent);
  }

  private boolean isChatUIActive(Context context) {
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    if (!pm.isScreenOn()) {
      return false;
    }

    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

    List<ActivityManager.RunningTaskInfo> taskInfos = am.getRunningTasks(1);

    return taskInfos.get(0).topActivity.getClassName().equals(HeartBeatActivity.class.getName());
  }

}
