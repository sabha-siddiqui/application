package com.easyfitness.DAO.cardio;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.easyfitness.DAO.DATABase;
import com.easyfitness.DAO.DATAProfile;
import com.easyfitness.DAO.DAOUtils;
import com.easyfitness.DAO.Profile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DATAOldCardio extends DATABase {

    public static final String TABLE_NAME = "EFcardio";

    public static final String KEY = "_id";
    public static final String DATE = "date";
    public static final String EXERCICE = "exercice";
    public static final String DISTANCE = "distance";
    public static final String DURATION = "duration";
    public static final String PROFIL_KEY = "profil_id";
    public static final String NOTES = "notes";
    public static final String DISTANCE_UNIT = "distance_unit";
    public static final String VITESSE = "vitesse";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
        + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + DATE + " DATE, "
        + EXERCICE + " TEXT, "
        + DISTANCE + " FLOAT, "
        + DURATION + " INTEGER, "
        + PROFIL_KEY + " INTEGER, "
        + NOTES + " TEXT, "
        + DISTANCE_UNIT + " TEXT, "
        + VITESSE + " FLOAT);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    private Cursor mCursor = null;
    private Context mContext = null;

    public DATAOldCardio(Context context) {
        super(context);
        mContext = context;
    }

    private List<OldCardio> getRecordsList(String pRequest) {
        List<OldCardio> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        if (mCursor.moveToFirst()) {
            do {
                Date date;
                try {
                    date = new SimpleDateFormat(DAOUtils.DATE_FORMAT).parse(mCursor.getString(mCursor.getColumnIndex(DATAOldCardio.DATE)));
                } catch (ParseException e) {
                    e.printStackTrace();
                    date = new Date();
                }

                DATAProfile lDAOProfile = new DATAProfile(mContext);
                Profile lProfile = lDAOProfile.getProfil(mCursor.getLong(mCursor.getColumnIndex(DATAOldCardio.PROFIL_KEY)));

                OldCardio value = new OldCardio(date,
                    mCursor.getString(mCursor.getColumnIndex(DATAOldCardio.EXERCICE)),
                    mCursor.getFloat(mCursor.getColumnIndex(DATAOldCardio.DISTANCE)),
                    mCursor.getLong(mCursor.getColumnIndex(DATAOldCardio.DURATION)),
                    lProfile);

                value.setId(Long.parseLong(mCursor.getString(mCursor.getColumnIndex(DATAOldCardio.KEY))));
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        return valueList;
    }

    public Cursor GetCursor() {
        return mCursor;
    }

    public List<OldCardio> getAllRecords() {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY "
            + KEY + " DESC";

        return getRecordsList(selectQuery);
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

    public boolean tableExists() {
        boolean isExist = true;
        Cursor res;

        SQLiteDatabase db = this.getReadableDatabase();
        try {
            res = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            res.close();
        } catch (SQLiteException e) {
            isExist = false;
        }
        return isExist;
    }

    public boolean dropTable() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(TABLE_DROP);
        return true;
    }
}
