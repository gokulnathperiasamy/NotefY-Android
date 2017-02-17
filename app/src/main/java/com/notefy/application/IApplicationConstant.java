package com.notefy.application;

import com.notefy.activity.SettingsActivity;

public interface IApplicationConstant {

	public static final int NOTIFICATION_ID = 65536;
	public static final String NOTIFICATION_MESSAGE = "NotificationMessage";
	public static final String NOTIFICATION_MESSAGE_CONTACT_ID = "NotificationMessageContactID";
	
	public static final String CONTACT_DETAILS = "ContactDetails";
	public static final String REMAINDER_ADDED = "RemainderAdded";
	
	public static final String SHAREDPREFERENCES_NAME = SettingsActivity.class.getCanonicalName();
	public static final String IS_FIRST_LAUNCH = "IsFirstLaunch";
	public static final String SHAREDPREFERENCES_IS_NOTEFY_ENABLED = "IsNotefYEnabled";
	public static final String SHAREDPREFERENCES_IS_TOAST_ENABLED = "IsToastEnabled";
	public static final String SHAREDPREFERENCES_IS_NOTIFICATION_ENABLED = "IsNotificationEnabled";
	public static final String SHAREDPREFERENCES_IS_ALERT_DIALOG_ENABLED = "IsAlertDialogEnabled";
	
	public static final String SHAREDPREFERENCES_IS_PLAY_SOUND_ENABLED = "IsPlaySoundEnabled";
	public static final String SHAREDPREFERENCES_IS_VIBRATE_ENABLED = "IsVibrateEnabled";
	
	public static final String EXIT_CONFIRMATION = "Click again to Exit!";
	
}
