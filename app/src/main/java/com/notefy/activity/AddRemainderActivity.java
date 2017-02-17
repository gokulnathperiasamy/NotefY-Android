package com.notefy.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.notefy.R;
import com.notefy.application.IApplicationConstant;
import com.notefy.database.BaseDBHelper;
import com.notefy.database.RemainderDAO;


public class AddRemainderActivity extends AppCompatActivity implements OnClickListener {
	
	private Toolbar mToolbar;
	Context context;
	private TextView mTitle;
	private ImageView mDeleteImage;
	private ImageView mToolBarBack;
	private ImageView mToolBarAdd;
	private LinearLayout addSection;
	private int number;
	private ConcurrentHashMap<String, String> messages;
	
	private BaseDBHelper baseDBHelper;
	private String contactID;
	private List<String> remainderMessage;
	private boolean isRemainderAdded;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_remainder);
		
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mTitle = (TextView) mToolbar.findViewById(R.id.toolbarText);
		mDeleteImage = (ImageView) mToolbar.findViewById(R.id.deleteImage);
		mToolBarBack =   (ImageView) mToolbar.findViewById(R.id.backButton);
		mToolBarAdd = (ImageView) mToolbar.findViewById(R.id.addButton);
		addSection = (LinearLayout) findViewById(R.id.add_section);
		context = this;
		number = 0;
		messages = new ConcurrentHashMap<String, String>();
		
		setupData();
		setUpTitle();
		addView();
		showRemainders();
	}

	private void setUpTitle() {
		if (mToolBarBack != null && mDeleteImage != null && mTitle != null && mToolBarBack != null) {
			mDeleteImage.setVisibility(View.GONE);
			
			mToolBarBack.setVisibility(View.VISIBLE);
			mToolBarBack.setImageResource(R.mipmap.ic_action_done);
			mToolBarBack.setOnClickListener(this);
			
			mToolBarAdd.setVisibility(View.VISIBLE);
			mToolBarAdd.setOnClickListener(this);
			
			mTitle.setText(getContactName(contactID));
			mTitle.setVisibility(View.VISIBLE);
			
			mTitle.setTextColor(getResources().getColor(R.color.white));
			setSupportActionBar(mToolbar);
		}
	}
	
	private void setupData() {
		contactID = getIntent().getExtras().getString(IApplicationConstant.CONTACT_DETAILS);
		isRemainderAdded = getIntent().getExtras().getBoolean(IApplicationConstant.REMAINDER_ADDED);
		loadRemainders(contactID);
	}
	
	private void addView(){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View messageView = inflater.inflate(R.layout.add_child, addSection, false);
		
		messageView.setTag(number);
		EditText messageEdit = (EditText) messageView.findViewById(R.id.message_edittext);
		messageEdit.requestFocus();
		ImageButton delete = (ImageButton) messageView.findViewById(R.id.delete_button);
		delete.setOnClickListener(this);
		
		if(addSection != null){
			addSection.addView(messageView);
		}
	}
	
	private void showRemainders() {
		if (remainderMessage != null && remainderMessage.size() > 0) {
			for (String message : remainderMessage) {
				addChild(message, false);
			}
		}
	}

	private String getContactName(String contactID) {
		String[] projection = new String[] {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER};
		Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone._ID + " = ?", new String[] { contactID }, Phone.DISPLAY_NAME + " ASC");
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				return cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			}
		}
		cursor.close();
		return contactID;
	}
	
	private void loadRemainders(String contactID) {
		remainderMessage = new ArrayList<String>();
		String[] mProjection = { RemainderDAO.Columns.CONTACT_ID, RemainderDAO.Columns.REMAINDER_MESSAGE };
		Cursor savedRemainderCursor = getApplicationContext().getContentResolver().query(RemainderDAO.CONTENT_URI, mProjection, RemainderDAO.Columns.CONTACT_ID + " = ? ", new String[] { contactID }, RemainderDAO.Columns.CONTACT_ID + " ASC");
		if (savedRemainderCursor != null && savedRemainderCursor.getCount() > 0) {
			while (savedRemainderCursor.moveToNext()) {
				String remainder = savedRemainderCursor.getString(savedRemainderCursor.getColumnIndex(RemainderDAO.Columns.REMAINDER_MESSAGE));
				remainderMessage.add(remainder);
			}
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.addButton:
				addChild("", true, view);
				break;
			case R.id.delete_button:
				removeChild(view);
				break;
			case R.id.backButton:
				saveAndGoToRemainderListActivity();
				break;
		}
	}
	
	private void saveAndGoToRemainderListActivity() {
		getRemaindersFromList();
		saveRemainders();
		Intent intent = new Intent(this, RemainderListActivity.class);
		startActivity(intent);
		finish();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (isRemainderAdded) {
			discardAndGoToRemainderListActivity();
		} else {
			discardAndGoToContactListActivity();
		}
	}
	
	private void discardAndGoToRemainderListActivity() {
		Intent intent = new Intent(this, RemainderListActivity.class);
		startActivity(intent);
		finish();
	}

	private void discardAndGoToContactListActivity() {
		Intent intent = new Intent(this, ContactListActivity.class);
		startActivity(intent);
		finish();
	}
	
	private void addChild(String message, boolean flag) {
		addChild(message, flag, null);
	}

	private void addChild(String message, boolean flag, final View view) {
		if(isPreviousViewEmpty() && flag){
			return;
		}
		int childCount = addSection.getChildCount();
		if (childCount == 0) {
			addView();
			return;
		}
		View addLayout = addSection.getChildAt(childCount -1);
		final EditText messageEdit = (EditText) addLayout.findViewById(R.id.message_edittext);
		messageEdit.addTextChangedListener(new TextWatcher() {  
			String oldString;
			String newString;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {  
            	
            }                       
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            	oldString = s.toString();
            }                       
            @Override
            public void afterTextChanged(Editable s) {
            	newString = s.toString();
            	for (Entry<String, String> message : messages.entrySet()) {
    				if (message.getValue().equalsIgnoreCase(oldString)) {
    					messages.remove(message.getKey());
    					messages.put(String.valueOf(message.getKey().toString()), newString);
    				}
    			}
            	
            }
        });
		if (message != null && message.length() > 0) {
			messageEdit.setText(message);
		}
		messages.put(String.valueOf(number), messageEdit.getText().toString().trim());
		number++;
		addView();
	}
	
	private boolean isPreviousViewEmpty() {
		int childCount = addSection.getChildCount();
		View addLayout = addSection.getChildAt(childCount - 1);
		if (addLayout != null) {
			EditText messageEdit = (EditText) addLayout.findViewById(R.id.message_edittext);
			String message = messageEdit.getText().toString().trim();
			if (!(message != null && message.length() > 0)) {
				return true;
			}
		}
		return false;
	}

	private void removeChild(View view) {
		View buttonLayout = (View) view.getParent();
		View addLayout = (View) buttonLayout.getParent(); 
		for (int i = 0; i < addSection.getChildCount(); i++) {
			if(addLayout.getTag().toString().equals(addSection.getChildAt(i).getTag().toString())){
				addSection.removeViewAt(i);
				messages.remove(addLayout.getTag().toString());
				break;
			}
		}
	}
	
	private void getRemaindersFromList() {
		int childCount = addSection.getChildCount();
		if (childCount <= 0) {
			deleteExistingRecords();
			return;
		}
		if (childCount == messages.size()) {
			return;
		}
		View addLayout = addSection.getChildAt(childCount - 1);
		EditText messageEdit = (EditText) addLayout.findViewById(R.id.message_edittext);
		String message = messageEdit.getText().toString().trim();
		if (message != null && message.length() > 0) {
			messages.put(String.valueOf(number), message);	
		}
	}

	private void saveRemainders() {
		if (messages != null && messages.size() == 0) {
			deleteExistingRecords();
		} else {
			List<String> remainders = new ArrayList<String>();
			for (Entry<String, String> message : messages.entrySet()) {
				if (message.getValue().trim().length() > 0) {
					remainders.add(message.getValue());
				}
			}
			if (remainders.size() > 0) {
				deleteExistingRecords();
				saveToDB(remainders);
			}
		}
	}

	private void deleteExistingRecords() {
		try {
			getApplicationContext().getContentResolver().delete(RemainderDAO.CONTENT_URI, RemainderDAO.Columns.CONTACT_ID + " = ? ", new String[] {contactID});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveToDB(List<String> remainderMessage) {
		baseDBHelper = new BaseDBHelper(getApplicationContext());
		baseDBHelper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		for (String message : remainderMessage) {
			contentValues.clear();
			contentValues.put(RemainderDAO.Columns.CONTACT_ID, contactID);
			contentValues.put(RemainderDAO.Columns.REMAINDER_MESSAGE, message.trim());
	        getApplicationContext().getContentResolver().insert(RemainderDAO.CONTENT_URI, contentValues);	
		}
		baseDBHelper.close();
	}

}
