package com.easyfitness.DAO;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DATABodyMeasure;
import com.easyfitness.DAO.bodymeasures.DATABodyPart;
import com.easyfitness.DAO.record.DATAFonte;
import com.easyfitness.DAO.record.DATARecord;
import com.easyfitness.DAO.program.DATAProgram;
import com.easyfitness.DAO.program.DATAProgramHistory;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.Unit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 22;
    public static final String OLD09_DATABASE_NAME = "easyfitness";
    public static final String DATABASE_NAME = "easyfitness.db";
    private static DatabaseHelper sInstance;
    private Context mContext = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public static void renameOldDatabase(Activity activity) {
        File oldDatabaseFile = activity.getDatabasePath(OLD09_DATABASE_NAME);
        if (oldDatabaseFile.exists()) {
            File newDatabaseFile = new File(oldDatabaseFile.getParentFile(), DATABASE_NAME);
            oldDatabaseFile.renameTo(newDatabaseFile);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATARecord.TABLE_CREATE);
        db.execSQL(DATAProfile.TABLE_CREATE);
        db.execSQL(DATAProfileWeight.TABLE_CREATE);
        db.execSQL(DATAMachine.TABLE_CREATE);
        db.execSQL(DATABodyMeasure.TABLE_CREATE);
        db.execSQL(DATABodyPart.TABLE_CREATE);
        db.execSQL(DATAProgram.TABLE_CREATE);
        db.execSQL(DATAProgramHistory.TABLE_CREATE);
        initBodyPartTable(db);
    }

    @Override
    public void onUpgrade(
        final SQLiteDatabase db, final int oldVersion,
        final int newVersion) {
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    db.execSQL("ALTER TABLE " + DATAFonte.TABLE_NAME + " ADD COLUMN " + DATAFonte.NOTES + " TEXT");
                    db.execSQL("ALTER TABLE " + DATAFonte.TABLE_NAME + " ADD COLUMN " + DATAFonte.WEIGHT_UNIT + " INTEGER DEFAULT 0");
                    break;
                case 5:
                    db.execSQL(DATAMachine.TABLE_CREATE_5);
                    db.execSQL("ALTER TABLE " + DATAFonte.TABLE_NAME + " ADD COLUMN " + DATAFonte.EXERCISE_KEY + " INTEGER");
                    break;
                case 6:
                    if (!isFieldExist(db, DATAMachine.TABLE_NAME, DATAMachine.BODYPARTS)) // Easyfitness 0.9 : Probleme d'upgrade
                        db.execSQL("ALTER TABLE " + DATAMachine.TABLE_NAME + " ADD COLUMN " + DATAMachine.BODYPARTS + " TEXT");
                    break;
                case 7:
                    db.execSQL("ALTER TABLE " + DATAMachine.TABLE_NAME + " ADD COLUMN " + DATAMachine.PICTURE + " TEXT");
                    break;
                case 8:
                    db.execSQL("ALTER TABLE " + DATAFonte.TABLE_NAME + " ADD COLUMN " + DATAFonte.TIME + " TEXT");
                    break;
                case 9:
                    db.execSQL(DATABodyMeasure.TABLE_CREATE);
                    break;
                case 10:
                    db.execSQL("ALTER TABLE " + DATAMachine.TABLE_NAME + " ADD COLUMN " + DATAMachine.FAVORITES + " INTEGER");
                    break;
                case 11:
                    db.execSQL("ALTER TABLE " + DATAFonte.TABLE_NAME + " RENAME TO tmp_table_name");
                    db.execSQL(DATAFonte.TABLE_CREATE);
                    db.execSQL("INSERT INTO " + DATAFonte.TABLE_NAME + " SELECT * FROM tmp_table_name");
                    break;
                case 12:
                    db.execSQL("DROP TABLE IF EXISTS tmp_table_name");
                    break;
                case 13:
                    db.execSQL("ALTER TABLE " + DATAProfile.TABLE_NAME + " ADD COLUMN " + DATAProfile.SIZE + " INTEGER");
                    db.execSQL("ALTER TABLE " + DATAProfile.TABLE_NAME + " ADD COLUMN " + DATAProfile.BIRTHDAY + " DATE");
                    break;
                case 14:
                    db.execSQL("ALTER TABLE " + DATAProfile.TABLE_NAME + " ADD COLUMN " + DATAProfile.PHOTO + " TEXT");
                    break;
                case 15:
                    db.execSQL("ALTER TABLE " + DATARecord.TABLE_NAME + " ADD COLUMN " + DATARecord.DISTANCE + " REAL");
                    db.execSQL("ALTER TABLE " + DATARecord.TABLE_NAME + " ADD COLUMN " + DATARecord.DURATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + DATARecord.TABLE_NAME + " ADD COLUMN " + DATARecord.EXERCISE_TYPE + " INTEGER DEFAULT " + ExerciseType.STRENGTH.ordinal());
                    break;
                case 16:
                    db.execSQL("ALTER TABLE " + DATABodyMeasure.TABLE_NAME + " ADD COLUMN " + DATABodyMeasure.UNIT + " INTEGER");
                    migrateWeightTable(db);
                    break;
                case 17:
                    db.execSQL("ALTER TABLE " + DATAProfile.TABLE_NAME + " ADD COLUMN " + DATAProfile.GENDER + " INTEGER");
                    break;
                case 18:
                    db.execSQL("ALTER TABLE " + DATARecord.TABLE_NAME + " ADD COLUMN " + DATARecord.SECONDS + " INTEGER DEFAULT 0");
                    break;
                case 19:
                    db.execSQL("ALTER TABLE " + DATARecord.TABLE_NAME + " ADD COLUMN " + DATARecord.DISTANCE_UNIT + " INTEGER DEFAULT 0");
                    break;
                case 20:
                    db.execSQL(DATABodyPart.TABLE_CREATE);
                    initBodyPartTable(db);
                    break;
                case 21:
                    db.execSQL(DATAProgram.TABLE_CREATE);
                    db.execSQL(DATAProgramHistory.TABLE_CREATE);

                    db.execSQL("ALTER TABLE " + DATARecord.TABLE_NAME + " ADD COLUMN " + DATARecord.RECORD_TYPE + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE " + DATARecord.TABLE_NAME + " ADD COLUMN " + DATARecord.TEMPLATE_KEY + " INTEGER DEFAULT -1");
                    db.execSQL("ALTER TABLE " + DATARecord.TABLE_NAME + " ADD COLUMN " + DATARecord.TEMPLATE_RECORD_KEY + " INTEGER DEFAULT -1");
                    db.execSQL("ALTER TABLE " + DATARecord.TABLE_NAME + " ADD COLUMN " + DATARecord.TEMPLATE_SESSION_KEY + " INTEGER DEFAULT -1");
                    db.execSQL("ALTER TABLE " + DATARecord.TABLE_NAME + " ADD COLUMN " + DATARecord.TEMPLATE_REST_TIME + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE " + DATARecord.TABLE_NAME + " ADD COLUMN " + DATARecord.TEMPLATE_ORDER + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE " + DATARecord.TABLE_NAME + " ADD COLUMN " + DATARecord.TEMPLATE_RECORD_STATUS + " INTEGER DEFAULT 3");
                    break;
                case 22:
                    upgradeBodyMeasureUnits(db);
                    break;
            }
            upgradeTo++;
        }
    }

    @Override
    public void onDowngrade(
        final SQLiteDatabase db, final int oldVersion,
        final int newVersion) {
        int upgradeTo = oldVersion - 1;
        while (upgradeTo >= newVersion) {
            switch (upgradeTo) {
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 20:
                    db.delete(DATAProgram.TABLE_NAME, null, null);
                    break;
            }
            upgradeTo--;
        }
    }
    public boolean isFieldExist(SQLiteDatabase db, String tableName, String fieldName) {
        boolean isExist = true;
        Cursor res;

        try {
            res = db.rawQuery("SELECT " + fieldName + " FROM " + tableName, null);
            res.close();
        } catch (SQLiteException e) {
            isExist = false;
        }

        return isExist;
    }

    public boolean tableExists(SQLiteDatabase db, String tableName) {
        boolean isExist = true;
        Cursor res;

        try {
            res = db.rawQuery("SELECT * FROM " + tableName, null);
            res.close();
        } catch (SQLiteException e) {
            isExist = false;
        }
        return isExist;
    }

    private void migrateWeightTable(SQLiteDatabase db) {
        List<ProfileWeight> valueList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DATAProfileWeight.TABLE_NAME;
        Cursor mCursor = null;
        mCursor = db.rawQuery(selectQuery, null);
        if (mCursor.moveToFirst()) {
            do {
                ContentValues value = new ContentValues();

                value.put(DATABodyMeasure.DATE, mCursor.getString(mCursor.getColumnIndex(DATAProfileWeight.DATE)));
                value.put(DATABodyMeasure.BODYPART_ID, BodyPartExtensions.WEIGHT);
                value.put(DATABodyMeasure.MEASURE, mCursor.getFloat(mCursor.getColumnIndex(DATAProfileWeight.POIDS)));
                value.put(DATABodyMeasure.PROFIL_KEY, mCursor.getLong(mCursor.getColumnIndex(DATAProfileWeight.PROFIL_KEY)));

                db.insert(DATABodyMeasure.TABLE_NAME, null, value);
            } while (mCursor.moveToNext());
            mCursor.close();
        }
    }

    private void upgradeBodyMeasureUnits(SQLiteDatabase db) {
        DATABodyMeasure daoBodyMeasure = new DATABodyMeasure(mContext);
        String selectQuery = "SELECT * FROM " + DATABodyMeasure.TABLE_NAME + " ORDER BY date(" + DATABodyMeasure.DATE + ") DESC";
        List<BodyMeasure> valueList = daoBodyMeasure.getMeasuresList(db, selectQuery);

        for (BodyMeasure bodyMeasure : valueList) {
            switch (bodyMeasure.getBodyPartID()) {
                case BodyPartExtensions.LEFTBICEPS:
                case BodyPartExtensions.RIGHTBICEPS:
                case BodyPartExtensions.PECTORAUX:
                case BodyPartExtensions.WAIST:
                case BodyPartExtensions.BEHIND:
                case BodyPartExtensions.LEFTTHIGH:
                case BodyPartExtensions.RIGHTTHIGH:
                case BodyPartExtensions.LEFTCALVES:
                case BodyPartExtensions.RIGHTCALVES:
                    bodyMeasure.setUnit(Unit.CM);
                    break;
                case BodyPartExtensions.WEIGHT:
                    bodyMeasure.setUnit(Unit.KG);
                    break;
                case BodyPartExtensions.MUSCLES:
                case BodyPartExtensions.WATER:
                case BodyPartExtensions.FAT:
                    bodyMeasure.setUnit(Unit.PERCENTAGE);
                    break;
            }
            daoBodyMeasure.updateMeasure(db, bodyMeasure);
        }


    }

    public void initBodyPartTable(SQLiteDatabase db){
        int display_order=0;

        addInitialBodyPart(db, BodyPartExtensions.LEFTBICEPS, "","", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.RIGHTBICEPS, "","", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.PECTORAUX,"","", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.WAIST, "","", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.BEHIND,"","", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.LEFTTHIGH,"","", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.RIGHTTHIGH,"","", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.LEFTCALVES, "","", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.RIGHTCALVES,"","", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.WEIGHT,"","", 0, BodyPartExtensions.TYPE_WEIGHT);
        addInitialBodyPart(db, BodyPartExtensions.MUSCLES, "","", 0, BodyPartExtensions.TYPE_WEIGHT);
        addInitialBodyPart(db, BodyPartExtensions.WATER, "","", 0, BodyPartExtensions.TYPE_WEIGHT);
        addInitialBodyPart(db, BodyPartExtensions.FAT, "","", 0, BodyPartExtensions.TYPE_WEIGHT);
    }

    public void addInitialBodyPart(SQLiteDatabase db, long pKey, String pCustomName, String pCustomPicture, int pDisplay, int pType) {
        ContentValues value = new ContentValues();

        value.put(DATABodyPart.KEY, pKey);
        value.put(DATABodyPart.BODYPART_RESID, pKey);
        value.put(DATABodyPart.CUSTOM_NAME, pCustomName);
        value.put(DATABodyPart.CUSTOM_PICTURE, pCustomPicture);
        value.put(DATABodyPart.DISPLAY_ORDER, pDisplay);
        value.put(DATABodyPart.TYPE, pType);

        db.insert(DATABodyPart.TABLE_NAME, null, value);
    }
}
