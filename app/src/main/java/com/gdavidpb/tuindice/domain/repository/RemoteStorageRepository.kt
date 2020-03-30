package com.gdavidpb.tuindice.domain.repository

import android.net.Uri
import java.io.InputStream

interface RemoteStorageRepository {
    suspend fun resolveResource(resource: String): Uri
    suspend fun uploadResource(resource: String, stream: InputStream): Uri
}