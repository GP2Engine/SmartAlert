package com.example.smartalert;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class dbHelper extends SQLiteOpenHelper {      //Local SQLite database management
    public dbHelper(@Nullable Context context) {
        super(context, "mydatabase.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table users(uname text primary key, pass text)");
        db.execSQL("Create table events(id integer primary key AUTOINCREMENT, uname text, event_type text, longitude double, latitude double, timestamp datetime, image blob)");

        db.execSQL("Insert into users(uname, pass) VALUES('tasos', '1234')");   //Inserting 2 ready accounts for quick testing
        db.execSQL("Insert into users(uname, pass) VALUES('bill', '1234')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists users");
        db.execSQL("drop table if exists events");
    }

    //insert users in database
    public boolean insertUser(String uname, String pass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uname",uname);
        contentValues.put("pass",pass);
        long ins = db.insert("users", null, contentValues);
        if(ins==-1) return false;
        else return true;
    }

    //change users' password
    public boolean changePass(String uname, String oldpass, String newpass){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uname",uname);
        contentValues.put("pass",newpass);
        long ins = db.update("users", contentValues, "uname=? and pass=?", new String[]{uname,oldpass});
        if(ins==-1) return false;
        else return true;
    }

    //Check for username duplicates
    public Boolean checkuname(String uname) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from users where uname=?", new String[]{uname});
        if (cursor.getCount()>0) return false;
        else return true;
    }

    //Check credentials' validity
    public Boolean validcreds(String uname,String pass){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from users where uname=? and pass=?", new String[]{uname,pass});
        if(cursor.getCount()>0) return true;
        else return false;
    }

    //Getting current timestamp
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    //insert fall related events in database
    public boolean insertFallEvent(String uname, String event_type, double longitude, double latitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uname",uname);
        contentValues.put("event_type", event_type);
        contentValues.put("longitude",longitude);
        contentValues.put("latitude",latitude);
        contentValues.put("timestamp",getDateTime());
        long ins = db.insert("events", null, contentValues);
        if(ins==-1) return false;
        else return true;
    }

    //insert earthquake events in database
    public boolean insertEarthquakeEvent(String uname, String event_type, double longitude, double latitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uname",uname);
        contentValues.put("event_type", event_type);
        contentValues.put("longitude",longitude);
        contentValues.put("latitude",latitude);
        contentValues.put("timestamp",getDateTime());
        long ins = db.insert("events", null, contentValues);
        if(ins==-1) return false;
        else return true;
    }

    //insert fire report events in database
    public boolean insertFireEvent(String uname, String event_type, double longitude, double latitude, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uname",uname);
        contentValues.put("event_type", event_type);
        contentValues.put("longitude",longitude);
        contentValues.put("latitude",latitude);
        contentValues.put("timestamp",getDateTime());
        contentValues.put("image",image);
        long ins = db.insert("events", null, contentValues);
        if(ins==-1) return false;
        else return true;
    }

    //getting the fall event logs of a specific user
    public Cursor getUserFalls(String uname) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from events where uname=? and event_type like '%Fall%'", new String[]{uname});
        return cursor;
    }

    //getting the earthquake sense logs of a specific user
    public Cursor getUserEarthquakes(String uname) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from events where uname=? and event_type like '%Earthquake%'", new String[]{uname});
        return cursor;
    }

    //getting the fire reports of a specific user
    public Cursor getUserFires(String uname) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from events where uname=? and event_type like '%Fire%'", new String[]{uname});
        return cursor;
    }

    //getting the logs of the fall events of a specific timestamp
    public Cursor getUserFallsByTime(String uname, String timestamp) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from events where timestamp=? and uname=? and event_type like '%Fall%'", new String[]{timestamp, uname});
        return cursor;
    }

    //getting the logs of the earthquake senses of a specific timestamp
    public Cursor getUserEarthquakesByTime(String uname, String timestamp) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from events where timestamp=? and uname=? and event_type like '%Earthquake%'", new String[]{timestamp, uname});
        return cursor;
    }

    //getting the fire reports of a specific timestamp
    public Cursor getUserFiresByTime(String uname, String timestamp) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from events where timestamp=? and uname=? and event_type like '%Fire%'", new String[]{timestamp, uname});
        return cursor;
    }

}
