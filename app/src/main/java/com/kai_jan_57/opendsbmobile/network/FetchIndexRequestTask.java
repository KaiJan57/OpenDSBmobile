package com.kai_jan_57.opendsbmobile.network;

import android.util.Log;

import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.Application;
import com.kai_jan_57.opendsbmobile.database.Login;
import com.kai_jan_57.opendsbmobile.database.Node;
import com.kai_jan_57.opendsbmobile.utils.FileUtils;
import com.kai_jan_57.opendsbmobile.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.kai_jan_57.opendsbmobile.network.RequestSenderTask.RequestType.GetData;

public class FetchIndexRequestTask extends RequestSenderTask {

    private final String TAG = getClass().getCanonicalName();

    private static boolean busy = false;
    private final FetchIndexEventListener mFetchIndexEventListener;
    private NodeUpdateEventListener mNodeUpdateEventListener;

    private final String mId;
    private final String mPassword;

    private final Login mLogin;

    private Date mDate;

    public FetchIndexRequestTask(FetchIndexEventListener fetchIndexEventListener, Login login, String id, String password) {
        mFetchIndexEventListener = fetchIndexEventListener;
        mId = id;
        mPassword = password;
        mLogin = login;
    }

    public void setNodeUpdateEventListener(NodeUpdateEventListener pNodeUpdateEventListener) {
        mNodeUpdateEventListener = pNodeUpdateEventListener;
    }

    public boolean execute() {
        if (!busy && mFetchIndexEventListener != null && mLogin != null) {
            busy = true;
            mDate = new Date();
            ExecuteRequestSenderTask(getJsonApiPath(), GetData);
            return true;
        }
        return false;
    }

    @Override
    JSONObject onSetupPacket() throws JSONException {
        JSONObject jsonObject = super.onSetupPacket();
        jsonObject.put(ProtocolConstants.DATE, ProtocolConstants.DATE_ENCODER.format(mDate));
        jsonObject.put(ProtocolConstants.LAST_UPDATE, ProtocolConstants.DATE_ENCODER.format(mDate));
        jsonObject.put(ProtocolConstants.BUNDLE_ID, Application.getInstance().getResources().getString(R.string.BUNDLE_ID));
        jsonObject.put(ProtocolConstants.LOGIN_ID, mId);
        jsonObject.put(ProtocolConstants.LOGIN_PASSWORD, mPassword);
        return jsonObject;
    }

    @Override
    void onProgress(int progress) {
        mFetchIndexEventListener.onProgress(progress);
    }

    @Override
    void onJsonParsed(JSONObject jsonObject) {
        busy = false;
        processJson(jsonObject);
    }

    private List<File> mCacheFilesToKeep;
    private List<Long> mNodeIdsToKeep;

    private List<Node> mUpdatedNodes = new ArrayList<>();
    private List<Node> mNewNodes = new ArrayList<>();
    private List<Node> mNotCachedNodes = new ArrayList<>();

    private void parseNodesRecursively(Node.Dao nodeDao, Login login, Node.Method method, JSONObject last, Long parentId) throws JSONException, ParseException {
        Node node = new Node(login.mId);
        node.mParentId = parentId;
        node.mItemId = last.getString(ProtocolConstants.CONTENT_ID);
        node.mItemIndex = last.getInt(ProtocolConstants.INDEX);
        node.mMethod = method;
        int contentType = last.getInt(ProtocolConstants.CONTENT_TYPE);
        if (contentType > 0 && contentType < Node.ContentType.values().length) {
            node.mContentType = Node.ContentType.values()[contentType];
        }
        String date = last.getString(ProtocolConstants.DATE);
        if (date.length() > 0) {
            node.mDate = (ProtocolConstants.DATE_PARSER.parse(date));
        }
        node.mTitle = last.getString(ProtocolConstants.TITLE);
        node.mContent = last.getString(ProtocolConstants.DETAIL_STRING);
        node.mPreviewUrl = last.getString(ProtocolConstants.PREVIEW_URL);
        node.mPriority = last.getInt(ProtocolConstants.PRIORITY);
        node.mTags = last.getString(ProtocolConstants.TAGS);
        if (last.has(ProtocolConstants.NEW_COUNT)) {
            node.mNewCount = last.getInt(ProtocolConstants.NEW_COUNT);
        }
        if (last.has(ProtocolConstants.SAVE_LAST_STATE)) {
            node.mSaveLastState = last.getBoolean(ProtocolConstants.SAVE_LAST_STATE);
        }

        UpdateType updateType = null;
        // copy cache information and store cache ids
        if (!node.mContent.isEmpty()) {
            Node equivalent = nodeDao.getNodeByContent(node.mContent);
            if (equivalent == null) {
                // second try
                equivalent = nodeDao.getEquivalentNode(login.mId, node.mMethod, node.mContentType, node.mTitle, node.mItemIndex);
            }
            if (equivalent != null) {
                // why not just check for preview date? Because one could click faster than a preview loads!
                boolean keep = false;
                if (equivalent.mContentCacheDate != null) {
                    keep = true;
                    node.mContentCacheDate = equivalent.mContentCacheDate;
                    mCacheFilesToKeep.add(equivalent.getContentCache());
                    if (equivalent.isNewContentCacheRequired(node.mDate)) {
                        // cache is deprecated
                        updateType = UpdateType.CACHE_DEPRECATED;
                    }
                } else {
                    // item was not cached before
                    updateType = UpdateType.NOT_CACHED;
                }
                if (equivalent.mPreviewCacheDate != null) {
                    keep = true;
                    node.mPreviewCacheDate = equivalent.mPreviewCacheDate;
                    node.mPreviewCacheResolution = equivalent.mPreviewCacheResolution;
                }
                if (keep) {
                    mCacheFilesToKeep.add(equivalent.getPreviewCache());
                }
            } else {
                // item is new
                updateType = UpdateType.NEW;
            }
        }

        node.mId = nodeDao.addNode(node);
        mNodeIdsToKeep.add(node.mId);
        if (updateType != null) {
            switch (updateType) {
                case CACHE_DEPRECATED: mUpdatedNodes.add(node); break;
                case NEW: mNewNodes.add(node); break;
                case NOT_CACHED: mNotCachedNodes.add(node); break;
            }
        }
        JSONArray children = last.getJSONArray(ProtocolConstants.CHILDREN);
        for (int i = 0; i < children.length(); i++) {
            JSONObject child = children.getJSONObject(i);
            parseNodesRecursively(nodeDao, login, method, child, node.mId);
        }
    }

    private void processJson(JSONObject jsonObject) {
        try {
            LoginResult loginResult = LoginResult.values()[jsonObject.getInt(ProtocolConstants.RESULT_CODE)];
            String resultStatusInfo = jsonObject.getString(ProtocolConstants.RESULT_STATUS_INFO);
            String mandantId = jsonObject.getString(ProtocolConstants.MANDANT_ID);
            if (loginResult == LoginResult.Login_OK) {
                mLogin.mLastUpdate = mDate;
                mLogin.mMandantId = mandantId;
                Login.Dao loginDao = Application.getInstance().getDatabase().getLoginDao();
                loginDao.updateLogin(mLogin);

                Node.Dao nodeDao = Application.getInstance().getDatabase().getNodeDao();

                // extract all contents into node objects
                mCacheFilesToKeep = new ArrayList<>();
                mNodeIdsToKeep = new ArrayList<>();
                mUpdatedNodes = new ArrayList<>();
                mNewNodes = new ArrayList<>();
                mNotCachedNodes = new ArrayList<>();
                JSONArray menuItems = jsonObject.getJSONArray(ProtocolConstants.RESULT_MENU_ITEMS);
                for (int i = 0; i < menuItems.length(); i++) {
                    JSONObject menuItem = menuItems.getJSONObject(i);
                    if (menuItem.getInt(ProtocolConstants.INDEX) == 0) {
                        JSONArray entries = menuItem.getJSONArray(ProtocolConstants.CHILDREN);
                        for (int j = 0; j < entries.length(); j++) {
                            JSONObject entry = entries.getJSONObject(j);
                            Node.Method method = Node.Method.fromMethodName(entry.getString(ProtocolConstants.METHOD_NAME));
                            JSONArray rootContentArray = entry.getJSONObject(ProtocolConstants.ROOT).getJSONArray(ProtocolConstants.CHILDREN);
                            for (int k = 0; k < rootContentArray.length(); k++) {
                                JSONObject content = rootContentArray.getJSONObject(k);
                                parseNodesRecursively(nodeDao, mLogin, method, content, null);
                            }
                        }
                        break;
                    }
                }
                //nodeDao.deleteAllNodesByLoginId(mLogin.mId);
                nodeDao.clean(mLogin.mId, mNodeIdsToKeep);

                if (mNodeUpdateEventListener != null) {
                    for (Node node : mUpdatedNodes) {
                        mNodeUpdateEventListener.onNodeUpdated(node, UpdateType.CACHE_DEPRECATED);
                    }
                    for (Node node : mNewNodes) {
                        mNodeUpdateEventListener.onNodeUpdated(node, UpdateType.NEW);
                    }
                    for (Node node : mNotCachedNodes) {
                        mNodeUpdateEventListener.onNodeUpdated(node, UpdateType.NOT_CACHED);
                    }
                }

                // Test NodeUpdateEventListener
                /*if (mNodeUpdateEventListener != null) {
                    for (UpdateType updateType : UpdateType.values()) {
                        Node testNode = new Node(mLogin.mId);
                        testNode.mTitle = String.format("Test Title: %s", updateType.name());
                        testNode.mContent = "Some long story too big to fit into a ordinary notification. Please close this message after finished reading.";
                        mNodeUpdateEventListener.onNodeUpdated(testNode, updateType);
                    }
                }*/

                File[] files = new File(Application.getInstance().getFilesDir(), String.valueOf(mLogin.mId)).listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!mCacheFilesToKeep.contains(file)) {
                            if (!file.setWritable(true) || !file.delete()) {
                                if (file.isDirectory()) {
                                    FileUtils.deleteRecursively(file);
                                } else {
                                    Log.e(TAG, "Could not delete file while cleaning cache: " + file.toString());
                                }
                            }
                        }
                    }
                }
                mFetchIndexEventListener.onSuccess(mLogin.mId, false);
            } else {
                mFetchIndexEventListener.onFail(loginResult, resultStatusInfo);
            }
        } catch (Exception e) {
            onException(e);
            Log.e(TAG, LogUtils.getStackTrace(e));
        }
    }

    public enum UpdateType {
        NEW,
        NOT_CACHED,
        CACHE_DEPRECATED,
    }

    @Override
    void onEmptyResponse() {
        busy = false;
        onException(null);
    }

    @Override
    void onException(Exception exception) {
        busy = false;
        mFetchIndexEventListener.onException(exception);
        Log.e(TAG, LogUtils.getStackTrace(exception));

        // obviously, network failed -> try loading from cache!
        if (Application.getInstance().getDatabase().getNodeDao().getNodeCountByLogin(mLogin.mId) > 0) {
            mFetchIndexEventListener.onSuccess(mLogin.mId, true);
        }
    }

    public enum LoginResult {
        Login_OK,
        Login_Failed,
        Licence_Expired,
    }

    public interface FetchIndexEventListener {
        void onProgress(int progress);

        void onException(Exception exception);

        void onSuccess(long loginId, boolean cached);

        void onFail(LoginResult loginResult, String resultStatusInfo);
    }

    public interface NodeUpdateEventListener {
        void onNodeUpdated(Node node, UpdateType updateType);
    }
}
