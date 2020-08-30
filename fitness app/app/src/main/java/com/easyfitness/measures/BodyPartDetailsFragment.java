package com.easyfitness.measures;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.easyfitness.BtnClickListener;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DATABodyMeasure;
import com.easyfitness.DAO.bodymeasures.DATABodyPart;
import com.easyfitness.MainActivity;
import com.easyfitness.ProfileViMo;
import com.easyfitness.R;
import com.easyfitness.SettingsFragment;
import com.easyfitness.ValueEditorDialogbox;
import com.easyfitness.enums.Unit;
import com.easyfitness.enums.UnitType;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.UnitConverter;
import com.easyfitness.views.EditableInputView;
import com.easyfitness.utils.ExpandedListView;
import com.easyfitness.views.GraphView;
import com.github.mikephil.charting.data.Entry;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BodyPartDetailsFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private TextView addButton = null;
    private EditableInputView nameEdit = null;
    private ExpandedListView measureList = null;
    private Toolbar bodyToolbar = null;
    private GraphView mDateGraph = null;
    private DATABodyMeasure mBodyMeasureDb = null;
    private DATABodyPart mDbBodyPart;
    private BodyPart mInitialBodyPart;
    private String mCurrentPhotoPath = null;

    private BtnClickListener itemClickDeleteRecord = view -> {
        switch (view.getId()) {
            case R.id.deleteButton:
                showDeleteDialog((long)view.getTag());
                break;
            case R.id.editButton:
                showEditDialog((long)view.getTag());
                break;

        }
    };
    private OnClickListener onClickAddMeasure = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ValueEditorDialogbox editorDialogbox;
            BodyMeasure lastBodyMeasure = mBodyMeasureDb.getLastBodyMeasures(mInitialBodyPart.getId(), getProfile());
            double lastValue;
            if (lastBodyMeasure == null) {
                lastValue=0;
            } else {
                lastValue=lastBodyMeasure.getBodyMeasure();
            }
            Unit unitDef = getValidUnit(lastBodyMeasure);

            editorDialogbox = new ValueEditorDialogbox(getActivity(), new Date(), "", lastValue, unitDef);
            editorDialogbox.setTitle(R.string.AddLabel);
            editorDialogbox.setPositiveButton(R.string.AddLabel);
            editorDialogbox.setOnDismissListener(dialog -> {
                if (!editorDialogbox.isCancelled()) {
                    Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                    float value = Float.parseFloat(editorDialogbox.getValue().replaceAll(",", "."));
                    Unit unit = Unit.fromString(editorDialogbox.getUnit());
                    mBodyMeasureDb.addBodyMeasure(date, mInitialBodyPart.getId(), value, getProfile().getId(), unit);
                    refreshData();
                }
            });
            editorDialogbox.show();
        }
    };
    private ProfileViMo profileViMo;

    private OnItemLongClickListener itemlongclickDeleteRecord = (listView, view, position, id) -> {


        final long selectedID = id;

        String[] profilListArray = new String[1];
        profilListArray[0] = getActivity().getResources().getString(R.string.DeleteLabel);

        AlertDialog.Builder itemActionBuilder = new AlertDialog.Builder(getActivity());
        itemActionBuilder.setTitle("").setItems(profilListArray, (dialog, which) -> {

            switch (which) {
                case 0:
                    mBodyMeasureDb.deleteMeasure(selectedID);
                    refreshData();
                    KToast.infoToast(getActivity(), getActivity().getResources().getText(R.string.removedid).toString() + " " + selectedID, Gravity.BOTTOM, KToast.LENGTH_SHORT);
                    break;
                default:
            }
        });
        itemActionBuilder.show();

        return true;
    };

    private ImageButton deleteButton;
    private EditableInputView.OnTextChangedListener onTextChangeListener = this::requestForSave;
    private TextView editDate;
    private EditText editText;
    private ImageView bodyPartImageView;

    public static BodyPartDetailsFragment newInstance(long bodyPartID, boolean showInput) {
        BodyPartDetailsFragment f = new BodyPartDetailsFragment();
        Bundle args = new Bundle();
        args.putLong("bodyPartID", bodyPartID);
        args.putBoolean("showInput", showInput);
        f.setArguments(args);

        return f;
    }

    private View.OnClickListener onClickToolbarItem = v -> {
        switch (v.getId()) {
            case R.id.deleteButton:
                delete();
                break;
        }
    };

    private void delete() {
        AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this.getActivity());

        deleteDialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
        deleteDialogBuilder.setMessage(getActivity().getResources().getText(R.string.delete_bodypart_confirm));

        deleteDialogBuilder.setPositiveButton(this.getResources().getString(R.string.global_yes), (dialog, which) -> {
            mDbBodyPart.delete(mInitialBodyPart.getId());
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
        DATABodyMeasure mDbBodyMeasure = new DATABodyMeasure(getContext());

        Profile lProfile = getProfile();

        List<BodyMeasure> listBodyMeasure = mDbBodyMeasure.getBodyPartMeasuresList(mInitialBodyPart.getId(), lProfile);
        for (BodyMeasure record : listBodyMeasure) {
            mDbBodyMeasure.deleteMeasure(record.getId());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bodytracking_details_fragment, container, false);

        mDbBodyPart = new DATABodyPart(getContext());

        addButton = view.findViewById(R.id.buttonAdd);
        nameEdit = view.findViewById(R.id.BODYPART_NAME);
        measureList = view.findViewById(R.id.listWeightProfil);
        bodyToolbar = view.findViewById(R.id.bodyTrackingDetailsToolbar);
        bodyPartImageView = view.findViewById(R.id.BODYPART_LOGO);
        CardView nameCardView = view.findViewById(R.id.nameCardView);
        mDateGraph = view.findViewById(R.id.bodymeasureChart);

        long bodyPartID = getArguments().getLong("bodyPartID", 0);
        mInitialBodyPart = mDbBodyPart.getBodyPart(bodyPartID);


        if (mInitialBodyPart.getBodyPartResKey()!=-1) {
            bodyPartImageView.setVisibility(View.VISIBLE);
            bodyPartImageView.setImageDrawable(mInitialBodyPart.getPicture(getContext()));
        } else {
            bodyPartImageView.setImageDrawable(null);
            bodyPartImageView.setVisibility(View.GONE);
        }
        if (mInitialBodyPart.getType()==BodyPartExtensions.TYPE_WEIGHT) {
            nameEdit.ActivateDialog(false);
        }
        nameEdit.setOnTextChangeListener(onTextChangeListener);
        addButton.setOnClickListener(onClickAddMeasure);
        measureList.setOnItemLongClickListener(itemlongclickDeleteRecord);
        mBodyMeasureDb = new DATABodyMeasure(view.getContext());

        ((MainActivity) getActivity()).getActivityToolbar().setVisibility(View.GONE);

        nameEdit.setText(mInitialBodyPart.getName(getContext()));
        bodyToolbar.setNavigationIcon(R.drawable.ic_back);
        bodyToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());

        deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(onClickToolbarItem);
        if(mInitialBodyPart.getType()== BodyPartExtensions.TYPE_WEIGHT) {
            deleteButton.setVisibility(View.GONE);
        }

        profileViMo = new ViewModelProvider(requireActivity()).get(ProfileViMo.class);
        profileViMo.getProfile().observe(getViewLifecycleOwner(), profile -> {
            refreshData();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshData();
    }

    private void DrawGraph(List<BodyMeasure> valueList) {
        if (valueList.size() < 1) {
            mDateGraph.clear();
            return;
        }

        ArrayList<Entry> yVals = new ArrayList<>();

        float minBodyMeasure = -1;

        for (int i = valueList.size() - 1; i >= 0; i--) {
            float normalizedMeasure;
            switch (valueList.get(i).getUnit().getUnitType()){
                case WEIGHT:
                    normalizedMeasure =UnitConverter.weightConverter(valueList.get(i).getBodyMeasure(),valueList.get(i).getUnit(),SettingsFragment.getDefaultWeightUnit(getActivity()).toUnit());
                    break;
                case SIZE:
                    normalizedMeasure =UnitConverter.sizeConverter(valueList.get(i).getBodyMeasure(),valueList.get(i).getUnit(),SettingsFragment.getDefaultSizeUnit(getActivity()));
                    break;
                default:
                    normalizedMeasure=valueList.get(i).getBodyMeasure();
            }

            Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate().getTime()), normalizedMeasure);
            yVals.add(value);
            if (minBodyMeasure == -1) minBodyMeasure = valueList.get(i).getBodyMeasure();
            else if (valueList.get(i).getBodyMeasure() < minBodyMeasure)
                minBodyMeasure = valueList.get(i).getBodyMeasure();
        }

        mDateGraph.draw(yVals);
    }
    private void FillRecordTable(List<BodyMeasure> valueList) {
        Cursor oldCursor = null;

        if (valueList.isEmpty()) {
            measureList.setAdapter(null);
        } else {
            if (measureList.getAdapter() == null) {
                BodyMeasureCursorAdapter mTableAdapter = new BodyMeasureCursorAdapter(getActivity(), mBodyMeasureDb.getCursor(), 0, itemClickDeleteRecord);
                measureList.setAdapter(mTableAdapter);
            } else {
                oldCursor = ((BodyMeasureCursorAdapter) measureList.getAdapter()).swapCursor(mBodyMeasureDb.getCursor());
                if (oldCursor != null)
                    oldCursor.close();
            }
        }
    }

    public String getName() {
        return getArguments().getString("name");
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfile() != null) {
                List<BodyMeasure> valueList = mBodyMeasureDb.getBodyPartMeasuresList(mInitialBodyPart.getId(), getProfile());
                DrawGraph(valueList);
                FillRecordTable(valueList);
            }
        }
    }

    private void showDeleteDialog(final long idToDelete) {

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    mBodyMeasureDb.deleteMeasure(idToDelete);
                    refreshData();
                    Toast.makeText(getActivity(), getResources().getText(R.string.removedid) + " " + idToDelete, Toast.LENGTH_SHORT)
                        .show();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getText(R.string.DeleteRecordDialog)).setPositiveButton(getResources().getText(R.string.global_yes), dialogClickListener)
            .setNegativeButton(getResources().getText(R.string.global_no), dialogClickListener).show();
    }

    private void showEditDialog(final long idToEdit) {
        BodyMeasure bodyMeasure = mBodyMeasureDb.getMeasure(idToEdit);

        ValueEditorDialogbox editorDialogbox = new ValueEditorDialogbox(getActivity(), bodyMeasure.getDate(), "", bodyMeasure.getBodyMeasure(), getValidUnit(bodyMeasure));
        editorDialogbox.setOnDismissListener(dialog -> {
            if (!editorDialogbox.isCancelled()) {
                Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                float value = Float.parseFloat(editorDialogbox.getValue().replaceAll(",", "."));
                Unit unit = Unit.fromString(editorDialogbox.getUnit());

                BodyMeasure updatedBodyMeasure = new BodyMeasure(bodyMeasure.getId(), date, bodyMeasure.getBodyPartID(), value, bodyMeasure.getProfileID(), unit);
                int i = mBodyMeasureDb.updateMeasure(updatedBodyMeasure);
                refreshData();
            }
        });
        editorDialogbox.setOnCancelListener(null);

        editorDialogbox.show();
    }


    private Profile getProfile() {
        return profileViMo.getProfile().getValue();
    }

    public Fragment getFragment() {
        return this;
    }

    private void requestForSave(View view) {
        boolean toUpdate = false;
        switch (view.getId()) {
            case R.id.BODYPART_NAME:
                mInitialBodyPart.setCustomName(nameEdit.getText());
                toUpdate = true;
                break;
            case R.id.BODYPART_LOGO:
                // TODO if it has been deleted, remove the CustomPicture
                mInitialBodyPart.setCustomPicture(mCurrentPhotoPath);
                toUpdate = true;
                break;
        }

        if (toUpdate) {
            mDbBodyPart.update(mInitialBodyPart);
            KToast.infoToast(getActivity(), mInitialBodyPart.getCustomName() + " updated", Gravity.BOTTOM, KToast.LENGTH_SHORT);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Date date = DateConverter.dateToDate(year, month, dayOfMonth);
        if (editDate != null)
            editDate.setText(DateConverter.dateToLocalDateStr(date, getContext()));
    }

    private Unit getValidUnit(BodyMeasure lastBodyMeasure) {
        UnitType unitType = BodyPartExtensions.getUnitType(mInitialBodyPart.getBodyPartResKey());
        if (lastBodyMeasure != null) {
            if (unitType!=lastBodyMeasure.getUnit().getUnitType())
            {
                lastBodyMeasure=null;
            }
        }

        Unit unitDef=Unit.UNITLESS;
        if (lastBodyMeasure == null) {
            switch (unitType) {
                case WEIGHT:
                    unitDef = SettingsFragment.getDefaultWeightUnit(getActivity()).toUnit();
                    break;
                case SIZE:
                    unitDef = SettingsFragment.getDefaultSizeUnit(getActivity());
                    break;
                case PERCENTAGE:
                    unitDef = Unit.PERCENTAGE;
                    break;
            }
        }
        else {
            unitDef=lastBodyMeasure.getUnit();
        }
        return unitDef;
    }
}
