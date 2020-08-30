package com.easyfitness.DAO.record;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DATABase;
import com.easyfitness.DAO.DATAMachine;
import com.easyfitness.DAO.DAOUtils;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.R;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.RecordType;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.enums.ProgramRecordStatus;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DATARecord extends DATABase {

    public static final String TABLE_NAME = "EFfontes";

    public static final String KEY = "_id";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String EXERCISE = "machine";
    public static final String PROFILE_KEY = "profil_id";
    public static final String EXERCISE_KEY = "machine_id";
    public static final String NOTES = "notes";
    public static final String EXERCISE_TYPE = "type";

    public static final String SETS = "serie";
    public static final String REPS = "repetition";
    public static final String WEIGHT = "poids";
    public static final String WEIGHT_UNIT = "unit";
    public static final String DISTANCE = "distance";
    public static final String DURATION = "duration";
    public static final String DISTANCE_UNIT = "distance_unit";
    public static final String SECONDS = "seconds";
    public static final String RECORD_TYPE = "RECORD_TYPE";
    public static final String TEMPLATE_KEY =  "TEMPLATE_KEY";
    public static final String TEMPLATE_RECORD_KEY =  "TEMPLATE_RECORD_KEY";
    public static final String TEMPLATE_SESSION_KEY =  "TEMPLATE_SESSION_KEY";
    public static final String TEMPLATE_ORDER =  "TEMPLATE_ORDER";
    public static final String TEMPLATE_REST_TIME =  "TEMPLATE_SECONDS";

    public static final int FREE_RECORD_TYPE =  0;
    public static final int PROGRAM_RECORD_TYPE =  1;
    public static final int TEMPLATE_TYPE =  2;

    public static final String TEMPLATE_RECORD_STATUS = "TEMPLATE_RECORD_STATUS";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
        + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + PROFILE_KEY + " INTEGER, "
        + EXERCISE_KEY + " INTEGER,"
        + DATE + " DATE, "
        + TIME + " TEXT,"
        + EXERCISE + " TEXT, "
        + SETS + " INTEGER, "
        + REPS + " INTEGER, "
        + WEIGHT + " REAL, "
        + WEIGHT_UNIT + " INTEGER, "
        + NOTES + " TEXT, "
        + DISTANCE + " REAL, "
        + DURATION + " TEXT, "
        + EXERCISE_TYPE + " INTEGER, "
        + SECONDS + " INTEGER, "
        + DISTANCE_UNIT + " INTEGER,"
        + TEMPLATE_KEY + " INTEGER,"
        + TEMPLATE_RECORD_KEY + " INTEGER,"
        + TEMPLATE_SESSION_KEY + " INTEGER,"
        + TEMPLATE_ORDER + " INTEGER,"
        + TEMPLATE_REST_TIME + " INTEGER,"
        + TEMPLATE_RECORD_STATUS + " INTEGER,"
        + RECORD_TYPE + " INTEGER"
        + " );";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS "
        + TABLE_NAME + ";";


    protected Profile mProfile = null;
    protected Cursor mCursor = null;
    protected Context mContext;

    public DATARecord(Context context) {
        super(context);
        mContext = context;
    }

    public void setProfile(Profile pProfile) {
        mProfile = pProfile;
    }

    public int getCount() {
        String countQuery = "SELECT " + KEY + " FROM " + TABLE_NAME;
        open();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int value = cursor.getCount();
        cursor.close();
        close();
        return value;
    }

    public long addRecord(Record record) {
        return addRecord(record.getDate(), record.getTime(),
            record.getExercise(), record.getExerciseType(),
            record.getSets(), record.getReps(), record.getWeight(), record.getWeightUnit(),
            record.getNote(),
            record.getDistance(), record.getDistanceUnit(), record.getDuration(),
            record.getSeconds(),
            record.getProfileId(), record.getRecordType(),
            record.getTemplateRecordId(),record.getTemplateId(), record.getTemplateSessionId(), record.getRestTime(), record.getProgramRecordStatus());
    }

    public long addRecord(Date pDate, String pTime, String pExercise, ExerciseType pExerciseType, int pSets, int pReps, float pWeight,
                          WeightUnit pUnit, int pSeconds, float pDistance, DistanceUnit pDistanceUnit, long pDuration, String pNote, long pProfileId,
                          long pTemplateRecordId, RecordType pRecordType){
        return addRecord(pDate, pTime, pExercise, pExerciseType, pSets, pReps, pWeight, pUnit, pNote, pDistance, pDistanceUnit, pDuration, pSeconds, pProfileId,
            pRecordType, pTemplateRecordId, -1, -1, 0, ProgramRecordStatus.SUCCESS);
    }

    public long addRecord(Date pDate, String pTime, String pExercise, ExerciseType pExerciseType, int pSets, int pReps, float pWeight,
                          WeightUnit pWeightUnit, String pNote, float pDistance, DistanceUnit pDistanceUnit, long pDuration, int pSeconds, long pProfileId,
                          RecordType pRecordType, long pTemplateRecordId, long pTemplateId, long pTemplateSessionId,
                          int pRestTime, ProgramRecordStatus pProgramRecordStatus) {

        ContentValues value = new ContentValues();
        long new_id = -1;
        long machine_key = -1;
        DATAMachine lDAOMachine = new DATAMachine(mContext);
        if (!lDAOMachine.machineExists(pExercise)) {
            machine_key = lDAOMachine.addMachine(pExercise, "", pExerciseType, "", false, "");
        } else {
            machine_key = lDAOMachine.getMachine(pExercise).getId();
        }

        int templateOrder = 0;
        if (pRecordType==RecordType.TEMPLATE_TYPE) {
            Cursor cursor = this.getProgramTemplateRecords(pTemplateId);
            List<Record> records = fromCursorToList(cursor);
            templateOrder = records.size();
        }

        value.put(DATARecord.DATE, DateConverter.dateToDBDateStr(pDate));
        value.put(DATARecord.TIME, pTime);
        value.put(DATARecord.EXERCISE, pExercise);
        value.put(DATARecord.EXERCISE_KEY, machine_key);
        value.put(DATARecord.EXERCISE_TYPE, pExerciseType.ordinal());
        value.put(DATARecord.PROFILE_KEY, pProfileId);
        value.put(DATARecord.SETS, pSets);
        value.put(DATARecord.REPS, pReps);
        value.put(DATARecord.WEIGHT, pWeight);
        value.put(DATARecord.WEIGHT_UNIT, pWeightUnit.ordinal());
        value.put(DATARecord.DISTANCE, pDistance);
        value.put(DATARecord.DISTANCE_UNIT, pDistanceUnit.ordinal());
        value.put(DATARecord.DURATION, pDuration);
        value.put(DATARecord.SECONDS, pSeconds);
        value.put(DATARecord.NOTES, pNote);
        value.put(DATARecord.TEMPLATE_KEY, pTemplateId);
        value.put(DATARecord.TEMPLATE_RECORD_KEY, pTemplateRecordId);
        value.put(DATARecord.TEMPLATE_SESSION_KEY, pTemplateSessionId);
        value.put(DATARecord.TEMPLATE_REST_TIME, pRestTime);
        value.put(DATARecord.TEMPLATE_ORDER, templateOrder);
        value.put(DATARecord.RECORD_TYPE, pRecordType.ordinal());
        value.put(DATARecord.TEMPLATE_RECORD_STATUS, pProgramRecordStatus.ordinal());

        SQLiteDatabase db = open();
        new_id = db.insert(DATARecord.TABLE_NAME, null, value);
        close();

        return new_id;
    }

    public void addList(List<Record> list) {
        for (Record record: list) {
            addRecord(record.getDate(), record.getTime(),
                record.getExercise(), record.getExerciseType(),
                record.getSets(), record.getReps(), record.getWeight(), record.getWeightUnit(),
                record.getSeconds(),
                record.getDistance(), record.getDistanceUnit(), record.getDuration(),
                record.getNote(),
                record.getProfileId(), record.getTemplateRecordId(),
                record.getRecordType());
        }
    }
    public int deleteRecord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int ret = db.delete(TABLE_NAME, KEY + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return ret;
    }
    public Record getRecord(long id) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY + "=" + id;

        mCursor = getRecordsListCursor(selectQuery);
        if (mCursor.moveToFirst()) {
            return fromCursor(mCursor);
        } else {
            return null;
        }
    }

    private Record fromCursor(Cursor cursor) {
        Date date = DateConverter.DBDateStrToDate(cursor.getString(cursor.getColumnIndex(DATAFonte.DATE)));

        long machine_key = -1;
        DATAMachine lDAOMachine = new DATAMachine(mContext);
        if (cursor.getString(cursor.getColumnIndex(DATAFonte.EXERCISE_KEY)) == null) {
            machine_key = lDAOMachine.addMachine(cursor.getString(cursor.getColumnIndex(DATAFonte.EXERCISE)), "", ExerciseType.STRENGTH, "", false, "");
        } else {
            machine_key = cursor.getLong(cursor.getColumnIndex(DATAFonte.EXERCISE_KEY));
        }

        Record value = new Record(date,
            cursor.getString(cursor.getColumnIndex(DATARecord.TIME)),
            cursor.getString(cursor.getColumnIndex(DATARecord.EXERCISE)),
            machine_key,
            cursor.getLong(cursor.getColumnIndex(DATARecord.PROFILE_KEY)),
            cursor.getInt(cursor.getColumnIndex(DATARecord.SETS)),
            cursor.getInt(cursor.getColumnIndex(DATARecord.REPS)),
            cursor.getFloat(cursor.getColumnIndex(DATARecord.WEIGHT)),
            WeightUnit.fromInteger(cursor.getInt(cursor.getColumnIndex(DATARecord.WEIGHT_UNIT))),
            cursor.getInt(cursor.getColumnIndex(DATARecord.SECONDS)),
            cursor.getFloat(cursor.getColumnIndex(DATARecord.DISTANCE)),
            DistanceUnit.fromInteger(cursor.getInt(cursor.getColumnIndex(DATARecord.DISTANCE_UNIT))),
            cursor.getLong(cursor.getColumnIndex(DATARecord.DURATION)),
            cursor.getString(cursor.getColumnIndex(DATARecord.NOTES)),
            ExerciseType.fromInteger(cursor.getInt(cursor.getColumnIndex(DATARecord.EXERCISE_TYPE))),
            cursor.getLong(cursor.getColumnIndex(DATARecord.TEMPLATE_KEY)),
            cursor.getLong(cursor.getColumnIndex(DATARecord.TEMPLATE_RECORD_KEY)),
            cursor.getLong(cursor.getColumnIndex(DATARecord.TEMPLATE_SESSION_KEY)),
            cursor.getInt(cursor.getColumnIndex(DATARecord.TEMPLATE_REST_TIME)),
            cursor.getInt(cursor.getColumnIndex(DATARecord.TEMPLATE_ORDER)),
            ProgramRecordStatus.fromInteger(cursor.getInt(cursor.getColumnIndex(DATARecord.TEMPLATE_RECORD_STATUS))),
            RecordType.fromInteger(cursor.getInt(cursor.getColumnIndex(DATARecord.RECORD_TYPE))));

        value.setId(cursor.getLong(cursor.getColumnIndex(DATARecord.KEY)));
        return value;
    }

    public List<Record> fromCursorToList(Cursor cursor) {
        List<Record> valueList = new ArrayList<>();
        if (cursor!=null && cursor.moveToFirst()) {
            do {
                Record record = fromCursor(cursor);
                if (record!=null) valueList.add(record);
            } while (cursor.moveToNext());
        }
        return valueList;
    }

    public List<Record> getAllRecords() {
        String selectQuery = "SELECT * FROM " + TABLE_NAME
            + " ORDER BY " + KEY + " DESC";
        return getRecordsList(selectQuery);
    }
    public Cursor getAllRecordByMachines(Profile pProfile, String pMachines) {
        return getAllRecordByMachines(pProfile, pMachines, -1);
    }

    public Cursor getAllRecordByMachines(Profile pProfile, String pMachines, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;
        String selectQuery = "SELECT * FROM " + TABLE_NAME
            + " WHERE " + EXERCISE + "=\"" + pMachines + "\""
            + " AND " + PROFILE_KEY + "=" + pProfile.getId()
            + " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop;
        return getRecordsListCursor(selectQuery);
    }
    public Cursor getAllRecordsByProfile(Profile pProfile) {
        return getAllRecordsByProfile(pProfile, -1);
    }
    public List<Record> getAllRecordsByProfileList(Profile pProfile) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = getAllRecordsByProfile(pProfile, -1);
        List<Record> list = fromCursorToList(cursor);
        return list;
    }

    public Cursor getAllRecordsByProfile(Profile pProfile, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;
        String selectQuery = "SELECT * FROM " + TABLE_NAME +
            " WHERE " + PROFILE_KEY + "=" + pProfile.getId() +
            " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop;
        return getRecordsListCursor(selectQuery);
    }
    private Cursor getRecordsListCursor(String pRequest) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(pRequest, null);
    }
    public List<String> getAllMachinesStrList() {
        return getAllMachinesStrList(null);
    }
    public List<String> getAllMachinesStrList(Profile pProfile) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        String selectQuery = "";
        if (pProfile == null) {
            selectQuery = "SELECT DISTINCT " + EXERCISE + " FROM "
                + TABLE_NAME + " ORDER BY " + EXERCISE + " ASC";
        } else {
            selectQuery = "SELECT DISTINCT " + EXERCISE + " FROM "
                + TABLE_NAME + "  WHERE " + PROFILE_KEY + "=" + pProfile.getId() + " ORDER BY " + EXERCISE + " ASC";
        }
        mCursor = db.rawQuery(selectQuery, null);

        int size = mCursor.getCount();

        List<String> valueList = new ArrayList<>(size);
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                valueList.add(mCursor.getString(0));
                i++;
            } while (mCursor.moveToNext());
        }
        close();
        return valueList;
    }
    public String[] getAllMachines(Profile pProfile) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        String selectQuery = "SELECT DISTINCT " + EXERCISE + " FROM "
            + TABLE_NAME + "  WHERE " + PROFILE_KEY + "=" + pProfile.getId() + " ORDER BY " + EXERCISE + " ASC";
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

    public List<String> getAllDatesList(Profile pProfile, Machine pMachine) {

        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        String selectQuery = "SELECT DISTINCT " + DATE + " FROM " + TABLE_NAME;
        if (pMachine != null) {
            selectQuery += " WHERE " + EXERCISE_KEY + "=" + pMachine.getId();
            if (pProfile != null)
                selectQuery += " AND " + PROFILE_KEY + "=" + pProfile.getId(); // pProfile should never be null but depending on how the activity is resuming it happen. to be fixed
        } else {
            if (pProfile != null)
                selectQuery += " WHERE " + PROFILE_KEY + "=" + pProfile.getId(); // pProfile should never be null but depending on how the activity is resuming it happen. to be fixed
        }
        selectQuery += " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal();
        selectQuery += " ORDER BY " + DATE + " DESC";

        mCursor = db.rawQuery(selectQuery, null);
        int size = mCursor.getCount();

        List<String> valueList = new ArrayList<>(size);
        if (mCursor.moveToFirst()) {
            do {
                int i = 0;

                Date date;
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    date = dateFormat.parse(mCursor.getString(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                    date = new Date();
                }

                DateFormat dateFormat3 = android.text.format.DateFormat.getDateFormat(mContext.getApplicationContext());
                dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"));
                valueList.add(dateFormat3.format(date));
                i++;
            } while (mCursor.moveToNext());
        }

        close();
        return valueList;
    }

    public Cursor getTop3DatesRecords(Profile pProfile) {

        String selectQuery = null;

        if (pProfile == null)
            return null;

        selectQuery = "SELECT * FROM " + TABLE_NAME
            + " WHERE " + PROFILE_KEY + "=" + pProfile.getId()
            + " AND " + DATE + " IN (SELECT DISTINCT " + DATE + " FROM " + TABLE_NAME + " WHERE " + PROFILE_KEY + "=" + pProfile.getId() + " AND " + TEMPLATE_KEY + "=-1" + " ORDER BY " + DATE + " DESC LIMIT 3)"
            + " AND " + TEMPLATE_KEY + "=-1"
            + " ORDER BY " + DATE + " DESC," + KEY + " DESC";

        return getRecordsListCursor(selectQuery);
    }

    public Cursor getProgramTemplateRecords(long mTemplateId) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME
            + " WHERE " + TEMPLATE_KEY + "=" + mTemplateId
            + " AND " + RECORD_TYPE + "=" + RecordType.TEMPLATE_TYPE.ordinal()
            + " ORDER BY " + TEMPLATE_ORDER + " ASC";

        return getRecordsListCursor(selectQuery);
    }

    public Cursor getProgramWorkoutRecords(long mProgramSessionId) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME
            + " WHERE " + TEMPLATE_SESSION_KEY + "=" + mProgramSessionId
            + " ORDER BY " + TEMPLATE_ORDER + " ASC";

        return getRecordsListCursor(selectQuery);
    }
    public Cursor getFilteredRecords(Profile pProfile, String pMachine, String pDate) {

        boolean lfilterMachine = true;
        boolean lfilterDate = true;
        String selectQuery = null;

        if (pMachine == null || pMachine.isEmpty() || pMachine.equals(mContext.getResources().getText(R.string.all).toString())) {
            lfilterMachine = false;
        }

        if (pDate == null || pDate.isEmpty() || pDate.equals(mContext.getResources().getText(R.string.all).toString())) {
            lfilterDate = false;
        }

        if (lfilterMachine && lfilterDate) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine
                + "\" AND " + DATE + "=\"" + pDate + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " ORDER BY " + DATE + " DESC," + KEY + " DESC";
        } else if (!lfilterMachine && lfilterDate) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + DATE + "=\"" + pDate + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " ORDER BY " + DATE + " DESC," + KEY + " DESC";
        } else if (lfilterMachine) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " ORDER BY " + DATE + " DESC," + KEY + " DESC";
        } else {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " ORDER BY " + DATE + " DESC," + KEY + " DESC";
        }
        return getRecordsListCursor(selectQuery);
    }
    public Record getLastRecord(Profile pProfile) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        Record lReturn = null;
        String selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
            + " WHERE " + PROFILE_KEY + "=" + pProfile.getId();
        mCursor = db.rawQuery(selectQuery, null);
        if (mCursor.moveToFirst()) {
            try {
                long value = mCursor.getLong(0);
                lReturn = getRecord(value);
            } catch (NumberFormatException e) {
                lReturn = null;
            }
        }

        close();
        return lReturn;
    }
    public Record getLastExerciseRecord(long machineID, Profile p) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        Record lReturn = null;

        String selectQuery;
        if (p == null) {
            selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                + " WHERE " + EXERCISE_KEY + "=" + machineID;
        } else {
            selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                + " WHERE " + EXERCISE_KEY + "=" + machineID +
                " AND " + PROFILE_KEY + "=" + p.getId();
        }
        mCursor = db.rawQuery(selectQuery, null);
        if (mCursor.moveToFirst()) {
            try {
                long value = mCursor.getLong(0);
                lReturn = this.getRecord(value);
            } catch (NumberFormatException e) {
                lReturn = null;
            }
        }

        close();
        return lReturn;
    }
    public List<Record> getAllRecordByMachineStrArray(Profile pProfile, String pMachines) {
        return getAllRecordByMachineStrArray(pProfile, pMachines, -1);
    }

    public List<Record> getAllRecordByMachineStrArray(Profile pProfile, String pMachines, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachines + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop;
        return getRecordsList(selectQuery);
    }

    public List<Record> getAllRecordByMachineIdArray(Profile pProfile, long pMachineId, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + EXERCISE_KEY + "=\"" + pMachineId + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop;
        return getRecordsList(selectQuery);
    }
    public List<Record> getAllRecordByMachineIdArray(Profile pProfile, long pMachineId) {
        return getAllRecordByMachineIdArray(pProfile, pMachineId, -1);
    }



    public List<Record> getAllTemplateRecordByProgramArray(long pTemplateId) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME
            + " WHERE " + TEMPLATE_KEY + "=" + pTemplateId
            + " AND " + RECORD_TYPE + "=" + DATARecord.TEMPLATE_TYPE;

        return getRecordsList(selectQuery);
    }
    private List<Record> getRecordsList(String pRequest) {
        List<Record> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);
        if (mCursor.moveToFirst() && mCursor.getCount() > 0) {
            do {
                Record value = fromCursor(mCursor);
                value.setId(mCursor.getLong(mCursor.getColumnIndex(DATARecord.KEY)));
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        return valueList;
    }
    public int updateRecord(Record record) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DATARecord.KEY, record.getId());
        value.put(DATARecord.DATE, DateConverter.dateToDBDateStr(record.getDate()));
        value.put(DATARecord.TIME, record.getTime());
        value.put(DATARecord.EXERCISE, record.getExercise());
        value.put(DATARecord.EXERCISE_KEY, record.getExerciseId());
        value.put(DATARecord.EXERCISE_TYPE, record.getExerciseType().ordinal());
        value.put(DATARecord.PROFILE_KEY, record.getProfileId());
        value.put(DATARecord.SETS, record.getSets());
        value.put(DATARecord.REPS, record.getReps());
        value.put(DATARecord.WEIGHT, record.getWeight());
        value.put(DATARecord.WEIGHT_UNIT, record.getWeightUnit().ordinal());
        value.put(DATARecord.DISTANCE, record.getDistance());
        value.put(DATARecord.DISTANCE_UNIT, record.getDistanceUnit().ordinal());
        value.put(DATARecord.DURATION, record.getDuration());
        value.put(DATARecord.SECONDS, record.getSeconds());
        value.put(DATARecord.NOTES, record.getNote());
        value.put(DATARecord.TEMPLATE_KEY, record.getTemplateId());
        value.put(DATARecord.TEMPLATE_RECORD_KEY, record.getTemplateRecordId());
        value.put(DATARecord.TEMPLATE_SESSION_KEY, record.getTemplateSessionId());
        value.put(DATARecord.TEMPLATE_REST_TIME, record.getRestTime());
        value.put(DATARecord.TEMPLATE_ORDER, record.getTemplateOrder());
        value.put(DATARecord.TEMPLATE_RECORD_STATUS, record.getProgramRecordStatus().ordinal());
        value.put(DATARecord.RECORD_TYPE, record.getRecordType().ordinal());
        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(record.getId())});
    }

    public void closeCursor() {
        if (mCursor != null) mCursor.close();
    }

    public void closeAll() {
        if (mCursor != null) mCursor.close();
        close();
    }



}
