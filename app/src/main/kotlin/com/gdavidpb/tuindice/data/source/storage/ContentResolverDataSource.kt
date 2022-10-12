package com.gdavidpb.tuindice.data.source.storage

import android.content.ContentResolver
import android.net.Uri
import com.gdavidpb.tuindice.base.domain.repository.ContentRepository
import java.io.InputStream

@Suppress("BlockingMethodInNonBlockingContext")
open class ContentResolverDataSource(
        private val contentResolver: ContentResolver
) : ContentRepository {
    override suspend fun openInputStream(url: Uri): InputStream {
        return contentResolver.openInputStream(url) ?: throw RuntimeException("openInputStream")
    }
}