package com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source

import com.gdavidpb.tuindice.base.domain.model.Quarter

interface LocalDataSource {
	suspend fun getCurrentQuarterName(uid: String): String?
}