package pw.vodes.anilistkmp

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.normalizedCache
import pw.vodes.anilistkmp.graphql.*
import pw.vodes.anilistkmp.graphql.fragment.MediaBig
import pw.vodes.anilistkmp.graphql.fragment.MediaSmall
import pw.vodes.anilistkmp.graphql.fragment.User
import pw.vodes.anilistkmp.graphql.type.*

class AnilistApiClient(token: String? = null, memoryCacheSize: Int = 10, configure: ApolloClient.Builder.() -> Unit = {}) {
    private lateinit var cacheFactory: MemoryCacheFactory
    var apolloClient: ApolloClient
        private set

    init {
        val builder = ApolloClient.Builder().serverUrl("https://graphql.anilist.co")
        configure(builder)
        if (!token.isNullOrBlank())
            builder.addHttpHeader("Authorization", "Bearer $token")
        if (memoryCacheSize > 0) {
            cacheFactory = MemoryCacheFactory(memoryCacheSize * 1024 * 1024, 30000)
            builder.normalizedCache(cacheFactory).fetchPolicy(FetchPolicy.NetworkOnly)
        }
        apolloClient = builder.build()
    }

    /**
     * Search media with various criteria.
     *
     * @param search Any of the names the piece of media might have
     * @param type Type of media
     * @param sort One or many criteria to sort by
     * @param season The season this media was a part of
     * @param seasonYear The year of the aforementioned season
     * @param format The format this media must have
     * @param status The status this media currently has
     * @param formatIn The media must have one of these formats
     * @param sourceIn The media must be of these sources
     * @param idIn The media ID must be one of these
     * @param idNotIn The media ID must NOT be one of these
     * @param onList The media must be on the currently authenticated user's list.
     * @param page The page to return
     * @param perPage The amount of media fetched per page. Defaults to 50 in most queries.
     *
     * @return [pw.vodes.anilistkmp.ApolloResponse] with a list of MediaBig if any.
     */
    suspend fun searchMedia(
        search: String? = null,
        type: MediaType = MediaType.ANIME,
        sort: List<MediaSort>? = null,
        season: MediaSeason? = null,
        seasonYear: Int? = null,
        format: MediaFormat? = null,
        status: MediaStatus? = null,
        formatIn: List<MediaFormat>? = null,
        statusIn: List<MediaStatus>? = null,
        sourceIn: List<MediaSource>? = null,
        idIn: List<Int>? = null,
        idNotIn: List<Int>? = null,
        onList: Boolean? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): ApolloResponse<List<MediaBig>> {
        val query = SearchMediaQuery.Builder().type(type).apply {
            search?.let { search(it) }
            sort?.let { sort(it) }
            season?.let { season(it) }
            seasonYear?.let { seasonYear(it) }
            format?.let { format(it) }
            status?.let { status(it) }
            formatIn?.let { formatIn(it) }
            statusIn?.let { statusIn(it) }
            sourceIn?.let { sourceIn(it) }
            idIn?.let { idIn(it) }
            idNotIn?.let { idNotIn(it) }
            onList?.let { onList(it) }
            page?.let { page(it) }
            perPage?.let { perPage(it) }
        }.build()
        val response = apolloClient.query(query).fetchPolicy(FetchPolicy.CacheFirst).execute()
        val media = response.data?.Page?.media?.mapNotNull { it?.mediaBig } ?: emptyList()
        val pageData = response.data?.Page?.pageInfo?.let {
            PageData(it.currentPage ?: 0, it.total ?: 1, it.hasNextPage ?: false)
        }
        return ApolloResponse(media, pageData, response.exception, response.errors)
    }

    /**
     * Search media with various criteria.
     *
     * This returns a smaller subset of metadata and doesn't have pagination.
     *
     * @param search Any of the names the piece of media might have
     * @param type Type of media
     * @param sort One or many criteria to sort by
     * @param season The season this media was a part of
     * @param seasonYear The year of the aforementioned season
     * @param format The format this media must have
     * @param status The status this media currently has
     * @param formatIn The media must have one of these formats
     * @param sourceIn The media must be of these sources
     * @param idIn The media ID must be one of these
     * @param idNotIn The media ID must NOT be one of these
     * @param onList The media must be on the currently authenticated user's list.
     *
     * @return [pw.vodes.anilistkmp.ApolloResponse] with a list of MediaSmall if any.
     */
    suspend fun searchMediaSmall(
        search: String? = null,
        type: MediaType = MediaType.ANIME,
        sort: List<MediaSort>? = null,
        season: MediaSeason? = null,
        seasonYear: Int? = null,
        format: MediaFormat? = null,
        status: MediaStatus? = null,
        formatIn: List<MediaFormat>? = null,
        statusIn: List<MediaStatus>? = null,
        sourceIn: List<MediaSource>? = null,
        idIn: List<Int>? = null,
        idNotIn: List<Int>? = null,
        onList: Boolean? = null,
    ): ApolloResponse<List<MediaSmall>> {
        val query = SearchMediaSmallQuery.Builder().type(type).apply {
            search?.let { search(it) }
            sort?.let { sort(it) }
            season?.let { season(it) }
            seasonYear?.let { seasonYear(it) }
            format?.let { format(it) }
            status?.let { status(it) }
            formatIn?.let { formatIn(it) }
            statusIn?.let { statusIn(it) }
            sourceIn?.let { sourceIn(it) }
            idIn?.let { idIn(it) }
            idNotIn?.let { idNotIn(it) }
            onList?.let { onList(it) }
        }.build()
        val response = apolloClient.query(query).fetchPolicy(FetchPolicy.CacheFirst).execute()
        val media = response.data?.Page?.media?.mapNotNull { it?.mediaSmall } ?: emptyList()
        return ApolloResponse(media, null, response.exception, response.errors)
    }

    /**
     * Search users with various criteria.
     *
     * @param search Username
     * @param sort One or many criteria to sort by. See [pw.vodes.anilistkmp.graphql.type.UserSort]
     * @param isModerator If the user has to be an Anilist Moderator
     * @param page The page to return
     * @param perPage The amount of users fetched per page. Defaults to 50 in most queries.
     *
     * @return [pw.vodes.anilistkmp.ApolloResponse] with a List of Users if any.
     */
    suspend fun searchUser(
        search: String? = null,
        sort: List<UserSort>? = null,
        isModerator: Boolean? = null,
        page: Int? = null,
        perPage: Int? = null
    ): ApolloResponse<List<User>> {
        val query = UserSearchQuery.Builder().apply {
            search?.let { search(it) }
            sort?.let { sort(it) }
            isModerator?.let { isModerator(it) }
            page?.let { page(it) }
            perPage?.let { perPage(it) }
        }.build()
        val response = apolloClient.query(query).fetchPolicy(FetchPolicy.CacheFirst).execute()
        val users = response.data?.Page?.users?.mapNotNull { it?.user } ?: emptyList()
        val pageData = response.data?.Page?.pageInfo?.let {
            PageData(it.currentPage ?: 0, it.total ?: 1, it.hasNextPage ?: false)
        }
        return ApolloResponse(users, pageData, response.exception, response.errors)
    }

    /**
     * Fetch a user by their ID.
     *
     * @param id ID of the user.
     * @return [pw.vodes.anilistkmp.ApolloResponse] with a User, can be null if none found.
     */
    suspend fun fetchUserByID(id: Int): ApolloResponse<User?> {
        val query = UserQuery.Builder().id(id).build()
        val response = apolloClient.query(query).fetchPolicy(FetchPolicy.CacheFirst).execute()
        val user = response.data?.user?.user
        return ApolloResponse(user, null, response.exception, response.errors)
    }

    /**
     * Fetch the currently authenticated user.
     *
     * @return User, can be null if not authenticated.
     */
    suspend fun fetchViewer(): ApolloResponse<User?> {
        val response = apolloClient.query(ViewerQuery()).fetchPolicy(FetchPolicy.CacheFirst).execute()
        val user = response.data?.viewer?.user
        return ApolloResponse(user, null, response.exception, response.errors)
    }

    /**
     * Fetch media by its ID.
     *
     * @param id ID of the media.
     * @param type Type of media
     * @return [pw.vodes.anilistkmp.ApolloResponse] with a MediaBig class if any.
     */
    suspend fun fetchMediaByID(id: Int, type: MediaType = MediaType.ANIME): ApolloResponse<MediaBig?> {
        val query = MediaQuery.Builder().id(id).type(type).build()
        val response = apolloClient.query(query).fetchPolicy(FetchPolicy.CacheFirst).execute()
        val media = response.data?.media?.mediaBig
        return ApolloResponse(media, null, response.exception, response.errors)
    }


    /**
     * Fetch a users media list.
     *
     * @param userID ID of the user.
     * @param type Type of media to fetch.
     * @param sort One or many criteria to sort by. Sorts by STATUS and UPDATED_DESC by default.
     * @param status The status the entry must have on the list. For example PAUSED or CURRENT.
     * @param mediaIdIn The media ID must be one of these.
     * @param page The page to return
     * @param perPage The amount of users fetched per page. Defaults to 50 in most queries.
     */
    suspend fun fetchUserMediaList(
        userID: Int,
        type: MediaType = MediaType.ANIME,
        sort: List<MediaListSort>? = null,
        status: MediaListStatus? = null,
        mediaIdIn: List<Int>? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): ApolloResponse<List<MediaListEntry>> {
        val query = UserMediaListQuery.Builder().userId(userID).type(type).apply {
            sort?.let { sort(it) }
            status?.let { status(it) }
            mediaIdIn?.let { mediaIdIn(it) }
            page?.let { page(it) }
            perPage?.let { perPage(it) }
        }.build()
        val response = apolloClient.query(query).execute()
        val entries = response.data?.Page?.mediaList
            ?.mapNotNull { it?.commonMediaListEntry }
            ?.filter { it.media != null }
            ?.map { MediaListEntry(it.basicMediaListEntry, it.media!!.mediaSmall) }
            ?: emptyList()
        val pageData = response.data?.Page?.pageInfo?.let {
            PageData(it.currentPage ?: 0, it.total ?: 1, it.hasNextPage ?: false)
        }
        return ApolloResponse(entries, pageData, response.exception, response.errors)
    }
}