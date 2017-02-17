package com.notefy.database;

import android.provider.BaseColumns;

public abstract class BaseDAO {
	
	protected static final String DB_NAME = "com.notefy.database";
	protected static final int DB_VERSION = 1;
	
	protected static final String TABLE_REMAINDER = "remainder";
	protected static final String TABLE_REMAINDER_AUTHORITY = "com.notefy.database";
	
	public class Columns {
		public static final String _ID = BaseColumns._ID;
	}

}
