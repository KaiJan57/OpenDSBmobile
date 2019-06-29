package com.kai_jan_57.opendsbmobile.fragments.contentviewer;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;

import com.kai_jan_57.opendsbmobile.Application;
import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.activities.ContentViewerActivity;
import com.kai_jan_57.opendsbmobile.database.AppDatabase;
import com.kai_jan_57.opendsbmobile.database.Node;

import androidx.annotation.MenuRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public abstract class ContentViewerFragment extends Fragment {
    private static final String ARG_NODE_ID = "node_id";

    private Node mNode;

    Node getNode() {
        return mNode;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param node Content to load.
     * @return A new instance of fragment ImageViewerFragment.
     */
    public static ContentViewerFragment newInstance(Node node) {
        ContentViewerFragment fragment;
        switch (node.mContentType) {
            default:
            case HTML:
            case XAP:
            case URL:
                fragment = Application.getInstance().getDatabase().getLoginDao().getLoginById(node.mLoginId).mParser > 0 ? new TableviewViewFragment() : new WebViewFragment();
                break;
            case IMG:
                fragment = new ImageViewerFragment();
        }
        Bundle args = new Bundle();
        args.putLong(ARG_NODE_ID, node.mId);
        fragment.setArguments(args);
        return fragment;
    }

    void setupAdvancedSharing(@MenuRes int menuId) {
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity instanceof ContentViewerActivity) {
            ContentViewerActivity contentViewerActivity = (ContentViewerActivity) fragmentActivity;
            ImageButton shareButton = fragmentActivity.findViewById(R.id.photopage_bottom_control_share);
            contentViewerActivity.registerForContextMenu(shareButton);
            shareButton.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                menu.setHeaderTitle(R.string.menu_header_advanced_sharing);
                getActivity().getMenuInflater().inflate(menuId, menu);
                contentViewerActivity.disableAutoHide();
            });
        }
    }

    public boolean isSearchable() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNode = AppDatabase.getInstance(getContext()).getNodeDao().getNodeById(getArguments().getLong(ARG_NODE_ID));
            /*if (getActivity() instanceof AppCompatActivity) {
                 ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                 if (actionBar != null) {
                     final SearchView searchView = new SearchView(actionBar.getThemedContext());
                     searchView.exp
                 }
            }*/
        }
    }

    public abstract void onSwipedOut();

    public abstract void shareContent(View view);

    public boolean shareAdvanced(MenuItem pMenuItem) {
        return false;
    }

    public void search(String query) {
        // to be overriden and implemented in case of searchable
    }
}
