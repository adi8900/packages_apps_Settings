/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.settings.accounts;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.UserInfo;
import android.os.UserManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.TestConfig;
import com.android.settings.testutils.SettingsRobolectricTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;
import java.util.List;

@RunWith(SettingsRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION_O)
public class AutoSyncDataPreferenceControllerTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private PreferenceScreen mScreen;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private UserManager mUserManager;
    @Mock
    private Fragment mFragment;

    private Preference mPreference;
    private Context mContext;
    private AutoSyncDataPreferenceController mController;
    private AutoSyncDataPreferenceController.ConfirmAutoSyncChangeFragment mConfirmSyncFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ShadowApplication shadowContext = ShadowApplication.getInstance();
        shadowContext.setSystemService(Context.USER_SERVICE, mUserManager);
        mContext = shadowContext.getApplicationContext();
        mController = new AutoSyncDataPreferenceController(mContext, mFragment);
        mConfirmSyncFragment = new AutoSyncDataPreferenceController.ConfirmAutoSyncChangeFragment();
        mConfirmSyncFragment.setTargetFragment(mFragment, 0);
        mPreference = new Preference(mContext);
        mPreference.setKey(mController.getPreferenceKey());
        when(mScreen.findPreference(mPreference.getKey())).thenReturn(mPreference);
    }

    @Test
    public void displayPref_managedProfile_shouldNotDisplay() {
        when(mUserManager.isManagedProfile()).thenReturn(true);

        mController.displayPreference(mScreen);

        assertThat(mPreference.isVisible()).isFalse();
    }

    @Test
    public void displayPref_linkedUser_shouldDisplay() {
        when(mUserManager.isManagedProfile()).thenReturn(false);
        when(mUserManager.isLinkedUser()).thenReturn(true);

        mController.displayPreference(mScreen);

        assertThat(mPreference.isVisible()).isTrue();
    }

    @Test
    public void displayPref_oneProfile_shouldDisplay() {
        List<UserInfo> infos = new ArrayList<>();
        infos.add(new UserInfo(1, "user 1", 0));
        when(mUserManager.isManagedProfile()).thenReturn(false);
        when(mUserManager.isLinkedUser()).thenReturn(false);
        when(mUserManager.getProfiles(anyInt())).thenReturn(infos);

        mController.displayPreference(mScreen);

        assertThat(mPreference.isVisible()).isTrue();
    }

    @Test
    public void displayPref_moreThanOneProfile_shouldNotDisplay() {
        List<UserInfo> infos = new ArrayList<>();
        infos.add(new UserInfo(1, "user 1", 0));
        infos.add(new UserInfo(2, "user 2", 0));
        when(mUserManager.isManagedProfile()).thenReturn(false);
        when(mUserManager.isLinkedUser()).thenReturn(false);
        when(mUserManager.getProfiles(anyInt())).thenReturn(infos);

        mController.displayPreference(mScreen);

        assertThat(mPreference.isVisible()).isFalse();
    }

    @Test
    public void autoSyncData_shouldNotBeSetOnCancel() {
        final ShadowApplication application = ShadowApplication.getInstance();
        final Context context = application.getApplicationContext();
        final SwitchPreference preference = new SwitchPreference(context);
        preference.setChecked(false);
        mController = new AutoSyncDataPreferenceController(context, mFragment);
        mConfirmSyncFragment.mPreference = preference;
        mConfirmSyncFragment.mEnabling = true;

        mConfirmSyncFragment.onClick(null, DialogInterface.BUTTON_NEGATIVE);
        assertThat(preference.isChecked()).isFalse();
    }
}
