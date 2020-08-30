package com.easyfitness.programs;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.easyfitness.DAO.program.DATAProgramHistory;
import com.easyfitness.DAO.program.ProgramHistory;
import com.easyfitness.DAO.record.DATARecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.DAO.program.DATAProgram;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.enums.DisplayType;
import com.easyfitness.fonte.FontesFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

public class ProgramPagerFragment extends Fragment {
    FragmentPagerItemAdapter pagerAdapter = null;
    ViewPager mViewPager = null;

    private long mTemplateId;

    public static ProgramPagerFragment newInstance(long templateId) {
        ProgramPagerFragment f = new ProgramPagerFragment();

        Bundle args = new Bundle();
        args.putInt("displayType", DisplayType.PROGRAM_EDIT_DISPLAY.ordinal());
        args.putLong("templateId", templateId);
        f.setArguments(args);

        return f;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.program_pager, container, false);
        mViewPager = view.findViewById(R.id.program_pager);

        if (mViewPager.getAdapter() == null) {

            Bundle args = this.getArguments();

            mTemplateId = args.getLong("templateId");

            pagerAdapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
                .add("Info", ProgramInfoFragment.class, args)
                .add("Editor", FontesFragment.class, args)
                .create());

            mViewPager.setAdapter(pagerAdapter);

            SmartTabLayout viewPagerTab = view.findViewById(R.id.programPagerTab);
            viewPagerTab.setViewPager(mViewPager);

            viewPagerTab.setOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    Fragment frag = pagerAdapter.getPage(position);
                    if (frag != null)
                        frag.onHiddenChanged(false);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

        Toolbar top_toolbar;
        ((MainActivity) getActivity()).getActivityToolbar().setVisibility(View.GONE);
        top_toolbar = view.findViewById(R.id.toolbar);
        top_toolbar.setNavigationIcon(R.drawable.ic_back);
        top_toolbar.setNavigationOnClickListener(onClickToolbarItem);

        ImageButton deleteButton;
        deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(onClickToolbarItem);
        return view;
    }

    private View.OnClickListener onClickToolbarItem = v -> {
        switch (v.getId()) {
            case R.id.deleteButton:
                deleteProgram();
                break;
            default:
                getActivity().onBackPressed();
        }
    };

    private void deleteProgram() {
        AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this.getActivity());

        deleteDialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
        deleteDialogBuilder.setMessage("Do you want to delete this program?");

        deleteDialogBuilder.setPositiveButton(this.getResources().getString(R.string.global_yes), (dialog, which) -> {
            DATAProgram dB = new DATAProgram(getContext());
            DATAProgramHistory dbProgramHistory = new DATAProgramHistory(getContext());
            dB.delete(mTemplateId);
            deleteRecordsAssociatedToTemplate();
            List<ProgramHistory> lProgramHistories = dbProgramHistory.getAll();
            for (ProgramHistory history : lProgramHistories)
            {
                if (history.getProgramId() == mTemplateId)
                {
                    dbProgramHistory.delete(history);
                }
            }

            getActivity().onBackPressed();
        });

        deleteDialogBuilder.setNegativeButton(this.getResources().getString(R.string.global_no), (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog deleteDialog = deleteDialogBuilder.create();
        deleteDialog.show();
    }

    private void deleteRecordsAssociatedToTemplate() {
        DATARecord mDbRecord = new DATARecord(getContext());
        List<Record> listRecords = mDbRecord.getAllTemplateRecordByProgramArray(mTemplateId);
        for (Record record : listRecords) {
            mDbRecord.deleteRecord(record.getId());
        }
    }

    public FragmentPagerItemAdapter getViewPagerAdapter() {
        return (FragmentPagerItemAdapter) ((ViewPager) (getView().findViewById(R.id.program_pager))).getAdapter();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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
