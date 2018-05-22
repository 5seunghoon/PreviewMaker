package com.tistory.deque.previewmaker.PreviewData;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tistory.deque.previewmaker.PreviewEditActivity;
import com.tistory.deque.previewmaker.R;

import java.util.ArrayList;

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ViewHolder> {
  private PreviewEditActivity mActivity;
  private ArrayList<PreviewItem> mPreviewItems;

  private final String TAG = "PreviewEditActivity";

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public ImageView previewImageView;

    public ViewHolder(View v) {
      super(v);
      previewImageView = v.findViewById(R.id.previewItemImageView);
    }
  }

  public PreviewAdapter(PreviewEditActivity mActivity, ArrayList<PreviewItem> mPreviewItems){
    this.mActivity = mActivity;
    this.mPreviewItems = mPreviewItems;
  }

  @NonNull
  @Override
  public PreviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_list_item, parent, false);
    ViewHolder vh = new ViewHolder(v);
    return vh;
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
    holder.previewImageView.setImageURI(mPreviewItems.get(position).getThumbnailImageURI());
    holder.previewImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mActivity.clickPreviewItem(v, position);
      }
    });
  }

  @Override
  public int getItemCount() {
    return mPreviewItems.size();
  }
}
