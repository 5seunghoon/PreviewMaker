package com.tistory.deque.previewmaker;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class StampAdatper extends RecyclerView.Adapter<StampAdatper.ViewHolder>  {
  private ArrayList<StampItem> mStampItems;

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public TextView StampNameTextView;
    public ImageView StampImageTextView;
    public Button deleteItemButton;

    public ViewHolder(View v) {
      super(v);
      StampNameTextView = v.findViewById(R.id.stampListTextView);
      StampImageTextView = v.findViewById(R.id.stampListImageView);
      deleteItemButton = v.findViewById(R.id.deleteItempButton);
    }
  }

  public StampAdatper(ArrayList<StampItem> items){
    mStampItems = items;
  }
  @NonNull
  @Override
  public StampAdatper.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

    ViewHolder vh = new ViewHolder(v);
    return vh;
  }

  @Override
  public void onBindViewHolder(@NonNull StampAdatper.ViewHolder holder, final int position) {
    holder.StampNameTextView.setText(mStampItems.get(position).getStampName());
    holder.StampImageTextView.setImageURI(mStampItems.get(position).getImageURI());

    holder.deleteItemButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Snackbar.make(v, position + " 삭제", Snackbar.LENGTH_LONG).show();
      }
    });

    holder.StampNameTextView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickItem(v, position);
      }
    });
    holder.StampImageTextView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickItem(v, position);
      }
    });
  }

  private void clickItem(View v, int position){
    Snackbar.make(v, position + " 선택", Snackbar.LENGTH_LONG).show();
  }

  @Override
  public int getItemCount() {
    return mStampItems.size();
  }
}
