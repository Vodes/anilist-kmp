package pw.vodes.anilistkmp

import com.apollographql.apollo.api.Error
import com.apollographql.apollo.exception.ApolloException

data class ApolloResponse<T>(val data: T, val pageData: PageData? = null, val exception: ApolloException? = null, val errors: List<Error>? = null)

data class PageData(val currentPage: Int, val total: Int, val hasNextPage: Boolean)