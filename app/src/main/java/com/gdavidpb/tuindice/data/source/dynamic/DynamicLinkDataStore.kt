package com.gdavidpb.tuindice.data.source.dynamic

import androidx.core.net.toUri
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.repository.LinkRepository
import com.gdavidpb.tuindice.utils.extensions.await
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

open class DynamicLinkDataStore(
        private val dynamicLinks: FirebaseDynamicLinks
) : LinkRepository {
    override suspend fun resolveLink(data: String): String {
        val uri = data.toUri()
        val host = uri.host

        if (host != BuildConfig.URL_APP) throw IllegalArgumentException("host: $data")

        val link = dynamicLinks.getDynamicLink(uri).await().link

        return link?.toString() ?: throw IllegalArgumentException("link: '$data'")
    }
}