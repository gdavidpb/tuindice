package com.gdavidpb.tuindice.data.source.sync.api.mapper

import com.gdavidpb.tuindice.data.model.SyncResult
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncResponse
import com.gdavidpb.tuindice.record.data.source.api.mapper.toVersionedAcademicRecord
import com.gdavidpb.tuindice.summary.data.mapper.toUser

fun SyncResponse.toSyncResult(): SyncResult {
	return SyncResult(
		record = record.toVersionedAcademicRecord(),
		user = user.toUser()
	)
}
