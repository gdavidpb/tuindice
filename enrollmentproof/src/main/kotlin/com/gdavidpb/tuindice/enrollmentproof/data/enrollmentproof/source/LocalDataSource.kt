package com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source

interface LocalDataSource {
	suspend fun getCurrentQuarterName(uid: String): String?
}