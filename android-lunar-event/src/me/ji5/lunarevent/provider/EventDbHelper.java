package me.ji5.lunarevent.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import me.ji5.utils.Log;

public class EventDbHelper extends SQLiteOpenHelper implements EventDbConstants {
    public EventDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getTableEventSql_v1());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("onUpgrade:" + db.getVersion() + ", " + oldVersion + ", " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT);
        onCreate(db);
    }

    protected String getTableEventSql_v1() {
        StringBuilder sb_table = new StringBuilder();
        sb_table.append("CREATE TABLE ").append(TABLE_EVENT).append(" ( ")
                .append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ")
                .append(FIELD_EVENT_ID).append(" INTEGER, ")
                .append(FIELD_EVENT_TITLE).append(" TEXT, ")
                .append(FIELD_EVENT_DESCRIPTION).append(" TEXT, ")
                .append(FIELD_EVENT_START).append(" INTEGER, ")
                .append(FIELD_EVENT_END).append(" INTEGER, ")
                .append(FIELD_EVENT_LOCATION).append(" TEXT, ")
                .append(FIELD_EVENT_CALENDAR_ID).append(" INTEGER, ")
                .append(FIELD_EVENT_ALLDAY).append(" INTEGER, ")
                .append(FIELD_EVENT_HAS_ALARM).append(" INTEGER, ")
                .append(FIELD_EVENT_TIMEZONE).append(" TEXT, ")
                .append(FIELD_EVENT_CUSTOM_APP_PACKAGE).append(" TEXT, ")
                .append(FIELD_CREATED_AT).append(" INTEGER, ")
                .append(FIELD_UPDATED_AT).append(" INTEGER ")
                .append(");");
        return sb_table.toString();
    }

}
