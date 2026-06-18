package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.base.domain.model.SyncReport
import io.ktor.http.HttpStatusCode

class SyncRemoteException(
	val statusCode: HttpStatusCode,
	val syncReport: SyncReport?,
	cause: Throwable
) : RuntimeException(cause)
