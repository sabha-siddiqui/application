package com.easyfitness.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.enums.ExerciseType;

import java.util.ArrayList;
import java.util.List;

public class DATAMachine extends DATABase {


    public static final String TABLE_NAME = "EFmachines";

    public static final String KEY = "_id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String TYPE = "type";
    public static final String PICTURE = "picture";
    public static final String BODYPARTS = "bodyparts";
    public static final String FAVORITES = "favorites";

    public static final String TABLE_CREATE_5 = "CREATE TABLE " + TABLE_NAME
        + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME
        + " TEXT, " + DESCRIPTION + " TEXT, " + TYPE + " INTEGER);";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
        + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME
        + " TEXT, " + DESCRIPTION + " TEXT, " + TYPE + " INTEGER, " + BODYPARTS + " TEXT, " + PICTURE + " TEXT, " + FAVORITES + " INTEGER);"; //", " + PICTURE_RES + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS "
        + TABLE_NAME + ";";

    private Profile mProfile = null;
    private Cursor mCursor = null;

    public DATAMachine(Context context) {
        super(context);
    }

    public long addMachine(String pName, String pDescription, ExerciseType pType, String pPicture, boolean pFav, String pBodyParts) {
        long new_id = -1;

        ContentValues value = new ContentValues();

        value.put(DATAMachine.NAME, pName);
        value.put(DATAMachine.DESCRIPTION, pDescription);
        value.put(DATAMachine.TYPE, pType.ordinal());
        value.put(DATAMachine.PICTURE, pPicture);
        value.put(DATAMachine.FAVORITES, pFav);
        value.put(DATAMachine.BODYPARTS, pBodyParts);

        SQLiteDatabase db = this.getWritableDatabase();
        new_id = db.insert(DATAMachine.TABLE_NAME, null, value);
        close();

        return new_id;
    }
    public Machine getMachine(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.query(TABLE_NAME, new String[]{KEY, NAME, DESCRIPTION, TYPE, BODYPARTS, PICTURE, FAVORITES}, KEY + "=?",
            new String[]{String.valueOf(id)}, null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        if (mCursor.getCount() == 0)
            return null;

        Machine value = new Machine(mCursor.getString(mCursor.getColumnIndex(DATAMachine.NAME)),
            mCursor.getString(mCursor.getColumnIndex(DATAMachine.DESCRIPTION)),
            ExerciseType.fromInteger(mCursor.getInt(mCursor.getColumnIndex(DATAMachine.TYPE))),
            mCursor.getString(mCursor.getColumnIndex(DATAMachine.BODYPARTS)),
            mCursor.getString(mCursor.getColumnIndex(DATAMachine.PICTURE)),
            mCursor.getInt(mCursor.getColumnIndex(DATAMachine.FAVORITES)) == 1);

        value.setId(mCursor.getLong(mCursor.getColumnIndex(DATAMachine.KEY)));
        mCursor.close();
        close();
        return value;
    }

    public Machine getMachine(String pName) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.query(TABLE_NAME, new String[]{KEY, NAME, DESCRIPTION, TYPE, BODYPARTS, PICTURE, FAVORITES}, NAME + "=?",
            new String[]{pName}, null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        if (mCursor.getCount() == 0)
            return null;

        Machine value = new Machine(mCursor.getString(mCursor.getColumnIndex(DATAMachine.NAME)),
            mCursor.getString(mCursor.getColumnIndex(DATAMachine.DESCRIPTION)),
            ExerciseType.fromInteger(mCursor.getInt(mCursor.getColumnIndex(DATAMachine.TYPE))),
            mCursor.getString(mCursor.getColumnIndex(DATAMachine.BODYPARTS)),
            mCursor.getString(mCursor.getColumnIndex(DATAMachine.PICTURE)),
            mCursor.getInt(mCursor.getColumnIndex(DATAMachine.FAVORITES)) == 1);

        value.setId(mCursor.getLong(mCursor.getColumnIndex(DATAMachine.KEY)));
        mCursor.close();
        close();
        return value;
    }

    public boolean machineExists(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.query(TABLE_NAME, new String[]{NAME}, NAME + "=?",
            new String[]{name}, null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        if (mCursor.getCount() == 0)
            return false;
        return true;
    }
    private ArrayList<Machine> getMachineList(String pRequest) {
        ArrayList<Machine> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);
        if (mCursor.moveToFirst()) {
            do {
                Machine value = new Machine(mCursor.getString(mCursor.getColumnIndex(DATAMachine.NAME)),
                    mCursor.getString(mCursor.getColumnIndex(DATAMachine.DESCRIPTION)),
                    ExerciseType.fromInteger(mCursor.getInt(mCursor.getColumnIndex(DATAMachine.TYPE))),
                    mCursor.getString(mCursor.getColumnIndex(DATAMachine.BODYPARTS)),
                    mCursor.getString(mCursor.getColumnIndex(DATAMachine.PICTURE)),
                    mCursor.getInt(mCursor.getColumnIndex(DATAMachine.FAVORITES)) == 1);

                value.setId(mCursor.getLong(mCursor.getColumnIndex(DATAMachine.KEY)));
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        return valueList;
    }
    private Cursor getMachineListCursor(String pRequest) {
        ArrayList<Machine> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(pRequest, null);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void closeCursor() {
        mCursor.close();
    }

    public Cursor getAllMachines() {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY "
            + FAVORITES + " DESC," + NAME + " COLLATE NOCASE ASC";
        return getMachineListCursor(selectQuery);
    }
    public Cursor getAllMachines(int type) {
        String selectQuery = "";
        selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + TYPE + "=" + type + " ORDER BY "
            + FAVORITES + " DESC," + NAME + " COLLATE NOCASE ASC";
        return getMachineListCursor(selectQuery);
    }
    public Cursor getFilteredMachines(CharSequence filterString) {

        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + NAME + " LIKE " + "'%" + filterString + "%' " + " ORDER BY "
            + FAVORITES + " DESC," + NAME + " ASC";
        return getMachineListCursor(selectQuery);
    }
    public void deleteAllEmptyExercises() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, NAME + " = ?",
            new String[]{""});
        db.close();
    }

    public ArrayList<Machine> getAllMachinesArray() {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY "
            + FAVORITES + " DESC," + NAME + " COLLATE NOCASE ASC";

        return getMachineList(selectQuery);
    }
    public List<Machine> getAllMachines(List<Long> idList) {

        String ids = idList.toString();
        ids = ids.replace('[', '(');
        ids = ids.replace(']', ')');
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY + " in " + ids + " ORDER BY "
            + FAVORITES + " DESC," + NAME + " COLLATE NOCASE ASC";
        return getMachineList(selectQuery);
    }

    public String[] getAllMachinesName() {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;

        String selectQuery = "SELECT DISTINCT  " + NAME + " FROM "
            + TABLE_NAME + " ORDER BY " + NAME + " COLLATE NOCASE ASC";
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
        mCursor.close();
        close();
        return valueList;
    }

    public int updateMachine(Machine m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(DATAMachine.NAME, m.getName());
        value.put(DATAMachine.DESCRIPTION, m.getDescription());
        value.put(DATAMachine.TYPE, m.getType().ordinal());
        value.put(DATAMachine.BODYPARTS, m.getBodyParts());
        value.put(DATAMachine.PICTURE, m.getPicture());
        if (m.getFavorite()) value.put(DATAMachine.FAVORITES, 1);
        else value.put(DATAMachine.FAVORITES, 0);

        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }
    public void delete(Machine m) {
        if (m != null) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, KEY + " = ?",
                new String[]{String.valueOf(m.getId())});
            db.close();
        }
    }
    public void delete(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY + " = ?", new String[]{String.valueOf(id)});
        db.close();
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
        addMachine("Dev Couche", "Developper couche : blabla ", ExerciseType.STRENGTH, "", true, "");
        addMachine("Biceps", "Developper couche : blabla ", ExerciseType.STRENGTH, "", false, "");
    }
}
