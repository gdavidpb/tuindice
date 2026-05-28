package com.gdavidpb.tuindice.base.domain.repository

interface ApplicationRepository : FileRepository {
	suspend fun clearData()
}
