package com.kai_jan_57.opendsbmobile.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.kai_jan_57.opendsbmobile.Application;
import com.kai_jan_57.opendsbmobile.database.converters.DateConverter;
import com.kai_jan_57.opendsbmobile.network.RequestSenderTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.kai_jan_57.opendsbmobile.network.ProtocolConstants.URL_SEPARATOR_CHAR;

@Entity(tableName = Node.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(entity = Login.class,
                        parentColumns = Login.COLUMN_ID,
                        childColumns = Node.COLUMN_LOGIN_ID,
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(entity = Node.class,
                        parentColumns = Node.COLUMN_ID,
                        childColumns = Node.COLUMN_PARENT_ID,
                        onDelete = ForeignKey.CASCADE
                )
        })
public class Node {

    public static final String TABLE_NAME = "cache";

    static final String COLUMN_ID = BaseColumns._ID;
    static final String COLUMN_LOGIN_ID = "login_id";
    static final String COLUMN_PARENT_ID = "parent_id";
    private static final String COLUMN_ITEM_ID = "item_id";
    private static final String COLUMN_ITEM_INDEX = "item_index";
    private static final String COLUMN_METHOD = "method";
    private static final String COLUMN_CONTENT_TYPE = "content_type";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_CONTENT_CACHE_DATE = "content_cache_date";
    private static final String COLUMN_PREVIEW_URL = "preview_url";
    private static final String COLUMN_PREVIEW_CACHE_DATE = "preview_cache_date";
    private static final String COLUMN_PREVIEW_CACHE_RESOLUTION = "preview_cache_resolution";
    private static final String COLUMN_PRIORITY = "priority";
    private static final String COLUMN_TAGS = "tags";
    private static final String COLUMN_NEW_COUNT = "new_count";
    private static final String COLUMN_SAVE_LAST_STATE = "save_last_state";

    private static final String PREVIEW_CACHE_FILE_PREFIX = "preview_";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ID)
    public Long mId;

    @ColumnInfo(index = true, name = COLUMN_LOGIN_ID)
    public
    long mLoginId;

    @ColumnInfo(index = true, name = COLUMN_PARENT_ID)
    public Long mParentId;

    @ColumnInfo(name = COLUMN_ITEM_ID)
    public String mItemId;

    @ColumnInfo(name = COLUMN_ITEM_INDEX)
    public Integer mItemIndex;

    @ColumnInfo(name = COLUMN_METHOD)
    @TypeConverters(Method.class)
    public Method mMethod;

    @ColumnInfo(name = COLUMN_CONTENT_TYPE)
    @TypeConverters(ContentType.class)
    public ContentType mContentType;

    @ColumnInfo(name = COLUMN_DATE)
    @TypeConverters(DateConverter.class)
    public Date mDate;

    @ColumnInfo(name = COLUMN_TITLE)
    public String mTitle;

    @ColumnInfo(name = COLUMN_CONTENT)
    public String mContent;

    @ColumnInfo(name = COLUMN_CONTENT_CACHE_DATE)
    @TypeConverters(DateConverter.class)
    public Date mContentCacheDate;

    @ColumnInfo(name = COLUMN_PREVIEW_URL)
    public String mPreviewUrl;

    @ColumnInfo(name = COLUMN_PREVIEW_CACHE_DATE)
    @TypeConverters(DateConverter.class)
    public Date mPreviewCacheDate;

    @ColumnInfo(name = COLUMN_PREVIEW_CACHE_RESOLUTION)
    public Integer mPreviewCacheResolution;

    @ColumnInfo(name = COLUMN_PRIORITY)
    public Integer mPriority;

    @ColumnInfo(name = COLUMN_TAGS)
    public String mTags;

    @ColumnInfo(name = COLUMN_NEW_COUNT)
    public Integer mNewCount;

    @ColumnInfo(name = COLUMN_SAVE_LAST_STATE)
    public boolean mSaveLastState;

    @Ignore
    public Node(long loginId) {
        this(loginId, null);
    }

    public Node(long loginId, Long parentId) {
        mLoginId = loginId;
        mParentId = parentId;
    }

    public Node(ContentValues values) {
        if (values != null) {
            if (values.containsKey(COLUMN_ID)) {
                mId = values.getAsLong(COLUMN_ID);
            }
            if (values.containsKey(COLUMN_LOGIN_ID)) {
                mLoginId = values.getAsLong(COLUMN_LOGIN_ID);
            }
            if (values.containsKey(COLUMN_PARENT_ID)) {
                mParentId = values.getAsLong(COLUMN_PARENT_ID);
            }
            if (values.containsKey(COLUMN_ITEM_ID)) {
                mItemId = values.getAsString(COLUMN_ITEM_ID);
            }
            if (values.containsKey(COLUMN_ITEM_INDEX)) {
                mItemIndex = values.getAsInteger(COLUMN_ITEM_INDEX);
            }
            if (values.containsKey(COLUMN_METHOD)) {
                mMethod = Method.toMethod(values.getAsInteger(COLUMN_METHOD));
            }
            if (values.containsKey(COLUMN_CONTENT_TYPE)) {
                mContentType = ContentType.toContentType(values.getAsInteger(COLUMN_CONTENT_TYPE));
            }
            if (values.containsKey(COLUMN_DATE)) {
                mDate = DateConverter.toDate(values.getAsLong(COLUMN_DATE));
            }
            if (values.containsKey(COLUMN_TITLE)) {
                mTitle = values.getAsString(COLUMN_TITLE);
            }
            if (values.containsKey(COLUMN_CONTENT)) {
                mContent = values.getAsString(COLUMN_CONTENT);
            }
            if (values.containsKey(COLUMN_CONTENT_CACHE_DATE)) {
                mContentCacheDate = new Date(values.getAsLong(COLUMN_CONTENT_CACHE_DATE));
            }
            if (values.containsKey(COLUMN_PREVIEW_URL)) {
                mPreviewUrl = values.getAsString(COLUMN_PREVIEW_URL);
            }
            if (values.containsKey(COLUMN_PREVIEW_CACHE_DATE)) {
                mPreviewCacheDate = new Date(values.getAsLong(COLUMN_PREVIEW_CACHE_DATE));
            }
            if (values.containsKey(COLUMN_PREVIEW_CACHE_RESOLUTION)) {
                mPreviewCacheResolution = values.getAsInteger(COLUMN_PREVIEW_CACHE_RESOLUTION);
            }
            if (values.containsKey(COLUMN_PRIORITY)) {
                mPriority = values.getAsInteger(COLUMN_PRIORITY);
            }
            if (values.containsKey(COLUMN_TAGS)) {
                mTags = values.getAsString(COLUMN_TAGS);
            }
            if (values.containsKey(COLUMN_NEW_COUNT)) {
                mNewCount = values.getAsInteger(COLUMN_NEW_COUNT);
            }
            if (values.containsKey(COLUMN_SAVE_LAST_STATE)) {
                mSaveLastState = values.getAsBoolean(COLUMN_SAVE_LAST_STATE);
            }
        }
    }

    @NonNull
    public String toString() {
        return String.format("Node{mContent=\"%s\"}", mContent);
    }

    private static final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("ddMMyyyyHHmm", Locale.US);

    public String getPreviewUrl(int resolution) {
        return String.format("%sImage.ashx?bg=t&nopic=false&x=%s&f=%s&%s", RequestSenderTask.getHost(), String.valueOf(resolution), mPreviewUrl, mSimpleDateFormat.format(mDate));
    }

    public boolean isNewContentCacheRequired(Date newDate) {
        return mContentCacheDate == null || newDate == null || newDate.after(mContentCacheDate) || !getContentCache().exists();
    }

    public boolean isNewPreviewCacheRequired(Date newDate, int newResolution) {
        return mPreviewCacheDate == null || newDate == null || newDate.after(mPreviewCacheDate) || mPreviewCacheResolution == null || newResolution > mPreviewCacheResolution || !getPreviewCache().exists();
    }

    public File getCacheDir() {
        File result = new File(Application.getInstance().getFilesDir(), String.valueOf(mLoginId));
        if (!result.isDirectory() && !result.mkdirs()) {
            return null;
        }
        if (!result.setWritable(true)) {
            return null;
        }
        return result;
    }

    private String prepareCacheString(String input) {
        return mItemId + '_' + input.substring(input.lastIndexOf(URL_SEPARATOR_CHAR) + 1);
    }

    public File getContentCache() {
        //return new File(getCacheDir(), FileUtils.encodeSafeFilename(mContent) + FileUtils.getExtension(mContent));
        return new File(getCacheDir(), prepareCacheString(mContent));
    }

    public File getPreviewCache() {
        //return new File(getCacheDir(), FileUtils.encodeSafeFilename(mPreviewUrl) + FileUtils.getExtension(mPreviewUrl));
        return new File(getCacheDir(), PREVIEW_CACHE_FILE_PREFIX + prepareCacheString(mPreviewUrl));
    }

    @androidx.room.Dao
    public interface Dao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        long addNode(Node node);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        long[] addAllNodes(List<Node> nodes);

        @Query("select * from " + TABLE_NAME)
        Cursor selectAll();

        @Query("select * from " + TABLE_NAME + " where " + COLUMN_ID + " = :id")
        Cursor getNode(long id);

        @Query("select * from " + TABLE_NAME + " where " + COLUMN_ID + " = :id")
        Node getNodeById(long id);

        @Query("select * from " + TABLE_NAME + " where " + COLUMN_LOGIN_ID + " = :loginId and " + COLUMN_TITLE + " = :title and " + COLUMN_METHOD + " = :method and " + COLUMN_CONTENT_TYPE + " = :type and " + COLUMN_ITEM_INDEX + " = :itemIndex and length(" + COLUMN_CONTENT + ") > 0")
        @TypeConverters({Method.class, ContentType.class})
        Node getEquivalentNode(long loginId, Method method, ContentType type, String title, int itemIndex);

        @Query("select * from " + TABLE_NAME + " where " + COLUMN_CONTENT + " = :content")
        Node getNodeByContent(String content);

        @Query("select " + COLUMN_ID + " from " + TABLE_NAME + " where " + COLUMN_LOGIN_ID + " = :loginId and " + COLUMN_METHOD + " = :method and " + COLUMN_PARENT_ID + " is null")
        @TypeConverters(Method.class)
        long[] getRootNodeIdsByMethod(long loginId, Method method);

        @Query("select count(*) from " + TABLE_NAME + " where " + COLUMN_LOGIN_ID + " = :loginId and " + COLUMN_METHOD + " = :method")
        @TypeConverters(Method.class)
        int getNodeCountByMethod(long loginId, Method method);

        @Query("select count(*) from " + TABLE_NAME + " where " + COLUMN_CONTENT + " = :content")
        int getNodeCountByContent(String content);

        @Query("select count(*) from " + TABLE_NAME + " where " + COLUMN_LOGIN_ID + " = :loginId")
        int getNodeCountByLogin(long loginId);

        @Query("select * from " + TABLE_NAME + " where " + COLUMN_LOGIN_ID + " = :loginId")
        List<Node> getNodesByLogin(long loginId);

        @Query("select * from " + TABLE_NAME + " where " + COLUMN_PARENT_ID + " = :parentId order by " + COLUMN_ITEM_INDEX)
        List<Node> getChildren(long parentId);

        @Query("select * from " + TABLE_NAME + " where " + COLUMN_PARENT_ID + " = :parentId and " + COLUMN_ITEM_INDEX + " = :childIndex")
        Node getChildByIndex(long parentId, int childIndex);

        @Query("select count(*) from " + TABLE_NAME + " where " + COLUMN_PARENT_ID + " = :parentId")
        int getChildCount(long parentId);


        @Query("delete from " + TABLE_NAME + " where " + COLUMN_ID + " = :id")
        int deleteId(long id);

        @Delete
        void delete(Node... nodes);

        @Query("delete from " + TABLE_NAME + " where " + COLUMN_LOGIN_ID + " = :loginId")
        void deleteAllNodesByLoginId(long loginId);

        @Query("delete from " + TABLE_NAME + " where " + COLUMN_LOGIN_ID + " = :loginId and " + COLUMN_ID + " not in (:nodesToKeep)")
        void clean(long loginId, List<Long> nodesToKeep);

        @Update(onConflict = OnConflictStrategy.REPLACE)
        int updateNode(Node node);

        @Update(onConflict = OnConflictStrategy.REPLACE)
        int updateNodes(List<Node> node);
    }

    public enum Method {
        Unknown,
        Timetables,
        Tiles,
        News,
        List,
        Settings,
        Feedback,
        About,
        Logout;

        public static Method fromMethodName(String methodName) {
            switch (methodName) {
                case "logout":
                    return Method.Logout;
                case "feedback":
                    return Method.Feedback;
                case "news":
                    return Method.News;
                case "timetable":
                    return Method.Timetables;
                case "about":
                    return Method.About;
                case "tiles":
                    return Method.Tiles;
                case "settings":
                    return Method.Settings;
                default:
                    return Method.Unknown;
            }
        }

        @TypeConverter
        public static Method toMethod(int method) {
            if (method > 0 && method < Method.values().length) {
                return Method.values()[method];
            } else {
                return null;
            }
        }

        @TypeConverter
        public static Integer toInt(Method method) {
            if (method == null) {
                return null;
            }
            return method.ordinal();
        }
    }

    public enum ContentType {
        NONE,
        FOLDER,
        SPOT,
        HTML,
        IMG,
        NEWS,
        URL,
        VIDEO,
        PDF,
        DSBPLAN,
        XAP;

        @TypeConverter
        public static ContentType toContentType(int contentType) {
            if (contentType > 0 && contentType < ContentType.values().length) {
                return ContentType.values()[contentType];
            } else {
                return ContentType.NONE;
            }
        }

        @TypeConverter
        public static Integer toInt(ContentType contentType) {
            if (contentType == null) {
                return null;
            }
            return contentType.ordinal();
        }
    }
}
