package pw.vodes.anilistkmp

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.normalizedCache

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
}