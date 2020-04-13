package com.gdavidpb.tuindice.data.source.storage

import android.content.ContentResolver
import android.net.Uri
import com.gdavidpb.tuindice.domain.repository.ContentRepository
import java.io.InputStream

open class ContentResolverDataStore(
        private val contentResolver: ContentResolver
) : ContentRepository {
    override suspend fun openInputStream(url: Uri): InputStream {
        return contentResolver.openInputStream(url) ?: throw RuntimeException("openInputStream")
    }
}