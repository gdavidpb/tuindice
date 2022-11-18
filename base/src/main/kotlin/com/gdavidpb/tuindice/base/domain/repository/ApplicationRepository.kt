package com.gdavidpb.tuindice.base.domain.repository

interface ApplicationRepository {
	suspend fun clearData()
}