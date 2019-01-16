package com.gdavidpb.tuindice.domain.repository

import android.net.Uri

interface RemoteStorageRepository {
    suspend fun resolveResource(resource: String): Uri
}