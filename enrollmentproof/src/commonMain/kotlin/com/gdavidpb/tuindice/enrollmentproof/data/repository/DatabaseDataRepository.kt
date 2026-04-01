package com.gdavidpb.tuindice.enrollmentproof.data.repository

interface DatabaseDataRepository {
	suspend fun getCurrentQuarterName(): String?
}