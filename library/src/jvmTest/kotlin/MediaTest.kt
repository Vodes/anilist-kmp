import kotlinx.coroutines.runBlocking
import pw.vodes.anilistkmp.AnilistApiClient
import pw.vodes.anilistkmp.ext.searchMedia
import pw.vodes.anilistkmp.graphql.type.MediaType
import kotlin.test.Test
import kotlin.test.assertTrue

class MediaTest {

    private val client by lazy { AnilistApiClient() }

    @Test
    fun testMediaSearch(): Unit = runBlocking {
        val response = client.searchMedia("jujutsu", type = MediaType.ANIME)
        assertTrue { response.data.firstOrNull()?.title?.english?.lowercase() == "jujutsu kaisen" }
    }

    @Test
    fun testSearchMultipleIDs(): Unit = runBlocking {
        val response = client.searchMedia(idIn = listOf(1, 21))
        val names = listOf("cowboy bebop", "one piece")

        assertTrue { response.data.isNotEmpty() }
        for (entry in response.data) {
            assertTrue { entry.title?.english?.lowercase() in names }
        }
    }
}