package com.universalsync.android.onboarding.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.universalsync.android.R
import com.universalsync.android.onboarding.OnboardingIcon
import com.universalsync.android.onboarding.OnboardingScaffold
import com.universalsync.android.onboarding.OnboardingUiState

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
