package com.easyfitness.measures;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DATABodyMeasure;
import com.easyfitness.DAO.bodymeasures.DATABodyPart;
import com.easyfitness.MainActivity;
import com.easyfitness.ProfileViMo;
import com.easyfitness.R;
import com.easyfitness.utils.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class BodyPartListFragment extends Fragment {
    ArrayList<BodyPart> dataModels;
    ListView measureList = null;

    private View.OnClickListener clickAddButton = v -> {
        final EditText editText = new EditText(getContext());
        editText.setText("");
        editText.setGravity(Gravity.CENTER);
        editText.requestFocus();

        LinearLayout linearLayout = new LinearLayout(getContext().getApplicationContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editText);

        final SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
            .setTitleText(getContext().getString(R.string.enter_bodypart_name))
            .setCancelText(getContext().getString(R.string.global_cancel))
            .setHideKeyBoardOnDismiss(true)
            .setCancelClickListener(sDialog -> {
                editText.clearFocus();
                Keyboard.hide(getContext(), editText);
                sDialog.dismissWithAnimation();})
            .setConfirmClickListener(sDialog -> {

                editText.clearFocus();
                Keyboard.hide(getContext(), editText);
                DATABodyPart daoBodyPart = new DATABodyPart(getContext());
                long temp_key = daoBodyPart.add(-1, editText.getText().toString(), "", daoBodyPart.getCount(), BodyPartExtensions.TYPE_MUSCLE);

                sDialog.dismiss();
                BodyPartDetailsFragment bodyPartDetailsFragment = BodyPartDetailsFragment.newInstance(temp_key, true);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, bodyPartDetailsFragment, MainActivity.BODYTRACKINGDETAILS);
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

    private OnItemClickListener onClickListItem = (parent, view, position, id) -> {

        TextView textView = view.findViewById(R.id.LIST_BODYPART_ID);
        long bodyPartID = Long.parseLong(textView.getText().toString());

        BodyPartDetailsFragment fragment = BodyPartDetailsFragment.newInstance(bodyPartID, true);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, MainActivity.BODYTRACKINGDETAILS);
        transaction.addToBackStack(null);
        transaction.commit();
    };
    private DATABodyPart mdbBodyPart;
    private DATABodyMeasure mdbMeasure;
    private BodyPartListAdapter mListAdapter;
    private ProfileViMo profileViMo;
    public static BodyPartListFragment newInstance(String name, int id) {
        BodyPartListFragment f = new BodyPartListFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_bodytrack, container, false);

        Button addButton = view.findViewById(R.id.addBodyPart);
        addButton.setOnClickListener(clickAddButton);

        measureList = view.findViewById(R.id.listBodyMeasures);
        measureList.setOnItemClickListener(onClickListItem);

        mdbMeasure = new DATABodyMeasure(this.getContext());
        mdbBodyPart = new DATABodyPart(this.getContext());
        dataModels = new ArrayList<>();

        profileViMo = new ViewModelProvider(requireActivity()).get(ProfileViMo.class);
        profileViMo.getProfile().observe(getViewLifecycleOwner(), profile -> {
            mListAdapter.setProfile(profile);
            refreshData();
        });

        mListAdapter = new BodyPartListAdapter(dataModels, getContext());
        mListAdapter.setProfile(getProfile());
        measureList.setAdapter(mListAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mdbBodyPart.deleteAllEmptyBodyPart();
        refreshData();
    }

    private void refreshData() {
        if (dataModels==null) {
            dataModels = new ArrayList<>();
        }

        dataModels.clear();

        List<BodyPart> lBodyPartList = mdbBodyPart.getMusclesList();
        for (BodyPart bp: lBodyPartList) {
            BodyMeasure bm = null;
            if (getProfile()!=null)
                bm = mdbMeasure.getLastBodyMeasures(bp.getId(), getProfile());

            bp.setLastMeasure(bm);

            dataModels.add(bp);
        }

        if (mListAdapter==null) {
            mListAdapter = new BodyPartListAdapter(dataModels, getContext());
            mListAdapter.setProfile(getProfile());
            measureList.setAdapter(mListAdapter);
        }
        else {
            mListAdapter.notifyDataSetChanged();
        }
    }

    private Profile getProfile() {
        return profileViMo.getProfile().getValue();
    }

}
