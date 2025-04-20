package pw.vodes.anilistkmp.ext

import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import pw.vodes.anilistkmp.AnilistApiClient
import pw.vodes.anilistkmp.ApolloResponse
import pw.vodes.anilistkmp.PageData
import pw.vodes.anilistkmp.graphql.MediaQuery
import pw.vodes.anilistkmp.graphql.SearchMediaQuery
import pw.vodes.anilistkmp.graphql.SearchMediaSmallQuery
import pw.vodes.anilistkmp.graphql.fragment.MediaBig
import pw.vodes.anilistkmp.graphql.fragment.MediaSmall
import pw.vodes.anilistkmp.graphql.type.*

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
suspend fun AnilistApiClient.searchMedia(
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
suspend fun AnilistApiClient.searchMediaSmall(
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
 * Fetch media by its ID.
 *
 * @param id ID of the media.
 * @param type Type of media
 * @return [pw.vodes.anilistkmp.ApolloResponse] with a MediaBig class if any.
 */
suspend fun AnilistApiClient.fetchMediaByID(id: Int, type: MediaType = MediaType.ANIME): ApolloResponse<MediaBig?> {
    val query = MediaQuery.Builder().id(id).type(type).build()
    val response = apolloClient.query(query).fetchPolicy(FetchPolicy.CacheFirst).execute()
    val media = response.data?.media?.mediaBig
    return ApolloResponse(media, null, response.exception, response.errors)
}