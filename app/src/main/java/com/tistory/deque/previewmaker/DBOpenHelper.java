package com.tistory.deque.previewmaker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class DBOpenHelper extends SQLiteOpenHelper {
  private static DBOpenHelper dbHelper;
  public SQLiteDatabase db;

  public static final String TABLE_NAME_STAMPS = "STAMPS_TABLE";
  public static final String STAMP_NAME_KEY = "STAMP_NAME";
  public static final String STAMP_URI_KEY = "STAMP_URI";
  public static final String STAMP_WIDTH_KEY = "STAMP_WIDTH";
  public static final String STAMP_HEIGHT_KEY = "STAMP_HEIGHT";
  public static final String STAMP_POS_WIDTH_PERCENT_KEY = "STAMP_POS_WIDTH_PERCENT";
  public static final String STAMP_POS_HEIGHT_PERCENT_KEY = "STAMP_POS_HEIGHT_PERCENT";

  private static final String TAG = "DBOpenHelper";

  private DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
    super(context, name, factory, version);
  }

  public static DBOpenHelper getDbOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
    if(dbHelper != null) {
      Log.d(TAG, "call singletun : helper not null");
      return dbHelper;
    }
    else{
      dbHelper = new DBOpenHelper(context, name, factory, version);
      Log.d(TAG, "call singletun : helper null");
      return dbHelper;
    }
  }

  public void dbOpen(){
    db = dbHelper.getWritableDatabase();
  }

  public void dbClose(){
    db.close();
    Log.d(TAG, "database close");
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    createStampsTable(db);
    Log.d(TAG, "onCreate");
  }

  public void createStampsTable(SQLiteDatabase db){
    String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_STAMPS + "(" +
      "_ID INTEGER PRIMARY KEY AUTOINCREMENT, "+
      STAMP_NAME_KEY + " TEXT" +
      ", " +
      STAMP_URI_KEY + " TEXT" +
      ", " +
      STAMP_WIDTH_KEY + " INTEGER" +
      ", " +
      STAMP_HEIGHT_KEY + " INTEGER" +
      ", " +
      STAMP_POS_WIDTH_PERCENT_KEY + " INTEGER" +
      ", " +
      STAMP_POS_HEIGHT_PERCENT_KEY + " INTEGER" +
      ")";
    Log.d(TAG, "SQL EXEC : " + sql);
    try{
      db.execSQL(sql);
      Log.d(TAG, "create db : " + TABLE_NAME_STAMPS);
    } catch (Exception e){
      Log.d(TAG, "create table exception : " + e.toString());
    }
  }


  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_STAMPS);
    onCreate(db);
  }


  public boolean dbInsertStamp(String stampName, Uri imageURI){
    ContentValues stampValue = new ContentValues();
    stampValue.put(STAMP_NAME_KEY, stampName);
    stampValue.put(STAMP_URI_KEY, imageURI.toString());
    long result = db.insert(TABLE_NAME_STAMPS, null, stampValue);
    if(result == -1){
      Log.d(TAG, "insert error : name : " + stampName + " , uri : " + imageURI);
      return false;
    }
    else{
      Log.d(TAG, "insert success : name : " + stampName + " , uri : " + imageURI);
      return true;
    }
  }

  public boolean dbDeleteStamp(int id){
    long result = db.delete(TABLE_NAME_STAMPS,  "_ID" + "=" + id, null);
    if(result == -1){
      Log.d(TAG, "delete error : id : + " + id);
      return false;
    } else {
      Log.d(TAG, "delete suc : id : + " + id);
      return true;
    }
  }
}
