package com.easyfitness.machines;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.easyfitness.DAO.DATAMachine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.enums.ExerciseType;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MachineFragment extends Fragment {
    ListView machineList = null;
    Button addButton = null;
    AutoCompleteTextView searchField = null;
    MachineCursorAdapter mTableAdapter;

    private DATAMachine mDbMachine = null;
    public TextWatcher onTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            if (charSequence.length() == 0) {
                refreshData();
            } else {
                if (mTableAdapter != null) {
                        mTableAdapter.getFilter().filter(charSequence);
                        mTableAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };
    private OnItemClickListener onClickListItem = (parent, view, position, id) -> {
        TextView textViewID = view.findViewById(R.id.LIST_MACHINE_ID);
        long machineId = Long.valueOf(textViewID.getText().toString());

        ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(machineId, ((MainActivity) getActivity()).getCurrentProfile().getId());
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
        transaction.addToBackStack(null);
        transaction.commit();
    };
    private View.OnClickListener clickAddButton = v -> {
        long new_id = -1;


        SweetAlertDialog dlg = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
            .setTitleText(getString(R.string.what_type_of_exercise))
            .setContentText("")
            .setCancelText(getResources().getText(R.string.CardioLabel).toString())
            .setConfirmText(getResources().getText(R.string.strength_category).toString())
            .setNeutralText(getResources().getText(R.string.staticExercise).toString())
            .showCancelButton(true)
            .setConfirmClickListener(sDialog -> {
                long temp_machine_key = -1;
                String pMachine = "";
                DATAMachine lDAOMachine = new DATAMachine(getContext());
                temp_machine_key = lDAOMachine.addMachine(pMachine, "", ExerciseType.STRENGTH, "", false,"");
                sDialog.dismissWithAnimation();

                ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(temp_machine_key, ((MainActivity) getActivity()).getCurrentProfile().getId());
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
                transaction.addToBackStack(null);
                transaction.commit();
            })
            .setNeutralClickListener(sDialog -> {
                long temp_machine_key = -1;
                String pMachine = "";
                DATAMachine lDAOMachine = new DATAMachine(getContext());
                temp_machine_key = lDAOMachine.addMachine(pMachine, "", ExerciseType.ISOMETRIC, "", false, "");
                sDialog.dismissWithAnimation();

                ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(temp_machine_key, ((MainActivity) getActivity()).getCurrentProfile().getId());
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
                transaction.addToBackStack(null);
                transaction.commit();
            })
            .setCancelClickListener(sDialog -> {
                long temp_machine_key = -1;
                String pMachine = "";
                DATAMachine lDAOMachine = new DATAMachine(getContext());
                temp_machine_key = lDAOMachine.addMachine(pMachine, "", ExerciseType.CARDIO, "", false, "");
                sDialog.dismissWithAnimation();

                ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(temp_machine_key, ((MainActivity) getActivity()).getCurrentProfile().getId());
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
                transaction.addToBackStack(null);
                transaction.commit();
            });

        dlg.show();

        dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setBackgroundResource(R.color.record_background_odd);
        dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setPadding(0, 0, 0, 0);
        dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP);
        }
        dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setBackgroundResource(R.color.record_background_odd);
        dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setPadding(0, 0, 0, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP);
        }

        dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setBackgroundResource(R.color.record_background_odd);
        dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setPadding(0, 0, 0, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP);
        }
    };

    public static MachineFragment newInstance(String name, int id) {
        MachineFragment f = new MachineFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.tab_machine, container, false);

        addButton = view.findViewById(R.id.addExercise);
        addButton.setOnClickListener(clickAddButton);

        searchField = view.findViewById(R.id.searchField);
        searchField.addTextChangedListener(onTextChangeListener);

        machineList = view.findViewById(R.id.listMachine);
        machineList.setOnItemClickListener(onClickListItem);
        mDbMachine = new DATAMachine(getContext());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mDbMachine.deleteAllEmptyExercises();
        refreshData();
        searchField.setText("");

    }

    public String getName() {
        return getArguments().getString("name");
    }

    public MachineFragment getThis() {
        return this;
    }

    private void refreshData() {
        Cursor c = null;
        Cursor oldCursor = null;

        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfil() != null) {
                c = mDbMachine.getAllMachines();
                if (c == null || c.getCount() == 0) {
                    machineList.setAdapter(null);
                } else {
                    if (machineList.getAdapter() == null) {
                        mTableAdapter = new MachineCursorAdapter(getActivity(), c, 0, mDbMachine);
                        machineList.setAdapter(mTableAdapter);
                    } else {
                        mTableAdapter = ((MachineCursorAdapter) machineList.getAdapter());
                        oldCursor = mTableAdapter.swapCursor(c);
                        if (oldCursor != null) oldCursor.close();
                    }

                    mTableAdapter.setFilterQueryProvider(constraint -> mDbMachine.getFilteredMachines(constraint));
                }
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) refreshData();
    }

    private Profile getProfil() {
        return ((MainActivity) getActivity()).getCurrentProfile();
    }

}
