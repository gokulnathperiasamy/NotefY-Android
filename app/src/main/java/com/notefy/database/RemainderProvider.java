package com.notefy.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class RemainderProvider extends BaseProvider {
	
	static {
        uriMatcher.addURI(RemainderDAO.AUTHORITY, RemainderDAO.TABLE_NAME, RemainderDAO.REMAINDER_LIST);
        uriMatcher.addURI(RemainderDAO.AUTHORITY, RemainderDAO.TABLE_NAME + "/#", RemainderDAO.REMAINDER_ITEM);
    }
	
	@Override
	public boolean onCreate() {
		baseDBHelper = new BaseDBHelper(getContext(), BaseDAO.DB_NAME , null, BaseDAO.DB_VERSION);
        sqLiteDatabase = baseDBHelper.getWritableDatabase();
        if (sqLiteDatabase == null) {
            return false;
        } else if (sqLiteDatabase.isReadOnly()) {
        	sqLiteDatabase.close();
        	sqLiteDatabase = null;
            return false;
        }
        return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
		sqLiteQueryBuilder.setTables(RemainderDAO.TABLE_NAME);
        switch (uriMatcher.match(uri)) {
            case RemainderDAO.REMAINDER_LIST:
                break;
            case RemainderDAO.REMAINDER_ITEM:
            	sqLiteQueryBuilder.appendWhere(BaseDAO.Columns._ID + " = "+ uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }
        return sqLiteQueryBuilder.query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
	        case RemainderDAO.REMAINDER_LIST:
	            return RemainderDAO.CONTENT_TYPE;
	        case RemainderDAO.REMAINDER_ITEM:
	            return RemainderDAO.CONTENT_ITEM_TYPE;
	        default:
	            throw new IllegalArgumentException("Invalid URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentValues) {
		if (uriMatcher.match(uri) != RemainderDAO.REMAINDER_LIST) {
			throw new IllegalArgumentException("Invalid URI: " + uri);
		}
		long id = sqLiteDatabase.insert(RemainderDAO.TABLE_NAME, null, contentValues);
		if (id>0) {
			return ContentUris.withAppendedId(uri, id);
		}
		throw new SQLException("Error inserting into table: " + RemainderDAO.TABLE_NAME);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int deleted = 0;
        switch (uriMatcher.match(uri)) {
            case RemainderDAO.REMAINDER_LIST:
            	sqLiteDatabase.delete(RemainderDAO.TABLE_NAME, selection, selectionArgs);
                break;
            case RemainderDAO.REMAINDER_ITEM:
                String where = BaseDAO.Columns._ID + " = " + uri.getLastPathSegment();
                if (!selection.isEmpty()) {
                    where += " AND "+selection;
                }
                deleted = sqLiteDatabase.delete(RemainderDAO.TABLE_NAME, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: "+uri);
        }
        return deleted;
	}

	@Override
	public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
		int updated = 0;
        switch (uriMatcher.match(uri)) {
            case RemainderDAO.REMAINDER_LIST:
            	sqLiteDatabase.update(RemainderDAO.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case RemainderDAO.REMAINDER_ITEM:
                String where = BaseDAO.Columns._ID + " = " + uri.getLastPathSegment();
                if (!selection.isEmpty()) {
                    where += " AND " + selection;
                }
                updated = sqLiteDatabase.update(RemainderDAO.TABLE_NAME,contentValues, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: "+uri);
        }
        return updated;
	}
}
