package com.notefy.activity;

import java.util.ArrayList;

import com.notefy.R;
import com.notefy.entity.ContactListEntity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContactListActivity extends AppCompatActivity implements OnClickListener {
	
	private Toolbar toolbar;
	Context context;
	private ArrayList<ContactListEntity> listData;
	private RecyclerView mRecyclerView;
	private ContactListAdapter contactListAdapter;
	private TextView mTitle;
	private ImageView mDeleteImage;
	private ImageView mToolBarBack;
	private ImageView mSearchButton;
	private SearchView mSearchView;
	private LinearLayout noMessageLayout;
	private LinearLayoutManager mLinearLayoutManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		
		mTitle = (TextView) toolbar.findViewById(R.id.toolbarText);
		mTitle.setText(getResources().getString(R.string.select_contact));
		mDeleteImage = (ImageView) toolbar.findViewById(R.id.deleteImage);
		mToolBarBack = (ImageView) toolbar.findViewById(R.id.backButton);
		mSearchButton = (ImageView) toolbar.findViewById(R.id.searchButton);
		mSearchView = (SearchView) toolbar.findViewById(R.id.svSearchContact);
		mToolBarBack.setOnClickListener(this);
		mSearchView.setOnClickListener(this);
		mDeleteImage.setOnClickListener(null);
		mSearchButton.setOnClickListener(this);
		context = this;
		setupTitle();
		
		mRecyclerView = (RecyclerView) findViewById(R.id.place_listc);
		noMessageLayout = (LinearLayout) findViewById(R.id.no_contact);
		mLinearLayoutManager = new LinearLayoutManager(context.getApplicationContext());
		mRecyclerView.setLayoutManager(mLinearLayoutManager);
		setupData();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setupData();
	}
	
	private void setupTitle() {
		if (mToolBarBack != null && mDeleteImage != null && mTitle != null) {
			mToolBarBack.setVisibility(View.VISIBLE);
			mSearchButton.setVisibility(View.VISIBLE);
			mDeleteImage.setVisibility(View.GONE);
			mSearchView.setVisibility(View.GONE);
			mTitle.setVisibility(View.GONE);
			mTitle.setText(getResources().getString(R.string.contact_list_lable));
			mTitle.setTextColor(getResources().getColor(R.color.white));
			setSupportActionBar(toolbar);
		}
	}
	
	private void setupData() {
		listData = getContactList();
		if(listData != null && listData.size() > 0){
			noMessageLayout.setVisibility(View.GONE);
			mSearchButton.setVisibility(View.VISIBLE);
			mRecyclerView.setVisibility(View.VISIBLE);
			mTitle.setText(getResources().getString(R.string.contact_list_lable));
			mTitle.setVisibility(View.VISIBLE);
			mSearchView.setVisibility(View.GONE);
			contactListAdapter = new ContactListAdapter(listData, context);
			mRecyclerView.setAdapter(contactListAdapter);
			addSearchViewListener();
		} else{
			noMessageLayout.setVisibility(View.VISIBLE);
			mSearchButton.setVisibility(View.GONE);
			mRecyclerView.setVisibility(View.GONE);
			mTitle.setText(getResources().getString(R.string.no_contact_list_lable));
			mTitle.setVisibility(View.VISIBLE);
			mSearchView.setVisibility(View.GONE);
		}
	}
	
	private ArrayList<ContactListEntity> getContactList() {
		String[] projection = new String[] {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER};
		ArrayList<ContactListEntity> contactList = new ArrayList<ContactListEntity>();
		ContactListEntity contactListEntity;
		ContentResolver contentResolver = getContentResolver();
		Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, Phone.DISPLAY_NAME + " ASC");
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				contactListEntity = new ContactListEntity();
				String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (name == null || name.length() == 0) {
					name = "[No Name]";
				}
				if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					contactListEntity.setContactID(id);
					contactListEntity.setContactName(name);
					contactList.add(contactListEntity);
				}
			}
		}
		cursor.close();
		return contactList;
	}
	
	@Override
	public void onClick(View view) {
		Intent intent;
		switch(view.getId()) {
			case R.id.place_list:
				intent = new Intent(this, AddRemainderActivity.class);
				startActivity(intent);
				break;
			case R.id.backButton:
				intent = new Intent(this, RemainderListActivity.class);
				startActivity(intent);
				finish();
				break;
			case R.id.searchButton:
				toggleSearchView();
				break;
			case R.id.no_contact_text2:
			case R.id.no_contact_text1:
				openContactManager();
				break;
		}
	}
	
	private void toggleSearchView() {
		mTitle.setVisibility(View.GONE);
		mSearchButton.setVisibility(View.GONE);
		mSearchView.setVisibility(View.VISIBLE);
		((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		mSearchView.requestFocus();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(this, RemainderListActivity.class);
		startActivity(intent);
		finish();
	}

	private void openContactManager() {
		Intent intent= new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
        startActivity(intent);
	}
	
	private void addSearchViewListener() {
		SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		mSearchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
		
		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String arg) {
				refreshData(arg);
				return true;
			}
			@Override
			public boolean onQueryTextChange(String arg) {
				refreshData(arg);
				return true;
			}
		});
	}
	
	@SuppressLint("DefaultLocale")
	private void refreshData(String inputString) {
		if (inputString != null && inputString.equals("")) {
			contactListAdapter = new ContactListAdapter(listData, context);
			mRecyclerView.setAdapter(contactListAdapter);
			return;
		}
		ArrayList<ContactListEntity> filteredList = new ArrayList<ContactListEntity>();
		String searchString = inputString.toString().toLowerCase();
		String regularExpression = ".*\\b\\Q" + searchString + "\\E.*";
		for (ContactListEntity contactListEntity : listData) {
			String splitName[] = contactListEntity.getContactName().toLowerCase().split(" ");
			for (String words : splitName) {
				if (words.matches(regularExpression))
					filteredList.add(contactListEntity);
			}
			// Search anywhere in the string...
			/*if (contactListEntity.getContactName().toLowerCase().contains(inputString.toLowerCase())) {
				filteredList.add(contactListEntity);
			}*/
		}
		if (filteredList != null && filteredList.size() > 0) {
			contactListAdapter = new ContactListAdapter(filteredList, context);
			mRecyclerView.setAdapter(contactListAdapter);
		} else {
			contactListAdapter = new ContactListAdapter(new ArrayList<ContactListEntity>(), context);
			mRecyclerView.setAdapter(contactListAdapter);
		}
	}

}
