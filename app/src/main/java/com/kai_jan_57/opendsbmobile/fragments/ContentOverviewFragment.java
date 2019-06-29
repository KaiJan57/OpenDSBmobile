package com.kai_jan_57.opendsbmobile.fragments;


import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.activities.ContentViewerActivity;
import com.kai_jan_57.opendsbmobile.adapters.NewsAdapter;
import com.kai_jan_57.opendsbmobile.adapters.ImagePreviewAdapter;
import com.kai_jan_57.opendsbmobile.database.AppDatabase;
import com.kai_jan_57.opendsbmobile.database.Node;
import com.kai_jan_57.opendsbmobile.viewmodels.PreviewViewModel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContentOverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContentOverviewFragment extends Fragment {
    private static final String ARG_METHOD = "method";
    private static final String ARG_LOGIN_ID = "login_id";

    private RecyclerView mRecyclerView;
    private Node.Method mMethod;
    private long mLoginId;

    public ContentOverviewFragment() {
        // Required empty public constructor
    }

    public static ContentOverviewFragment newInstance(Node.Method method, long loginId) {
        ContentOverviewFragment fragment = new ContentOverviewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_METHOD, Node.Method.toInt(method));
        args.putLong(ARG_LOGIN_ID, loginId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMethod = Node.Method.toMethod(getArguments().getInt(ARG_METHOD));
            mLoginId = getArguments().getLong(ARG_LOGIN_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_content_overview, container, false);
    }

    @SuppressWarnings("SameParameterValue")
    private int getSpanCount(int minSpanCount) {
        Point size = new Point();
        Activity activity = getActivity();
        if (activity != null) {
            activity.getWindowManager().getDefaultDisplay().getSize(size);
        } else {
            return minSpanCount;
        }
        return Math.round((float) size.x / Math.min(size.x, size.y) * minSpanCount);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        mRecyclerView = view.findViewById(R.id.recyclerView);
        long[] rootNodeIds = AppDatabase.getInstance(getActivity()).getNodeDao().getRootNodeIdsByMethod(mLoginId, mMethod);
        if (rootNodeIds.length > 0) {
            if (AppDatabase.getInstance(getActivity()).getNodeDao().getNodeById(rootNodeIds[0]).mContentType == Node.ContentType.NEWS) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                mRecyclerView.setAdapter(new NewsAdapter(getActivity(), rootNodeIds));
            } else {
                //mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false));
                mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(getSpanCount(2), StaggeredGridLayoutManager.VERTICAL));
                mRecyclerView.setAdapter(new ImagePreviewAdapter((AppCompatActivity) getActivity(), rootNodeIds, new ImagePreviewAdapter.ItemClickEventListener() {
                    @Override
                    public void onClick(ImagePreviewAdapter.ViewHolder holder, int position) {

                        if (holder.getStatus() == PreviewViewModel.Status.FAILED) {
                            RecyclerView.Adapter timetablePreviewAdapter = mRecyclerView.getAdapter();
                            if (timetablePreviewAdapter != null) {
                                timetablePreviewAdapter.notifyItemChanged(position);
                            }
                        }
                        ContentViewerActivity.start(getActivity(), rootNodeIds, position);
                    }

                    @Override
                    public boolean onLongClick(ImagePreviewAdapter.ViewHolder holder, int position) {
                        return true;
                    }
                }));
                //new LinearSnapHelper().attachToRecyclerView(mRecyclerView);
            }
        }
    }

}
