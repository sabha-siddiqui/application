package com.easyfitness.DAO.bodymeasures;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DATABase;

import java.util.ArrayList;
import java.util.List;

public class DATABodyPart extends DATABase {

    public static final String TABLE_NAME = "EFbodyparts";

    public static final String KEY = "_id";
    public static final String BODYPART_RESID = "bodypart_id";
    public static final String CUSTOM_NAME = "custom_name";
    public static final String CUSTOM_PICTURE = "custom_picture";
    public static final String DISPLAY_ORDER = "display_order";
    public static final String TYPE = "type";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + BODYPART_RESID + " INTEGER, " + CUSTOM_NAME + " TEXT, " + CUSTOM_PICTURE + " TEXT, "+ DISPLAY_ORDER + " INTEGER, " + TYPE + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    private Cursor mCursor = null;

    public DATABodyPart(Context context) {
        super(context);
    }

    public long add(int pBodyPartId, String pCustomName, String pCustomPicture, int pDisplay, int pType) {
        long new_id = -1;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DATABodyPart.BODYPART_RESID, pBodyPartId);
        value.put(DATABodyPart.CUSTOM_NAME, pCustomName);
        value.put(DATABodyPart.CUSTOM_PICTURE, pCustomPicture);
        value.put(DATABodyPart.DISPLAY_ORDER, pDisplay);
        value.put(DATABodyPart.TYPE, pType);

        new_id = db.insert(DATABodyPart.TABLE_NAME, null, value);
        db.close();
        return  new_id;
    }

    public BodyPart getBodyPart(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        mCursor = null;
        mCursor = db.query(TABLE_NAME,
            new String[]{KEY, BODYPART_RESID, CUSTOM_NAME, CUSTOM_PICTURE, DISPLAY_ORDER, TYPE},
            KEY + "=?",
            new String[]{String.valueOf(id)},
            null, null, null, null);
        BodyPart value = null;
        if (mCursor != null && mCursor.getCount()!=0) {
            mCursor.moveToFirst();

            value = new BodyPart(mCursor.getLong(mCursor.getColumnIndex(KEY)),
                mCursor.getInt(mCursor.getColumnIndex(BODYPART_RESID)),
                mCursor.getString(mCursor.getColumnIndex(CUSTOM_NAME)),
                mCursor.getString(mCursor.getColumnIndex(CUSTOM_PICTURE)),
                mCursor.getInt(mCursor.getColumnIndex(DISPLAY_ORDER)),
                mCursor.getInt(mCursor.getColumnIndex(TYPE))
            );
        }

        db.close();
        return value;
    }

    public BodyPart getBodyPartfromBodyPartKey(long bodyPartKey) {
        SQLiteDatabase db = this.getWritableDatabase();

        mCursor = null;
        mCursor = db.query(TABLE_NAME,
            new String[]{KEY, BODYPART_RESID, CUSTOM_NAME, CUSTOM_PICTURE, DISPLAY_ORDER, TYPE},
            BODYPART_RESID + "=?",
            new String[]{String.valueOf(bodyPartKey)},
            null, null, null, null);
        BodyPart value = null;
        if (mCursor != null && mCursor.getCount()!=0) {
            mCursor.moveToFirst();

            value = new BodyPart(mCursor.getLong(mCursor.getColumnIndex(KEY)),
                mCursor.getInt(mCursor.getColumnIndex(BODYPART_RESID)),
                mCursor.getString(mCursor.getColumnIndex(CUSTOM_NAME)),
                mCursor.getString(mCursor.getColumnIndex(CUSTOM_PICTURE)),
                mCursor.getInt(mCursor.getColumnIndex(DISPLAY_ORDER)),
                mCursor.getInt(mCursor.getColumnIndex(TYPE))
            );
        }

        db.close();
        return value;
    }
    public List<BodyPart> getList() {
        return getList("SELECT * FROM " + TABLE_NAME  + " ORDER BY " + DISPLAY_ORDER + " ASC");
    }
    public List<BodyPart> getMusclesList() {
        return getList("SELECT * FROM " + TABLE_NAME  + " WHERE " + TYPE + "=" + BodyPartExtensions.TYPE_MUSCLE +  " ORDER BY " + DISPLAY_ORDER + " ASC");
    }
    private List<BodyPart> getList(String pRequest) {
        List<BodyPart> valueList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        if (mCursor.moveToFirst()) {
            do {
                BodyPart value = new BodyPart(mCursor.getLong(mCursor.getColumnIndex(this.KEY)),
                    mCursor.getInt(mCursor.getColumnIndex(this.BODYPART_RESID)),
                    mCursor.getString(mCursor.getColumnIndex(this.CUSTOM_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(this.CUSTOM_PICTURE)),
                    mCursor.getInt(mCursor.getColumnIndex(this.DISPLAY_ORDER)),
                    mCursor.getInt(mCursor.getColumnIndex(this.TYPE))
                );

                valueList.add(value);
            } while (mCursor.moveToNext());
        }

        return valueList;
    }

    public Cursor getCursor() {
        return mCursor;
    }
    public int update(BodyPart m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(DATABodyPart.BODYPART_RESID, m.getBodyPartResKey());
        value.put(DATABodyPart.CUSTOM_NAME, m.getCustomName());
        value.put(DATABodyPart.CUSTOM_PICTURE, m.getCustomPicture());
        value.put(DATABodyPart.DISPLAY_ORDER, m.getDisplayOrder());
        value.put(DATABodyPart.TYPE, m.getType());

        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }
    public void delete(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY + " = ?",
            new String[]{String.valueOf(id)});
    }
    public int getCount() {
        String countQuery = "SELECT * FROM " + TABLE_NAME;
        open();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int value = cursor.getCount();
        cursor.close();
        close();
        return value;
    }

    public void deleteAllEmptyBodyPart() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, this.BODYPART_RESID + "=? " + " AND " + this.CUSTOM_NAME + "=?",
            new String[]{"-1", ""});
        db.close();
    }

}


