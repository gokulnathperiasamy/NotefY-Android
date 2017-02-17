package com.notefy.database;

import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class RemainderDAO extends BaseDAO {
	
	protected static final String TABLE_NAME = BaseDAO.TABLE_REMAINDER;
	protected static final String AUTHORITY = BaseDAO.TABLE_REMAINDER_AUTHORITY;
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    
    protected static final int REMAINDER_LIST = 1;
    protected static final int REMAINDER_ITEM = 2;
    
    protected static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/notefy.notefyDB/" + TABLE_NAME;
    protected static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/notefy/notefyDB" + TABLE_NAME;

    public class Columns {
		public static final String CONTACT_ID = "contact_id";
		public static final String REMAINDER_MESSAGE = "remainder_message";
	}
	
	protected static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " 
			+ TABLE_NAME
			+ "(" 
			+ BaseDAO.Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
			+ Columns.CONTACT_ID + " TEXT NOT NULL, " 
			+ Columns.REMAINDER_MESSAGE + " TEXT  " 
			+ ");";
	
	protected static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	protected static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}
}
