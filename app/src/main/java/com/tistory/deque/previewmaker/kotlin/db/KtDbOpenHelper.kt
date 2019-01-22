package com.tistory.deque.previewmaker.kotlin.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.model.enums.StampAnchorEnum
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import java.io.File

class KtDbOpenHelper(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {

    companion object {
        private var dbHelper: KtDbOpenHelper? = null

        const val PK_ID = "_ID"
        const val TABLE_NAME_STAMPS = "STAMPS_TABLE"
        const val STAMP_NAME_KEY = "STAMP_NAME"
        const val STAMP_URI_KEY = "STAMP_URI"
        const val STAMP_WIDTH_KEY = "STAMP_WIDTH"
        const val STAMP_HEIGHT_KEY = "STAMP_HEIGHT"
        const val STAMP_POS_WIDTH_PERCENT_KEY = "STAMP_POS_WIDTH_PERCENT"
        const val STAMP_POS_HEIGHT_PERCENT_KEY = "STAMP_POS_HEIGHT_PERCENT"
        const val STAMP_POS_ANCHOR_KEY = "STAMP_POS_ANCHOR"

        const val STAMP_POS_WIDTH_PERCENT_KEY_INIT_VALUE = 50000 // 50,000 = 50%
        const val STAMP_POS_HEIGHT_PERCENT_KEY_INIT_VALUE = 50000 // 50,000 = 50%
        const val STAMP_WIDTH_KEY_INIT_VALUE = -1
        const val STAMP_HEIGHT_KEY_INIT_VALUE = -1
        val STAMP_POS_ANCHOR_KEY_INIT_VALUE = StampAnchorEnum.CENTER.value

        const val DP_OPEN_HELPER_NAME = "DB_OPEN_HELPER_NAME"
        const val dbVersion = 1

        fun getDbOpenHelper(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int): KtDbOpenHelper {
            if (dbHelper == null) {
                dbHelper = KtDbOpenHelper(context, name, factory, version)
            }
            dbHelper?.let {
                EzLogger.d("DB HELPER is not null")
            } ?: EzLogger.d("DB HELPER is null")
            return dbHelper
                    ?: KtDbOpenHelper(context, name, factory, version)
        }

    }

    var db: SQLiteDatabase? = null

    fun dbOpen() {
        EzLogger.d("DB OPEN")
        db = dbHelper?.writableDatabase
    }

    fun dbClose() {
        EzLogger.d("DB CLOSE")
        db?.close()
    }

    override fun onCreate(db: SQLiteDatabase?) {
        EzLogger.d("CREATE STAMP TABLE")
        db?.let {
            createStampTable(it)
        } ?: EzLogger.d("DB NULL")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_STAMPS")
        onCreate(db)
    }

    private fun createStampTable(db: SQLiteDatabase) {
        val sql = """CREATE TABLE IF NOT EXISTS $TABLE_NAME_STAMPS($PK_ID INTEGER PRIMARY KEY AUTOINCREMENT, $STAMP_NAME_KEY TEXT, $STAMP_URI_KEY TEXT, $STAMP_WIDTH_KEY INTEGER, $STAMP_HEIGHT_KEY INTEGER, $STAMP_POS_WIDTH_PERCENT_KEY INTEGER, $STAMP_POS_HEIGHT_PERCENT_KEY INTEGER, $STAMP_POS_ANCHOR_KEY INTEGER)"""
        EzLogger.d("SQL EXEC : $sql")
        try {
            db.execSQL(sql)
            EzLogger.d("create db : $TABLE_NAME_STAMPS")
        } catch (e: Exception) {
            EzLogger.d("create table exception : " + e.toString())
        }
    }

    fun dbInsertStamp(stampName: String, imageUri: Uri): Boolean {
        db?.let {
            val stampValue = ContentValues()
            stampValue.put(STAMP_NAME_KEY, stampName)
            stampValue.put(STAMP_URI_KEY, imageUri.toString())
            stampValue.put(STAMP_WIDTH_KEY, STAMP_WIDTH_KEY_INIT_VALUE)
            stampValue.put(STAMP_HEIGHT_KEY, STAMP_HEIGHT_KEY_INIT_VALUE)
            stampValue.put(STAMP_POS_WIDTH_PERCENT_KEY, STAMP_POS_WIDTH_PERCENT_KEY_INIT_VALUE)
            stampValue.put(STAMP_POS_HEIGHT_PERCENT_KEY, STAMP_POS_HEIGHT_PERCENT_KEY_INIT_VALUE)
            stampValue.put(STAMP_POS_ANCHOR_KEY, STAMP_POS_ANCHOR_KEY_INIT_VALUE)
            val result = it.insert(TABLE_NAME_STAMPS, null, stampValue)

            return if (result.toInt() == -1) {
                EzLogger.d("insert error : name : $stampName , uri : $imageUri")
                false
            } else {
                EzLogger.d("insert success : name : $stampName , uri : $imageUri")
                true
            }
        }
        EzLogger.d("db null -> insert fail")
        return false
    }

    fun dbDeleteStamp(id: Int): Boolean {
        db?.let {
            val result = it.delete(TABLE_NAME_STAMPS, "$PK_ID=$id", null).toLong()
            return if (result.toInt() == -1) {
                EzLogger.d("delete error : id : $id")
                false
            } else {
                EzLogger.d("delete suc : id : $id")
                true
            }
        }
        EzLogger.d("db null -> delete fail")
        return false
    }

    fun dbGetAll(): ArrayList<Stamp> {
        /**
         * Read DB, on List
         * DB에서 stamp를 전부 읽어서 리스트로 불러옴
         * 이때 DB에 있는 stamp의 실제 파일이 존재하지 않을 경우 DB에서 삭제함
         */
        val stampList = ArrayList<Stamp>()
        dbHelper?.let {
            val sql = "SELECT * FROM $TABLE_NAME_STAMPS;"
            EzLogger.d("Cursor open, sql : $sql")
            it.db?.rawQuery(sql, null)?.use { cursor ->
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val stampId = cursor.getInt(0)
                    val uriPath = cursor.getString(2)
                    val uriString = Uri.parse(uriPath).path

                    EzLogger.d("uriPath : $uriPath, uriString : $uriString")

                    val stampFile = File(uriString)

                    EzLogger.d("stampFile : $stampFile")

                    if (!stampFile.exists()) {
                        dbDeleteStamp(stampId)
                        EzLogger.d("stamp file not exist -> delete -> continue")
                        cursor.moveToNext()
                        continue
                    }

                    val stamp = Stamp(
                            stampId,
                            Uri.parse(uriPath),
                            cursor.getString(1),
                            cursor.getInt(3),
                            cursor.getInt(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7)
                    )
                    EzLogger.d("stamp : $stamp")
                    stampList.add(stamp)

                    cursor.moveToNext()
                }
            }
        }
        return stampList
    }
}