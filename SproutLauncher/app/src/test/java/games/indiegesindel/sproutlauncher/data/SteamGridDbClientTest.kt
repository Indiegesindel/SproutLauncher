package games.indiegesindel.sproutlauncher.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assume.assumeTrue
import org.junit.Test

class SteamGridDbClientTest {
    private val liveApiKey: String? = System.getProperty("steamgriddb.apiKey")?.takeIf { it.isNotBlank() }
    private val httpClient = OkHttpClient()

    private fun client(apiKey: String?) = SteamGridDbClient(httpClient, { apiKey }, Dispatchers.IO)

    private fun requireLiveKey(): String {
        assumeTrue("steamgriddb.apiKey not configured; skipping live test", liveApiKey != null)
        return liveApiKey!!
    }

    @Test
    fun search_withoutApiKey_failsWithoutNetwork() = runBlocking {
        val result = client(apiKey = null).search("portal")
        assertThat(result.reason()).isEqualTo(SteamGridDbException.Reason.MISSING_API_KEY)
    }

    @Test
    fun grids_withBlankApiKey_failsWithoutNetwork() = runBlocking {
        val result = client(apiKey = "   ").gridsForGame(PORTAL_ID)
        assertThat(result.reason()).isEqualTo(SteamGridDbException.Reason.MISSING_API_KEY)
    }

    @Test
    fun live_search_withInvalidKey_isUnauthorized() = runBlocking {
        requireLiveKey()
        val result = client(apiKey = "invalid_key").search("portal")
        assertThat(result.reason()).isEqualTo(SteamGridDbException.Reason.UNAUTHORIZED)
    }

    @Test
    fun live_search_findsGame() = runBlocking {
        val games = client(requireLiveKey()).search("portal").getOrThrow()
        assertThat(games.any { it.id == PORTAL_ID }).isTrue()
        assertThat(games.any { it.name.orEmpty().contains("Portal") }).isTrue()
    }

    @Test
    fun live_search_withoutMatches_isEmpty() = runBlocking {
        val games = client(requireLiveKey()).search("thisshouldnotexistandresultinanemptyresponse").getOrThrow()
        assertThat(games).isEmpty()
    }

    @Test
    fun live_grids_returnHttpsUrls() = runBlocking {
        val grids = client(requireLiveKey()).gridsForGame(PORTAL_ID).getOrThrow()
        assertThat(grids).isNotEmpty()
        assertThat(grids.all { it.url.orEmpty().startsWith("https://") }).isTrue()
        assertThat(grids.all { it.thumb.orEmpty().startsWith("https://") }).isTrue()
    }

    @Test
    fun live_grids_forUnknownGame_isEmpty() = runBlocking {
        val grids = client(requireLiveKey()).gridsForGame(123456789).getOrThrow()
        assertThat(grids).isEmpty()
    }

    private fun Result<*>.reason() = (exceptionOrNull() as? SteamGridDbException)?.reason

    private companion object {
        const val PORTAL_ID = 9022
    }
}
