package com.gdavidpb.tuindice.enrollmentproof.data.contract

interface DatabaseDataSource {
	suspend fun getCurrentQuarterName(): String?
}