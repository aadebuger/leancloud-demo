package com.avoscloud.beijing.push.demo.keepalive;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.OnlinePeerQueryListener;
import com.avos.avoscloud.Session;
import com.avos.avoscloud.SessionManager;

/**
 * Created by nsun on 4/25/14.
 */
public class HeartBeatActivity extends Activity implements View.OnClickListener, Constants {

  private String currentName;

  private ImageButton sendBtn;
  private EditText composeZone;

  ListView chatList;
  ChatDataAdapter adapter;
  List<ChatMessage> messages = new LinkedList<ChatMessage>();
  Session session;
  String selfId;

  public static final String AVOSCLOUD_IM_SDK_ACTION = "this is action defined by you";
  public static final String AVOSCLOUD_IM_SDK_SESSION_OPEN = "AVOSCLOUD_IM_SDK_SESSION_OPEN";
  public static final String AVOSCLOUD_IM_SDK_SESSION_RESUME = "AVOSCLOUD_IM_SDK_SESSION_RESUME";
  public static final String AVOSCLOUD_IM_SDK_SESSION_PAUSE = "AVOSCLOUD_IM_SDK_SESSION_PAUSE";
  public static final String AVOSCLOUD_IM_SDK_SESSION_ONMESSAGE =
      "AVOSCLOUD_IM_SDK_SESSION_ONMESSAGE";
  public static final String AVOSCLOUD_IM_SDK_SESSION_ONMESSAGESEND =
      "AVOSCLOUD_IM_SDK_SESSION_ONMESSAGESEND";
  public static final String AVOSCLOUD_IM_SDK_SESSION_ONMESSAGEFAILURE =
      "AVOSCLOUD_IM_SDK_SESSION_ONMESSAGEFAILURE";
  public static final String AVOSCLOUD_IM_SDK_SESSION_ONSTATUSONLINE =
      "AVOSCLOUD_IM_SDK_SESSION_ONSTATUSONLINE";
  public static final String AVOSCLOUD_IM_SDK_SESSION_ONSTATUSOFFLINE =
      "AVOSCLOUD_IM_SDK_SESSION_ONSTATUSOFFLINE";


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.heartbeat);

    currentName = PreferenceManager.getDefaultSharedPreferences(this).getString("username", null);

    chatList = (ListView) this.findViewById(R.id.avoscloud_chat_list);
    adapter = new ChatDataAdapter(this, messages);
    chatList.setAdapter(adapter);
    sendBtn = (ImageButton) this.findViewById(R.id.sendBtn);
    composeZone = (EditText) this.findViewById(R.id.chatText);
    selfId = AVInstallation.getCurrentInstallation().getInstallationId();
    session = SessionManager.getInstance(selfId);
    sendBtn.setOnClickListener(this);

    ChatMessage message = new ChatMessage();
    message.setType(0);
    message.setMessage("欢迎加入聊天室:" + currentName);
    messages.add(message);
    adapter.notifyDataSetChanged();

  }

  @Override
  public void onNewIntent(Intent intent) {
    if (intent != null && intent.getAction() != null
        && AVOSCLOUD_IM_SDK_ACTION.equals(intent.getAction())) {
      if (AVOSCLOUD_IM_SDK_SESSION_ONMESSAGE.equals(intent.getExtras().getString(
          Session.AV_SESSION_INTENT_OPERATION_KEY))) {
        String messageParamString =
            intent.getExtras().getString(Session.AV_SESSION_INTENT_DATA_KEY);
        ChatMessage message = JSON.parseObject(messageParamString, ChatMessage.class);
        messages.add(message);
        adapter.notifyDataSetChanged();
      } 
    }
    super.onNewIntent(intent);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_off) {
      quitSession();
      finish();
      return true;
    } else if (item.getItemId() == R.id.action_show_users) {
      /*
       * Intent i = new Intent(this, ChatService.class); i.setAction(SESSION_SHOW_ONLINE);
       * startService(i);
       */
      session.queryOnlinePeers(session.getOnlinePeers(), new OnlinePeerQueryListener() {

        @Override
        public void onResults(List<String> peerIds) {
          ChatMessage message = new ChatMessage();
          message.setType(0);
          message.setMessage("当前在线的用户: " + peerIds.toString());
          messages.add(message);
          adapter.notifyDataSetChanged();
        }
      });

      return true;
    } else if (item.getItemId() == R.id.action_trigger_push) {
      /*
       * Intent i = new Intent(this, ChatService.class); i.setAction(SESSION_TRIGGER_PUSH);
       * startService(i);
       */
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void quitSession() {
    JSONObject obj = new JSONObject();
    obj.put("dn", currentName);
    obj.put("st", "off");
    session.sendMessage(obj.toJSONString(), session.getAllPeers());
    session.close();
  }

  @Override
  public void onBackPressed() {
    new AlertDialog.Builder(this).setMessage(R.string.confirm_exit)
        .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            quitSession();

            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
          }
        }).setNegativeButton("Cancel", null).show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.heartbeat, menu);
    return true;
  }


  private String makeMessage(String msg) {
    JSONObject obj = new JSONObject();
    obj.put("msg", msg);
    obj.put("dn", currentName);

    return obj.toJSONString();
  }

  @Override
  public void onClick(View v) {
    String text = composeZone.getText().toString();

    if (TextUtils.isEmpty(text)) {
      return;
    }

    session.sendMessage(makeMessage(text), session.getAllPeers());

    /*
     * Intent i = new Intent(this, ChatService.class); i.setAction(SEND_MESSAGE);
     * i.putExtra(SEND_MESSAGE_MESSAGE, makeMessage(text)); i.putExtra(SESSION_UPDATE_TYPE_NORMAL,
     * true); startService(i);
     */

    composeZone.getEditableText().clear();
    ChatMessage message = new ChatMessage();
    message.setMessage(text);
    message.setType(1);
    message.setUsername(currentName);
    messages.add(message);
    adapter.notifyDataSetChanged();
  }

}
