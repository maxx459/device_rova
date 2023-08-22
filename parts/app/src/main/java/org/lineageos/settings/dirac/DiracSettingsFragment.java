/*
 * Copyright (C) 2018 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.dirac;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Switch;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.android.settingslib.widget.MainSwitchPreference;
import com.android.settingslib.widget.OnMainSwitchChangeListener;

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

import org.lineageos.settings.R;

@AndroidEntryPoint(PreferenceFragmentCompat.class)
public class DiracSettingsFragment extends Hilt_DiracSettingsFragment implements
        Preference.OnPreferenceChangeListener, OnMainSwitchChangeListener {

    private static final String PREF_ENABLE = "dirac_enable";
    private static final String PREF_HEADSET = "dirac_headset_pref";
    private static final String PREF_PRESET = "dirac_preset_pref";

    private MainSwitchPreference mSwitchBar;

    private ListPreference mHeadsetType;
    private ListPreference mPreset;

    private Handler mHandler = new Handler(Looper.myLooper());

    @Inject
    DiracUtils mDiracUtils;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.dirac_settings);

        boolean enhancerEnabled = mDiracUtils.isDiracEnabled();

        mSwitchBar = (MainSwitchPreference) findPreference(PREF_ENABLE);
        mSwitchBar.addOnSwitchChangeListener(this);
        mSwitchBar.setChecked(enhancerEnabled);

        mHeadsetType = (ListPreference) findPreference(PREF_HEADSET);
        mHeadsetType.setOnPreferenceChangeListener(this);
        mHeadsetType.setEnabled(enhancerEnabled);

        mPreset = (ListPreference) findPreference(PREF_PRESET);
        mPreset.setOnPreferenceChangeListener(this);
        mPreset.setEnabled(enhancerEnabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!mDiracUtils.isDiracEnabled()) {
            getActivity().recreate();
            return false;
        }

        try {
            switch (preference.getKey()) {
                case PREF_HEADSET:
                    mDiracUtils.setHeadsetType(Integer.parseInt(newValue.toString()));
                    DiracTileService.sync(getActivity());
                    return true;
                case PREF_PRESET:
                    mDiracUtils.setLevel(String.valueOf(newValue));
                    DiracTileService.sync(getActivity());
                    return true;
                default:
                    return false;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            getActivity().recreate();
            return false;
        }
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        if (isChecked == mDiracUtils.isDiracEnabled()) {
            getActivity().recreate();
            return;
        }

        try {
            mDiracUtils.setEnabled(isChecked);
            DiracTileService.sync(getActivity());
        } catch (RuntimeException e) {
            e.printStackTrace();
            getActivity().recreate();
            return;
        }

        if (isChecked) {
            mSwitchBar.setEnabled(false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mSwitchBar.setEnabled(true);
                        setEnabled(isChecked);
                    } catch(Exception ignored) {
                    }
                }
            }, 1020);
        } else {
            setEnabled(isChecked);
        }
    }

    private void setEnabled(boolean enabled){
        mSwitchBar.setChecked(enabled);
        mHeadsetType.setEnabled(enabled);
        mPreset.setEnabled(enabled);
        DiracTileService.sync(getActivity());
    }
}