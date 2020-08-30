package com.easyfitness.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.utils.DateConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DATAProfileWeight extends DATABase {


    public static final String TABLE_NAME = "EFweight";

    public static final String KEY = "_id";
    public static final String POIDS = "poids";
    public static final String DATE = "date";
    public static final String PROFIL_KEY = "profil_id";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " DATE, " + POIDS + " REAL , " + PROFIL_KEY + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    private Profile mProfile = null;
    private Cursor mCursor = null;

    public DATAProfileWeight(Context context) {
        super(context);
    }

    public void setProfil(Profile pProfile) {
        mProfile = pProfile;
    }

    public void addWeight(Date pDate, float pWeight, Profile pProfile) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        value.put(DATAProfileWeight.DATE, dateFormat.format(pDate));
        value.put(DATAProfileWeight.POIDS, pWeight);
        value.put(DATAProfileWeight.PROFIL_KEY, pProfile.getId());

        db.insert(DATAProfileWeight.TABLE_NAME, null, value);
        db.close();
    }
    private ProfileWeight getMeasure(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.query(TABLE_NAME,
            new String[]{KEY, DATE, POIDS, PROFIL_KEY},
            KEY + "=?",
            new String[]{String.valueOf(id)},
            null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        Date date;
        date = DateConverter.DBDateStrToDate(mCursor.getString(1));

        ProfileWeight value = new ProfileWeight(mCursor.getLong(0),
            date,
            mCursor.getFloat(2),
            mCursor.getLong(3)
        );

        db.close();
        return value;
    }
    public ProfileWeight getLastMeasure() {
        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.query(TABLE_NAME,
            new String[]{KEY, DATE, POIDS, PROFIL_KEY},
            PROFIL_KEY + "=?",
            new String[]{String.valueOf(mProfile.getId())},
            null, null, DATE + " desc, " + KEY + " desc", null);

        if (mCursor != null)
            mCursor.moveToFirst();

        Date date;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = dateFormat.parse(mCursor.getString(1));
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }

        ProfileWeight value = new ProfileWeight(mCursor.getLong(0),
            date,
            mCursor.getFloat(2),
            mCursor.getLong(3)
        );

        db.close();
        return value;
    }
    private List<ProfileWeight> getMeasuresList(String pRequest) {
        List<ProfileWeight> valueList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);
        if (mCursor.moveToFirst()) {
            do {
                Date date;
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    date = dateFormat.parse(mCursor.getString(1));
                } catch (ParseException e) {
                    e.printStackTrace();
                    date = new Date();
                }

                ProfileWeight value = new ProfileWeight(mCursor.getLong(0),
                    date,
                    mCursor.getFloat(2),
                    mCursor.getLong(3)
                );
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        return valueList;
    }

    public Cursor GetCursor() {
        return mCursor;
    }
    public List<ProfileWeight> getWeightList(Profile pProfile) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.getId() + " GROUP BY " + DATE + " ORDER BY date(" + DATE + ") DESC";
        return getMeasuresList(selectQuery);
    }
    public int updateMeasure(ProfileWeight m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(DATAProfileWeight.DATE, m.getDate().toString());
        value.put(DATAProfileWeight.POIDS, m.getWeight());
        value.put(DATAProfileWeight.PROFIL_KEY, m.getProfilId());
        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }
    public void deleteMeasure(ProfileWeight m) {
        deleteMeasure(m.getId());
    }
    public void deleteMeasure(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY + " = ?",
            new String[]{String.valueOf(id)});
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

    public List<ProfileWeight> getAllRecords() {
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        return getMeasuresList(selectQuery);
    }

    public void populate() {
        Date date = new Date();
        int poids = 10;

        for (int i = 1; i <= 5; i++) {
            date.setTime(date.getTime() + i * 1000 * 60 * 60 * 24 * 2);
            addWeight(date, (float) i, mProfile);
        }
    }
}


