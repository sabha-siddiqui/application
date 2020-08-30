package com.easyfitness;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.Unit;
import com.easyfitness.enums.WeightUnit;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat {

    Toolbar top_toolbar = null;
    MainActivity mActivity = null;

    public final static String WEIGHT_UNIT_PARAM =  "defaultUnit";
    public final static String DISTANCE_UNIT_PARAM =  "defaultDistanceUnit";
    public final static String SIZE_UNIT_PARAM =  "defaultSizeUnit";
    public static SettingsFragment newInstance(String name, int id) {
        SettingsFragment f = new SettingsFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (MainActivity) getActivity();


        Preference myPref = findPreference("prefShowMP3");
        myPref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof Boolean) {
                Boolean boolVal = (Boolean) newValue;
                mActivity.showMP3Toolbar(boolVal);
            }

            return true;
        });

        Preference myPref2 = findPreference(WEIGHT_UNIT_PARAM);
        myPref2.setOnPreferenceChangeListener((preference, newValue) -> {
            ListPreference listPreference = (ListPreference) preference;
            if (newValue instanceof String) {
                updateSummary(listPreference, (String) newValue, getString(R.string.pref_preferredUnitSummary));
            }

            return true;
        });

        Preference myPref3 = findPreference(DISTANCE_UNIT_PARAM);
        myPref3.setOnPreferenceChangeListener((preference, newValue) -> {
            ListPreference listPreference = (ListPreference) preference;
            if (newValue instanceof String) {
                updateSummary(listPreference, (String) newValue, getString(R.string.pref_preferredUnitSummary));
            }

            return true;
        });

        Preference myPref4 = findPreference(SIZE_UNIT_PARAM);
        myPref4.setOnPreferenceChangeListener((preference, newValue) -> {
            ListPreference listPreference = (ListPreference) preference;
            if (newValue instanceof String) {
                updateSummary(listPreference, (String) newValue, getString(R.string.pref_preferredUnitSummary));
            }

            return true;
        });

        Preference dayNightModePref = findPreference("dayNightAuto");
        dayNightModePref.setOnPreferenceChangeListener((preference, newValue) -> {
            ListPreference listPreference = (ListPreference) preference;
            if (newValue instanceof String) {
                updateSummary(listPreference, (String) newValue, "");
            }

            return true;
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String param) {
        setPreferencesFromResource(R.xml.settings2, param);

        SharedPreferences sharedPreferences;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ListPreference myPref2 = (ListPreference) findPreference(SettingsFragment.WEIGHT_UNIT_PARAM);
        String boolVal = sharedPreferences.getString(SettingsFragment.WEIGHT_UNIT_PARAM, String.valueOf(WeightUnit.KG));
        updateSummary(myPref2, boolVal, getString(R.string.pref_preferredUnitSummary));

        ListPreference myPref3 = (ListPreference) findPreference(SettingsFragment.DISTANCE_UNIT_PARAM);
        String boolVal3 = sharedPreferences.getString(SettingsFragment.DISTANCE_UNIT_PARAM, String.valueOf(DistanceUnit.KM));
        updateSummary(myPref3, boolVal3, getString(R.string.pref_preferredUnitSummary));

        ListPreference myPref4 = (ListPreference) findPreference(SettingsFragment.SIZE_UNIT_PARAM);
        String boolVal4 = sharedPreferences.getString(SettingsFragment.SIZE_UNIT_PARAM, String.valueOf(Unit.CM));
        updateSummary(myPref4, boolVal4, getString(R.string.pref_preferredUnitSummary));

        ListPreference dayNightModePref = (ListPreference) findPreference("dayNightAuto");
        String dayNightValue = sharedPreferences.getString("dayNightAuto", "2");
        updateSummary(dayNightModePref, dayNightValue, "");
    }

    private void updateSummary(ListPreference pref, String val, String prefix) {
        int prefIndex = pref.findIndexOfValue(val);
        if (prefIndex >= 0) {
            pref.setSummary(prefix + pref.getEntries()[prefIndex]);
        }
    }

    public static WeightUnit getDefaultWeightUnit(Activity activity) {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(activity);
        WeightUnit weightUnit = WeightUnit.KG;
        try {
            weightUnit = WeightUnit.fromInteger(Integer.parseInt(SP.getString(SettingsFragment.WEIGHT_UNIT_PARAM, String.valueOf(WeightUnit.KG))));
        } catch (NumberFormatException e) {
            weightUnit = WeightUnit.KG;
        }
        return weightUnit;
    }

    public static DistanceUnit getDefaultDistanceUnit(Activity activity) {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(activity);
        DistanceUnit distanceUnit = DistanceUnit.KM;
        try {
            distanceUnit = DistanceUnit.fromInteger(Integer.parseInt(SP.getString(SettingsFragment.DISTANCE_UNIT_PARAM, String.valueOf(DistanceUnit.KM))));
        } catch (NumberFormatException e) {
            distanceUnit = DistanceUnit.KM;
        }
        return distanceUnit;
    }

    public static Unit getDefaultSizeUnit(Activity activity) {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(activity);
        Unit unit = Unit.CM;
        try {
            unit = Unit.fromInteger(Integer.parseInt(SP.getString(SettingsFragment.SIZE_UNIT_PARAM, String.valueOf(Unit.CM))));
        } catch (NumberFormatException e) {
            unit = Unit.CM;
        }
        return unit;
    }
}
