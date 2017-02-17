package com.notefy.service;

import com.notefy.R;
import com.notefy.application.IApplicationConstant;
import com.notefy.database.RemainderDAO;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Handler;
//import android.os.Looper;
import android.os.Vibrator;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class CallServiceHelper {

	private Context context;
	private TelephonyManager telephonyManager;
	private IncomingCallListener incomingCallListener;
	private OutgoingCallReceiver outgoingCallReceiver;
	
	private boolean showToast;
	private boolean showNotification;
	private boolean showAlertDialog;
	private String contactID;
	
	private boolean playSound;
	private boolean vibrate;
	
	private SharedPreferences sharedPreferences;
	private AlertDialog dialog;
	
	private long ALERT_TIMEOUT = 20000;

	public CallServiceHelper(Context context) {
		this.context = context;
		this.incomingCallListener = new IncomingCallListener();
		this.outgoingCallReceiver = new OutgoingCallReceiver();
		getPreferenceValues();
	}
	
	private void getPreferenceValues() {
		sharedPreferences = context.getApplicationContext().getSharedPreferences(IApplicationConstant.SHAREDPREFERENCES_NAME, 0);
		showToast = sharedPreferences.getBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_TOAST_ENABLED, false);
		showNotification = sharedPreferences.getBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_NOTIFICATION_ENABLED, false);
		showAlertDialog = sharedPreferences.getBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_ALERT_DIALOG_ENABLED, false);
		playSound = sharedPreferences.getBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_PLAY_SOUND_ENABLED, false);
		vibrate = sharedPreferences.getBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_VIBRATE_ENABLED, false);
	}

	public void start() {
		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(incomingCallListener, PhoneStateListener.LISTEN_CALL_STATE);
		context.registerReceiver(outgoingCallReceiver, new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL));
	}
	
	public void stop() {
		telephonyManager.listen(incomingCallListener, PhoneStateListener.LISTEN_NONE);
		context.unregisterReceiver(outgoingCallReceiver);
	}
	
	private class IncomingCallListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
				case TelephonyManager.CALL_STATE_RINGING:
					contactID = "";
					handleCalls(context, incomingNumber);
					break;
			}
		}
	}
	
	public class OutgoingCallReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	contactID = "";
	        handleCalls(context, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
	    }
	}
	
	private void handleCalls(Context context, String phoneNumber) {
		String remainderMessage = getRemainderMessage(context, phoneNumber);
		displayRemainder(context, remainderMessage);
	}
	
	private String getRemainderMessage(Context context, String phoneNumber) {
		contactID = "" + getContactIDFromNumber(context, phoneNumber);
		return loadRemainders(contactID);
	}
	
	private String loadRemainders(String contactID) {
		String returnMessage = "";
		String[] mProjection = { RemainderDAO.Columns.CONTACT_ID, RemainderDAO.Columns.REMAINDER_MESSAGE };
		Cursor savedRemainderCursor = context.getApplicationContext().getContentResolver().query(RemainderDAO.CONTENT_URI, mProjection, RemainderDAO.Columns.CONTACT_ID + " = ? ", new String[] { contactID }, RemainderDAO.Columns.CONTACT_ID + " ASC");
		if (savedRemainderCursor.getCount() > 0) {
			while (savedRemainderCursor.moveToNext()) {
				returnMessage += savedRemainderCursor.getString(savedRemainderCursor.getColumnIndex(RemainderDAO.Columns.REMAINDER_MESSAGE));
				returnMessage += " \n";
			}
		}
		return returnMessage;
	}
	
	private void displayRemainder(Context context, String remainderMessage) {
		if (context == null || remainderMessage == null || remainderMessage.length() == 0) 
			return;
		playSoundAndVibrate();
		if (showToast) showToast(context, remainderMessage);
		if (showNotification) showNotification(context, remainderMessage);
		if (showAlertDialog) showAlertDialog(context, remainderMessage);
	}
	
	private void playSoundAndVibrate() {
		if (playSound) {
			ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 1000);
			toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(250);
		}
	}

	private static int getContactIDFromNumber(Context context, String phoneNumber) {
		phoneNumber = Uri.encode(phoneNumber);
		int contactIDFromNumber = Integer.MAX_VALUE;
		Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, phoneNumber),new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup._ID}, null, null, null);
		while(contactLookupCursor.moveToNext()){
			contactIDFromNumber = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(PhoneLookup._ID));
		}
		contactLookupCursor.close();
		return contactIDFromNumber;
	}

	private void showToast(Context context, String message) {
		if (message.length() > 200) {
			message = message.substring(0, 199) + " ...";
		}
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
	
	private void showNotification(Context context, String message) {
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(context)
			.setContentTitle(context.getResources().getString(R.string.notefy_app_name))
			.setContentText(message)
			.setDefaults(Notification.DEFAULT_ALL)
			.setAutoCancel(true)
			.setContentIntent(null)
			.setSmallIcon(R.mipmap.ic_launcher);
		Notification notification = mNotifyBuilder.build();
	    notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    mNotificationManager.notify(IApplicationConstant.NOTIFICATION_ID, notification);
	}
	
	private void showAlertDialog(final Context context, final String message) {
		// Dismiss the previous dialog...
		if (dialog != null) dialog.dismiss();
		Handler handler = new Handler();
		try {
			showDialog(message);
			handler.postDelayed(new Runnable() {
				public void run() {
					if (dialog != null) dialog.dismiss();
				}
			}, ALERT_TIMEOUT);
		} catch (Exception e) {
			if (dialog != null) dialog.dismiss();
			e.printStackTrace();
		}
	}
	
	private void showDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context.getApplicationContext());
		builder.setMessage(message).setTitle(context.getApplicationContext().getString(R.string.notefy_app_name));
		builder.setPositiveButton(context.getResources().getString(R.string.dismiss_dialog), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg, int which) {
				dialog.cancel();
			}
		});
		builder.setNegativeButton(context.getResources().getString(R.string.dismiss_and_delete_dialog), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				deleteRemainder();
			}
		});
		builder.setOnCancelListener(new Dialog.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
			}
		});

		dialog = builder.create();
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		if (message.length() > 200) {
			dialog.getWindow().setLayout(dialog.getWindow().getAttributes().width, getHeight(dialog));
		}
	}
	
	private int getHeight(Dialog dialog) {
		int heightby2;
		try {
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    	Display display = wm.getDefaultDisplay();
	    	Point size = new Point();
	    	display.getSize(size);
	    	int height = size.y;
	    	heightby2 = height/2;
		} catch (Exception e) {
			return dialog.getWindow().getAttributes().height;
		}
		return heightby2;
	}
	
	private void deleteRemainder() {
		try {
			context.getApplicationContext().getContentResolver().delete(RemainderDAO.CONTENT_URI, RemainderDAO.Columns.CONTACT_ID + " = ? ", new String[] {contactID});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
