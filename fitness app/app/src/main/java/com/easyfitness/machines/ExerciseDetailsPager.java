package com.easyfitness.machines;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.easyfitness.DAO.DATAMachine;
import com.easyfitness.DAO.DATAProfile;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.record.DATARecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.fonte.FonteHistoryFragment;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

public class ExerciseDetailsPager extends Fragment {
    Toolbar top_toolbar = null;
    long machineIdArg = 0;
    long machineProfilIdArg = 0;
    FragmentPagerItemAdapter pagerAdapter = null;
    ViewPager mViewPager = null;
    SmartTabLayout viewPagerTab = null;
    ImageButton deleteButton = null;
    ImageButton saveButton = null;
    MaterialFavoriteButton favoriteButton = null;
    Machine machine = null;
    boolean isFavorite = false;
    boolean toBeSaved = false;
    DATAMachine mDbMachine = null;
    DATARecord mDbRecord = null;
    private String name;
    private int id;
    private View.OnClickListener onClickToolbarItem = v -> {
        switch (v.getId()) {
            case R.id.deleteButton:
                deleteMachine();
                break;
            default:
                getActivity().onBackPressed();
        }
    };
    public static ExerciseDetailsPager newInstance(long machineId, long machineProfile) {
        ExerciseDetailsPager f = new ExerciseDetailsPager();
        Bundle args = new Bundle();
        args.putLong("machineID", machineId);
        args.putLong("machineProfile", machineProfile);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.exercise_pager, container, false);
        mViewPager = view.findViewById(R.id.pager);

        if (mViewPager.getAdapter() == null) {

            Bundle args = this.getArguments();
            machineIdArg = args.getLong("machineID");
            machineProfilIdArg = args.getLong("machineProfile");

            pagerAdapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
                .add(getString(R.string.MachineLabel), MachineDetailsFragment.class, args)
                .add(getString(R.string.HistoryLabel), FonteHistoryFragment.class, args)
                .create());

            mViewPager.setAdapter(pagerAdapter);

            viewPagerTab = view.findViewById(R.id.viewpagertab);
            viewPagerTab.setViewPager(mViewPager);

            viewPagerTab.setOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    Fragment frag1 = pagerAdapter.getPage(position);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

        mDbRecord = new DATARecord(getContext());
        mDbMachine = new DATAMachine(getContext());
        machine = mDbMachine.getMachine(machineIdArg);

        ((MainActivity) getActivity()).getActivityToolbar().setVisibility(View.GONE);
        top_toolbar = view.findViewById(R.id.actionToolbarMachine);
        top_toolbar.setNavigationIcon(R.drawable.ic_back);
        top_toolbar.setNavigationOnClickListener(onClickToolbarItem);

        deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(onClickToolbarItem);
        favoriteButton = view.findViewById(R.id.favButton);
        favoriteButton.setOnClickListener(v -> {
            MaterialFavoriteButton mFav = (MaterialFavoriteButton) v;
            boolean t = mFav.isFavorite();
            mFav.setFavoriteAnimated(!t);
            isFavorite = !t;
            saveMachine();
        });
        favoriteButton.setFavorite(machine.getFavorite());

        return view;
    }

    private void saveMachine() {
        final Machine newMachine = getExerciseFragment().getMachine();
        newMachine.setFavorite(favoriteButton.isFavorite());

        getExerciseFragment().requestForSave();
    }

    private void deleteMachine() {
        AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this.getActivity());

        deleteDialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
        deleteDialogBuilder.setMessage(getActivity().getResources().getText(R.string.deleteMachine_confirm_text));
        deleteDialogBuilder.setPositiveButton(this.getResources().getString(R.string.global_yes), (dialog, which) -> {
            mDbMachine.delete(machine);
            deleteRecordsAssociatedToMachine();
            getActivity().onBackPressed();
        });

        deleteDialogBuilder.setNegativeButton(this.getResources().getString(R.string.global_no), (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog deleteDialog = deleteDialogBuilder.create();
        deleteDialog.show();
    }

    private void deleteRecordsAssociatedToMachine() {
        DATARecord mDbRecord = new DATARecord(getContext());
        DATAProfile mDbProfil = new DATAProfile(getContext());

        Profile lProfile = mDbProfil.getProfil(this.machineProfilIdArg);

        List<Record> listRecords = mDbRecord.getAllRecordByMachineStrArray(lProfile, machine.getName());
        for (Record record : listRecords) {
            mDbRecord.deleteRecord(record.getId());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.machine_details_menu, menu);

        MenuItem item = menu.findItem(R.id.saveButton);
        item.setVisible(toBeSaved);

        super.onCreateOptionsMenu(menu, inflater);
    }

    public MachineDetailsFragment getExerciseFragment() {
        MachineDetailsFragment mpExerciseFrag;
        mpExerciseFrag = (MachineDetailsFragment) pagerAdapter.getPage(0);
        return mpExerciseFrag;
    }

    public FonteHistoryFragment getHistoricFragment() {
        FonteHistoryFragment mpHistoryFrag;
        mpHistoryFrag = (FonteHistoryFragment) pagerAdapter.getPage(1);
        return mpHistoryFrag;
    }

    public ViewPager getViewPager() {
        return (ViewPager) getView().findViewById(R.id.pager);
    }

    public FragmentPagerItemAdapter getViewPagerAdapter() {
        return (FragmentPagerItemAdapter) ((ViewPager) (getView().findViewById(R.id.pager))).getAdapter();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {

            if (getViewPagerAdapter() != null) {
                Fragment frag1;
                for (int i = 0; i < 3; i++) {
                    frag1 = getViewPagerAdapter().getPage(i);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false);
                }
            }
        }
    }
}
