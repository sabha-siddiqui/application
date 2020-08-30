package com.easyfitness.fonte;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easyfitness.BtnClickListener;
import com.easyfitness.CountdownDialogbox;
import com.easyfitness.DAO.DATAMachine;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.Weight;
import com.easyfitness.DAO.record.DATACardio;
import com.easyfitness.DAO.record.DATAFonte;
import com.easyfitness.DAO.record.DATARecord;
import com.easyfitness.DAO.record.DATAStatic;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.DatePickerDialogFragment;
import com.easyfitness.MainActivity;
import com.easyfitness.ProfileViMo;
import com.easyfitness.R;
import com.easyfitness.SettingsFragment;
import com.easyfitness.TimePickerDialogFragment;
import com.easyfitness.enums.DisplayType;
import com.easyfitness.machines.ExerciseDetailsPager;
import com.easyfitness.machines.MachineArrayFullAdapter;
import com.easyfitness.machines.MachineCursorAdapter;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.utils.ExpandedListView;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.Keyboard;
import com.easyfitness.utils.UnitConverter;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.views.WorkoutValuesInputView;
import com.ikovac.timepickerwithseconds.MyTimePickerDialog;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class FontesFragment extends Fragment {

    private int lTableColor = 1;
    private DisplayType mDisplayType = DisplayType.FREE_WORKOUT_DISPLAY;
    private long mTemplateId;
    private MainActivity mActivity = null;
    private AutoCompleteTextView machineEdit = null;
    private MachineArrayFullAdapter machineEditAdapter = null;
    private CircularImageView machineImage = null;
    private ImageButton machineListButton = null;
    private ImageButton detailsExpandArrow = null;
    private LinearLayout detailsLayout = null;
    private CardView detailsCardView = null;
    private CheckBox autoTimeCheckBox = null;
    private TextView dateEdit = null;
    private TextView timeEdit = null;

    private Button addButton = null;
    private ExpandedListView recordList = null;
    private AlertDialog machineListDialog;
    private DatePickerDialogFragment mDateFrag = null;
    private TimePickerDialogFragment mTimeFrag = null;
    private WorkoutValuesInputView workoutValuesInputView;

    private DATAFonte mDbBodyBuilding = null;
    private DATACardio mDbCardio = null;
    private DATAStatic mDbStatic = null;
    private DATARecord mDbRecord = null;
    private DATAMachine mDbMachine = null;

    private MyTimePickerDialog.OnTimeSetListener timeSet = (view, hourOfDay, minute, second) -> {
        String strMinute = "00";
        String strHour = "00";
        String strSecond = "00";

        if (minute < 10) strMinute = "0" + minute;
        else strMinute = Integer.toString(minute);
        if (hourOfDay < 10) strHour = "0" + hourOfDay;
        else strHour = Integer.toString(hourOfDay);
        if (second < 10) strSecond = "0" + second;
        else strSecond = Integer.toString(second);

        String date = strHour + ":" + strMinute + ":" + strSecond;
        timeEdit.setText(date);
        Keyboard.hide(getContext(), timeEdit);
    };
    private OnClickListener collapseDetailsClick = v -> {
        detailsLayout.setVisibility(detailsLayout.isShown() ? View.GONE : View.VISIBLE);
        detailsExpandArrow.setImageResource(detailsLayout.isShown() ? R.drawable.ic_expand_less_black_24dp : R.drawable.ic_expand_more_black_24dp);
        saveSharedParams();
    };

    private TextWatcher exerciseTextWatcher =  new TextWatcher () {
        public void afterTextChanged(Editable s) {
            String exerciseName = s.toString();
            MachineArrayFullAdapter adapter = (MachineArrayFullAdapter) machineEdit.getAdapter();
            if (adapter!=null) {
                boolean exerciseExists = adapter.containsExercise(exerciseName);
                if (exerciseExists == false) {
                    workoutValuesInputView.setShowExerciseTypeSelector(true);
                    updateMachineImage();
                } else {
                    setCurrentMachine(exerciseName);
                }
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };
    private BtnClickListener itemClickCopyRecord = v -> {
        Record r = mDbRecord.getRecord((long)v.getTag());
        if (r != null) {
            setCurrentMachine(r.getExercise());
            if (r.getExerciseType() == ExerciseType.STRENGTH) {
                workoutValuesInputView.setReps(r.getReps());
                workoutValuesInputView.setSets(r.getSets());

                Float poids = r.getWeight();
                poids = UnitConverter.weightConverter(poids, WeightUnit.KG, r.getWeightUnit());
                workoutValuesInputView.setWeight(poids, r.getWeightUnit());
            } else if (r.getExerciseType() == ExerciseType.ISOMETRIC) {
                workoutValuesInputView.setSeconds(r.getSeconds());
                workoutValuesInputView.setSets(r.getSets());
                Float poids = r.getWeight();
                poids = UnitConverter.weightConverter(poids, WeightUnit.KG, r.getWeightUnit());
                workoutValuesInputView.setWeight(poids, r.getWeightUnit());
            }else if (r.getExerciseType() == ExerciseType.CARDIO) {
                float distance = r.getDistance();
                DistanceUnit distanceUnit = DistanceUnit.KM;
                if (r.getDistanceUnit() == DistanceUnit.MILES) {
                    distance = UnitConverter.KmToMiles((r.getDistance()));
                    distanceUnit = DistanceUnit.MILES;
                }
                workoutValuesInputView.setDistance(distance, distanceUnit);
                workoutValuesInputView.setDuration(r.getDuration());
            }
            KToast.infoToast(getMainActivity(), getString(R.string.recordcopied), Gravity.BOTTOM, KToast.LENGTH_SHORT);
        }
    };

    private OnClickListener clickAddButton = v -> {
        if (machineEdit.getText().toString().isEmpty()) {
            KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            return;
        }

        String timeStr;
        Date date;

        if (autoTimeCheckBox.isChecked()) {
            date = new Date();
            timeStr = DateConverter.currentTime();
        }else {
            date = DateConverter.editToDate(dateEdit.getText().toString());
            timeStr = timeEdit.getText().toString();
        }

        ExerciseType exerciseType;
        Machine lMachine = mDbMachine.getMachine(machineEdit.getText().toString());
        if (lMachine == null) {
            exerciseType = workoutValuesInputView.getSelectedType();
        } else {
            exerciseType = lMachine.getType();
        }

        if (exerciseType == ExerciseType.STRENGTH) {
            if (!workoutValuesInputView.isFilled()) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }
            float tmpPoids = workoutValuesInputView.getWeightValue();
            tmpPoids = UnitConverter.weightConverter(tmpPoids,workoutValuesInputView.getWeightUnit(), WeightUnit.KG); // Always convert to KG

            if(mDisplayType == DisplayType.FREE_WORKOUT_DISPLAY) {
                mDbBodyBuilding.addBodyBuildingRecord(date, timeStr,
                    machineEdit.getText().toString(),
                    workoutValuesInputView.getSets(),
                    workoutValuesInputView.getReps(),
                    tmpPoids,
                    workoutValuesInputView.getWeightUnit(),
                    "",
                    getProfile().getId(), -1);

                float iTotalWeightSession = mDbBodyBuilding.getTotalWeightSession(date, getProfile());
                float iTotalWeight = mDbBodyBuilding.getTotalWeightMachine(date, machineEdit.getText().toString(), getProfile());
                int iNbSeries = mDbBodyBuilding.getNbSeries(date, machineEdit.getText().toString(), getProfile());

                boolean bLaunchRest = workoutValuesInputView.isRestTimeActivated();
                int restTime = workoutValuesInputView.getRestTime();
                if (bLaunchRest && DateConverter.dateToLocalDateStr(date, getContext()).equals(DateConverter.dateToLocalDateStr(new Date(), getContext()))) { // Only launch Countdown if date is today.
                    CountdownDialogbox cdd = new CountdownDialogbox(getActivity(), restTime, lMachine);
                    cdd.setNbSeries(iNbSeries);
                    cdd.setTotalWeightMachine(iTotalWeight);
                    cdd.setTotalWeightSession(iTotalWeightSession);
                    cdd.show();
                }
            } else if (mDisplayType == DisplayType.PROGRAM_EDIT_DISPLAY) {
                for (int i = 0; i<workoutValuesInputView.getSets(); i++ ) {
                    mDbBodyBuilding.addWeightRecordToProgramTemplate(mTemplateId, -1, date, timeStr,
                        machineEdit.getText().toString(),
                        1,
                        workoutValuesInputView.getReps(),
                        tmpPoids,
                        workoutValuesInputView.getWeightUnit(),
                        workoutValuesInputView.getRestTime()
                    );
                }
            }
        } else if (exerciseType == ExerciseType.ISOMETRIC) {
            if (!workoutValuesInputView.isFilled()) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }
            float tmpPoids = workoutValuesInputView.getWeightValue();
            tmpPoids = UnitConverter.weightConverter(tmpPoids,workoutValuesInputView.getWeightUnit(), WeightUnit.KG); // Always convert to KG

            if(mDisplayType == DisplayType.FREE_WORKOUT_DISPLAY) {
                mDbStatic.addStaticRecord(date,
                    machineEdit.getText().toString(),
                    workoutValuesInputView.getSets(),
                    workoutValuesInputView.getSeconds(),
                    tmpPoids,
                    getProfile().getId(),
                    workoutValuesInputView.getWeightUnit(),
                    "",
                    timeStr, -1
                );

                float iTotalWeightSession = mDbStatic.getTotalWeightSession(date, getProfile());
                float iTotalWeight = mDbStatic.getTotalWeightMachine(date, machineEdit.getText().toString(), getProfile());
                int iNbSeries = mDbStatic.getNbSeries(date, machineEdit.getText().toString(), getProfile());

                boolean bLaunchRest = workoutValuesInputView.isRestTimeActivated();
                int restTime = workoutValuesInputView.getRestTime();

                if (bLaunchRest && DateConverter.dateToLocalDateStr(date, getContext()).equals(DateConverter.dateToLocalDateStr(new Date(), getContext()))) { // Only launch Countdown if date is today.
                    CountdownDialogbox cdd = new CountdownDialogbox(getActivity(), restTime, lMachine);
                    cdd.setNbSeries(iNbSeries);
                    cdd.setTotalWeightMachine(iTotalWeight);
                    cdd.setTotalWeightSession(iTotalWeightSession);
                    cdd.show();
                }
            } else if (mDisplayType == DisplayType.PROGRAM_EDIT_DISPLAY) {
                for (int i = 0; i<workoutValuesInputView.getSets(); i++ ) {
                    mDbStatic.addStaticRecordToProgramTemplate(mTemplateId, -1, date, timeStr,
                        machineEdit.getText().toString(),
                        1,
                        workoutValuesInputView.getSeconds(),
                        tmpPoids, // Always save in KG
                        workoutValuesInputView.getWeightUnit(),
                        workoutValuesInputView.getRestTime()
                    );
                }
            }
        } else if (exerciseType == ExerciseType.CARDIO) {
            if (!workoutValuesInputView.isFilled()) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            long duration = workoutValuesInputView.getDurationValue();

            float distance = workoutValuesInputView.getDistanceValue();
            if (workoutValuesInputView.getDistanceUnit()==DistanceUnit.MILES) {
                distance = UnitConverter.MilesToKm(distance); // Always convert to KG
            }

            if (mDisplayType == DisplayType.FREE_WORKOUT_DISPLAY) {
                mDbCardio.addCardioRecord(date,
                    timeStr,
                    machineEdit.getText().toString(),
                    distance,
                    duration,
                    getProfile().getId(),
                    workoutValuesInputView.getDistanceUnit(), -1);
                boolean bLaunchRest = workoutValuesInputView.isRestTimeActivated();
                int restTime = workoutValuesInputView.getRestTime();
                if (bLaunchRest && DateConverter.dateToLocalDateStr(date, getContext()).equals(DateConverter.dateToLocalDateStr(new Date(), getContext()))) { // Only launch Countdown if date is today.
                    CountdownDialogbox cdd = new CountdownDialogbox(getActivity(), restTime, lMachine);
                    cdd.show();
                }
            } else if (mDisplayType == DisplayType.PROGRAM_EDIT_DISPLAY) {
                mDbCardio.addCardioRecordToProgramTemplate(mTemplateId, -1,
                    date,
                    timeStr,
                    machineEdit.getText().toString(),
                    distance,
                    workoutValuesInputView.getDistanceUnit(),
                    duration,
                    workoutValuesInputView.getRestTime()
                );
            }
        }

        getActivity().findViewById(R.id.drawer_layout).requestFocus();
        Keyboard.hide(getContext(), v);

        lTableColor = (lTableColor + 1) % 2;

        refreshData();
        if (mDisplayType == DisplayType.FREE_WORKOUT_DISPLAY)
            addButton.setText(getView().getContext().getString(R.string.AddLabel) + "\n(" + DateConverter.currentTime() + ")");

        mDbCardio.closeCursor();
        mDbBodyBuilding.closeCursor();
        mDbStatic.closeCursor();
        mDbRecord.closeCursor();

        saveSharedParams();
    };
    private OnClickListener onClickMachineListWithIcons = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Cursor c;
            Cursor oldCursor;
            if (machineListDialog != null && machineListDialog.isShowing()) {
                return;
            }

            ListView machineList = new ListView(v.getContext());
            c = mDbMachine.getAllMachines();

            if (c == null || c.getCount() == 0) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.createExerciseFirst).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                machineList.setAdapter(null);
            } else {
                if (machineList.getAdapter() == null) {
                    MachineCursorAdapter mTableAdapter = new MachineCursorAdapter(getActivity(), c, 0, mDbMachine);
                    machineList.setAdapter(mTableAdapter);
                } else {
                    MachineCursorAdapter mTableAdapter = ((MachineCursorAdapter) machineList.getAdapter());
                    oldCursor = mTableAdapter.swapCursor(c);
                    if (oldCursor != null) oldCursor.close();
                }

                machineList.setOnItemClickListener((parent, view, position, id) -> {
                    TextView textView = view.findViewById(R.id.LIST_MACHINE_ID);
                    long machineID = Long.parseLong(textView.getText().toString());
                    DATAMachine lMachineDb = new DATAMachine(getContext());
                    Machine lMachine = lMachineDb.getMachine(machineID);

                    setCurrentMachine(lMachine.getName());

                    getMainActivity().findViewById(R.id.drawer_layout).requestFocus();
                    Keyboard.hide(getContext(), getMainActivity().findViewById(R.id.drawer_layout));

                    if (machineListDialog.isShowing()) {
                        machineListDialog.dismiss();
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(R.string.selectMachineDialogLabel);
                builder.setView(machineList);
                machineListDialog = builder.create();
                machineListDialog.show();
            }
        }
    };
    private OnItemLongClickListener itemlongclickDeleteRecord = (listView, view, position, id) -> {
        showRecordListMenu(id);
        return true;
    };
    private OnItemClickListener onItemClickFilterList = (parent, view, position, id) -> setCurrentMachine(machineEdit.getText().toString());
    private DatePickerDialog.OnDateSetListener dateSet = (view, year, month, day) -> {
        dateEdit.setText(DateConverter.dateToString(year, month + 1, day));
        Keyboard.hide(getContext(), dateEdit);
    };
    private OnClickListener clickDateEdit = v -> {
        switch (v.getId()) {
            case R.id.editDate:
                showDatePickerFragment();
                break;
            case R.id.editTime:
                showTimePicker(timeEdit);
                break;
        }
    };
    private OnFocusChangeListener touchRazEdit = (v, hasFocus) -> {
        if (hasFocus) {
            updateMachineImage();

            workoutValuesInputView.setWeightComment("");
            workoutValuesInputView.setShowExerciseTypeSelector(true);

            v.post(() -> {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            });
        } else {
            if (!workoutValuesInputView.isShowExerciseTypeSelector())
                setCurrentMachine(machineEdit.getText().toString());
        }
        updateMachineImage();
    };

    private void updateMachineImage() {
        switch (workoutValuesInputView.getSelectedType()) {
            case CARDIO:
                machineImage.setImageResource(R.drawable.ic_training_white_50dp);
                break;
            case ISOMETRIC:
                machineImage.setImageResource(R.drawable.ic_static);
                break;
            case STRENGTH:
            default:
                machineImage.setImageResource(R.drawable.ic_gym_bench_50dp);
        }
    }

    private CompoundButton.OnCheckedChangeListener checkedAutoTimeCheckBox = (buttonView, isChecked) -> {
        dateEdit.setEnabled(!isChecked);
        timeEdit.setEnabled(!isChecked);
        if (isChecked) {
            dateEdit.setText(DateConverter.currentDate());
            timeEdit.setText(DateConverter.currentTime());
        }
    };
    private ProfileViMo profileViMo;
    public static FontesFragment newInstance(int displayType, long templateId) {
        FontesFragment f = new FontesFragment();
        Bundle args = new Bundle();
        args.putLong("templateId", templateId);
        args.putInt("displayType", displayType);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab_fontes, container, false);

        mTemplateId = getArguments().getLong("templateId", -1);
        mDisplayType = DisplayType.fromInteger(getArguments().getInt("displayType", DisplayType.FREE_WORKOUT_DISPLAY.ordinal()));

        machineEdit = view.findViewById(R.id.editMachine);

        workoutValuesInputView = view.findViewById(R.id.WorkoutValuesInput);

        recordList = view.findViewById(R.id.listRecord);
        machineListButton = view.findViewById(R.id.buttonListMachine);
        addButton = view.findViewById(R.id.addperff);

        detailsCardView = view.findViewById(R.id.detailsCardView);
        detailsLayout = view.findViewById(R.id.notesLayout);
        detailsExpandArrow = view.findViewById(R.id.buttonExpandArrow);
        machineImage = view.findViewById(R.id.imageMachine);

        autoTimeCheckBox = view.findViewById(R.id.autoTimeCheckBox);
        dateEdit = view.findViewById(R.id.editDate);
        timeEdit = view.findViewById(R.id.editTime);
        addButton.setOnClickListener(clickAddButton);
        machineListButton.setOnClickListener(onClickMachineListWithIcons); //onClickMachineList

        dateEdit.setOnClickListener(clickDateEdit);
        timeEdit.setOnClickListener(clickDateEdit);
        autoTimeCheckBox.setOnCheckedChangeListener(checkedAutoTimeCheckBox);
        machineEdit.addTextChangedListener(exerciseTextWatcher);
        machineEdit.setOnItemClickListener(onItemClickFilterList);
        recordList.setOnItemLongClickListener(itemlongclickDeleteRecord);
        detailsExpandArrow.setOnClickListener(collapseDetailsClick);

        restoreSharedParams();

        WeightUnit weightUnit = SettingsFragment.getDefaultWeightUnit(getActivity());
        workoutValuesInputView.setWeightUnit(weightUnit);

        DistanceUnit distanceUnit = SettingsFragment.getDefaultDistanceUnit(getActivity());
        workoutValuesInputView.setDurationUnit(distanceUnit);

        mDbBodyBuilding = new DATAFonte(getContext());
        mDbCardio = new DATACardio(getContext());
        mDbStatic = new DATAStatic(getContext());
        mDbRecord = new DATARecord(getContext());
        mDbMachine = new DATAMachine(getContext());

        machineImage.setOnClickListener(v -> {
            Machine m = mDbMachine.getMachine(machineEdit.getText().toString());
            if (m != null) {
                ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(m.getId(), ((MainActivity) getActivity()).getCurrentProfile().getId());
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        profileViMo = new ViewModelProvider(requireActivity()).get(ProfileViMo.class);
        profileViMo.getProfile().observe(getViewLifecycleOwner(), profile -> {
            refreshData();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mActivity = (MainActivity) this.getActivity();
        dateEdit.setText(DateConverter.currentDate());
        timeEdit.setText(DateConverter.currentTime());
        if (mDisplayType==DisplayType.PROGRAM_EDIT_DISPLAY) {
            addButton.setText(R.string.add_to_template);
            detailsCardView.setVisibility(View.GONE);
        }
        refreshData();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public String getName() {
        return getArguments().getString("name");
    }

    public MainActivity getMainActivity() {
        return (MainActivity) this.getActivity();
    }

    private void showRecordListMenu(final long id) {

        String[] profilListArray = new String[3];
        profilListArray[0] = getActivity().getResources().getString(R.string.DeleteLabel);
        profilListArray[1] = getActivity().getResources().getString(R.string.EditLabel);
        profilListArray[2] = getActivity().getResources().getString(R.string.ShareLabel);

        AlertDialog.Builder itemActionbuilder = new AlertDialog.Builder(getView().getContext());
        itemActionbuilder.setTitle("").setItems(profilListArray, (dialog, which) -> {

            switch (which) {
                case 0:
                    showDeleteDialog(id);
                    break;
                case 1:
                    Toast.makeText(getActivity(), R.string.edit_soon_available, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Record r = mDbRecord.getRecord(id);
                    String text = "";
                    if (r.getExerciseType() == ExerciseType.STRENGTH ||r.getExerciseType() == ExerciseType.ISOMETRIC  ) {
                        text = getView().getContext().getResources().getText(R.string.ShareTextDefault).toString();
                        text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamWeight), String.valueOf(r.getWeight()));
                        text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamMachine), r.getExercise());
                    } else {
                        text = "I have done __METER__ in __TIME__ on __MACHINE__.";
                        text = text.replace("__METER__", String.valueOf(r.getDistance()));
                        text = text.replace("__TIME__", String.valueOf(r.getDuration()));
                        text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamMachine), r.getExercise());
                    }
                    shareRecord(text);
                    break;
                default:
            }
        });
        itemActionbuilder.show();
    }

    private void showDeleteDialog(final long idToDelete) {

        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(R.string.DeleteRecordDialog))
            .setContentText(getResources().getText(R.string.areyousure).toString())
            .setCancelText(getResources().getText(R.string.global_no).toString())
            .setConfirmText(getResources().getText(R.string.global_yes).toString())
            .showCancelButton(true)
            .setConfirmClickListener(sDialog -> {
                mDbRecord.deleteRecord(idToDelete);

                updateRecordTable(machineEdit.getText().toString());
                KToast.infoToast(getActivity(), getResources().getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                sDialog.dismissWithAnimation();
            })
            .show();
    }

    private void showDatePickerFragment() {
        if (mDateFrag == null) {
            mDateFrag = DatePickerDialogFragment.newInstance(dateSet);
            mDateFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog");
        } else {
            if (!mDateFrag.isVisible())
                mDateFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog");
        }
    }

    private void showTimePicker(TextView timeTextView) {
        String tx =  timeTextView.getText().toString();
        int hour;
        try {
            hour = Integer.parseInt(tx.substring(0, 2));
        } catch (Exception e) {
            hour = 0;
        }
        int min;
        try {
            min = Integer.parseInt(tx.substring(3, 5));
        } catch (Exception e) {
            min = 0;
        }
        int sec;
        try {
            sec = Integer.parseInt(tx.substring(6));
        } catch (Exception e) {
            sec = 0;
        }

        switch(timeTextView.getId()) {
            case R.id.editTime:
                if (mTimeFrag == null) {
                    mTimeFrag = TimePickerDialogFragment.newInstance(timeSet, hour, min, sec);
                    mTimeFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog_time");
                } else {
                    if (!mTimeFrag.isVisible()) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("HOUR", hour);
                        bundle.putInt("MINUTE", min);
                        bundle.putInt("SECOND", sec);
                        mTimeFrag.setArguments(bundle);
                        mTimeFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog_time");
                    }
                }
                break;
        }
    }

    public boolean shareRecord(String text) {
        AlertDialog.Builder newProfilBuilder = new AlertDialog.Builder(getView().getContext());

        newProfilBuilder.setTitle(getView().getContext().getResources().getText(R.string.ShareTitle));
        newProfilBuilder.setMessage(getView().getContext().getResources().getText(R.string.ShareInstruction));
        final EditText input = new EditText(getView().getContext());
        input.setText(text);
        newProfilBuilder.setView(input);

        newProfilBuilder.setPositiveButton(getView().getContext().getResources().getText(R.string.ShareText), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, value);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        newProfilBuilder.setNegativeButton(getView().getContext().getResources().getText(R.string.global_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        newProfilBuilder.show();

        return true;
    }

    public FontesFragment getFragment() {
        return this;
    }

    private Profile getProfile() {
        return profileViMo.getProfile().getValue();
    }

    public String getMachine() {
        return machineEdit.getText().toString();
    }

    private void setCurrentMachine(String machineStr) {
        if (machineStr.isEmpty()) {
            updateMachineImage();
            machineImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            workoutValuesInputView.setShowExerciseTypeSelector(true);
            workoutValuesInputView.setWeightComment("");
            return;
        }

        Machine lMachine = mDbMachine.getMachine(machineStr);
        if (lMachine == null) {
            machineEdit.setText("");
            machineImage.setImageResource(R.drawable.ic_gym_bench_50dp); // Default image
            changeExerciseTypeUI(ExerciseType.STRENGTH, true);
            updateMinMax(null);
            return;
        }
        if (!machineEdit.getText().toString().equals(lMachine.getName()))
            machineEdit.setText(lMachine.getName());
        switch (lMachine.getType()) {
            case CARDIO:
                machineImage.setImageResource(R.drawable.ic_training_white_50dp);
                break;
            case ISOMETRIC:
                machineImage.setImageResource(R.drawable.ic_static);
                break;
            default:
                machineImage.setImageResource(R.drawable.ic_gym_bench_50dp);
        }
        ImageUtil imgUtil = new ImageUtil();
        ImageUtil.setThumb(machineImage, imgUtil.getThumbPath(lMachine.getPicture())); // Overwrite image is there is one
        updateRecordTable(lMachine.getName());
        changeExerciseTypeUI(lMachine.getType(), false);
        updateMinMax(lMachine);
        updateLastRecord(lMachine);
    }

    private void updateMinMax(Machine m) {
        String comment ="";
        String unitStr = "";
        float weight = 0;
        if (getProfile() != null && m != null) {
            if (m.getType() == ExerciseType.STRENGTH || m.getType() == ExerciseType.ISOMETRIC) {
                DecimalFormat numberFormat = new DecimalFormat("#.##");
                Weight minValue = mDbBodyBuilding.getMin(getProfile(), m);
                if (minValue != null && minValue.getStoredWeight()!=0) {
                    weight = UnitConverter.weightConverter(minValue.getStoredWeight(), WeightUnit.KG, minValue.getStoredUnit());
                    unitStr = minValue.getStoredUnit().toString();

                    comment = getContext().getString(R.string.min) + ":" + numberFormat.format(weight) + unitStr + " - ";
                }

                Weight maxValue = mDbBodyBuilding.getMax(getProfile(), m);
                if (maxValue != null && maxValue.getStoredWeight()!=0) {
                    weight = UnitConverter.weightConverter(maxValue.getStoredWeight(), WeightUnit.KG, maxValue.getStoredUnit());
                    unitStr = maxValue.getStoredUnit().toString();
                    comment = comment + getContext().getString(R.string.max) + ":" + numberFormat.format(weight) +  unitStr;
                } else {
                    comment = "";
                }
            } else if (m.getType() == ExerciseType.CARDIO) {
                comment = "";
            }
        } else {
            comment ="";
        }

        workoutValuesInputView.setWeightComment(comment);
    }

    private void updateLastRecord(Machine m) {
        Record lLastRecord = mDbRecord.getLastExerciseRecord(m.getId(), getProfile());
        WeightUnit weightUnit = SettingsFragment.getDefaultWeightUnit(getActivity());
        DistanceUnit distanceUnit = SettingsFragment.getDefaultDistanceUnit(getActivity());
        workoutValuesInputView.setSets(1);
        workoutValuesInputView.setReps(10);
        workoutValuesInputView.setSeconds(60);
        workoutValuesInputView.setWeight(50, weightUnit);
        workoutValuesInputView.setDistance(10, distanceUnit);
        workoutValuesInputView.setDuration(600000);
        if (lLastRecord == null) {
        } else if (lLastRecord.getExerciseType() == ExerciseType.STRENGTH) {
            workoutValuesInputView.setSets(lLastRecord.getSets());
            workoutValuesInputView.setReps(lLastRecord.getReps());
            workoutValuesInputView.setWeight(UnitConverter.weightConverter(lLastRecord.getWeight(), WeightUnit.KG, lLastRecord.getWeightUnit()), lLastRecord.getWeightUnit());
        } else if (lLastRecord.getExerciseType() == ExerciseType.CARDIO) {
            workoutValuesInputView.setDuration(lLastRecord.getDuration());
            if (lLastRecord.getDistanceUnit() == DistanceUnit.MILES)
                workoutValuesInputView.setDistance(UnitConverter.KmToMiles(lLastRecord.getDistance()), DistanceUnit.MILES);
            else
                workoutValuesInputView.setDistance(lLastRecord.getDistance(), DistanceUnit.KM);
        } else if (lLastRecord.getExerciseType() == ExerciseType.ISOMETRIC) {
            workoutValuesInputView.setSets(lLastRecord.getSets());
            workoutValuesInputView.setSeconds(lLastRecord.getSeconds());
            workoutValuesInputView.setWeight(UnitConverter.weightConverter(lLastRecord.getWeight(), WeightUnit.KG, lLastRecord.getWeightUnit()), lLastRecord.getWeightUnit());
        }
    }

    private void updateRecordTable(String pMachine) {
        this.getMainActivity().setCurrentMachine(pMachine);
        if (getView()==null) return;
        getView().post(() -> {

            Cursor c = null;
            if (mDisplayType==DisplayType.FREE_WORKOUT_DISPLAY) {
                c = mDbRecord.getTop3DatesRecords(getProfile());
            } else if (mDisplayType==DisplayType.PROGRAM_EDIT_DISPLAY) {
                c = mDbRecord.getProgramTemplateRecords(mTemplateId);
            }

            List<Record> records = mDbRecord.fromCursorToList(c);

            if (records.size()==0) {
                recordList.setAdapter(null);
            } else {
                    if (recordList.getAdapter() == null) {
                        RecordArrayAdapter mTableAdapter = new RecordArrayAdapter(getActivity(), getContext(), records, mDisplayType, itemClickCopyRecord);
                        recordList.setAdapter(mTableAdapter);
                    } else {
                        ((RecordArrayAdapter) recordList.getAdapter()).setRecords(records);
                    }
            }
        });
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfile() != null) {
                mDbRecord.setProfile(getProfile());

                ArrayList<Machine> machineListArray;
                machineListArray = mDbMachine.getAllMachinesArray();
                machineEditAdapter = new MachineArrayFullAdapter(getContext(), machineListArray);
                machineEdit.setAdapter(machineEditAdapter);
                Profile profile = getProfile();

                if (machineEdit.getText().toString().isEmpty()) {
                    Record lLastRecord = mDbRecord.getLastRecord(getProfile());
                    if (lLastRecord != null) {
                        setCurrentMachine(lLastRecord.getExercise());
                    } else {
                        WeightUnit weightUnit = SettingsFragment.getDefaultWeightUnit(getActivity());
                        DistanceUnit distanceUnit = SettingsFragment.getDefaultDistanceUnit(getActivity());
                        machineEdit.setText("");
                        workoutValuesInputView.setSets(1);
                        workoutValuesInputView.setReps(10);
                        workoutValuesInputView.setSeconds(60);
                        workoutValuesInputView.setWeight(50, weightUnit);
                        workoutValuesInputView.setDistance(10, distanceUnit);
                        workoutValuesInputView.setDuration(600000);
                        setCurrentMachine("");
                        changeExerciseTypeUI(ExerciseType.STRENGTH, true);
                    }
                } else {
                    setCurrentMachine(machineEdit.getText().toString());
                }
                if (autoTimeCheckBox.isChecked()) {
                    dateEdit.setText(DateConverter.currentDate());
                    timeEdit.setText(DateConverter.currentTime());
                }
                updateRecordTable(machineEdit.getText().toString());
            }
        }
    }

    private void changeExerciseTypeUI(ExerciseType pType, boolean displaySelector) {
        workoutValuesInputView.setShowExerciseTypeSelector(displaySelector);
        switch (pType) {
            case CARDIO:
                workoutValuesInputView.setSelectedType(ExerciseType.CARDIO);
                break;
            case ISOMETRIC:
                workoutValuesInputView.setSelectedType(ExerciseType.ISOMETRIC);
                break;
            case STRENGTH:
            default:
                workoutValuesInputView.setSelectedType(ExerciseType.STRENGTH);
        }
    }

    public void saveSharedParams() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("restTime2", workoutValuesInputView.getRestTime());
        editor.putBoolean("restCheck", workoutValuesInputView.isRestTimeActivated());
        editor.putBoolean("showDetails", this.detailsLayout.isShown());
        editor.apply();
    }

    public void restoreSharedParams() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        workoutValuesInputView.setRestTime(sharedPref.getInt("restTime2", 60));
        workoutValuesInputView.activatedRestTime(sharedPref.getBoolean("restCheck", true));

        if (sharedPref.getBoolean("showDetails", false)) {
            detailsLayout.setVisibility(View.VISIBLE);
        } else {
            detailsLayout.setVisibility(View.GONE);
        }
        detailsExpandArrow.setImageResource(sharedPref.getBoolean("showDetails", false) ? R.drawable.ic_expand_less_black_24dp : R.drawable.ic_expand_more_black_24dp);
    }

}
