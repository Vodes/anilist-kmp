package pw.vodes.anilistkmp.ext

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import pw.vodes.anilistkmp.AnilistApiClient
import pw.vodes.anilistkmp.ApolloResponse
import pw.vodes.anilistkmp.MediaListEntry
import pw.vodes.anilistkmp.PageData
import pw.vodes.anilistkmp.graphql.UserMediaListQuery
import pw.vodes.anilistkmp.graphql.UserQuery
import pw.vodes.anilistkmp.graphql.UserSearchQuery
import pw.vodes.anilistkmp.graphql.ViewerQuery
import pw.vodes.anilistkmp.graphql.fragment.User
import pw.vodes.anilistkmp.graphql.type.MediaListSort
import pw.vodes.anilistkmp.graphql.type.MediaListStatus
import pw.vodes.anilistkmp.graphql.type.MediaType
import pw.vodes.anilistkmp.graphql.type.UserSort

/**
 * Search users with various criteria.
 *
 * @param search Username
 * @param sort One or many criteria to sort by. See [pw.vodes.anilistkmp.graphql.type.UserSort]
 * @param isModerator If the user has to be an Anilist Moderator
 * @param page The page to return
 * @param perPage The amount of users fetched per page. Defaults to 50 in most queries.
 * @param configure A function to customize the query behavior. May include a fetchPolicy by default.
 *
 * @return [pw.vodes.anilistkmp.ApolloResponse] with a List of Users if any.
 */
suspend fun AnilistApiClient.searchUser(
    search: String? = null,
    sort: List<UserSort>? = null,
    isModerator: Boolean? = null,
    page: Int? = null,
    perPage: Int? = null,
    configure: ApolloCall<UserSearchQuery.Data>.() -> Unit = { fetchPolicy(FetchPolicy.CacheFirst) },
): ApolloResponse<List<User>> {
    val query = UserSearchQuery.Builder().apply {
        search?.let { search(it) }
        sort?.let { sort(it) }
        isModerator?.let { isModerator(it) }
        page?.let { page(it) }
        perPage?.let { perPage(it) }
    }.build()
    val response = apolloClient.query(query).apply(configure).execute()
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
 * @param configure A function to customize the query behavior. May include a fetchPolicy by default.
 *
 * @return [pw.vodes.anilistkmp.ApolloResponse] with a User, can be null if none found.
 */
suspend fun AnilistApiClient.fetchUserByID(
    id: Int,
    configure: ApolloCall<UserQuery.Data>.() -> Unit = { fetchPolicy(FetchPolicy.CacheFirst) },
): ApolloResponse<User?> {
    val query = UserQuery.Builder().id(id).build()
    val response = apolloClient.query(query).apply(configure).execute()
    val user = response.data?.user?.user
    return ApolloResponse(user, null, response.exception, response.errors)
}

/**
 * Fetch the currently authenticated user.
 *
 * @param configure A function to customize the query behavior. May include a fetchPolicy by default.
 *
 * @return User, can be null if not authenticated.
 */
suspend fun AnilistApiClient.fetchViewer(
    configure: ApolloCall<ViewerQuery.Data>.() -> Unit = { fetchPolicy(FetchPolicy.CacheFirst) }
): ApolloResponse<User?> {
    val response = apolloClient.query(ViewerQuery()).apply(configure).execute()
    val user = response.data?.viewer?.user
    return ApolloResponse(user, null, response.exception, response.errors)
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
 * @param configure A function to customize the query behavior. May include a fetchPolicy by default.
 *
 * @return [pw.vodes.anilistkmp.ApolloResponse] with a user's media list, can be empty if none found.
 */
suspend fun AnilistApiClient.fetchUserMediaList(
    userID: Int,
    type: MediaType = MediaType.ANIME,
    sort: List<MediaListSort>? = null,
    status: MediaListStatus? = null,
    mediaIdIn: List<Int>? = null,
    page: Int? = null,
    perPage: Int? = null,
    configure: ApolloCall<UserMediaListQuery.Data>.() -> Unit = { }
): ApolloResponse<List<MediaListEntry>> {
    val query = UserMediaListQuery.Builder().userId(userID).type(type).apply {
        sort?.let { sort(it) }
        status?.let { status(it) }
        mediaIdIn?.let { mediaIdIn(it) }
        page?.let { page(it) }
        perPage?.let { perPage(it) }
    }.build()
    val response = apolloClient.query(query).apply(configure).execute()
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