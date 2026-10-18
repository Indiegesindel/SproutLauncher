package games.indiegesindel.sproutlauncher.data

import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import android.util.Log
import games.indiegesindel.sproutlauncher.model.InstalledApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InstalledAppsRepository(
    private val launcherApps: LauncherApps,
    private val scope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher
) {
    private val _apps = MutableStateFlow<List<InstalledApp>?>(null)
    val apps: StateFlow<List<InstalledApp>?> = _apps.asStateFlow()

    private val removalListeners = mutableListOf<(String) -> Unit>()
    private val changeListeners = mutableListOf<() -> Unit>()
    private var refreshJob: Job? = null

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(packageName: String, user: UserHandle) {
            if (user != Process.myUserHandle()) return
            removalListeners.forEach { it(packageName) }
            onPackagesChanged()
        }

        override fun onPackageAdded(packageName: String, user: UserHandle) = onPackagesChanged()
        override fun onPackageChanged(packageName: String, user: UserHandle) = onPackagesChanged()

        override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) =
            onPackagesChanged()

        override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) =
            onPackagesChanged()
    }

    fun start() {
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        refresh()
    }

    fun addPackageRemovedListener(listener: (String) -> Unit) {
        removalListeners += listener
    }

    fun addPackagesChangedListener(listener: () -> Unit) {
        changeListeners += listener
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch(ioDispatcher) {
            try {
                _apps.value = launcherApps.getActivityList(null, Process.myUserHandle())
                    .map {
                        InstalledApp(
                            packageName = it.componentName.packageName,
                            activityName = it.componentName.className,
                            label = it.label.toString()
                        )
                    }
                    .sortedBy { it.label.lowercase() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to query launcher activities", e)
                if (_apps.value == null) _apps.value = emptyList()
            }
        }
    }

    private fun onPackagesChanged() {
        changeListeners.forEach { it() }
        refresh()
    }

    private companion object {
        const val TAG = "InstalledAppsRepository"
    }
}
