package com.notefy.activity;

import com.notefy.R;
import com.notefy.application.IApplicationConstant;
import com.notefy.service.CallService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity implements OnClickListener {

	private Toolbar mToolbar;
	Context context;
	private TextView mTitle;
	private ImageView mDeleteImage;
	private ImageView mToolBarBack;
	private ImageView mToolBarAdd;
	private CheckBox notefy;
	private CheckBox alert;
	private CheckBox notificaiton;
	private CheckBox toast;
	private CheckBox playSound;
	private CheckBox vibrate;
	private boolean notefyEnabled = false;
	private SharedPreferences sharedPreferences;
	private Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		context = this;
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mTitle = (TextView) mToolbar.findViewById(R.id.toolbarText);
		mDeleteImage = (ImageView) mToolbar.findViewById(R.id.deleteImage);
		mToolBarBack =   (ImageView) mToolbar.findViewById(R.id.backButton);
		mToolBarAdd = (ImageView) mToolbar.findViewById(R.id.addButton);
		setUpTitle();
		
		notefy = (CheckBox) findViewById(R.id.cb_notefy);
		alert = (CheckBox) findViewById(R.id.cb_alert);
		notificaiton = (CheckBox) findViewById(R.id.cb_notificaiton);
		toast = (CheckBox) findViewById(R.id.cb_toast);
		playSound = (CheckBox) findViewById(R.id.cb_play_sound);
		vibrate = (CheckBox) findViewById(R.id.cb_vibrate);
		
		notefy.setOnClickListener(this);
		alert.setOnClickListener(this);
		notificaiton.setOnClickListener(this);
		toast.setOnClickListener(this);
		playSound.setOnClickListener(this);
		vibrate.setOnClickListener(this);
		
		loadSharedPreference();
		toggleNotificationSettings();
	}
	
	private void loadSharedPreference() {
		sharedPreferences = getApplicationContext().getSharedPreferences(IApplicationConstant.SHAREDPREFERENCES_NAME, MODE_PRIVATE);
		notefyEnabled = sharedPreferences.getBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_NOTEFY_ENABLED, true);
		notefy.setChecked(notefyEnabled);
		toast.setChecked(sharedPreferences.getBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_TOAST_ENABLED, false));
		notificaiton.setChecked(sharedPreferences.getBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_NOTIFICATION_ENABLED, false));
		alert.setChecked(sharedPreferences.getBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_ALERT_DIALOG_ENABLED, true));
		playSound.setChecked(sharedPreferences.getBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_PLAY_SOUND_ENABLED, false));
		vibrate.setChecked(sharedPreferences.getBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_VIBRATE_ENABLED, false));
	}
	
	private void saveSharedPreference() {
		notefyEnabled = notefy.isChecked();
		editor = sharedPreferences.edit();
		editor.putBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_NOTEFY_ENABLED, notefyEnabled);
		editor.putBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_TOAST_ENABLED, toast.isChecked());
		editor.putBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_NOTIFICATION_ENABLED, notificaiton.isChecked());
		editor.putBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_ALERT_DIALOG_ENABLED, alert.isChecked());
		editor.putBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_PLAY_SOUND_ENABLED, playSound.isChecked());
		editor.putBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_VIBRATE_ENABLED, vibrate.isChecked());
		editor.commit();
	}

	private void setUpTitle() {
		if (mToolBarBack != null && mDeleteImage != null && mTitle != null) {
			mToolBarBack.setVisibility(View.VISIBLE);
			mDeleteImage.setVisibility(View.GONE);
			mToolBarAdd.setVisibility(View.GONE);
			mToolBarBack.setOnClickListener(this);
			mTitle.setText(getResources().getString(R.string.action_settings));
			mTitle.setTextColor(getResources().getColor(R.color.white));
			mTitle.setVisibility(View.VISIBLE);
			setSupportActionBar(mToolbar);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.backButton:
				saveAndGoToRemainderListActivity();
				finish();
				break;
			case R.id.cb_notefy:
				toggleNotificationSettings();
				break;
		}
	}

	private void toggleNotificationSettings() {
		toast.setEnabled(notefy.isChecked());
		notificaiton.setEnabled(notefy.isChecked());
		alert.setEnabled(notefy.isChecked());
		playSound.setEnabled(notefy.isChecked());
		vibrate.setEnabled(notefy.isChecked());
		if (!notefy.isChecked()) {
			notificaiton.setAlpha(0.4f);
			toast.setAlpha(0.4f);
			alert.setAlpha(0.4f);
			playSound.setAlpha(0.4f);
			vibrate.setAlpha(0.4f);
		} else {
			notificaiton.setAlpha(1f);
			toast.setAlpha(1f);
			alert.setAlpha(1f);
			playSound.setAlpha(1f);
			vibrate.setAlpha(1f);
		}
	}

	private void setNotefYEnabled(boolean notefyEnabled2) {
		Intent intent = new Intent(this, CallService.class);
		try {
			if (notefyEnabled) {
				startService(intent);
			} else {
				stopService(intent);
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		saveAndGoToRemainderListActivity();
	}
	
	private void saveAndGoToRemainderListActivity() {
		saveSharedPreference();
		setNotefYEnabled(notefyEnabled);
		Intent intent = new Intent(this, RemainderListActivity.class);
		startActivity(intent);
		finish();
	}

}
