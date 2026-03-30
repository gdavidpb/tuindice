package com.gdavidpb.tuindice.enrollmentproof.data.source

interface DatabaseDataSource {
	suspend fun getCurrentQuarterName(): String?
}