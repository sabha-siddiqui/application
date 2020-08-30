package com.easyfitness.DAO.program;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DATABase;

import java.util.ArrayList;
import java.util.List;

public class DATAProgram extends DATABase {

    public static final String TABLE_NAME = "EFworkout";

    public static final String KEY = "_id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " TEXT, " + DESCRIPTION + " TEXT);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    private Cursor mCursor = null;



    public DATAProgram(Context context) {
        super(context);
    }

    public long add(Program m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DATAProgram.NAME, m.getName());
        value.put(DATAProgram.DESCRIPTION, m.getDescription());

        long new_id = db.insert(DATAProgram.TABLE_NAME, null, value);

        close();
        return new_id;
    }

    public Program get(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (mCursor != null) mCursor.close();
        mCursor = null;
        mCursor = db.query(TABLE_NAME,
            new String[]{KEY, NAME, DESCRIPTION},
            KEY + "=?",
            new String[]{String.valueOf(id)},
            null, null, null, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();

            Program value = new Program(mCursor.getLong(mCursor.getColumnIndex(DATAProgram.KEY)),
                mCursor.getString(mCursor.getColumnIndex(DATAProgram.NAME)),
                mCursor.getString(mCursor.getColumnIndex(DATAProgram.DESCRIPTION))
            );
            mCursor.close();
            close();
            return value;
        } else {
            mCursor.close();
            close();
            return null;
        }
    }
    public List<Program> getList(String pRequest) {
        List<Program> valueList = new ArrayList<>();


        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);
        if (mCursor.moveToFirst()) {
            do {
                Program value = new Program(mCursor.getLong(mCursor.getColumnIndex(DATAProgram.KEY)),
                    mCursor.getString(mCursor.getColumnIndex(DATAProgram.NAME)),
                    mCursor.getString(mCursor.getColumnIndex(DATAProgram.DESCRIPTION))
                );

                valueList.add(value);
            } while (mCursor.moveToNext());
        }

        close();
        return valueList;
    }
    public List<Program> getAll() {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + NAME + " ASC";
        return getList(selectQuery);
    }

    public int update(Program m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(DATAProgram.NAME, m.getName());
        value.put(DATAProgram.DESCRIPTION, m.getDescription());
        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }

    public void delete(Program m) {
        delete(m.getId());
    }

    public void delete(long id) {
        open();

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY + " = ?",
            new String[]{String.valueOf(id)});

        close();
    }

    public int getCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        open();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int value = cursor.getCount();
        cursor.close();
        close();
        return value;
    }

    public void populate() {
        Program m = new Program(0,"Template 1", "Description 1");
        this.add(m);
        m = new Program(0,"Template 2", "Description 2");
        this.add(m);
    }
    public void deleteAllEmptyWorkout() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,  NAME + "=?",
            new String[]{""});
        db.close();
    }
}
