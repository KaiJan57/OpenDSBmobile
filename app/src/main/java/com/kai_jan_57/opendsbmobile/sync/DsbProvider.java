package com.kai_jan_57.opendsbmobile.sync;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kai_jan_57.opendsbmobile.database.AppDatabase;
import com.kai_jan_57.opendsbmobile.database.Login;
import com.kai_jan_57.opendsbmobile.database.Node;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public class DsbProvider extends ContentProvider {

    private static final String AUTHORITY = "com.kai_jan_57.opendsbmobile.provider";

    private static final String BASE_PATH = "dsbmobile";

    public enum URI_CODE {
        LOGINS("logins/*"),
        NODES("nodes/*");

        final String mMatch;

        URI_CODE(String match) {
            mMatch = "/" + match;
        }

        static URI_CODE fromCode(int code) {
            code -= 1;
            if (code < 0 || code > URI_CODE.values().length) {
                return null;
            }
            return URI_CODE.values()[code];
        }

        int getCode() {
            return this.ordinal() + 1;
        }
    }

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI(AUTHORITY, BASE_PATH + URI_CODE.LOGINS.mMatch, URI_CODE.LOGINS.getCode());
        MATCHER.addURI(AUTHORITY, BASE_PATH + URI_CODE.NODES.mMatch, URI_CODE.NODES.getCode());
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final URI_CODE code = URI_CODE.fromCode(MATCHER.match(uri));
        if (code != null) {
            final Context context = getContext();
            if (context == null) {
                return null;
            }
            Login.Dao login = AppDatabase.getInstance(context).getLoginDao();
            Node.Dao node = AppDatabase.getInstance(context).getNodeDao();
            final Cursor cursor;
            switch (code) {
                case LOGINS: {
                    cursor = login.selectAll();
                    break;
                }
                case NODES: {
                    cursor = node.selectAll();
                }
                default: {
                    throw unknownUriException(uri);
                }
            }
            cursor.setNotificationUri(context.getContentResolver(), uri);
            return cursor;
        }
        throw unknownUriException(uri);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        URI_CODE code = URI_CODE.fromCode(MATCHER.match(uri));
        if (code != null) {
            switch (code) {
                case LOGINS:
                    return AUTHORITY + '.' + Login.TABLE_NAME;
                case NODES:
                    return AUTHORITY + '.' + Node.TABLE_NAME;
            }
        }
        throw unknownUriException(uri);
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        URI_CODE code = URI_CODE.fromCode(MATCHER.match(uri));
        if (code != null) {
            final Context context = getContext();
            if (context == null) {
                return null;
            }
            switch (code) {
                case LOGINS: {
                    final long id = AppDatabase.getInstance(context).getLoginDao().addLogin(new Login(values));
                    context.getContentResolver().notifyChange(uri, null);
                    return ContentUris.withAppendedId(uri, id);
                }
                case NODES: {
                    final long id = AppDatabase.getInstance(context).getNodeDao().addNode(new Node(values));
                    context.getContentResolver().notifyChange(uri, null);
                    return ContentUris.withAppendedId(uri, id);
                }
            }
        }
        throw unknownUriException(uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        URI_CODE code = URI_CODE.fromCode(MATCHER.match(uri));
        if (code != null) {
            final Context context = getContext();
            if (context == null) {
                return 0;
            }
            switch (code) {
                case LOGINS: {
                    final int count = AppDatabase.getInstance(context).getLoginDao()
                            .deleteId(ContentUris.parseId(uri));
                    context.getContentResolver().notifyChange(uri, null);
                    return count;
                }
                case NODES: {
                    final int count = AppDatabase.getInstance(context).getNodeDao()
                            .deleteId(ContentUris.parseId(uri));
                    context.getContentResolver().notifyChange(uri, null);
                    return count;
                }
            }
        }
        throw unknownUriException(uri);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        URI_CODE code = URI_CODE.fromCode(MATCHER.match(uri));
        if (code != null) {
            final Context context = getContext();
            if (context == null) {
                return 0;
            }
            switch (code) {
                case LOGINS: {
                    final Login login = new Login(values);
                    login.mId = ContentUris.parseId(uri);
                    final int count = AppDatabase.getInstance(context).getLoginDao()
                            .updateLogin(login);
                    context.getContentResolver().notifyChange(uri, null);
                    return count;
                }
                case NODES: {
                    final Node node = new Node(values);
                    node.mId = ContentUris.parseId(uri);
                    final int count = AppDatabase.getInstance(context).getNodeDao()
                            .updateNode(node);
                    context.getContentResolver().notifyChange(uri, null);
                    return count;
                }
            }
        }
        throw unknownUriException(uri);
    }

    private IllegalArgumentException unknownUriException(Uri uri) {
        return new IllegalArgumentException("Unknown URI: " + uri);
    }
}
