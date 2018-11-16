package com.tistory.deque.previewmaker.Model_StampData;

import android.Manifest;
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

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.tistory.deque.previewmaker.Activity.MainActivity;
import com.tistory.deque.previewmaker.R;
import com.tistory.deque.previewmaker.Util.Logger;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StampAdapter extends RecyclerView.Adapter<StampAdapter.ViewHolder> {
    private MainActivity mActivity;
    private ArrayList<StampItem> mStampItems;
    private final String TAG = "MainActivity";

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.stampListTextView)
        TextView StampNameTextView;
        @BindView(R.id.stampListImageView)
        ImageView StampImageTextView;
        @BindView(R.id.selectLayout)
        LinearLayout selectLayout;
        @BindView(R.id.deleteStempButton)
        Button deleteItemButton;

        public ViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);

            StampNameTextView = v.findViewById(R.id.stampListTextView);
            StampImageTextView = v.findViewById(R.id.stampListImageView);
            selectLayout = v.findViewById(R.id.selectLayout);
            deleteItemButton = v.findViewById(R.id.deleteStempButton);
        }

        @OnClick(R.id.deleteStempButton)
        public void onClickDeleteStampButton(View view){
            clickDel(view, getAdapterPosition());
        }

        @OnClick(R.id.selectLayout)
        public void onClickSelectlayout(View view){
            clickItem(view, getAdapterPosition());
        }
    }


    public StampAdapter(ArrayList<StampItem> items, MainActivity activity) {
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
    }

    private void clickItem(View v, final int position) {
        TedPermission.with(mActivity.getApplicationContext())
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        mActivity.callFromListItem(position);
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    }
                })
                .setRationaleMessage(mActivity.getString(R.string.tedpermission_select_stamp_rational))
                .setDeniedMessage(mActivity.getString(R.string.tedpermission_select_stamp_deny_rational))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .setGotoSettingButton(true)
                .check();
    }

    private void clickDel(final View v, final int position) {
        AlertDialog.Builder stampDeleteAlert = new AlertDialog.Builder(mActivity);
        stampDeleteAlert.setMessage("낙관 [" + mStampItems.get(position).getStampName() + "] 을 정말 삭제하시겠습니까?").setCancelable(true)
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
        final AlertDialog delAlert = stampDeleteAlert.create();

        TedPermission.with(mActivity.getApplicationContext())
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        delAlert.show();
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    }
                })
                .setRationaleMessage(mActivity.getString(R.string.tedpermission_del_stamp_rational))
                .setDeniedMessage(mActivity.getString(R.string.tedpermission_del_stamp_deny_rational))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .setGotoSettingButton(true)
                .check();
    }

    private void deleteStampAndScan(View v, int position) {
        mActivity.callFromListItemToDelete(v, position);
    }

    @Override
    public int getItemCount() {
        return mStampItems.size();
    }
}
