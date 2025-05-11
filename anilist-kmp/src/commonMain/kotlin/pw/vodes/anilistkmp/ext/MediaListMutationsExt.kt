package pw.vodes.anilistkmp.ext

import com.apollographql.apollo.ApolloCall
import pw.vodes.anilistkmp.AnilistApiClient
import pw.vodes.anilistkmp.ApolloResponse
import pw.vodes.anilistkmp.graphql.DeleteMediaListEntryMutation
import pw.vodes.anilistkmp.graphql.SaveMediaListEntryMutation
import pw.vodes.anilistkmp.graphql.type.MediaListStatus

/**
 * Save a media list entry.
 *
 * For a new entry you need mediaId and for existing ones you need the regular id.
 *
 * @param id The ID of the list entry.
 * @param mediaId The ID of the media for that entry.
 * @param status Status of the entry.
 * @param score Score of the entry.
 * @param progress Progress of the entry.
 * @param notes Notes on the entry.
 * @param hiddenFromStatusLists If the entry should be hidden on status lists.
 * @param configure A function to customize the query behavior. May include a fetchPolicy by default.
 *
 * @return [pw.vodes.anilistkmp.ApolloResponse] with the updated/created media list entry.
 */
suspend fun AnilistApiClient.saveMediaListEntry(
    id: Int?,
    mediaId: Int? = null,
    status: MediaListStatus? = null,
    score: Double? = null,
    progress: Int? = null,
    notes: String? = null,
    hiddenFromStatusLists: Boolean? = null,
    configure: ApolloCall<SaveMediaListEntryMutation.Data>.() -> Unit = { }
): ApolloResponse<SaveMediaListEntryMutation.SaveMediaListEntry?> {
    val mutation = SaveMediaListEntryMutation.Builder().apply {
        id?.let { id(it) }
        mediaId?.let { mediaId(it) }
        status?.let { status(it) }
        score?.let { score(it) }
        progress?.let { progress(it) }
        notes?.let { notes(it) }
        hiddenFromStatusLists?.let { hiddenFromStatusLists(it) }
    }.build()
    val response = apolloClient.mutation(mutation).apply(configure).execute()
    return ApolloResponse(response.data?.SaveMediaListEntry, null, response.exception, response.errors)
}

/**
 * Delete a media list entry.
 *
 * @param id ID of the media list entry to delete.
 * @param configure A function to customize the query behavior. May include a fetchPolicy by default.
 *
 * @return [pw.vodes.anilistkmp.ApolloResponse] with a boolean indicating if an entry was deleted.
 */
suspend fun AnilistApiClient.deleteMediaListEntries(
    id: Int,
    configure: ApolloCall<DeleteMediaListEntryMutation.Data>.() -> Unit = { }
): ApolloResponse<Boolean?> {
    val mutation = DeleteMediaListEntryMutation.Builder().id(id).build()
    val response = apolloClient.mutation(mutation).apply(configure).execute()
    return ApolloResponse(response.data?.DeleteMediaListEntry?.deleted, null, response.exception, response.errors)
}