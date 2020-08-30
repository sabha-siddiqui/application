package com.easyfitness.fonte;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.easyfitness.DAO.DATAMachine;
import com.easyfitness.DAO.record.DATARecord;
import com.easyfitness.DAO.DAOUtils;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.MainActivity;
import com.easyfitness.ProfileViMo;
import com.easyfitness.R;
import com.easyfitness.enums.DisplayType;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import androidx.lifecycle.ViewModelProvider;

public class FonteHistoryFragment extends Fragment {
    Spinner dateList = null;
    Spinner exerciseList = null;

    ListView filterList = null;

    MainActivity mActivity = null;
    private ProfileViMo profileViMo;

    List<String> mExerciseArray = null;
    List<String> mDateArray = null;

    ArrayAdapter<String> mAdapterMachine = null;
    ArrayAdapter<String> mAdapterDate = null;

    long machineIdArg = -1;
    long machineProfilIdArg = -1;

    Machine mSelectedMachine = null;
    private DATARecord mDbRecord = null;
    private OnItemLongClickListener itemlongclickDeleteRecord = (listView, view, position, id) -> {

        mDbRecord.deleteRecord(id);

        FillRecordTable(exerciseList.getSelectedItem().toString(), dateList
            .getSelectedItem().toString());

        KToast.infoToast(getActivity(), getResources().getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);

        return true;
    };
    private OnItemSelectedListener onItemSelectedList = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            if (parent.getId() == R.id.filterMachine) {
                DATAMachine lDbMachine = new DATAMachine(getContext());
                Machine machine = null;
                if (!exerciseList.getSelectedItem().toString().equals(getView().getResources().getText(R.string.all).toString())) {
                    mSelectedMachine = lDbMachine.getMachine(exerciseList.getSelectedItem().toString());
                } else {
                    mSelectedMachine = null;
                }
                refreshDates(mSelectedMachine);
                if (dateList.getCount() > 1) {
                    dateList.setSelection(1);
                } else {
                    dateList.setSelection(0);
                }
            }
            if (dateList.getCount() >= 1 && exerciseList.getCount() >= 1) {
                FillRecordTable(exerciseList.getSelectedItem().toString(), dateList
                    .getSelectedItem().toString());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    public static FonteHistoryFragment newInstance(long machineId, long machineProfile) {
        FonteHistoryFragment f = new FonteHistoryFragment();
        Bundle args = new Bundle();
        args.putLong("machineID", machineId);
        args.putLong("machineProfile", machineProfile);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_history, container, false);

        Bundle args = this.getArguments();
        machineIdArg = args.getLong("machineID");
        machineProfilIdArg = args.getLong("machineProfile");

        dateList = view.findViewById(R.id.filterDate);
        exerciseList = view.findViewById(R.id.filterMachine);
        filterList = view.findViewById(R.id.listFilterRecord);
        mDbRecord = new DATARecord(view.getContext());

        mExerciseArray = new ArrayList<>();
        mExerciseArray.add(getContext().getResources().getText(R.string.all).toString());
        mAdapterMachine = new ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_item, //simple_spinner_dropdown_item
            mExerciseArray);
        mAdapterMachine.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseList.setAdapter(mAdapterMachine);
        mDbRecord.closeCursor();

        if (machineIdArg != -1) {
            view.findViewById(R.id.tableRowFilterMachine).setVisibility(View.GONE);
            DATAMachine lDbMachine = new DATAMachine(getContext());
            mSelectedMachine = lDbMachine.getMachine(machineIdArg);
            mExerciseArray.add(mSelectedMachine.getName());
            mAdapterMachine.notifyDataSetChanged();
            exerciseList.setSelection(mAdapterMachine.getPosition(mSelectedMachine.getName()));
        } else {
            exerciseList.setOnItemSelectedListener(onItemSelectedList);
        }

        mDateArray = new ArrayList<>();
        mDateArray.add(getContext().getResources().getText(R.string.all).toString());
        mAdapterDate = new ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_item,
            mDateArray);
        mAdapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateList.setAdapter(mAdapterDate);
        filterList.setOnItemLongClickListener(itemlongclickDeleteRecord);
        dateList.setOnItemSelectedListener(onItemSelectedList);

        profileViMo = new ViewModelProvider(requireActivity()).get(ProfileViMo.class);
        profileViMo.getProfile().observe(getViewLifecycleOwner(), profile -> {
            refreshData();
            if (dateList.getCount() >= 1 && exerciseList.getCount() >= 1) {
                FillRecordTable(exerciseList.getSelectedItem().toString(), dateList
                    .getSelectedItem().toString());
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mActivity = (MainActivity) this.getActivity();
        refreshData();
    }

    public String getName() {
        return getArguments().getString("name");
    }

    private void FillRecordTable(String pMachine, String pDate) {
        Cursor oldCursor = null;
        if (!pDate.equals(getContext().getResources().getText(R.string.all).toString())) {
            Date date;
            try {
                DateFormat dateFormat3 = android.text.format.DateFormat.getDateFormat(getContext().getApplicationContext());
                dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"));
                date = dateFormat3.parse(pDate);
            } catch (ParseException e) {
                e.printStackTrace();
                date = new Date();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            pDate = dateFormat.format(date);
        }

        Cursor c = mDbRecord.getFilteredRecords(getProfile(), pMachine, pDate);

        List<Record> records = mDbRecord.fromCursorToList(c);

        if (records.size()==0) {
            filterList.setAdapter(null);
        } else {
            if (filterList.getAdapter() == null) {
                RecordArrayAdapter mTableAdapter = new RecordArrayAdapter(getActivity(), getContext(), records, DisplayType.HISTORY_DISPLAY, null);
                filterList.setAdapter(mTableAdapter);
            } else {
                ((RecordArrayAdapter) filterList.getAdapter()).setRecords(records);
            }
        }
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfile() != null) {
                if (machineIdArg == -1)
                {
                    mExerciseArray.clear();
                    mExerciseArray.add(getContext().getResources().getText(R.string.all).toString());
                    mExerciseArray.addAll(mDbRecord.getAllMachinesStrList(getProfile()));
                    mAdapterMachine.notifyDataSetChanged();
                    mDbRecord.closeCursor();

                    exerciseList.setSelection(0);
                }

                refreshDates(mSelectedMachine);
            }
        }
    }

    private void refreshDates(Machine m) {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfile() != null) {
                mDateArray.clear();
                mDateArray.add(getView().getResources().getText(R.string.all).toString());
                mDateArray.addAll(mDbRecord.getAllDatesList(getProfile(), m));
                if (mDateArray.size() > 1) {
                    dateList.setSelection(1);
                }
                mAdapterDate.notifyDataSetChanged();
                mDbRecord.closeCursor();
            }
        }
    }

    private Profile getProfile() {
        return profileViMo.getProfile().getValue();
    }
}
