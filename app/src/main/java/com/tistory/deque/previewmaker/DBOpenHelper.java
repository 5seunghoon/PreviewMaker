package com.tistory.deque.previewmaker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class DBOpenHelper extends SQLiteOpenHelper {
  private static DBOpenHelper dbHelper;
  public SQLiteDatabase db;

  public static final String PK_ID = "_ID";
  public static final String TABLE_NAME_STAMPS = "STAMPS_TABLE";
  public static final String STAMP_NAME_KEY = "STAMP_NAME";
  public static final String STAMP_URI_KEY = "STAMP_URI";
  public static final String STAMP_WIDTH_KEY = "STAMP_WIDTH";
  public static final String STAMP_HEIGHT_KEY = "STAMP_HEIGHT";
  public static final String STAMP_POS_WIDTH_PERCENT_KEY = "STAMP_POS_WIDTH_PERCENT";
  public static final String STAMP_POS_HEIGHT_PERCENT_KEY = "STAMP_POS_HEIGHT_PERCENT";

  public static final int STAMP_POS_WIDTH_PERCENT_KEY_INIT_VALUE = 50000; // 50,000 = 50%
  public static final int STAMP_POS_HEIGHT_PERCENT_KEY_INIT_VALUE = 50000; // 50,000 = 50%
  public static final int STAMP_WIDTH_KEY_INIT_VALUE = -1;
  public static final int STAMP_HEIGHT_KEY_INIT_VALUE = -1;

  public static final String DP_OPEN_HELPER_NAME = "DB_OPEN_HELPER_NAME";
  public static final int dbVersion = 1;

  private static final String TAG = "MainActivity";

  private DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
    super(context, name, factory, version);
  }

  public static DBOpenHelper getDbOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
    if(dbHelper != null) {
      Logger.d(TAG, "call singletun : helper not null");
      return dbHelper;
    }
    else{
      dbHelper = new DBOpenHelper(context, name, factory, version);
      Logger.d(TAG, "call singletun : helper null");
      return dbHelper;
    }
  }

  public void dbOpen(){
    db = dbHelper.getWritableDatabase();
  }

  public void dbClose(){
    db.close();
    Logger.d(TAG, "database close");
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    createStampsTable(db);
    Logger.d(TAG, "onCreate");
  }

  public void createStampsTable(SQLiteDatabase db){
    String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_STAMPS + "(" +
      PK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"+
      ", " +
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
    Logger.d(TAG, "SQL EXEC : " + sql);
    try{
      db.execSQL(sql);
      Logger.d(TAG, "create db : " + TABLE_NAME_STAMPS);
    } catch (Exception e){
      Logger.d(TAG, "create table exception : " + e.toString());
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
    stampValue.put(STAMP_WIDTH_KEY, STAMP_WIDTH_KEY_INIT_VALUE);
    stampValue.put(STAMP_HEIGHT_KEY, STAMP_HEIGHT_KEY_INIT_VALUE);
    stampValue.put(STAMP_POS_WIDTH_PERCENT_KEY, STAMP_POS_WIDTH_PERCENT_KEY_INIT_VALUE);
    stampValue.put(STAMP_POS_HEIGHT_PERCENT_KEY, STAMP_POS_HEIGHT_PERCENT_KEY_INIT_VALUE);
    long result = db.insert(TABLE_NAME_STAMPS, null, stampValue);
    if(result == -1){
      Logger.d(TAG, "insert error : name : " + stampName + " , uri : " + imageURI);
      return false;
    }
    else{
      Logger.d(TAG, "insert success : name : " + stampName + " , uri : " + imageURI);
      return true;
    }
  }

  public boolean dbDeleteStamp(int id){
    long result = db.delete(TABLE_NAME_STAMPS,  PK_ID + "=" + id, null);
    if(result == -1){
      Logger.d(TAG, "delete error : id : " + id);
      return false;
    } else {
      Logger.d(TAG, "delete suc : id : " + id);
      return true;
    }
  }

  public void dbUpdateStamp(int id, int width, int height, int posWidthPer, int posHeightPer){
    String sql = "UPDATE " + TABLE_NAME_STAMPS + " SET "
      + STAMP_WIDTH_KEY + " = " + width
      + ", "
      + STAMP_HEIGHT_KEY + " = " + height
      + ", "
      + STAMP_POS_WIDTH_PERCENT_KEY + " = " + posWidthPer
      + ", "
      + STAMP_POS_HEIGHT_PERCENT_KEY + " = " + posHeightPer
      + " WHERE _ID IN(" + id + ")" + ";";
    db.execSQL(sql);

    Logger.d(TAG, "input sql : " + sql);

  }
}
