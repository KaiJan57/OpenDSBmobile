package com.kai_jan_57.opendsbmobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.database.AppDatabase;
import com.kai_jan_57.opendsbmobile.database.Node;
import com.kai_jan_57.opendsbmobile.network.ProtocolConstants;
import com.kai_jan_57.opendsbmobile.utils.ShareUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private final Context mContext;

    private final long[] mRootNodeIds;

    public NewsAdapter(Context pContext, long[] rootNodeIds) {
        mContext = pContext;
        mRootNodeIds = rootNodeIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_news, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Node node = AppDatabase.getInstance(mContext).getNodeDao().getNodeById(mRootNodeIds[position]);

        holder.setTitle(node.mTitle);
        holder.setSubtitle(ProtocolConstants.DATE_PARSER.format(node.mDate));
        holder.setMessage(node.mContent);
    }

    @Override
    public int getItemCount() {
        return mRootNodeIds.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTitleTextView;
        private final TextView mSubtitleTextView;
        private final TextView mMessageTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            mTitleTextView = itemView.findViewById(R.id.newsTitle);
            mSubtitleTextView = itemView.findViewById(R.id.newsSubtitle);
            mMessageTextView = itemView.findViewById(R.id.newsMessage);

            itemView.findViewById(R.id.clickableLayout).setOnClickListener(v -> {
                String message = mTitleTextView.getText() + System.lineSeparator() + mSubtitleTextView.getText() +
                        System.lineSeparator() + System.lineSeparator() + mMessageTextView.getText();
                ShareUtils.shareText(mContext, message, "text/plain", mContext.getString(R.string.share_news));
            });
        }

        void setTitle(String title) {
            mTitleTextView.setText(title);
        }

        void setSubtitle(String subtitle) {
            mSubtitleTextView.setText(subtitle);
        }

        void setMessage(String message) {
            mMessageTextView.setText(message);
        }
    }
}
