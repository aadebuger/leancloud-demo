package com.avoscloud.beijing.push.demo.keepalive;

import java.util.LinkedList;
import java.util.List;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.Session;
import com.avos.avoscloud.SessionManager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

  private EditText nameInput;
  private Button joinButton;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);


    joinButton = (Button) findViewById(R.id.button);
    joinButton.setOnClickListener(this);
    nameInput = (EditText) findViewById(R.id.editText);

    String predefinedName =
        PreferenceManager.getDefaultSharedPreferences(this).getString("username", null);
    if (predefinedName != null) {
      nameInput.setText(predefinedName);
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
  }

  @Override
  public void onClick(View v) {
    String name = nameInput.getText().toString();
    if (name == null || name.trim().isEmpty()) {
      nameInput.setError("");
      return;
    }

    SharedPreferences spr = PreferenceManager.getDefaultSharedPreferences(this);
    spr.edit().putString("username", name).commit();

    // Intent intent = new Intent(this, HeartBeatActivity.class);
    // startActivity(intent);
    AVQuery<AVObject> aviq = new AVQuery<AVObject>("_Installation");
    final String selfId = AVInstallation.getCurrentInstallation().getInstallationId();
    aviq.whereEqualTo("valid", true).findInBackground(new FindCallback<AVObject>() {
      @Override
      public void done(List<AVObject> installations, AVException exception) {
        if (null == exception) {
          List<String> peerIds = new LinkedList<String>();
          for (AVObject object : installations) {
            if (!selfId.equals(object.getString("installationId"))
                && object.getString("installationId") != null) {
              peerIds.add(object.getString("installationId"));
            }
          }
          Session session = SessionManager.getInstance(selfId);
          session
              .setSignatureFactory(new KeepAliveSignatureFactory(AVOSCloud.applicationId, selfId));
          session.open(selfId, peerIds);
        } else {
          Toast.makeText(MainActivity.this, "查询异常", Toast.LENGTH_SHORT).show();
        }
      }
    });

  }
}
