package com.tistory.deque.previewmaker.Model_PreviewData;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tistory.deque.previewmaker.Activity.PreviewEditActivity;
import com.tistory.deque.previewmaker.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ViewHolder> {
    private PreviewEditActivity mActivity;
    private ArrayList<PreviewItem> mPreviewItems;

    private final String TAG = "PreviewEditActivity";

    public class ViewHolder extends RecyclerView.ViewHolder {
        private PreviewEditActivity previewEditActivity;

        @BindView(R.id.previewItemImageView)
        ImageView previewImageView;

        public ViewHolder(PreviewEditActivity previewEditActivity, View v) {
            super(v);
            this.previewEditActivity = previewEditActivity;

            ButterKnife.bind(this, v);
        }

        @OnClick(R.id.previewItemImageView)
        public void onClickPreviewItemImageView(View view){
            mActivity.clickPreviewItem(getAdapterPosition());
        }
    }

    public PreviewAdapter(PreviewEditActivity mActivity, ArrayList<PreviewItem> mPreviewItems) {
        this.mActivity = mActivity;
        this.mPreviewItems = mPreviewItems;
    }

    @NonNull
    @Override
    public PreviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_list_item, parent, false);
        ViewHolder vh = new ViewHolder(mActivity, v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.previewImageView.setImageURI(mPreviewItems.get(position).getThumbnailImageURI());
    }

    @Override
    public int getItemCount() {
        return mPreviewItems.size();
    }
}
