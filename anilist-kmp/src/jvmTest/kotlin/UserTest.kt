import kotlinx.coroutines.runBlocking
import pw.vodes.anilistkmp.AnilistApiClient
import pw.vodes.anilistkmp.ext.fetchUserMediaList
import pw.vodes.anilistkmp.ext.searchUser
import kotlin.test.Test
import kotlin.test.assertTrue

class UserTest {
    private val client by lazy { AnilistApiClient() }

    @Test
    fun testUserSearch(): Unit = runBlocking {
        val response = client.searchUser("Vodes")
        assertTrue { response.data.isNotEmpty() }
        assertTrue { response.data.first().name == "Vodes" }
    }

    @Test
    fun testUserCollectionFetch(): Unit = runBlocking {
        val response = client.fetchUserMediaList(545503, mediaIdIn = listOf(21))
        assertTrue { response.data.isNotEmpty() }
    }
}