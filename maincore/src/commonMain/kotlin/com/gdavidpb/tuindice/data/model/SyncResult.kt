package com.gdavidpb.tuindice.data.model

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord

data class SyncResult(
	val record: VersionedAcademicRecord,
	val user: User
)
