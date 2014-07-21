package com.luna.anytime;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FunctionCallback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class DoingDetailActivity extends AnyTimeActivity {

	TextView loadingText;
	TextView atTimeTitleText;
	TextView countText;
	String doingObjectId;
	String doingObjectTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_doing_detail);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		doingObjectId = intent.getStringExtra("childobj");
		doingObjectTitle = intent.getStringExtra("childtitle");
		loadingText = (TextView) findViewById(R.id.textView_doing_detail_loading);
		atTimeTitleText = (TextView) findViewById(R.id.textView_doing_detail_same_time_title);
		countText = (TextView) findViewById(R.id.textView_doing_detail_count);
		SearchData();
	}

	private void SearchData() {
		AVQuery<AVObject> query = new AVQuery<AVObject>("DoingList");
		query.whereEqualTo("doingListChildObjectId", doingObjectId);
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, -10);
		// query.whereNotEqualTo("userObjectId", userId);
		query.whereGreaterThan("createdAt", c.getTime());
		query.countInBackground(new CountCallback() {
			@Override
			public void done(int count, AVException e) {
				if (e == null) {
					showDetail(count);
				} else {
					loadingText
							.setText(getString(R.string.doing_list_error_loading));
				}
				mHandler.obtainMessage(1).sendToTarget();
			}
		});
	}

	private void showDetail(int count) {
		findViewById(R.id.view_doing_detail_1).setVisibility(View.VISIBLE);
		findViewById(R.id.view_doing_detail_2).setVisibility(View.VISIBLE);
		findViewById(R.id.textView_doing_detail_get_chievement).setVisibility(
				View.VISIBLE);
		loadingText.setVisibility(View.INVISIBLE);
		atTimeTitleText
				.setText(getString(R.string.doing_detail_same_time_title)
						.replace("{0}", doingObjectTitle));
		countText.setText(count + getString(R.string.doing_detail_person));
	}

	private void uploadData() {
		AVObject doing = new AVObject("DoingList");
		doing.put("userObjectId", getUserId());
		doing.put("doingListChildObjectId", doingObjectId);
		doing.saveInBackground();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getAchievement() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("userObjectId", getUserId());
		AVCloud.callFunctionInBackground("hello", parameters,
				new FunctionCallback() {
					@Override
					public void done(Object object, AVException e) {
						if (e == null) {
							Log.e("at", object.toString());// processResponse(object);
						} else {
							// handleError();
						}
					}
				});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (1 == msg.what) {
				uploadData();
				getAchievement();
			}
		}
	};
}
