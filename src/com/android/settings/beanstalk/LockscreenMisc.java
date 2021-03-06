/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.android.settings.beanstalk;

import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.preference.PreferenceGroup;
import android.content.res.Resources;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import com.android.settings.cyanogenmod.ButtonSettings;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class LockscreenMisc extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String KEY_ALLOW_ROTATION = "allow_rotation";
    private static final String KEY_SEE_TRHOUGH = "see_through";
    private static final String KEY_BLUR_BEHIND = "blur_behind";
    private static final String KEY_BLUR_RADIUS = "blur_radius";
    private static final String KEY_BATTERY_STATUS = "lockscreen_battery_status";
    private static final String BATTERY_AROUND_LOCKSCREEN_RING = "battery_around_lockscreen_ring";
    private static final String HOME_UNLOCK_SCREEN = "home_unlock_screen";
    private static final String MENU_UNLOCK_SCREEN = "menu_unlock_screen";
    private static final String CAMERA_UNLOCK_SCREEN = "camera_unlock_screen";
    private static final String KEY_ENABLE_POWER_MENU = "lockscreen_enable_power_menu";

    private CheckBoxPreference mSeeThrough;
    private ListPreference mBatteryStatus;
    private CheckBoxPreference mAllowRotation;
    private CheckBoxPreference mBlurBehind;
    private SeekBarPreference mBlurRadius;

    private CheckBoxPreference mLockRingBattery;
    private CheckBoxPreference mHomeUnlock;
    private CheckBoxPreference mMenuUnlock;
    private CheckBoxPreference mCameraUnlock;
    private CheckBoxPreference mEnablePowerMenu;

    public boolean hasButtons() {
        return !getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_interface_misc);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
	final int deviceKeys = res.getInteger(
                    com.android.internal.R.integer.config_deviceHardwareKeys);

        mSeeThrough = (CheckBoxPreference) findPreference(KEY_SEE_TRHOUGH);
	mSeeThrough.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_SEE_THROUGH, 0) == 1);

        mAllowRotation = (CheckBoxPreference) findPreference(KEY_ALLOW_ROTATION);
        mAllowRotation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_ROTATION, 0) == 1); 

	mBatteryStatus = (ListPreference) findPreference(KEY_BATTERY_STATUS);
        if (mBatteryStatus != null) {
            mBatteryStatus.setOnPreferenceChangeListener(this);
        }

        mLockRingBattery = (CheckBoxPreference) findPreference(BATTERY_AROUND_LOCKSCREEN_RING);
        mLockRingBattery.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, 0) == 1); 

        mHomeUnlock = (CheckBoxPreference) findPreference(HOME_UNLOCK_SCREEN);
        mHomeUnlock.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.HOME_UNLOCK_SCREEN, 0) == 1); 

        mMenuUnlock = (CheckBoxPreference) findPreference(MENU_UNLOCK_SCREEN);
        mMenuUnlock.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.MENU_UNLOCK_SCREEN, 0) == 1); 

        mCameraUnlock = (CheckBoxPreference) findPreference(CAMERA_UNLOCK_SCREEN);
        mCameraUnlock.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.CAMERA_UNLOCK_SCREEN, 0) == 1);

        mEnablePowerMenu = (CheckBoxPreference) findPreference(KEY_ENABLE_POWER_MENU);
        mEnablePowerMenu.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_ENABLE_POWER_MENU, 0) == 1);

        mBlurBehind = (CheckBoxPreference) findPreference(KEY_BLUR_BEHIND);
        mBlurBehind.setChecked(Settings.System.getInt(getContentResolver(), 
            Settings.System.LOCKSCREEN_BLUR_BEHIND, 0) == 1);
        mBlurRadius = (SeekBarPreference) findPreference(KEY_BLUR_RADIUS);
        mBlurRadius.setProgress(Settings.System.getInt(getContentResolver(), 
            Settings.System.LOCKSCREEN_BLUR_RADIUS, 12));
        mBlurRadius.setOnPreferenceChangeListener(this);

	// remove glowpad torch is torch not supported
        if (!hasButtons()) {
            prefScreen.removePreference(mHomeUnlock);
        }

	// Remove lockscreen button actions if device doesn't have hardware keys
        if (!hasButtons()) {
            prefScreen.removePreference(mMenuUnlock);
        }

	// Hide the CameraUnlock setting if no camera button is available
        if ((deviceKeys & ButtonSettings.KEY_MASK_CAMERA) == 0) {
            prefScreen.removePreference(mCameraUnlock);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

	// Update battery status
        if (mBatteryStatus != null) {
            ContentResolver cr = getActivity().getContentResolver();
            int batteryStatus = Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_BATTERY_VISIBILITY, 0);
            mBatteryStatus.setValueIndex(batteryStatus);
            mBatteryStatus.setSummary(mBatteryStatus.getEntries()[batteryStatus]);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSeeThrough) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_SEE_THROUGH, mSeeThrough.isChecked()
                    ? 1 : 0);
            return true;

        } else if (preference == mAllowRotation) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_ROTATION, mAllowRotation.isChecked()
                    ? 1 : 0);
            return true;

        } else if (preference == mEnablePowerMenu) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_ENABLE_POWER_MENU, mEnablePowerMenu.isChecked()
                    ? 1 : 0);
            return true;

        } else if (preference == mLockRingBattery) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.BATTERY_AROUND_LOCKSCREEN_RING, mLockRingBattery.isChecked()
                    ? 1 : 0);
            return true;

        } else if (preference == mHomeUnlock) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.HOME_UNLOCK_SCREEN, mHomeUnlock.isChecked()
                    ? 1 : 0);
            return true;

        } else if (preference == mMenuUnlock) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MENU_UNLOCK_SCREEN, mMenuUnlock.isChecked()
                    ? 1 : 0);
            return true;

        } else if (preference == mCameraUnlock) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CAMERA_UNLOCK_SCREEN, mMenuUnlock.isChecked()
                    ? 1 : 0);
            return true;

        } else if (preference == mBlurBehind) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_BLUR_BEHIND,
                    mBlurBehind.isChecked() ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
	ContentResolver cr = getActivity().getContentResolver();
        if (preference == mBlurRadius) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BLUR_RADIUS, (Integer)objValue);
            return true;
	} else if (preference == mBatteryStatus) {
            int value = Integer.valueOf((String) objValue);
            int index = mBatteryStatus.findIndexOfValue((String) objValue);
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_BATTERY_VISIBILITY, value);
            mBatteryStatus.setSummary(mBatteryStatus.getEntries()[index]);
            return true;
        }

         return false;
    }

    public static class DeviceAdminLockscreenReceiver extends DeviceAdminReceiver {}

}
