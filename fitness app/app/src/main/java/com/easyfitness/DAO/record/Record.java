package com.easyfitness.DAO.record;

import com.easyfitness.DAO.Weight;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.RecordType;
import com.easyfitness.enums.WeightUnit;

import java.util.Date;
public class Record {
    private long mId;

    private Date mDate;
    private String mTime;
    private String mExercise;
    private long mExerciseId;
    private long mProfileId;

    private int mSets;
    private int mReps;
    private float mWeight;
    private WeightUnit mWeightUnit;

    private int mSecond;

    private float mDistance;
    private DistanceUnit mDistanceUnit;
    private long mDuration;

    private String mNote;

    private ExerciseType mExerciseType;
    private RecordType mRecordType;

    private int mRestTime;

    private long mTemplateId;
    private long mTemplateSessionId;
    private long mTemplateRecordId;

    private int mTemplateOrder;
    private ProgramRecordStatus mProgramRecordStatus;

    public Record(Date date, String time, String exercise, long exerciseId, long profileId, int sets, int reps, float weight, WeightUnit weightUnit, int second, float distance, DistanceUnit distanceUnit, long duration, String note, ExerciseType exerciseType, long recordTemplateId) {
        this(date, time, exercise,  exerciseId, profileId,  sets, reps, weight, weightUnit, second, distance, distanceUnit, duration, note, exerciseType, -1, recordTemplateId, -1, 0, 0, ProgramRecordStatus.SUCCESS, RecordType.FREE_RECORD_TYPE);
    }

    public Record(Date date, String time, String exercise, long exerciseId, long profileId, int sets, int reps, float weight, WeightUnit weightUnit, int second, float distance, DistanceUnit distanceUnit, long duration, String note, ExerciseType exerciseType, long templateId, long templateRecordId, long templateSessionId, int restTime, int templateOrder, ProgramRecordStatus programRecordStatus, RecordType recordType) {
        mDate = date;
        mTime = time;
        mExercise = exercise;
        mExerciseId = exerciseId;
        mProfileId = profileId;
        mSets = sets;
        mReps = reps;
        mWeight = weight;
        mWeightUnit = weightUnit;
        mSecond = second;
        mDistance = distance;
        mDistanceUnit = distanceUnit;
        mDuration = duration;
        mNote = note;
        mExerciseType = exerciseType;
        mRecordType = recordType;
        mRestTime = restTime;
        mTemplateId = templateId;
        mTemplateRecordId = templateRecordId;
        mTemplateSessionId = templateSessionId;
        mTemplateOrder = templateOrder;
        mProgramRecordStatus = programRecordStatus;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getExercise() {
        return mExercise;
    }

    public void setExercise(String exercise) {
        mExercise = exercise;
    }

    public long getExerciseId() {
        return mExerciseId;
    }

    public void setExerciseId(long exerciseId) {
        mExerciseId = exerciseId;
    }

    public long getProfileId() {
        return mProfileId;
    }

    public void setProfileId(long profileId) {
        mProfileId = profileId;
    }

    public int getSets() {
        return mSets;
    }

    public void setSets(int sets) {
        mSets = sets;
    }

    public int getReps() {
        return mReps;
    }

    public void setReps(int reps) {
        mReps = reps;
    }

    public float getWeight() {
        return mWeight;
    }

    public void setWeight(float weight) {
        mWeight = weight;
    }

    public WeightUnit getWeightUnit() {
        return mWeightUnit;
    }

    public void setWeightUnit(WeightUnit weightUnit) {
        mWeightUnit = weightUnit;
    }

    public int getSeconds() {
        return mSecond;
    }

    public void setSeconds(int second) {
        mSecond = second;
    }

    public float getDistance() {
        return mDistance;
    }

    public void setDistance(float distance) {
        mDistance = distance;
    }

    public DistanceUnit getDistanceUnit() {
        return mDistanceUnit;
    }

    public void setDistanceUnit(DistanceUnit distanceUnit) {
        mDistanceUnit = distanceUnit;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        mNote = note;
    }

    public ExerciseType getExerciseType() {
        return mExerciseType;
    }

    public void setExerciseType(ExerciseType exerciseType) {
        mExerciseType = exerciseType;
    }

    public RecordType getRecordType() {
        return mRecordType;
    }

    public void setRecordType(RecordType recordType) {
        mRecordType = recordType;
    }

    public int getTemplateOrder() {
        return mTemplateOrder;
    }

    public void setTemplateOrder(int templateOrder) {
        mTemplateOrder = templateOrder;
    }

    public long getTemplateId() {
        return mTemplateId;
    }

    public void setTemplateId(long templateId) {
        mTemplateId = templateId;
    }

    public int getRestTime() {
        return mRestTime;
    }

    public void setRestTime(int restTime) {
        mRestTime = restTime;
    }

    public long getTemplateSessionId() {
        return mTemplateSessionId;
    }

    public void setTemplateSessionId(long templateSessionId) {
        mTemplateSessionId = templateSessionId;
    }

    public ProgramRecordStatus getProgramRecordStatus() {
        return mProgramRecordStatus;
    }

    public void setProgramRecordStatus(ProgramRecordStatus programRecordStatus) {
        mProgramRecordStatus = programRecordStatus;
    }

    public long getTemplateRecordId() {
        return mTemplateRecordId;
    }

    public void setTemplateRecordId(long id) {
        mTemplateRecordId = id;
    }
}
