package com.notefy.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseDBHelper extends SQLiteOpenHelper {

	public BaseDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	public BaseDBHelper(Context context) {
		super(context, BaseDAO.DB_NAME, null, BaseDAO.DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		RemainderDAO.onCreate(database);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		RemainderDAO.onUpgrade(database, oldVersion, newVersion);
	}

}
