package com.kai_jan_57.opendsbmobile.network;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProtocolConstants {
    // parse
    public static final String RESULT_CODE = "Resultcode";
    public static final String MANDANT_ID = "MandantId";
    public static final String RESULT_MENU_ITEMS = "ResultMenuItems";
    public static final String INDEX = "Index";
    public static final String TITLE = "Title";
    public static final String CHILDREN = "Childs";
    public static final String NEW_COUNT = "NewCount";
    public static final String METHOD_NAME = "MethodName";
    public static final String SAVE_LAST_STATE = "SaveLastState";
    public static final String ICON_LINK = "IconLink";
    public static final String ROOT = "Root";
    public static final String TAGS = "Tags";
    public static final String DETAIL_STRING = "Detail";
    public static final String CONTENT_TYPE = "ConType";
    public static final String PRIORITY = "Prio";
    public static final String CONTENT_ID = "Id";
    public static final String DATE = "Date";
    public static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
    public static final String PREVIEW_URL = "Preview";

    public static char URL_SEPARATOR_CHAR = '/';

    // send
    static final SimpleDateFormat DATE_ENCODER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssSSSSS", Locale.US);
    static final String LAST_UPDATE = "LastUpdate";
    static final String BUNDLE_ID = "BundleId";
    static final String LOGIN_ID = "UserId";
    static final String LOGIN_PASSWORD = "UserPw";
}
