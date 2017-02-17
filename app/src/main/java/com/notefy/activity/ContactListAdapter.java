package com.notefy.activity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.notefy.R;
import com.notefy.application.IApplicationConstant;
import com.notefy.entity.ContactListEntity;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> implements OnClickListener {

	List<ContactListEntity> listData;
	Context context;
	
	public static class ViewHolder extends RecyclerView.ViewHolder {

		TextView contactName;
		TextView imageText;
		TextView contactID;
		CircleImageView imageCircle; 
		RelativeLayout parentLayout;
		RelativeLayout imageLayout;
		
		public ViewHolder(View itemView) {
			super(itemView);
			contactName = (TextView) itemView.findViewById(R.id.contact_namec);
			imageText = (TextView) itemView.findViewById(R.id.contact_image_textc);
			contactID = (TextView) itemView.findViewById(R.id.contact_idc);
			imageCircle = (CircleImageView) itemView.findViewById(R.id.contact_image_imagec);
			parentLayout = (RelativeLayout) itemView.findViewById(R.id.parent_relative_layoutc);
			imageLayout = (RelativeLayout) itemView.findViewById(R.id.contact_image_layoutc);
		}
		
	}
	
	public ContactListAdapter(List<ContactListEntity> listData, Context context) {
		this.listData = listData;
		this.context = context;
	}
	
	@Override
	public int getItemCount() {
		return listData.size();
	}
	
	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		if (holder.contactName != null) holder.contactName.setText(listData.get(position).getContactName());
		if (holder.contactID != null) holder.contactID.setText(listData.get(position).getContactID());
		if (holder.imageLayout != null) holder.imageLayout.setTag(position);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				holder.parentLayout.setOnTouchListener(new View.OnTouchListener() {
					@SuppressLint("ClickableViewAccessibility")
					@TargetApi(Build.VERSION_CODES.LOLLIPOP)
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						v
						.findViewById(R.id.ripple_view)
						.getBackground()
						.setHotspot(event.getX(), event.getY());
						return(false);
					}
				});
			}
		InputStream photoStream = getPhotoUri(listData.get(position).getContactID());
		if(photoStream != null){
			Bitmap bitmap = BitmapFactory.decodeStream(photoStream);
			if (holder.imageCircle != null) holder.imageCircle.setImageBitmap(bitmap);
			if (holder.imageText != null) holder.imageText.setVisibility(View.GONE);
		} else {
			ColorDrawable cd;
			if(position % 10 == 1 || position % 10 == 6) {
				cd = new ColorDrawable(Color.parseColor("#B39DDB"));
			} else if(position % 10 == 2 || position % 10 == 7) {
				cd = new ColorDrawable(Color.parseColor("#A5D6A7"));
			} else if(position % 10 == 3 || position % 10 == 8) {
				cd = new ColorDrawable(Color.parseColor("#FF8A65"));
			} else if(position % 10 == 4 || position % 10 == 9) {
				cd = new ColorDrawable(Color.parseColor("#80CBC4"));
			} else {
				cd = new ColorDrawable(Color.parseColor("#F48FB1"));
			}
			if (holder.imageCircle != null) holder.imageCircle.setImageDrawable(cd);
			if (holder.imageText != null) holder.imageText.setVisibility(View.VISIBLE);
			if (holder.imageText != null) holder.imageText.setText(listData.get(position).getContactName().substring(0, 1));
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int position) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_list_layout, viewGroup, false);
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchAddRemainderActivity(listData.get(position).getContactID());
			}
		});
		return new ViewHolder(v);
	}
	
	private void launchAddRemainderActivity(String contactID) {
		Intent intent = new Intent(context, AddRemainderActivity.class);
		intent.putExtra(IApplicationConstant.CONTACT_DETAILS, contactID);
		context.startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		// Do Nothing...
	}
	
	public InputStream getPhotoUri(String contactID) {
	    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactID));
	    Uri photo = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

	    Cursor cursor = context.getContentResolver().query(photo, new String[] {Contacts.Photo.PHOTO}, null, null, null);
	       
	    if (cursor == null) {
	    	return null;
	    }
	    try {
	    	if (cursor.moveToFirst()) {
	    		byte[] data = cursor.getBlob(0);
	    		if (data != null) {
	    			return new ByteArrayInputStream(data);
	    		}
	    	}
	    } finally {
	    	cursor.close();
	    }
	    return null;
	}

}