package com.easyfitness.DAO.program;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DATABase;
import com.easyfitness.DAO.Profile;
import com.easyfitness.enums.ProgramStatus;

import java.util.ArrayList;
import java.util.List;

public class DATAProgramHistory extends DATABase {


    public static final String TABLE_NAME = "EFworkoutHistory";

    public static final String KEY = "_id";
    public static final String PROGRAM_KEY = "program_key";
    public static final String PROFILE_KEY = "profile_key";
    public static final String STATUS = "description";
    public static final String START_DATE = "start_date";
    public static final String START_TIME = "start_time";
    public static final String END_DATE = "end_date";
    public static final String END_TIME = "end_time";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        PROGRAM_KEY + " INTEGER, " +
        PROFILE_KEY + " INTEGER, " +
        STATUS + " INTEGER, " +
        START_DATE + " TEXT, " +
        START_TIME + " TEXT, " +
        END_DATE + " TEXT, " +
        END_TIME + " TEXT);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    private Cursor mCursor = null;

    public DATAProgramHistory(Context context) {
        super(context);
    }

    public long add(ProgramHistory m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DATAProgramHistory.PROGRAM_KEY, m.getProgramId());
        value.put(DATAProgramHistory.PROFILE_KEY, m.getProfileId());
        value.put(DATAProgramHistory.START_DATE, m.getStartDate());
        value.put(DATAProgramHistory.START_TIME, m.getStartTime());
        value.put(DATAProgramHistory.END_DATE, m.getEndDate());
        value.put(DATAProgramHistory.END_TIME, m.getEndTime());
        value.put(DATAProgramHistory.STATUS, m.getStatus().ordinal());

        long new_id = db.insert(DATAProgramHistory.TABLE_NAME, null, value);

        close();
        return new_id;
    }

    public ProgramHistory get(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (mCursor != null) mCursor.close();
        mCursor = null;
        mCursor = db.query(TABLE_NAME,
            new String[]{KEY, PROGRAM_KEY, PROFILE_KEY, STATUS, START_DATE, START_TIME, END_DATE, END_TIME},
            KEY + "=?",
            new String[]{String.valueOf(id)},
            null, null, null, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();

            ProgramHistory value = new ProgramHistory(mCursor.getLong(mCursor.getColumnIndex(DATAProgramHistory.KEY)),
                mCursor.getInt(mCursor.getColumnIndex(DATAProgramHistory.PROGRAM_KEY)),
                mCursor.getInt(mCursor.getColumnIndex(DATAProgramHistory.PROFILE_KEY)),
                ProgramStatus.fromInteger(mCursor.getInt(mCursor.getColumnIndex(DATAProgramHistory.STATUS))),
                mCursor.getString(mCursor.getColumnIndex(DATAProgramHistory.START_DATE)),
                mCursor.getString(mCursor.getColumnIndex(DATAProgramHistory.START_TIME)),
                mCursor.getString(mCursor.getColumnIndex(DATAProgramHistory.END_DATE)),
                mCursor.getString(mCursor.getColumnIndex(DATAProgramHistory.END_TIME))
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
    public List<ProgramHistory> getList(String pRequest) {
        List<ProgramHistory> valueList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        if (mCursor.moveToFirst()) {
            do {
                ProgramHistory value = new ProgramHistory(mCursor.getLong(mCursor.getColumnIndex(DATAProgramHistory.KEY)),
                    mCursor.getInt(mCursor.getColumnIndex(DATAProgramHistory.PROGRAM_KEY)),
                    mCursor.getInt(mCursor.getColumnIndex(DATAProgramHistory.PROFILE_KEY)),
                    ProgramStatus.fromInteger(mCursor.getInt(mCursor.getColumnIndex(DATAProgramHistory.STATUS))),
                    mCursor.getString(mCursor.getColumnIndex(DATAProgramHistory.START_DATE)),
                    mCursor.getString(mCursor.getColumnIndex(DATAProgramHistory.START_TIME)),
                    mCursor.getString(mCursor.getColumnIndex(DATAProgramHistory.END_DATE)),
                    mCursor.getString(mCursor.getColumnIndex(DATAProgramHistory.END_TIME))
                );
                valueList.add(value);
            } while (mCursor.moveToNext());
        }

        close();
        return valueList;
    }

    public Cursor GetCursor() {
        return mCursor;
    }

    public List<ProgramHistory> getAll() {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + KEY + " DESC";

        return getList(selectQuery);
    }

    public ProgramHistory getRunningProgram(Profile profile) {
        String selectQuery = "";
        if (profile==null)
          selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + STATUS + "=" + ProgramStatus.RUNNING.ordinal() + " ORDER BY " + KEY + " DESC";
        else
          selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + STATUS + "=" + ProgramStatus.RUNNING.ordinal() + " AND " + PROFILE_KEY + "=" + profile.getId() + " ORDER BY " + KEY + " DESC";

        List<ProgramHistory> list = getList(selectQuery);
        if (list.size()>0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public int update(ProgramHistory m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(DATAProgramHistory.PROGRAM_KEY, m.getProgramId());
        value.put(DATAProgramHistory.PROFILE_KEY, m.getProfileId());
        value.put(DATAProgramHistory.START_DATE, m.getStartDate());
        value.put(DATAProgramHistory.START_TIME, m.getStartTime());
        value.put(DATAProgramHistory.END_DATE, m.getEndDate());
        value.put(DATAProgramHistory.END_TIME, m.getEndTime());
        value.put(DATAProgramHistory.STATUS, m.getStatus().ordinal());

        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }
    public void delete(ProgramHistory m) {
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
    }
}
