package com.kai_jan_57.opendsbmobile.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kai_jan_57.opendsbmobile.R;

import java.util.ArrayList;
import java.util.List;

import miguelbcr.ui.tableFixHeadesWrapper.TableFixHeaderAdapter;

public class TableViewAdapter extends TableFixHeaderAdapter<
        String, TableViewAdapter.CellViewGroup,
        String, TableViewAdapter.CellViewGroup,
        List<String>,
        TableViewAdapter.CellViewGroup,
        TableViewAdapter.CellViewGroup,
        TableViewAdapter.CellViewGroup> {

    private Context mContext;

    @Override
    protected CellViewGroup inflateFirstHeader() {
        return new CellViewGroup(mContext);
    }

    @Override
    protected CellViewGroup inflateHeader() {
        return new CellViewGroup(mContext);
    }

    @Override
    protected CellViewGroup inflateFirstBody() {
        return new CellViewGroup(mContext);
    }

    @Override
    protected CellViewGroup inflateBody() {
        return new CellViewGroup(mContext);
    }

    @Override
    protected CellViewGroup inflateSection() {
        return new CellViewGroup(mContext);
    }

    @Override
    protected List<Integer> getHeaderWidths() {
        List<Integer> headerWidths = new ArrayList<>();
        for (int i = 0; i < getColumnCount() + 1; i++) {
            headerWidths.add((int) mContext.getResources().getDimension(R.dimen.table_cell_width));
        }
        return headerWidths;
    }

    @Override
    protected int getHeaderHeight() {
        return (int) mContext.getResources().getDimension(R.dimen.table_cell_height);
    }

    @Override
    protected int getSectionHeight() {
        return (int) mContext.getResources().getDimension(R.dimen.table_cell_height);
    }

    @Override
    protected int getBodyHeight() {
        return (int) (mContext.getResources().getDimension(R.dimen.table_cell_height) * 1.5f);
    }

    @Override
    protected boolean isSection(List<List<String>> items, int row) {
        return items.size() > row && items.get(row).size() == 1;
    }

    public TableViewAdapter(Context context) {
        super(context);
        mContext = context;
    }

    public class CellViewGroup extends FrameLayout
            implements
            TableFixHeaderAdapter.FirstHeaderBinder<String>,
            TableFixHeaderAdapter.HeaderBinder<String>,
            TableFixHeaderAdapter.FirstBodyBinder<List<String>>,
            TableFixHeaderAdapter.BodyBinder<List<String>>,
            TableFixHeaderAdapter.SectionBinder<List<String>> {

        private TextView mTextView;

        public CellViewGroup(@NonNull Context context) {
            super(context);
            LayoutInflater.from(context).inflate(R.layout.tableview_cell_layout, this, true);
            mTextView = findViewById(R.id.tableview_cell_text);
        }

        @Override
        public void bindFirstHeader(String headerName) {
            mTextView.setBackgroundResource(R.color.colorPrimary);
            mTextView.setText(headerName);
        }

        @Override
        public void bindHeader(String headerName, int column) {
            mTextView.setBackgroundResource(R.color.colorPrimary);
            mTextView.setText(headerName);
        }

        @Override
        public void bindFirstBody(List<String> items, int row) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(0, 0, /*(int) getResources().getDimension(R.dimen.one_dp)*/ 0,
                    row == getRowCount() - 1 ? 0 : (int) getResources().getDimension(R.dimen.one_dp));
            mTextView.setLayoutParams(layoutParams);
            if (items.size() > 1) {
                mTextView.setText(items.get(0));
            }
        }

        @Override
        public void bindBody(List<String> items, int row, int column) {
            if (items.size() > 1) {
                // TODO: replace workaround for cancelled period highlight by creating a TableEntry object
                if (items.get(items.size() - 1).equalsIgnoreCase("x")) {
                    mTextView.setBackgroundResource(R.color.colorAccent);
                } else {
                    TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(R.style.AppTheme, new int[]{android.R.attr.colorBackground});
                    mTextView.setBackgroundColor(mContext.getColor(typedArray.getResourceId(0, 0)));
                }
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(0, 0, 0,
                        row == getRowCount() - 1 ? 0 : (int) getResources().getDimension(R.dimen.one_dp));
                mTextView.setLayoutParams(layoutParams);
                if (items.size() > column) {
                    mTextView.setText(items.get(column + 1));
                }
            }
        }

        @Override
        public void bindSection(List<String> items, int row, int column) {
            //mTextView.setBackgroundResource(R.color.colorAccent);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.text_size_section));
            mTextView.setTypeface(null, Typeface.BOLD);
            //mTextView.setBackgroundResource(R.drawable.table_row_border);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(0, row == items.size() - 1 ? 0 : 2 * (int) getResources().getDimension(R.dimen.one_dp),
                    column == 0 ? /*(int) getResources().getDimension(R.dimen.one_dp)*/ 0 : 0, /*row == items.size() - 1 ? 0 : 3 * (int) getResources().getDimension(R.dimen.one_dp)*/
                    0);
            mTextView.setLayoutParams(layoutParams);
            mTextView.setText(column == 0 ? items.get(0) : "");
        }
    }

}
