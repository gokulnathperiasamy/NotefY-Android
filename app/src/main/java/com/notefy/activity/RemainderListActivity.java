package com.notefy.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.notefy.R;
import com.notefy.application.IApplicationConstant;
import com.notefy.database.RemainderDAO;
import com.notefy.entity.RemainderListEntity;
import com.notefy.service.CallService;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RemainderListActivity extends AppCompatActivity implements OnClickListener, OnDeleteClickListener {

    private Toolbar toolbar;
    private Context context;
    private ArrayList<RemainderListEntity> listData;
    private RecyclerView mRecyclerView;
    private RemainderListAdapter remainderListAdapter;
    private TextView mTitle;
    private ImageView mDeleteImage;
    private ImageView mToolBarBack;
    private ImageView appIcon;
    private ArrayList<RemainderListEntity> deleteList = new ArrayList<>();
    private LinearLayout noMessageLayout;
    private LinearLayoutManager mLinearLayoutManager;
    private SharedPreferences sharedPreferences;
    private Editor editor;
    private boolean isNotefyEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remainder_list);
        firstLaunchSettings();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitle = (TextView) toolbar.findViewById(R.id.toolbarText);
        mDeleteImage = (ImageView) toolbar.findViewById(R.id.deleteImage);
        mToolBarBack = (ImageView) toolbar.findViewById(R.id.backButton);
        appIcon = (ImageView) toolbar.findViewById(R.id.appIcon);
        appIcon.setVisibility(View.GONE);
        mTitle.setVisibility(View.VISIBLE);
        mToolBarBack.setOnClickListener(this);
        mToolBarBack.setVisibility(View.GONE);
        mDeleteImage.setVisibility(View.GONE);
        mDeleteImage.setOnClickListener(this);
        context = this;
        setupTitle();

        mRecyclerView = (RecyclerView) findViewById(R.id.place_list);
        noMessageLayout = (LinearLayout) findViewById(R.id.no_message);
        mLinearLayoutManager = new LinearLayoutManager(context.getApplicationContext());

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        listData = new ArrayList<>();
        refreshData();
    }

    private void firstLaunchSettings() {
        sharedPreferences = getApplicationContext().getSharedPreferences(IApplicationConstant.SHAREDPREFERENCES_NAME, MODE_PRIVATE);
        isNotefyEnabled = sharedPreferences.getBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_NOTEFY_ENABLED, true);
        if (sharedPreferences != null && sharedPreferences.getBoolean(IApplicationConstant.IS_FIRST_LAUNCH, true)) {
            editor = sharedPreferences.edit();
            editor.putBoolean(IApplicationConstant.IS_FIRST_LAUNCH, false);
            editor.putBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_NOTEFY_ENABLED, true);
            editor.putBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_TOAST_ENABLED, false);
            editor.putBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_NOTIFICATION_ENABLED, false);
            editor.putBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_ALERT_DIALOG_ENABLED, true);
            editor.putBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_PLAY_SOUND_ENABLED, false);
            editor.putBoolean(IApplicationConstant.SHAREDPREFERENCES_IS_VIBRATE_ENABLED, false);
            editor.apply();
        }
        setNotefYEnabled(isNotefyEnabled);
    }

    private void setNotefYEnabled(boolean isNotefyEnabled) {
        Intent intent = new Intent(this, CallService.class);
        try {
            if (isNotefyEnabled) {
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
        this.finishAffinity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        listData = new ArrayList<>();
        setupData();
        if (listData != null && listData.size() > 0) {
            noMessageLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            remainderListAdapter = new RemainderListAdapter(listData, context);
            remainderListAdapter.setOnDeleteClickListener(this);
            mRecyclerView.setAdapter(remainderListAdapter);
        } else {
            noMessageLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    private void setupData() {
        RemainderListEntity remainderListEntity = null;
        String previousContactID = null;
        List<String> remainderMessage = null;
        int messageCount = 0;
        String[] mProjection = {RemainderDAO.Columns.CONTACT_ID, RemainderDAO.Columns.REMAINDER_MESSAGE};
        Cursor savedRemainderCursor = getApplicationContext().getContentResolver().query(RemainderDAO.CONTENT_URI, mProjection, null, null, RemainderDAO.Columns.CONTACT_ID + " ASC");
        if (savedRemainderCursor != null && savedRemainderCursor.getCount() > 0) {
            while (savedRemainderCursor.moveToNext()) {
                String currentContactID = savedRemainderCursor.getString(savedRemainderCursor.getColumnIndex(RemainderDAO.Columns.CONTACT_ID));
                if (previousContactID != null && currentContactID.equalsIgnoreCase(previousContactID) && remainderListEntity != null) {
                    remainderMessage.add(savedRemainderCursor.getString(savedRemainderCursor.getColumnIndex(RemainderDAO.Columns.REMAINDER_MESSAGE)));
                    messageCount++;
                } else {
                    remainderListEntity = new RemainderListEntity();
                    remainderMessage = new ArrayList<>();
                    messageCount = 0;
                    remainderListEntity.setContactID(currentContactID);
                    String contactName = getContactName(currentContactID);
                    if (contactName == null) {
                        continue;
                    }
                    remainderListEntity.setContactName(contactName);
                    remainderMessage.add(savedRemainderCursor.getString(savedRemainderCursor.getColumnIndex(RemainderDAO.Columns.REMAINDER_MESSAGE)));
                    messageCount++;
                    previousContactID = currentContactID;
                }
                if (remainderListEntity != null) {
                    remainderListEntity.setNumberOfMessages("" + messageCount);
                    remainderListEntity.setRemainderMessage(remainderMessage);
                    if (messageCount > 1 && listData != null) {
                        listData.remove(remainderListEntity);
                    }
                    if (listData != null) {
                        listData.add(remainderListEntity);
                    }
                }
            }
        }
        if (listData != null) {
            Collections.sort(listData, new Comparator<RemainderListEntity>() {
                @Override
                public int compare(RemainderListEntity entity1, RemainderListEntity entity2) {
                    return entity1.getContactName().compareTo(entity2.getContactName());
                }
            });
        }
    }

    private String getContactName(String contactID) {
        String[] projection = new String[]{ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER};
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone._ID + " = ?", new String[]{contactID}, Phone.DISPLAY_NAME + " ASC");
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            }
        }
        if (cursor != null) cursor.close();
        return null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_button:
                launchActivity(R.id.add_button);
                break;
            case R.id.deleteImage:
                if (deleteList != null && deleteList.size() > 0) {
                    confirmAndDelete();
                }
                break;
            case R.id.backButton:
                if (remainderListAdapter != null) {
                    remainderListAdapter.notifyDataSetChanged(listData);
                }
                setupTitle();
                break;
        }
    }

    private void confirmAndDelete() {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.delete_confirmation)).setTitle(getResources().getString(R.string.notefy_app_name));
        builder.setPositiveButton(getResources().getString(R.string.yes_option), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteRemainders();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.no_option), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void deleteRemainders() {
        for (RemainderListEntity remainderListEntity : deleteList) {
            context.getContentResolver().delete(RemainderDAO.CONTENT_URI, RemainderDAO.Columns.CONTACT_ID + " = ? ", new String[]{remainderListEntity.getContactID()});
        }
        refreshData();
        setupTitle();
    }

    private void launchActivity(int activityID) {
        Intent intent = null;
        switch (activityID) {
            case R.id.add_button:
                intent = new Intent(this, ContactListActivity.class);
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    private void setupTitle() {
        if (mToolBarBack != null && mDeleteImage != null && mTitle != null) {
            mToolBarBack.setVisibility(View.GONE);
            mDeleteImage.setVisibility(View.GONE);
            appIcon.setVisibility(View.GONE);
            mTitle.setText(getResources().getString(R.string.notefy_app_name));
            mTitle.setTextColor(getResources().getColor(R.color.white));
            setSupportActionBar(toolbar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.action_settings) {
            intent = new Intent(context, SettingsActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.about) {
            showAboutDialog();
        } else if (id == R.id.rate_us) {
            launchRateUs();
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchRateUs() {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAboutDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).setPositiveButton(getResources().getString(R.string.dismiss_dialog),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).create();

        alertDialog.setTitle(R.string.notefy_app_name);
        alertDialog.setMessage(getResources().getString(R.string.copyright_message) + "\n\n" + getResources().getString(R.string.notefy_mail));
        //alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.show();
    }

    @Override
    public void onDeleteClick(List<RemainderListEntity> deleteItems) {
        deleteList = new ArrayList<>();
        if (toolbar != null && deleteItems != null) {
            deleteList.addAll(deleteItems);
            if (deleteItems.size() < 1) {
                setupTitle();
            } else {
                setUpDeleteToolBar(deleteItems);
            }
        }
    }

    private void setUpDeleteToolBar(List<RemainderListEntity> deleteItems) {
        if (mToolBarBack != null && mDeleteImage != null && mTitle != null) {
            mToolBarBack.setVisibility(View.VISIBLE);
            mDeleteImage.setVisibility(View.VISIBLE);
            appIcon.setVisibility(View.GONE);
            mTitle.setText(getResources().getString(R.string.notefy_app_name) + " (" + deleteItems.size() + ")");
        }
    }

}
