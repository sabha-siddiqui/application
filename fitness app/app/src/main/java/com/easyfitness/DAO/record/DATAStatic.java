package com.easyfitness.DAO.record;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DATAMachine;
import com.easyfitness.DAO.DAOUtils;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.Weight;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.RecordType;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.graph.GraphData;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.enums.ProgramRecordStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DATAStatic extends DATARecord {

    public static final int MAX_FCT = 1;
    public static final int NBSERIE_FCT = 2;
    public static final int MAX_LENGTH = 3;

    private static final String TABLE_ARCHI = KEY + "," + DATE + "," + EXERCISE + "," + SETS + "," + SECONDS + "," + WEIGHT + "," + WEIGHT_UNIT + "," + PROFILE_KEY + "," + NOTES + "," + EXERCISE_KEY + "," + TIME;


    public DATAStatic(Context context) {
        super(context);
    }

    public long addStaticRecord(Date pDate, String pMachine, int pSerie, int pSeconds, float pPoids, long pProfileId, WeightUnit pUnit, String pNote, String pTime, long pTemplateRecordId) {
        return addRecord(pDate, pTime, pMachine, ExerciseType.ISOMETRIC, pSerie, 0, pPoids, pUnit, pSeconds, 0, DistanceUnit.KM, 0, pNote, pProfileId, pTemplateRecordId, RecordType.FREE_RECORD_TYPE);
    }

    public long addStaticRecordToProgramTemplate(long pTemplateId, long pTemplateSessionId,Date pDate, String pTime, String pExerciseName, int pSets, int pSeconds, float pWeight, WeightUnit pWeightUnit, int restTime) {
        return addRecord(pDate, pTime, pExerciseName, ExerciseType.ISOMETRIC, pSets, 0, pWeight,
            pWeightUnit, "", 0, DistanceUnit.KM, 0, pSeconds, -1,
            RecordType.TEMPLATE_TYPE, -1, pTemplateId, pTemplateSessionId,
            restTime, ProgramRecordStatus.NONE);
    }
    public List<GraphData> getStaticFunctionRecords(Profile pProfile, String pMachine,
                                                    int pFunction) {

        String selectQuery = null;

        // TODO attention aux units de poids. Elles ne sont pas encore prise en compte ici.
        if (pFunction == DATAStatic.MAX_FCT) {
            selectQuery = "SELECT MAX(" + WEIGHT + ") , " + SECONDS + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal()
                + " GROUP BY " + SECONDS
                + " ORDER BY " + SECONDS + " ASC";
        } else if (pFunction == DATAStatic.MAX_LENGTH) {
            selectQuery = "SELECT MAX(" + SECONDS + ") , " + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else if (pFunction == DATAStatic.NBSERIE_FCT) {
            selectQuery = "SELECT count(" + KEY + ") , " + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else {
            return null;
        }
        List<GraphData> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.rawQuery(selectQuery, null);

        double i = 0;
        if (pFunction == DATAStatic.NBSERIE_FCT || pFunction == DATAStatic.MAX_LENGTH) {
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

                    GraphData value = new GraphData(DateConverter.nbDays(date.getTime()), mCursor.getDouble(0));
                    valueList.add(value);
                } while (mCursor.moveToNext());
            }
        } else if (pFunction == DATAStatic.MAX_FCT) {
            if (mCursor.moveToFirst()) {
                do {
                    GraphData value = new GraphData(mCursor.getDouble(1), mCursor.getDouble(0));
                    valueList.add(value);
                } while (mCursor.moveToNext());
            }

        }
        return valueList;
    }
    public int getNbSeries(Date pDate, String pMachine, Profile pProfile) {
        int lReturn = 0;
        DATAMachine lDAOMachine = new DATAMachine(mContext);
        long machine_key = lDAOMachine.getMachine(pMachine).getId();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String lDate = dateFormat.format(pDate);

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        String selectQuery = "SELECT SUM(" + SETS + ") FROM " + TABLE_NAME
            + " WHERE " + DATE + "=\"" + lDate + "\" AND " + EXERCISE_KEY + "=" + machine_key
            + " AND " + PROFILE_KEY + "=" + pProfile.getId()
            + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
            + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal();
        mCursor = db.rawQuery(selectQuery, null);
        mCursor.moveToFirst();
        try {
            lReturn = mCursor.getInt(0);
        } catch (NumberFormatException e) {
            lReturn = 0;
        }

        close();
        return lReturn;
    }
    public float getTotalWeightMachine(Date pDate, String pMachine, Profile pProfile) {
        if (pProfile==null) return 0;
        float lReturn = 0;
        DATAMachine lDAOMachine = new DATAMachine(mContext);
        long machine_key = lDAOMachine.getMachine(pMachine).getId();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String lDate = dateFormat.format(pDate);

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        String selectQuery = "SELECT " + SETS + ", " + WEIGHT + " FROM " + TABLE_NAME
            + " WHERE " + DATE + "=\"" + lDate + "\" AND " + EXERCISE_KEY + "=" + machine_key
            + " AND " + PROFILE_KEY + "=" + pProfile.getId()
            + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
            + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal();
        mCursor = db.rawQuery(selectQuery, null);
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                float value = mCursor.getInt(0) * mCursor.getFloat(1) ;
                lReturn += value;
                i++;
            } while (mCursor.moveToNext());
        }
        close();
        return lReturn;
    }
    public float getTotalWeightSession(Date pDate, Profile pProfile) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        float lReturn = 0;

        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String lDate = dateFormat.format(pDate);
        String selectQuery = "SELECT " + SETS + ", " + WEIGHT  + " FROM " + TABLE_NAME
            + " WHERE " + DATE + "=\"" + lDate + "\""
            + " AND " + PROFILE_KEY + "=" + pProfile.getId()
            + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
            + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal();
        mCursor = db.rawQuery(selectQuery, null);
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                float value = mCursor.getInt(0) * mCursor.getFloat(1) ;
                lReturn += value;
                i++;
            } while (mCursor.moveToNext());
        }
        close();
        return lReturn;
    }
    public Weight getMax(Profile p, Machine m) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        Weight w = null;

        String selectQuery = "SELECT MAX(" + WEIGHT + "), " + WEIGHT_UNIT + " FROM " + TABLE_NAME
            + " WHERE " + PROFILE_KEY + "=" + p.getId() + " AND " + EXERCISE_KEY + "=" + m.getId()
            + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
            + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal();
        mCursor = db.rawQuery(selectQuery, null);
        if (mCursor.moveToFirst()) {
            do {
                w = new Weight(mCursor.getFloat(0), WeightUnit.fromInteger(mCursor.getInt(1)));
            } while (mCursor.moveToNext());
        }
        close();
        return w;
    }
    public Weight getMin(Profile p, Machine m) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        Weight w = null;
        String selectQuery = "SELECT MIN(" + WEIGHT + "), " + WEIGHT_UNIT + " FROM " + TABLE_NAME
            + " WHERE " + PROFILE_KEY + "=" + p.getId() + " AND " + EXERCISE_KEY + "=" + m.getId()
            + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
            + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal();
        mCursor = db.rawQuery(selectQuery, null);
        if (mCursor.moveToFirst()) {
            do {
                w = new Weight(mCursor.getFloat(0), WeightUnit.fromInteger(mCursor.getInt(1)));
            } while (mCursor.moveToNext());
        }
        close();
        return w;
    }
}
