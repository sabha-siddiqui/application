package com.easyfitness.DAO;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

import com.csvread.CsvReader;
import com.csvread.CsvWriter;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DATABodyMeasure;
import com.easyfitness.DAO.bodymeasures.DATABodyPart;
import com.easyfitness.DAO.cardio.DATAOldCardio;
import com.easyfitness.DAO.record.DATACardio;
import com.easyfitness.DAO.record.DATARecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.Unit;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.DateConverter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CVSManager {

    static private String TABLE_HEAD = "table";
    static private String ID_HEAD = "id";

    private Context mContext = null;

    public CVSManager(Context pContext) {
        mContext = pContext;
    }

    public boolean exportDatabase(Profile pProfile) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return false;
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s_", Locale.getDefault());
            Date date = new Date();
            File exportDir = Environment.getExternalStoragePublicDirectory("/FastnFitness/export/" + dateFormat.format(date) + pProfile.getName());
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            PrintWriter printWriter = null;
            try {
                exportBodyMeasures(exportDir, pProfile);
                exportRecords(exportDir, pProfile);
                exportExercise(exportDir, pProfile);
                exportBodyParts(exportDir, pProfile);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (printWriter != null) printWriter.close();
            }
            return true;
        }
    }

    private boolean exportRecords(File exportDir, Profile pProfile) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault());
            Date date = new Date();

            CsvWriter csvOutputFonte = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_Records_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));
            DATARecord dbc = new DATARecord(mContext);
            dbc.open();
            List<Record> records = null;
            Cursor cursor = dbc.getAllRecordsByProfile(pProfile);
            records = dbc.fromCursorToList(cursor);
            csvOutputFonte.write(TABLE_HEAD);
            csvOutputFonte.write(ID_HEAD);
            csvOutputFonte.write(DATARecord.DATE);
            csvOutputFonte.write(DATARecord.TIME);
            csvOutputFonte.write(DATARecord.EXERCISE);
            csvOutputFonte.write(DATARecord.EXERCISE_TYPE);
            csvOutputFonte.write(DATARecord.PROFILE_KEY);
            csvOutputFonte.write(DATARecord.SETS);
            csvOutputFonte.write(DATARecord.REPS);
            csvOutputFonte.write(DATARecord.WEIGHT);
            csvOutputFonte.write(DATARecord.WEIGHT_UNIT);
            csvOutputFonte.write(DATARecord.SECONDS);
            csvOutputFonte.write(DATARecord.DISTANCE);
            csvOutputFonte.write(DATARecord.DISTANCE_UNIT);
            csvOutputFonte.write(DATARecord.DURATION);
            csvOutputFonte.write(DATARecord.NOTES);
            csvOutputFonte.write(DATARecord.RECORD_TYPE);
            csvOutputFonte.endRecord();

            for (int i = 0; i < records.size(); i++) {
                csvOutputFonte.write(DATARecord.TABLE_NAME);
                csvOutputFonte.write(Long.toString(records.get(i).getId()));

                Date dateRecord = records.get(i).getDate();

                csvOutputFonte.write(DateConverter.dateToDBDateStr(dateRecord));
                csvOutputFonte.write(records.get(i).getTime());
                csvOutputFonte.write(records.get(i).getExercise());
                csvOutputFonte.write(Integer.toString(ExerciseType.STRENGTH.ordinal()));
                csvOutputFonte.write(Long.toString(records.get(i).getProfileId()));
                csvOutputFonte.write(Integer.toString(records.get(i).getSets()));
                csvOutputFonte.write(Integer.toString(records.get(i).getReps()));
                csvOutputFonte.write(Float.toString(records.get(i).getWeight()));
                csvOutputFonte.write(Integer.toString(records.get(i).getWeightUnit().ordinal()));
                csvOutputFonte.write(Integer.toString(records.get(i).getSeconds()));
                csvOutputFonte.write(Float.toString(records.get(i).getDistance()));
                csvOutputFonte.write(Integer.toString(records.get(i).getDistanceUnit().ordinal()));
                csvOutputFonte.write(Long.toString(records.get(i).getDuration()));
                if (records.get(i).getNote() == null) csvOutputFonte.write("");
                else csvOutputFonte.write(records.get(i).getNote());
                csvOutputFonte.write(Integer.toString(records.get(i).getRecordType().ordinal()));
                csvOutputFonte.endRecord();
            }
            csvOutputFonte.close();
            dbc.closeAll();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean exportBodyMeasures(File exportDir, Profile pProfile) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault());
            Date date = new Date();
            CsvWriter cvsOutput = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_BodyMeasures_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));
            DATABodyMeasure daoBodyMeasure = new DATABodyMeasure(mContext);
            daoBodyMeasure.open();

            DATABodyPart daoBodyPart = new DATABodyPart(mContext);

            List<BodyMeasure> bodyMeasures;
            bodyMeasures = daoBodyMeasure.getBodyMeasuresList(pProfile);

            cvsOutput.write(TABLE_HEAD);
            cvsOutput.write(ID_HEAD);
            cvsOutput.write(DATABodyMeasure.DATE);
            cvsOutput.write("bodypart_label");
            cvsOutput.write(DATABodyMeasure.MEASURE);
            cvsOutput.write(DATABodyMeasure.PROFIL_KEY);
            cvsOutput.endRecord();

            for (int i = 0; i < bodyMeasures.size(); i++) {
                cvsOutput.write(DATABodyMeasure.TABLE_NAME);
                cvsOutput.write(Long.toString(bodyMeasures.get(i).getId()));
                Date dateRecord = bodyMeasures.get(i).getDate();
                cvsOutput.write(DateConverter.dateToDBDateStr(dateRecord));
                BodyPart bp = daoBodyPart.getBodyPart(bodyMeasures.get(i).getBodyPartID());
                cvsOutput.write(bp.getName(mContext)); // Write the full name of the BodyPart
                cvsOutput.write(Float.toString(bodyMeasures.get(i).getBodyMeasure()));
                cvsOutput.write(Long.toString(bodyMeasures.get(i).getProfileID()));

                cvsOutput.endRecord();
            }
            cvsOutput.close();
            daoBodyMeasure.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean exportBodyParts(File exportDir, Profile pProfile) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault());
            Date date = new Date();
            CsvWriter cvsOutput = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_CustomBodyPart_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));
            DATABodyPart daoBodyPart = new DATABodyPart(mContext);
            daoBodyPart.open();


            List<BodyPart> bodyParts;
            bodyParts = daoBodyPart.getList();

            cvsOutput.write(TABLE_HEAD);
            cvsOutput.write(DATABodyPart.KEY);
            cvsOutput.write(DATABodyPart.CUSTOM_NAME);
            cvsOutput.write(DATABodyPart.CUSTOM_PICTURE);
            cvsOutput.endRecord();

            for (BodyPart bp : bodyParts ) {
                if (bp.getBodyPartResKey()==-1) { // Only custom BodyPart are exported
                    cvsOutput.write(DATABodyMeasure.TABLE_NAME);
                    cvsOutput.write(Long.toString(bp.getId()));
                    cvsOutput.write(bp.getName(mContext));
                    cvsOutput.write(bp.getCustomPicture());
                    cvsOutput.endRecord();
                }
            }
            cvsOutput.close();
            daoBodyPart.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean exportExercise(File exportDir, Profile pProfile) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault());
            Date date = new Date();

            CsvWriter csvOutput = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_Exercises_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));
            DATAMachine dbcMachine = new DATAMachine(mContext);
            dbcMachine.open();
            List<Machine> records = null;
            records = dbcMachine.getAllMachinesArray();
            csvOutput.write(TABLE_HEAD);
            csvOutput.write(ID_HEAD);
            csvOutput.write(DATAMachine.NAME);
            csvOutput.write(DATAMachine.DESCRIPTION);
            csvOutput.write(DATAMachine.TYPE);
            csvOutput.write(DATAMachine.BODYPARTS);
            csvOutput.write(DATAMachine.FAVORITES);
            csvOutput.endRecord();

            for (int i = 0; i < records.size(); i++) {
                csvOutput.write(DATAMachine.TABLE_NAME);
                csvOutput.write(Long.toString(records.get(i).getId()));
                csvOutput.write(records.get(i).getName());
                csvOutput.write(records.get(i).getDescription());
                csvOutput.write(Integer.toString(records.get(i).getType().ordinal()));
                csvOutput.write(records.get(i).getBodyParts());
                csvOutput.write(Boolean.toString(records.get(i).getFavorite()));
                csvOutput.endRecord();
            }
            csvOutput.close();
            dbcMachine.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean importDatabase(String file, Profile pProfile) {

        boolean ret = true;

        try {
            CsvReader csvRecords = new CsvReader(file, ',', Charset.forName("UTF-8"));

            csvRecords.readHeaders();

            ArrayList<Record> recordsList = new ArrayList<>() ;

            DATAMachine dbcMachine = new DATAMachine(mContext);

            while (csvRecords.readRecord()) {
                switch (csvRecords.get(TABLE_HEAD)) {
                    case DATARecord.TABLE_NAME: {
                        Date date;
                        date = DateConverter.DBDateStrToDate(csvRecords.get(DATARecord.DATE));
                        String time = csvRecords.get(DATARecord.TIME);
                        String exercise = csvRecords.get(DATARecord.EXERCISE);
                        if ( dbcMachine.getMachine(exercise) != null ) {
                            long exerciseId = dbcMachine.getMachine(exercise).getId();
                            ExerciseType exerciseType = dbcMachine.getMachine(exercise).getType();

                            float poids = TryGetFloat(csvRecords.get(DATARecord.WEIGHT), 0);
                            int repetition = TryGetInteger(csvRecords.get(DATARecord.REPS), 0);
                            int serie = TryGetInteger(csvRecords.get(DATARecord.SETS), 0);
                            WeightUnit unit = WeightUnit.KG;
                            if (!csvRecords.get(DATARecord.WEIGHT_UNIT).isEmpty()) {
                                unit = WeightUnit.fromInteger(TryGetInteger(csvRecords.get(DATARecord.WEIGHT_UNIT), WeightUnit.KG.ordinal()));
                            }
                            int second = TryGetInteger(csvRecords.get(DATARecord.SECONDS), 0);
                            float distance = TryGetFloat(csvRecords.get(DATARecord.DISTANCE), 0);
                            int duration = TryGetInteger(csvRecords.get(DATARecord.DURATION), 0);
                            DistanceUnit distance_unit = DistanceUnit.KM;
                            if (!csvRecords.get(DATARecord.DISTANCE_UNIT).isEmpty()) {
                                distance_unit = DistanceUnit.fromInteger(TryGetInteger(csvRecords.get(DATARecord.DISTANCE_UNIT), DistanceUnit.KM.ordinal()));
                            }
                            String notes = csvRecords.get(DATARecord.NOTES);

                            Record record = new Record(date, time, exercise, exerciseId, pProfile.getId(), serie, repetition, poids, unit, second, distance, distance_unit, duration, notes, exerciseType, -1);
                            recordsList.add(record);
                        } else {
                            return false;
                        }

                        break;
                    }
                    case DATAOldCardio.TABLE_NAME: {
                        DATACardio dbcCardio = new DATACardio(mContext);
                        dbcCardio.open();
                        Date date;

                        date =DateConverter.DBDateStrToDate(csvRecords.get(DATACardio.DATE));

                        String exercice = csvRecords.get(DATAOldCardio.EXERCICE);
                        float distance = Float.valueOf(csvRecords.get(DATAOldCardio.DISTANCE));
                        int duration = Integer.valueOf(csvRecords.get(DATAOldCardio.DURATION));
                        dbcCardio.addCardioRecord(date, "", exercice, distance, duration, pProfile.getId(), DistanceUnit.KM, -1);
                        dbcCardio.close();

                        break;
                    }
                    case DATAProfileWeight.TABLE_NAME: {
                        DATABodyMeasure dbcWeight = new DATABodyMeasure(mContext);
                        dbcWeight.open();
                        Date date;
                        date = DateConverter.DBDateStrToDate(csvRecords.get(DATAProfileWeight.DATE));

                        float poids = Float.parseFloat(csvRecords.get(DATAProfileWeight.POIDS));
                        dbcWeight.addBodyMeasure(date, BodyPartExtensions.WEIGHT, poids, pProfile.getId(), Unit.KG);

                        break;
                    }
                    case DATABodyMeasure.TABLE_NAME: {
                        DATABodyMeasure dbcBodyMeasure = new DATABodyMeasure(mContext);
                        dbcBodyMeasure.open();
                        Date date = DateConverter.DBDateStrToDate(csvRecords.get(DATABodyMeasure.DATE));
                        Unit unit = Unit.fromInteger(Integer.parseInt(csvRecords.get(DATABodyMeasure.UNIT))); // Mandatory. Cannot not know the Unit.
                        String bodyPartName = csvRecords.get("bodypart_label");
                        DATABodyPart dbcBodyPart = new DATABodyPart(mContext);
                        dbcBodyPart.open();
                        List<BodyPart> bodyParts;
                        bodyParts = dbcBodyPart.getList();
                        for (BodyPart bp : bodyParts) {
                            if (bp.getName(mContext).equals(bodyPartName)) {
                                float measure = Float.valueOf(csvRecords.get(DATABodyMeasure.MEASURE));
                                dbcBodyMeasure.addBodyMeasure(date, bp.getId(), measure, pProfile.getId(), unit);
                                dbcBodyPart.close();
                                break;
                            }
                        }
                        break;
                    }
                    case DATABodyPart.TABLE_NAME: {
                        DATABodyPart dbcBodyPart = new DATABodyPart(mContext);
                        dbcBodyPart.open();
                        int bodyPartId = -1;
                        String customName = csvRecords.get(DATABodyPart.CUSTOM_NAME);
                        String customPicture = csvRecords.get(DATABodyPart.CUSTOM_PICTURE);
                        dbcBodyPart.add(bodyPartId, customName, customPicture, 0, BodyPartExtensions.TYPE_MUSCLE);
                        break;
                    }
                    case DATAProfile.TABLE_NAME:
                        // TODO : import profiles
                        break;
                    case DATAMachine.TABLE_NAME:
                        DATAMachine dbc = new DATAMachine(mContext);
                        String name = csvRecords.get(DATAMachine.NAME);
                        String description = csvRecords.get(DATAMachine.DESCRIPTION);
                        ExerciseType type = ExerciseType.fromInteger(Integer.parseInt(csvRecords.get(DATAMachine.TYPE)));
                        boolean favorite = TryGetBoolean(csvRecords.get(DATAMachine.FAVORITES), false);
                        String bodyParts = csvRecords.get(DATAMachine.BODYPARTS);
                        if (dbc.getMachine(name) == null) {
                            dbc.addMachine(name, description, type, "", favorite, bodyParts);
                        } else {
                            Machine m = dbc.getMachine(name);
                            m.setDescription(description);
                            m.setFavorite(favorite);
                            m.setBodyParts(bodyParts);
                            dbc.updateMachine(m);
                        }
                        break;
                }
            }

            csvRecords.close();
            DATARecord daoRecord = new DATARecord(mContext);
            daoRecord.addList(recordsList);

        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }

        return ret;
    }

    private int TryGetInteger(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private float TryGetFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean TryGetBoolean(String value, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Unit TryGetUnit(String value, Unit defaultValue) {
        Unit unit = Unit.fromString(value);
        if (unit!=null) {
            return unit;
        }
        return defaultValue;
    }

}
