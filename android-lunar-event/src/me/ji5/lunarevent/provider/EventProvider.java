package me.ji5.lunarevent.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import me.ji5.utils.Log;

public class EventProvider extends ContentProvider implements EventDbConstants {
	protected static final String TAG = "EventProvider";
	
    protected SQLiteDatabase mDb = null;
    protected EventDbHelper mDbHelper = null;

	// public constants for client development
    public static final String AUTHORITY = "me.ji5.lunarevent";
	public static final Uri EVENT_URI = Uri.parse("content://" + AUTHORITY + "/" + Events.CONTENT_PATH);;

	// helper constants for use with the UriMatcher
    protected static final int EVENT_LIST = 1;
    protected static final int EVENT_ID = 2;

    protected static final UriMatcher URI_MATCHER;

	/**
	* Column and content type definitions for the Provider.
	*/
	public static interface Events extends BaseColumns {
	    public static final String CONTENT_PATH = "events";
	    public static final String CONTENT_POSTFIX = "/vnd.me.ji5.lunarevent";
	    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_POSTFIX;
	    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_POSTFIX;
        public static final String[] PROJECTION_ALL = { _ID,
                FIELD_EVENT_ID, FIELD_EVENT_TITLE, FIELD_EVENT_DESCRIPTION, FIELD_EVENT_START, FIELD_EVENT_END,
                FIELD_EVENT_CALENDAR_ID, FIELD_EVENT_LOCATION,
                FIELD_UPDATED_AT, FIELD_CREATED_AT
        };
	    public static final String SORT_ORDER_DEFAULT = FIELD_CREATED_AT + " ASC";
	}

	// prepare the UriMatcher
	static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, Events.CONTENT_PATH, EVENT_LIST);
        URI_MATCHER.addURI(AUTHORITY, Events.CONTENT_PATH + "/#", EVENT_ID);
	}
	
	@Override
	public boolean onCreate() {
	    mDbHelper = new EventDbHelper(getContext());
	    mDb = mDbHelper.getWritableDatabase();
	    
        if (mDb == null) {
            return false;
        }
        
        if (mDb.isReadOnly()) {
            mDb.close();
            mDb = null;

            return false;
        }
	      
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
       switch (URI_MATCHER.match(uri)) {
          case EVENT_LIST:
             return Events.CONTENT_TYPE;
          case EVENT_ID:
             return Events.CONTENT_ITEM_TYPE;
          default:
             throw new IllegalArgumentException("Unsupported URI: " + uri);
       }
	}

    public String getTableName(Uri uri) {
       switch (URI_MATCHER.match(uri)) {
          case EVENT_LIST:
             return TABLE_EVENT;
          case EVENT_ID:
             return TABLE_EVENT;
          default:
             throw new IllegalArgumentException("Unsupported URI: " + uri);
       }
    }
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
	    if (mDb == null || mDb.isReadOnly()) {
	        Log.e("Database is NULL or READ-ONLY!!!");
	        return null;
	    }
	    
        if (URI_MATCHER.match(uri) != EVENT_LIST) {
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }

        try {
            long id = mDb.insertOrThrow(getTableName(uri), null, values);
            if (id >= 0) {
                // notify all listeners of changes and return itemUri:
                Uri itemUri = ContentUris.withAppendedId(uri, id);
                getContext().getContentResolver().notifyChange(itemUri, null);
                return itemUri;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // s.th. went wrong:
        throw new SQLException("Problem while inserting into " + getTableName(uri) + ", uri: " + uri); // use another exception here!!!
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (mDb == null || mDb.isReadOnly()) {
            Log.e("Database is NULL or READ-ONLY!!!");
            return 0;
        }
	    
	    int delCount = 0;

        switch (URI_MATCHER.match(uri)) {
            case EVENT_LIST:
                delCount = mDb.delete(getTableName(uri), selection, selectionArgs);
                break;
                
            case EVENT_ID:
                String idStr = uri.getLastPathSegment();
                String where = FIELD_EVENT_ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                   where += " AND " + selection;
                }
                delCount = mDb.delete(getTableName(uri), where, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        
        // notify all listeners of changes:
        if (delCount > 0) {
           getContext().getContentResolver().notifyChange(uri, null);
        }        
        
        return delCount;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (mDb == null) {
            Log.e("Database is NULL!!!");
            return null;
        }
        
	   SQLiteQueryBuilder builder = new SQLiteQueryBuilder(); 
	   builder.setTables(getTableName(uri));
	   
	   switch (URI_MATCHER.match(uri)) {
            case EVENT_LIST:
                // all nice and well
                break;
            case EVENT_ID:
                // limit query to one row at most:
                builder.appendWhere(BaseColumns._ID + " = " + uri.getLastPathSegment());
                
                if (TextUtils.isEmpty(sortOrder)) { 
                    sortOrder = FIELD_CREATED_AT;
                }
                break;
            
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        
        Cursor cursor = builder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder); 
        // if we want to be notified of any changes: 
        cursor.setNotificationUri(getContext().getContentResolver(), uri); 

        return cursor; 
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (mDb == null || mDb.isReadOnly()) {
            Log.e("Database is NULL or READ-ONLY!!!");
            return 0;
        }
	    
	    int updateCount = 0;
	    
        switch (URI_MATCHER.match(uri)) {
            case EVENT_LIST:
                updateCount = mDb.update(getTableName(uri), values, selection, selectionArgs);
                break;
            case EVENT_ID:
                String idStr = uri.getLastPathSegment();
                String where = BaseColumns._ID + " = " + idStr; 
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = mDb.update(getTableName(uri), values, where, selectionArgs); 
                break;
            
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        
        // notify all listeners of changes:
        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        
        return updateCount;
	}
}
