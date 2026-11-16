package com.nutomic.universalSyncandroid.onboarding.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nutomic.universalSyncandroid.R
import com.nutomic.universalSyncandroid.onboarding.OnboardingIcon
import com.nutomic.universalSyncandroid.onboarding.OnboardingScaffold
import com.nutomic.universalSyncandroid.onboarding.OnboardingUiState

@Composable
fun WelcomePage(
    uiState: OnboardingUiState,
    pageIndex: Int,
    requestTvFocus: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    OnboardingScaffold(
        icon = OnboardingIcon.Logo,
        title = stringResource(R.string.welcome_title),
        description = stringResource(R.string.welcome_text),
        pageIndex = pageIndex,
        pageCount = uiState.pages.size,
        canGoBack = false,
        backVisible = false,
        nextLabel = stringResource(R.string.cont),
        requestTvFocus = requestTvFocus,
        onBack = onBack,
        onNext = onContinue,
    )
}
