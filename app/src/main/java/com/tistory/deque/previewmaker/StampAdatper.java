package com.tistory.deque.previewmaker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class StampAdatper extends RecyclerView.Adapter<StampAdatper.ViewHolder>  {
  private Activity mActivity;
  private ArrayList<StampItem> mStampItems;
  private final String TAG ="StampAdapter";
  private DBOpenHelper mDBOpenHelper;

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

  public StampAdatper(ArrayList<StampItem> items, Activity activity, DBOpenHelper dbOpenHelper){
    mActivity = activity;
    mStampItems = items;
    mDBOpenHelper = dbOpenHelper;
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
    Snackbar.make(v, "POS : " + position + ", ID " + mStampItems.get(position).getID(), Snackbar.LENGTH_LONG).show();
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
    //delete stamp and do media scan
    int id = mStampItems.get(position).getID();
    Uri imageURI = mStampItems.get(position).getImageURI();
    String name = mStampItems.get(position).getStampName();
    File file = new File(imageURI.getPath());
    if(file.delete()) {

      Log.d(TAG, "Stamp delete suc");
      Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
      mediaScanIntent.setData(imageURI);
      mActivity.sendBroadcast(mediaScanIntent);
      Log.d(TAG, "media scanning end");

      try{
        mStampItems.remove(position);
      } catch (IndexOutOfBoundsException e){
        Log.d(TAG, "out ouf bound");
      }

      notifyDataSetChanged();

      mDBOpenHelper.dbDeleteStamp(id);

      Snackbar.make(v, "낙관 [" + name + "] 삭제 완료", Snackbar.LENGTH_LONG).show();
    } else {
      Log.d(TAG, "Stamp delete fail" + imageURI);
    }
  }

  @Override
  public int getItemCount() {
    return mStampItems.size();
  }
}
