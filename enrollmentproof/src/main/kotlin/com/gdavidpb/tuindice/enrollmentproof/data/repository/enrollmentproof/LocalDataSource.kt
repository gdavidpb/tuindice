package com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof

interface LocalDataSource {
	suspend fun getCurrentQuarterName(uid: String): String?
}