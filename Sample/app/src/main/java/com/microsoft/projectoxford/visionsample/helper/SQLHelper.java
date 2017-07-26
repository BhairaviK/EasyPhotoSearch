package com.microsoft.projectoxford.visionsample.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by bkanna on 7/26/2017.
 */

public class SQLHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "EasyPhotoFind.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLENAME = "photo_info";
    private static final String COLUMN_URI = "uri";;
    private static final String COLUMN_TAGS = "tags";
    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override

    public void onCreate(SQLiteDatabase database) {
        try {
            String SQL = String.format("CREATE TABLE IF NOT EXISTS %s(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, %s STRING NOT NULL, %s STRING);", TABLENAME, COLUMN_URI, COLUMN_TAGS);
            database.execSQL(SQL);
        }
        catch(Exception e){

        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public void insert(Uri uri, String tags) {
       // printAll();
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_URI, uri.toString());
            values.put(COLUMN_TAGS, tags);
            long r = database.insertOrThrow(TABLENAME, COLUMN_TAGS, values);
            Log.d("sqlhelper", String.valueOf(r));
            if(r!=-1)
                database.setTransactionSuccessful();
        }
        catch (Exception e){
            Log.e("sqlhelper", e.getMessage());
        }
        finally {
            database.endTransaction();
        }

    }

    public void printAll() {
        SQLiteDatabase database = getReadableDatabase();
        try {
            String SQL = "SELECT " + COLUMN_URI + " FROM "+  TABLENAME;
            Cursor cursor = database.rawQuery(SQL, null);
            if(cursor.moveToFirst()) {
                do {
                    Log.d("sqlhelper", cursor.getString(0));
                }while(cursor.moveToNext());
            }
            SQLiteDatabase database1 = getReadableDatabase();
            String SQL1 = "SELECT " + COLUMN_TAGS + " FROM "+  TABLENAME;
            Cursor cursor1 = database1.rawQuery(SQL1, null);
            if(cursor1.moveToFirst()) {
                do {
                    Log.d("sqlhelper", cursor1.getString(0));
                }while(cursor1.moveToNext());
            }
        }
        catch (Exception e) {
Log.e("sqlhelper", e.getMessage());
        }

    }

    public ArrayList<String>  retrieve(String tags) {
        ArrayList<String> result = new ArrayList<String>();
        SQLiteDatabase database = getReadableDatabase();
        String[] separatedTags = tags.split(" ");
        if(separatedTags.length <= 0)
            return result;
        try {
            String SQL = "SELECT "+ COLUMN_URI + " FROM " + TABLENAME + " WHERE " + COLUMN_TAGS + " like '%" + separatedTags[0] +"%'";
            for (int i=1; i<separatedTags.length; i++) {
                SQL += " AND " + COLUMN_TAGS + " like '%" + separatedTags[i] +"%'";
            }
            Cursor cursor = database.rawQuery(SQL, null);
            if (cursor.moveToFirst()) {
                do {
                    result.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }

            if (cursor != null && !cursor.isClosed())
                cursor.close();
        } finally {
        }
        return result;
    }
}
