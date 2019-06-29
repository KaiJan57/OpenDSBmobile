package com.kai_jan_57.opendsbmobile.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kai_jan_57.opendsbmobile.R;
import com.kai_jan_57.opendsbmobile.database.AppDatabase;
import com.kai_jan_57.opendsbmobile.database.Node;
import com.kai_jan_57.opendsbmobile.network.ProtocolConstants;
import com.kai_jan_57.opendsbmobile.viewmodels.PreviewViewModel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder> {

    private final AppCompatActivity mContext;

    private final long[] mRootNodeIds;

    private final ItemClickEventListener mItemClickEventListener;

    public ImagePreviewAdapter(AppCompatActivity pContext, long[] rootNodeIds, ItemClickEventListener itemClickEventListener) {
        mContext = pContext;
        mRootNodeIds = rootNodeIds;
        mItemClickEventListener = itemClickEventListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item_timetable, parent, false), mItemClickEventListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PreviewViewModel previewViewModel = ViewModelProviders.of(mContext).get(getClass().getCanonicalName() + mRootNodeIds[0] + position, PreviewViewModel.class);

        previewViewModel.getTitle().observe(mContext, holder.mTitleTextView::setText);
        previewViewModel.getSubtitle().observe(mContext, holder.mSubtitleTextView::setText);
        previewViewModel.getProgress().observe(mContext, holder.mProgressBar::setProgress);

        previewViewModel.getStatus().observe(mContext, pStatus -> {
            holder.setStatus(pStatus);
            switch (pStatus) {
                case IDLE: {
                    holder.mPreviewImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            previewViewModel.loadPreviewImage(AppDatabase.getInstance(mContext).getNodeDao().getNodeById(mRootNodeIds[position]), holder.mPreviewImage.getMeasuredWidth());
                            holder.mPreviewImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
                    break;
                }
                case LOADING: {
                    holder.mPreviewImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_item_downloading));
                    holder.mErrorTextView.setVisibility(View.GONE);
                    holder.mProgressBar.setVisibility(View.VISIBLE);
                    break;
                }
                case FAILED: {
                    holder.mPreviewImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_item_image_broken));
                    String errorMessage;
                    if (previewViewModel.getLastException().getMessage().equals(PreviewViewModel.ERROR_NO_PREVIEW)) {
                        errorMessage = mContext.getString(R.string.error_no_preview);
                    } else {
                        errorMessage = previewViewModel.getLastException().getLocalizedMessage();
                    }
                    holder.mErrorTextView.setText(errorMessage);
                    holder.mErrorTextView.setVisibility(View.VISIBLE);
                    holder.mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        previewViewModel.getPreviewImage().observe(mContext, pBitmap -> {
            holder.mPreviewImage.setImageBitmap(Bitmap.createBitmap(pBitmap, 0, 0,
                    pBitmap.getWidth(), pBitmap.getHeight() < pBitmap.getWidth() ? pBitmap.getHeight() : pBitmap.getWidth()));
            holder.mErrorTextView.setVisibility(View.GONE);
            holder.mProgressBar.setVisibility(View.GONE);
            holder.itemView.invalidate();
        });

        Node node = AppDatabase.getInstance(mContext).getNodeDao().getNodeById(mRootNodeIds[position]);
        holder.mTitleTextView.setText(node.mTitle);
        holder.mSubtitleTextView.setText(ProtocolConstants.DATE_PARSER.format(node.mDate));
    }

    @Override
    public int getItemCount() {
        if (mRootNodeIds != null) {
            return mRootNodeIds.length;
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final LinearLayout mClickableLayout;

        final ImageView mPreviewImage;

        final TextView mTitleTextView;
        final TextView mSubtitleTextView;
        final TextView mErrorTextView;

        final ProgressBar mProgressBar;

        PreviewViewModel.Status mStatus = PreviewViewModel.Status.LOADING;

        ViewHolder(@NonNull View itemView, ItemClickEventListener pItemClickEventListener) {
            super(itemView);
            mClickableLayout = itemView.findViewById(R.id.clickableLayout);
            mClickableLayout.setOnClickListener(v -> {
                if (pItemClickEventListener != null) {
                    pItemClickEventListener.onClick(this, getLayoutPosition());
                }
            });

            mClickableLayout.setOnLongClickListener(v -> pItemClickEventListener != null && pItemClickEventListener.onLongClick(this, getLayoutPosition()));

            mPreviewImage = itemView.findViewById(R.id.imageViewPreview);

            mTitleTextView = itemView.findViewById(R.id.textViewTitle);
            mSubtitleTextView = itemView.findViewById(R.id.textViewSubtitle);
            mErrorTextView = itemView.findViewById(R.id.textViewError);

            mProgressBar = itemView.findViewById(R.id.progressBar);

        }

        void setStatus(PreviewViewModel.Status status) {
            mStatus = status;
        }

        public PreviewViewModel.Status getStatus() {
            return mStatus;
        }
    }

    public interface ItemClickEventListener {
        void onClick(ViewHolder holder, int position);

        boolean onLongClick(ViewHolder holder, int position);
    }
}
