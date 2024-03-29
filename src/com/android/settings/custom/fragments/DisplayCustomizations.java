/*
 * Copyright (C) 2021 Wave-OS
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

package com.android.settings.custom.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.custom.CustomUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.blaster.support.preference.OverlaySwitchPreference;
import com.blaster.support.preference.SecureSettingMasterSwitchPreference;
import com.blaster.support.preference.SystemSettingMasterSwitchPreference;
import com.blaster.support.preference.SystemSettingSwitchPreference;

public class DisplayCustomizations extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "Display Customizations";
    private static final String BRIGHTNESS_SLIDER = "qs_show_brightness";
    private static final String KEY_BATTERY_CHARGING_LIGHT = "battery_charging_light";
    private static final String KEY_NETWORK_TRAFFIC = "network_traffic_state";
    private static final String KEY_OLD_MOBILETYPE = "use_old_mobiletype";
    private static final String KEY_COMBINED_ICONS = "com.android.systemui.flags.no_combined_icons";

    private SecureSettingMasterSwitchPreference mBrightnessSlider;
    private SystemSettingMasterSwitchPreference mNetworkTraffic;
    private SystemSettingSwitchPreference mOldMobileType;
    private OverlaySwitchPreference mCombinedIcons;
    Preference mBatteryLightPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.display_customizations);
        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        mBrightnessSlider = (SecureSettingMasterSwitchPreference)
                findPreference(BRIGHTNESS_SLIDER);
        mBrightnessSlider.setOnPreferenceChangeListener(this);
        boolean enabled = Settings.Secure.getInt(resolver,
                BRIGHTNESS_SLIDER, 1) == 1;
        mBrightnessSlider.setChecked(enabled);

        mBatteryLightPref = (Preference) findPreference(KEY_BATTERY_CHARGING_LIGHT);
        if (!getResources()
                .getBoolean(com.android.internal.R.bool.config_intrusiveBatteryLed))
        {
			if (mBatteryLightPref != null) {
				prefSet.removePreference(mBatteryLightPref);
			}
        }

        mNetworkTraffic = (SystemSettingMasterSwitchPreference)
                findPreference(KEY_NETWORK_TRAFFIC);
        enabled = Settings.System.getIntForUser(resolver,
                KEY_NETWORK_TRAFFIC, 0, UserHandle.USER_CURRENT) == 1;
        mNetworkTraffic.setChecked(enabled);
        mNetworkTraffic.setOnPreferenceChangeListener(this);

        mOldMobileType = (SystemSettingSwitchPreference)
                findPreference(KEY_OLD_MOBILETYPE);
        mOldMobileType.setOnPreferenceChangeListener(this);

        mCombinedIcons = (OverlaySwitchPreference)
                findPreference(KEY_COMBINED_ICONS);
        mCombinedIcons.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBrightnessSlider) {
            boolean value = (boolean) newValue;
            Settings.Secure.putInt(resolver,
                    BRIGHTNESS_SLIDER, value ? 1 : 0);
            return true;
        } else if (preference == mNetworkTraffic) {
            boolean value = (Boolean) newValue;
            Settings.System.putIntForUser(resolver, KEY_NETWORK_TRAFFIC,
                    value ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mOldMobileType) {
            CustomUtils.showSystemUiRestartDialog(getActivity());
            return true;
        } else if (preference == mCombinedIcons) {
            CustomUtils.showSystemUiRestartDialog(getActivity());
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
       return MetricsProto.MetricsEvent.CUSTOM;
    }
}
