package com.nutomic.universalSyncandroid.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import com.nutomic.universalSyncandroid.R
import com.nutomic.universalSyncandroid.service.UniversalSyncService
import me.zhanghai.compose.preference.Preference

fun EntryProviderScope<SettingsRoute>.settingsRootEntry() {
    entry<SettingsRoute.Root> {
        SettingsRootScreen()
    }
}

@Composable
fun SettingsRootScreen() {
    val navigator = LocalSettingsNavigator.current
    val stService = LocalUniversalSyncService.current
    val stServiceTick = LocalServiceUpdateTick.current

    val isUniversalSyncOptionsEnabled by remember(stService, stServiceTick) {
        derivedStateOf { stService != null && stService.currentState == UniversalSyncService.State.ACTIVE }
    }

    SettingsScaffold(
        title = stringResource(R.string.settings_title),
    ) {
        item {
            Preference(
                title = { Text(stringResource(R.string.run_conditions_title)) },
                summary = { Text(stringResource(R.string.run_conditions_summary)) },
                onClick = { navigator.navigateTo(SettingsRoute.RunConditions) },
            )
        }
        item {
            Preference(
                title = { Text(stringResource(R.string.category_user_interface)) },
                onClick = { navigator.navigateTo(SettingsRoute.UserInterface) },
            )
        }
        item {
            Preference(
                title = { Text(stringResource(R.string.category_behaviour)) },
                onClick = { navigator.navigateTo(SettingsRoute.Behavior) },
            )
        }
        item {
            Preference(
                title = { Text(stringResource(R.string.category_universal sync_options)) },
                summary = { Text(stringResource(R.string.category_universal sync_options_summary)) },
                onClick = { navigator.navigateTo(SettingsRoute.UniversalSyncOptions) },
                enabled = isUniversalSyncOptionsEnabled,
            )
        }
        item {
            Preference(
                title = { Text(stringResource(R.string.category_backup)) },
                onClick = { navigator.navigateTo(SettingsRoute.ImportExport) },
            )
        }
        item {
            Preference(
                title = { Text(stringResource(R.string.category_debug)) },
                onClick = { navigator.navigateTo(SettingsRoute.Troubleshooting) },
            )
        }
        item {
            Preference(
                title = { Text(stringResource(R.string.category_experimental)) },
                onClick = { navigator.navigateTo(SettingsRoute.Experimental) },
            )
        }
        item {
            Preference(
                title = { Text(stringResource(R.string.category_about)) },
                onClick = { navigator.navigateTo(SettingsRoute.About) },
            )
        }
    }
}
