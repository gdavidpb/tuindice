package com.gdavidpb.tuindice.enrollmentproof.data.repository

interface DatabaseDataSource {
	suspend fun getCurrentQuarterName(uid: String): String?
}