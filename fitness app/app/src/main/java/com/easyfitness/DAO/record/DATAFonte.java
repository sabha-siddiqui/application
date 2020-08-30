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

public class DATAFonte extends DATARecord {

    public static final int SUM_FCT = 0;
    public static final int MAX1_FCT = 1;
    public static final int MAX5_FCT = 2;
    public static final int NBSERIE_FCT = 3;

    private static final String TABLE_ARCHI = KEY + "," + DATE + "," + EXERCISE + "," + SETS + "," + REPS + "," + WEIGHT + "," + WEIGHT_UNIT + "," + PROFILE_KEY + "," + NOTES + "," + EXERCISE_KEY + "," + TIME;

    public DATAFonte(Context context) {
        super(context);
    }

    public long addBodyBuildingRecord(Date pDate, String pTime, String pExercise, int pSets, int pReps, float pWeight, WeightUnit pWeightUnit, String pNote, long pProfileId, long pTemplateRecordId) {
        return addRecord(pDate, pTime, pExercise, ExerciseType.STRENGTH, pSets, pReps, pWeight, pWeightUnit, 0, 0, DistanceUnit.KM, 0, pNote, pProfileId, pTemplateRecordId, RecordType.FREE_RECORD_TYPE);
    }

    public long addWeightRecordToProgramTemplate(long pTemplateId, long pTemplateSessionId, Date pDate, String pTime, String pExerciseName, int pSets, int pReps, float pWeight, WeightUnit pWeightUnit, int restTime) {
        return addRecord(pDate, pTime, pExerciseName, ExerciseType.STRENGTH, pSets, pReps, pWeight,
            pWeightUnit, "", 0, DistanceUnit.KM, 0, 0, -1,
            RecordType.TEMPLATE_TYPE, -1, pTemplateId, pTemplateSessionId,
            restTime, ProgramRecordStatus.NONE);
    }

    public void addBodyBuildingList(List<Record> fonteList) {
        addList(fonteList);
    }

    public List<GraphData> getBodyBuildingFunctionRecords(Profile pProfile, String pMachine,
                                                          int pFunction) {

        String selectQuery = null;

        // TODO attention aux units de poids. Elles ne sont pas encore prise en compte ici.
        if (pFunction == DATAFonte.SUM_FCT) {
            selectQuery = "SELECT SUM(" + SETS + "*" + REPS + "*"
                + WEIGHT + "), " + DATE + " FROM " + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else if (pFunction == DATAFonte.MAX5_FCT) {
            selectQuery = "SELECT MAX(" + WEIGHT + ") , " + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + REPS + ">=5"
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else if (pFunction == DATAFonte.MAX1_FCT) {
            selectQuery = "SELECT MAX(" + WEIGHT + ") , " + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + REPS + ">=1"
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else if (pFunction == DATAFonte.NBSERIE_FCT) {
            selectQuery = "SELECT count(" + KEY + ") , " + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        }

        List<GraphData> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.rawQuery(selectQuery, null);

        double i = 0;
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
        return valueList;
    }

    public int getNbSeries(Date pDate, String pMachine, Profile pProfile) {

        if (pProfile==null) return 0;
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
        String selectQuery = "SELECT " + SETS + ", " + WEIGHT + ", " + REPS + " FROM " + TABLE_NAME
            + " WHERE " + DATE + "=\"" + lDate + "\" AND " + EXERCISE_KEY + "=" + machine_key
            + " AND " + PROFILE_KEY + "=" + pProfile.getId()
            + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
            + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal();
        mCursor = db.rawQuery(selectQuery, null);
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                float value = mCursor.getInt(0) * mCursor.getFloat(1) * mCursor.getInt(2);
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
        String selectQuery = "SELECT " + SETS + ", " + WEIGHT + ", " + REPS + " FROM " + TABLE_NAME
            + " WHERE " + DATE + "=\"" + lDate + "\""
            + " AND " + PROFILE_KEY + "=" + pProfile.getId()
            + " AND " + TEMPLATE_RECORD_STATUS + "<" + ProgramRecordStatus.PENDING.ordinal()
            + " AND " + RECORD_TYPE + "!=" + RecordType.TEMPLATE_TYPE.ordinal();
        mCursor = db.rawQuery(selectQuery, null);
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                float value = mCursor.getInt(0) * mCursor.getFloat(1) * mCursor.getInt(2);
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
            + " AND " + TEMPLATE_RECORD_STATUS + "<" + ProgramRecordStatus.PENDING.ordinal()
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
            + " AND " + TEMPLATE_RECORD_STATUS + "<" + ProgramRecordStatus.PENDING.ordinal()
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

    public void populate() {
        Date date = new Date();
        int poids = 10;

        for (int i = 1; i <= 5; i++) {
            String machine = "Biceps";
            date.setDate(date.getDay() + i * 10);
            addBodyBuildingRecord(date, "12:34:56", machine, i * 2, 10 + i, poids * i, WeightUnit.KG, "", mProfile.getId(), -1);
        }

        date = new Date();
        poids = 12;

        for (int i = 1; i <= 5; i++) {
            String machine = "Dev Couche";
            date.setDate(date.getDay() + i * 10);
            addBodyBuildingRecord(date, "12:34:56", machine, i * 2, 10 + i, poids * i, WeightUnit.KG, "", mProfile.getId(), -1);
        }
    }

}
