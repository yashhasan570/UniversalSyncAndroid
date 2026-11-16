package com.nutomic.universalSyncandroid.settings

import android.content.ComponentName
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.lifecycleScope
import com.nutomic.universalSyncandroid.UniversalSyncApp
import com.nutomic.universalSyncandroid.activities.UniversalSyncActivity
import com.nutomic.universalSyncandroid.service.NotificationHandler
import com.nutomic.universalSyncandroid.service.UniversalSyncService
import com.nutomic.universalSyncandroid.theme.ApplicationTheme
import com.nutomic.universalSyncandroid.util.LocalActivityScope
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import me.zhanghai.compose.preference.Preferences
import me.zhanghai.compose.preference.ProvidePreferenceLocals

val LocalUniversalSyncService = staticCompositionLocalOf<UniversalSyncService?> { null }
val LocalServiceUpdateTick = staticCompositionLocalOf { 0 }

class SettingsActivity : UniversalSyncActivity() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    lateinit var prefFlow: MutableStateFlow<Preferences>
    @Inject
    lateinit var notificationHandler: NotificationHandler

    private var universalSyncServiceState by mutableStateOf<UniversalSyncService?>(service)

    // The ticker will help update the ui whenever the state of universal sync service updates
    private var serviceUpdateTick by mutableIntStateOf(0)
    val stateChangeListener = UniversalSyncService.OnServiceStateChangeListener {
        serviceUpdateTick++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as UniversalSyncApp).component().inject(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val activityScope = this.lifecycleScope
        prefFlow = createPreferenceFlow(sharedPreferences, activityScope)

        val routeStr = intent.getStringExtra(EXTRA_START_DESTINATION)
        val startDestination: SettingsRoute = SettingsRoute.fromString(routeStr)

        setContent {
            val backStack = rememberSettingsNavBackStack(startDestination)
            val navigator = remember(backStack) {
                object : Navigator<SettingsRoute> {
                    override fun navigateTo(route: SettingsRoute) {
                        backStack.add(route)
                    }
                    override fun navigateBack() {
                        if (backStack.size == 1) {
                            finish()
                        } else {
                            backStack.removeLastOrNull()
                        }
                    }
                    override fun navigateUp() {
                        finish()
                    }
                }
            }

            ApplicationTheme {
                CompositionLocalProvider(
                    LocalActivityScope provides activityScope,
                    LocalSettingsNavigator provides navigator,
                    LocalUniversalSyncService provides universalSyncServiceState,
                    LocalServiceUpdateTick provides serviceUpdateTick,
                ) {
                    ProvidePreferenceLocals(flow = prefFlow) {
                        SettingsNavDisplay(
                            backStack = backStack,
                        )
                    }
                }
            }
        }
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        super.onServiceConnected(name, service)
        universalSyncServiceState = this.service
        universalSyncServiceState?.registerOnServiceStateChangeListener(stateChangeListener)
        serviceUpdateTick++
    }

    override fun onServiceDisconnected(name: ComponentName) {
        universalSyncServiceState?.unregisterOnServiceStateChangeListener(stateChangeListener)
        super.onServiceDisconnected(name)
        universalSyncServiceState = null
        serviceUpdateTick++
    }

    override fun onStop() {
        universalSyncServiceState?.let {
            notificationHandler.updatePersistentNotification(it)
        }
        super.onStop()
    }

    override fun onDestroy() {
        universalSyncServiceState?.unregisterOnServiceStateChangeListener(stateChangeListener)
        super.onDestroy()
    }

    companion object {
        const val EXTRA_START_DESTINATION = "com.nutomic.universalSyncandroid.settings.EXTRA_START_DESTINATION"
    }
}
