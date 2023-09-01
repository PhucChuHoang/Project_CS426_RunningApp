package com.example.project_cs426_runningapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.IOException

class SQLiteDBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?,
                     DATABASE_NAME: String, DATABASE_VERSION: Int):
        SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val query = ("CREATE TABLE EVENT " + "(" +
            "event_name nvarchar(50) PRIMARY KEY, " +
            "status boolean)")

        if (db != null) {
            db.execSQL(query)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (db != null) {
            db.execSQL("DROP TABLE IF EXISTS EVENT")
        }
        onCreate(db)
    }

    fun addEvent(event_name: String, status: Boolean?) {
        val values = ContentValues()

        values.put("event_name", event_name)
        values.put("status", status)

        val db = this.writableDatabase

        db.insert("EVENT", null, values)

        db.close()
    }

    fun getEvent(): Cursor? {
        val db = this.readableDatabase

        return db.rawQuery("SELECT * FROM EVENT", null)
    }

    fun clearTable() {
        val db = this.writableDatabase

        db.execSQL("DELETE FROM EVENT")
    }
}