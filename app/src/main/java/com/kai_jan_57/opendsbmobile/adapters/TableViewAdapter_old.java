package com.kai_jan_57.opendsbmobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.kai_jan_57.opendsbmobile.R;

public class TableViewAdapter_old extends AbstractTableAdapter<String, String, String> {

    public TableViewAdapter_old(Context context) {
        super(context);
    }

    class CellViewHolder extends AbstractViewHolder {
        final TextView mCellTextView;

        CellViewHolder(View itemView) {
            super(itemView);
            mCellTextView = itemView.findViewById(R.id.tableview_cell_text);
        }
    }

    @Override
    public int getColumnHeaderItemViewType(int position) {
        return 0;
    }

    @Override
    public int getRowHeaderItemViewType(int position) {
        return 0;
    }

    @Override
    public int getCellItemViewType(int position) {
        return 0;
    }

    @Override
    public AbstractViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.tableview_cell_layout, parent, false);
        return new CellViewHolder(layout);
    }

    @Override
    public void onBindCellViewHolder(AbstractViewHolder holder, Object cellItemModel, int columnPosition, int rowPosition) {
        CellViewHolder viewHolder = (CellViewHolder) holder;
        String text = String.valueOf(cellItemModel);
        if (text.isEmpty()) {
            text = "---";
        }
        viewHolder.mCellTextView.setText(text);

        viewHolder.itemView.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        viewHolder.mCellTextView.requestLayout();
    }

    class ColumnHeaderViewHolder extends AbstractViewHolder {

        final TextView mColumnHeaderTextView;

        ColumnHeaderViewHolder(View itemView) {
            super(itemView);
            mColumnHeaderTextView = itemView.findViewById(R.id.tableview_column_header_text);
        }

    }

    @Override
    public AbstractViewHolder onCreateColumnHeaderViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.tableview_column_header_layout, parent, false);

        return new ColumnHeaderViewHolder(layout);
    }

    @Override
    public void onBindColumnHeaderViewHolder(AbstractViewHolder holder, Object columnHeaderItemModel, int columnPosition) {
        ColumnHeaderViewHolder columnHeaderViewHolder = (ColumnHeaderViewHolder) holder;
        columnHeaderViewHolder.mColumnHeaderTextView.setText(String.valueOf(columnHeaderItemModel));

        columnHeaderViewHolder.mColumnHeaderTextView.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        columnHeaderViewHolder.mColumnHeaderTextView.requestLayout();
    }

    class RowHeaderViewHolder extends AbstractViewHolder {

        final TextView mRowTextView;

        RowHeaderViewHolder(View itemView) {
            super(itemView);
            mRowTextView = itemView.findViewById(R.id.tableview_row_text);
        }
    }

    @Override
    public AbstractViewHolder onCreateRowHeaderViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.tableview_row_header_layout, parent, false);

        return new RowHeaderViewHolder(layout);
    }

    @Override
    public void onBindRowHeaderViewHolder(AbstractViewHolder holder, Object rowHeaderItemModel, int rowPosition) {
        RowHeaderViewHolder rowHeaderViewHolder = (RowHeaderViewHolder) holder;
        rowHeaderViewHolder.mRowTextView.setText(String.valueOf(rowHeaderItemModel));
    }

    @Override
    public View onCreateCornerView() {
        return null;
    }
}
