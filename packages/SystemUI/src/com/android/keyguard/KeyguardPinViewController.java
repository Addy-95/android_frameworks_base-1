/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.keyguard;

import static com.android.keyguard.KeyguardAbsKeyInputView.MINIMUM_PASSWORD_LENGTH_BEFORE_REPORT;

import android.os.UserHandle;
import android.provider.Settings;
import android.view.View;

import com.android.internal.util.LatencyTracker;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.internal.widget.LockscreenCredential;
import com.android.keyguard.PasswordTextView.QuickUnlockListener;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.systemui.R;
import com.android.systemui.classifier.FalsingCollector;

public class KeyguardPinViewController
        extends KeyguardPinBasedInputViewController<KeyguardPINView> {
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;

    private int userId = KeyguardUpdateMonitor.getCurrentUser();

    private LockPatternUtils mLockPatternUtils;

    protected KeyguardPinViewController(KeyguardPINView view,
            KeyguardUpdateMonitor keyguardUpdateMonitor,
            SecurityMode securityMode, LockPatternUtils lockPatternUtils,
            KeyguardSecurityCallback keyguardSecurityCallback,
            KeyguardMessageAreaController.Factory messageAreaControllerFactory,
            LatencyTracker latencyTracker, LiftToActivateListener liftToActivateListener,
            EmergencyButtonController emergencyButtonController,
            FalsingCollector falsingCollector) {
        super(view, keyguardUpdateMonitor, securityMode, lockPatternUtils, keyguardSecurityCallback,
                messageAreaControllerFactory, latencyTracker, liftToActivateListener,
                emergencyButtonController, falsingCollector);
        mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        mLockPatternUtils = lockPatternUtils;
    }

    @Override
    protected void onViewAttached() {
        super.onViewAttached();

        View cancelBtn = mView.findViewById(R.id.cancel_button);
        if (cancelBtn != null) {
            cancelBtn.setOnClickListener(view -> {
                getKeyguardSecurityCallback().reset();
                getKeyguardSecurityCallback().onCancelClicked();
            });
        }

        boolean quickUnlock = (Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, 0, UserHandle.USER_CURRENT) == 1);

        if (quickUnlock) {
            mPasswordEntry.setQuickUnlockListener(new QuickUnlockListener() {
                public void onValidateQuickUnlock(String password) {
                    validateQuickUnlock(password);
                }
            });
        } else {
            mPasswordEntry.setQuickUnlockListener(null);
        }
    }

    @Override
    public void reloadColors() {
        super.reloadColors();
        mView.reloadColors();
    }

    @Override
    void resetState() {
        super.resetState();
        mMessageAreaController.setMessage("");
    }

    @Override
    public boolean startDisappearAnimation(Runnable finishRunnable) {
        return mView.startDisappearAnimation(
                mKeyguardUpdateMonitor.needsSlowUnlockTransition(), finishRunnable);
    }

    private void validateQuickUnlock(String password) {
        if (password != null) {
            if (password.length() > MINIMUM_PASSWORD_LENGTH_BEFORE_REPORT
                    && kpvCheckPassword(password)) {
                mPasswordEntry.setEnabled(false);
                getKeyguardSecurityCallback().reportUnlockAttempt(userId, true, 0);
                getKeyguardSecurityCallback().dismiss(true, userId);
                mView.resetPasswordText(true, true);
            }
        }
    }

    private boolean kpvCheckPassword(String password) {
        try {
            return mLockPatternUtils.checkCredential(
                    LockscreenCredential.createPinOrNone(password), userId, null);
        } catch (RequestThrottledException ex) {
            return false;
        }
    }
}
