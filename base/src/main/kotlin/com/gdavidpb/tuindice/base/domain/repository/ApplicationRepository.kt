package com.gdavidpb.tuindice.base.domain.repository

interface ApplicationRepository {
	suspend fun canOpenFile(path: String): Boolean
	suspend fun clearData()
}