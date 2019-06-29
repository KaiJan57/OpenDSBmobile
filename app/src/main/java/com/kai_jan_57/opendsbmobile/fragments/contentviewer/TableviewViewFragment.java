package com.kai_jan_57.opendsbmobile.fragments.contentviewer;

import androidx.lifecycle.ViewModelProviders;

import android.content.res.TypedArray;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inqbarna.tablefixheaders.TableFixHeaders;
import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.adapters.TableViewAdapter;
import com.kai_jan_57.opendsbmobile.viewmodels.TableviewViewModel;

public class TableviewViewFragment extends ContentViewerFragment {

    private TableviewViewModel mViewModel;

    private TextView mTextViewTitle;
    private TableViewAdapter mTableViewAdapter;

    public static TableviewViewFragment newInstance() {
        return new TableviewViewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_table_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        if (getActivity() != null) {
            TypedArray typedArray = getActivity().getTheme().obtainStyledAttributes(R.style.AppTheme, new int[]{R.attr.colorControlNormal});
            view.findViewById(R.id.tableBackground).setBackgroundColor(getActivity().getColor(typedArray.getResourceId(0, 0)));
        }

        mTextViewTitle = view.findViewById(R.id.textViewTitle);

        TableFixHeaders tableView = view.findViewById(R.id.tableView);
        mTableViewAdapter = new TableViewAdapter(getContext());
        tableView.setAdapter(mTableViewAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(TableviewViewModel.class);

        mViewModel.getTitle().observe(this, title -> mTextViewTitle.setText(title));
        mViewModel.getColumnHeaders().observe(this, columns -> {
            mTableViewAdapter.setFirstHeader(columns.get(0));
            mTableViewAdapter.setHeader(columns.size() > 2 ? columns.subList(1, columns.size()) : columns);
        });
        mViewModel.getTableContent().observe(this, tableContent -> {
            mTableViewAdapter.setFirstBody(tableContent);
            mTableViewAdapter.setBody(tableContent);
            mTableViewAdapter.setSection(tableContent);
        });

        if (mViewModel.getDate() == null || mViewModel.getDate().before(getNode().mDate)) {
            mViewModel.loadTable(getNode());
        }
    }

    @Override
    public void onSwipedOut() {

    }

    @Override
    public void shareContent(View view) {

    }
}
