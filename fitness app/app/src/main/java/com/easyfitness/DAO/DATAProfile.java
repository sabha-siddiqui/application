package com.easyfitness.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.DATABodyMeasure;
import com.easyfitness.utils.DateConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DATAProfile extends DATABase {
    public static final String TABLE_NAME = "EFprofil";

    public static final String KEY = "_id";
    public static final String NAME = "name";
    public static final String CREATIONDATE = "creationdate";
    public static final String SIZE = "size";
    public static final String BIRTHDAY = "birthday";
    public static final String PHOTO = "photo";
    public static final String GENDER = "gender";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CREATIONDATE + " DATE, " + NAME + " TEXT, " + SIZE + " INTEGER, " + BIRTHDAY + " DATE, " + PHOTO + " TEXT, " + GENDER + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    private Cursor mCursor = null;



    public DATAProfile(Context context) {
        super(context);
    }
    public void addProfil(Profile m) {
        Profile check = getProfil(m.getName());
        if (check != null) return;

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DATAProfile.CREATIONDATE, DateConverter.dateToDBDateStr(new Date()));
        value.put(DATAProfile.NAME, m.getName());
        value.put(DATAProfile.BIRTHDAY, DateConverter.dateToDBDateStr(m.getBirthday()));
        value.put(DATAProfile.SIZE, m.getSize());
        value.put(DATAProfile.PHOTO, m.getPhoto());
        value.put(DATAProfile.GENDER, m.getGender());

        db.insert(DATAProfile.TABLE_NAME, null, value);

        close();
    }
    public void addProfil(String pName) {
        Profile check = getProfil(pName);
        if (check != null) return;

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DATAProfile.CREATIONDATE, DateConverter.dateToDBDateStr(new Date()));
        value.put(DATAProfile.NAME, pName);

        db.insert(DATAProfile.TABLE_NAME, null, value);

        close();
    }
    public Profile getProfil(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (mCursor != null) mCursor.close();
        mCursor = null;
        mCursor = db.query(TABLE_NAME,
            new String[]{KEY, CREATIONDATE, NAME, SIZE, BIRTHDAY, PHOTO, GENDER},
            KEY + "=?",
            new String[]{String.valueOf(id)},
            null, null, null, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();

            Profile value = new Profile(mCursor.getLong(mCursor.getColumnIndex(DATAProfile.KEY)),
                DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DATAProfile.CREATIONDATE))),
                mCursor.getString(mCursor.getColumnIndex(DATAProfile.NAME)),
                mCursor.getInt(mCursor.getColumnIndex(DATAProfile.SIZE)),
                mCursor.getString(mCursor.getColumnIndex(DATAProfile.BIRTHDAY)) != null ? DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DATAProfile.BIRTHDAY))) : new Date(0),
                mCursor.getString(mCursor.getColumnIndex(DATAProfile.PHOTO)),
                mCursor.getInt(mCursor.getColumnIndex(DATAProfile.GENDER))
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
    public Profile getProfil(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (mCursor != null) mCursor.close();
        mCursor = null;
        mCursor = db.query(TABLE_NAME,
            new String[]{KEY, CREATIONDATE, NAME, SIZE, BIRTHDAY, PHOTO, GENDER},
            NAME + "=?",
            new String[]{name},
            null, null, null, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();

            Profile value = new Profile(mCursor.getLong(mCursor.getColumnIndex(DATAProfile.KEY)),
                DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DATAProfile.CREATIONDATE))),
                mCursor.getString(mCursor.getColumnIndex(DATAProfile.NAME)),
                mCursor.getInt(mCursor.getColumnIndex(DATAProfile.SIZE)),
                mCursor.getString(mCursor.getColumnIndex(DATAProfile.BIRTHDAY)) != null ? DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DATAProfile.BIRTHDAY))) : new Date(0),
                mCursor.getString(mCursor.getColumnIndex(DATAProfile.PHOTO)),
                mCursor.getInt(mCursor.getColumnIndex(DATAProfile.GENDER))
            );

            mCursor.close();
            close();
            return value;
        } else {
            close();
            return null;
        }

    }
    public List<Profile> getProfilsList(String pRequest) {
        List<Profile> valueList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);
        if (mCursor.moveToFirst()) {
            do {
                Profile value = new Profile(mCursor.getLong(mCursor.getColumnIndex(DATAProfile.KEY)),
                    DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DATAProfile.CREATIONDATE))),
                    mCursor.getString(mCursor.getColumnIndex(DATAProfile.NAME)),
                    mCursor.getInt(mCursor.getColumnIndex(DATAProfile.SIZE)),
                    mCursor.getString(mCursor.getColumnIndex(DATAProfile.BIRTHDAY)) != null ? DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DATAProfile.BIRTHDAY))) : new Date(0),
                    mCursor.getString(mCursor.getColumnIndex(DATAProfile.PHOTO)),
                    mCursor.getInt(mCursor.getColumnIndex(DATAProfile.GENDER))
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
    public List<Profile> getAllProfils() {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + KEY + " DESC";
        return getProfilsList(selectQuery);
    }
    public List<Profile> getTop10Profils() {
        String selectQuery = "SELECT TOP 10 * FROM " + TABLE_NAME + " ORDER BY " + KEY + " DESC";
        return getProfilsList(selectQuery);
    }
    public String[] getAllProfil() {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        String selectQuery = "SELECT DISTINCT  " + NAME + " FROM " + TABLE_NAME + " ORDER BY " + NAME + " ASC";
        mCursor = db.rawQuery(selectQuery, null);

        int size = mCursor.getCount();

        String[] valueList = new String[size];
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                String value = mCursor.getString(0);
                valueList[i] = value;
                i++;
            } while (mCursor.moveToNext());
        }

        close();
        return valueList;
    }
    public Profile getLastProfil() {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        String selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME;
        mCursor = db.rawQuery(selectQuery, null);
        mCursor.moveToFirst();
        long value = Long.parseLong(mCursor.getString(0));

        Profile prof = this.getProfil(value);
        mCursor.close();
        close();
        return prof;
    }
    public int updateProfile(Profile m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(DATAProfile.CREATIONDATE, DateConverter.dateToDBDateStr(m.getCreationDate()));
        value.put(DATAProfile.NAME, m.getName());
        value.put(DATAProfile.BIRTHDAY, DateConverter.dateToDBDateStr(m.getBirthday()));
        value.put(DATAProfile.SIZE, m.getSize());
        value.put(DATAProfile.PHOTO, m.getPhoto());
        value.put(DATAProfile.GENDER, m.getGender());
        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }
    public void deleteProfil(Profile m) {
        deleteProfil(m.getId());
    }
    public void deleteProfil(long id) {
        open();
        DATAProfileWeight mWeightDb;
        mWeightDb = new DATAProfileWeight(null);
        List<ProfileWeight> valueList = mWeightDb.getWeightList(getProfil(id));
        for (int i = 0; i < valueList.size(); i++) {
            mWeightDb.deleteMeasure(valueList.get(i).getId());
        }
        DATABodyMeasure mBodyDb;
        mBodyDb = new DATABodyMeasure(null);
        List<BodyMeasure> bodyMeasuresList = mBodyDb.getBodyMeasuresList(getProfil(id));
        for (int i = 0; i < bodyMeasuresList.size(); i++) {
            mBodyDb.deleteMeasure(bodyMeasuresList.get(i).getId());
        }
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
        Date date = new Date();
        Date dateBirthday = DateConverter.getNewDate();
        Profile m = new Profile(0, date, "Champignon", 120, dateBirthday, null, 0);
        this.addProfil(m);
        m = new Profile(0, date, "Musclor", 150, dateBirthday, null, 0);
        this.addProfil(m);
    }
}
