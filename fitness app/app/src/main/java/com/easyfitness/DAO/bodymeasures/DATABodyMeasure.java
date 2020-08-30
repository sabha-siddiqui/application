package com.easyfitness.DAO.bodymeasures;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DATABase;
import com.easyfitness.DAO.Profile;
import com.easyfitness.enums.Unit;
import com.easyfitness.utils.DateConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DATABodyMeasure extends DATABase {
    public static final String TABLE_NAME = "EFbodymeasures";

    public static final String KEY = "_id";
    public static final String BODYPART_ID = "bodypart_id";
    public static final String MEASURE = "mesure";
    public static final String DATE = "date";
    public static final String UNIT = "unit";
    public static final String PROFIL_KEY = "profil_id";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " DATE, " + BODYPART_ID + " INTEGER, " + MEASURE + " REAL , " + PROFIL_KEY + " INTEGER, " + UNIT + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    private Profile mProfile = null;
    private Cursor mCursor = null;

    public DATABodyMeasure(Context context) {
        super(context);
    }

    public void addBodyMeasure(Date pDate, long pBodyPartId, float pMeasure, long pProfileId, Unit pUnit) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        BodyMeasure existingBodyMeasure = getBodyMeasuresFromDate(pBodyPartId, pDate, pProfileId);
        if (existingBodyMeasure==null) {

            String dateString = DateConverter.dateToDBDateStr(pDate);
            value.put(DATABodyMeasure.DATE, dateString);
            value.put(DATABodyMeasure.BODYPART_ID, pBodyPartId);
            value.put(DATABodyMeasure.MEASURE, pMeasure);
            value.put(DATABodyMeasure.PROFIL_KEY, pProfileId);
            value.put(DATABodyMeasure.UNIT, pUnit.ordinal());

            db.insert(DATABodyMeasure.TABLE_NAME, null, value);

        } else {
            existingBodyMeasure.setBodyMeasure(pMeasure);
            existingBodyMeasure.setUnit(pUnit);

            updateMeasure(existingBodyMeasure);
        }
        db.close();
    }
    public BodyMeasure getMeasure(long id) {

        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.query(TABLE_NAME,
            new String[]{KEY, DATE, BODYPART_ID, MEASURE, PROFIL_KEY, UNIT},
            KEY + "=?",
            new String[]{String.valueOf(id)},
            null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        Date date = DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DATE)));

        BodyMeasure value = new BodyMeasure(mCursor.getLong(mCursor.getColumnIndex(KEY)),
            date,
            mCursor.getInt(mCursor.getColumnIndex(BODYPART_ID)),
            mCursor.getFloat(mCursor.getColumnIndex(MEASURE)),
            mCursor.getLong(mCursor.getColumnIndex(PROFIL_KEY)),
            Unit.fromInteger(mCursor.getInt(mCursor.getColumnIndex(UNIT)))
        );

        db.close();
        return value;
    }
    public List<BodyMeasure> getMeasuresList(SQLiteDatabase db, String pRequest) {
        List<BodyMeasure> valueList = new ArrayList<>();


        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);
        if (mCursor.moveToFirst()) {
            do {
                Date date = DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DATE)));

                BodyMeasure value = new BodyMeasure(mCursor.getLong(mCursor.getColumnIndex(KEY)),
                    date,
                    mCursor.getInt(mCursor.getColumnIndex(BODYPART_ID)),
                    mCursor.getFloat(mCursor.getColumnIndex(MEASURE)),
                    mCursor.getLong(mCursor.getColumnIndex(PROFIL_KEY)),
                    Unit.fromInteger(mCursor.getInt(mCursor.getColumnIndex(UNIT)))
                );
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        return valueList;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public List<BodyMeasure> getBodyPartMeasuresList(long pBodyPartID, Profile pProfile) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY date(" + DATE + ") DESC";

        return getMeasuresList(getReadableDatabase(), selectQuery);
    }

    public List<BodyMeasure> getBodyPartMeasuresListTop4(long pBodyPartID, Profile pProfile) {
        if (pProfile==null) return null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY date(" + DATE + ") DESC LIMIT 4;";
        return getMeasuresList(getReadableDatabase(), selectQuery);
    }
    public List<BodyMeasure> getBodyMeasuresList(Profile pProfile) {
        if (pProfile==null) return null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY date(" + DATE + ") DESC";
        return getMeasuresList(getReadableDatabase(), selectQuery);
    }

    public List<BodyMeasure> getAllBodyMeasures() {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY date(" + DATE + ") DESC";
        return getMeasuresList(getReadableDatabase(), selectQuery);
    }
    public BodyMeasure getLastBodyMeasures(long pBodyPartID, Profile pProfile) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY date(" + DATE + ") DESC";

        List<BodyMeasure> array = getMeasuresList(getReadableDatabase(), selectQuery);
        if (array.size() <= 0) {
            return null;
        }
        return getMeasuresList(getReadableDatabase(), selectQuery).get(0);
    }

    public BodyMeasure getBodyMeasuresFromDate(long pBodyPartID, Date pDate, long pProfileId) {
        String dateString = DateConverter.dateToDBDateStr(pDate);
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + DATE + "=\"" + dateString + "\" AND " + PROFIL_KEY + "=" + pProfileId + " ORDER BY date(" + DATE + ") DESC";

        List<BodyMeasure> array = getMeasuresList(getReadableDatabase(), selectQuery);
        if (array.size() <= 0) {
            return null;
        }

        return getMeasuresList(getReadableDatabase(), selectQuery).get(0);
    }
    public int updateMeasure(BodyMeasure m) {
        return updateMeasure(getWritableDatabase(), m);
    }
    public int updateMeasure(SQLiteDatabase db, BodyMeasure m) {
        ContentValues value = new ContentValues();
        String dateString = DateConverter.dateToDBDateStr(m.getDate());
        value.put(DATABodyMeasure.DATE, dateString);
        value.put(DATABodyMeasure.BODYPART_ID, m.getBodyPartID());
        value.put(DATABodyMeasure.MEASURE, m.getBodyMeasure());
        value.put(DATABodyMeasure.PROFIL_KEY, m.getProfileID());
        value.put(DATABodyMeasure.UNIT, m.getUnit().ordinal());

        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }

    public void deleteMeasure(long id) {
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

    public void populate() {
        Date date = new Date();
        int poids = 10;

        for (int i = 1; i <= 5; i++) {
            date.setTime(date.getTime() + i * 1000 * 60 * 60 * 24 * 2);
        }
    }
}


