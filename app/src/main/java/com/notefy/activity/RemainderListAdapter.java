package com.notefy.activity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
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
import com.notefy.entity.RemainderListEntity;

public class RemainderListAdapter extends RecyclerView.Adapter<RemainderListAdapter.ViewHolder> implements OnClickListener {

	private List<RemainderListEntity> listData;
	private Context context;
	protected static String CONTACT_NAME = "ContactName";
	private OnDeleteClickListener onDeleteClickListener;
	private List<RemainderListEntity> selected;
	
	public OnDeleteClickListener getOnDeleteClickListener() {
		return onDeleteClickListener;
	}

	public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) {
		this.onDeleteClickListener = onDeleteClickListener;
	}
	
	public void notifyDataSetChanged(List<RemainderListEntity> data) {
		this.listData = data;
		if (selected!=null) {
			selected.clear();
		}
		notifyDataSetChanged();
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {

		TextView contactName;
		TextView message;
		TextView imageText;
		TextView messageNumber;
		TextView contactID;
		CircleImageView imageCircle; 
		RelativeLayout parentLayout;
		RelativeLayout imageLayout;
		
		public ViewHolder(View itemView) {
			super(itemView);
			contactName = (TextView) itemView.findViewById(R.id.contact_name1);
			message = (TextView) itemView.findViewById(R.id.message);
			imageText = (TextView) itemView.findViewById(R.id.contact_image_text);
			messageNumber = (TextView) itemView.findViewById(R.id.message_number);
			contactID = (TextView) itemView.findViewById(R.id.contact_id1);
			imageCircle = (CircleImageView) itemView.findViewById(R.id.contact_image_image);
			parentLayout = (RelativeLayout) itemView.findViewById(R.id.parent_relative_layout);
			imageLayout = (RelativeLayout) itemView.findViewById(R.id.contact_image_layout);
		}
	}
	
	public RemainderListAdapter(List<RemainderListEntity> listData, Context context) {
		this.listData = listData;
		this.context = context;
		this.selected = new ArrayList<RemainderListEntity>();
	}
	
	@Override
	public int getItemCount() {
		return listData.size();
	}
	
	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@SuppressLint("NewApi")
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
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
		
		if (holder.contactName != null) holder.contactName.setText(listData.get(position).getContactName());
		List<String> remainderMessage = listData.get(position).getRemainderMessage();
		String displayMessage = "";
		for (String rm : remainderMessage) {
			displayMessage = rm;
			break;
		}
		if (remainderMessage.size() > 1) {
			displayMessage += " ...";
		}
		if (holder.message != null) holder.message.setText(displayMessage);
		if (holder.contactID != null) holder.contactID.setText(listData.get(position).getContactID());
		if (holder.imageLayout != null) holder.imageLayout.setTag(position);
		if(selected != null) {
			if(selected.contains(listData.get(position))) {
				if (holder.parentLayout != null) holder.parentLayout.findViewById(R.id.ripple_view).setBackgroundColor(context.getResources().getColor(R.color.selected_grey));
				if (holder.imageCircle != null) holder.imageCircle.setImageResource(R.mipmap.ic_navigation_check);
				if (holder.imageText != null) holder.imageText.setVisibility(View.GONE);
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					if (holder.parentLayout != null) holder.parentLayout.findViewById(R.id.ripple_view).setBackground(context.getResources().getDrawable(R.drawable.ripple, null));
				} else {
					if (holder.parentLayout != null) holder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
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
		}
		if (holder.messageNumber != null) holder.messageNumber.setText(listData.get(position).getNumberOfMessages());
		if (holder.imageLayout != null) holder.imageLayout.setOnClickListener(this);
		if (holder.parentLayout != null) holder.parentLayout.setOnClickListener(this);
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

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int position) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.remainder_list_layout, viewGroup, false);
		return new ViewHolder(v);
	}
	
	private void launchAddRemainderActivity(String contactID) {
		Intent intent = new Intent(context, AddRemainderActivity.class);
		intent.putExtra(IApplicationConstant.CONTACT_DETAILS, contactID);
		intent.putExtra(IApplicationConstant.REMAINDER_ADDED, true);
		context.startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.parent_relative_layout:
				launchAddRemainderActivity(((TextView) v.findViewById(R.id.contact_id1)).getText().toString());
				break;
			case R.id.contact_image_layout:
				toggleItemSelection(v);
				break;
		}
	}

	@SuppressLint("NewApi")
	private void toggleItemSelection(View v) {
		View parentRelative = (View) v.getParent();
		if (onDeleteClickListener!=null) {
			if(selected != null){
				if(selected.contains(listData.get(Integer.parseInt(v.getTag().toString())))){
					selected.remove(listData.get(Integer.parseInt(v.getTag().toString())));
					View rippleView = parentRelative.findViewById(R.id.ripple_view);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						rippleView.setBackground(context.getResources().getDrawable(R.drawable.ripple, null));
					} else {
						rippleView.setBackgroundColor(context.getResources().getColor(R.color.white));
					}
					CircleImageView image = (CircleImageView) v.findViewById(R.id.contact_image_image);
					int position = Integer.parseInt(v.getTag().toString());
					InputStream photoStream = getPhotoUri(listData.get(position).getContactID());
					if(photoStream != null && image != null){
						Bitmap bitmap = BitmapFactory.decodeStream(photoStream);
						image.setImageBitmap(bitmap);
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
						image.setImageDrawable(cd);
					}
					TextView text = (TextView) v.findViewById(R.id.contact_image_text);
					text.setVisibility(View.VISIBLE);
				}
				else {
					selected.add(listData.get(Integer.parseInt(v.getTag().toString())));
					View rippleView = parentRelative.findViewById(R.id.ripple_view);
					rippleView.setBackgroundColor(context.getResources().getColor(R.color.selected_grey));
					CircleImageView image = (CircleImageView) v.findViewById(R.id.contact_image_image);
					image.setImageResource(R.mipmap.ic_navigation_check);
					TextView text = (TextView) v.findViewById(R.id.contact_image_text);
					text.setVisibility(View.GONE);
				}
			}
			onDeleteClickListener.onDeleteClick(selected);
		}
	}

}