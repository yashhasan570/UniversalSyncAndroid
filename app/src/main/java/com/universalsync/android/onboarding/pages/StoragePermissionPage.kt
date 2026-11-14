package com.universalsync.android.onboarding.pages

import androidx.activity.compose.LocalActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import com.universalsync.android.R
import com.universalsync.android.onboarding.OnboardingActivity.Companion.REQUEST_WRITE_STORAGE
import com.universalsync.android.onboarding.OnboardingIcon
import com.universalsync.android.onboarding.OnboardingScaffold
import com.universalsync.android.onboarding.OnboardingUiState
import com.universalsync.android.onboarding.PermissionButton
import com.universalsync.android.util.PermissionUtil

@Composable
fun StoragePermissionPage(
    uiState: OnboardingUiState,
    pageIndex: Int,
    requestTvFocus: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val activity = LocalActivity.current
    val actionFocusRequester = remember { FocusRequester() }

    OnboardingScaffold(
        icon = OnboardingIcon.Vector(Icons.Outlined.Storage),
        title = stringResource(R.string.storage_permission_title),
        description = stringResource(R.string.storage_permission_desc),
        pageIndex = pageIndex,
        pageCount = uiState.pages.size,
        nextLabel = stringResource(R.string.cont),
        nextEnabled = uiState.hasStoragePermission,
        requestTvFocus = requestTvFocus,
        onBack = onBack,
        onNext = onContinue,
        actionFocusRequester = actionFocusRequester,
        action = {
            PermissionButton(
                granted = uiState.hasStoragePermission,
                onClick = {
                    activity?.let {
                        PermissionUtil.requestStoragePermission(it, REQUEST_WRITE_STORAGE)
                    }
                },
                focusRequester = actionFocusRequester,
            )
        },
    )
}
