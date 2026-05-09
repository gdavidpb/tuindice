package com.gdavidpb.tuindice.domain.repository

interface CoreCacheStateRepository {
	suspend fun requiresBaseRehydration(): Boolean
}
