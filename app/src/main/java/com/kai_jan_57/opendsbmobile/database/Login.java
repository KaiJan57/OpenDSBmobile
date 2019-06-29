package com.kai_jan_57.opendsbmobile.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.kai_jan_57.opendsbmobile.database.converters.DateConverter;

import java.util.Date;
import java.util.List;

@Entity(tableName = Login.TABLE_NAME)
public class Login {

    public static final String TABLE_NAME = "logins";

    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_NAME = "name";
    private static final String COLUMN_ALIAS = "alias";
    private static final String COLUMN_LAST_UPDATE = "last_update";
    private static final String COLUMN_MANDANT_ID = "mandant_id";

    private static final String COLUMN_NOTIFICATIONS = "notifications_enabled";
    private static final String COLUMN_PARSER = "parser";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ID)
    public long mId;

    @ColumnInfo(name = COLUMN_NAME)
    public String mName;

    @ColumnInfo(name = COLUMN_ALIAS)
    public String mAlias;

    @ColumnInfo(name = COLUMN_LAST_UPDATE)
    @TypeConverters(DateConverter.class)
    public Date mLastUpdate;

    @ColumnInfo(name = COLUMN_MANDANT_ID)
    public String mMandantId;

    @ColumnInfo(name = COLUMN_NOTIFICATIONS)
    public boolean mNotificationsEnabled = true;

    @ColumnInfo(name = COLUMN_PARSER)
    public int mParser = 1;

    @Ignore
    public Login(String loginId) {
        this(loginId, loginId);
    }

    public Login(String name, String alias) {
        mName = name;
        mAlias = alias;
    }

    public Login(ContentValues values) {
        if (values != null) {
            if (values.containsKey(COLUMN_NAME) && values.containsKey(COLUMN_ALIAS)) {
                mName = values.getAsString(COLUMN_NAME);
                mAlias = values.getAsString(COLUMN_ALIAS);
            }
            if (values.containsKey(COLUMN_LAST_UPDATE)) {
                mLastUpdate = DateConverter.toDate(values.getAsLong(COLUMN_LAST_UPDATE));
            }
            if (values.containsKey(COLUMN_MANDANT_ID)) {
                mMandantId = values.getAsString(COLUMN_MANDANT_ID);
            }
            if (values.containsKey(COLUMN_NOTIFICATIONS)) {
                mNotificationsEnabled = values.getAsBoolean(COLUMN_NOTIFICATIONS);
            }
            if (values.containsKey(COLUMN_PARSER)) {
                mParser = values.getAsInteger(COLUMN_PARSER);
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return (mAlias.isEmpty() || mName.equals(mAlias)) ? mName : mAlias + String.format(" (%s)", mName);
    }

    @androidx.room.Dao
    public interface Dao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        long addLogin(Login login);

        @Query("select * from " + TABLE_NAME)
        Cursor selectAll();

        @Query("select * from " + TABLE_NAME + " where " + COLUMN_ID + " = :id")
        Cursor getLogins(long id);

        @Query("select * from " + TABLE_NAME + " where " + COLUMN_ID + " = :id")
        Login getLoginById(long id);

        @Query("select * from " + TABLE_NAME + " where " + COLUMN_NAME + " = :name")
        Login getLoginByName(String name);

        @Query("select count(*) from " + TABLE_NAME)
        int getCount();

        @Query("select " + COLUMN_ID + " from " + TABLE_NAME)
        List<Long> getIds();

        @Query("delete from " + TABLE_NAME + " where " + COLUMN_ID + " = :id")
        int deleteId(long id);

        @Delete
        void delete(Login... logins);

        @Query("delete from " + TABLE_NAME + " where not exists (select * from " + TABLE_NAME + " where " + COLUMN_NAME + " in (:retain))")
        void cleanUp(List<String> retain);

        @Query("delete from " + TABLE_NAME)
        void removeAllLogins();

        @Update(onConflict = OnConflictStrategy.REPLACE)
        int updateLogin(Login login);
    }

}
