package com.easyfitness.programs;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.record.DATARecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.DAO.program.DATAProgram;
import com.easyfitness.DAO.program.DATAProgramHistory;
import com.easyfitness.DAO.program.Program;
import com.easyfitness.DAO.program.ProgramHistory;
import com.easyfitness.MainActivity;
import com.easyfitness.ProfileViMo;
import com.easyfitness.R;
import com.easyfitness.enums.DisplayType;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.ProgramStatus;
import com.easyfitness.enums.RecordType;
import com.easyfitness.fonte.RecordArrayAdapter;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Keyboard;
import com.easyfitness.utils.OnCustomEventListener;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProgramRunnerFragment extends Fragment {
    private Button mStartStopButton;
    private Button mNewButton;
    private Button mEditButton;
    private ListView mProgramRecordsList;
    private Spinner mProgramsSpinner;
    private TextView mProgramRecordsListTitle;

    private DATAProgram mDbWorkout;
    private DATAProgramHistory mDbWorkoutHistory;
    private DATARecord mDbRecord;
    private ArrayAdapter<Program> mAdapterPrograms;
    private List<Program> mProgramsArray;
    private Program mRunningProgram;
    private ProgramHistory mRunningProgramHistory;
    private boolean mIsProgramRunning = false;

    private ProfileViMo profileViMo;

    private View.OnClickListener onClickEditProgram = view -> {

        Program program = (Program) mProgramsSpinner.getSelectedItem();
        if (program==null) return;

        ProgramPagerFragment fragment = ProgramPagerFragment.newInstance(program.getId());
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, MainActivity.WORKOUTPAGER);
        transaction.addToBackStack(null);
        transaction.commit();
    };

    private View.OnClickListener clickAddProgramButton = v -> {
        final EditText editText = new EditText(getContext());
        editText.setText("");
        editText.setGravity(Gravity.CENTER);
        editText.requestFocus();

        LinearLayout linearLayout = new LinearLayout(getContext().getApplicationContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editText);

        final SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
            .setTitleText(getString(R.string.enter_workout_name))
            .setCancelText(getContext().getString(R.string.global_cancel))
            .setHideKeyBoardOnDismiss(true)
            .setCancelClickListener(sDialog -> {
                editText.clearFocus();
                Keyboard.hide(getContext(), editText);
                sDialog.dismissWithAnimation();})
            .setConfirmClickListener(sDialog -> {

                editText.clearFocus();
                Keyboard.hide(getContext(), editText);
                DATAProgram daoProgram = new DATAProgram(getContext());
                long temp_key = daoProgram.add(new Program(0, editText.getText().toString(), ""));

                sDialog.dismiss();
                ProgramPagerFragment fragment = ProgramPagerFragment.newInstance(temp_key);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment, MainActivity.WORKOUTPAGER);
                transaction.addToBackStack(null);
                transaction.commit();
            });
        dialog.setOnShowListener(sDialog -> {
            editText.requestFocus();
            Keyboard.show(getContext(), editText);
        });

        dialog.setCustomView(linearLayout);
        dialog.show();
    };

    private View.OnClickListener clickStartStopButton = v -> {
        if (mRunningProgram == null) {
            mRunningProgram=(Program)mProgramsSpinner.getSelectedItem();
            if (mRunningProgram != null) {
                long runningProgramId = mRunningProgram.getId();
                long profileId = getProfile().getId();
                ProgramHistory programHistory = new ProgramHistory(-1, runningProgramId, profileId, ProgramStatus.RUNNING, DateConverter.currentDate(), DateConverter.currentTime(), "", "");
                long workoutHistoryId = mDbWorkoutHistory.add(programHistory);
                mRunningProgramHistory = mDbWorkoutHistory.get(workoutHistoryId);
                mProgramsSpinner.setEnabled(false);
                mStartStopButton.setText(R.string.finish_program);
                Cursor cursor = mDbRecord.getProgramTemplateRecords(((Program) mProgramsSpinner.getSelectedItem()).getId());
                List<Record> recordList = mDbRecord.fromCursorToList(cursor);
                for (Record record : recordList) {
                    record.setTemplateRecordId(record.getId());
                    record.setTemplateSessionId(workoutHistoryId);
                    record.setRecordType(RecordType.PROGRAM_RECORD_TYPE);
                    record.setProgramRecordStatus(ProgramRecordStatus.PENDING);
                    record.setProfileId(getProfile().getId());
                    mDbRecord.addRecord(record);
                }
                refreshData();
            }
        }else{
            stopProgram();
        }
    };



    private void stopProgram(){
        mRunningProgramHistory.setEndDate(DateConverter.currentDate());
        mRunningProgramHistory.setEndTime(DateConverter.currentTime());
        mRunningProgramHistory.setStatus(ProgramStatus.CLOSED);
        mDbWorkoutHistory.update(mRunningProgramHistory);
        mRunningProgram=null;
        mRunningProgramHistory=null;
        mProgramsSpinner.setEnabled(true);
        mStartStopButton.setText(R.string.start_program);
        refreshData();
    }

    private AdapterView.OnItemSelectedListener onProgramSelected = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            refreshData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private OnCustomEventListener onProgramCompletedListener = new OnCustomEventListener() {
        @Override
        public void onEvent(String eventName) {
            final SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(getString(R.string.program_completed))
                .setConfirmText(getContext().getString(R.string.global_yes))
                .setCancelText(getContext().getString(R.string.global_no))
                .setHideKeyBoardOnDismiss(true)
                .setConfirmClickListener(sDialog -> {
                    stopProgram();
                    sDialog.dismiss();
                });
            dialog.show();
        }
    };

    public static ProgramRunnerFragment newInstance(String name, int id) {
        ProgramRunnerFragment f = new ProgramRunnerFragment();

        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbWorkout = new DATAProgram(this.getContext());
        mDbWorkoutHistory = new DATAProgramHistory(this.getContext());
        mDbRecord = new DATARecord(this.getContext());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.program_runnertab, container, false);

        mStartStopButton = view.findViewById(R.id.startStopProgram);
        mProgramsSpinner = view.findViewById(R.id.programSpinner);
        mProgramRecordsList = view.findViewById(R.id.listProgramRecord);
        mProgramRecordsListTitle = view.findViewById(R.id.programListTitle);
        mNewButton = view.findViewById(R.id.newProgram);
        mEditButton = view.findViewById(R.id.editProgram);
        TextView emptyList = view.findViewById(R.id.listProgramEmpty);
        mProgramRecordsList.setEmptyView(emptyList);

        mStartStopButton.setOnClickListener(clickStartStopButton);
        mProgramsSpinner.setOnItemSelectedListener(onProgramSelected);
        mNewButton.setOnClickListener(clickAddProgramButton);
        mEditButton.setOnClickListener(onClickEditProgram);

        profileViMo = new ViewModelProvider(requireActivity()).get(ProfileViMo.class);
        profileViMo.getProfile().observe(getViewLifecycleOwner(), profile -> {
            refreshData();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        if (mProgramsSpinner.getAdapter() == null) {
            mProgramsArray = mDbWorkout.getAll();
            mAdapterPrograms = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item,
                mProgramsArray);
            mAdapterPrograms.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mProgramsSpinner.setAdapter(mAdapterPrograms);
        } else {
            if (mProgramsArray == null)
                mProgramsArray = mDbWorkout.getAll();
            else {
                mProgramsArray.clear();
                mProgramsArray.addAll(mDbWorkout.getAll());
                mAdapterPrograms.notifyDataSetChanged();
            }
        }
        mIsProgramRunning = false;
        mRunningProgramHistory = mDbWorkoutHistory.getRunningProgram(getProfile());
        if (mRunningProgramHistory!=null) {
            int position = 0;
            for (int i = 0; i < mAdapterPrograms.getCount(); i++) {
                if (mAdapterPrograms.getItem(i).getId() == mRunningProgramHistory.getProgramId()) {
                    position = i;
                    mRunningProgram = mAdapterPrograms.getItem(i);
                    mIsProgramRunning = true;
                    mProgramsSpinner.setSelection(position);
                    mProgramsSpinner.setEnabled(false);
                    mStartStopButton.setText(R.string.finish_program);
                    mProgramRecordsListTitle.setText(R.string.program_ongoing);
                    break;
                }
            }
        }

        if (!mIsProgramRunning) {
            mRunningProgram=null;
            mProgramsSpinner.setEnabled(true);
            mStartStopButton.setText(R.string.start_program);
            mProgramRecordsListTitle.setText(R.string.program_preview);
        }
        Cursor cursor = null;
        if (mIsProgramRunning) {
            cursor = mDbRecord.getProgramWorkoutRecords(mRunningProgramHistory.getId());
        } else {
            Program selectedProgram = (Program) mProgramsSpinner.getSelectedItem();
            if (selectedProgram !=null) {
                cursor = mDbRecord.getProgramTemplateRecords(((Program)mProgramsSpinner.getSelectedItem()).getId());
            }
        }

        List<Record> recordList = mDbRecord.fromCursorToList(cursor);

        DisplayType displayType;
        if (mIsProgramRunning) {
            displayType = DisplayType.PROGRAM_RUNNING_DISPLAY;
        } else {
            displayType = DisplayType.PROGRAM_PREVIEW_DISPLAY;
        }

        if (recordList.size()==0) {
            mProgramRecordsList.setAdapter(null);
        } else {
            if (mProgramRecordsList.getAdapter() == null) {
                RecordArrayAdapter mTableAdapter = new RecordArrayAdapter(getActivity(), getContext(), recordList, displayType, null);
                mTableAdapter.setOnProgramCompletedListener(onProgramCompletedListener);
                mProgramRecordsList.setAdapter(mTableAdapter);
            } else {
                RecordArrayAdapter mTableAdapter = (RecordArrayAdapter)mProgramRecordsList.getAdapter();
                if (mTableAdapter.getDisplayType()!=displayType) {
                    mTableAdapter = new RecordArrayAdapter(getActivity(), getContext(), recordList, displayType, null);
                    mTableAdapter.setOnProgramCompletedListener(onProgramCompletedListener);
                    mProgramRecordsList.setAdapter(mTableAdapter);
                } else {
                    mTableAdapter.setRecords(recordList);
                }
            }
        }
    }

    private Profile getProfile() {
        return profileViMo.getProfile().getValue();
    }

}
