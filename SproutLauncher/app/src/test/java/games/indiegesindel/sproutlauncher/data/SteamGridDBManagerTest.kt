package games.indiegesindel.sproutlauncher.data

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class SteamGridDBManagerTest {
    private lateinit var context: Context
    private lateinit var settings: SettingsManager
    private lateinit var manager: SteamGridDBManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("sprout_launcher_settings", Context.MODE_PRIVATE).edit().clear().commit()
        settings = SettingsManager(context)
        manager = SteamGridDBManager(context)
    }

    @Test
    fun search_with_no_api_key() = runTest(UnconfinedTestDispatcher()) {
        val response = manager.search("portal")

        assert(response.isEmpty())
    }

    @Test
    fun search_with_wrong_api_key() = runTest(UnconfinedTestDispatcher()) {
        settings.setSteamGridDBApiKey("wrong_key")
        val response = manager.search("portal")

        assert(response.isEmpty())
    }

    @Test
    fun search_with_api_key() = runTest(UnconfinedTestDispatcher()) {
        val apiKey = System.getenv("STEAMGRIDDB_API_KEY") ?: error("STEAMGRIDDB_API_KEY env var not set")
        settings.setSteamGridDBApiKey(apiKey)

        val response = manager.search("portal")

        assert(response.isNotEmpty())
        assert(response.any { it.name.contains("Portal") })
        assert(response.any { it.id == 9022 })
    }

    @Test
    fun search_unsuccessful_with_api_key() = runTest(UnconfinedTestDispatcher()) {
        val apiKey = System.getenv("STEAMGRIDDB_API_KEY") ?: error("STEAMGRIDDB_API_KEY env var not set")
        settings.setSteamGridDBApiKey(apiKey)

        val response = manager.search("thisshouldnotexistandresultinanemptyresponse")

        assert(response.isEmpty())
    }

    @Test
    fun grids_with_no_api_key() = runTest(UnconfinedTestDispatcher()) {
        val response = manager.getGridsByGameId(9022) // 9022 -> Portal

        assert(response.isEmpty())
    }

    @Test
    fun grids_with_wrong_api_key() = runTest(UnconfinedTestDispatcher()) {
        settings.setSteamGridDBApiKey("wrong_key")
        val response = manager.getGridsByGameId(9022) // 9022 -> Portal

        assert(response.isEmpty())
    }

    @Test
    fun grids_with_api_key() = runTest(UnconfinedTestDispatcher()) {
        val apiKey = System.getenv("STEAMGRIDDB_API_KEY") ?: error("STEAMGRIDDB_API_KEY env var not set")
        settings.setSteamGridDBApiKey(apiKey)

        val response = manager.getGridsByGameId(9022) // 9022 -> Portal

        assert(response.isNotEmpty())
        assert(response.any { it.id == 479951 })
        assert(response.all { it.url.startsWith("https://") })
        assert(response.all { it.thumb.startsWith("https://") })
    }

    @Test
    fun grids_with_api_key_game_not_found() = runTest(UnconfinedTestDispatcher()) {
        val apiKey = System.getenv("STEAMGRIDDB_API_KEY") ?: error("STEAMGRIDDB_API_KEY env var not set")
        settings.setSteamGridDBApiKey(apiKey)

        val response = manager.getGridsByGameId(123456789)

        assert(response.isEmpty())
    }
}
