package com.tistory.deque.previewmaker;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class StampAdapter extends RecyclerView.Adapter<StampAdapter.ViewHolder>  {
  private MainActivity mActivity;
  private ArrayList<StampItem> mStampItems;
  private final String TAG ="MainActivity";

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public TextView StampNameTextView;
    public ImageView StampImageTextView;
    public LinearLayout selectLayout;
    public Button deleteItemButton;

    public ViewHolder(View v) {
      super(v);
      StampNameTextView = v.findViewById(R.id.stampListTextView);
      StampImageTextView = v.findViewById(R.id.stampListImageView);
      selectLayout = v.findViewById(R.id.selectLayout);
      deleteItemButton = v.findViewById(R.id.deleteItempButton);
    }
  }

  public StampAdapter(ArrayList<StampItem> items, MainActivity activity){
    mStampItems = items;
    mActivity = activity;
  }

  @NonNull
  @Override
  public StampAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

    ViewHolder vh = new ViewHolder(v);
    return vh;
  }

  @Override
  public void onBindViewHolder(@NonNull StampAdapter.ViewHolder holder, final int position) {
    holder.StampNameTextView.setText(mStampItems.get(position).getStampName());
    holder.StampImageTextView.setImageURI(mStampItems.get(position).getImageURI());
    holder.deleteItemButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickDel(v, position);
      }
    });
    holder.selectLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickItem(v, position);
      }
    });
  }

  private void clickItem(View v, int position){
    mActivity.callFromListItem(position);
  }

  private void clickDel(final View v, final int position){
    AlertDialog.Builder stampDeleteAlert = new AlertDialog.Builder(mActivity);
    stampDeleteAlert.setMessage("낙관 [" + mStampItems.get(position).getStampName() + "] 을 정말 삭제하시겠어요?").setCancelable(true)
      .setPositiveButton("YES", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          deleteStampAndScan(v, position);
        }
      })
      .setNegativeButton("NO",
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          return;
        }
      });
    AlertDialog alert = stampDeleteAlert.create();
    alert.show();
  }

  private void deleteStampAndScan(View v, int position){
    mActivity.callFromListItemToDelete(v, position);
  }

  @Override
  public int getItemCount() {
    return mStampItems.size();
  }
}
