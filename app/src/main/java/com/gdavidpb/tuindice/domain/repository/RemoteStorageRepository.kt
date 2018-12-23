package com.gdavidpb.tuindice.domain.repository

import android.net.Uri
import io.reactivex.Single

interface RemoteStorageRepository {
    fun resolveResource(resource: String): Single<Uri>
}